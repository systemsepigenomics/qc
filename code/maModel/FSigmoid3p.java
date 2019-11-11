package maModel; 

//import acemap.*;
//import acemapMath.*;
import aceStyle.*;


/**
 *Error function implementation for a sigmoid with an additional y-scaling 
 *parameter. an y-offset must be supplied in the constructor.
 *@author Sebastian Noth
 *@version 0.58
 */
public class FSigmoid3p extends FitFunction{
	
	private double	X[], Y[];
	private double	P[];
	private double	w[];
	
	
	private double	yOffset;
	
	public FSigmoid3p(double X[], double Y[], double yOffset){
		this.X = X;
		this.Y = Y;
		this.yOffset = yOffset;
		w = null;
		
		P = new double[2];
		P[0] =  0.0;
		P[1] = -1.0;
	}
	public FSigmoid3p(double P[]){
		this.P = copy(P);
	}
	
	public	void	setY(double Y[]){
		this.Y = Y;
	}
	public	void	setW(double w[]){
		this.w = w;
	}
	public void setP(double p[]){
		P = copy(p);
	}
	
	public double	err(double p[]){
		double acc = 0.0;
		
		if(w==null){
			for(int i=0; i<X.length; i++)
				acc += SQR(Y[i] - f(X[i], p));
		}
		else{
			for(int i=0; i<X.length; i++)
				if(w[i]>0.05)
					acc += w[i] * SQR(Y[i] - f(X[i], p));
		}
		return acc;
	}
		
	public	double	f(double x, double p[]){
		return yOffset + p[0]/(1.0+Math.exp(-(x-p[1])*p[2]));
	}
	
	public	double	f(double x){
		return f(x, P);
	}
	
	
	public	double[]	fit(){
		P = fit(P, 0.0001, 100);
		
		//System.out.println("x0="+P[0]+", slope="+P[1]);
		
		return P;
	}
	public	double[]	fit(double conv, int maxcycle){
		P = fit(P, conv, maxcycle);
		
		//System.out.println("x0="+P[0]+", slope="+P[1]);
		
		return P;
	}
	
	public double[]	getP(){
		return copy(P);
	}
	public double[] get4P(){
		double ret[] = new double[4];
		ret[0] = yOffset;
		for(int i=0; i<3; i++)
			ret[i+1] = P[i];
		return ret;
	}
	
}