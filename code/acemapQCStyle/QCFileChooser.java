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
 *A filechooser object that displays only selected file types.
 *@author Guillaume Brysbaert
 *@version 0.58
 */

public class QCFileChooser extends JFileChooser
{
	Component parent;

	public final static	int OPEN_TXT_MA0        	=  0;
	public final static	int SAVE_TXT		        =  1;
	public final static	int SAVE_MA0		        =  2;
	public final static	int OPEN_DIR 		       	=  3;
	public final static	int OPEN_TXT	        	=  4;
	
	boolean save;
	
	public QCFileChooser(java.awt.Component parent, int variant)
	{
		super();

		this.parent = parent;
		save = false;

		switch(variant)
		{
			case 0:
				setFileSelectionMode(JFileChooser.FILES_ONLY);
				setMultiSelectionEnabled(false);
				setDialogTitle("select an input file");
				setFileFilter(new QCFilter(0));
			break;
			case 1:
				setFileSelectionMode(JFileChooser.FILES_ONLY);
				setMultiSelectionEnabled(false);
				setDialogTitle("save txt file as");
				setFileFilter(new QCFilter(1));
				save = true;
			break;
			case 2:
				setFileSelectionMode(JFileChooser.FILES_ONLY);
				setMultiSelectionEnabled(false);
				setDialogTitle("save ma0 file as");
				setFileFilter(new QCFilter(2));
				save = true;
			break;
			case 3:
				setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				setMultiSelectionEnabled(false);
				setDialogTitle("select a directory file");
				setFileFilter(new QCFilter(3));
			break;
			case 4:
				setFileSelectionMode(JFileChooser.FILES_ONLY);
				setMultiSelectionEnabled(false);
				setDialogTitle("select a txt file");
				setFileFilter(new QCFilter(1));
			break;
			default:
				setFileSelectionMode(JFileChooser.FILES_ONLY);
				setMultiSelectionEnabled(false);
				setDialogTitle("choose file");
			break;
		}
	}

	public QCFileChooser()
	{
		super();
	}
	
	/**
	 *Get the selected file, if it's approved
	 *@return the selected file
	 */
	
	public File getFile()
	{
		int retVal=-1;
		try
		{
			if(save)
				retVal = showDialog(parent, "save");
			else
				retVal = showDialog(parent, "open");
		}
		catch(Exception e)
		{
			return null;
		}
		
		if(retVal== JFileChooser.APPROVE_OPTION)
			return getSelectedFile();
		return null;
	}

	/**
	 *Get the selected files, if it's approved
	 *@return the selected files
	 */

	public File[] getFiles()
	{
		int retVal=-1;
		try
		{
			if(save)
				retVal = showDialog(parent, "save");
			else
				retVal = showDialog(parent, "open");
		}
		catch(Exception e)
		{ 
			return null;
		}
		
		if(retVal== JFileChooser.APPROVE_OPTION)
			return getSelectedFiles();
			
		return null;
	}
}
	