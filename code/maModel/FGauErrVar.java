package maModel; 
//import acemap.*;
import acemapMath.*;

public class FGauErrVar extends FitFunction{
	private double	sig[], var[], w[];
	private double	P[];
	
	// p[0] : scl
	// p[1] : mean
	// p[2] : variance
	public FGauErrVar(double sig[], double var[]){
		delta      = 0.00001;
		
		this.sig   = sig;
		this.var   = var;
	}
	public FGauErrVar(double sig[], double var[], double x[]){
		this(sig, var);
		this.w = w;
	}
	
	public	void		setW(double w[]){ this.w = w; }
	public	void		setData(double sig[], double var[]){
		this.sig = sig;
		this.var = var;
	}
	public	void		setP(double P[]){
		this.P = P;
	}
	public	double		getVar(double p[], double x){
		return /*!!!*/0.01+ /*!!!*/ ABS(p[0])/100.0 * Math.exp(-SQR(x - p[1]) / (2.0*p[2]*p[2])); 		
		
	}
	public	double		getVar(double x){
		return getVar(P, x);	
	}
	public	double		err(double p[]){
		double res = 0.0;
		
		for(int i=0; i<sig.length; i++)
			res += w[i] * SQR(var[i] - getVar(p, sig[i]));
				
		return res;
	}
	public	double[]	fit(){
		P = fit(P, 0.1, 100);
		return P;
	}
}