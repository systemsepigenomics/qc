package acemapQC;

import acemapCore.*;
import aceStyle.*;
import maModel.*;

import acemapQCStyle.*;
import acemapQCCore.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.io.*;

import java.util.*;


//import maModel.*;

/**
 *Top interface class : contains the image and info buttons
 *@author Guillaume Brysbaert
 *@version 0.58
 */

public class GUITop extends JPanel implements ActionListener
{
	//logo
	final ImageIcon logo = new ImageIcon(QCUtils.imagePath+File.separator+"acemapQC_title.jpg");
	AceLabel logoLabel;
	
	final AceLabel		infoLab1 = new AceLabel("|");
	final AceButton infoBut = new AceButton("info");
	final AceLabel		infoLab2 = new AceLabel("|");
	
	final AceLabel		helpLab1 = new AceLabel("|");
	final AceButton helpBut = new AceButton("help");
	final AceLabel		helpLab2 = new AceLabel("|");
	
	int lineAscent = this.getFontMetrics(this.getFont()).getAscent();
	
	public GUITop()
	{
		super(null);
		setBackground(Color.white);
		
		logoLabel = new AceLabel(logo);
		
		add(infoLab1);
		add(infoBut);
		add(infoLab2);
		
		add(helpLab1);
		add(helpBut);
		add(helpLab2);
		
		add(logoLabel);
		
		infoBut.addActionListener(this);
		helpBut.addActionListener(this);
	}
	
	/**
	 *Set the bounds of each element of the Panel
	 */
	
	public void setBounds(int x, int y, int w, int h)
	{
		super.setBounds(x,y,w,h);
		logoLabel.setBounds(w/2-logo.getIconWidth()/2, 0, logo.getIconWidth(), h);
		//logoLabel.setBounds(0, 0, logo.getIconWidth(), h);
		logoLabel.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, new Color(220, 220, 220)));
		
		infoLab1.setBounds(16,h/2-lineAscent,5,20);
		infoBut.setBounds(20,h/2-lineAscent,25,20);
		infoLab2.setBounds(47,h/2-lineAscent,5,20);
		
		helpLab1.setBounds(w-57,h/2-lineAscent,5,20);
		helpBut.setBounds(w-50,h/2-lineAscent,25,20);
		helpLab2.setBounds(w-23,h/2-lineAscent,5,20);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		Object 			o = e.getSource();
		
		if(o==infoBut)
		{
			File infoFile = new File("info"+File.separator+"acemapQC_info.html");
			String link = "file:///"+infoFile.getAbsolutePath();
			
			//launch it in the default browser
			BareBonesBrowserLaunch.openURL(link);
			
			/*
			System.out.println("INFO BUTTON PRESSED");
			
			ace_frame infoFrame = new ace_frame("ace.map creator - info", 700, 500);
			AceLabel infoLabel1 = new AceLabel("Systems Epigenomics Group");
			AceLabel infoLabel2 = new AceLabel(" ");
			AceLabel infoLabel3 = new AceLabel("Arndt Benecke");
			AceLabel infoLabel4 = new AceLabel("Guillaume Brysbaert");
			
			int[] string_t_widths = {200, 200};
			
			ace_stringtable_scr infoTable = new ace_stringtable_scr(16, string_t_widths, 2, true, null, 0);
			
			Vector vectForTable = new Vector();
			
		//	String[] fusionTmp = new String[1];
			
			//file name
		//	for(int i = 0; i < 10; i++)
		//	{
				String[] fusionTmp0 = new String[2];
				fusionTmp0[0] = "copyright notice and disclaimer";
			
			//	String[] fusionTmp1 = new String[1];
				fusionTmp0[1] = "Download and use of the ace.map qc 1.0 software is dependent upon and concomitantly de facto indicator of unconditional acceptance of the following terms: Access to the http://www.iri.cnrs.fr/seg web-server and download of software or supplementary materials from this source requires acceptance of and compliance with: 1. The intellectual property rights for the software as well as any additional material provided here remain entirely with the authors. 2. The use of the software and the additional material is restricted to non-commercial research use only. 3. Commercial entities/users shall inquire with Arndt Benecke – Institut des Hautes Études Scientifiques (arndt@ihes.fr, http://www.ihes.fr/~arndt) for commercial licenses in order to download and use any of the software or materials made available. 4. Neither the authors nor their institutional partners provide any guarantees to the correct functioning of the software. We do not accept any explicit or implicit liability for damage caused directly or indirectly through the download or use of the software and supplementary materials provided. 5. Reverse engineering is not permissible. The source codes will be made available to qualified users upon written request and return of a signed Materials Transfer Agreement. 6. Any scientific publication or other form of communication based on or involving the use of the material provided here shall make proper reference to the source of the material according to scientific standards (see above).";
				
				vectForTable.add(fusionTmp0);
					
				//	fusionTmp[1] = "
			//	vectForTable.add(fusionTmp0);
		//	}
			
			String[] fusionTmp1 = new String[2];
			fusionTmp1[0] = "copyright notice and disclaimer";
		
		//	String[] fusionTmp1 = new String[1];
			fusionTmp1[1] = "Download and use of the ace.map qc 1.0 software is dependent upon and concomitantly de facto indicator of unconditional acceptance of the following terms: Access to the http://www.iri.cnrs.fr/seg web-server and download of software or supplementary materials from this source requires acceptance of and compliance with: 1. The intellectual property rights for the software as well as any additional material provided here remain entirely with the authors. 2. The use of the software and the additional material is restricted to non-commercial research use only. 3. Commercial entities/users shall inquire with Arndt Benecke – Institut des Hautes Études Scientifiques (arndt@ihes.fr, http://www.ihes.fr/~arndt) for commercial licenses in order to download and use any of the software or materials made available. 4. Neither the authors nor their institutional partners provide any guarantees to the correct functioning of the software. We do not accept any explicit or implicit liability for damage caused directly or indirectly through the download or use of the software and supplementary materials provided. 5. Reverse engineering is not permissible. The source codes will be made available to qualified users upon written request and return of a signed Materials Transfer Agreement. 6. Any scientific publication or other form of communication based on or involving the use of the material provided here shall make proper reference to the source of the material according to scientific standards (see above).";
			
			vectForTable.add(fusionTmp1);
			
			infoTable.setStringData2(vectForTable, null);
			
			infoFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			
			//layout
			GridLayout grid = new GridLayout(1,1);
			infoFrame.getContentPane().setLayout(grid);
			
			//background color
			infoFrame.setBackground(Color.white);
			infoFrame.getContentPane().setBackground(Color.white);
			
			//bounds
			int w = infoFrame.getSize().width;
			int h = infoFrame.getSize().height;
			
			//infoLabel1.setBounds(0, 0, w, lineAscent+2);
			
			infoFrame.add(infoTable);
			
			infoFrame.setVisible(true);
			*/
		}
		else if(o==helpBut)
		{
			File helpFile = new File("info"+File.separator+"acemapQCUsersGuide.pdf");
			String link = "file:///"+helpFile.getAbsolutePath();
			
			//launch it in the default browser
			BareBonesBrowserLaunch.openURL(link);
		}
		return;
	}
}