package acemapQCStyle;

import java.awt.*;
import java.awt.event.*;
import java.lang.String;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.lang.Math;
import java.util.*;

/**
 *Frame object that splits the content pane (sizes are selectable)
 *into 6 separately accessible content panes : 1 up, 3 under, 1 under and 1 bottom
 *@author Guillaume Brysbaert
 *@version 0.58
 */
 
public class SplitFrame1311 extends JFrame
{
	private int HSplit1;
	private int HSplit2;
	private int HSplit3;

	private int VSplit1;
	private int VSplit2;

	private Container topPane  = null;
	private Container leftPane = null;
	private Container centerPane = null;
	private Container rightPane = null;
	private Container middlePane = null;
	private Container bottomPane = null;

	/**
	 *Create a splitted frame
	 *@param s name of the frame
	 */

	public SplitFrame1311(String s)
	{
		super(s);
		getContentPane().setLayout(null);
		HSplit1 = 60;
		HSplit2 = 170;
		HSplit3 = 600;
		VSplit1 = 200;
		VSplit2 = 400;
	}
	
	/**
	 *Create a splitted frame
	 *@param s name of the frame
	 *@param width width of the frame
	 *@param height height of the frame
	 */
	
	public SplitFrame1311(String s, int width, int height)
	{
		super(s);
		getContentPane().setLayout(null);
		
		setSize(width, height);
		
		HSplit1 = 60;
		HSplit2 = 170;
		HSplit3 = height-120;
		VSplit1 = width*2/5;
		VSplit2 = width*3/5;
	}
	
	public void setHSplit1(int HSplitEdge1)
	{
		this.HSplit1 = HSplitEdge1;
	}
	
	public void setHSplit2(int HSplitEdge2)
	{
		this.HSplit2 = HSplitEdge2;
	}
	
	public void setHSplit3(int HSplitEdge3)
	{
		this.HSplit3 = HSplitEdge3;
	}
	
	public void setVSplit1(int VSplitEdge1)
	{
		this.VSplit1 = VSplitEdge1;
	}
	
	public void setVSplit2(int VSplitEdge2)
	{
		this.VSplit2 = VSplitEdge2;
	}
	
/*	public int getWidth()
	{
		Dimension d = getContentPane().getSize();
		return d.width;		
	}
	
	public int getHeight()
	{
		Dimension d = getContentPane().getSize();
		return d.height;		
	}*/
	
	public void adjustPanels()
	{
		Dimension d = getContentPane().getSize();
		int w = d.width;
		int h = d.height;
		
		VSplit1 = w*2/5;
		VSplit2 = w*3/5;
		HSplit3 = h-120;
		
		if(topPane != null)
			topPane.setBounds(0, 0, w, HSplit1);
		
		if(leftPane != null)
			//leftPane.setSize(        splitEdge, h);
			leftPane.setBounds(0, HSplit1, VSplit1, HSplit2-HSplit1);
		
		if(centerPane != null)
			centerPane.setBounds(VSplit1, HSplit1, VSplit2-VSplit1, HSplit2-HSplit1);
	
		if(rightPane != null)
			//rightPane.setSize(                w-splitEdge, h);
			rightPane.setBounds(VSplit2, HSplit1, w-VSplit2, HSplit2-HSplit1);
			
		if(middlePane != null)
			middlePane.setBounds(0, HSplit2, w, HSplit3-HSplit2);
				
		if(bottomPane != null)
			bottomPane.setBounds(0, HSplit3, w, h-HSplit3);
	}
	
	public void setTopPane(Container tNew)
	{
		if(topPane != null)
			getContentPane().remove(topPane);
		
		topPane = tNew;
		int w = getContentPane().getSize().width;
		
		topPane.setBounds(0, 0, w, HSplit1);
		
		getContentPane().add(topPane);
	}
	
	public void setLeftPane(Container lNew)
	{
		if(leftPane != null)
			getContentPane().remove(leftPane);
		
		leftPane = lNew;
		
		leftPane.setBounds(0, HSplit1, VSplit1, HSplit2-HSplit1);
		
		getContentPane().add(leftPane);
	}
	
	public void setCenterPane(Container cNew)
	{
		if(centerPane != null)
			getContentPane().remove(centerPane);
		
		centerPane = cNew;
		
		centerPane.setBounds(VSplit1, HSplit1, VSplit2-VSplit1, HSplit2-HSplit1);
		
		getContentPane().add(centerPane);
	}
	
	public void setRightPane(Container rNew)
	{
		if(rightPane != null)
			getContentPane().remove(rightPane);
		
		rightPane = rNew;
		
		int w = getContentPane().getSize().width;
		
		rightPane.setBounds(VSplit2, HSplit1, w-VSplit2, HSplit2-HSplit1);
		
		getContentPane().add(rightPane);
	}
	
	
	public void setMiddlePane(Container mNew)
	{
		if(middlePane != null)
			getContentPane().remove(middlePane);
	
		middlePane = mNew;
		
		Dimension d = getContentPane().getSize();
		int w = d.width;
		int h = d.width;
		
		middlePane.setBounds(0, HSplit2, w, HSplit3-HSplit2);
		
		getContentPane().add(middlePane);
	}
	
	public void setBottomPane(Container bNew)
	{
		if(bottomPane != null)
			getContentPane().remove(bottomPane);
		
		bottomPane = bNew;
		
		bNew.setBackground(Color.white);
		
		Dimension d = getContentPane().getSize();
		int w = d.width;
		int h = d.width;
		
		bottomPane.setBounds(0, HSplit3, w, h-HSplit3);
		
		getContentPane().add(bottomPane);
	}
	
	public Container getTopPane()
	{
		if(topPane != null)
			return topPane;
		return null;
	}
	
	public Container getLeftPane()
	{
		if(leftPane != null)
			return leftPane;
		return null;
	}
	
	public Container getCenterPane()
	{
		if(centerPane != null)
			return centerPane;
		return null;
	}
	
	public Container getRightPane()
	{
		if(rightPane != null)
			return rightPane;
		return null;
	}
	
	public Container getBottomPane()
	{
		if(bottomPane != null)
			return bottomPane;
		return null;
	}
	
	public Container getMiddlePane()
	{
		if(middlePane != null)
			return middlePane;
		return null;
	}
	
/*	public void paint()
	{
		paintAll(this.getGraphics());
	}*/
}
