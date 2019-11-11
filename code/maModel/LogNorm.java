package maModel;

//import acemap.*;
import acemapMath.*;
import java.io.*;
import aceStyle.*;
import java.util.*;
import javax.swing.JFileChooser;
import acemapCore.*;

public class LogNorm{
	
	private	double	SQR(double x){
		return x*x;
	}
	
	
	
	public LogNorm(){}
	
	
	private	double	SIGMIN = 0.00001;
	
	private	MTable	srcDat = null;
	private	double	logDat[];
	private boolean	invalid[];
	
	private	double	mju, sigma, gamma;
	private	int		rej;
	
	
	public	void	setFile(File f){
		
		CompositeMf cmf = new CompositeMf();
		cmf.load(f, true, false, false);
					
		srcDat = new MTable (cmf.F_ma, null, false, null, false);
		initBuffers();
	}
	public	void	setData(MTable mt){
		srcDat = mt;	
		initBuffers();
	}
	private	void	initBuffers(){
		logDat  = new double[srcDat.size];
		invalid = new boolean[srcDat.size];
	}
	
	
	public	void	calcLogDat(double gamma){
		this.gamma = gamma;
		rej=0;
		
		for(int i=0; i<srcDat.size; i++){
			if(  (srcDat.data[i].signal-gamma) < SIGMIN ){
				logDat[i]  = Math.log(SIGMIN);
				invalid[i] = true;
				rej++;
			}
			else{
				logDat[i]  = Math.log(srcDat.data[i].signal-gamma);
				invalid[i] = false;
			}
		}
	}
	
	public	double[]	getLogData(){
		return logDat;
	}
	
	public	void	calcND(){
		mju   = 0.0;	
		sigma = 0.0;
	
		for(int i=0; i<logDat.length; i++)
			if(!invalid[i])
				mju+=logDat[i];
					
		mju /= ((double)(logDat.length-rej));
		
		for(int i=0; i<logDat.length; i++)
			if(!invalid[i])
				sigma += SQR(logDat[i]-mju);
				
			
		sigma/= (double)(logDat.length-rej-1);
		sigma = Math.sqrt(sigma);
	
		//System.out.println("for gamma="+gamma+" : N("+mju+" ; "+sigma+"), rej="+rej);
	
	}
	
	public	NDp		getNDparams(){
		NDp ret = new NDp(1.0, mju, sigma);
		return ret;
	}
	
	private	double	logProb(int index){
		double res = -Math.log(Utils.sqrt2pi * sigma) -logDat[index] -SQR(logDat[index]-mju)/(2.0*sigma*sigma);		
		return res;
	}
	
	public	double	prob(double x){
		if(x-gamma<=0.0)
			return 0.0;
		else
			return 1.0/(Utils.sqrt2pi*sigma*(x-gamma))*Math.exp(-SQR(Math.log(x-gamma)-mju)/(2.0*sigma*sigma)  );
	}
	
	
	public	double	Likelihood(){
		double res = 0;
		
		for(int i=0; i<logDat.length; i++)
			if(!invalid[i])
				res += logProb(i);
		
		return res/(double)(logDat.length-rej);
	}
	
	
	
	
	
	public	void	run(MTable mt){
		setData(mt);
		
		
		calcLogDat(0.0);
		
		NDmix ndm = new NDmix(logDat);
		
		for(int i=0; i<100; i++)
			ndm.EM();
		
		
				
		
		
		
		try{
			FileWriter fwr = new FileWriter(AceUtil.getTemp());
			
			
			calcLogDat(1.0);
			calcND();
			Utils.qsortD(logDat);
			
			double res     = 0.5;
			int    binsA[] = new int[1];
			double off     = Utils.histoBounds(logDat, res, 0.005, 0.995, binsA);
			int    bins    = binsA[0];
			
			double arr[]   = Utils.histo(logDat, off, res, bins);
			
			
			
			for(int i=0; i<bins; i++)
				fwr.write("\t"+Utils.getDecs(off+res*(0.5+(double)i), 3, false));
			fwr.write(Utils.lineend);
			
			
			
			
			
					
			for(int i=0; i<20; i++){
				calcLogDat(0.1*(double)i);
				calcND();
				Utils.qsortD(logDat);
						
				arr  = Utils.histo(logDat, off, res, bins);
			
				fwr.write("hist("+Utils.getDecs(0.1*(double)i,3,false)+")");
				for(int j=0; j<bins; j++)
					fwr.write("\t"+Utils.getDecs( arr[j] , 3, false));
				fwr.write(Utils.lineend);
			
			
			
				fwr.write("est("+Utils.getDecs(0.1*(double)i,3,false)+")");
				for(int j=0; j<bins; j++)
					fwr.write("\t"+Utils.getDecs(Utils.normDist( off+res*(0.5+(double)j), mju, sigma) , 3, false));
											
				fwr.write(Utils.lineend);
			
			
			
			}			
						
			
			fwr.close();
		}catch(Exception e){}
			
			
			
			
			
			
		if(Utils.request("Calculate Likelihood manually?", "yes", "no")){
			
			NDp ndp[] = new NDp[2];
			double f = Utils.getNumber("Distribution 1\npart");
			double m = Utils.getNumber("Distribution 1\nmean");
			double s = Utils.getNumber("Distribution 1\nvariance");
			ndp[0] = new NDp(f,m,s);
			
			f = Utils.getNumber("Distribution 2\npart");
			m = Utils.getNumber("Distribution 2\nmean");
			s = Utils.getNumber("Distribution 2\nvariance");
			ndp[1] = new NDp(f,m,s);
			
			
			calcLogDat(0.0);
			ndm = new NDmix(logDat);
			
			
			System.out.println("manual estimation Likelihood="+ndm.L(ndp));
			
			if(Utils.request("Re-run EM?", "yes", "no")){
				
				ndm.setParameters(ndp);
				for(int i=0; i<100; i++)
				ndm.EM();
			}
		}
			
			
			
			
	} 
}
	