package maModel;

//import acemap.*;
import acemapMath.*;
import java.io.*;
import aceStyle.*;
import acemapCore.*;


public class BatchAllSigDist{
	
	public static void main(String args[]){
		
		AceUtil.init(null);
		
		File dir = Utils.getUserFile("set input directory", true);
		File out = Utils.getUserFile("set output file", false);
		
		File data[] = dir.listFiles();
		
		CompositeMf   cmf;
		FileWriter     fwr=null;
		KeyValueMap  kvm;
		MTable         mTab;
		
		String		   set1, set2, set3;
		
		try{
			fwr = new FileWriter(out);
		}catch(Exception e){}
		
		
		for(int i=0; i<data.length; i++){
			
			if(data[i].isDirectory()) continue;
						
			cmf  = new CompositeMf();
			cmf.load(data[i], true, false, false);
					
			mTab = new MTable(cmf.F_ma, null, false, cmf, true);
			kvm  = mTab.info.kvm;
			
			//System.out.println("single dmodel :"+kvm.valueFor("SINGLE_D_MODEL"));
			
			set1 =  kvm.valueFor("SINGLE_D_MODEL");
			set2 =  kvm.valueFor("MIXTURE_D_MODEL");
			set3 =  kvm.valueFor("SIGNAL_DISTRIBUTION");
			
			try{
				fwr.write(mTab.name+"\t"+set1+"\t"+set2+"\t"+set3+Utils.lineend);
			}catch(Exception e2){}
		}
		
		try{
			fwr.close();
		}catch(Exception ex2){}
	}
}