package maModel; 

//import acemap.*;
import acemapMath.*;

/**
 *Sigmoid implementation used to estimate variance-variance as a function of (log-)signal.
 *@author Sebastian Noth
 *@version 0.73
 */
public class FSigErrVar extends FitFunction{
	private double	yoff;
	private double	sig[], var[], w[];

	private	double	P[];
		
	// p[0] : scl
	// p[1] : slope
	// p[2] : xshift
	public FSigErrVar(double yoff, double sig[], double var[]){
		delta      = 0.00001;
		
		this.yoff  = yoff;
		
		this.sig   = sig;
		this.var   = var;
	}
	public FSigErrVar(double yoff, double sig[], double var[], double w[]){
		this(yoff, sig, var);
		this.w = w;
	}
	public FSigErrVar(double p4[]){
		P    = new double[3];
		
		yoff = p4[0];
		P[0] = p4[1];
		P[1] = p4[2];
		P[2] = p4[3];
	}

	
	public	void		setW(double w[]){ 
		this.w = w;
	}
	public	void		setData(double sig[], double var[]){
		this.sig = sig;
		this.var = var;
	}
	public	void		setP(double P[]){
		this.P = P;
	}
	public	double		getVar(double p[], double x){
		return yoff+p[0] / (1.0 + Math.exp(-p[1]*(x-p[2]))); 		
		//return 0.1;
	}
	public	double		getVar(double x){
		return getVar(P, x);
	}
	public	double		err(double p[]){
		double res = 0.0;
		double val;
				
		//*DEB*/int c=0;
				
		for(int i=0; i<sig.length; i++){
			val = getVar(p, sig[i]);
			res += w[i] * SQR( var[i] - val );
		}
		//*DEB*/System.out.println("in err: "+c+" out of "+sig.length);
		return res;
	}
	
	public	double[]	fit(){
		P = fit(P, 0.001, 100);
		return P;
	}
}