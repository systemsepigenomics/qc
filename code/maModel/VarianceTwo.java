package maModel;

import acemapCore.*;
import acemapMath.*;
import java.util.*;

/**
 *Used to sample signal deviation over replicate data sets
 *@author Sebastian Noth
 *@version 0.73
 */
public class VarianceTwo{
	private	Vector		inp;

	public VarianceTwo(){
		inp = new Vector();
	}
	
	public void	addTable(MTable m){
		inp.add(m);
	}
	

	
	public MTable	go(){
		int size = inp.size();
		MTable tab[] = new MTable[size];
		
		for(int i=0; i<tab.length; i++){
			tab[i] = (MTable)(inp.get(i));
			tab[i].sortBySignal();
		}
		
		int rlen = TBufTool.countIntersection(tab);
		MTable res = new MTable(rlen, 0);
		TBufTool tbt = new TBufTool();
		tbt.prepareWalk(tab);	
		
		int cnt=0;
		int curs[];
		float mean, var;
		
		String kontroll="";
		
		while( (curs=tbt.nextIntersect()) != null){
			mean = 0.0f;
			var  = 0.0f;
			
			for(int i=0; i<size; i++){
				if(curs[i] == -1)
					return res;
				
				if(i==0)
					kontroll = tab[0].data[curs[0]].name;
				else	
					if(kontroll.compareTo(tab[i].data[curs[i]].name) != 0)
						System.out.println("nnn...");
				
				
				mean += tab[i].data[curs[i]].signal;
			}
			mean = mean / (float)size;
			for(int i=0; i<size; i++)
				var += SQR(tab[i].data[curs[i]].signal - mean);
			var = (float)Math.sqrt(var / (float)size);
			res.data[cnt].name   = tab[0].data[curs[0]].name;
			res.data[cnt].signal = mean;
			res.data[cnt].moq    = var/mean;
			cnt++;
		}
		return res;
	}

	public float SQR(float x){
		return x*x;
	}
}