package maModel; 

//import acemap.*;
//import acemapMath.*;
import acemapCore.*;
import aceStyle.*;

/**
 *Implementation for the neonex function. all five parameters are used in the 
 *optimization process.
 *@author Sebastian Noth
 *@version 0.58
 */
 
public class FNeonex extends FitFunction
{
	private double 	pUse[], P[], Pinit[];
	private double	w[];
	
	private double	X[], Y[];
	
	private	boolean logErr;
	
	private int		locked = -1;

	public	FNeonex(){
		w=null;
	}
	
	public	FNeonex(MTable mTab, boolean logErr){
		delta      = 0.000001;
		w=null;
		this.logErr = logErr;
		
		X = new double[mTab.size];
		Y    = new double[mTab.size];
		for(int i=0; i<Y.length; i++){
			X[i] = Math.log( mTab.data[i].signal );
			Y[i]    =           mTab.data[i].moq;
		}
		Pinit    = new double[5];
		if(logErr){
			Pinit[0] = -0.95;
			Pinit[1] =  1.3;
			Pinit[2] =  0.0;
			Pinit[3] =  0.5;
			Pinit[4] = -1.0;
		}
		else{
			Pinit[0] =  0.1;
			Pinit[1] =  0.5;
			Pinit[2] =  0.0;
			Pinit[3] =  0.5;
			Pinit[4] = -1.0;
		}
		P = copy(Pinit);
	}
	
	public	FNeonex(double X[], double Y[], boolean logErr){
		delta      = 0.000001;
		w=null;
		this.logErr = logErr;
		
		this.X = X;
		this.Y    = Y;
		
		
		Pinit    = new double[5];
		if(logErr){
			Pinit[0] = -0.95;
			Pinit[1] =  1.3;
			Pinit[2] =  0.0;
			Pinit[3] =  0.5;
			Pinit[4] = -1.0;
		}
		else{
			Pinit[0] =  0.1;
			Pinit[1] =  0.5;
			Pinit[2] =  0.0;
			Pinit[3] =  0.5;
			Pinit[4] = -1.0;
		}
		P = copy(Pinit);
		
	}
	
	public	void	setW(double w[]){
		this.w = w;
	}
	public	void	setY(double Y[]){
		this.Y = Y;
	}
	
	public	void	setP(double p[]){
		Pinit = copy(p);
		P     = copy(p);
	}
	
	public	void	lock(int dir){
		locked = dir;
	}
	
	
	
	private void	transformParameters(double z[]){
		if(locked!=-1)
			z[locked] = Pinit[locked];
		
		z[3] = 0.5; 				
		double o    = z[0]; // y off
		double r    = z[1];	// y range
		double hoff = z[2]; // x shift
		double y0   = 0.5; //z[3]; // connection point y value
		double y0d  = z[4]; // slope at y0
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
	
	public void		setup(){
		transformParameters(P);
	}
	
	public double	err(double p[]){
		if(p[3]>=0.9){
			return 1.0E8+ (p[3]-0.9)*1.0E9;
		}
		
		
		if(p[3]>=0.9 || p[3]<=0.01)
			return 10000000000.0;
		
		transformParameters(p);
		
		
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
					x0 = f(X[i]);
					//*DEB*/meanerr += ABS( Y[i]-x0 );
					acc += w[i] * SQR( Y[i]-x0 );
				}
			}	
		}
		//*DEB*/System.out.println("merr"+meanerr+", acc="+acc);
		return acc;
	}
	public double	f(double x){
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
	
}
