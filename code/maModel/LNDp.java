package maModel;

//import acemap.*;
//import acemapMath.*;
import java.io.*;
import aceStyle.*;
import java.util.*;
import javax.swing.JFileChooser;
import acemapCore.*;

/**
 *Log normal distribution parameter storage object
 *@author Sebastian Noth, Guillaume Brysbaert
 *@version 0.58
 */
public class LNDp{
	public double f, m, s, x0;
	public double logm;
	
	private final double sqrt2pi = 2.506628274631;
	
	private double SQR(double x) {return x*x;}
	
	/**
	 *default constructor
	 */
	public LNDp(){
		f = 0.5;
		m = 1.0;
		s = 1.0;	
		x0= 0.0;
	}
	/**
	 *Constructor that initializes all parameters
	 *@param f : the probability of this distribution in case of mixture distributions. Set to 1 for the single distribution case.
	 *@param m : the exponent of the mean
	 *@param s : the (unsquared) variance
	 *@param x0 : the x0 value
	 */
	public LNDp(double f, double m, double s, double x0){
		this.f = f;
		this.m = m;
		this.s = s;
		this.x0= x0;
		logm = Math.log(m);
	}
	
	public double getF()
	{
		return f;
	}

	public double getM()
	{
		return m;
	}
	
	public double getS()
	{
		return s;
	}
	
	public double getX0()
	{
		return x0;
	}
	
	/**
	 *The probability density at x
	 */
	double p(double x){
		if(x<=x0)
			return 0.0;
		else
			return f / ((x-x0)*sqrt2pi*s) * Math.exp( -SQR(Math.log((x-x0)/m))/(2.0*s*s)); 	
	}
	/**
	 *Returns the logarithm of the x0-subtracted value x
	 */
	double log(double x){
		if(x<=x0) 
			return 0.0;
		else
			return Math.log(x-x0);
	}
}
	