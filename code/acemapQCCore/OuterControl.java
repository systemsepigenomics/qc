package acemapQCCore;

import acemapCore.*;
import acemapQCStyle.*;
import acemapQC.*;

import java.awt.*;
import java.awt.event.*;
import java.lang.String;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import aceStyle.*;

/**
 *Frame handling object for the main window.
 *@author Guillaume Brysbaert
 *@version 0.58
 */

public class OuterControl implements ComponentListener, WindowListener
{
	final SplitFrame1311    mainFrame   = new SplitFrame1311("ace.map QC "+QCUtils.versionString+" - microarray quality control for AB1700", 800, 580);
	
	final GUITop guiTop;
	final GUIIn guiInput;
	final GUICreate guiCreate;
	final GUIRef guiReference;
	final GUIBottom guiBot;
	
	ImageIcon windowicon;
	
	public OuterControl()
	{
		QCUtils.currentFrame = mainFrame;
		
		//set the look and feel
		try
	    {
	      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    }
	    catch (Exception e)
	    {
	      e.printStackTrace();
	    }
		//
		
		mainFrame.setDefaultCloseOperation(    JFrame.DO_NOTHING_ON_CLOSE);
		
		//create work directories
		if(!(new File(QCUtils.inputPath).exists()))
			(new File(QCUtils.inputPath)).mkdir();
		if(!(new File(QCUtils.refPath).exists()))
			(new File(QCUtils.refPath)).mkdir();
		
	//	Utils.init(mainFrame);
	//	acemap_config.reload();
		
	/*	if(loadLU)
			loadABGeneTable();*/
		
	//	tempPath = acemap_config.getAsFile("TEMPFILE PATH");
		
		if(QCUtils.tempPath == null || !QCUtils.tempPath.exists())
		{
			QCUtils.tempPath = new File("temp");
			QCUtils.tempPath.mkdir();
			System.out.println("creating temp path "+QCUtils.tempPath.getAbsolutePath() );
		}
		else
		{
			System.out.println("found valid temp path "+QCUtils.tempPath.getAbsolutePath() );
			//delete files in "temp" directory
			QCUtils.cleanTmp();
		}
		
		if(QCUtils.tempPath == null)
			Utils.message("temp path not properly set");
		
		//mainFrame.setSplitEdge(560);
		
//		AceUtil.init(mainFrame);
		
		//initiation of the panels
		guiTop = new GUITop();
		guiInput = new GUIIn();
		guiCreate = new GUICreate();
		guiReference = new GUIRef();
		guiBot = new GUIBottom();
		
		AcePanel middleP = new AcePanel();
		
		windowicon = new ImageIcon(QCUtils.imagePath+File.separator+"w_icon.jpg");
		
		mainFrame.setIconImage( windowicon.getImage()   );
		mainFrame.addComponentListener(this);
		mainFrame.addWindowListener(this);
		
//		mainFrame.setLeftPane(gui_left);
				
		//guiTop.setBackground(new Color(220, 220, 220));
		middleP.setBackground(Color.white);
			
		mainFrame.setTopPane(guiTop);
		mainFrame.setLeftPane(guiInput);
		mainFrame.setCenterPane(guiCreate);
		mainFrame.setRightPane(guiReference);
		mainFrame.setMiddlePane(middleP);
		mainFrame.setBottomPane(guiBot);
		
		//unselect analyze button
		SplitFrame1311 tmpSF1311 = (SplitFrame1311)QCUtils.currentFrame;
		GUICreate guiCreate = (GUICreate)tmpSF1311.getCenterPane();
		guiCreate.analyzeBut.setEnabled(false);
		
		QCUtils.init(QCUtils.currentFrame);
		
		mainFrame.pack();
		mainFrame.setSize(new Dimension(800, 580));
		mainFrame.setVisible(true);
		
		//mainFrame.paintAll(mainFrame.getGraphics());
		
		//INITMESSAGE
		System.out.println("initiation for class "+this.getClass().getName()+" accomplished");
		
	}
	
	//// COMPONENT EVENTS ////
	
	public void componentHidden(ComponentEvent e){}
	public void componentMoved(ComponentEvent e){}
	public void componentShown(ComponentEvent e){}

	public void componentResized(ComponentEvent e)
	{
		mainFrame.setSize(800,580);
		mainFrame.adjustPanels();
		guiCreate.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 1, new Color(220, 220, 220)));
	//	mainFrame.paintAll(mainFrame.getGraphics());
	}
	
	
	//// WINDOW EVENTS ////
	
	public void 	windowActivated(WindowEvent e)
	{
	//	mainFrame.paintAll(mainFrame.getGraphics());
	}
	public void 	windowClosed(WindowEvent e){
		//System.out.println("closed");
		//AceUtil.message("closed");
	}
	public void 	windowDeactivated(WindowEvent e)
	{
	//	mainFrame.paintAll(mainFrame.getGraphics());
	}
	public void 	windowDeiconified(WindowEvent e)
	{
	//	mainFrame.paintAll(mainFrame.getGraphics());
	}
	public void 	windowIconified(WindowEvent e){}
	public void 	windowOpened(WindowEvent e){}
	public void 	windowClosing(WindowEvent e)
	{
		QCUtils.cleanTmp();
		System.exit(0);
	}
}
