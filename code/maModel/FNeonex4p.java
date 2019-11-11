package maModel;


import	acemapCore.*;
import	acemapMath.*;
import  aceStyle.*;
import  java.io.*;

/**
 *Implementation for the neonex function, but not optimizing the y-offset 
 *that must be supplied in the constructor
 *@author Sebastian Noth
 *@version 0.73
 */
public class FNeonex4p extends FitFunction{
	
	private double 	pUse[], P[], Pinit[];
	private double	w[];
	private double	X[], Y[];
	
	private	boolean logErr;
	private int		locked = -1;
	private double	yOffset = 0.0;

	public	FNeonex4p(){
		w=null;
	}
	public	FNeonex4p(double X[], double Y[], double yOff, boolean logErr){
		delta      = 0.000001;
		yOffset = yOff;
		w=null;
		this.logErr = logErr;
		
		this.X = X;
		this.Y    = Y;
		
		
		Pinit    = new double[4];
		if(logErr){
			Pinit[0] =  1.3;
			Pinit[1] =  0.0;
			Pinit[2] =  0.5;
			Pinit[3] = -1.0;
		}
		else{
			Pinit[0] =  0.5;
			Pinit[1] =  0.0;
			Pinit[2] =  0.5;
			Pinit[3] = -1.0;
		}
		P = copy(Pinit);
		
	}
	
	public	void		setW(double w[]){
		this.w = w;
	}
	public	void		setY(double Y[]){
		this.Y = Y;
	}
	public	void		setP(double p[]){
		Pinit = copy(p);
		P     = copy(p);
	}
	public	void		lock(int dir){
		locked = dir;
	}
	private void		transformParameters(double z[]){
		if(locked!=-1)
			z[locked] = Pinit[locked];
				 				
		double o    = yOffset; // y off
		double r    = z[0];	// y range
		double hoff = z[1]; // x shift
		double y0   = z[2]; // connection point y value
		double y0d  = z[3]; // slope at y0
		double c    = -y0d/y0;
		
		double x0, k;
		// gau+nex
		//x0 = 2.0*Math.log(y0)*y0 / y0d;
		//k    = Math.sqrt( -2.0*Math.log(y0) * y0*y0 / (y0d*y0d) );
		
		// neonex
		x0   = -2.0 * Math.log(1.0-y0) * (1.0-y0) / y0d; 
		k    = Math.sqrt( x0 * (1.0-y0) / y0d );	
		
		//System.out.println("k="+k);
			
		double m    = x0 - ( Math.log(y0) * y0 / y0d);
		
		if(Double.isNaN(x0)) System.out.println("x0, "+toString(z,4));
		if(Double.isNaN(k)) System.out.println("k");
		if(Double.isNaN(m)) System.out.println("m");
		
		
		pUse    = new double[7];
		
		pUse[0] = o;
		pUse[1] = r;
		pUse[2] = hoff;
		pUse[3] = c;
		pUse[4] = m;
		pUse[5] = k;
		pUse[6] = x0; // ==b
		
		//System.out.toStringtoString(pUse);

	}
	public	void		setup(){
		transformParameters(P);
	}
	public	double		err(double p[]){
		/*
		if(p[2]>=0.9){
			return 1.0E8+ (p[2]-0.9)*1.0E9;
		}
				
		if(p[2]>=0.9 || p[2]<=0.01)
			return 10000000000.0;
		*/
		transformParameters(p);
		
		
		if(yOffset<0.0){
			
			try{
				File dir = new File("C:\\seppl");
				File f   = new File(dir, Utils.getCurrentTime()[0]+"_fnc.txt");
				
				FileWriter fwr = new FileWriter(f);
				for(int i=0; i<100; i++){
					fwr.write(Utils.getDecs((double)i/10.0-5.0, 3, false)+"\t"+
							  Utils.getDecs( f( (double)i/10.0 -5.0), 3, false) +Utils.lineend );
				}
				fwr.close();	
				
				
				
				
			}catch(Exception e){}
		}
		
		
		
		double meanerr=0.0;
		
		
		double acc = 0.0;
		double x0;
		
		if(w==null){
			if(logErr){
				for(int i=0; i<X.length; i++){
					x0 = f(X[i]);
					if(x0<Y[i])
						acc += SQR( Math.log( Y[i]-x0 ));
					else{
						acc += 0.0;  // penalty for non-classifed points
					}
				}
			}
			else{
				for(int i=0; i<X.length; i++){
					x0 = f(X[i]);
					acc += SQR( Y[i]-x0 );
				}
			}
		}
		else{
			if(logErr){
				for(int i=0; i<X.length; i++){
					x0 = f(X[i]);
					if(x0<Y[i])
						acc += w[i] * SQR( Math.log( Y[i]-x0 ));
					else{
						acc += 0.0;  // penalty for non-classifed points
					}
				}
			}
			else{
				for(int i=0; i<X.length; i++){
					if(w[i]<0.05) continue;
					x0 = f(X[i]);
					//*DEB*/meanerr += ABS( Y[i]-x0 );
					acc += w[i] * SQR( Y[i]-x0 );
				}
			}	
		}
		//*DEB*/System.out.println("merr"+meanerr+", acc="+acc);
		return acc;
	}
	public	double		f(double x){
		double o  = pUse[0];	// y offset
		double r  = pUse[1];	// y scale
		double ho = pUse[2];	// x shift
		double c  = pUse[3];   // nex shift
		double m  = pUse[4];   // nex slope
		double k  = pUse[5];   // neo 'slope'
		double b  = pUse[6];   // blend centre, x0
		
		//double w  = 1.0/( 1.0 + Math.exp(-30.0*(x-ho-b)) );
		
		double ye;
		//ga-nex
		//ye = o + (r-o) * (w*Math.exp(-(x-ho-m)*c) + (1.0-w)*Math.exp(-SQR(x-ho)/(2.0*k*k)) );
		
		//neonex old
		//ye = o + (r-o) * (w*Math.exp(-(x-ho-m)*c) + (1.0-w)*(1.0 - Math.exp(-SQR(x-ho)/(2.0*k*k))) );
		
		if(x-ho>b)
			ye = o + r * Math.exp(-(x-ho-m)*c);
		else
			ye = o + r * (1.0 - Math.exp(-SQR(x-ho)/(2.0*k*k)));
				
		return ye;
	}
	public	double[]	fit(){
		P = fit(P, 0.001, 80);
		
		//System.out.println("pT="+toString(pUse, 3));
		return P;
	}
	public	double[]	fit(double conv, int maxcycle){
		P = fit(P, conv, maxcycle);
		
		//System.out.println("pT="+toString(pUse, 3));
		return P;
	}
	public	double[]	get5P(){
		double ret[] = new double[5];
		ret[0] = yOffset;
		for(int i=0; i<4; i++)
			ret[i+1] = P[i];
		return ret;
	}
	
}
