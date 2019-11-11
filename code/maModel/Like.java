package maModel;

//import acemap.*;
import acemapMath.*;
import java.io.*;
import aceStyle.*;
import java.util.*;
import javax.swing.JFileChooser;
import acemapCore.*;


/**
 *Helper class that extracts model parameters from files inside of a directory 
 *and compiles aparameter list for them.
 *@author Sebastian Noth
 *@version 0.73
 */
class Like{
	Like(){}
	
	public static void tab(MTable mTab, File log){
		
		LNDmix lndm = new LNDmix(mTab);
		
		LNDp   D1[] = new LNDp[1];
		LNDp   D2[] = new LNDp[2];
		LNDp   D3[] = new LNDp[2];
		
		double l1, l2, l3;
		
		KeyValueMap kvm = mTab.info.kvm;
		String buff, el[];
		
		buff = kvm.valueFor("SINGLE_D_MODEL");
		el   = Utils.getElementArray(buff);
		D1[0]= new LNDp(1.0,
						Math.exp( Utils.parseFast(el[0])),
						Utils.parseFast(el[1]),
						0.0  );
						
		buff = kvm.valueFor("MIXTURE_D_MODEL");
		el   = Utils.getElementArray(buff);
		D2[0]= new LNDp(Utils.parseFast(el[0]),
						Math.exp(Utils.parseFast(el[1])),
						Utils.parseFast(el[2]),
						0.0 );
		D2[1]= new LNDp(Utils.parseFast(el[3]),
						Math.exp(Utils.parseFast(el[4])),
						Utils.parseFast(el[5]),
						0.0 );
		
		buff = kvm.valueFor("SIGNAL_DISTRIBUTION");
		el   = Utils.getElementArray(buff);
		D3[0]= new LNDp(Utils.parseFast(el[0]),
						Utils.parseFast(el[1]),
						Utils.parseFast(el[2]),
						Utils.parseFast(el[3])  );
		D3[1]= new LNDp(Utils.parseFast(el[4]),
						Utils.parseFast(el[5]),
						Utils.parseFast(el[6]),
						Utils.parseFast(el[7])  );
						
		l1   = lndm.L(D1);				
		l2   = lndm.L(D2);				
		l3   = lndm.L(D3);
		
		FileWriter fwr;
		try{
			fwr = new FileWriter(log, true);
			fwr.write(mTab.name	+"\t"+Utils.getDecs(l1, 3, false)
								+"\t"+Utils.getDecs(l2, 3, false)
								+"\t"+Utils.getDecs(l3, 3, false)
								+Utils.lineend );
			fwr.close();
		}catch(Exception e){System.out.println(e.toString());}				
						
		
		
	}
	
	
	
}
	