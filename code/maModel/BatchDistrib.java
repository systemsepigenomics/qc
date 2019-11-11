package maModel;

//import acemap.*;
import acemapMath.*;
import acemapCore.*;
import aceStyle.*;
import java.io.*;

public class BatchDistrib{
	
	public BatchDistrib(){}
	
	public static void main(String args[]){
		AceUtil.init(null);
		
		
		File  bDir   = Utils.getUserFile("set source directory", true);
		File  data[] = bDir.listFiles();
		
		File  resDir = Utils.getUserFile("set result directory", true);
		resDir.mkdir();
					
		FileWriter fwr1=null, fwr2=null;
		
		File fo1 = new File(resDir, "data.txt"  ); 
		File fo2 = new File(resDir, "curves.txt");
				
		
		MTable 			mTab;
		KeyValueMap	kvm;
		CompositeMf	cmf;
		
		String arr1[], arr2[];
		
		double res = 0.25;
		double sta = -8.0;
		int    no  = 64;
				
		try{
			fwr1 = new FileWriter(fo1);
			fwr2 = new FileWriter(fo2);
						
			fwr1.write("FILE\tSGL_m\tSGL_v\tSGL_L\tMIX_f1\tMIX_m1\tMIX_s1\tMIX_f2\tMIX_m2\tMIX_s2\tMIX_L\tMIX_iter"+Utils.lineend);
			
			for(int i=0; i<=no; i++)
				fwr2.write("\t"+Utils.getDecs( sta+ res*(double)i, 2, false));
			
			fwr2.write(Utils.lineend);
			fwr1.close();
			fwr2.close();
		
		}catch(Exception e){}
		
		
		
		double m,v,L1,f1,m1,v1,f2,m2,v2,L2;
		int it;
		
		double his[], buff[];
		
		
		for(int i=0; i<data.length; i++){
			
				
			
			if(data[i].isDirectory()) continue;
						
			cmf  = new CompositeMf();
			cmf.load(data[i], true, false, false);
					
			mTab = new MTable(cmf.F_ma, null, false, cmf, true);
			kvm  = mTab.info.kvm;
			
			//System.out.println("single dmodel :"+kvm.valueFor("SINGLE_D_MODEL"));
			
			arr1 = Utils.getElementArray( kvm.valueFor("SINGLE_D_MODEL")  );
			arr2 = Utils.getElementArray( kvm.valueFor("MIXTURE_D_MODEL") );
			
			buff = new double[mTab.size];
			
			for(int j=0; j<buff.length; j++){
				if(mTab.data[j].signal<=0.0f)
					buff[j] = Math.log(0.00001);
				else
					buff[j] = Math.log((double)mTab.data[j].signal);
			}
			
			
			Utils.qsortD(buff);
			
			his = Utils.histo(buff, sta-res*0.5, res, no+1); 
			
			double temp;
			
			
			m  = Utils.parseFast(arr1[0]);
			v  = Utils.parseFast(arr1[1]);
			L1 = Utils.parseFast(arr1[2]);
			
			f1 = Utils.parseFast( arr2[0]);
			m1 = Utils.parseFast( arr2[1]);
			v1 = Utils.parseFast( arr2[2]);
			f2 = Utils.parseFast( arr2[3]);
			m2 = Utils.parseFast( arr2[4]);
			v2 = Utils.parseFast( arr2[5]);
			
			L2 = Utils.parseFast( arr2[6]);
			it = Utils.parseFastI(arr2[7]);
						
			try{
				
				fwr1 = new FileWriter(fo1, true);
				fwr2 = new FileWriter(fo2, true);
				
				
				fwr1.write(mTab.name	+"\t"+arr1[0]
										+"\t"+arr1[1]
										+"\t"+arr1[2]
										
										+"\t"+arr2[0]
										+"\t"+arr2[1]
										+"\t"+arr2[2]
										+"\t"+arr2[3]
										+"\t"+arr2[4]
										+"\t"+arr2[5]
										+"\t"+arr2[6]
										+"\t"+arr2[7] + Utils.lineend );
					
				
				// histogram
				fwr2.write(mTab.name+"[hist]");
				for(int j=0; j<his.length; j++)
					fwr2.write("\t"+Utils.getDecs(his[j], 4, false));
				fwr2.write(Utils.lineend);					
				
				// single distribution model					
				fwr2.write(mTab.name+"[single]");
				for(int j=0; j<his.length; j++){
					temp = Utils.normDist( sta+ res*(double)j , m, v);
					fwr2.write("\t"+Utils.getDecs(temp, 4, false));
				}
				fwr2.write(Utils.lineend);	
				
				// single distribution model					
				fwr2.write(mTab.name+"[mix]");
				for(int j=0; j<his.length; j++){
					temp =	f1 * Utils.normDist( sta+ res*(double)j , m1, v1) +
							f2 * Utils.normDist( sta+ res*(double)j , m2, v2) ;
					fwr2.write("\t"+Utils.getDecs(temp, 4, false));
				}
				fwr2.write(Utils.lineend);	
				
				fwr1.close();
				fwr2.close();
								
			}catch(Exception e2){}
			
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>> accomplished for file "+mTab.name+" ("+(i+1)+" of "+data.length);
		}
		
			
	}	
	
}