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
 *Density graph (in-)panel
 *@author Sebastian Noth, Guillaume Brysbaert
 *@version 0.58
 */

public class InPan extends JPanel
{
	public  MTable		mTab;
	double 	bins[][];
	int		blen;
	
	FontMetrics fm;
		
	double	lo, hi;
	
	public int lSig = -1000;
	public int lVar = -1000;
	
	//maxima
	double minlS = 0.0;
	double maxlS = 0.0;
	double minlV = 0.0;
	double maxlV = 0.0;
	
	InPan(double bins[][]){
		super(null);
	
		this.bins = bins;
		blen = bins.length;
		
		fm = this.getFontMetrics(this.getFont());
	}

	private double getValue(int x, int y, int w, int h){
		int    imin = (x*blen)/w;
		int    jmin = (y*blen)/h;
		
		return bins[ imin ][ jmin ];
	}
	
	public void setRange(double lo, double hi){
		this.lo = lo;
		this.hi = hi;
		
		Graphics g = getGraphics();
		if(g!=null) paintComponent(g);
	}
	
	
	public void paintComponent(Graphics g)
	{
		//get the name to place the cross at the right place in case of switching between two or more frames
	/*	try
		{
			ProbeCard tmpCard = (ProbeCard)(this.getParent().getParent().getParent().getParent().getParent().getParent());
			System.out.println("PParent title : "+tmpCard.getTitle());
		}
		catch(Exception e)
		{
			System.out.println("Tile problem "+e.toString());
		}*/
		
		int h = getSize().height;
		int w = getSize().width;
		
		//double fsig = (double)w  / 12.0;
		//double fvar = (double)h / 16.0;
						
		double v;	
									
		for(int i=0; i<w; i++){
			for(int j=0; j<h; j++){
				
				v = getValue(i,j,w,h);
				v = (v-lo) / (hi-lo);
				if(v<0.0) v=0.0;
				if(v>1.0) v=1.0;
				
				g.setColor(Utils.getColorFor(2.0f*(float)v-1.0f));
				g.drawLine(i, h-j-1, i, h-j-1);
			}
		}
		
		g.setColor(Color.white);
		g.drawLine(0, h/2, w, h/2 );
		g.drawLine(w/2, 0, w/2, h);
		g.drawString( Utils.getDecs(lo*1000, 3, false)+" to "+Utils.getDecs(hi*1000, 3, false), 20, 20 );
		
		//DEB - g.setColor(Color.yellow);
		
	/*	System.out.println("INP width : "+this.getSize().width+"\tINP height : "+this.getSize().height);
		
		for(int i=0; i<mTab.size; i++)
		{
			double s = Math.log(mTab.data[i].signal);
			double var = Math.log(mTab.data[i].moq);
			System.out.println("S : "+mTab.data[i].signal+"\tvar : "+mTab.data[i].moq);
			int valT[] = Plot.locate(-6.0, 6.0, -4.0, 4.0, 160, mTab.data[i].signal, mTab.data[i].moq, this.getSize().width, this.getSize().height);
			System.out.println("valT[0] : "+valT[0]+"\tvalT[1] : "+valT[1]);
			g.drawString("X", valT[0]-fm.charWidth('X')/2, valT[1]+fm.getAscent()/2);
			//g.drawString(".", valT[0], valT[1]);
		}*/
		
		g.setColor(Color.white);
		
		//draw the max values of the axes, if they are different from 0
		if(minlS != 0 && maxlS != 0 && minlV != 0 && maxlV != 0)
		{
			g.drawString(Utils.getDecs(minlS, 1, false), 5, h/2+15);
			g.drawString(Utils.getDecs(maxlS, 1, false), w-20, h/2+15);
			g.drawString(Utils.getDecs(minlV, 1, false), w/2+5, h-5);
			g.drawString(Utils.getDecs(maxlV, 1, false), w/2+5, 15);
		}
		
		//draw the cross if it needs to be : the middle of the cross is the right point
		
		if(lSig != -1000 && lVar != -1000)
			g.drawString("X", lSig-fm.charWidth('X')/2, lVar+fm.getAscent()/2);
	}
	
	public void drawCross(int lSig, int lVar)
	{
	//	System.out.println("LSIG : "+lSig+"\tLVAR : "+lVar);
		Graphics g = getGraphics();
		g.setColor(Color.white);
		if(g!=null)
			g.drawString("X", lSig, lVar);
	}
	
	/**
	 *Set the extreme values, for axes
	 */
	
	public void setExtremes(double minlS, double maxlS, double minlV, double maxlV)
	{
		this.minlS = minlS;
		this.maxlS = maxlS;
		this.minlV = minlV;
		this.maxlV = maxlV;
	}
}
