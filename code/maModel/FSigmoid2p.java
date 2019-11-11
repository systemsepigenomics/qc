package maModel; 

//import acemapMath.*;
import aceStyle.*;


/**
 *Implementation for a standard two parameter sigmoid.
 *@author Sebastian Noth
 *@version 0.58
 */
public class FSigmoid2p extends FitFunction{
	
	private double	X[], Y[];
	private double	P[];
	
	public FSigmoid2p(double X[], double Y[]){
		this.X = X;
		this.Y = Y;
		
		P = new double[2];
		P[0] =  0.0;
		P[1] = -1.0;
	}
	public FSigmoid2p(double P[]){
		this.P = copy(P);
	}
	
	public	void	setY(double Y[]){
		this.Y = Y;
	}
	public	void	setP(double p[]){
		P = copy(p);
	}
	
	public double	err(double p[]){
		double acc = 0.0;
		
		//*DEB*/System.out.println("len="+X.length);
		
		for(int i=0; i<X.length; i++){
			//if(Double.isNaN(Y[i])) System.out.println("herer");
			acc += SQR(Y[i] - f(X[i], p));
		}
		//System.out.println("err="+Utils.getDecs(acc, 3, false)+" : "+toString(p, 3));
		return acc;
	}
		
	public	double	f(double x, double p[]){
		return 1.0/(1.0+Math.exp(-(x-p[0])*p[1]));
	}
	public	double	f(double x){
		return f(x, P);
	}
	
	public	double[]	fit(){
		P = fit(P, 0.0001, 100);
		
		System.out.println("x0="+P[0]+", slope="+P[1]);
		
		return P;
	}
	
	public	double[]	getP(){
		return copy(P);
	}
}