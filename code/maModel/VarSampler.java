package maModel; 

//import acemap.*;
import acemapMath.*;
import acemapCore.*;
import aceStyle.*;
import java.io.*;


class VarSampler{
	
	VarSampler(){}
	
	
	private	MTable	mTab;
	private	int		sigS, varS;	
	
	double	bins[][];
	double	means[];
	private double	sigRes =   0.25;		//1.5;
	private	double	sigBin =   0.25;		//1.5;	
	private	double	sigLo  =  -4.0;		//-1.25;
	private	double	sigHi  =   8.0;		//4.0;		
	
	private	double	varRes =    0.1;
	private double	varLo  =   -2.0;
	private double	varHi  =    8.0; 
	
	public boolean	writeHead(FileWriter fwr){
		sigS = (int)(( sigHi-sigLo + sigRes ) / sigRes);
		
		try{
			for(int i=0; i<sigS; i++)
				fwr.write("\t"+Utils.getDecs( (double)i * sigRes + sigLo, 3, true));
			
			fwr.write(Utils.lineend);
		}catch(Exception e){ return false; }
		return true;
	}
	
	
	public	void	setInput(MTable mTab, File fout){
		this.mTab = mTab;
		
		sigS = (int)(( sigHi-sigLo + sigRes ) / sigRes);
		varS = (int)(( varHi-varLo + varRes ) / varRes);
							
		bins  = new double[sigS][varS];
		means = new double[sigS]; 
		
		
		
		/* !!
		MVarEstim.GA_NEX = Utils.request("choose method:", "GANEX", "NeONEX");
		
		// read neonex parameters
		String buf = mTab.info.kvm.valueFor("NEONEX_PARAMETERS");
		if(buf==null){
			System.out.println("no parameters");
		}
		
		String arr[] = Utils.getElementArray(buf);
		double X[] = new double[5];
		for(int i=0; i<5; i++)
			X[i] = Utils.parseFast(arr[i]);
		// END read neonex parameters
		*/
		
		
		
		mTab.sortBySignal();
		double logSig[] = new double[mTab.size];
		double logVar[] = new double[mTab.size];
		
		
		
		
		
		
		for(int i=0; i<mTab.size; i++){
			logSig[i] = Math.log(mTab.data[i].signal);	
			logVar[i] = mTab.data[i].moq>0.0f?Math.log(mTab.data[i].moq   ):-4.0;	
			//!!logVar[i] = (double)mTab.data[i].moq - MVarEstim.neonex(logSig[i], X); //Math.log(mTab.data[i].moq   ); // (double)mTab.data[i].moq;
		}
		
				
		
		double loS, hiS, loV, hiV, bRes;
		
						
		int	sigCurs = 0;
		int varCurs = 0;
				
		int datCurI, datCur = 0;
		int datMax = mTab.size;
		
		int varCbeg;
		int varClen;
		
		for(int s=0; s<sigS; s++){
			
			loS = (double)s * sigRes + sigLo - 0.5*sigRes;
			hiS = (double)s * sigRes + sigLo + 0.5*sigRes;
			
			for(int v=0; v<varS; v++)
				bins[s][v] = 0.0;
			
			
			while(datCur<datMax && logSig[datCur]<loS) datCur++;
			
			varCbeg = datCur;
			varClen = 0;			
			
			while(datCur<datMax && logSig[datCur]<hiS){
				datCur++;
				varClen++;
			}
			
			//inner loop: variance hist
			Utils.qsort2D(logVar, logSig, varCbeg, varCbeg+varClen-1);
				
			means[s] = 0.0;
			
			if(varClen>0){
				bRes = 1.0 / varRes / (double)varClen;	
								
				for(datCurI=0; datCurI<varClen; datCurI++)
					means[s] += logVar[varCbeg + datCurI]; 
				
				means[s] /= (double)varClen;
				
				/*
				datCurI = 0;
				System.out.println("scan "+Utils.getDecs(loS,3,false)+"<logSig<"+Utils.getDecs(hiS,3,false)+", start "+varCbeg+", len "+varClen+" unit="+bRes);
			
				for(int v=0; v<varS; v++){
										
					loV = (double)v * varRes + varLo - 0.5*varRes;
					hiV = (double)v * varRes + varLo + 0.5*varRes;
					
					//System.out.println("------->interval "+Utils.getDecs(loV,3,false)+"<logVar<"+Utils.getDecs(hiV,3,false)+" start index "+(varCbeg + datCurI));	
										
					//if(logVar[varCbeg + datCurI]<loV)
					//	System.out.println("skipping"); // index "+datCurI+" : "+logVar[varCbeg + datCurI]+"<"+loV);					
										
					while(datCurI<varClen && logVar[varCbeg + datCurI]<loV) datCurI++;
					
					//System.out.println("reached index 
										
					
					while(datCurI<varClen && logVar[varCbeg + datCurI]<hiV){
						datCurI++;
						bins[s][v] += bRes;
						
					}
				}
				*/
			}
		}	
			
		
		//File out = Utils.getUserFile("select target file", false);
		
		try{
			FileWriter fwr; // = new FileWriter(out); //new File("D:\\sebastian\\"+mTab.name+"_DhistCorr.txt"));
			
			
			/* !!
			for(int v=0; v<varS; v++)
				fwr.write("\t"+Utils.getDecs((double)v * varRes + varLo, 3, true));
			
			fwr.write(Utils.lineend);
			
			for(int s=0; s<sigS; s++){
				fwr.write(Utils.getDecs((double)s * sigRes + sigLo, 3, true ));
				
				for(int v=0; v<varS; v++)
					fwr.write("\t"+Utils.getDecs(bins[s][v], 3, true ));
						  
				fwr.write(Utils.lineend);
			}
			fwr.close();
			*/
			
			fwr = new FileWriter(fout, true); //!!new File("D:\\sebastian\\"+mTab.name+"_VmeansNew.txt"));
			
			fwr.write(mTab.name);
			for(int i=0; i<sigS; i++){
				//fwr.write(Utils.getDecs((double)i * sigRes + sigLo, 3, true )+"\t"+
				//		  Utils.getDecs( means[i], 3, true)+Utils.lineend);
				fwr.write("\t"+Utils.getDecs( means[i], 3, true));
			}
			fwr.write(Utils.lineend);			  
			fwr.close();
			
			
			
		}catch(Exception e){ System.out.println(e.toString());}
		
	}
	
	public static void main(String args[]){
		AceUtil.init(null);
		
		File path = Utils.getUserFile("select source directory", true);    //new File("D:\\sebastian\\batchVar");
		
		File arr[] = path.listFiles();
		
		//File res = Utils.getUserFile("select target file", true); //new File(path, "results");
		
		//res.mkdir();
			
		File out = Utils.getUserFile("select target file", false);//new File(res, "table.txt");
		
		FileWriter fwr;
		
		MTable mTab;
		CompositeMf cmf;
		VarSampler sam;
		
				
		try{
				sam = new VarSampler();
					fwr = new FileWriter(out);
					sam.writeHead(fwr);
					fwr.close();
				}catch(Exception e1){ System.out.println(e1.toString()); }		
				
				
				
		
		for(int i=0; i<arr.length; i++){
			
			if(arr[i].isDirectory()) continue;
			
			cmf = new CompositeMf();
						
			cmf.load(arr[i], true, false, false);
									
			mTab = new MTable(cmf.F_ma, null, false, cmf, true);
			
			sam = new VarSampler();
			sam.setInput(mTab, out);
			
			/*
			if(i==0){
				try{
					fwr = new FileWriter(out);
					sam.writeHead(fwr);
					fwr.close();
				}catch(Exception e1){ System.out.println(e1.toString()); }
			}	
			*/
			
			
			/*
			try{
				fwr = new FileWriter(out, true);
							
				fwr.write(mTab.name);
				
				for(int k=0; k<sam.means.length; k++)
					fwr.write("\t"+Utils.getDecs(sam.means[k], 3, false));
				
				fwr.write(Utils.lineend);
				fwr.close();
				System.out.println("--------> completed processing of "+arr[i].getName()+" ("+(i+1)+" of "+arr.length+")");
				
			}catch(Exception e){ System.out.println(e.toString()); }
			*/
		}
		
	}
	
} 
