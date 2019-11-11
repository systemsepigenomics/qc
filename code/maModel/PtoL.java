package maModel;

//import acemap.*;
import acemapMath.*;
import java.io.*;
import aceStyle.*;
import java.util.*;
import javax.swing.JFileChooser;
import acemapCore.*;


/**
 * helper class that creates three lists (files) from annotation parametrs from ma0 files in a directory 
 *@author Sebastian Noth
 *@version 0.73
 */
class PtoL{
	PtoL(){}
	
	
	public static void append(MTable mtab, File f){
		String b1, b2, b3, b4, b5;
		
		
		try{
			FileWriter fwr = new FileWriter(f, true);
			
			b1 = mtab.info.kvm.valueFor("SIGNAL_DISTRIBUTION");
			b2 = mtab.info.kvm.valueFor("VAR_BLEND");
			b3 = mtab.info.kvm.valueFor("VAR_MODEL1");
			b4 = mtab.info.kvm.valueFor("VAR_MODEL2_m");
			b5 = mtab.info.kvm.valueFor("VAR_MODEL2_s");
			
			
			
			
			fwr.write(mtab.name+"\t"+b1+"\t"+b2+"\t"+b3+"\t"+b4+"\t"+b5+Utils.lineend);
			
			fwr.close();
	
		}catch(Exception e){System.out.println(e.toString()); }
		
		
		
		
	}
	
	public static void main(String args[]){
		AceUtil.init(null);

		
		File dirin  = Utils.getUserFile("set input directory", true);
		
		File fin[]  = dirin.listFiles();
		
		File dirout = Utils.getUserFile("set output directory", true);
				
		FileWriter w1, w2, w3;
		File l1, l2, l3;
		
		l1 = new File(dirout, "err_Fx0");
		l2 = new File(dirout, "err_Fvar");
		l3 = new File(dirout, "err_Like");
		
		BBReader bbr;
		String name, buff, el[];
		
		try{
			
			w1 = new FileWriter(l1);
			w2 = new FileWriter(l2);
			w3 = new FileWriter(l3);
					
			
			for(int i=0; i<fin.length; i++){
				if(fin[i].isDirectory()) continue;
				
				bbr  = new BBReader(fin[i]);
				name = fin[i].getName();
				name = name.substring(0, name.length()-8); 
				
				w1.write(name);
				w2.write(name);
				w3.write(name);
				
				// skip headline
				bbr.getToNextRET();	
								
				while(true){
					buff = bbr.getToNextRET();
					if(buff==null) break;
					el = Utils.getElementArray(buff);
					if(el==null || el.length!=3) break;
					
					w1.write("\t"+el[0]);
					w2.write("\t"+el[1]);
					w3.write("\t"+el[2]);
				}
				
				w1.write(Utils.lineend);
				w2.write(Utils.lineend);
				w3.write(Utils.lineend);
				
				bbr.close();
			}
			
			w1.close();
			w2.close();
			w3.close();
			
			
			
			
		}catch(Exception e){}

		
	}
	
	
	
	
}
	