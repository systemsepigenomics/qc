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
 *Panel that shows the parameters of the model
 *@author Guillaume Brysbaert
 *@version 0.58
 */

public class GUIBottom extends JPanel
{
	private double[] parameters;
	final	String[] parametersTitles = {"s_range", "weight", "mean", "var", "x0", "weight", "mean", "var", "x0", "v_range", "x_offset", "slope", "x0", "mean", "var", "y_offset", "scale", "x_offset", "slope", "y_offset", "scale", "x_offset", "slope"};
	
	Font bottomFont;
	Font bottomTitleFont;
	
	int cellWidth = 50;
	
	
	public GUIBottom()
	{
		super(null);
		setBackground(Color.white);
		
		bottomFont = new Font("Verdana", Font.PLAIN, 10);
		bottomTitleFont = new Font("Arial Bold", Font.PLAIN, 12);
		
		paint(this.getGraphics());
	}
	
	/**
	 *Sets the array that contains the parameters to paint
	 */
	
	public void setParameters(double[] param)
	{
		this.parameters = param;
		paint(this.getGraphics());
	}
	
	/**
	 *Paints all the parameters
	 */
	
	public void paintComponent(Graphics g)
	{
		int w = this.getSize().width;
		int h = this.getSize().height;
		
		g.setColor(Color.white);
		g.fillRect(0, 0, w, h);
		
		if(parameters != null)
	 		drawParameters(g);
	}
	
	/**
	 *Draws the parameters(called by the paint method)
	 */
	 private void drawParameters(Graphics g)
	 {
	 	int w = this.getSize().width;
		int h = this.getSize().height;
		
		int cellHeight = h/9;
		int startTab = w/2-13*50/2;
		
		int lineAscent = this.getFontMetrics(this.getFont()).getAscent();
		
	//	Toolkit tk = Toolkit.getDefaultToolkit();
	//	Image img = tk.getImage(QCUtils.imagePath+File.separator+"ParamBackgr.jpg");
	//  prepareImage(img, null);
		
		
	//	g.drawImage(img, 20, 5, w-40, h-5, Color.white, null);
		
		//draw the two 3D lines
		g.setColor(new Color(90, 90, 90));
		g.fill3DRect(startTab-cellHeight+3, 4, 2, h-5-3, true);
		g.fill3DRect(startTab-cellHeight+3, 3, w-(startTab-20)*2, 2, true);
		
 		g.setColor(Color.black);
 		//g.drawLine(startTab, h/2, 987, h/2);
 		
 		//write title
		g.setFont(bottomTitleFont);
		//g.drawString("AB1700 Input Data Parameter", startTab+8*50+(int)(2.5*50)-Utils.stdWidth("AB1700 Input Data Parameter"), (int)(2.5*cellHeight));
		g.drawString("AB1700 Input Data Parameter", startTab+9*50+17, (int)(2.5*cellHeight));
		g.drawString("Estimates", startTab+9*50+17+57, (int)(3.5*cellHeight));
		
 		g.setFont(bottomFont);
 		
 		//draw the horizontal lines
 		g.drawLine(startTab, cellHeight, 8*cellWidth+startTab, cellHeight);
 		g.drawLine(startTab, 2*cellHeight, 8*cellWidth+startTab, 2*cellHeight);
 		g.drawLine(startTab, 3*cellHeight, 8*cellWidth+startTab, 3*cellHeight);
 		g.drawLine(startTab, 4*cellHeight, 8*cellWidth+startTab, 4*cellHeight);
 		g.drawLine(startTab, 5*cellHeight, 13*cellWidth+startTab, 5*cellHeight);
 		g.drawLine(startTab, 6*cellHeight, 13*cellWidth+startTab, 6*cellHeight);
 		g.drawLine(startTab, 7*cellHeight, 13*cellWidth+startTab, 7*cellHeight);
 		g.drawLine(startTab, 8*cellHeight, 13*cellWidth+startTab, 8*cellHeight);
 		
 	//	g.drawLine(startTab, h/3, 8*cellWidth, h/3);
 	//	g.drawLine(startTab, 2*h/3, 13*cellWidth, 2*h/3);
 		
 		//draw the first vertical lines
 		//g.drawLine(startTab, h/3-lineAscent-4, startTab, h/3+lineAscent+4);
 		//g.drawLine(startTab, 2*h/3-lineAscent-4, startTab, 2*h/3+lineAscent+4);
 		g.drawLine(startTab, cellHeight, startTab, 4*cellHeight);
 		g.drawLine(startTab, 5*cellHeight, startTab, 8*cellHeight);
 		
 		//titles
 		g.drawString("signal distribution 1", startTab+2*cellWidth-Utils.stdWidth("signal distribution 1")/2, 2*cellHeight-3);
 		g.drawString("signal distribution 2", startTab+6*cellWidth-Utils.stdWidth("signal distribution 2")/2, 2*cellHeight-3);
 		g.drawString("var blending", startTab+cellWidth-Utils.stdWidth("var blending")/2, 6*cellHeight-3);
 		g.drawString("var distribution 1", (int)(startTab+3.5*cellWidth)-Utils.stdWidth("var distribution 1")/2, 6*cellHeight-3);
 		g.drawString("var distribution 2 - mean", startTab+7*cellWidth-Utils.stdWidth("var distribution 2 - mean")/2, 6*cellHeight-3);
 		g.drawString("var distribution 2 - var", startTab+11*cellWidth-Utils.stdWidth("var distribution 2 - var")/2, 6*cellHeight-3);
 		//g.drawString("signal distribution 2", startTab+6*cellWidth-Utils.stdWidth("signal distribution 2")/2, 7*cellHeight-3);
 	
 		for(int i = 0; i < 9; i++)
		{
			int index = i-1;
			//omit the signal range and the variance range
			if(i == 0)
				continue;
			
			g.setColor(Color.black);
			
			String currentTitle = Utils.getDecs(parameters[i],4, false);
			
			g.drawString(parametersTitles[i], startTab+index*cellWidth+24-Utils.stdWidth(parametersTitles[i])/2, 3*cellHeight-3);
			//vertical lines
			//if(i != 7)			
			if(index!=3 && index!=7)
				g.drawLine(startTab+(index+1)*cellWidth,  2*cellHeight, startTab+(index+1)*cellWidth, 4*cellHeight);
			else
				g.drawLine(startTab+(index+1)*cellWidth,  cellHeight, startTab+(index+1)*cellWidth, 4*cellHeight);
			
			g.setColor(Color.gray);
			g.drawString(Utils.getDecs(parameters[i],4, false), startTab+index*cellWidth+24-Utils.stdWidth(currentTitle)/2, 4*cellHeight-3);
		}
		
		for(int i = 10; i < parameters.length; i++)
		{
			int index = i-2;
			g.setColor(Color.black);
			
			String currentTitle = Utils.getDecs(parameters[i],4, false);
			
			//horizontal lines
			g.drawString(parametersTitles[i], startTab+(index-8)*cellWidth+24-Utils.stdWidth(parametersTitles[i])/2, 7*cellHeight-3);
			if(index!= 9 && index!=12 && index!=16 && index != parameters.length-1-2)
				g.drawLine(startTab+(index-8+1)*cellWidth, 6*cellHeight, startTab+(index-8+1)*cellWidth, 8*cellHeight);
			else
				g.drawLine(startTab+(index-8+1)*cellWidth, 5*cellHeight, startTab+(index-8+1)*cellWidth, 8*cellHeight);
			
			g.setColor(Color.gray);
			g.drawString(Utils.getDecs(parameters[i],4, false), startTab+(index-8)*cellWidth+24-Utils.stdWidth(currentTitle)/2, 8*cellHeight-3);
		}
	}
	
	/**
	 *Sets the bounds of each element of the Panel
	 */
	
	public void setBounds(int x, int y, int w, int h)
	{
		super.setBounds(x,y,w,h);
	}
}
