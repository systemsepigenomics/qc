package maModel; 

//import acemapMath.*;
import acemapQCCore.*;
import acemapCore.*;
import aceStyle.*;
import java.io.*;

/**
 *micro array signal variance distribution estimation class.
 *Uses an EM algorithm to improve parameters.
 *@author Sebastian Noth
 *@version 0.58
 */
public class VarEstimSig{
	private	double		logsig[], var[], logvar0[], logvar34[], varred[], sqrvar[], w1[], w2[], post[];
	
	private	double		d1M, d1S, lastLike;
	
	private final double	thr03 = 0.335;
	
	/**
	 *blend function
	 */
	private FSigmoid2p	fS_prob;
	//private FNeonex4p	f_m, f_s;
	/**
	 *sigmoid function to estimate variance-mean
	 */
	private FSigmoid3p	f_m;
	/**
	 *sigmoid function to estimate quadratic variance-variance
	 */
	private FSigmoid3p	f_s;
	/**
	 *source data
	 */
	private MTable		mTab;
	
	/**
	 *constructor using an MTable object. all data vectors are initialized
	 */
	public VarEstimSig(MTable mTab){
		this.mTab     = mTab;
		mTab.sortBySignal();
		double acc;
		int    i0, lend, im;
		d1M = 0.0;
		d1S = 1.0;
		
		var      = new double[mTab.size];
		logsig   = new double[mTab.size];
		logvar0  = new double[mTab.size];
		logvar34 = new double[mTab.size];
		sqrvar   = new double[mTab.size];
		w1       = new double[mTab.size];
		w2       = new double[mTab.size];
		varred   = new double[mTab.size];
		post     = new double[mTab.size];
		
		
		
		// calculate log signals and different log variances
		double tSig, tVar;
		for(int i=0; i<mTab.size; i++){
			tSig = mTab.data[i].signal;
			tVar = mTab.data[i].moq;
			
			/* AVOIDING NONPOSITIVE SIGNALS AND VARIANCES */
			if(tSig<=0.0) tSig = 0.00001;
			if(tVar< 0.0) tVar = -tVar;
			if(tVar==0.0){
				tVar = 0.001;
			}
			/**/
			
			logsig  [i] = Math.log(tSig);
			var     [i] = tVar;
			logvar0 [i] = Math.log(tVar  );
			varred  [i] = tVar - thr03;
			if(varred[i]>0.0)
				logvar34[i] = Math.log(varred[i]);
			else
				logvar34[i] = -100.0;
		}
		/*  OLD CODE
		for(int i=0; i<mTab.size; i++){
			logsig  [i] = Math.log(mTab.data[i].signal);
			var     [i] = (double) mTab.data[i].moq;
			logvar0 [i] = Math.log(mTab.data[i].moq   );
			varred  [i] = (double) mTab.data[i].moq - thr03;
			if(varred[i]>0.0)
				logvar34[i] = Math.log(varred[i]);
			else
				logvar34[i] = -100.0;
		}
		*/
			
		double um=0.0;
		int st = 0;
		while(logsig[st]<4.0 && st < logsig.length-1) st++;
		for(int i=st; i<logsig.length; i++)
			um += logvar0[i];
		um /= (double)(logsig.length-st);
		
		
		double uv=0.0;
		for(int i=st; i<logsig.length; i++)
			uv += SQR(logvar0[i]-um);
			
		uv /= (double)(logsig.length-st);	
				
		
		/*SIG
		double p0[] = new double[4];
		// init functions
		p0[0] =  3.0;
		p0[1] =  0.5;
		p0[2] =  0.5;
		p0[3] = -1.0;
		*/
		double p0[] = new double[3];
		// init functions
		p0[0] =  3.0;
		p0[1] =  0.0;
		p0[2] = -1.0;
		
		//SIGf_m = new FNeonex4p(logsig, logvar0, um, false);
		f_m = new FSigmoid3p(logsig, logvar0, um);
		
		f_m.setP(p0);
		//SIGf_m.setup();
		
		recalcSqrVar();
		
		//*DEB*/ System.out.println("uv="+uv);		
	
		p0[0] =  0.5;
		p0[1] =  0.0;
		p0[2] = -1.0;
		
		//SIGf_s = new FNeonex4p(logsig, sqrvar, uv, false);
		f_s = new FSigmoid3p(logsig, sqrvar, uv);
		f_s.setP(p0);
		//SIGf_s.setup();
		
		
		p0= new double[2];
		p0[0] =  0.0;
		p0[1] = -1.0;
		fS_prob = new FSigmoid2p(logsig, null);
		fS_prob.setP(p0);
		
		System.out.println("first weight calculation");
		recalcWeights();
		System.out.println("END first weight calculation");
		
		System.out.println("first D1 calculation");
		recalcD1();
		System.out.println("END first D1 calculation");
		
				
	}
	/**
	 *calculates probability of data element i of belonging to the first 
	 *(constant) distribution
	 */
	private	double		p1(int i){
		double p;
		p = fS_prob.f(logsig[i]);
		if(varred[i]<=0.0) return 0.0;
		double ret =  p / (varred[i]*d1S*Utils.sqrt2pi) * Math.exp( -SQR(logvar34[i] - d1M) / (2.0*d1S*d1S) );
		
		if(/*i<20 && */Double.isNaN(ret) ) System.out.println("NaN in p1 calc : red="+varred[i]+", d1S="+d1S+", p="+p+" l34="+logvar34[i]+", d1M="+d1M);
		
		return ret;
	}			// ok
	/**
	 *calculates probability of data element i of belonging to the second distribution
	 */
	 private double		p2(int i){
		double m, s, p;
		m = f_m.f(logsig[i]);
		s = f_s.f(logsig[i]);  if(s<=0.0) return 0.0;
		s = Math.sqrt(s);
		p = 1.0 - fS_prob.f(logsig[i]);
		double ret = p / (var[i]   * s *Utils.sqrt2pi) * Math.exp( -SQR(logvar0[i] -   m ) / (2.0* s * s ) );
		
		if(/*i<20 && */Double.isNaN(ret) ) System.out.println("NaN in p2 calc : var="+var[i]+", s="+s+", p="+p+" l="+logvar0[i]+", m="+m);
			
		return ret;
	}			// ok
	/**
	 *recalculates weighted mean and variance of the first distribution
	 */
	private void		recalcD1(){
		double acc, wacc;
		acc = 0.0; wacc = 0.0;
		for(int i=0; i<logsig.length; i++){
			//if(w1[i]!=0.0) System.out.println("w="+w1[i]);
			acc  += w1[i] * logvar34[i];
			wacc += w1[i];
		}
		System.out.println("logsig.length= "+logsig.length+" wacc= "+wacc );
		d1M = acc / wacc;
		
		acc = 0.0; wacc = 0.0;
		for(int i=0; i<logsig.length; i++){
			acc  += w1[i] * SQR(logvar34[i] - d1M);
			wacc += w1[i];
		}
		d1S = Math.sqrt(acc/wacc);
		System.out.println("D1=("+d1M+" ; "+d1S+")");
		
	}		// ok
	/**
	 *recalculates the data vector used for optimizing the variance-variance 
	 *function estimate according to the newly calculated variance-mean function 
	 */
	private void		recalcSqrVar(){
		double x0;
		for(int i=0; i<logsig.length; i++){
			sqrvar[i] = SQR(logvar0[i] - f_m.f(logsig[i]));
		}
	}	// ok
	/**
	 *recalculates weights for the reestimation of the functions
	 */
	private	void		recalcWeights(){
		System.out.println("in recalcWeights()");
		
			
		double p_1, p_2, p_Tr;
		for(int i=0; i<logsig.length; i++){
			p_1  = p1(i);
			p_2  = p2(i);
			p_Tr = p_1+p_2;
			if(p_Tr!=0.0) p_Tr = 1.0/p_Tr;
			w1[i] = p_1 * p_Tr;
			w2[i] = p_2 * p_Tr;
			//if(w1[i]<0.0) System.out.println("logsig = "+logsig[i]+", w1="+p_1+", w2="+p_2);
		}
		
		/* LOG
		try{
			String d = Utils.getCurrentTime()[0];
			
			File dir = new File("c:\\seppl");
			
			FileWriter fwr = new FileWriter(new File(dir, d+"_log.txt"));
			
			for(int i=0; i<logsig.length; i++){
				fwr.write(	Utils.getDecs(logsig[i], 3,false)+"\t"+
							Utils.getDecs(logvar0[i],3,false)+"\t"+
							Utils.getDecs(w2[i],     3,false)+Utils.lineEnd);
			}
			
			fwr.close();
		}catch(Exception e){}
		
		*/
		
		
	}	// ok
	/**
	 *calculates similar values as recalcWeights() with the exception 
	 *of undefined points. The values are used to estimate the 'blend' function
	 */
	private void		recalcPost(){
		System.out.println("in recalcPost()");
		for(int i=0; i<logsig.length; i++){
			if( w1[i]==0.0 && w2[i]==0.0)
				post[i] = 0.5;
			else
				post[i] = w1[i];
			
			//if(post[i]!=0.0) System.out.println("post="+post[i]);
		}
		
		
		fS_prob.setY(post);
		fS_prob.fit();
	}
	/**
	 *calculates the logLikelihood for element i
	 */
	private double		L(int i){
		double sum = p1(i) + p2(i);
		if(sum<=0.0){
			return 0.0;
		}
		return Math.log(sum);
	}			// ok\
	/**
	 *calculates the overall Likelihood
	 */
	private	double		L(){
		double acc = 0.0;
		for(int i=0; i<logsig.length; i++)
			acc += L(i);
		return acc;
	}				// ok
	/**
	 *reestimates variance-mean parameters
	 */
	private	void		enhanceM(){
		f_m.setW(w2);
		f_m.fit(0.01, 20);
	}		// ok
	/**
	 *reestimates variance-variance parameters
	 */
	private	void		enhanceS(){
		f_s.setY(sqrvar);
		f_s.setW(w2);
		f_s.fit(0.001, 10);
	}		// ok
	/**
	 *one cycle of expectation maximization.	
	 */
	public	double 		EM(){
		// step I   : enhance mean function estimate D2_m(t+1) based on weights(t)
		enhanceM();
		
		// step II  : recalculate sqr meanless variances using Model_x0(t+1)
		recalcSqrVar();
		
		// step III : enhance variance function estimate D2_s(t+1) based on weights(t)
		enhanceS();
		
		// step IV  : recalculate model D1(t+1) based on weights(t)
		recalcD1();
		
		// step V   : recalculate weights(t+1)
		recalcWeights();
		
		// step VI  : enhance a posteriori model
		recalcPost();
		
		// step VII : calculate and return logLikelihood(t+1)
		return L();
	}
	
	/**
	 *performs EM cycles until desired convergence is reached or cycle limit is exceeded
	 */
	public	double 		run(double conv, int maxcycle, File logdir){
		double Like = EM();
		int    cycle = 1;
		
		String name  = mTab.name;
		name = name.substring(0, name.length()-4);
		name = name.concat("_EMlog.txt"); 
			
		double dLike;	
				
		do{
			/*DEB*/System.out.println("++++ EM  ++++ CYCLE "+cycle);
			
			lastLike = Like;
			Like     = EM();
			cycle++;
						
			System.out.println("L="+Like);
			
			dLike = lastLike-Like;
			if(dLike>0.0 && dLike<conv ) break;
														
		}while(  /*Like-lastLike>conv &&*/ cycle<maxcycle);
				
		return Like;
	}	

	/**
	 *updates the annotation file of the source data file.
	 */
	public	void		updateAN()
	{
		File fin  = mTab.cmf.F_txt;
		File fout = AceUtil.getTemp();
		
		double	pSig[] = fS_prob.getP();
		double	pM[]   = f_m.get4P();
		double	pS[]   = f_s.get4P();
		
		String inset = new String(	 "VAR_BLEND"	+"\t"+Utils.getDecs(pSig[0], 4, false)
													+"\t"+Utils.getDecs(pSig[1], 4, false)
													+Utils.lineEnd
									+"VAR_MODEL1"	+"\t"+Utils.getDecs(thr03,   4, false)
													+"\t"+Utils.getDecs(d1M,     4, false)
													+"\t"+Utils.getDecs(d1S,     4, false)
													+Utils.lineEnd
									+"VAR_MODEL2_m"	+"\t"+Utils.getDecs(pM[0],   4, false)
													+"\t"+Utils.getDecs(pM[1],   4, false)
													+"\t"+Utils.getDecs(pM[2],   4, false)
													+"\t"+Utils.getDecs(pM[3],   4, false)
													//SIG+"\t"+Utils.getDecs(pM[4],   4, false)
													+Utils.lineEnd
									+"VAR_MODEL2_s"	+"\t"+Utils.getDecs(pS[0],   4, false)
													+"\t"+Utils.getDecs(pS[1],   4, false)
													+"\t"+Utils.getDecs(pS[2],   4, false)
													+"\t"+Utils.getDecs(pS[3],   4, false) );
													//SIG+"\t"+Utils.getDecs(pS[4],   4, false) );
		QCUtils.insertBehind(fin, fout, "SIGNAL_DISTRIBUTION", inset);
		
		mTab.cmf.F_txt = fout;
		mTab.cmf.save();
		
		mTab.info.kvm = new KeyValueMap(fout, true);
		
	}
	
	/**
	 *Updates the annotation file of the source data file.
	 *@param temp a new temp file (out)
	 */
	
	public	void		updateAN(File fin, File temp)
	{
	/*	File fin;
		if(mTab.cmf != null)
			fin = mTab.cmf.F_txt;
		else
			fin = new File("");*/
		
		File fout = temp;
		
		double	pSig[] = fS_prob.getP();
		double	pM[]   = f_m.get4P();
		double	pS[]   = f_s.get4P();
		
		
		
		String inset = new String(	 "VAR_BLEND"	+"\t"+Utils.getDecs(pSig[0], 4, false)
													+"\t"+Utils.getDecs(pSig[1], 4, false)
													+Utils.lineEnd
									+"VAR_MODEL1"	+"\t"+Utils.getDecs(thr03,   4, false)
													+"\t"+Utils.getDecs(d1M,     4, false)
													+"\t"+Utils.getDecs(d1S,     4, false)
													+Utils.lineEnd
									+"VAR_MODEL2_m"	+"\t"+Utils.getDecs(pM[0],   4, false)
													+"\t"+Utils.getDecs(pM[1],   4, false)
													+"\t"+Utils.getDecs(pM[2],   4, false)
													+"\t"+Utils.getDecs(pM[3],   4, false)
													//SIG+"\t"+Utils.getDecs(pM[4],   4, false)
													+Utils.lineEnd
									+"VAR_MODEL2_s"	+"\t"+Utils.getDecs(pS[0],   4, false)
													+"\t"+Utils.getDecs(pS[1],   4, false)
													+"\t"+Utils.getDecs(pS[2],   4, false)
													+"\t"+Utils.getDecs(pS[3],   4, false) );
													//SIG+"\t"+Utils.getDecs(pS[4],   4, false) );
		QCUtils.insertBehind(fin, fout, "SIGNAL_DISTRIBUTION", inset);											
		
		if(mTab.cmf != null)
		{
			mTab.cmf.F_txt = fout;
			if((mTab.cmf.z_file) != null)
			{
				mTab.cmf.save();
				System.out.println("mTab.cmf saved");
			}
		}
		
		mTab.info.kvm = new KeyValueMap(fout, true);
		System.out.println("KeyValueMap modified");
	}
	/**
	 *returns blending function parameters
	 */
	public	double[]	getParBlend(){
		return fS_prob.getP();
	}
	/**
	 *returns parameters for the first distribution
	 */
	public	LNDp		getParD1(){
		return new LNDp(1.0, d1M, d1S, thr03);
	}
	/**
	 *returns parameters for the sigmoid function that estimates the variance-mean
	 *as a function of the signal 
	 */
	public	double[]	getParD2_Mean(){
		return f_m.get4P();
	}
	/**
	 *returns parameters for the sigmoid function that estimates the variance-variance
	 *as a function of the signal 
	 */
	public	double[]	getParD2_Var(){
		return f_s.get4P();
	}
	
				
	private	double		SQR(double x){ return x*x; }
	private	double		ABS(double x){ return x<0.0?-x:x; }
	
}
