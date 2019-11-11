package maModel;

//import acemap.*;
//import acemapMath.*;

import acemapQCCore.*;

import maModel.*;

import java.io.*;
import aceStyle.*;
import java.util.*;
import javax.swing.JFileChooser;
import acemapCore.*;

/**
 *Object representing a mixture distribution composed of log normal distributions.
 *
 *The distribution parameters are estimated using an EM algorithm. Defining data is read
 *from a MTable object.
 *@author Guillaume Brysbaert, Sebastian Noth
 *@version 0.58
 */

public class LNDMix{
	
	private double ABS(double x){ return x<0.0?-x:x; }

	private LNDp	lndp[];
	
	private double	dat[];
	private double	logdat[][];
	
	private	double	logL;
	
	private MTable	mTab;

	/**
	 *Constructor requiring an MTable object.
	 */	
	public LNDMix(MTable mTab){
		this.mTab = mTab;
		dat = new double[mTab.size];
		for(int i=0; i<dat.length; i++){ // !!!
			/* AVOIDING NONPOSITIVE SIGNAL */
			if(mTab.data[i].signal <= 0.0f)
				dat[i] = 0.0000001;
			else
			/**/	
				dat[i] = (double)mTab.data[i].signal;
		}
				
		lndp    = new LNDp[2];
		//lndp[0] = new LNDp(0.5, 0.0, 1.0, 0.0);
		//lndp[1] = new LNDp(0.5, 0.0, 1.0, 1.0);
	}
	
	/**
	 *Overwrites distribution parameters with the given array of LNDp objects.
	 */
	public void		setParameters(LNDp[] lndp){
		this.lndp = lndp;
	}
	
	private double	SQR(double x){
		return x*x;
	}
	/**
	 *Calculates the single probability for a definition data value
	 *@param k index of distribution
	 *@param index index of definition data item
	 */
	private double	Pi(int k, int index){
		LNDp l = lndp[k];
		
		if(dat[index]<=l.x0)
			return 0.0;
		else
			return l.f / ( (dat[index] - l.x0) * Utils.sqrt2pi*l.s) * Math.exp( -SQR( logdat[k][index] - l.logm)/(2.0*SQR(l.s)));
			
		
		//return lndp[k].p(x);
	}
	/**
	 *Calculates the combined probability of all distributions for a definition data value
	 *@param index index of definition data item
	 */
	private double	Pt(int index){
		double res = 0.0;
		
		for(int j=0; j<lndp.length; j++){
			res += Pi(j, index);
		}
			
		return res;	
	}
	
	/**
	 *calculates the log likelihood of the current mixture distribution and definition data
	 */
	public	double	L(){
		
		double res = 0.0;
		for(int i=0; i<dat.length; i++){
			if(Pt(i) <= 0.0)
				System.out.println("signal : "+dat[i]);
			else
				res += Math.log( Pt(i) );
		}
		
		return res;
	}
	/**
	 *calculates the log likelihood for a mixture distribution and definition data
	 *@param lndpM a lognormal mixture distribution
	 */
	public double	L(LNDp[] lndpM){
		LNDp[] temp = lndp;
		lndp = lndpM;
		
		logdat  = new double[lndp.length][dat.length];
		
		
		
		for(int j=0; j<lndp.length; j++){
			
			for(int i=0; i<dat.length; i++){
				if(dat[i]>lndp[j].x0)
					logdat[j][i] = Math.log( dat[i]-lndp[j].x0 );
				else	
					logdat[j][i] = 0.0;
			}
		}
		
		
		
		double res = L();
		
		lndp = temp;	
		
		return res;
	}
	/**
	 *Prints parameters of the first two contributiong distributions to stdout.
	 */
	private void dataOut(){
		System.out.println("D1 ("+Utils.getDecs(lndp[0].f , 3, false)+"; "
								 +Utils.getDecs(lndp[0].m , 3, false)+", "
								 +Utils.getDecs(lndp[0].s , 3, false)+", "
								 +Utils.getDecs(lndp[0].x0, 3, false)
					   +"), D2 ("+Utils.getDecs(lndp[1].f , 3, false)+"; "
					             +Utils.getDecs(lndp[1].m , 3, false)+", "
					             +Utils.getDecs(lndp[1].s , 3, false)+", "
					             +Utils.getDecs(lndp[1].x0, 3, false)+")" );
	}
	
	
	
	/**
	 *Improves the parameter estimate using  a single EM step
	 */
	public	void	EM(){
		double ws[]      = new double[lndp.length];  // weights
		double m[]       = new double[lndp.length];  // means
		double p[]       = new double[lndp.length];  // probabilities
		
		LNDp   lndpNew[] = new LNDp[  lndp.length]; 
		
		double w;
		double pTot;
		double div  = (double)(dat.length);
				
		
		// initialization				
		for(int i=0; i<ws.length; i++){
			// initialize weight sums
			ws[i] = 0.0;
			m[ i] = 0.0;
			// initialize new parameter vector
			lndpNew[i]    = new LNDp();
			lndpNew[i].x0 = lndp[i].x0;
			lndpNew[i].s  = 0.0;
			lndpNew[i].f  = 0.0;
		}
				
		//*DEB*/Sytem.out.println("dat.length = "+dat.length);
		for(int i=0; i<dat.length; i++){
			pTot = 0.0;
			// calculate probabilities of all involved distributions
			for(int j=0; j<lndp.length; j++){
				p[j] = Pi(j, i);
				pTot +=p[j];
			}
			
			// accumulate
			for(int j=0; j<lndpNew.length; j++){
				// current weight
				w = p[j] / pTot;
						
				// advance weight accumulator
				ws[j] += w;
										
				// add to mean accumulator: if x>x0 then ln(x-x0), 0 otherwise		
				m[j]  += w * logdat[j][i];        //lndp[j].log(dat[i]);
			}
		}
		
		for(int j=0; j<lndp.length; j++){
			m[j]  = m[j]/ws[j];
			ws[j] = 0.0;
		}
			
		
		for(int i=0; i<dat.length; i++){
			pTot = 0.0;
			// calculate probabilities of all involved distributions
			for(int j=0; j<lndp.length; j++){
				p[j] = Pi(j, i);
				pTot +=p[j];
			}
			
			// accumulate
			for(int j=0; j<lndpNew.length; j++){
				// current weight
				w = p[j] / pTot;
						
				// advance weight accumulator
				ws[j]        += w;
				
				// advance multiplicator				
				lndpNew[j].f += w;
				
				// advance variance estimate; use new mean estimate
				lndpNew[j].s += w * SQR( logdat[j][i] - m[j] );        //lndp[j].log(dat[i]) - m[j] );
								
			}
		}
		
		
		// transform mean
		for(int j=0; j<lndp.length; j++){	
			lndpNew[j].logm = m[j];
			lndpNew[j].m    = Math.exp( m[j] );
		
			lndpNew[j].s    = Math.sqrt( lndpNew[j].s / ws[j]);
			lndpNew[j].f    = lndpNew[j].f / div;
		
		}	
		
		// replace old by new parameters			
		lndp = lndpNew;
		
	}
	/**
	 *As EM(), but returns the resulting likelihood
	 */
	public	double	EMlike()
	{
		EM();
		double like = L();
		
		
		//dataOut();
		
		
		
		
		return like;
	}
	/**
	 *Runs a series of EM steps requesting the x0 value for the second 
	 *contributing distribution until either a maximum of steps is reached or the likelihood
	 *increase between two steps falls below a convergence limit
	 */ 
	public	void	run(){
		double x0start = Utils.getNumber("enter x0");
		
		lndp[0] = new LNDp(0.5, 0.2, 1.0, 0.0     );
		lndp[1] = new LNDp(0.5, 2.0, 1.0, x0start );
				
		double conv = 0.000001;
		int maxiter = 150;
		
		int    iter = 0;
		double like = -1000000.0;
		double lastlike;
		
		do{
			lastlike = like;
			like = EMlike();
			System.out.println("step "+iter+": L="+Utils.getDecs(like, 3, false));
			iter++;

		}while(iter<maxiter && like-lastlike>conv); 
		
		
		if(Utils.request("once more?", "yes", "no"))
			run();
	}
	
	/**
	 *as run(), but allows to specify x0 and convergence limit directly
	 */
	public	double	run(double x0start, double convmax){
			
		lndp[0] = new LNDp(0.5, 0.2, 1.0, 0.0     );
		lndp[1] = new LNDp(0.5, 2.0, 1.0, x0start );
		
		logdat  = new double[lndp.length][dat.length];
		
		
		for(int j=0; j<lndp.length; j++){
			
			for(int i=0; i<dat.length; i++){
				if(dat[i]>lndp[j].x0)
					logdat[j][i] = Math.log( dat[i]-lndp[j].x0 );
				else	
					logdat[j][i] = 0.0;
			}
		}
		
		//*DEB*/ System.out.println("run called, dat size "+dat.length);
		//*DEB*/ System.out.println("initial likelihood = "+L());
		
				
		int maxiter = 150;
		
		int    iter = 0;
		double like = -100000000.0;
		double lastlike;
		
		do{
			lastlike = like;
			like = EMlike();
			System.out.println("step "+iter+": L="+Utils.getDecs(like, 3, false));
			iter++;
			
		}while(iter<maxiter && like-lastlike>convmax); 
		
		System.out.println("EM converged. logL= "+Utils.getDecs(like, 3, false)+" model:");
		dataOut();
		
		
		return like;
	}
	/**
	 *Performs a series of run(...) calls using different x0 values and selects the 
	 *parameter set associated with the largest likelihood value
	 *@param logdir a directory where a logfile is written to. If null, no logfile is created.
	 */
	public	void	optimize(File logdir){
		FileWriter fwr=null;
		File log;
		if(logdir!=null){
			String nameO = mTab.cmf.getFilename();
			if(mTab.cmf == null)
				log = null;
			else
				log = new File(logdir, nameO.substring(0, nameO.length()-4)+"_log.txt");
		}
		else
			log = null;
		if(log!=null){
			try{
				fwr = new FileWriter(log);
				fwr.write("D1_x0\tD1_w\tD1_m\tD1_v\tD2_x0\tD2_w\tD2_m\tD2_v\tlogL"+Utils.lineEnd);
			}catch(Exception ex){}
		}
		
		int crseLen     = 20; //50
		int fineLen     = 4; //10
			
		double crseStep = 0.06; //0.02
			
		double crseL[]  = new double[crseLen];
		double fineL[]  = new double[fineLen];
		
		int    iMax;
		double lMax;
						
		for(int i=0; i<crseLen; i++){
			crseL[i] = run(crseStep * (double)i, 0.1); //0.01
			if(log!=null){
				try{
					fwr.write(	Utils.getDecs( lndp[0].x0, 3, false)+"\t"+
								Utils.getDecs( lndp[0].f , 3, false)+"\t"+
								Utils.getDecs( lndp[0].m , 3, false)+"\t"+
								Utils.getDecs( lndp[0].s , 3, false)+"\t"+
								Utils.getDecs( lndp[1].x0, 3, false)+"\t"+
								Utils.getDecs( lndp[1].f , 3, false)+"\t"+
								Utils.getDecs( lndp[1].m , 3, false)+"\t"+
								Utils.getDecs( lndp[1].s , 3, false)+"\t"+
								Utils.getDecs( crseL[i]  , 3, false)+Utils.lineEnd );
				}catch(Exception excp){}
			}
		}
		
		if(log!=null){
			try{
				fwr.close();
			}catch(Exception ex2){System.out.println("ii");}
			
		}
		
		
		
		
		
		
		iMax = 0;
		lMax = crseL[0];
		
		for(int i=1; i<crseLen; i++){
			if(crseL[i]>lMax){
				lMax = crseL[i];
				iMax = i;
			}
		}
		
			
		double fineSta  = (double)(iMax-1) * crseStep;
		double fineStep = crseStep*2.0 / (double)fineLen;
		
		for(int i=0; i<fineLen; i++){
			fineL[i] = run(fineSta + fineStep*(double)i, 0.001);
			if(log!=null){
				try{
					fwr.write(	Utils.getDecs( lndp[0].x0, 4, false)+"\t"+
								Utils.getDecs( lndp[0].f , 4, false)+"\t"+
								Utils.getDecs( lndp[0].m , 4, false)+"\t"+
								Utils.getDecs( lndp[0].s , 4, false)+"\t"+
								Utils.getDecs( lndp[1].x0, 4, false)+"\t"+
								Utils.getDecs( lndp[1].f , 4, false)+"\t"+
								Utils.getDecs( lndp[1].m , 4, false)+"\t"+
								Utils.getDecs( lndp[1].s , 4, false)+"\t"+
								Utils.getDecs( fineL[i]  , 4, false)+Utils.lineEnd );
				}catch(Exception excp){}
			}
		}	
		iMax = 0;
		lMax = fineL[0];
		
		
		
		
		for(int i=1; i<fineLen; i++){
			if(fineL[i]>lMax){
				lMax = fineL[i];
				iMax = i;
			}
		}
		
		// keep best parameters
		run(fineSta + fineStep*(double)iMax, 0.00001);
		
		System.out.println(">>>>>>>>>>>> FINAL MODEL:");
		dataOut();
		
		// maximum likelihood at fineL[iMax]
		
		if(log!=null){
			try{
				fwr.close();
			}catch(Exception ex2){}
		}
		
		if(mTab.cmf == null || mTab.cmf.F_txt == null)
			return;
		
		File fin = mTab.cmf.F_txt;
		File fout = QCUtils.getTemp();
		
		String inset = new String("SIGNAL_DISTRIBUTION"	+"\t"+Utils.getDecs(lndp[0].f , 4, false)
														+"\t"+Utils.getDecs(lndp[0].m , 4, false)
														+"\t"+Utils.getDecs(lndp[0].s , 4, false)
														+"\t"+Utils.getDecs(lndp[0].x0, 4, false)
														+"\t"+Utils.getDecs(lndp[1].f , 4, false)
														+"\t"+Utils.getDecs(lndp[1].m , 4, false)
														+"\t"+Utils.getDecs(lndp[1].s , 4, false)
														+"\t"+Utils.getDecs(lndp[1].x0, 4, false) 
														+"\t"+Utils.getDecs(lMax,       4, false)  );
		
		QCUtils.insertBehind(fin, fout, "MIXTURE_D_MODEL", inset);												
		
		if(mTab.cmf != null)
		{
			mTab.cmf.F_txt = fout;
			if(mTab.cmf.z_file != null)
			{
				mTab.cmf.save();
				mTab.info.kvm = new KeyValueMap(fout, true);
				System.out.println("mTab.cmf saved");
			}
		}
	}
	
	/**
	 *returns the current parameters for the mixture distribution
	 */
	public	LNDp[]	getParams(){
		return lndp;
	}
}	
	