package maModel;

import acemapCore.*;
import java.io.*;

/**
 *Provides functionality to calculate the probability of a signal to ly in the
 *second contributing distribution of a LogNormal mixture distribution.
 *@author Sebastian Noth
 *@version 0.58
 */
public class PLNDist{
	
	private LNDp	D[];
	/**
	 *number of sample intervals
	 */
	private final int 		polySteps  = 200;	
	/**
	 *distribution sample range : a distribution [m, v] will be sampled in the interval
	 *[m-v*polyBord; m+v*polyBord]
	 */
	private final double	polyBord = 5.0;
	
	public PLNDist(LNDp[]	dis){
		D = dis;
	}
	
	private	double	pD2(double x){
		double p1 = D[0].p(x);
		double p2 = D[1].p(x);
		
		if(p1==0.0 && p2==0.0) return 0.0;
		
		return p2/(p1+p2);
	}
	
	
	private double SQR(double x){
		return x*x;
	}
		
	public float	a_value(float signal, float normvar){
		double sig = signal;
		double var = signal*normvar;
		
		double start = sig-polyBord*var;
		// step width
		double len   = (2.0*polyBord*var) / (double)polySteps;
		
		double acc = 0.5*len*pD2(start)*Math.exp(-SQR(start-sig)/(2.0*var*var))/(Utils.sqrt2pi*var);
		double x;
						
		for(int i=1; i<polySteps; i++){
			x = start + len*(double)i;
			acc += (len*pD2(x)*Math.exp(-SQR(x-sig) / (2.0*var*var))/(Utils.sqrt2pi*var));
		}
			
		x = sig+ var*polyBord;
		acc += (0.5*len*pD2(x)*Math.exp(-SQR(x-sig)/(2.0*var*var))/(Utils.sqrt2pi*var));
			
		return (float)acc;
	}
}		
	