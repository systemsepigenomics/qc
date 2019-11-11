package acemapQCStyle;

import java.awt.*;
import java.awt.event.*;
import java.lang.String;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.lang.Math;
import java.util.*;


/**
 *File filter object used by QCFileChooser.
 *@author Guillaume Brysbaert
 *@version 0.58
 */
 
public class QCFilter extends javax.swing.filechooser.FileFilter
{
	final String[] exts    = {"txt", "ma0", "directory"};
	final String[] descs   = {"txt/ma0 files", "*.txt", "*.ma0", "dir"};
	
	final static int TXT_MA0	   = 0;
	final static int TXT   		   = 1;
	final static int MA0   		   = 2;
	final static int DIR		   = 3;
	
	String ext;
	String des;
	int    ECode;
	
	/**
	 *Create a filter for a QCFileChooser
	 *@param FCode the code that corresponds to the desired filter
	 */
	
	QCFilter(int FCode)
	{
		super();
		ECode  = FCode;
		//ext    = exts[FCode];
		des    = descs[FCode];
	}

	public boolean accept(File f)
	{
		if(f.isDirectory())
			return true;
		
		String s = f.getName();
        int i = s.lastIndexOf('.');
        
        if (i > 0 &&  i < s.length() - 1)
        {
			String e = s.substring(i+1).toLowerCase();
			switch(ECode){
				case TXT_MA0:
					if(e.compareTo(exts[0]) == 0 || e.compareTo(exts[1]) == 0)
						return true;
				break;
				case TXT:
					if(e.compareTo(exts[0]) == 0)
						return true;
				case MA0:
					if(e.compareTo(exts[1]) == 0)
						return true;
				break;
				case DIR:
					if(e.compareTo(exts[2]) == 0)
						return true;
				break;
				default:
			//		if(e.compareTo(ext) == 0)
						return true;
//				break;
			}
		}
		return false;
	}
	
	public String getDescription()
	{
		return des;
 	}
}
