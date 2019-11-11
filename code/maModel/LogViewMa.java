package maModel;

//import acemap.*;
import acemapMath.*;
import acemapCore.*;
import aceStyle.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;


class DrawPan extends JPanel{
	double sig[], var[];
			
	DrawPan(double sig[], double var[]){
		super(null);
		
		this.sig = sig;
		this.var = var;
		
	}
	
	public void paintComponent(Graphics g){
		int h = getSize().height;
		int w = getSize().width;
		
		g.setColor(new Color(255, 255, 255));
		g.fillRect(0,0,w,h);
		
		double fsig = (double)w  / 12.0;
		double fvar = (double)h / 16.0;
				
		g.setColor(new Color(255, 0, 0));
		g.drawLine(0, h/2, w, h/2 );
		g.drawLine(w/3, 0, w/3, h);

		g.setColor(Color.black);			
									
		for(int i=0; i<sig.length; i++)
			g.drawLine((int)(sig[i]*fsig), (int)(var[i]*fvar), (int)(sig[i]*fsig), (int)(var[i]*fvar));
				
		
	}
}

public class LogViewMa{
	LogViewMa(){}
	
	
	public static void main(String args[]){
		AceUtil.init(null);
		
		if(args!=null && args.length>0)
			System.out.println("---"+args[0]); //+" : "+args[1]);
		
		
		File f = Utils.getUserFile("select ma0 file", false);
		if(f==null) return;
		
		CompositeMf cmf = new CompositeMf();
		cmf  = new CompositeMf();
		cmf.load(f, true, false, false);
		MTable mTab = new MTable(cmf.F_ma, null, false, cmf, true);
		
		int valid=0;
		for(int i=0; i<mTab.size; i++)
			if(	mTab.data[i].signal> 0.02f  && 
				mTab.data[i].signal< 2900.0f && 
				mTab.data[i].moq   > 0.001f  && 
				mTab.data[i].moq   < 2900.0f    ) valid++; 
				
		System.out.println("valid "+valid);	
				
		double sig[] = new double[valid];
		double var[] = new double[valid];
		
		int cnt =0;
		for(int i=0; i<mTab.size; i++){
			if(	mTab.data[i].signal> 0.02f  && 
				mTab.data[i].signal< 2900.0f && 
				mTab.data[i].moq   > 0.001f  && 
				mTab.data[i].moq   < 2900.0f    ){
					 sig[cnt] = Math.log(mTab.data[i].signal ) + 4.0;
					 var[cnt] = 8.0 - Math.log(mTab.data[i].moq    );
					 cnt++; 
			}
		}
		
		DrawPan pan = new DrawPan(sig, var);
		
		JFrame frame = new JFrame();
		frame.setContentPane(pan);
		frame.setResizable(true);
		frame.setTitle(mTab.name);
		
		
		frame.setSize(450, 300+20);
		frame.show();
		
		
		
	}
}
