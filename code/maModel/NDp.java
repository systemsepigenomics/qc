package maModel;

//import acemap.*;
import acemapMath.*;
import java.io.*;
import aceStyle.*;
import java.util.*;
import javax.swing.JFileChooser;
import acemapCore.*;

/**
 *Normal distribution parameter storage object
 *@author Sebastian Noth
 *@version 0.73
 */
class NDp{
	double f;
	double m;
	double s;
	
	NDp(){
		f = 0.0;
		m = 0.0;
		s = 0.0;	
	}
	NDp(NDp ndp){
		f = ndp.f;
		m = ndp.m;
		s = ndp.s;	
	}
	NDp(double f, double m, double s){
		this.f=f;
		this.m=m;
		this.s=s;
	}
	
	void div(double w){
		if(w==0.0) return;
		f/=w;
		m/=w;
		s/=w;	
	}
}
	