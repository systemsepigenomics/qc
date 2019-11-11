package acemapQC;

import acemapCore.*;
import aceStyle.*;

import acemapQCStyle.*;
import acemapQCCore.*;
import maModel.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

import java.io.*;

//import maModel.*;

/**
 *Output interface class
 *@author Guillaume Brysbaert
 *@version 0.58
 */

public class GUIRef extends JPanel implements ActionListener
{
	
	//private	final	int[]			widthsR  = { 20, 150, 50, 50 };
			
	public final	AceLabel		refLab = new AceLabel("Ref. file:");
	public	AceTextField	refTxt  = new AceTextField("");
			AceButton		refBut   = new AceButton("browse");
	
	//cursor
	ThQCCursor thCursor;
	
	public GUIRef()
	{
		super(null);
		setBackground(Color.white);
		
		refBut.addActionListener(this);
		
		refTxt.setEnabled(false);
		
		add(refLab);
		add(refTxt);
		add(refBut);
		
		//thread for cursor
		thCursor = new ThQCCursor(this);
	}
	
	public void clear()
	{
		this.refTxt.setText("");
	}
	
	/**
	 *Set the bounds of each element of the Panel
	 */
	
	public void setBounds(int x, int y, int w, int h)
	{
		super.setBounds(x,y,w,h);
		
		int outputWidth = 50;
		int txtWidth = w*3/5;
		int browseWidth = 38;
		int init = (w-outputWidth-txtWidth-browseWidth-10)/2;
		
		refLab.setBounds(init, h/2-10, outputWidth, 20);
		refTxt.setBounds(init+outputWidth+5, h/2-10, txtWidth, 20);
		refBut.setBounds(init+outputWidth+5+txtWidth+5, h/2-10, browseWidth, 20);
	}
	
	
	
	public void actionPerformed(ActionEvent e)
	{
		Object 			o = e.getSource();
		
		if(o==refBut)
		{
			try
			{
				//get the input file
				QCFileChooser inputRefInChooser = new QCFileChooser(QCUtils.currentFrame, QCFileChooser.OPEN_TXT);
				
				inputRefInChooser.setCurrentDirectory(new File(QCUtils.refPath));
				inputRefInChooser.setSelectedFile(new File(""));
				
				File inputRef = inputRefInChooser.getFile();
				
				if(inputRef == null)
					return;
				
				//save the path
				QCUtils.refPath = inputRef.getParent();
				QCUtils.refFile = inputRef;
				
				openRef(inputRef);
			}
			catch(Exception ex)
			{
				Utils.message("Error when opening reference file.");
			}
			finally
			{
			//	thCursor.joinThread();
			}
			return;
		}
	}
	
/*	public void enableSave(boolean b)
	{
		if(b == true)
			outputBut.setEnabled(true);
		else
			outputBut.setEnabled(false);
	}*/
	
	public void openRef(File refFile)
	{
		//file the text field
		refTxt.setText(refFile.getName());
		
		//get the values : means and standard deviation
		KeyValueMap	kvm = new KeyValueMap();
		kvm.loadFrom(refFile, false);
		
		String meanAll = kvm.valueFor("MEAN");
		String[] means = meanAll.split("\t");
		String sdAll = kvm.valueFor("STANDARD_DEV");
		String[] sds = sdAll.split("\t");
		String wAll = kvm.valueFor("WEIGHT");
		String[] ws = wAll.split("\t");
		
	/*	for(int i = 0; i < means.length; i++)
		{
			System.out.println(i+" M  : "+means[i]);
			System.out.println(i+" SD : "+sds[i]);
		}*/
		
		QCUtils.refMeans = means;
		QCUtils.refSds = sds;
		QCUtils.refWeights = ws;
		
		//activate analyze button if possible
		if(QCUtils.currentInFile != null && QCUtils.refMeans != null && QCUtils.refSds != null && QCUtils.refWeights != null)
		{
			SplitFrame1311 tmpSF1311 = (SplitFrame1311)QCUtils.currentFrame;
			GUICreate guiCreate = (GUICreate)tmpSF1311.getCenterPane();
			guiCreate.analyzeBut.setEnabled(true);
		}
	}
}


