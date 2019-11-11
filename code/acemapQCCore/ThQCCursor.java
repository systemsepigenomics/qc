package acemapQCCore;

import acemapCore.*;

import aceStyle.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.String;
import java.util.*;
import java.util.regex.*;
import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.zip.*;
import java.io.*;
import java.lang.Math;
import maModel.*;



/**
 *Permit to change the cursor during a treatment.
 *@author Guillaume Brysbaert
 *@version 0.58
 */
 
public class ThQCCursor implements Runnable
{
	String imgPath;
	Thread aThread;
	boolean execute;
	JPanel curJP;
	boolean wait;
	
	public boolean refresh = true;
	
	public ThQCCursor(String imgPath, JPanel curJP)
	{
		this.imgPath = imgPath;
		this.curJP = curJP;
		this.wait = false;
		this.execute = false;
	}
	
	public ThQCCursor(JPanel curJP)
	{
		this.curJP = curJP;
		this.wait = true;
		this.execute = false;
	}
	
	 /**
      *Run implementation of the thread
      */
     
     public void run()
     {
    	int imgNb = 0;
    	Image img;
    	
        try
        {
           	while(execute == true)
        	{
        		Toolkit tk = Toolkit.getDefaultToolkit();
	            if(Thread.currentThread().getName().equals("waitCur"))
	            {
	            	if(wait == true)
	            	{
	            		img = tk.getImage(QCUtils.imagePath+File.separator+"kin"+imgNb+".gif");
						imgNb++;
		              	if(imgNb == 5)
		              		imgNb = 0;
		              /*
		               //refresh is too big
		                else if(imgNb == 1)
		              	{
		              		//refresh the view
		              		if(refresh == true)
		              			QCUtils.currentFrame.paint(QCUtils.currentFrame.getGraphics());
		              	}*/
	            	}
		            else
		               	img = tk.getImage(imgPath);
		           	Cursor myCursor = tk.createCustomCursor(img, new Point(16, 16), "cursor");
					if(QCUtils.currentFrame!=null)
    					QCUtils.currentFrame.setCursor(myCursor);
					curJP.setCursor(myCursor);
					if(curJP.getParent()!=null)
    					curJP.getParent().setCursor(myCursor);
	              	Thread.sleep(300);
		        } 
            }
        }
        catch (InterruptedException exception)
        {
            exception.printStackTrace();
        }
    }
    
    /**
     *Change the cursor (one time)
     */
    
    public void initCursor()
    {
    	Image img;
		try
		{
			Toolkit tk = Toolkit.getDefaultToolkit();
			if(wait == false)
				img = tk.getImage(imgPath);
	   		else
	   			img = tk.getImage(QCUtils.imagePath+File.separator+"kin0.gif");
    		Cursor myCursor = tk.createCustomCursor(img, new Point(16, 16), "wait");
    		if(QCUtils.currentFrame!=null)
    			QCUtils.currentFrame.setCursor(myCursor);
    		curJP.setCursor(myCursor);
    		if(curJP.getParent()!=null)
    			curJP.getParent().setCursor(myCursor);
    	}
    	catch(Exception e)
    	{
    		System.out.println("PB with the CURSOR");
    	}
    }
    
    /**
     *Launch the thread to change the cursor
     */
    
    public void launchThread()
    {
    	execute = true;
      	
    	try
		{
			aThread = new Thread(this);
			aThread.setName("waitCur");
			aThread.start();
			Thread.sleep(50);
		}
		catch(Exception ext)
		{ 
			System.out.println("Error when launching Thread for cursor : "+ext.toString());
		}
	}
	
 	/**
     *Wait for the end of the thread
     */	
     
     public boolean joinThread()
    {
    	execute = false;
    	
		try
		{
			aThread.join();
			if(QCUtils.currentFrame!=null)
				QCUtils.currentFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			curJP.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			if(curJP.getParent()!=null)
				curJP.getParent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			
			refresh = true;
			return true;
		}
		catch(Exception e)
		{
			System.out.println("Impossible to stop the thread...");
			if(QCUtils.currentFrame!=null)
				QCUtils.currentFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			curJP.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			if(curJP.getParent()!=null)
    			curJP.getParent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			return false;
		}
	}
}
