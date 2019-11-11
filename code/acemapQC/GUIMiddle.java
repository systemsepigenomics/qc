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
//import maModel.*;

/**
 *Density graph panel
 *@author Guillaume Brysbaert
 *@version 0.58
 */

public class GUIMiddle extends JPanel
{
	//private	final	int[]			widthsR  = { 20, 150, 50, 50 };
	private DensPan dspIn = null;
	private BarView bView = null;
	
	//density panels
//	public DensPan densPanels[];
	
	//arrow images
	Image arrowUp;// = new ImageIcon(QCUtils.imagePath+File.separator+"arr_up.jpg");
	Image arrowDown;// = new ImageIcon(QCUtils.imagePath+File.separator+"arr_down.jpg");
	
//	private final ace_sliderD sliderDens = new ace_sliderD(false, false,  0.0f, 1.0f);
	
	final	AceLabel		inputLab = new AceLabel("Input:");
	public final	AceTextField	inputTxt  = new AceTextField("");
	final	AceButton		inputBut   = new AceButton("browse");
	
	//total number of density panels (out panels)
	int DensPanNb = 0;
	
	//selected density panel (out)
	int DensPanSel = 0;
	
	//current slider position
	float sliderPos = 0;
	
	//progression value : specify if the progression state of the creation will be painted
	private boolean progression = false;
	private boolean progAtLeft = false;
	private StringBuffer progStringBuf;
	
	public GUIMiddle()
	{
		super(null);
//		sliderDens.addSliderDListener(this);
		setBackground(Color.white);
		
		//initialise the images
		Toolkit tk = Toolkit.getDefaultToolkit();
		arrowUp = tk.getImage(QCUtils.imagePath+File.separator+"arrUp.jpg");
		arrowDown = tk.getImage(QCUtils.imagePath+File.separator+"arrDown.jpg");
		prepareImage(arrowUp, null);
		prepareImage(arrowDown, null);
		
		progStringBuf = new StringBuffer();
	}
	
	/**
	 *Set the bounds of each element of the Panel
	 */
	
	public void setBounds(int x, int y, int w, int h)
	{
		super.setBounds(x,y,w,h);
		
		if(dspIn != null)
			//dspIn.setBounds(50, 50, w/2-100, h-100);
			dspIn.setBounds(25, 10, 2*w/5-25, h-10);
		
		if(bView != null)
			//bView.setBounds(3*w/5, 10, 2*w/5-25, h-10);
			bView.setBounds(2*w/5+20, 10, w-(2*w/5+20), h-30);
	}
	
/*	public void paint()
	{
		dspIn.repaint();
		densPanels[0].repaint();
		
		int w = this.getSize().width;
		int h = this.getSize().height;
		
		if(DensPanNb >= 1)
		{
			sliderDens.setBounds(w/2+50+w/2-100+25, 50, w/2-100, h-100);
			add(sliderDens);
		}
	}*/
	
	/**
	 *Set the density panel and add it to this current bottom panel
	 *@param dp the density panel to add
	 */
	
	public void setDensPanIn(DensPan dpI)
	{
		this.dspIn = dpI;
		
	/*	if(dspIn != null)
			add(dspIn);
		
		if(activeDenspan != null)
			add(activeDenspan);*/
		
		paint(this.getGraphics());
	}
	
/*	public void setDensPanOut(DensPan dpO)
	{
		initDensPanelsOut(1);
		this.densPanels[0] = dpO;
				
		if(dspIn != null)
			add(dspIn);
		
		if(densPanels != null)
		{
			int w = this.getSize().width;
			int h = this.getSize().height;
			
			densPanels[0].setBounds(w/2+50, 50, w/2-100, h-100);
			add(densPanels[0]);
			
			float tmpF = 1/((float)DensPanNb);
			
			sliderDens.setRange(0.0f, 1.0f, tmpF);
			sliderDens.setPositionSilent(sliderPos);
			sliderDens.setBounds(w/2+50+w/2-100+3, 50, 20, densPanels[0].inp.getSize().height);
			add(sliderDens);
		}
	}*/
	
	/**
	 *Set the BarView, visualization of the parameters
	 */
	
	public void setBarView(BarView bv)
	{
		this.bView = bv;
		paint(this.getGraphics());
	}

	public void paintComponent(Graphics g)
	{
		removeAll();
		int w = this.getSize().width;
		int h = this.getSize().height;
		
		g.setColor(Color.white);
		g.fillRect(0,0, w, h);
		
		if(dspIn != null)
		{
			add(dspIn);
			
			//int lineAscent = this.getFontMetrics(this.getFont()).getAscent();
			//if there is more than 1 density panel, draw arrows to switch the density panels
		}
		
		if(progression == false)
		{
			if(bView != null)
			{
				add(bView);
			}
		}
		//print the progression in the calculation of the parameters
		else
		{
			g.setColor(Color.gray);
			int lineAscent = this.getFontMetrics(this.getFont()).getAscent();
			
			//get each line;
			String[] progSplit = progStringBuf.toString().split(Utils.lineEnd);
			
			//and paint the lines
			int init = 20;
			
			if(progSplit.length >= 1)
			{
				for(int i = 14*(int)((progSplit.length-1)/14); i < progSplit.length; i++)
				{
					if(progAtLeft == true)
						g.drawString(progSplit[i], 20, init);
					else
						g.drawString(progSplit[i], 3*w/5, init);
					init = init+lineAscent+3;
				}
			}
		}
		
	//	setBounds(0, 0, w, h);
	}
	
/*	public void actionPerformed(ActionEvent e)
	{
		Object o = e.getSource();
	}*/

	/**
	 *Set the progression. If the progression is "true", labels with the current state 
	 *of the creation will be painted. If "false", the density panel of the created 
	 *synthetic data will be painted.
	 *@param progValue the progression value.
	 */
	
	public void setProgression(boolean progValue)
	{
		this.progAtLeft = false; //left
		this.progression = progValue;
	}
	
	/**
	 *Set the progression StringBuffer.
	 *@param sb the StringBuffer
	 */
	
	public void setProgStringBuf(StringBuffer sb)
	{
		this.progStringBuf = sb;
	}
	
	/**
	 *Set the progression StringBuffer at left of default places (right).
	 *@param sb the StringBuffer
	 *@param left if "true", the comment will be printed on the left else, default case (right)
	 */
	
	public void setProgStringBuf(StringBuffer sb, boolean left)
	{
		this.progAtLeft = left;
		this.progStringBuf = sb;
	}
}