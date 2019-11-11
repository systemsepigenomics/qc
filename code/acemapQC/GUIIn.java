package acemapQC;

import acemapCore.*;
import aceStyle.*;
import maModel.*;

import acemapQCStyle.*;
import acemapQCCore.*;
import maModel.*;

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.io.*;

//import maModel.*;

/**
 *Input interface class
 *@author Guillaume Brysbaert
 *@version 0.58
 */

public class GUIIn extends JPanel implements ActionListener
{
	//private	final	int[]			widthsR  = { 20, 150, 50, 50 };
			
	final	AceLabel		inputLab = new AceLabel("QC-file:");
	public final	AceTextField	inputTxt  = new AceTextField("");
	final	AceButton		inputBut   = new AceButton("browse");
	
	//for creation of a .ma0 file from a .txt file
	AceAssayChooserAB		assaychooser;
	int assay_nr;
	String[]			date_back;
	File				saveFile;
	float				scalingM;
	
	private	LNDp		signalModel[];
	boolean				mTabFresh;
	final	String[]	tech_abbr  = {"p", "f", "g"};
	
	String				savedate;
	
	ThQCCursor thCursor;
	
	public GUIIn()
	{
		super(null);
		setBackground(Color.white);
		
		inputBut.addActionListener(this);
		
		inputTxt.setEnabled(false);
		
		add(inputLab);
		add(inputTxt);
		add(inputBut);
		
		//thread for cursor
		thCursor = new ThQCCursor(this);
	}
	
	/**
	 *Set the bounds of each element of the Panel
	 */
	
	public void setBounds(int x, int y, int w, int h)
	{
		super.setBounds(x,y,w,h);
		
		int inputWidth = 46;
		int txtWidth = w*3/5;
		int browseWidth = 38;
		int init = (w-inputWidth-txtWidth-browseWidth-10)/2;
		
		inputLab.setBounds(init, h/2-10, inputWidth, 20);
		inputTxt.setBounds(init+inputWidth+5, h/2-10, txtWidth, 20);
		inputBut.setBounds(init+inputWidth+5+txtWidth+5, h/2-10, browseWidth, 20);
	}
	
	/**
	 *Open the file and calculate the parameters of the model, if necessary
	 */
	
	
	public void actionPerformed(ActionEvent e)
	{
		Object o = e.getSource();
		String Anames[];
		
		//INPUT//
		
		if(o==inputBut)
		{
			//get the input file
			QCFileChooser inputChooser = new QCFileChooser(QCUtils.currentFrame, QCFileChooser.OPEN_TXT_MA0);
			
			inputChooser.setCurrentDirectory(new File(QCUtils.inputPath));
			inputChooser.setSelectedFile(new File(""));
			
			File inputFile = inputChooser.getFile();
			
			if(inputFile == null)
				return;
			
			//save the path
			QCUtils.inputPath = inputFile.getParent();
			
			//reinitialize the parameters array
			QCUtils.parameters = new double[23];
			
			//empty the temp dir
			QCUtils.cleanTmp();
			
			//panels
			SplitFrame1311 tmpSF1311 = (SplitFrame1311)QCUtils.currentFrame;
			
			GUIMiddle guiMiddle = new GUIMiddle();
			guiMiddle.setProgression(true);
			StringBuffer progBuf = new StringBuffer();
			
			//reinitialize the output panel and bottom panel
			GUIRef guiReference = (GUIRef)tmpSF1311.getRightPane();
			//guiReference.clear();
			GUIBottom guiBottom = (GUIBottom)tmpSF1311.getBottomPane();
			guiBottom.setParameters(null);
			
			tmpSF1311.setMiddlePane(guiMiddle);
			tmpSF1311.adjustPanels();
			
			try
			{
				//cursor
				thCursor.initCursor();
				thCursor.launchThread();
				
				//temp white panel
			/*	AcePanel middleTmpP = new AcePanel();
				middleTmpP.setBackground(Color.white);
				tmpSF1311.setMiddlePane(middleTmpP);
				tmpSF1311.adjustPanels();*/
				
				//save the file in a static variable
				QCUtils.currentInFile = inputFile;
				System.out.println("INPUT FILE : "+inputFile.getName());
				
				//case of a ".ma0" file
				if(inputFile.getName().endsWith(".ma0"))
				{
					CompositeMfQC cmf = new CompositeMfQC();
					
					try
					{
						if(cmf != null && cmf.load(inputFile, true, false, false))
							System.out.println("INPUTFILE WELL LOADED");
						else
						{
							thCursor.joinThread();
							System.out.println("INPUTFILE NOT LOADED");
							return;
						}
						QCUtils.currentInCompMf = cmf;
					}
					catch(Exception ex)
					{
						thCursor.joinThread();
						System.out.println("ERROR WHEN LOADING THE FILE");
						ex.printStackTrace();
						return;
					}
					System.out.println("NAME MA :"+cmf.name_ma);
					//set the file name in the text field
					inputTxt.setText(inputFile.getName());
					inputTxt.getCaret().setDot(0);
					
					//MTable and density panel
					MTable mTab = new MTable(cmf.F_ma, null, false, cmf, true);
					QCUtils.currentMTable = mTab;
					DensPan pan = QCUtils.createDensPan(cmf, mTab);
					
					
					//set to GUIMiddle
					guiMiddle.setDensPanIn(pan);
					tmpSF1311.adjustPanels();
					//guiMiddle.setDensPanOut(pan);
					
					//refresh the frame
					QCUtils.currentFrame.paint(QCUtils.currentFrame.getGraphics());
				}
				//case of a ".txt" file
				else if(inputFile.getName().endsWith(".txt"))
				{
					CompositeMfQC compMf = new CompositeMfQC(new File(QCUtils.tempPath+File.separator+"temp_comp.ma0"));
					String dateT;
					
					if(savedate==null)
						dateT = Utils.getCurrentTime()[0];
					else
						dateT = savedate;
					
					TF_applied tf_applied = new TF_applied(8191);
					
					if(tf_applied.setReadFile(inputFile))
					{
						Anames = tf_applied.getAssayNames();
						
						assaychooser = new AceAssayChooserAB(Anames);
						
						Graphics gr = getGraphics();
						this.paintAll(gr);
						
						assay_nr = assaychooser.getSelected();
						
						if(assay_nr != -1)
						{
							date_back = Utils.getCurrentTime();
							
							try
							{
								saveFile = compMf.newMa(generateFileName(dateT, "user", 0, true ));
								ANsaveto(compMf.newTxt(generateFileName(dateT, "user", 0, false )));
							}
							catch(Exception ex)
							{ 
								saveFile = null;
								System.out.println("saveFile set to null for applied biosystems file"); 
							}
							
							if(saveFile != null)
							{
								System.out.println("SAVEFILE : "+saveFile.getPath());
								if(tf_applied.setAssay(assay_nr))
								{
									if(tf_applied.writeToFile(saveFile, null))
									{
										MTable mTab = new MTable(saveFile, null, false, null, false);
										
										//set the CompositeMf
										mTab.cmf = compMf;
									//	mTab.cmf.save();
										mTab.tech = 0+1; // tec{0,1,2} -> tech{1,2,3}
										scalingM = mTab.normalize();
										
										//create the density panel(and set the current mTAble)
										DensPan pan = QCUtils.createDensPan(compMf, mTab);
										QCUtils.currentMTable = mTab;
										
											//set to GUIMiddle
										guiMiddle.setDensPanIn(pan);
										QCUtils.currentFrame.paint(QCUtils.currentFrame.getGraphics());
										tmpSF1311.adjustPanels();
										
										//set the assay name in the text field
										if(Anames[assay_nr].length() >= 7)
											inputTxt.setText(Utils.cutExt(inputFile.getName())+"_"+Anames[assay_nr].substring(0,7));
										else
											inputTxt.setText(Utils.cutExt(inputFile.getName())+"_"+Anames[assay_nr]);
										inputTxt.getCaret().setDot(0);
										
										//save the composite_mf
										QCUtils.currentInCompMf = compMf;
										
										//save the current MTable
										QCUtils.currentMTable = mTab;
										
										//save the parameters to a file
										//saveParamToFile(QCUtils.parameters);
										
										//tmpSF1311.setMiddlePane(guiMiddle);
										tmpSF1311.adjustPanels();
									}
									else
									{
										System.out.println("Problem : impossible to create the file");
									}
								}
							}
						}
						//cursor
						//thCursor.joinThread();
					} // END if(tf_applied.setReadFile(fi))
					else
						System.out.println("failure in set read file");
				}
				//activate analyze button if possible
				if(QCUtils.currentInFile != null && QCUtils.refMeans != null && QCUtils.refSds != null)
				{
					GUICreate guiCreate = (GUICreate)tmpSF1311.getCenterPane();
					guiCreate.analyzeBut.setEnabled(true);
				}
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				Utils.message("Impossible to open the file "+inputFile.getName());
			}
			finally
			{
				guiMiddle.setProgression(false);
				//refresh the frame
				QCUtils.currentFrame.paint(QCUtils.currentFrame.getGraphics());
				thCursor.joinThread();
			}
			return;
		}
	}
	
	public String generateFileName(String date, String usern, int tech, boolean data)
	{
		return new String( date+usern+"_"+tech_abbr[tech]+(data?".dat":".txt"));
	}
	
	/**
	 *Saves annotate file (not zipped, to temporary file, used either for further zipping or printing)
	 */
	 
	boolean ANsaveto(File f)
	{
		String lineEnd = Utils.lineEnd;
		
		FileWriter fwr = null;
		try{
			fwr = new FileWriter(f);
		}catch(Exception e){ return false; }
		
		if(fwr == null)
			return false;
			
		try{
			fwr.write("-----------------------------------------------"+lineEnd);
			fwr.write("ace.map synthetic output generated "+(new Date()).toString()+lineEnd );
			fwr.write("ace.map QC version: "+QCUtils.versionString+lineEnd);
			fwr.write(QCUtils.httpLink+lineEnd);
			fwr.write("-----------------------------------------------"+lineEnd+lineEnd);
			
		}
		catch(Exception e)
		{ 
			return false;
		};
		
		try
		{
			fwr.close();
		}catch(Exception e){}
		return true;
	}
}
