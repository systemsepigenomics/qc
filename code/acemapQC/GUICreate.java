package acemapQC;

import acemapCore.*;
import aceStyle.*;
import maModel.*;
import maModel.*;

import acemapQCCore.*;
import acemapQCStyle.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import java.io.*;

//import maModel.*;

/**
 *Input interface class
 *@author Guillaume Brysbaert
 *@version 0.58
 */

public class GUICreate extends JPanel implements ActionListener
{
	//private	final	int[]			widthsR  = { 20, 150, 50, 50 };
	
	//create ref button
	final	AceLabel		createLab1 = new AceLabel("|");
	final	AceButton		createBut  = new AceButton("create ref");
	final	AceLabel		createLab2 = new AceLabel("|");
	
	//analyze button
	final	AceLabel		analyzeLab1 = new AceLabel("|");
	public final	AceButton		analyzeBut  = new AceButton("analyze");
	final	AceLabel		analyzeLab2 = new AceLabel("|");
	
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
	
	
	//get the values of the parameters to calculate at the end the means and the variances
	public 	ArrayList[] 	values;

	//cursor
	ThQCCursor thCursor;
	
	public GUICreate()
	{
		super(null);
		setBackground(Color.white);
		
	//	BorderLayout bl = new BorderLayout();
	//	setLayout(bl);
		
		createBut.addActionListener(this);
		analyzeBut.addActionListener(this);
		
	/*	add(createLab1, BorderLayout.NORTH);
		add(createBut, BorderLayout.NORTH);
		add(createLab2, BorderLayout.NORTH);*/
		
		add(createLab1);
		add(createBut);
		add(createLab2);
		
		add(analyzeLab1);
		add(analyzeBut);
		add(analyzeLab2);
		
		//thread for cursor
		thCursor = new ThQCCursor(this);
	}
	
	/**
	 *Set the bounds of each element of the Panel
	 */
	
	public void setBounds(int x, int y, int w, int h)
	{
		super.setBounds(x,y,w,h);
		
		int createWidth = 54;
		int init1 = (w-createWidth-2*7)/2;
		
		createLab1.setBounds(init1, 25, 5, 20);
		createBut.setBounds(init1+7, 25, createWidth, 20);
		createLab2.setBounds(init1+createWidth+7+2, 25, 5, 20);
		
		int analyzeWidth = 44;
		int init2 = (w-analyzeWidth-2*7)/2;
		
		analyzeLab1.setBounds(init2, h-35, 5, 20);
		analyzeBut.setBounds(init2+7, h-35, analyzeWidth, 20);
		analyzeLab2.setBounds(init2+analyzeWidth+7+2, h-35, 5, 20);
	}
	
	
	
	public void actionPerformed(ActionEvent e)
	{
		Object 			o = e.getSource();
	/*	JFileChooser 	chooser;
		ace_style 		tRow[];
		modelData		mDat = null;
		AceButton		but;*/
		
		if(o == createBut)
		{
			//get the input file
			QCFileChooser createRefInChooser = new QCFileChooser(QCUtils.currentFrame, QCFileChooser.OPEN_DIR);
			
			createRefInChooser.setCurrentDirectory(new File(QCUtils.inputPath));
			createRefInChooser.setSelectedFile(new File(""));
			
			File inputRefDir = createRefInChooser.getFile();
			
			if(inputRefDir == null)
				return;
			
			//save the path
			QCUtils.inputPath = inputRefDir.getPath();
			
			//get the input file
			QCFileChooser createRefOutChooser = new QCFileChooser(QCUtils.currentFrame, QCFileChooser.SAVE_TXT);
			
			createRefOutChooser.setCurrentDirectory(new File(QCUtils.refPath));
			createRefOutChooser.setSelectedFile(new File(""));
			
			File saveFile = createRefOutChooser.getFile();
			
			if(saveFile == null)
				return;
			
			//save the path
			QCUtils.refPath = saveFile.getPath();
			
			//start by clearing all
			QCUtils.clearAll();
			
			//cut the extension of the file name if necessary
			if(!saveFile.getName().endsWith(".txt"))
				saveFile = new File(saveFile.getPath()+".txt");
			
			//reinitialize the parameters array
			QCUtils.parameters = new double[23];
			
			//empty the temp dir
			QCUtils.cleanTmp();
			
			//panels
			SplitFrame1311 tmpSF1311 = (SplitFrame1311)QCUtils.currentFrame;
			GUIMiddle guiMiddle;
			
			if((GUIRef)tmpSF1311.getRightPane() != null)
				guiMiddle = (GUIMiddle)tmpSF1311.getMiddlePane();
			else
				guiMiddle = new GUIMiddle();
			
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
				
				//initiation of values array (stores to retreave just after)
				values = new ArrayList[23];
				for(int i = 0; i < values.length; i++)
					values[i] = new ArrayList();
				
				//progression state to print on screen, and refresh the frame
				progBuf.append("Creating the reference file..."+Utils.lineEnd);
				guiMiddle.setProgStringBuf(progBuf, true);
				QCUtils.currentFrame.paint(QCUtils.currentFrame.getGraphics());
					
				System.out.println("DIRECTORY CHOOSEN : "+inputRefDir.getName());
						
				
				//save the directory in a static variable
				QCUtils.currentRefDir = inputRefDir;
				
				//create the reference file
				StringBuffer title = new StringBuffer();
				title.append("-----------------------------------------------");
				title.append(Utils.lineEnd);
				title.append("ace.map QC reference file generated "+(new Date()).toString());
				title.append(Utils.lineEnd);
				title.append("ace.map QC version: "+QCUtils.versionString);
				title.append(Utils.lineEnd);
				title.append(QCUtils.httpLink);
				title.append(Utils.lineEnd);
				title.append("-----------------------------------------------");
				title.append(Utils.lineEnd);
				title.append(Utils.lineEnd);
				
				title.append("File"); title.append("\t");
				title.append("s-range"); title.append("\t");
				title.append("s1-weight"); title.append("\t");
				title.append("s1-mean"); title.append("\t");
				title.append("s1-var"); title.append("\t");
				title.append("s1-x0"); title.append("\t");
				title.append("s2-weight"); title.append("\t");
				title.append("s2-mean"); title.append("\t");
				title.append("s2-var"); title.append("\t");
				title.append("s2-x0"); title.append("\t");
				title.append("v-range"); title.append("\t");
				title.append("vb-x_offset"); title.append("\t");
				title.append("vb-slope"); title.append("\t");
				title.append("v1-x0"); title.append("\t");
				title.append("v1-m"); title.append("\t");
				title.append("v1-v"); title.append("\t");
				title.append("v2m-y_offset"); title.append("\t");
				title.append("v2m-scale"); title.append("\t");
				title.append("v2m-x_offset"); title.append("\t");
				title.append("v2m-slope"); title.append("\t");
				title.append("v2v-y_offset"); title.append("\t");
				title.append("v2v-scale"); title.append("\t");
				title.append("v2v-x_offset"); title.append("\t");
				title.append("v2v-slope"); title.append("\t");
				title.append(Utils.lineEnd);
				
				Utils.saveToFile(title.toString(), saveFile.getPath());
				
				File[] inFiles = inputRefDir.listFiles();
				//File inputFile = new File(inputRefDir.getPath()+File.separator+"312a_V7.ma0");
				
				//name of the file for which the model parameters are wrong
				Vector wrongFiles = new Vector();
				
				//treat each file
				for(int f = 0; f < inFiles.length;f++)
				{
					//case of a ".ma0" file
					if(inFiles[f].getName().endsWith(".ma0"))
					{
						CompositeMfQC cmf = new CompositeMfQC();
						
						try
						{
							if(cmf != null && cmf.load(inFiles[f], true, false, false))
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
						
						//MTable and density panel
						MTable mTab = new MTable(cmf.F_ma, null, false, cmf, true);
						QCUtils.currentMTable = mTab;
						
						//range
						Range ran = null;
						if(mTab.info.kvm != null && mTab.info.kvm.valueFor("RANGE_LOG") == null)
						{
							//range
							FlexTable flexTmp = new FlexTable();
							flexTmp.initFromTB(mTab, mTab.heads);
							ran = new Range(flexTmp, 99);
							ran.calculateSigRange();
							ran.calculateVarRange();
							ran.saveTo(mTab);
						}
						
						//if the parameters are already calculated, we get it
						if(mTab.info.kvm != null && mTab.info.kvm.valueFor("SIGNAL_DISTRIBUTION") != null
												 && mTab.info.kvm.valueFor("VAR_BLEND") != null
												 && mTab.info.kvm.valueFor("VAR_MODEL1") != null
												 && mTab.info.kvm.valueFor("VAR_MODEL2_m") != null
												 && mTab.info.kvm.valueFor("VAR_MODEL2_s") != null)
						{
							String buff, el[];
							double arr[];
							
							//signal range
							if(mTab.info.kvm.valueFor("RANGE_LOG") != null)
							{
								buff = mTab.info.kvm.valueFor("RANGE_LOG");
								el = Utils.getElementArray(buff);
								QCUtils.parameters[0] = Double.parseDouble(el[0]);
							}
							else
								QCUtils.parameters[0] = ran.logSigRange;
							
							//signal distribution
							buff = mTab.info.kvm.valueFor("SIGNAL_DISTRIBUTION");
							el = Utils.getElementArray(buff);
							
							QCUtils.parameters[1] = Double.parseDouble(el[0]);
							
							//check if the first model parameter (weight of the first distribution) is 0.0 (wrong)
							if(QCUtils.parameters[1] == 0.0)
							{
								wrongFiles.add(inFiles[f]);
								continue;
								//Utils.message("Error, the parameters of the model are not correct.\nImpossible to treat.");	
							}
							
							System.out.println("INSIDE");
							
							QCUtils.parameters[2] = Double.parseDouble(el[1]);
							QCUtils.parameters[3] = Double.parseDouble(el[2]);
							QCUtils.parameters[4] = Double.parseDouble(el[3]);
							
							QCUtils.parameters[5] = Double.parseDouble(el[4]);
							QCUtils.parameters[6] = Double.parseDouble(el[5]);
							QCUtils.parameters[7] = Double.parseDouble(el[6]);
							QCUtils.parameters[8] = Double.parseDouble(el[7]);
							
							//variance range
							if(mTab.info.kvm.valueFor("RANGE_LOG") != null)
							{
								buff = mTab.info.kvm.valueFor("RANGE_LOG");
								el = Utils.getElementArray(buff);
								QCUtils.parameters[9] = Double.parseDouble(el[1]);
							}
							else
								QCUtils.parameters[9] = ran.logVarRange;
							
							//variance distribution
							buff = mTab.info.kvm.valueFor("VAR_BLEND");
							el = Utils.getElementArray(buff);
							QCUtils.parameters[10] = Double.parseDouble(el[0]);
							QCUtils.parameters[11] = Double.parseDouble(el[1]);
							
							buff = mTab.info.kvm.valueFor("VAR_MODEL1");
							el = Utils.getElementArray(buff);
							QCUtils.parameters[12] = Double.parseDouble(el[0]);
							QCUtils.parameters[13] = Double.parseDouble(el[1]);
							QCUtils.parameters[14] = Double.parseDouble(el[2]);
							
							buff = mTab.info.kvm.valueFor("VAR_MODEL2_m");
							el = Utils.getElementArray(buff);
							QCUtils.parameters[15] = Double.parseDouble(el[0]);
							QCUtils.parameters[16] = Double.parseDouble(el[1]);
							QCUtils.parameters[17] = Double.parseDouble(el[2]);
							QCUtils.parameters[18] = Double.parseDouble(el[3]);
							
							buff = mTab.info.kvm.valueFor("VAR_MODEL2_s");
							el = Utils.getElementArray(buff);
							QCUtils.parameters[19] = Double.parseDouble(el[0]);
							QCUtils.parameters[20] = Double.parseDouble(el[1]);
							QCUtils.parameters[21] = Double.parseDouble(el[2]);
							QCUtils.parameters[22] = Double.parseDouble(el[3]);
							
							//save the values
							for(int i = 0; i < values.length; i++)
								values[i].add(Utils.getDecs(QCUtils.parameters[i],4, false));//new Double(QCUtils.parameters[i]));
							
							QCUtils.saveParamToFile(QCUtils.parameters, inFiles[f].getName(), saveFile.getPath(), true);
						}
						//if not, all the parameters are re-calculated
						else
						{
							//LNDp signalModel[];
							maModel.LNDMix lndm = new maModel.LNDMix(QCUtils.currentMTable);
							
							//progression state to print on screen, and refresh the frame
							progBuf.append("Estimating signal distribution parameters - file "+inFiles[f].getName()+Utils.lineEnd);
							guiMiddle.setProgStringBuf(progBuf, true);
						QCUtils.currentFrame.paint(QCUtils.currentFrame.getGraphics());
							
							lndm.optimize(null);
							signalModel = lndm.getParams();
							
							//State and Refresh
							progBuf.append("Estimating variance distribution parameters - file "+inFiles[f].getName()+Utils.lineEnd);
							guiMiddle.setProgStringBuf(progBuf, true);
						QCUtils.currentFrame.paint(QCUtils.currentFrame.getGraphics());
								
							VarEstimSig VES = new VarEstimSig(QCUtils.currentMTable);
							VES.run(0.0001, 80 , null);
							VES.updateAN(mTab.cmf.F_txt, QCUtils.getTemp());
							
							//get the PARAMETERS
							double varBle[] = VES.getParBlend();
							LNDp var1D  = VES.getParD1();
							double var2M[]  = VES.getParD2_Mean();
							double var2V[]  = VES.getParD2_Var();
							
							QCUtils.parameters[0] = ran.logSigRange;
																	
							//signal distribution
							QCUtils.parameters[1] = signalModel[0].getF();
							
							//check if the first model parameter (weight of the first distribution) is 0.0 (wrong)
							if(QCUtils.parameters[1] == 0.0)
							{
								wrongFiles.add(inFiles[f]);
								continue;
							}
							
							QCUtils.parameters[2] = signalModel[0].getM();
							QCUtils.parameters[3] = signalModel[0].getS();
							QCUtils.parameters[4] = signalModel[0].getX0();
							
							QCUtils.parameters[5] = signalModel[1].getF();
							QCUtils.parameters[6] = signalModel[1].getM();
							QCUtils.parameters[7] = signalModel[1].getS();
							QCUtils.parameters[8] = signalModel[1].getX0();
							
							//variance range
							QCUtils.parameters[9] = ran.logVarRange;
							
							//variance distribution
							QCUtils.parameters[10] = varBle[0];
							QCUtils.parameters[11] = varBle[1];
							
							QCUtils.parameters[12] = var1D.getX0();
							QCUtils.parameters[13] = var1D.getM();
							QCUtils.parameters[14] = var1D.getS();
							
							QCUtils.parameters[15] = var2M[0];
							QCUtils.parameters[16] = var2M[1];
							QCUtils.parameters[17] = var2M[2];
							QCUtils.parameters[18] = var2M[3];
							
							QCUtils.parameters[19] = var2V[0];
							QCUtils.parameters[20] = var2V[1];
							QCUtils.parameters[21] = var2V[2];
							QCUtils.parameters[22] = var2V[3];
							
							//calculate the parameters for the dataset
							//double[] QCUtils.parameters = calculateParameters(mTab, progBuf, ran);
							
							//save the values
							for(int i = 0; i < values.length; i++)
								values[i].add(Utils.getDecs(QCUtils.parameters[i],4, false));//new Double(QCUtils.parameters[i]));
							
							QCUtils.saveParamToFile(QCUtils.parameters, inFiles[f].getName(), saveFile.getPath(), true);
						}
						
						//progression state to print on screen, and refresh the frame
						progBuf.append("Calculating/retrieving parameters from file "+inFiles[f].getName()+Utils.lineEnd);
						guiMiddle.setProgStringBuf(progBuf, true);
						QCUtils.currentFrame.paint(QCUtils.currentFrame.getGraphics());
						
						//save the current MTable
						QCUtils.currentMTable = mTab;
						
						tmpSF1311.adjustPanels();
					}
					//case of a .txt file
					else if(inFiles[f].getName().endsWith(".txt"))
					{
						String Anames[];
						File saveFileComp;
						
					//	CompositeMfQC compMf = new CompositeMfQC(new File(QCUtils.tempPath+File.separator+"temp_comp.ma0"));
						String dateT;
						
						if(savedate==null)
							dateT = Utils.getCurrentTime()[0];
						else
							dateT = savedate;
						
						//get the names of the datasets
						TF_applied tf_applied = new TF_applied(8191);
						if(!tf_applied.setReadFile(inFiles[f]))
						{
							Utils.message("Impossible to read the file: "+inFiles[f]);
							continue;
						}
						Anames = tf_applied.getAssayNames();
						
						//calculate the model for each dataset
						for(int aNb = 0; aNb < Anames.length; aNb++)
						{
							if(aNb > 0)							
								tf_applied = new TF_applied(8191);
							
							//for the first one, the read file is already set
							if(aNb == 0 || (aNb > 0 && tf_applied.setReadFile(inFiles[f])))
							{
							//	Anames = tf_applied.getAssayNames();
							//	tf_applied.letOpened = true;
								
								//assaychooser = new AceAssayChooserAB(Anames);
								
								//Graphics gr = getGraphics();
								//this.paintAll(gr);
								
							//	for(int aNb = 0; aNb < Anames.length; aNb++)
							//	{
									//after the first one, we "reinitialize the reading"
								/*	if(aNb > 0)
									{
										tf_applied.reinitialize(inFiles[f]);
									//	tf_applied.setReadFile(inFiles[f]);
									//	Anames = tf_applied.getAssayNames();
									}*/
									
									//close after the last one
								//	if(aNb == Anames.length-1)
								//		tf_applied.letOpened = false;
									
									//progression state to print on screen, and refresh the frame
									progBuf.append("Calculating/retrieving parameters from file "+inFiles[f].getName()+" - "+Anames[aNb]+Utils.lineEnd);
									guiMiddle.setProgStringBuf(progBuf, true);
									QCUtils.currentFrame.paint(QCUtils.currentFrame.getGraphics());
									
									assay_nr = aNb;//assaychooser.getSelected();
									if(assay_nr != -1)
									{
									//	date_back = Utils.getCurrentTime();
										File tmpFile = new File(QCUtils.tempPath+File.separator+"temp_comp_"+aNb+".ma0");
										//tmpFile.delete();
										CompositeMfQC compMf = new CompositeMfQC(tmpFile);
										
										try
										{
											saveFileComp = compMf.newMa(generateFileName(dateT, "user", 0, true ));
											ANsaveto(compMf.newTxt(generateFileName(dateT, "user", 0, false )));
										}
										catch(Exception ex)
										{ 
											saveFileComp = null;
											System.out.println("saveFileComp set to null for applied biosystems file"); 
										}
										
										if(saveFileComp != null)
										{
											System.out.println("SAVEFILE : "+saveFileComp.getPath());
											if(tf_applied.setAssay(assay_nr))
											{
												if(tf_applied.writeToFile(saveFileComp, null))
												{
													MTable mTab = new MTable(saveFileComp, null, false, null, false);
													
													//set the CompositeMf
													mTab.cmf = compMf;
												//	mTab.cmf.save();
													mTab.tech = 0+1; // tec{0,1,2} -> tech{1,2,3}
													scalingM = mTab.normalize();
													
													//create the density panel(and set the current mTAble)
												//	DensPan pan = QCUtils.createDensPan(compMf, mTab);
													//QCUtils.currentMTable = mTab;
													
														//set to GUIMiddle
												//	guiMiddle.setDensPanIn(pan);
												//	QCUtils.currentFrame.paint(QCUtils.currentFrame.getGraphics());
												//	tmpSF1311.adjustPanels();
													
													//save the composite_mf
													QCUtils.currentInCompMf = compMf;
													
													//save the current MTable
													QCUtils.currentMTable = mTab;
													
													//save the parameters to a file
													//saveParamToFile(QCUtils.parameters);
													
													//tmpSF1311.setMiddlePane(guiMiddle);
													//tmpSF1311.adjustPanels();
													
													//calculate the parameters for the dataset
													double[] param = calculateParameters(mTab, progBuf, null);
													
													//save the values
													for(int i = 0; i < values.length; i++)
														values[i].add(Utils.getDecs(param[i],4, false));//new Double(QCUtils.parameters[i]));
							
													
													//save the parameters
													QCUtils.saveParamToFile(param, inFiles[f].getName()+" - "+Anames[aNb], saveFile.getPath(), true);
												}
												else
												{
													System.out.println("Problem : impossible to create the file");
												}
											}
										}
									}
							//	}
								//cursor
								//thCursor.joinThread();
							} // END if(tf_applied.setReadFile(fi))
							else
								System.out.println("failure in set read file");
						}
					}
					
					//empty the temp dir
					QCUtils.cleanTmp();
				}
				
				//beep to signalize the end
				Toolkit.getDefaultToolkit().beep();
				
				//if some files are wrong (wrong model parameters), give a message
				if(wrongFiles.size() > 0)
				{
					StringBuffer message = new StringBuffer();
					message.append("Failure in estimation of parameters for files: "+Utils.lineEnd);
					for(int v = 0; v < wrongFiles.size(); v++)
					{
						if(v < wrongFiles.size()-1)
							message.append(((File)wrongFiles.elementAt(v)).getName()+";");
						else
							message.append(((File)wrongFiles.elementAt(v)).getName());
					}
					message.append("\nEliminated from the calculation of the\nreference file.");
					Utils.message(message.toString());
				}
				
				//case of a ".txt" file
			/*	else if(inputFile.getName().endsWith(".txt"))
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
										
									// SIGNAL MODEL
										maModel.LNDMix lndm  = new maModel.LNDMix(mTab);
										
										progBuf.append("Estimating signal distribution parameters..."+Utils.lineEnd);
										guiMiddle.setProgStringBuf(progBuf);
											//refresh the frame
										QCUtils.currentFrame.paint(QCUtils.currentFrame.getGraphics());
										
										lndm.optimize(null);
										
										signalModel  = lndm.getParams();
										PLNDist plnd = new PLNDist(signalModel);
										for(int i=0; i<mTab.size; i++)
											mTab.data[i].a = plnd.a_value(mTab.data[i].signal, mTab.data[i].moq);
										mTab.enableA();
										
										System.out.println("SAVE BEFORE 1");
										mTab.saveAsMA0(saveFile);
										System.out.println("final saveFile = "+saveFile.getName());
										mTabFresh = true;
										
									
										
										//writing information into panel components
									/*	tab_gen.pan_top.progress.setString("");
										tab_gen.pan_top.MAD_file.setText(generateFileName(
														dateT,
														tab_gen.pan_top.Intern_User.getContent(),
									      				tab_gen.pan_top.technology.getSelectedIndex(), true )   );
									    tab_gen.pan_top.file_median.setContent(Utils.getDecs(scalingM,8,false));
										tab_gen.pan_top.raw_file_x.setContent(Anames[assay_nr]);
										tab_gen.pan_top.raw_file.setContent(Utils.getLastFolders(fi.getAbsolutePath(),2));
										*/
										
										
									//	acemap_config.setL0_rawDir(txt_opener.getCurrentDirectory() );
										
			/*							System.out.println("save successful");
										
										//set the assay name in the text field
										if(Anames[assay_nr].length() >= 7)
											inputTxt.setText(Utils.cutExt(inputFile.getName())+"_"+Anames[assay_nr].substring(0,7));
										else
											inputTxt.setText(Utils.cutExt(inputFile.getName())+"_"+Anames[assay_nr]);
										inputTxt.getCaret().setDot(0);
										
										//save the composite_mf
										QCUtils.currentInCompMf = compMf;
										
									//VARIANCE
										progBuf.append("Estimating variance distribution parameters..."+Utils.lineEnd);
										guiMiddle.setProgStringBuf(progBuf);
										//refresh the frame
										QCUtils.currentFrame.paint(QCUtils.currentFrame.getGraphics());
										
										
										VarEstimSig VES = new VarEstimSig(mTab);
										VES.run(0.0001, 80 , null);
										VES.updateAN(mTab.cmf.F_txt, QCUtils.getTemp());
										
										//get the PARAMETERS
										double varBle[] = VES.getParBlend();
										LNDp var1D  = VES.getParD1();
										double var2M[]  = VES.getParD2_Mean();
										double var2V[]  = VES.getParD2_Var();
																				
										//signal distribution
										QCUtils.parameters[0] = signalModel[0].getF();
										QCUtils.parameters[1] = signalModel[0].getM();
										QCUtils.parameters[2] = signalModel[0].getS();
										QCUtils.parameters[3] = signalModel[0].getX0();
										
										QCUtils.parameters[4] = signalModel[1].getF();
										QCUtils.parameters[5] = signalModel[1].getM();
										QCUtils.parameters[6] = signalModel[1].getS();
										QCUtils.parameters[7] = signalModel[1].getX0();
										
										//variance distribution
										QCUtils.parameters[8] = varBle[0];
										QCUtils.parameters[9] = varBle[1];
										
										QCUtils.parameters[10] = var1D.getX0();
										QCUtils.parameters[11] = var1D.getM();
										QCUtils.parameters[12] = var1D.getS();
										
										QCUtils.parameters[13] = var2M[0];
										QCUtils.parameters[14] = var2M[1];
										QCUtils.parameters[15] = var2M[2];
										QCUtils.parameters[16] = var2M[3];
										
										
										QCUtils.parameters[17] = var2V[0];
										QCUtils.parameters[18] = var2V[1];
										QCUtils.parameters[19] = var2V[2];
										QCUtils.parameters[20] = var2V[3];
										
										//save the current MTable
										QCUtils.currentMTable = mTab;
										
										//set the parameters to the bottom panel
										guiBottom.setParameters(QCUtils.parameters);
										
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
			
			*/
				//progression state to print on screen, and refresh the frame
				progBuf.append("Reference file created"+Utils.lineEnd);
				guiMiddle.setProgStringBuf(progBuf, true);
				QCUtils.currentFrame.paint(QCUtils.currentFrame.getGraphics());
				
				//means and variance (+weight, for the similarity index)+ save values to the reference file
				double means[] = new double[23];
				double variances[] = new double[23];
				double stdDevs[] = new double[23];
				double weights[] = new double[23];
				
				double sumInverses = 0;
				
				for(int i = 0; i < values.length; i++)
				{
					double tempM = 0;
					double tempF = 0;
					
					//means
					for(int j = 0; j < values[i].size(); j++)
						//tempM = tempM+((Double)values[i].elementAt(j)).doubleValue();
						tempM = tempM+Double.parseDouble(values[i].get(j).toString());
						//temp2 = temp2+Math.pow(((Double)values[i].elementAt(j)).doubleValue(), 2);
					
					means[i] = tempM/(double)(values[i].size());
					
					//variances and standard deviations
					for(int j = 0; j < values[i].size(); j++)
					//	tempF = tempF+Math.pow(((Double)values[i].elementAt(j)).doubleValue()-means[i], 2);
						tempF = tempF+Math.pow(Double.parseDouble(values[i].get(j).toString())-means[i], 2);
					
					//variance[i] = Math.sqrt((temp2*1/(values[i].size()))-Math.pow(means[i],2));
					variances[i] = tempF/(values[i].size());
					stdDevs[i] = Math.sqrt(variances[i]);
					
					if(Math.round(stdDevs[i]*10000) != 0)
						sumInverses = sumInverses+(1/stdDevs[i]);
						
					System.out.println("STDDEVS : "+stdDevs[i]);
				}
				
				System.out.println("SUMINVERSES : "+sumInverses);
				
				//calculate the weights, using the inverse of the variances and not considering variances = 0
				for(int j = 0; j < weights.length; j++)
				{
					if(Math.round(stdDevs[j]*10000) == 0)
						weights[j] = 0;
					else
						weights[j] = (1/stdDevs[j])/sumInverses;
				}
				
				Utils.appendToFile(Utils.lineEnd+Utils.lineEnd, saveFile.getPath());
				QCUtils.saveParamToFile(means, "MEAN", saveFile.getPath(), true);
				QCUtils.saveParamToFile(variances, "VARIANCE", saveFile.getPath(), true);
				QCUtils.saveParamToFile(stdDevs, "STANDARD_DEV", saveFile.getPath(), true);
				QCUtils.saveParamToFile(weights, "WEIGHT", saveFile.getPath(), true);
				
				guiReference.openRef(saveFile);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				Utils.message("Impossible to create a reference file");
			}
			finally
			{
			//	guiMiddle.setProgression(false);
				//refresh the frame
				QCUtils.currentFrame.paint(QCUtils.currentFrame.getGraphics());
				thCursor.joinThread();
			}
			return;
		}
		else if(o == analyzeBut)
		{
			//calculate the parameters
			MTable mTab = QCUtils.currentMTable;
			
			//panels
			SplitFrame1311 tmpSF1311 = (SplitFrame1311)QCUtils.currentFrame;
			
			GUIMiddle guiMiddle = (GUIMiddle)(tmpSF1311.getMiddlePane());
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
			
				if(QCUtils.currentInFile.getName().endsWith(".ma0"))
				{
					Range ran = null;
					if(mTab.info.kvm != null && mTab.info.kvm.valueFor("RANGE_LOG") == null)
					{
						//range
						FlexTable flexTmp = new FlexTable();
						flexTmp.initFromTB(mTab, mTab.heads);
						ran = new Range(flexTmp, 99);
						ran.calculateSigRange();
						ran.calculateVarRange();
						ran.saveTo(mTab);
					}
					//if the parameters are already calculated, we get it
					if(mTab.info.kvm != null && mTab.info.kvm.valueFor("SIGNAL_DISTRIBUTION") != null
											 && mTab.info.kvm.valueFor("VAR_BLEND") != null
											 && mTab.info.kvm.valueFor("VAR_MODEL1") != null
											 && mTab.info.kvm.valueFor("VAR_MODEL2_m") != null
											 && mTab.info.kvm.valueFor("VAR_MODEL2_s") != null)
					{
						String buff, el[];
						double arr[];
						
						//signal range
						if(mTab.info.kvm.valueFor("RANGE_LOG") != null)
						{
							buff = mTab.info.kvm.valueFor("RANGE_LOG");
							el = Utils.getElementArray(buff);
							QCUtils.parameters[0] = Double.parseDouble(el[0]);
						}
						else
							QCUtils.parameters[0] = ran.logSigRange;
							
						//signal distribution
						buff = mTab.info.kvm.valueFor("SIGNAL_DISTRIBUTION");
						el = Utils.getElementArray(buff);
						
						QCUtils.parameters[1] = Double.parseDouble(el[0]);
						QCUtils.parameters[2] = Double.parseDouble(el[1]);
						QCUtils.parameters[3] = Double.parseDouble(el[2]);
						QCUtils.parameters[4] = Double.parseDouble(el[3]);
						
						QCUtils.parameters[5] = Double.parseDouble(el[4]);
						QCUtils.parameters[6] = Double.parseDouble(el[5]);
						QCUtils.parameters[7] = Double.parseDouble(el[6]);
						QCUtils.parameters[8] = Double.parseDouble(el[7]);
						
						//variance range
						if(mTab.info.kvm.valueFor("RANGE_LOG") != null)
						{
							buff = mTab.info.kvm.valueFor("RANGE_LOG");
							el = Utils.getElementArray(buff);
							QCUtils.parameters[9] = Double.parseDouble(el[1]);
						}
						else
							QCUtils.parameters[9] = ran.logVarRange;
							
						//variance distribution
						buff = mTab.info.kvm.valueFor("VAR_BLEND");
						el = Utils.getElementArray(buff);
						QCUtils.parameters[10] = Double.parseDouble(el[0]);
						QCUtils.parameters[11] = Double.parseDouble(el[1]);
						
						buff = mTab.info.kvm.valueFor("VAR_MODEL1");
						el = Utils.getElementArray(buff);
						QCUtils.parameters[12] = Double.parseDouble(el[0]);
						QCUtils.parameters[13] = Double.parseDouble(el[1]);
						QCUtils.parameters[14] = Double.parseDouble(el[2]);
						
						buff = mTab.info.kvm.valueFor("VAR_MODEL2_m");
						el = Utils.getElementArray(buff);
						QCUtils.parameters[15] = Double.parseDouble(el[0]);
						QCUtils.parameters[16] = Double.parseDouble(el[1]);
						QCUtils.parameters[17] = Double.parseDouble(el[2]);
						QCUtils.parameters[18] = Double.parseDouble(el[3]);
						
						buff = mTab.info.kvm.valueFor("VAR_MODEL2_s");
						el = Utils.getElementArray(buff);
						QCUtils.parameters[19] = Double.parseDouble(el[0]);
						QCUtils.parameters[20] = Double.parseDouble(el[1]);
						QCUtils.parameters[21] = Double.parseDouble(el[2]);
						QCUtils.parameters[22] = Double.parseDouble(el[3]);
					}
					//if not, all the parameters are re-calculated
					else
					{
						//LNDp signalModel[];
						maModel.LNDMix lndm = new maModel.LNDMix(QCUtils.currentMTable);
							
						//progression state to print on screen, and refresh the frame
						progBuf.append("Estimating signal distribution parameters..."+Utils.lineEnd);
						guiMiddle.setProgStringBuf(progBuf);
					QCUtils.currentFrame.paint(QCUtils.currentFrame.getGraphics());
						
						lndm.optimize(null);
						signalModel = lndm.getParams();
						
						//State and Refresh
						progBuf.append("Estimating variance distribution parameters..."+Utils.lineEnd);
						guiMiddle.setProgStringBuf(progBuf);
					QCUtils.currentFrame.paint(QCUtils.currentFrame.getGraphics());
							
						VarEstimSig VES = new VarEstimSig(QCUtils.currentMTable);
						VES.run(0.0001, 80 , null);
						VES.updateAN(mTab.cmf.F_txt, QCUtils.getTemp());
						
						QCUtils.parameters[0] = ran.logSigRange;
						
						//get the PARAMETERS
						double varBle[] = VES.getParBlend();
						LNDp var1D  = VES.getParD1();
						double var2M[]  = VES.getParD2_Mean();
						double var2V[]  = VES.getParD2_Var();
						
						//signal distribution
						QCUtils.parameters[1] = signalModel[0].getF();
						QCUtils.parameters[2] = signalModel[0].getM();
						QCUtils.parameters[3] = signalModel[0].getS();
						QCUtils.parameters[4] = signalModel[0].getX0();
						
						QCUtils.parameters[5] = signalModel[1].getF();
						QCUtils.parameters[6] = signalModel[1].getM();
						QCUtils.parameters[7] = signalModel[1].getS();
						QCUtils.parameters[8] = signalModel[1].getX0();
						
						//variance range
						QCUtils.parameters[9] = ran.logVarRange;
						
						//variance distribution
						QCUtils.parameters[10] = varBle[0];
						QCUtils.parameters[11] = varBle[1];
						
						QCUtils.parameters[12] = var1D.getX0();
						QCUtils.parameters[13] = var1D.getM();
						QCUtils.parameters[14] = var1D.getS();
						
						QCUtils.parameters[15] = var2M[0];
						QCUtils.parameters[16] = var2M[1];
						QCUtils.parameters[17] = var2M[2];
						QCUtils.parameters[18] = var2M[3];
						
						
						QCUtils.parameters[19] = var2V[0];
						QCUtils.parameters[20] = var2V[1];
						QCUtils.parameters[21] = var2V[2];
						QCUtils.parameters[22] = var2V[3];
					}
					
					//save the current MTable
					QCUtils.currentMTable = mTab;
					
					//set the parameters to the bottom panel
					guiBottom.setParameters(QCUtils.parameters);
					
					//save the parameters to a file
					//saveParamToFile(QCUtils.parameters);
				
				//	tmpSF1311.setMiddlePane(guiMiddle);
				//	tmpSF1311.adjustPanels();
				}
				//case of a ".txt" file
				else if(QCUtils.currentInFile.getName().endsWith(".txt"))
				{
					CompositeMfQC compMf = QCUtils.currentInCompMf;
					saveFile = compMf.F_ma;
					
					if(saveFile == null)
					{
						Utils.message("Impossible to analyse this file!\nSave file is null");
						return;
					}
					
					String dateT;
					String Anames[];
					
					
					// SIGNAL MODEL
					maModel.LNDMix lndm  = new maModel.LNDMix(mTab);
					
					progBuf.append("Estimating signal distribution parameters..."+Utils.lineEnd);
					guiMiddle.setProgStringBuf(progBuf);
						//refresh the frame
					QCUtils.currentFrame.paint(QCUtils.currentFrame.getGraphics());
					
					lndm.optimize(null);
					
					signalModel  = lndm.getParams();
					PLNDist plnd = new PLNDist(signalModel);
					for(int i=0; i<mTab.size; i++)
						mTab.data[i].a = plnd.a_value(mTab.data[i].signal, mTab.data[i].moq);
					mTab.enableA();
					
					System.out.println("SAVE BEFORE 1");
					mTab.saveAsMA0(saveFile);
					System.out.println("final saveFile = "+saveFile.getName());
					mTabFresh = true;
					
				
					
					//writing information into panel components
				/*	tab_gen.pan_top.progress.setString("");
					tab_gen.pan_top.MAD_file.setText(generateFileName(
									dateT,
									tab_gen.pan_top.Intern_User.getContent(),
				      				tab_gen.pan_top.technology.getSelectedIndex(), true )   );
				    tab_gen.pan_top.file_median.setContent(Utils.getDecs(scalingM,8,false));
					tab_gen.pan_top.raw_file_x.setContent(Anames[assay_nr]);
					tab_gen.pan_top.raw_file.setContent(Utils.getLastFolders(fi.getAbsolutePath(),2));
					*/
					
					
				//	acemap_config.setL0_rawDir(txt_opener.getCurrentDirectory() );
					
					System.out.println("save successful");
					
					//save the composite_mf
					QCUtils.currentInCompMf = compMf;
					
				//VARIANCE
					progBuf.append("Estimating variance distribution parameters..."+Utils.lineEnd);
					guiMiddle.setProgStringBuf(progBuf);
					//refresh the frame
					QCUtils.currentFrame.paint(QCUtils.currentFrame.getGraphics());
					
					
					VarEstimSig VES = new VarEstimSig(mTab);
					VES.run(0.0001, 80 , null);
					VES.updateAN(mTab.cmf.F_txt, QCUtils.getTemp());
					
				//RANGE
					File fin = mTab.cmf.F_txt;
					File fout = QCUtils.getTemp();
					
					//signal range
					FlexTable flexTmp = new FlexTable();
					flexTmp.initFromTB(mTab, mTab.heads);
					Range ran = new Range(flexTmp, 99);
					ran.calculateSigRange();
					ran.calculateVarRange();
					ran.saveTo(mTab);
					QCUtils.parameters[0] = ran.logSigRange;
					
					//get the PARAMETERS
					double varBle[] = VES.getParBlend();
					LNDp var1D  = VES.getParD1();
					double var2M[]  = VES.getParD2_Mean();
					double var2V[]  = VES.getParD2_Var();
					
					//signal distribution
					QCUtils.parameters[1] = signalModel[0].getF();
					QCUtils.parameters[2] = signalModel[0].getM();
					QCUtils.parameters[3] = signalModel[0].getS();
					QCUtils.parameters[4] = signalModel[0].getX0();
					
					QCUtils.parameters[5] = signalModel[1].getF();
					QCUtils.parameters[6] = signalModel[1].getM();
					QCUtils.parameters[7] = signalModel[1].getS();
					QCUtils.parameters[8] = signalModel[1].getX0();
					
					//variance range
					QCUtils.parameters[9] = ran.logVarRange;
					
					//variance distribution
					QCUtils.parameters[10] = varBle[0];
					QCUtils.parameters[11] = varBle[1];
					
					QCUtils.parameters[12] = var1D.getX0();
					QCUtils.parameters[13] = var1D.getM();
					QCUtils.parameters[14] = var1D.getS();
					
					QCUtils.parameters[15] = var2M[0];
					QCUtils.parameters[16] = var2M[1];
					QCUtils.parameters[17] = var2M[2];
					QCUtils.parameters[18] = var2M[3];
					
					
					QCUtils.parameters[19] = var2V[0];
					QCUtils.parameters[20] = var2V[1];
					QCUtils.parameters[21] = var2V[2];
					QCUtils.parameters[22] = var2V[3];
					
					//save the current MTable
					QCUtils.currentMTable = mTab;
					
				/*	for(int t = 0; t < QCUtils.parameters.length; t++)
					{
						System.out.println("Param model "+t+" : "+QCUtils.parameters[t]);
					}*/
					
					//set the parameters to the bottom panel
					guiBottom.setParameters(QCUtils.parameters);
					
					//save the parameters to a file
					//saveParamToFile(QCUtils.parameters);
					
					//tmpSF1311.setMiddlePane(guiMiddle);
				//	tmpSF1311.adjustPanels();
				}
				else
				{
					System.out.println("Problem : impossible to analyse the file");
				}
				
				//create the bar view, for the parameters
				String[] titles = {"sigR", "s1-w", "s1-m", "s1-v", "s1-x", "s2-w", "s2-m", "s2-v", "s2-x", "varR", "vb-x", "vb-s", "v1-x", "v1-m", "v1-v", "v2m-y", "v2m-s", "v2m-x", "v2m-s", "v2v-y", "v2v-s", "v2v-x", "v2v-s"};
					//eliminate v1-x
			/*	double[] paramStr = new double[20];
				int d1 = 0;
				for(int d2 = 0; d2 < QCUtils.parameters.length; d2++)
				{
					if(d2 == 10)
						d2++;
					
					paramStr[d1] = QCUtils.parameters[d2];
					d1++;
				}*/
				
				BarView bView = new BarView(titles, QCUtils.parameters);
				bView.setReferences(QCUtils.refMeans, QCUtils.refSds, QCUtils.refWeights);
				bView.calculateSI();//calculate the similarity index
				guiMiddle.setBarView(bView);
				
				//save the SI value to the text file of the ma0, considering that it is possible to get more than 1 similarity index
				//(one by reference file)
				/*if(bView.si != 0.0 && !QCUtils.currentInFile.getName().endsWith(".txt") && QCUtils.refFile != null)
				{
					//check first if this value doesn't exist yet for the current reference file
					String tmpStr = Utils.getDecs(bView.si, 4, false)+"\t"+QCUtils.refFile.getName();
					int cnt = 0;
					String gotElt = mTab.info.kvm.valueFor("SIMILARITY_INDEX_"+cnt);
					boolean appendSIToFile = true;
					while(gotElt != null)
					{
						String[] splitGotElt = gotElt.split("\t");
						if(splitGotElt[splitGotElt.length-1].equals(QCUtils.refFile.getName()))
							appendSIToFile = false;
						cnt++;
						gotElt = mTab.info.kvm.valueFor("SIMILARITY_INDEX_"+cnt);
					}
					
					if(appendSIToFile == true)
					{
						File fin = 	mTab.cmf.F_txt;
						File fout = QCUtils.getTemp();
						
						String inset = new String("SIMILARITY_INDEX_"+cnt+"\t"+tmpStr);
						if(cnt > 0)
							Utils.insertBehind(fin, fout, "SIMILARITY_INDEX_"+(cnt-1), inset);
						else
							Utils.insertBehind(fin, fout, "VAR_MODEL2_s", inset);
						
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
				}*/

				//save the SI value to the text file of the ma0, deleting the last one if it already exists
				if(bView.si != 0.0 && !QCUtils.currentInFile.getName().endsWith(".txt") && QCUtils.refFile != null)
				{
					String tmpStr = Utils.getDecs(bView.si, 4, false)+"\t"+QCUtils.refFile.getName();
					String gotElt = mTab.info.kvm.valueFor("SIMILARITY_INDEX");
				
					File fin = 	mTab.cmf.F_txt;
					File fout = QCUtils.getTemp();
					
					//check first if a SI value doesn't exist yet
					if(gotElt != null)
					{
						QCUtils.removeMarkInFile(fin, fout, "SIMILARITY_INDEX");
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
					
					//then append the value
					fin = 	mTab.cmf.F_txt;
					fout = QCUtils.getTemp();
					String inset = new String("SIMILARITY_INDEX\t"+tmpStr);
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
				
				tmpSF1311.setMiddlePane(guiMiddle);
				tmpSF1311.adjustPanels();
				
				//beep to signalize the end
				Toolkit.getDefaultToolkit().beep();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
				Utils.message("Impossible to analyse the file");
			}
			finally
			{
				guiMiddle.setProgression(false);
				//refresh the frame
				QCUtils.currentFrame.paint(QCUtils.currentFrame.getGraphics());
				thCursor.joinThread();
			}
		}
	}
	
	/**
	 *Calculate parameters for the specified CompositeMfQC. 
	 *Needed for example for reference file.
	 *@param compMf the CompositeMfQC for which it's needed to get the parameters
	 *@return the list of parameters
	 */
	
	private double[] calculateParameters(MTable mTab, StringBuffer progBuf, Range ra)
	{
	//	CompositeMfQC compMf = QCUtils.currentInCompMf;
		//saveFile = compMf.F_ma;
		
		double[] param = new double[23];
		
		SplitFrame1311 tmpSF1311 = (SplitFrame1311)QCUtils.currentFrame;
		GUIMiddle guiMiddle;
		
		if((GUIRef)tmpSF1311.getRightPane() != null)
			guiMiddle = (GUIMiddle)tmpSF1311.getMiddlePane();
		else
			guiMiddle = new GUIMiddle();
		
		/*if(saveFile == null)
		{
			Utils.message("Impossible to analyse this file!\nSave file is null");
			return null;
		}*/
		
		String dateT;
		String Anames[];
		
		// SIGNAL MODEL
		maModel.LNDMix lndm  = new maModel.LNDMix(mTab);
		
		progBuf.append("   Estimating signal distribution parameters..."+Utils.lineEnd);
		guiMiddle.setProgStringBuf(progBuf);
			//refresh the frame
		QCUtils.currentFrame.paint(QCUtils.currentFrame.getGraphics());
		
		lndm.optimize(null);
		
		signalModel  = lndm.getParams();
		PLNDist plnd = new PLNDist(signalModel);
		for(int i=0; i<mTab.size; i++)
			mTab.data[i].a = plnd.a_value(mTab.data[i].signal, mTab.data[i].moq);
		mTab.enableA();
		
		//mTab.saveAsMA0(saveFile);
		//System.out.println("final saveFile = "+saveFile.getName());
		mTabFresh = true;
		
	//VARIANCE
		progBuf.append("   Estimating variance distribution parameters..."+Utils.lineEnd);
		guiMiddle.setProgStringBuf(progBuf);
		//refresh the frame
		QCUtils.currentFrame.paint(QCUtils.currentFrame.getGraphics());
		
		VarEstimSig VES = new VarEstimSig(mTab);
		VES.run(0.0001, 80 , null);
		VES.updateAN(mTab.cmf.F_txt, QCUtils.getTemp());
		
		//get the PARAMETERS
		double varBle[] = VES.getParBlend();
		LNDp var1D  = VES.getParD1();
		double var2M[]  = VES.getParD2_Mean();
		double var2V[]  = VES.getParD2_Var();
		
		//signal range
		Range ran = null;
		if(ra == null)
		{
			FlexTable flexTmp = new FlexTable();
			flexTmp.initFromTB(mTab, mTab.heads);
			ran = new Range(flexTmp, 99);
			ran.calculateSigRange();
			ran.calculateVarRange();
			ran.saveTo(mTab);
			param[0] = ran.logSigRange;
		}
		else
			ran = ra;
		
		//signal distribution
		param[1] = signalModel[0].getF();
		param[2] = signalModel[0].getM();
		param[3] = signalModel[0].getS();
		param[4] = signalModel[0].getX0();
		
		param[5] = signalModel[1].getF();
		param[6] = signalModel[1].getM();
		param[7] = signalModel[1].getS();
		param[8] = signalModel[1].getX0();
		
		//variance range
		param[9] = ran.logVarRange;
		
		//variance distribution
		param[10] = varBle[0];
		param[11] = varBle[1];
		
		param[12] = var1D.getX0();
		param[13] = var1D.getM();
		param[14] = var1D.getS();
		
		param[15] = var2M[0];
		param[16] = var2M[1];
		param[17] = var2M[2];
		param[18] = var2M[3];
		
		
		param[19] = var2V[0];
		param[20] = var2V[1];
		param[21] = var2V[2];
		param[22] = var2V[3];
		
		for(int t = 0; t < param.length; t++)
		{
			System.out.println("Param (ref) "+t+" : "+param[t]);
		}
		
		return param;
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

