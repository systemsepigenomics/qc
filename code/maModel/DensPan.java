package maModel;

//import acemap.*;
//import acemapMath.*;
import aceStyle.*;
import acemapCore.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;


/** 
 *Extended JPanel object used for density Plots (Density3DMa) as content pane
 *@author Sebastian Noth, Guillaume Brysbaert
 *@version 0.58
 */
public class DensPan extends JPanel implements SliderDListener{

	public InPan 		inp;
	AceSliderD sld;
	double 		max;

	public DensPan(double bins[][]){
		super(null);
		
		max = 0.0;
		
		for(int i=0; i<bins.length; i++)
			for(int j=0; j<bins[i].length; j++)
				if(bins[i][j]>max)
					max = bins[i][j];
		/*	lo = 0;
		hi = max;
		
		for(int i=0; i<bins.length; i++)
			for(int j=0; j<bins[i].length; j++)
				if(bins[i][j]>max*0.35)
				{
					max = bins[i][j];
					System.out.println("I : "+i);
					System.out.println("J : "+j);
				}*/
	
		sld = new AceSliderD(true, true, 0.0f, 1.0f);
		sld.addSliderDListener(this);
		double tempDouble = 0.00035/max;
	//	System.out.println("TEMPDouble : "+tempDouble);
	//	System.out.println("TEMPFloat : "+(float)tempDouble);
		//sld.setPositions(0.0f, (float)(tempDouble));
		if(tempDouble > 0.1)
			sld.setPositions(0.0f, (float)(tempDouble));
		else
			sld.setPositions(0.0f, 0.1f);
		
//			System.out.println("MAX0 : "+max);
		inp = new InPan(bins);
//			System.out.println("MAX1 : "+max);
		add(sld);
		add(inp);
		
		if(sld.knobsReleased())
			inp.setRange(sld.getLeftValue()*max, sld.getRightValue()*max  );
		//	inp.setRange(0, 0.35/1000);
	}
	
	/**
	 *Set the signal value for the in-panel (abscisse of the cross)
	 *@param s log value of the signal
	 */
	 
	public void setInLSig(int s)
	{
		inp.lSig = s;
	}
	
	/**
	 *Set the signal value for the in-panel (abscisse of the cross)
	 *@param v log value of the variance
	 */
	 
	public void setInLVar(int v)
	{
		inp.lVar = v;
	}
	
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x,y,w,h);
		
		inp.setBounds(0, 0,    w, h-20);
		sld.setBounds(5, h-20, w-10, 20);
	
	}
		
	
	public void sliderDChanged(Object source){
		if(sld.knobsReleased())
			inp.setRange(sld.getLeftValue()*max, sld.getRightValue()*max  );
	}
	
	/**
	 *Draw a cross at the location of the point that corresponds to the log-signal 
	 *and the log-variance
	 */
	
	public void drawCross(int lSignal, int lVariance)
	{
		inp.lSig = lSignal;
		inp.lVar = lVariance;
		inp.drawCross(lSignal, lVariance);
	}
}