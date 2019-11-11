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
 *Displays probability over two parameters (signal and variance) in a scaleable 
 *window.
 *
 *Opens a file opening dialog. The routine should only be used to display data
 *of ma0 and ma1 files, since inherent logaritmic transformation would lead to 
 *problems with ma2 (and higher) files that contain negative signal values.
 */
public class Density3DMa{
	Density3DMa(){}
	
	
	public static void main(String args[]){
		AceUtil.disableLoadLU();
		AceUtil.init(null);
		
		
		Utils.setFCDir(new File("C:\\seppl\\data\\modeling"));
		File f = Utils.getUserFile("select ma0 file", false);
		if(f==null) return;
		
		CompositeMf cmf = new CompositeMf();
		cmf  = new CompositeMf();
		cmf.load(f, true, false, false);
		MTable mTab = new MTable(cmf.F_ma, null, false, cmf, true);
		
		System.out.println("next: bins");		
		double bins[][] = Utils.filterGauss( Plot.sample(mTab, -4.0, 4.0, -4.0, 4.0, 160), 9);
		System.out.println("after bins");		
			
		DensPan pan = new DensPan(bins);
		
		JFrame frame = new JFrame();
		frame.addWindowListener(new Closer());
		frame.setContentPane(pan);
		frame.setResizable(true);
		frame.setTitle(mTab.name);
				
		frame.setSize(170, 170+40);
		frame.show();
	}
}
