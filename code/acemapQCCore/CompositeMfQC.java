package acemapQCCore;

import aceStyle.*;
import acemapCore.*;

import java.lang.String;
import java.io.*;
import java.util.*;
import java.text.*;
import java.util.zip.*;

import javax.swing.JFileChooser;


/**
 *Class that handles zip-based operations for the special ma0 file format cf AbstractCompositeMf - this class permit to specify the temps file)
 *@author Guillaume Brysbaert
 *@version 0.58
 */

public class CompositeMfQC extends AbstractCompositeMf
{
	/**
	 *Default constructor.
	 */
	 
	public	CompositeMfQC(){}
	
	/**
	 *Constructor that receives a File object. The file is not loaded.
	 */
	
	public	CompositeMfQC(File f)
	{
		z_file = f;
	}

	 
	public File getTemp()
	{
		return QCUtils.getTemp();
	}
}
