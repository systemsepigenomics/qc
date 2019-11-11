package acemapQC;

import acemapCore.*;
import aceStyle.*;
import maModel.*;
import maModel.*;

import acemapQCCore.*;
//import acemapQCStyle.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import java.io.*;

//import maModel.*;

/**
 *Contains and permits to calculate the log-signal
 *and log-variance range that corresponds to a flex table
 *@author Guillaume Brysbaert
 *@version 0.58
 */

public class Range
{
	public double logSigRange = 0;
	public double logVarRange = 0;
	public FlexTable flex;
	public float purcent;
	
	/*
	 *Initialise the object, fixing the flex table and proportion of signal/variance to keep
	 *@param flexforRange the flex table that contains probes and values
	 *@param purcentforRange the proportion that the user wants to keep
	 */
	
	public Range(FlexTable flexForRange, float purcentForRange)
	{
		if(flexForRange == null)
			return;
		this.flex = flexForRange;
		this.purcent = purcentForRange;
	}
	
	/**
	 *Calculate the log-signal range
	 */
	
	public void calculateSigRange()
	{
		if(this.flex == null)
			return;
		
		//get the signal column number
		short sigColNb = 1;
		for(short c = 0; c < flex.headLine.length; c++)
		{
			if(flex.headLine[c].startsWith("SIG"))
			{
				sigColNb = c;
				break;
			}
		}
		System.out.println("SIG column nb : "+sigColNb+"\tflex rows : "+flex.rows);
		
		//get the purcentage of the total nb of probes to let
		float letPF= (float)((flex.rows)*(100-purcent)/100);
		
		int halfP = (int)Math.round(letPF/2);
		
		flex.sortByColumn(sigColNb, true);
		
		//get the values, considering that row and column indexes start at 0
		String startRange = flex.get(halfP-1, sigColNb, 4, true);
		String endRange = flex.get(flex.rows-1-halfP, sigColNb, 4, true);
		
		System.out.println("(halfP-1) : "+(halfP-1)+"\tSTARTRANGE : "+startRange+"\tENDRANGE : "+endRange);
		
		double start = Math.log(Math.abs(Double.parseDouble(startRange)));
		double end = Math.log(Math.abs(Double.parseDouble(endRange)));
		
		System.out.println("SIG RANGE : "+(end-start));
		
		this.logSigRange = end-start;
	}
	
	/**
	 *Calculate the log-variance range
	 */
	
	public void calculateVarRange()
	{
		if(this.flex == null)
			return;
		
		//get the variance column number
		short varColNb = 2;
		for(short c = 0; c < flex.headLine.length; c++)
		{
			if(flex.headLine[c].startsWith("VAR"))
			{
				varColNb = c;
				break;
			}
		}
		
		System.out.println("VAR column nb : "+varColNb);
		
		//get the purcentage of the total nb of probes to let
		float letPF= (float)((flex.rows)*(100-purcent)/100);
		int halfP = (int)Math.round(letPF/2);
		
		flex.sortByColumn(varColNb, true);
		
		//get the values, considering that row and column indexes start at 0
		String startRange = flex.get(halfP-1, varColNb, 4, true);
		String endRange = flex.get(flex.rows-1-halfP, varColNb, 4, true);
		
		System.out.println("(halfP-1) : "+(halfP-1)+"\tSTARTRANGE : "+startRange+"\tENDRANGE : "+endRange);
		
		double start = Math.log(Math.abs(Double.parseDouble(startRange)));
		double end = Math.log(Math.abs(Double.parseDouble(endRange)));
		
		System.out.println("VAR RANGE : "+(end-start));
		
		this.logVarRange = end-start;
	}
	
	/**
	 *Save the range values.
	 *They will be saved after the model parameters estimation.
	 *@param mTab the mTable that corresponds to the file where it's needed to save
	 */
	
	public void saveTo(MTable mTab)
	{
		if(mTab.cmf == null || mTab.cmf.F_txt == null)
		{
			Utils.message("Impossible to save to the txt file");
			return;
		}
		
		File fin = 	mTab.cmf.F_txt;	
		File fout = QCUtils.getTemp();
				
		String inset = new String("RANGE_LOG"+"\t"+Utils.getDecs(this.logSigRange, 4, false)
														+"\t"+Utils.getDecs(this.logVarRange, 4, false));
		
		QCUtils.insertBehind(fin, fout, "VAR_MODEL2_s", inset);
		
		if(mTab.cmf != null)
		{
			mTab.cmf.F_txt = fout;
			if(mTab.cmf.z_file != null)
			{
				mTab.cmf.save();
				mTab.info.kvm = new KeyValueMap(fout, true);
				System.out.println("mTab.cmf saved");
			}
		}
	}
}

