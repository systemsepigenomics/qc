package maModel;

//import acemap.*;
//import acemapMath.*;
import java.io.*;
import aceStyle.*;
import java.util.*;
import javax.swing.JFileChooser;
import acemapCore.*;

public class Plot{
	
	Plot(){}
	
	
	public static double ABS(double x){
		return x<0.0?-x:x;
	}
	
	public static double[][]	sample(MTable mTab, double minlS, double maxlS, double minlV, double maxlV, int gran){
		
		double	bin[][] = new double[gran][gran];
		double	v, s;
		
		double	facS = (double)(gran-1) / (maxlS - minlS);
		double	facV = (double)(gran-1) / (maxlV - minlV);
				
		for(int i=0; i<gran; i++)
			for(int j=0; j<gran; j++)
				bin[i][j] = 0.0;
		
		for(int i=0; i<mTab.size; i++){
			s = Math.log( mTab.data[i].signal );
		//	System.out.println("s  : "+mTab.data[i].signal );
		//	System.out.println("(s-minlS)*facS  : "+(s-minlS)*facS );
			v = Math.log( ABS(mTab.data[i].moq)    );
			 if(s<minlS || s>maxlS || v<minlV || v>maxlV) continue;
			 	bin[(int)( (s-minlS)*facS )][(int)( (v-minlV)*facV )  ]+=1.0;
		}
		
		double fac = 1.0 / (double)(mTab.size);
		
		for(int i=0; i<gran; i++)
			for(int j=0; j<gran; j++)
				bin[i][j] *= fac;
		
		
		
		return bin;
		
	}
	
	
	/**
	 *Permit to locate a point in a density graph
	 *@param minlS borne inf signal
	 *@param maxlS borne sup signal
	 *@param minlV borne inf variance
	 *@param maxlV borne sup variance
	 *@param gran resolution
	 *@param signal signal value
	 *@param variance variance value
	 *@return the table that contains the abscisse and the ordinate of the location
	 */
	
	public static int[]	locate(double minlS, double maxlS, double minlV, double maxlV, int gran, double signal, double variance, int width, int height){
		
		int	val[] = new int[2];
		double	ls, lv;
		
		//gran = resolution
		
	//	double	facS = (double)(gran-1) / (maxlS - minlS);
	//	double	facV = (double)(gran-1) / (maxlV - minlV);
		
		double	facS = (double)width / (maxlS - minlS);
		double	facV = (double)height / (maxlV - minlV);
		
		ls = Math.log(signal);
		lv = Math.log(ABS(variance));
		//System.out.println("LV : "+lv);
		//val[0] = (int)(0.75*facS*gran);
		val[0] = (int)((ls-minlS)*facS);
		val[1] = (int)(height-(lv-minlV)*facV);
		//System.out.println("val[1] : "+val[1]);
		
	//	val[0] = (int)(val[0] * width/(gran-1));
	//	val[1] = (int)(height - val[1] * height/(gran-1));
		
		return val;
	}
	
	
	public static void writeBins(MTable mTab, File outdir){
		double bins[][] = sample(mTab, -8.0, 8.0, -8.0, 8.0, 160);
		
		String name = mTab.name+"_surf.txt";
		
		
		try{
			FileWriter fwr = new FileWriter(new File(outdir, name));
			
			for(int i=0; i<bins.length; i++){
				fwr.write(Utils.getDecs(bins[i][0], 4, false));
				for(int j=1; j<bins[i].length; j++)
					fwr.write("\t"+Utils.getDecs(bins[i][j], 4, false));
				fwr.write(Utils.lineEnd);
				
			}
			
			fwr.close();
		}catch(Exception e){}
	}
}
	