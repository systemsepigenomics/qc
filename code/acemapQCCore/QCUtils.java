package acemapQCCore;

import acemapCore.*;
import acemapQCStyle.*;
import acemapQC.*;

import maModel.*;

import java.lang.String;
import java.io.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import java.io.*;
import javax.imageio.*;
import javax.imageio.ImageIO.*;
import java.awt.*;
import java.awt.image.*;

/**
 *Collection of constants and helper functions. All members are static.
 *@author Guillaume Brysbaert
 *@version 0.58
 */

public class QCUtils
{
	//current in-file
	public 	static	File		currentInFile;
	
	//current ref-directory
	public 	static	File		currentRefDir;
	
	//current CompositeMfQC
	public 	static	CompositeMfQC		currentInCompMf;
	public 	static	CompositeMfQC[]		currentOutCompMf;
	
	//current synthetic created file paths
	public 	static	String[]	currentSynthPaths;
	
	//current MTable
	public 	static	MTable		currentMTable;
	
	//current Frame
	public 	static	Frame		currentFrame;
	
	// temp file directory
	public  static 	File 		tempPath = new File("temp");
	
	//table that contains the parameters
	public  static double[] 	parameters = new double[23];
		
	// number of the last temp file
	private static int tempCnt = 1;

	//input default path
	public static String	inputPath = "input";
	
	//references
	public static String	refPath = "reference_files";
	public static File		refFile;
	public static String[] 	refMeans; //means
	public static String[] 	refSds; //standard deviations
	public static String[] 	refWeights; //weights
	
	//image default path
	public static final String	imagePath = "images";
	
	//version
	public static final String	versionString = "1.0";
	
	//http link
	public static final String httpLink	= "http://www.iri.cnrs.fr/seg";
	
	
	private QCUtils(){}
	
	/**
	 *Create a density panel
	 *@param cmf the composite_mf needed for the densities
	 *@param MTable1 the MTable that corresponds to the composite_mf
	 *@return the density panel
	 */
	
	public static DensPan createDensPan(CompositeMfQC cmf, MTable MTable1)
	{
		MTable mTab = null;
		if(MTable1 != null)
			mTab = MTable1;
		else
			mTab = new MTable(cmf.F_ma, null, false, cmf, true);
		
		
		//save the current MTable
		//currentMTable = mTab;
		
		//maxima
		double minlS = -4.0;
		double maxlS = 4.0;
		double minlV = -4.0;
		double maxlV = 4.0;
	
			//rescaling
		/*	if(Double.parseDouble(sigMa01) > 50)
			{
				maxlS = Math.ceil(Math.log(Double.parseDouble(sigMa01)));
				minlS = -maxlS;
			}
			else if(Double.parseDouble(sigMa01) < 0.019)
			{
				
				minlS = Math.ceil(Math.log(Double.parseDouble(sigMa01)));
				maxlS = -maxlS;
			}
			
			if(Double.parseDouble(varMa01) > 50)
			{
				maxlV = Math.ceil(Math.log(Double.parseDouble(varMa01)));
				minlV = -maxlV;
			}
			else if(Double.parseDouble(varMa01) < 0.019)
			{
				minlV = Math.ceil(Math.log(Double.parseDouble(varMa01)));
				maxlV = -maxlV;
			}
			
			System.out.println("MAXLS : "+maxlS+"\tMINLS : "+minlS+"\tMAXLV : "+maxlV+"\tMINLV : "+minlV);
		}*/
		
		double bins[][] = Utils.filterGauss(Plot.sample(mTab, minlS, maxlS, minlV, maxlV, 160), 9);
		DensPan pan = new DensPan(bins);
		
		pan.inp.setExtremes(minlS, maxlS, minlV, maxlV);
		pan.inp.mTab = mTab;
		pan.setBackground(Color.white);
		
		return pan;
	}
	
	/**
	 *Returns the File object of a new temporary file located in the temp file directory, increasing tempCnt
	 */
	
	public static File getTemp()
	{
		int  num = tempCnt;
		
		File tow = new File(tempPath, "temp"+num+".txt");
		
		while(tow.exists())
		{
			num++;
			tow = new File(tempPath, "temp"+num+".txt");
		}
		tempCnt = num+1;
		
		//DEBUtils.message("default temp file requested");
		
		return tow;
	}
	
	/**
	 *Initialization function that needs to be called once during program startup.
	 *@param mainF parent Frame
	 */
	 
	public static void init(Frame mainF)
	{
		Utils.init(mainF);
		
		if(tempPath == null || !tempPath.exists())
		{
			tempPath.mkdir();
			System.out.println("creating temp path "+tempPath.getAbsolutePath() );
		}
		else
		{
			System.out.println("found valid temp path "+tempPath.getAbsolutePath() );
			//delete files in "temp" directory
			QCUtils.cleanTmp();
		}
	
		if(tempPath == null)
			Utils.message("temp path not properly set");
	}
	
	/**
	 *Deletes all files in the temp file directories (does neither delete subdirectories nor their content)
	 */
	
	public static void cleanTmp()
	{
		if(tempPath == null) return;
		File[] tmpf = tempPath.listFiles();
		if(tmpf==null)return;
		
		for(int k=0; k<tmpf.length; k++){
			try{
				tmpf[k].delete();
			}catch(Exception e){System.out.println("error deleting "+tmpf[k].getName()+" : "+e.toString() );}
		}
	}
	
	/*
	 *Returns the temp file directory
	 *@return the temp path
	 */
/*	 
	public static File getTempDir()
	{
		return tempPath;
	}*/
	
	/**
	 *Saves the parameters to the specified path
	 *@param param the parameters
	 *@param fileName the name of the file which corresponds to the parameters
	 *@param path the path where to save
	 *@param append if true, the data will be appended to the file, if not, it will save to blank file
	 */
	
	public static void saveParamToFile(double[] param, String fileName, String savePath, boolean append)
	{
		StringBuffer strBuf = new StringBuffer();
	/*	strBuf.append("SIGNAL_DISTRIBUTION"+"\t"+Utils.getDecs(param[0], 4, false));
		strBuf.append("\t"+Utils.getDecs(param[1], 4, false));
		strBuf.append("\t"+Utils.getDecs(param[2], 4, false));
		strBuf.append("\t"+Utils.getDecs(param[3], 4, false));
		strBuf.append("\t"+Utils.getDecs(param[4], 4, false));
		strBuf.append("\t"+Utils.getDecs(param[5], 4, false));
		strBuf.append("\t"+Utils.getDecs(param[6], 4, false));
		strBuf.append("\t"+Utils.getDecs(param[7], 4, false)+Utils.lineEnd);
		strBuf.append("VAR_BLEND"+"\t"+Utils.getDecs(param[8], 4, false));
		strBuf.append("\t"+Utils.getDecs(param[9], 4, false)+Utils.lineEnd);
		strBuf.append("VAR_MODEL1"+"\t"+Utils.getDecs(param[10], 4, false));
		strBuf.append("\t"+Utils.getDecs(param[11], 4, false));
		strBuf.append("\t"+Utils.getDecs(param[12], 4, false)+Utils.lineEnd);
		strBuf.append("VAR_MODEL2_m"+"\t"+Utils.getDecs(param[13], 4, false));
		strBuf.append("\t"+Utils.getDecs(param[14], 4, false));
		strBuf.append("\t"+Utils.getDecs(param[15], 4, false));
		strBuf.append("\t"+Utils.getDecs(param[16], 4, false)+Utils.lineEnd);
		strBuf.append("VAR_MODEL2_m"+"\t"+Utils.getDecs(param[17], 4, false));
		strBuf.append("\t"+Utils.getDecs(param[18], 4, false));
		strBuf.append("\t"+Utils.getDecs(param[19], 4, false));
		strBuf.append("\t"+Utils.getDecs(param[20], 4, false));*/
		
		strBuf.append(fileName);
		strBuf.append("\t");
		
		for(int i = 0; i < param.length; i++)
		{
			strBuf.append(Utils.getDecs(param[i], 4, false));
			if(i < param.length-1)
				strBuf.append("\t");
		}
		
		strBuf.append(Utils.lineEnd);
		
		if(append == false)
			Utils.saveToFile(strBuf.toString(), savePath);
		if(append == true)
			Utils.appendToFile(strBuf.toString(), savePath);
	}
	
	/**
	 *Get the length of the longuest string of the table
	 *@param tabStr the table that contains the string
	 *@return the length of the string, -1 if the tabStr is null
	 */
	
	public static int getMaxLength(String[] tabStr)
	{
		if(tabStr == null)
			return -1;
			
		int i = 0;
		int len = 0;
		while(i < tabStr.length)
		{
			if(tabStr[i] == null)
			{
				i++;
				break;
			}
			int tmpLen = Utils.stdWidth(tabStr[i]);
			if(tmpLen > len)
				len = tmpLen;
			i++;
		}
		return len;
	}
	
	/**
	 *Clear all the elements and variables (reinitialisation)
	 */
	
	public static void clearAll()
	{
		cleanTmp();
		
		//panels
		SplitFrame1311 tmpSF1311 = (SplitFrame1311)QCUtils.currentFrame;
		
		//static variables
		currentInFile = null;
		currentRefDir = null;
		currentInCompMf = null;
		currentOutCompMf = null;
		currentSynthPaths = null;
		currentMTable = null;;
		parameters = null;
		refMeans = null;
		refSds = null;
		
		//painted elements
		GUIMiddle guiMiddle = new GUIMiddle();
		GUIBottom guiBottom = new GUIBottom();
		
		tmpSF1311.setMiddlePane(guiMiddle);
		tmpSF1311.setBottomPane(guiBottom);
		
		GUICreate guiCreate = (GUICreate)tmpSF1311.getCenterPane();
		guiCreate.analyzeBut.setEnabled(false);
		
		GUIIn guiIn = (GUIIn)tmpSF1311.getLeftPane();
		guiIn.inputTxt.setText("");
		
		GUIRef guiRef = (GUIRef)tmpSF1311.getRightPane();
		guiRef.refTxt.setText("");
		
		tmpSF1311.adjustPanels();
	}
	
	/**
	 *Remove the mark and its values
	 */

	public static boolean	removeMarkInFile(File f_in, File f_out, String mark)
	{
		BBReader 	bbr;
		FileWriter 	fwr;
		String		buff;
		
		try{
			bbr = new BBReader(  f_in );
			fwr = new FileWriter(f_out);
			
			do{
				buff = bbr.getToNextRET();
				if(buff==null) break;
				if(buff.startsWith(mark)) continue;
				fwr.write(buff+Utils.lineEnd);
			}while(true);
			
			fwr.close();
			bbr.close();
			
		}catch(Exception exc){System.out.println(exc.toString());  return false; }
		return true;
	}
	
	/**
	 *Insert a String in a file, after the mark
	 *@param f_in	the in file
	 *@param f_out the out file
	 *@param mark the mark after which the String will be added
	 *@param inset the String to add
	 *@return true if all is OK, false if not
	 */
	
	public static boolean	insertBehind(File f_in, File f_out, String mark, String inset)
	{
		BBReader 	bbr;
		FileWriter 	fwr;
		String		buff;
		
		try
		{
			System.out.println("Insert started");
			if(f_in != null)
			{
				bbr = new BBReader(f_in);
				fwr = new FileWriter(f_out);
			
				do
				{
					buff = bbr.getToNextRET();
					if(buff==null) break;
					fwr.write(buff+Utils.lineEnd);
				}
				while(!buff.startsWith(mark) );
				
				fwr.write(inset+Utils.lineEnd);
				
				do
				{
					buff = bbr.getToNextRET();
					if(buff==null || buff.length() < 2) break;
					fwr.write(buff+Utils.lineEnd);
				}
				while(true);
				
				fwr.close();
				bbr.close();
			}
			else
			{
				fwr = new FileWriter(f_out);
				fwr.write(inset+Utils.lineEnd);
				fwr.close();
			}
			System.out.println("Insert finished");
		}
		catch(Exception exc)
		{
			exc.printStackTrace();
			System.out.println(exc.toString());  
			return false;
		}
		return true;
	}
}
