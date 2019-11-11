package maModel;

//import acemap.*;
import acemapMath.*;
import java.io.*;
import aceStyle.*;
import java.util.*;
import javax.swing.JFileChooser;
import acemapCore.*;


/**
 *Object representing a mixture distribution composed of normal distributions (gaussians).
 *
 *The distribution parameters are estimated using an EM algorithm. Defining data is read
 *from an MTable object.
 *@author Sebastian Noth
 *@version 0.73
 */
class NDmix{
	
	NDmix(double dat[]){
		this.dat = dat;
		
		ndp = new NDp[2];
		ndp[0] = new NDp(0.5, -0.1, 1.0);
		ndp[1] = new NDp(0.5,  0.1, 1.0);
	}
	
	NDmix(MTable mTab){
		dat = new double[mTab.size];
		for(int i=0; i<dat.length; i++)
			dat[i] = Math.log(mTab.data[i].signal);
	}
	
	public void		setParameters(NDp[] ndp){
		this.ndp = ndp;
	}
	
	private NDp		ndp[];
	private double	dat[];
	private	double	logL;
	
	private double	SQR(double x){
		return x*x;
	}
	
	private double	Pi(int k, double x){
		return ndp[k].f * Utils.normDist(x, ndp[k].m, ndp[k].s);
	}
	private double	Pt(double x){
		double res=0.0;
		
		for(int i=0; i<ndp.length; i++){
			res += Pi(i, x);
		}
			
		return res;	
	}
	
	// calculates the log Likelihood of the current mixed distribution, given data dat
	public	double	L(){
		
		double res = 0.0;
		
		for(int i=0; i<dat.length; i++)
			res += Math.log(Pt(dat[i]));
		return res;
	}
	
	public double	L(NDp[] ndpM){
		NDp[] temp = ndp;
		ndp = ndpM;
		
		double res = L();
		
		ndp = temp;	
		
		return res;
	}
	
	
	
	public	void	EM(){
		double ws[] = new double[ndp.length];
		
		double w;
		double PTot;
		
		double div = (double)(dat.length);
		
		
		NDp ndpNew[] = new NDp[ndp.length]; 
						
		for(int i=0; i<ws.length; i++){
			// initialize weight sums
			ws[i] = 0.0;
			// initialize new parameter vector
			ndpNew[i] = new NDp();
		}

				
		for(int i=0; i<dat.length; i++){
			// calculate function sum for data point i
			PTot = Pt(dat[i]);
			
			for(int j=0; j<ndpNew.length; j++){
				w = Pi(j, dat[i]) / PTot ;
				//   ndp[j].f /
				
				
				if(w>1.0)	System.out.println("pT="+Pt(dat[i])+", p1="+Pi(0, dat[i])+", p2="+Pi(1, dat[i])+", w="+w );
								
				ws[j] += w;
								
				ndpNew[j].f += w;
				
				ndpNew[j].m += w*dat[i] ; 
				ndpNew[j].s += w*SQR(dat[i]-ndp[j].m);
			
			
			}
			
		}
		
		for(int i=0; i<ndpNew.length; i++){
			ndpNew[i].m/=ws[i];
			ndpNew[i].s/=ws[i];
			ndpNew[i].f/=div;
		
			ndpNew[i].s = Math.sqrt(ndpNew[i].s);		
		}
		
		
		
		
		ndp = ndpNew;
		
	}
	
	public	double	EMLike(){
		EM();
		double Like = L();
		
		
		System.out.println("D1 ("+Utils.getDecs(ndp[0].f, 3, false)+"; "
								 +Utils.getDecs(ndp[0].m, 3, false)+", "
								 +Utils.getDecs(ndp[0].s, 3, false)
					   +"), D2 ("+Utils.getDecs(ndp[1].f, 3, false)+"; "
					             +Utils.getDecs(ndp[1].m, 3, false)+", "
					             +Utils.getDecs(ndp[1].s, 3, false)
					     +"), L="+Utils.getDecs(Like,     3, false) );
		
		
		
		
		return Like;
	}
	
	public	void run(){
		ndp    = new NDp[2];
		ndp[0] = new NDp(0.5, -1.0, 1.0);
		ndp[1] = new NDp(0.5,  1.0, 1.0);
				
		double conv = 0.000001;
		int maxiter = 150;
		
		int    iter = 0;
		double Like = -1000000000.0;
		double lastLike;
		
		do{
			lastLike = Like;
			Like = EMLike();
			System.out.println("step "+iter+": L="+Utils.getDecs(Like, 3, false));
			iter++;
			
		}while(iter<maxiter && Like-lastLike>conv); 
				
		
	}
	
	
	public	NDp[]	getParams(){
		return ndp;
	}
}
	