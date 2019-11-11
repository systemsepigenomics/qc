package maModel;

//import acemap.*;
import acemapMath.*;
import aceStyle.*;
import acemapCore.*;
import java.io.*;


class Convert{
	Convert(){}
	
	public static void main(String args[]){
		AceUtil.init(null);
				
		File fin  = Utils.getUserFile("source", false);
		File fout = Utils.getUserFile("target", false);
		
		int rows = 0;
		int cols = 0;
		
		BBReader bbr = new BBReader(fin);
		
		cols = Utils.getElementArray(bbr.getToNextRET()).length;
		rows = 1;
		
		String bu;
		while(true){
			bu = bbr.getToNextRET();
			if(bu==null) break;
			rows++;
		} 
		
		double din[][] = new double[rows][cols];
		
		bbr.close();
		bbr = new BBReader(fin);
		
		for(int i=0; i<rows; i++){
			for(int j=0; j<cols-1; j++)
				din[i][j] = Utils.parseFast(bbr.getToNextTAB());	
			din[i][cols-1] = Utils.parseFast(bbr.getToNextRET());
		}
		
		bbr.close();
		
		double dout[][] = new double[rows][cols];
		for(int i=0; i<cols;i++)
			dout[0][i] = din[0][i];
			
		for(int i=1; i<rows; i++)
			for(int j=0; j<cols; j++)
				dout[i][j] = din[i][j] + din[0][j];
				
		try{
			FileWriter fwr = new FileWriter(fout);
			
			for(int i=0; i<rows; i++){
				for(int j=0; j<cols-1; j++)
					fwr.write(Utils.getDecs(dout[i][j], 3, false)+"\t");
				fwr.write(Utils.getDecs(dout[i][cols-1], 3, false)+Utils.lineend);
				
			}
			fwr.close();
		}catch(Exception e){}
		
		
	}
	
	
}