package maModel;

//import acemap.*;
import acemapMath.*;
import acemapCore.*;
import aceStyle.*;
import java.io.*;
import java.lang.*;


/**
 * helper class that calculates neonex variance-mean-estimates.
 *@author Sebastian Noth
 *@version 0.73
 */
public class BatchNeonexVarEstim{
	
	
	public static void main(String args []){
		
		AceUtil.init(null);
		
		File dir = Utils.getUserFile("set input directory", true);
		File out = Utils.getUserFile("set output file", false);
		
		File data[] = dir.listFiles();
		
		CompositeMf   cmf;
		FileWriter     fwr=null;
		MTable         mTab;
		
		double         logsig[], logvar[], yOff;
		
		try{
			fwr = new FileWriter(out);
			fwr.write("file\ty_off\tscale\tx_off\ty0\ty0'"+Utils.lineend);
			fwr.close();
		}catch(Exception e){}
		
		FNeonex4p	neonex;
		
		
		for(int i=0; i<data.length; i++){
			if(data[i].isDirectory()) continue;
						
			cmf  = new CompositeMf();
			cmf.load(data[i], true, false, false);
					
			mTab = new MTable(cmf.F_ma, null, false, cmf, true);
			mTab.sortBySignal();
			
			
			logsig = new double[mTab.size];
			logvar = new double[mTab.size];
			
			for(int j=0; j<mTab.size; j++){
				logsig[j] = Math.log(mTab.data[j].signal);
				logvar[j] = Math.log(mTab.data[j].moq   );
			}
			
			int st=0;
			while(logsig[st]<4.0) st++;
			double acc = 0.0;
			for(int j=st; j<logvar.length; j++)
				acc += logvar[j];
			yOff = acc / (double)(logvar.length-st);
			
			double p0[] = new double[4];
			p0[0] =  3.0;
			p0[1] =  0.0;
			p0[2] =  0.5;
			p0[3] = -1.0;
			
			
			neonex = new FNeonex4p(logsig, logvar, yOff, false);
			neonex.setP(p0);
			neonex.fit(0.0001, 150);
			
			
			double res[] = neonex.get5P();
			
			System.out.println("sleep after file "+i);
			try{
				fwr = new FileWriter(out, true);
				fwr.write(mTab.name	+"\t"+Utils.getDecs(res[0], 5, false)
									+"\t"+Utils.getDecs(res[1], 5, false)
									+"\t"+Utils.getDecs(res[2], 5, false)
									+"\t"+Utils.getDecs(res[3], 5, false)
									+"\t"+Utils.getDecs(res[4], 5, false)
									+Utils.lineend                        );
				fwr.close();
				
				
				Thread.sleep(30000);
				
			}catch(Exception ex){}	
			System.out.println("wake");
			
		}
	}
}