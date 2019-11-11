package maModel;

//import acemap.*;
import acemapMath.*;
import aceStyle.*;
import acemapCore.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;


/**
 *Helper class that reacts on window closing event.
 */
public class Closer implements WindowListener{
	Closer(){}
	public void 	windowActivated(WindowEvent e){}
	public void 	windowClosed(WindowEvent e){
//		System.out.println("schotten dicht aber!");
	}
	public void 	windowDeactivated(WindowEvent e){}
	public void 	windowDeiconified(WindowEvent e){}
	public void 	windowIconified(WindowEvent e){}
	public void 	windowOpened(WindowEvent e){} 
	public void 	windowClosing(WindowEvent e){
	//	System.out.println("schotten dicht!");
		AceUtil.cleanExit();
		((JFrame)(e.getSource())).dispose();
		System.exit(0);
	}
}
