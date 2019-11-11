package acemapQCStyle;

import acemapCore.*;
import aceStyle.*;

//import acemapQCCore.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.io.*;
import java.nio.*;

//import maModel.*;

/**
 *Panel that shows a representation of the parameters of the model on a bar : 
 *one bar per parameter (the parameter x0 will be omitted for graphic convenience - this 
 *parameter is always constant). It allows to locate the parameters comparing to the mean 
 *and variance.
 *@author Guillaume Brysbaert
 *@version 0.58
 */

public class BarView extends JPanel
{
	//values
	private double[] parameters;
	private String[] titles;
	
	//references
	private double[] meansRef;
	private double[] sdsRef;
	private double[] wsRef;
	
	public final String[] heads = new String[9];
	
	Font barFont;
	
	//sizes of the elements of the bar views
 	public int cellHeight = 9;
 	public int cellWidth  = 21;
 	
 	//number of bars per column
 	public int nbBarsPerColumn = 12;
 	
 	//similarity index
 	public double si = 0.0;
 	
 	private double SQR(double x){
 		return x*x; 		
 	} 
	
	
	public BarView(String[] paramTitles, double[] param)
	{
		super(null);
		
	 	this.titles = paramTitles;
	 	this.parameters = param;
	 	barFont = new Font("Verdana", Font.PLAIN, 11);
	 	
	 	
	 	//fill the table that contains mean and standrad deviation heads
	 		//get the sigma character
	 	byte[] sigmaByte = new byte[2];
		sigmaByte[0] = (byte) 0xCF;
		sigmaByte[1] = (byte) 0x83;
		
	 	String sigma = "";
	 	try
	 	{
	 		sigma = new String(sigmaByte, "UTF-8");
	 	}
	 	catch(Exception e)
	 	{
	 		sigma = "S";
	 		e.printStackTrace();
	 		System.out.println("Impossible to create the sigma character");
	 	}
	 	
	 	heads[0] = "10"+sigma;
	 	heads[1] = "3"+sigma;
	 	heads[2] = "2"+sigma;
	 	heads[3] = "1"+sigma;
	 	heads[4] = "M";
	 	heads[5] = "1"+sigma;
	 	heads[6] = "2"+sigma;
	 	heads[7] = "3"+sigma;
	 	heads[8] = "10"+sigma;
	}
	
	/*
	 *Create a bar representation starting from parameters
	 */
	 
/*	 public static BView createFromParamList(String[] paramTitles, double[] param)
	 {
	 
	 }*/
	
	/**
	 *Paints all the bars for a representation of the parameters 
	 */
	
	public void paintComponent(Graphics g)
	{
		int w = this.getSize().width;
		int h = this.getSize().height;
	 	
		g.setColor(Color.white);
		g.fillRect(0, 0, w, h);
		
		//draw the bars
		drawBars(g);
		
		//draw the similarity index
		drawSimIndex(g);
		
	//	if(parameters != null)
	 //		drawParameters(g);
	}
	
	/**
	 *Draw the bars : one per parameter
	 */
	 private void drawBars(Graphics g)
	 {
	 	//width and height of the panel
	 	int w = this.getSize().width;
	 	int h = this.getSize().height;
		
	 	//get the sigma character
	 	byte[] sigmaByte = new byte[2];
		sigmaByte[0] = (byte) 0xCF;
		sigmaByte[1] = (byte) 0x83;
		
	 	String sigma = "";
	 	try
	 	{
	 		sigma = new String(sigmaByte, "UTF-8");
	 	}
	 	catch(Exception e)
	 	{
	 		sigma = "S";
	 		e.printStackTrace();
	 		System.out.println("Impossible to create the sigma character");
	 	}
	 	
	 	g.setFont(barFont);
	 	
	 	//sizes of the elements of the bar views
	  	int offsetV = 10;
	 	int offsetH = 0;
	 	int shiftTitle = 5;
	 	
	 	int maxTitlesSize = 0;
	 	if(titles != null)
	 	{
	 		maxTitlesSize = this.getMaxLength(titles);
	 	}
	 	
 		//heads
 		g.setColor(Color.gray);
	 	for(int i = 0; i < 9; i++)
	 	{
	 		if(i == 1 || i == 2 || i == 3 || i == 5 || i == 6 || i == 7)
	 		{
	 			g.drawString(heads[i], maxTitlesSize+shiftTitle+i*cellWidth-Utils.stdWidth("00")/2, 8);
	 			g.drawString(heads[i], w/2+maxTitlesSize+shiftTitle+i*cellWidth-Utils.stdWidth("00")/2, 8);
	 		}
	 		else if(i == 4)
	 		{
	 			g.drawString(heads[i], maxTitlesSize+shiftTitle+i*cellWidth-Utils.stdWidth("M")/2, 8);
	 			g.drawString(heads[i], w/2+maxTitlesSize+shiftTitle+i*cellWidth-Utils.stdWidth("M")/2, 8);
	 		}
	 		else if(i == 0 || i == 8)
	 		{
	 			g.drawString(heads[i], maxTitlesSize+shiftTitle+i*cellWidth-Utils.stdWidth("000")/2, 8);
	 			g.drawString(heads[i], w/2+maxTitlesSize+shiftTitle+i*cellWidth-Utils.stdWidth("000")/2, 8);
	 		}
	 	}
	 	
	 	boolean error = false;
	 	
	 	//draw the bars
	 	for(int b = 0; b < titles.length; b++)
	 	{
	 		//System.out.println("PARAMETERS[B] : "+parameters[b]+"\tMEANS[B] : "+meansRef[b]+"\tSDS[B] : "+sdsRef[b]);
	 		//start a new column
	 		if(b == nbBarsPerColumn)
	 		{
	 			//set the horizontal offset : draw on the right
	 			offsetH = w/2;
	 			offsetV = 10;
	 		}
	 		
	 		offsetV = 10+(b%nbBarsPerColumn)*(h/nbBarsPerColumn);
	 		
	 		g.setColor(Color.gray);
	 		//draw the name of the bar
	 		if(titles != null && titles[b] != null)
	 			g.drawString(titles[b], offsetH, offsetV+cellHeight);
			
	 		//horizontal
		 	g.drawLine(offsetH+maxTitlesSize+shiftTitle, offsetV, offsetH+maxTitlesSize+shiftTitle+8*cellWidth, offsetV);
		 	g.drawLine(offsetH+maxTitlesSize+shiftTitle, offsetV+cellHeight, offsetH+maxTitlesSize+shiftTitle+8*cellWidth, offsetV+cellHeight);
		 	
		 	//vertical lines
		 	for(int i = 0; i < 9; i++)
		  		g.drawLine(offsetH+maxTitlesSize+shiftTitle+i*cellWidth, offsetV, offsetH+maxTitlesSize+shiftTitle+i*cellWidth, offsetV+cellHeight);
		 	
		 	//colors
		 	g.setColor(new Color(255, 0, 0));
		 	g.fillRect(offsetH+maxTitlesSize+shiftTitle+1, offsetV+1, cellWidth-1, cellHeight-1);
		 	g.fillRect(offsetH+maxTitlesSize+shiftTitle+1+7*cellWidth, offsetV+1, cellWidth-1, cellHeight-1);
		 	g.setColor(new Color(255, 128, 0));
		 	g.fillRect(offsetH+maxTitlesSize+shiftTitle+1+cellWidth, offsetV+1, cellWidth-1, cellHeight-1);
		 	g.fillRect(offsetH+maxTitlesSize+shiftTitle+1+6*cellWidth, offsetV+1, cellWidth-1, cellHeight-1);
		 	g.setColor(new Color(255, 255, 34));
		 	g.fillRect(offsetH+maxTitlesSize+shiftTitle+1+2*cellWidth, offsetV+1, cellWidth-1, cellHeight-1);
		 	g.fillRect(offsetH+maxTitlesSize+shiftTitle+1+5*cellWidth, offsetV+1, cellWidth-1, cellHeight-1);
		 	g.setColor(new Color(0, 198, 0));
		 	g.fillRect(offsetH+maxTitlesSize+shiftTitle+1+3*cellWidth, offsetV+1, cellWidth-1, cellHeight-1);
		 	g.fillRect(offsetH+maxTitlesSize+shiftTitle+1+4*cellWidth, offsetV+1, cellWidth-1, cellHeight-1);
		 	
		 	
		 	 //places the values
			 if(parameters != null && meansRef != null && sdsRef != null && wsRef != null)
			 {
			 	double y = parameters[b]-meansRef[b];
				
			 	g.setColor(Color.black);
			 	
			 	if(y >= -3*sdsRef[b]  && y <= 3*sdsRef[b])
			 	{
			 		if(sdsRef[b] != 0.0)
			 			g.drawLine(offsetH+maxTitlesSize+shiftTitle+4*cellWidth+(int)((y/sdsRef[b])*cellWidth), offsetV-1, offsetH+maxTitlesSize+shiftTitle+4*cellWidth+(int)((y/sdsRef[b])*cellWidth), offsetV+cellHeight+1);
			 		else
			 			g.drawLine(offsetH+maxTitlesSize+shiftTitle+4*cellWidth, offsetV-1, offsetH+maxTitlesSize+shiftTitle+4*cellWidth, offsetV+cellHeight+1);
			 	}
			 	else if(y > 3*sdsRef[b]  && y <= 10*sdsRef[b])
			 	{
			 		if(sdsRef[b] != 0.0)
			 			g.drawLine(offsetH+maxTitlesSize+shiftTitle+7*cellWidth+(int)(((y-3*sdsRef[b])/(7*sdsRef[b]))*cellWidth), offsetV-1, offsetH+maxTitlesSize+shiftTitle+7*cellWidth+(int)(((y-3*sdsRef[b])/(7*sdsRef[b]))*cellWidth), offsetV+cellHeight+1);
			 		else
			 			g.drawLine(offsetH+maxTitlesSize+shiftTitle+7*cellWidth, offsetV-1, offsetH+maxTitlesSize+shiftTitle+7*cellWidth, offsetV+cellHeight+1);
			 	}
			 	else if(y < -3*sdsRef[b]  && y >= -10*sdsRef[b])
			 	{
			 		if(sdsRef[b] != 0.0)
			 			g.drawLine(offsetH+maxTitlesSize+shiftTitle+cellWidth+(int)(((y+3*sdsRef[b])/(7*sdsRef[b]))*cellWidth), offsetV-1, offsetH+maxTitlesSize+shiftTitle+cellWidth+(int)(((y+3*sdsRef[b])/(7*sdsRef[b]))*cellWidth), offsetV+cellHeight+1);
			 		else
			 			g.drawLine(offsetH+maxTitlesSize+shiftTitle+cellWidth, offsetV-1, offsetH+maxTitlesSize+shiftTitle+cellWidth, offsetV+cellHeight+1);
			 	}
			 	else if(y > 10*sdsRef[b])
		 			g.drawLine(offsetH+maxTitlesSize+shiftTitle+8*cellWidth, offsetV-1, offsetH+maxTitlesSize+shiftTitle+8*cellWidth, offsetV+cellHeight+1);
			 	else if (y < -10*sdsRef[b])
			 		g.drawLine(offsetH+maxTitlesSize+shiftTitle, offsetV-1, offsetH+maxTitlesSize+shiftTitle, offsetV+cellHeight+1);
			  	else
			  		error = true;
			  		//Utils.message("Case impossible to treat : "+parameters[b]);
			 }
			 else
			 {
			 	System.out.println("Error with parameters, meansRef or sdsRef : empty");
			 }
		 }
	//temporary put in comment
	//	 if(error == true)
	//	 	Utils.message("Impossible to treat the parameters!\nFormat may not be correct.");
	 }
	 
	 /**
	  *Calculate and draw the similarity index
	  */
	  
	 private void drawSimIndex(Graphics g)
	 {
	 	//width and height of the panel
	 	int w = this.getSize().width;
	 	int h = this.getSize().height;
		
		int botOffset = 5;//(h-10-(nbBarsPerColumn*cellHeight))/(nbBarsPerColumn);
		
		g.setColor(Color.gray);
		
		if(parameters == null || sdsRef == null || meansRef == null || parameters.length!=sdsRef.length || parameters.length!=meansRef.length)
		{
			g.drawString("Similarity index - S.I. : n/a",w/2, h-botOffset);
			return;
		}
		
		g.drawString("Similarity index - S.I. : "+Utils.getDecs(si, 4, false),w/2, h-botOffset);
		
		//draw the colored rectangle
		//g.drawRect(w-2*cellWidth+2, h-cellHeight-3, cellWidth, cellHeight);
		g.drawRect(w-2*cellWidth+2, h-botOffset-cellHeight, cellWidth, cellHeight);
		
	/*	if(si >= 0.75)
			g.setColor(new Color(0, 198, 0));
		else if(si >= 0.5 && si < 0.75)
			g.setColor(new Color(255, 255, 34));
		else if(si >= 0.25 && si < 0.5)
			g.setColor(new Color(255, 128, 0));
		else if(si < 0.25)
			g.setColor(new Color(255, 0, 0));
		*/
		
		if(si < 0.4)
			g.setColor(new Color(0, 0, 0));
		else if(si >= 0.8)
			g.setColor(new Color(0, 198, 0));
		else
			g.setColor(new Color(0, (int)(si*198/0.8), 0));
		
	/*	if(si >= 0.9)
			g.setColor(new Color(0, 170, 0));
		else if(si >= 0.8 && si < 0.9)
			g.setColor(new Color(0, 232, 0));
		else if(si >= 0.7 && si < 0.8)
			g.setColor(new Color(255, 255, 34));
		else if(si >= 0.6 && si < 0.7)
			g.setColor(new Color(255, 128, 0));
		else if(si < 0.6)
			g.setColor(new Color(255, 0, 0));*/
		
		g.fillRect(w-2*cellWidth+3, h-botOffset-cellHeight+1, cellWidth-1, cellHeight-1);
	 }
	 
	/**
	 *Draws the parameters(called by the paint method)
	 */
	 private void drawParameters(Graphics g)
	 {
	 /*	int w = this.getSize().width;
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
 	
 		for(int i = 0; i < 8; i++)
		{
			g.setColor(Color.black);
			
			String currentTitle = Utils.getDecs(parameters[i],4, false);
			
			g.drawString(parametersTitles[i], startTab+i*cellWidth+24-Utils.stdWidth(parametersTitles[i])/2, 3*cellHeight-3);
			//if(i != 7)
			if(i!=3 && i!=7)
				g.drawLine(startTab+(i+1)*cellWidth,  2*cellHeight, startTab+(i+1)*cellWidth, 4*cellHeight);
			else
				g.drawLine(startTab+(i+1)*cellWidth,  cellHeight, startTab+(i+1)*cellWidth, 4*cellHeight);
			
			g.setColor(Color.gray);
			g.drawString(Utils.getDecs(parameters[i],4, false), startTab+i*cellWidth+24-Utils.stdWidth(currentTitle)/2, 4*cellHeight-3);
		}
		
		for(int i = 8; i < parameters.length; i++)
		{
			g.setColor(Color.black);
			
			String currentTitle = Utils.getDecs(parameters[i],4, false);
			
			g.drawString(parametersTitles[i], startTab+(i-8)*cellWidth+24-Utils.stdWidth(parametersTitles[i])/2, 7*cellHeight-3);
			if(i!= 9 && i!=12 && i!=16 && i != parameters.length-1)
				g.drawLine(startTab+(i-8+1)*cellWidth, 6*cellHeight, startTab+(i-8+1)*cellWidth, 8*cellHeight);
			else
				g.drawLine(startTab+(i-8+1)*cellWidth, 5*cellHeight, startTab+(i-8+1)*cellWidth, 8*cellHeight);
			
			g.setColor(Color.gray);
			g.drawString(Utils.getDecs(parameters[i],4, false), startTab+(i-8)*cellWidth+24-Utils.stdWidth(currentTitle)/2, 8*cellHeight-3);
		}*/
	}
	
	/**
	 *Sets the bounds of all elements of the Panel
	 */
	
	public void setBounds(int x, int y, int w, int h)
	{
		super.setBounds(x,y,w,h);
	}
	
	/**
	 *Set the means and variances as reference : used to place the points on the parameters bars
	 *@param means the reference parameters means
	 *@param var the reference parameters variances
	 */
	
	public int setReferences(String[] means, String[] sds, String[] weights)
	{
		System.out.println("in setReferences(..)\n");
		if(means == null || sds == null || weights == null || means.length != sds.length || means.length != weights.length){
			System.out.println("setReferences(..) : invalid argument\n");
			return -1;
		}
		
		
		meansRef = new double[means.length];
		sdsRef = new double[sds.length];
		wsRef = new double[weights.length];
		
		for(int i = 0; i < means.length; i++)
		{
			meansRef[i] = Double.parseDouble(means[i]);
			sdsRef[i] = Double.parseDouble(sds[i]);
			wsRef[i] = Double.parseDouble(weights[i]);
		}
		return 1;
	}
	
	/**
	 *Calculate the similarity index value
	 */
	
	public void calculateSI()
	{
		//calculate the mean of the parameters of the standard deviation  (deviation of the parameter values compared to the reference means)
		//and of the reference standard deviation
		double sdMean = 0;
		double refSdMean = 0;
		for(int i = 0; i < parameters.length; i++)
		{
			try
			{
				if(sdsRef[i] != 0)
					//sdMean = sdMean + (Math.abs(parameters[i]-meansRef[i])*wsRef[i]);
					//sdMean = sdMean + (Math.abs((parameters[i]-meansRef[i])/sdsRef[i]))*wsRef[i];
					//sdMean = sdMean + (Math.abs((parameters[i]-meansRef[i])/sdsRef[i]));
					
					// SIMILARITY INDEX
					
					// LAST USED CALCULATION >>>>
					//sdMean = sdMean + (Math.abs(parameters[i]-meansRef[i])*wsRef[i]/sdsRef[i]);
					// <<<

					// NEW CALCULATION >>>>
					/**
					 * weighted mean of "probabilities" (similar to likelihood)
					 * the gaussian function may always reach 1.0
					 */
					sdMean = sdMean + wsRef[i]*Math.exp( -SQR(parameters[i]-meansRef[i]) / (2.0*SQR(sdsRef[i])  ) );
					// <<<<
					
					
					
					//	refSdMean = refSdMean + Math.abs(sdsRef[i]*wsRef[i]);
				
			//	System.out.println("PARAMETERS : "+i+" - "+parameters[i]+"\tSDMEAN : "+sdMean+"\tWS : "+wsRef[i]+"\tREFSDMEAN : "+refSdMean);
			}
			catch(Exception e0)
			{
				e0.printStackTrace();
				System.out.println("ERROR");
			}
		}
		
		//sdMean = sdMean/parameters.length;
		//refSdMean = refSdMean/parameters.length;
		
		//the similarity index is calculated as a value comprised between 0 sigma and 5 sigma (mean of the standard deviation)
		//double si = 1-sdMean/4;///(4*refSdMean);
		//double si = 1-sdMean/(4*refSdMean);
		
		
		// LAST USED CALCULATION >>>>
		/* 
		double si = 1-sdMean/5;
		if(si > 1)
			si = 1;
		else if (si < 0)
			si = 0;
		*/
		// <<<<<
		
		
		// NEW CALCULATION >>>>>
		System.out.println("uses new method");
		this.si = sdMean;
		// <<<<<
	}
	
	/**
	 *Get the length of the longuest string of the table
	 *@param tabStr the table that contains the string
	 *@return the length of the string, -1 if the tabStr is null
	 */
	
	public static int getMaxLength(String[] tabStr)
	{
		if(tabStr == null)
			return -1;
			
		int i = 0;
		int len = 0;
		while(i < tabStr.length)
		{
			if(tabStr[i] == null)
			{
				i++;
				break;
			}
			int tmpLen = Utils.stdWidth(tabStr[i]);
			if(tmpLen > len)
				len = tmpLen;
			i++;
		}
		return len;
	}
}
