package maModel;

import acemapQCCore.*;
import maModel.*;


//import acemap.*;
//import acemapMath.*;
import java.io.*;
import java.util.*;
import aceStyle.*;
import acemapCore.*;


/**
 *Class that may generate an arbitrary number of artificial micro array data 
 *files reading model parameters from 'real' data files
 *@author Guillaume Brysbaert, Sebastian Noth
 *@version 0.58
 */
 
public class MadSynthSig{
	
	public MadSynthSig(){
		rng = new Random();
	}
	
	//SIG
	double m_xo, m_scl, m_yo, m_sl;
	double s_xo, s_scl, s_yo, s_sl;
	
	//number of synthetic files created
	public static int synthNb = 0;
	
	private Random rng;
	
	private	double	draw(double m, double s){
		return m + s*rng.nextGaussian();	
	}
	private double	draw(LNDp p){
		return p.x0+Math.exp( draw(Math.log(p.m), p.s)  ); 
	}
	

	private LNDp		sigMod[];
	
	private double		D1_x0, D1_m, D1_s;
		
	private	FNeonex		f_s, f_m;
	private	FSigmoid2p	fBlend;
	
	private	MTable	dataReal, dataSynth;	


	public	void	setRealData(MTable mTab)
	{
		dataReal = mTab;
		
		if(dataReal.info.kvm == null) return;
		
		String buff, el[];
		double arr[];
		sigMod = new LNDp[2];
		
		// SIGNAL MODEL
		buff		= dataReal.info.kvm.valueFor("SIGNAL_DISTRIBUTION");
		System.out.println("PATH : "+dataReal.info.path);
		System.out.println("BUFF : "+buff);
		el			= Utils.getElementArray(buff);
		sigMod[0]	= new LNDp(	Utils.parseFast(el[0]),
								Utils.parseFast(el[1]),
								Utils.parseFast(el[2]),
								Utils.parseFast(el[3]) );
		sigMod[1]	= new LNDp(	Utils.parseFast(el[4]),
								Utils.parseFast(el[5]),
								Utils.parseFast(el[6]),
								Utils.parseFast(el[7]) );
		
		// BLENDING
		buff		= dataReal.info.kvm.valueFor("VAR_BLEND");
		System.out.println(buff);
		el			= Utils.getElementArray(buff);
		arr         = new double[2];
		for(int i=0; i<2; i++)
		//for(int i=0; i<arr.length; i++)
			arr[i]	= Utils.parseFast(el[i]);
			
		fBlend      = new FSigmoid2p(arr);
		
		
		// MODEL1
		buff		= dataReal.info.kvm.valueFor("VAR_MODEL1");
		el			= Utils.getElementArray(buff);
		D1_x0       = Utils.parseFast(el[0]);
		D1_m        = Utils.parseFast(el[1]);
		D1_s        = Utils.parseFast(el[2]);
		
		
		// MODEL2 mean
		buff		= dataReal.info.kvm.valueFor("VAR_MODEL2_m");
		el			= Utils.getElementArray(buff);
		arr         = new double[el.length];
		for(int i=0; i<4; i++)
			arr[i]	= Utils.parseFast(el[i]); 
		
		//SIGf_m         = new FNeonex();
		//SIGf_m.setP(arr);
		//SIGf_m.setup();
		m_yo  = arr[0];
		m_scl = arr[1];
		m_xo  = arr[2];
		m_sl  = arr[3];
		
		
		
		buff		= dataReal.info.kvm.valueFor("VAR_MODEL2_s");
		el			= Utils.getElementArray(buff);
		arr         = new double[el.length];
		for(int i=0; i<4; i++)
			arr[i]	= Utils.parseFast(el[i]); 
		
		s_yo  = arr[0];
		s_scl = arr[1];
		s_xo  = arr[2];
		s_sl  = arr[3];
		
		
		
		//SIGf_s        = new FNeonex();
		//SIGf_s.setP(arr);
		//SIGf_s.setup();
		
	}
	
	
	/**
	 *generates the artificial file
	 */
	public	MTable	create(){
		int size = dataReal.size;
		
		dataSynth      = new MTable(size, 0);
		dataSynth.tech = dataReal.tech;
		double 	logsig, sig, var;
		boolean	mod0;
		
		int 	sMod, vMod;
		double  c1, c2;
	
		
		double	x0, m, s;
		
				
				
		
		for(int i=0; i<size; i++)
		{
		//	do{
				c1     = rng.nextDouble();
				c2     = rng.nextDouble();
				sMod   = c1<sigMod[0].f?0:1;
				sig    = draw(sigMod[sMod]);
				logsig = Math.log(sig);
				vMod   = c2<fBlend.f(logsig)?0:1;
		//	}while(vMod==0); // generate only second part
						
			if(vMod==0){
				var= D1_x0 + Math.exp( draw(D1_m, D1_s)); 
			}
			else{
				//SIGm  = f_m.f(logsig);
				//SIGs  = Math.sqrt( f_s.f(logsig) );
				m =            m_yo + m_scl/(1.0+Math.exp(-(logsig-m_xo)*m_sl));
				s = Math.sqrt( s_yo + s_scl/(1.0+Math.exp(-(logsig-s_xo)*s_sl)) );
				
				
				
				var= Math.exp( draw(m, s) ); 
			}
			
			dataSynth.data[i].signal = (float)sig;
			dataSynth.data[i].moq    = (float)var;
		}
		
		dataReal.sortBySignal();
		dataSynth.sortBySignal();
		
		for(int i=0; i<size; i++)
			dataSynth.data[i].name = new String(dataReal.data[i].name);	
		
		//calculate the A value
		if(QCUtils.currentInFile.getName().endsWith(".ma0"))
		{
			PLNDist plnd = new PLNDist(sigMod);
			for(int j=0; j<dataSynth.size; j++)
				dataSynth.data[j].a = plnd.a_value(dataSynth.data[j].signal, dataSynth.data[j].moq);
			dataSynth.enableA();
		}
		
		return dataSynth;
	}

	/**
	 *saves the generated artificial file as a .ma0. Used in case of a .ma0 in-file.
	 *@param dir the directory where the file will be save
	 *@param outFileName name of the out file. It will be added to this name "_synth_"+nb (of the synthetic file)
	 */
	 
	public	CompositeMfQC	saveAsMA0(File dir, String outFileName)
	{
		if(dataReal.cmf == null)
		{
			System.out.println("no information found in source file");
			return null;
		}
		
		CompositeMfQC cmf = new CompositeMfQC();
		
		cmf.F_ma     = QCUtils.getTemp();
		//cmf.F_txt    = dataReal.cmf.F_txt;
		
		cmf.F_txt = writeAnnotation((CompositeMfQC)dataReal.cmf);
		
		File fsave;
		
		dataSynth.saveAsMA0(cmf.F_ma);
		
		String dat   = Utils.getCurrentTime()[0];
		
		cmf.name_ma  = AceUtil.generateFileName(dat, "synth", dataSynth.tech, true);
		cmf.name_txt = AceUtil.generateFileName(dat, "synth", dataSynth.tech, false);
		
		if(outFileName != null)
			fsave = new File(dir, outFileName+"_synth_"+synthNb+".ma0");
		else if(dataReal.cmf != null && dataReal.cmf.z_file != null)
		{
			String nameO = dataReal.cmf.getFilename();
			fsave = new File(dir, nameO.substring(0, nameO.length()-4)+"_synth_"+synthNb+".ma0");
		}
		else
			fsave = new File(dir, "temp_synth_"+synthNb+".ma0");
		
		cmf.saveTo(fsave);
		synthNb++;
		return cmf;
	}

	/**
	 *Write the annotation file
	 *@cmfCre the CompositeMfQC that is needed to generate the annotation file
	 */
	
	private File writeAnnotation(CompositeMfQC cmfCre)
	{
		File annot = QCUtils.getTemp();
		KeyValueMap	kvm = new KeyValueMap();
		
		kvm.loadFrom(cmfCre.F_txt, false);
	
		String lineEnd = Utils.lineEnd;
		String[] c_date = Utils.getCurrentTime();
			
		FileWriter			fwr = null;
		
		try
		{
			 fwr  = new FileWriter(annot); 
		}
		catch(Exception e)
		{ 
			return null;
		}
				
		BBReader bbro = new BBReader(cmfCre.F_txt);
		
		bbro.getToNextRET();
		
		String c_key, o_key;
		String value;
		
		boolean jump = false;
		boolean divg = false;
		
		String[] tabrow;
		
		int cntln = 0;
		
		try
		{
			fwr.write("-----------------------------------------------"+lineEnd);
			fwr.write("ace.map synthetic output generated "+(new Date()).toString()+lineEnd );
			fwr.write("ace.map QC version: "+QCUtils.versionString+lineEnd);
			fwr.write(QCUtils.httpLink+lineEnd);
			fwr.write("-----------------------------------------------"+lineEnd+lineEnd);
			
			//fwr.close();
				
		}
		catch(Exception e)
		{
			System.out.println("Error in writeAnnotation(...) : "+e.toString());
			return null;
		}
		
		
		boolean fine = true;
		int td;
		
		while(true)
		{
			divg = false;
			
			c_key = bbro.getToNextRET();
								
			if(c_key == null) break;
			
			td = c_key.indexOf(9);
			
			if(td == -1 )
			{
				try
				{
					if(c_key.equals(""))
						fwr.write(c_key+lineEnd);
					else if(c_key.equals("GENERAL"))
						fwr.write(c_key+lineEnd+"-------"+lineEnd);
					else if(c_key.equals("SAMPLE"))
						fwr.write(c_key+lineEnd+"------"+lineEnd);
					else if(c_key.equals("PROCESSING"))
						fwr.write(c_key+lineEnd+"----------"+lineEnd);
					else if(c_key.equals("DEPLOYMENT"))
						fwr.write(c_key+lineEnd+"----------"+lineEnd);
				}
				catch(Exception e)
				{
					fine = false;
				}
			}
			else
			{
				c_key = c_key.substring(0, td);
				value = kvm.valueFor(c_key);
				
				if(value==null)
				{
					Utils.error("problem");
					fine = false;
					break;
				}
				
				fine = true;
							
				//adapt the comment
				if(c_key.equals("COMMENT_GENERAL"))
					value = "Synthetic data";
				
				//omit the parameters of the model
				if(c_key.equals("SIGNAL_DISTRIBUTION") || c_key.equals("VAR_BLEND") || c_key.startsWith("VAR_MODEL"))
				{
					// just omit
					System.out.println("key omitted : "+c_key);
					fine = false;
					break;
				}
				
				if(!fine) break;
							
				try
				{
					fwr.write(c_key+"\t"+value+lineEnd);
					cntln++;
				}catch(Exception e){ System.out.println(e.toString()+" in writeAnnotation()"); fine = false; }
			}
			if(!fine)
				break;
		}
		
		try
		{
			bbro.close();
			fwr.close();
			return annot;
		}
		catch(Exception e)
		{ 
			System.out.println("Error when closing the streams");
			return null;
		}
	}
	
	public MTable getDataReal()
	{
		return dataReal;
	}
}
