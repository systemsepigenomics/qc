package maModel;

//import acemap.*;
import acemapMath.*;
import acemapCore.*;
import aceStyle.*;
import java.io.*;


/**
 * helper class to calculate and write signal bandwidths of files in a directory 
 *@author Sebastian Noth
 *@version 0.73
 */
public class BatBandwidth{
	
	public BatBandwidth(){}
	
	public static void main(String args[]){
		AceUtil.init(null);
		
		
		File  bDir   = Utils.getUserFile("select source data directory", true);
		File  data[] = bDir.listFiles();
		
		File  resDir = new File(bDir, "results");
		resDir.mkdir();
					
		FileWriter fwr=null;
		
		File out = new File(resDir, "bandwidths.txt"  ); 
				
		
		MTable 			mTab;
		KeyValueMap	kvm;
		CompositeMf	cmf;
		
		double perc01, perc99, perc01_034, perc99_034;
								
		try{
			fwr = new FileWriter(out);
						
			fwr.write("FILE\tPERC01\tPERC99\tLEN\tPERC01_034\tPERC99_034\tLEN_034"+Utils.lineend);
			fwr.close();
		
		}catch(Exception e){}
		
			
		
		for(int i=0; i<data.length; i++){
			if(data[i].isDirectory()) continue;
						
			cmf  = new CompositeMf();
			cmf.load(data[i], true, false, false);
					
			mTab = new MTable(cmf.F_ma, null, false, cmf, true);
			
			mTab.sortBySignal();
			perc01 = mTab.getSignalAt(0.025f);
			perc99 = mTab.getSignalAt(0.975f);
			
			mTab.sortByMOQ();
			int len=mTab.size;
			while(mTab.data[len-1].moq>=0.34) len--;
			
			mTab.sortBinBySignal(0, len-1);
			
			perc01_034 = mTab.data[ (int)(0.025* (double)(len-1)) ].signal; 
			perc99_034 = mTab.data[ (int)(0.975* (double)(len-1)) ].signal; 
			
								
			try{
				
				fwr = new FileWriter(out, true);
								
				fwr.write(mTab.name	+"\t"+Utils.getDecs(perc01,     4, false)
									+"\t"+Utils.getDecs(perc99,     4, false)
									+"\t"+mTab.size
									+"\t"+Utils.getDecs(perc01_034, 4, false)
									+"\t"+Utils.getDecs(perc99_034, 4, false)
									+"\t"+len
									+ Utils.lineend );
									
				fwr.close();
								
			}catch(Exception e2){}
			
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> accomplished for file "+mTab.name+" ("+(i+1)+" of "+data.length);
		}
	}

}