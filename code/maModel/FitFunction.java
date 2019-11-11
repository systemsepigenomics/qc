package maModel;

//import acemapMath.*;
import acemapCore.*;;

/**
 *For any extending class that implements the function err(),
 *minimizing parameters can be calculated using fit() and
 *appropriate initial parameters
 *@author Sebastian Noth
 *@version 0.58
 */ 
public abstract class FitFunction{
	protected	double delta = 0.0001;
	protected	double lasterr;
	protected	double[]	lastP;
	
	/**
	 *the only abstract function that must be implemented by extensions
	 *@param p : the parameter vector
	 *@return the error value attributed to the parameter vector
	 */
	protected	abstract double		err(double p[]);
	
	protected	double		SQR(double x){ return x*x; }
	protected	double		ABS(double x){ return x<0.0?-x:x; }
	/**
	 * returns an exct copy of vector p allocating
	 *new memory rather than supplying a reference
	 */
	protected	double[]	copy(double p[]){
		double ret[] = new double[p.length];
		for(int i=0; i<p.length; i++)
			ret[i] = p[i];
		return ret;	
	}
	/**
	 * returns the point that lies in the middle of the line connecting x and y
	 */
	private		double[]	mean(double x[], double[] y){
		double ret[] = new double[x.length];
		for(int i=0; i<x.length; i++)
			ret[i] = 0.5*x[i] + 0.5*y[i];
		return ret;
	}
	/**
	 *euclidic distance between x and y
	 */
	protected	double		dist(double x[], double y[]){
		double acc = 0.0;
		for(int i=0; i<x.length; i++)
			acc += SQR(x[i]-y[i]);
			
		return Math.sqrt( acc );
	}
	/**
	 *returns a String expression of the vector x displaying d decimals 
	 */
	protected	String		toString(double x[], int d){
		String ret = new String("["+Utils.getDecs(x[0], d, false) );
		for(int i=1; i<x.length; i++)
			ret = ret.concat(" ; "+Utils.getDecs(x[i], d, false) );
		ret = ret.concat("]");
		
		return ret;
	}
	
	/**
	 * calculates an approximation of the partial derivative after 
	 *the ip'th parameter using difference quotients
	 *@param p : the parameter vector
	 *@param ip : index of the parameter of the partial derivative\
	 *@return the partial derivative value
	 */
	protected	double		deriv(double p[], int ip){
		double pc[] = copy(p);
		pc[ip] += delta;
		return (err(pc) - err(p) )/delta;
	}
	/**
	 * calculates an approximation of the first derivative vector
	 */
	protected	double[]	deriv(double p[]){
		double ret[] = new double[p.length];
		double err0  = err(p);
		double pc[];
				
		for(int i=0; i<ret.length; i++){
			pc     = copy(p);
			pc[i] += delta;
			ret[i] = (err(pc) - err0) / delta; 	
		}
		return ret;
	}
	/**
	 *calculates the abscissa of a parabol's peak.
	 *Used to improve scan line minimum estimation.
	 *@param e1 ~ordinate of the first point that defines the parabol. The associated abscissae are -1, 0, 1 (in that order).
	 *@param e2 ~ordinate of the second point that defines the parabol. The associated abscissae are -1, 0, 1 (in that order).
	 *@param e3 ~ordinate of the third point that defines the parabol. The associated abscissae are -1, 0, 1 (in that order).
	 *@return the abscissa of the peak of the parabol defined by three points [-1, e1], [0, e2], [1, e3]
	 */
	protected	double		getMinimum(double e1, double e2, double e3){
		double m = -0.5*(e3-e1)/((e1+e3)-2.0*e2);
		return m;
	}
	/*
	protected	double[]	projection(double x[], double L[]){
		double part  = Utils.scalar(x, L);
		double ret[] = Utils.toUnit(Utils.add( x, Utils.mult(L, -part)));
		return ret;
	}*/
	
	/** minimizes the error improving the parameter vector estimate x
	 *@param x : initial parameter vector
	 *@param conv : convergence limit. corresponds to the maximum error difference between two scan cycles for which the process is ended (positive criterion).
	 *@param maxcycles : maximum number of scan cycles that are performed (negative criterion). 
	 *@return the final parameter vector
	 */
	public		double[] 	fit(double x[], double conv, int maxcycles){
		int j, cycle=0;
			
		double errB, errS, errO, errH;	// base, search and last error
		double x0[], x1[], x0h[];		// base and search parameter vector
		double g[], d[], glen, dlen;	// gradient
		double step;					// steplength
		double e1, e2, e3, mpos;		// for quadratic minimum estimation
		d=null;
		
		double	de[];
		
		x0   = copy(x);
		errS = err(x0);
		
		double cErr = errS;
		
		double cEro = 0.0; // compiler
		
		//*DEB*/System.out.println("#### GRA START #### error = "+Utils.getDecs(errS, 6, false));         //+this.getClass().getName());
			
		int dir = 0;
		int dim = x.length;
		
		double part;
		double obase[][] = new double[x.length][x.length];
		
		int    breaks = 0;
			
		// main loop. needs breaking criterion
		do{
			// save last error
			errO = errS;
						
			///////// SCANLINE DETERMINATION ////////////////////////////////////
			//
			// GRADIENT
			g    = Utils.mult(deriv(x0), -1.0);
			glen = Math.sqrt( Utils.scalar(g,g) );
			if(glen < conv){
				System.out.println("glen ="+glen);
			}
			if(glen!=0.0)
				g    = Utils.mult(g, 1.0/glen);
			
						
			// INIT. USE GRADIENT DIRECTLY
			if(dir==0){ 
				d        = copy(g);
				obase[0] = copy(g);
				cEro = cErr;
				cErr = cEro - 1000.0;
			}
			else{
				if(dir<dim){
					double[] d2 = copy(obase[dir-1]);
					//*DEB*/System.out.println("last d="+toString(d2, 3));
					//*DEB*/System.out.println("     g="+toString(g , 3));
									
					//d = copy(g);
					
					de = Utils.mult(obase[0], Utils.scalar(g, obase[0] ));
					
					for(int i=1; i<dir; i++){
						de = Utils.add(de, Utils.mult(obase[i],  Utils.scalar(g, obase[i] ) / Utils.scalar(obase[i], obase[i] ) ));
					}
					
									
															
					obase[dir] = Utils.add(g, Utils.mult(de, -1.0)   ) ;
					
					d = Utils.toUnit( obase[dir] );
					
					
					if(ABS(Utils.scalar(d, d2))>0.001){
						//System.out.println("not ortho: "+dir+" :"+Utils.scalar(d, d2)+", "+Utils.scalar(d2,g));					
						d        = copy(g);	
						obase[0] = copy(g);
						dir      = 0;
						cEro = cErr;
						cErr = cEro - 1000.0;
					}
				
				
				}
				else{
					System.out.println("undefined");	
					d = null;
				}
			}
						
			if(Utils.scalar(d, d) ==0.0){
				d        = copy(g);
				obase[0] = copy(g);
				cEro     = cErr;
				cErr     = cEro - 1000.0;
				dir      = 0;
			}
			
								
			// base error
			errB = errS;
			//*DEB*/System.out.println("#### GRA #### CYCLE "+cycle);		
			//*DEB*/System.out.println("#### GRA #### x="+toString(x0,3)+", err="+Utils.getDecs(errB, 3, false) );			
			//*DEB*/System.out.println("#### GRA #### d="+toString(d,3) );			
					
			step = 0.01;
			// inner loop
			
			x1   = Utils.add(x0, Utils.mult(d , step  ));
			errS = err(x1);
			//*DEB*/System.out.println("errS="+Utils.getDecs(errS,0,false));
			
			
			// seek from right to left
			if(errS >= errB){
				e1 = errB;
				e2 = errS;
				e3 = 0.0;  // for compiler
				
				j=0;
				while(errS >= errB && j<10){
					step = step * 0.5;
					x1   = Utils.add(x0, Utils.mult(d , step));
					errS = err(x1);
					// narrow interval
					e3   = e2;
					e2   = errS;
					j++;
				}
								
				if(j>=10){
					breaks++;
					//*DEB*/System.out.println("breaking at dir="+dir+" (cycle "+cycle+")");
					//*DEB*/System.out.println("#### GRA <<<< in cycle "+cycle+" BACK search : performed "+j+" steps");	
					//*DEB*/System.out.println("#### GRA STOP #### (reduced stepsize to"+step+")");
					//*DEB*/return x0;
				}
				else{
					breaks = 0;
					
					// calculate quadratic estimate of minimum
					//                      e(x0)  e(x1)  e(last_x1)
					mpos = 1.0 + getMinimum(e1,    e2,    e3        );
				
					// x0 estimate for next step	 
					x0h   = Utils.add(x0, Utils.mult(d, step*mpos));
					errH  = err(x0h);
					
					//*DEB*/System.out.println("mpos="+mpos+", e2="+e2+", errH="+errH);
					
					if(errH<e2){
						//*DEB*/System.out.println("#### GRA #### resQ found new x0");
						
						//*DEB*/System.out.println("d(0)="+toString(d, 4)+" l="+dist(x0, x0h));
						
						x0   = copy(x0h);
						errS = errH;
					}
					else{
						
						///*DEB*/System.out.println("d(1)="+toString(d, 4)+" l="+dist(x0, x1));
						
						x0   = copy(x1);
						errS = e2;	
					}
					//*DEB*/System.out.println("#### GRA <<<< in cycle "+cycle+" BACK search : performed "+j+" steps");	
				}
			}
			else{ // seek from left to right
				breaks = 0;
				e1     = 0.0;  // for compiler
				e2     = errB;
				e3     = errS;
				
				j    = 1;
				while(e3<e2){
					j++;
					// new x1 position on scanline					
					x1 = Utils.add(x0, Utils.mult(d , step * (double)j    ));
					// propagate error history					
					e1   = e2;
					e2   = e3;
					e3   = err(x1);
					
					if(j>150)
						System.out.println("j="+j+" d= "+toString(d,5));
										
					//*DEB*/System.out.println("step="+step+" j="+j+", x= "+toString(x,3)+", err="+Utils.getDecs(err(x2), 0, false)     );	
				}
				
				// calculate quadratic estimate of minimum
				//                                e(x0(t-2))  e(x1(t-1))  e(x1(t))
				mpos = (double)(j-1) + getMinimum(e1,         e2,         e3        );
				
				// x0 estimate for next step	 
				x0h   = Utils.add(x0, Utils.mult(d, step*mpos));
				errH = err(x0h);
				
				if(errH<e2){
					//*DEB*/System.out.println("#### GRA #### resQ found new x0");
					
					//*DEB*/System.out.println("d(2)="+toString(d, 4)+" l="+dist(x0, x0h));
				
					
					
					x0   = copy(x0h);
					errS = errH;
				}
				else{
					
					//*DEB*/System.out.println("d(3)="+toString(d, 4)+" l="+dist(x0, Utils.add(x0, Utils.mult(d , step * (double)(j-1)))));
													
									
					x0   = Utils.add(x0, Utils.mult(d , step * (double)(j-1)));
					errS = e2;	
				}
								
				//*DEB*/System.out.println("#### GRA >>>> in cycle "+cycle+" FWD search : performed "+j+" steps");	
			}
						
			//*DEB*/System.out.println("#### GRA ____ result : error = "+Utils.getDecs(errS, 2, false)+" (delta ="+Utils.getDecs(errO-errS, 3, false)+")");
			
			if(dir==dim-1)
				cErr = errS;
			
			cycle++;
			dir = (dir+1)%dim;
			
			//*DEB*/System.out.println("c "+cycle+" : "+toString(x0, 5)+", err="+Utils.getDecs(errS, 3, false));
			
		}while(cycle<maxcycles && cEro-cErr>conv && breaks<dim);
		
		if(breaks>=dim)
			System.out.println("converged");
		
		/*DEB*/System.out.println("#### GRA STOP #### after "+cycle+" cycles, x="+toString(x0,3)+", err="+Utils.getDecs(errS, 6, false) );
		//*DEB*/System.out.println("#### GRA STOP #### error = "+Utils.getDecs(errS, 2, false));
		//*DEB*/System.out.println("<");
		lasterr = errS;
		lastP = copy(x0);
		return x0;
	}
	/**
	 *returns a copy of the last minimal parameter vector
	 */
	public		double[]	getX(){
		return copy(lastP);
	}
	/**
	 *returns the last calculated error. (equivalent to err(getX()), 
	 *but uses a saved value)
	 */
	public		double		getLastErr(){
		return lasterr;
	}
	
}
