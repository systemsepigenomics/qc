package acemapQC;

import aceStyle.*;
import acemapCore.*;
import java.io.*;

/////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////// ABSTRACT SECTION //////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////
/**
 *Abstract base class for objects that provide import functionanlity for vendor-
 *provided micro array data (raw) files.
 *@author Sebastian Noth
 *@version 0.58
 */
public abstract class TableFile{

	byte[]            buffer;
	

	int[]             starts;
	int[]             lengths;

	String[]          column_names;
	String[]          assay_names;

	FileInputStream   fis;
	FileOutputStream  fos;

	int               columns;
	int               assays;

	int               geneID_col;
	int               signal_col;
	
	//int               sdev_col;
	//int               sn_col;
	
	int               flags_col;
	int               cv_col;

	boolean           ready_read;
	boolean           ready_write;
	boolean           f_end;
	boolean           line_has_data;
	boolean           all_data_read;

	static int        BUFFER_SIZE = 90000;
	int               row_start;
	int               data_remain;
	int               cursor;
	boolean           lengthtwo;

	int               row_index;

	int               filling;


	public TableFile(){
		ready_read  = false;
		ready_write = false;
		buffer = new byte[BUFFER_SIZE];
		row_index =0;
	}

	public TableFile(File f_in){
		this();
		setReadFile(f_in);
	}
	
	public boolean reinitialize(File f_in)
	{
		cursor = 0;
		f_end = false;
		try
		{
			fis = new FileInputStream(f_in);
			return true;
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
			return false;
		}
	}

	/**
	 *Read beginning at "init" index
	 *@param init the initial line where to start reading
	 */
	
	public boolean readRow(int init)
	{
		cursor = init;
		return readRow();
	}

	public boolean readRow(){
		//DEBSystem.out.println("readRow()");
		
		if(!ready_read || f_end)
		{
			System.out.println("PB READING 1!!! : ready_read : "+ready_read+"\tf_end : "+f_end);
			return false;
		}

		int cnt, len=0, col=0;
		int ret, data_keep;
		
		row_start = cursor;

		starts[0] = row_start;

		
		while(true){
			if(cursor == filling){
				
				for(int a=0; a<=col; a++)
					starts[a] = starts[a]-row_start;

				data_keep = cursor-row_start;
				
				for(int a=0; a<data_keep; a++)
					buffer[a] = buffer[a + row_start];

				filling = data_keep;

				try{
					ret = fis.read(buffer, data_keep, BUFFER_SIZE-data_keep);
				}catch(Exception e){
					e.printStackTrace();
					System.out.println(e.toString());
					System.out.println("file read error in data refresh in TableFile");
					System.out.println("data remain: "+data_remain);
					return false;
				}
				
				if(ret == -1){
					//DEBSystem.out.println("file end detected via -1 ret value");
					try{
						fis.close();
					}catch(Exception e){
						System.out.println(e.toString());
					}
					f_end = true;
					
					// in case new line read contains only ret characters
					if((cursor - row_start) <3)
						line_has_data = false;
					else
						line_has_data = true;

					return true;
				}
				filling += ret;
				cursor -= row_start;
				row_start = 0;
			}

			// TAB found: set length of current column length counter to len
			if( buffer[cursor] == 9){
				
				if(col == (columns-1)){
					lengths[col] = len;
					System.out.println("too many tabs: "+col+" , "+columns+" columns, row "+row_index);
				}
				else{
					lengths[col] = len;
					len=-1;
					col++;  // increment current column
					starts[col]=cursor+1;  // set start of new column
				}
			}

			else{
				if( buffer[cursor] == 10 || buffer[cursor] == 13){
					if(col==0 && len==0){
						//System.out.println("found acsii "+buffer[cursor]+" at beginning of row");
						starts[0] = starts[0]+1;
						cursor++;
						continue;
					}
					
					// line end detected
					lengths[col]=len;
					cursor++;  // !!!!!!!!!!!!!!!!!!!!!
					break;
				}
			}
				
			cursor++;
			len++;
		}

		row_index++;
		return true;
	}


	public abstract boolean writeRow();
	public abstract boolean writeToFile(File f_out, AceProgress progr);
	public abstract boolean setReadFile(File f_in);

}

/////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////// APPLIED SECTION //////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////
/**
 *Used to process Applied Biosystem export files. Provides functionality to retrieve
 *a list of contained assays. Enables export of a single, chosen assay.
 *@author Sebastian Noth
 *@version 0.58
 */
class TF_applied extends TableFile{

	final byte[]      nullgene = {78, 85, 76, 76, 45, 71, 69, 78, 69};

	int 	flag_border;
		
	TF_applied(int flag_border){
		this.flag_border = flag_border;
	}
	
	int		probe_col;
	boolean	atEnd, atBeg;

	private boolean isNumber(byte[] buf, int start, int length){
		for(int i=0; i<length; i++)
			if(buf[start+i] < (byte)'0' || buf[start+i] > (byte)'9')
				return false;
				
		return true;
	}
	private boolean	isNullGene(byte[] buf, int start, int length){
		if(length != 9)
			return false;

		for(int a=0; a<4; a++){
			if(buf[start+a] != nullgene[a])
				return false;
		}
		for(int a=5; a<9; a++){
			if(buf[start+a] != nullgene[a])
				return false;
		}
		return true;
	}
	private boolean outOfFlagRange(byte[] buf, int start, int length){
		
		int r = Utils.parseFastI(buf, start, length) ; 
		
		if( r<0 || r>flag_border )
			return true;
		else
			return false;
	}
	public  boolean writeRow(){
		if(!ready_write)
			return false;
					
		try{
			fos.write(buffer, starts[probe_col], lengths[probe_col]);
			fos.write(9);
					
			fos.write(buffer, starts[signal_col], lengths[signal_col]);
			fos.write(9);
			
			fos.write(buffer, starts[cv_col], lengths[cv_col]);
			//fos.write(9);
			//fos.write(buffer, starts[flags_col], lengths[flags_col]);
			fos.write(13);
			fos.write(10);
			
			//DEBSystem.out.println(new String(buffer, starts[geneID_col], lengths[geneID_col]));
			
			
		}catch(Exception e){
			System.out.println("Exception in writeRow : "+e.toString());
			return false;
		}
		return true;
	}
	public  boolean writeToFile(File f_out, AceProgress progr)
	{
		if(false == ready_write){
			System.out.println("write ready false");
			return false;
		}

		try{
			fos = new FileOutputStream(f_out);
		}catch(Exception e){
			System.out.println(e.toString());
			return false;
		}
		
		if(progr != null)
			progr.setPart(0.0);
		//DEBSystem.out.println("writing to file "+f_out);
		
		byte temp[];
		//byte t2[];
		
		try{
			temp = (new String("PROBE")).getBytes("US-ASCII");
			fos.write(temp);
			fos.write(9);
			
			temp = (new String("NORMALIZED_SIGNAL")).getBytes("US-ASCII");
			fos.write(temp);
			fos.write(9);
			
			temp = (new String("CV")).getBytes("US-ASCII");
			fos.write(temp);
			fos.write(13);
			fos.write(10);
		}
		catch(Exception e)
		{
			System.out.println("PB WRITTING!!");
			return false;
		}

		if(!isNullGene(         	buffer, starts[geneID_col], lengths[geneID_col])){
			if(isNumber(			buffer, starts[probe_col ], lengths[probe_col ])){ 
				if(!outOfFlagRange( buffer, starts[flags_col], lengths[flags_col])){
					if(!writeRow())
					{
						System.out.println("PB WRITTING 2!!");
						return false;
					}
				}
			}
		}
		
		//DEBint ovf = 0;
		
		while(true){
			if(!readRow())
			{
				System.out.println("PB WRITTING 3!!");
				return false;
			}
			
			// detecting end
			if(f_end){
				if(line_has_data){
					if(!isNullGene(         buffer, starts[geneID_col], lengths[geneID_col])){ 
						if(!outOfFlagRange( buffer, starts[flags_col], lengths[flags_col])){
							if(!writeRow())
							{
								System.out.println("PB WRITTING 4!!");
								return false;
							}
						}
					}
				}
				try{
					fos.flush();
					fos.close();
				}catch(Exception e){
					//DEBSystem.out.println("problem!! "+e.toString());
				}
				
				if(progr != null)
					progr.setPart(0.0);
								
				return true;
			}
			
			// loop through all rows
			if(!isNullGene(         	buffer, starts[geneID_col], lengths[geneID_col])){ 
				if(isNumber(			buffer, starts[probe_col ], lengths[probe_col ])){ 
					if(!outOfFlagRange( buffer, starts[flags_col], lengths[flags_col])){
						if(!writeRow())
						{
							System.out.println("PB WRITTING 5!!");
							return false;
						}
					}
				}
				else{
					//DEBSystem.out.println("detected too high flag : "+Utils.parseFastI(buffer, starts[flags_col], lengths[flags_col])
					//DEB+"string : "+new String(buffer, starts[flags_col], lengths[flags_col])   );	
					//DEBovf++;	
				}
			}
			
			//DEBSystem.out.println("over flag : "+ovf);

			if(row_index%400 == 0 && progr != null)
				progr.setPart( ((double)row_index)/40000.0 );
		}
		
		
		
	}


	//////////////////////////////////////////////////////
	//////////////////////////////////////////////////////

	public  boolean	setReadFile(File f_in){
		if(ready_read){
			ready_read  = false;
			ready_write = false;
			buffer = new byte[20000];
			try{
				fis.close();
			}catch(Exception e){
				System.out.println("in setReadFile()[1]: "+e.toString());
			}
		}

		f_end = false;
		
		geneID_col	= -1;
		signal_col	= -1;
		
		probe_col	= -1;
		
		flags_col	= -1;
		cv_col		= -1;
				
		int i;
		try{
			fis = new FileInputStream(f_in);
		}catch(Exception e){
			System.out.println("in setReadFile()[2]: "+e.toString());
			return false;
		}

		int len, curs = 0;
		columns  = 1;
		try{
			do{
				fis.read(buffer, curs, 1);
				if( buffer[curs] == 9)
					columns++;

				if(curs > 5000){
					System.out.println("wrong format");
					fis.close();
					return false;
				}

				curs++;
			}while(buffer[curs-1] != 10 && buffer[curs-1]!=13);
		}catch(Exception e){
			return false;
		}

		if(columns < 5 || columns > 500){
			System.out.println("wrong format");
			try{
				fis.close();
			}catch(Exception e){
				System.out.println("in setReadFile()[3]: "+e.toString());
			}
			return false;
		}
				
		len = curs;	
		column_names = new String[columns];
		starts       = new int[columns];
		lengths      = new int[columns];

		int start,stop=0;
		assays=0;
		
		atEnd = false;
		atBeg = false;
		
		
			
		
		for(i=0; i<columns; i++){
			start=stop;
			while(buffer[stop] != 9 && buffer[stop] != 10 & buffer[stop] != 13)
				stop++;
			try{
				column_names[i] = new String(buffer, start, stop-start, "US-ASCII");
			}catch(Exception e){
				System.out.println("in setReadFile()[4]: "+e.toString());
				return false;
			}	
											
			if(column_names[i].compareToIgnoreCase("PROBE") == 0 || column_names[i].compareToIgnoreCase("PROBE_ID") == 0){
				probe_col	= i;	//*DEB*/System.out.println("PROBE_ID found in column "+i);
			}
			
			if(column_names[i].compareToIgnoreCase("GENE") == 0 || column_names[i].compareToIgnoreCase("GENE_ID") == 0){
				geneID_col	= i;	//*DEB*/System.out.println("GENE_ID found in column "+i);
			}
			
		
			if(column_names[i].toUpperCase().endsWith("ASSAY_NORMALIZED_SIGNAL")){
				atEnd = true;
				assays++;
			}
			if(column_names[i].toUpperCase().startsWith("ASSAY_NORMALIZED_SIGNAL")){
				atBeg = true;
				assays++;
			}
			
			stop++;
		}
				
		if(atBeg==atEnd){
			Utils.error("not supported headline format");
			return false;
		}
		
		System.out.println("assay name at "+(atBeg?"start":"end"));
		
		
		if(assays == 0){
			try{
				fis.close();
			}catch(Exception e){
				System.out.println("in setReadFile()[5]: "+e.toString());
			}
			System.out.println("no assay found");
			
			return false;
		}
		
		if(	probe_col	== -1 ||
			geneID_col	== -1 )
		{
			Utils.error("column missing");
			return false;
		}
		
		assay_names = new String[assays];
		
		start=0;
		
		if(atEnd){
			for(i=0; i<assays; i++){
				while(!(column_names[start].toUpperCase().endsWith("ASSAY_NORMALIZED_SIGNAL")))
					start++;
				assay_names[i] = column_names[start].substring(0, column_names[start].length()-24);
				start++;
			}
		}
		else{
			for(i=0; i<assays; i++){
				while(!(column_names[start].toUpperCase().startsWith("ASSAY_NORMALIZED_SIGNAL")))
					start++;
				assay_names[i] = column_names[start].substring(24); // column_names[start].length()-24);
				start++;
			}
		}
		
		ready_read = true;
		

		cursor      = 0;
		filling     = 0;
		
		if( !readRow()){
			System.out.println(" read row failure");	
			return false;
		}
		
		return true;
	} 
	
	public String[] getAssayNames(){
		String cop[] = new String[assay_names.length];
		for(int i=0; i<cop.length; i++)
			cop[i] = new String(assay_names[i]);
		
		return cop;
	}
	public int		getNumberOfAssays(){
		return assays;
	}
	public boolean	setAssay(int index){
		if(index < 0 || index >= assays)
			return false;

		signal_col     = -1;
		
		flags_col      = -1;
		cv_col         = -1;

		if(assays==1)
			assay_names[0]=new String("");


		if(atEnd){
			for(int i=0; i<columns; i++){
				if( column_names[i].startsWith(assay_names[index])){
					
					if(column_names[i].toUpperCase().endsWith("ASSAY_NORMALIZED_SIGNAL")){
						if(signal_col != -1){
							System.out.println("signal column key not unique");
							return false;
						}
						signal_col = i;
					}
					////////////////////////////////////////////////////////////////////
					if(column_names[i].endsWith("FLAGS")){
						if(flags_col != -1){
							System.out.println("flags column key not unique");
							return false;
						}
						flags_col = i;
					}
									
					if(column_names[i].endsWith("CV")){
						if(cv_col != -1){
							System.out.println("cv column key not unique");
							return false;
						}
						cv_col = i;
					}
				}
			}
		}
		else{  // at start
			for(int i=0; i<columns; i++){
				if( column_names[i].endsWith(assay_names[index])){
					
					if(column_names[i].toUpperCase().startsWith("ASSAY_NORMALIZED_SIGNAL")){
						if(signal_col != -1){
							System.out.println("signal column key not unique");
							return false;
						}
						signal_col = i;
					}
					////////////////////////////////////////////////////////////////////
					if(column_names[i].toUpperCase().startsWith("FLAGS")){
						if(flags_col != -1){
							System.out.println("flags column key not unique");
							return false;
						}
						flags_col = i;
					}
									
					if(column_names[i].toUpperCase().startsWith("CV")){
						if(cv_col != -1){
							System.out.println("cv column key not unique");
							return false;
						}
						cv_col = i;
					}
				}
			}
		}
		
		System.out.println("probe col="+probe_col+", signal col="+signal_col+", var col="+cv_col);
		
		if(signal_col == -1 || cv_col == -1 || flags_col == -1){
			System.out.println("column keys missing");
			return false;
		}
		ready_write = true;
		System.out.println("fine: "+ready_write);
		
		return true;
	}
}


/////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// AFFYMETRIX SECTION //////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////
/**
 *Used to process Affymetrix export files.
 *@author Sebastian Noth
 *@version 0.58
 */ 
class TF_affy extends TableFile{

	public	boolean writeRow(){
		if(false == ready_write)
			return false;

		try{
			fos.write(buffer, starts[geneID_col], lengths[geneID_col]);
			fos.write(9);
			fos.write(buffer, starts[signal_col], lengths[signal_col]);
			fos.write(9);
			fos.write(buffer, starts[cv_col], lengths[cv_col]);
			fos.write(13);  // windows style return
			fos.write(10);
		}catch(Exception e){
			System.out.println(e.toString());
			return false;
		}
		return true;
	}
	public	boolean writeToFile(File f_out, AceProgress progr)
	{
		if(false == ready_write){
			System.out.println("write ready false");
			return false;
		}

		try
		{
			fos = new FileOutputStream(f_out);
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
			return false;

		}

		progr.setPart(0.0);

		

				
		byte temp[];
		//byte t2[];		
		try{
			temp = (new String("PROBE")).getBytes("US-ASCII");
			fos.write(temp);
			fos.write(9);
			
			temp = (new String("SIGNAL")).getBytes("US-ASCII");
			fos.write(temp);
			fos.write(9);
			
			temp = (new String("P_VALUE")).getBytes("US-ASCII");
			fos.write(temp);
			fos.write(13);
			fos.write(10);

		}catch(Exception e){
			return false;
		}
		
		if(!writeRow())
			return false;
		
		while(true){
			if(!readRow())
				return false;

			if(f_end){
				if(line_has_data){
					if(!writeRow())
							return false;
				}
				try{
					fos.flush();
					fos.close();
				}catch(Exception e){
					
				}
				progr.setPart(0.0);
				return true;
			}
			if(!writeRow())
				return false;

			if(row_index%300 == 0)
				progr.setPart( ((double)row_index)/30000.0 );
		}
	}
	
	public	boolean setReadFile(File f_in){
		if(ready_read){
			ready_read  = false;
			ready_write = false;
			buffer = new byte[20000];
			try{
				fis.close();
			}catch(Exception e){
			}
		}

		f_end = false;
		
		//sdev_col       = -1;
		//sn_col         = -1;
		//flags_col      = -1;
		geneID_col = -1;
		signal_col     = -1;
		cv_col         = -1;	


		int i;
		try{
			fis = new FileInputStream(f_in);
		}catch(Exception e){
			System.out.println(e.toString());
			return false;
		}

		int len, curs = 0;
		columns  = 1;
		try{
			do{
				fis.read(buffer, curs, 1);
				if( buffer[curs] == 9)
					columns++;

				if(curs > 5000){
					System.out.println("wrong format");
					fis.close();
					return false;
				}

				curs++;
			}while(buffer[curs-1] != 10 && buffer[curs-1]!=13);
		}catch(Exception e){
			return false;
		}

		if(columns < 3 || columns > 500){
			System.out.println("wrong format");
			try{
				fis.close();
			}catch(Exception e){
				System.out.println(e.toString());
			}
			return false;
		}
				
		len = curs;	
		column_names = new String[columns];
		starts       = new int[columns];
		lengths      = new int[columns];

		int start,stop=0;
		assays=0;

		// detection of column identifiers
		for(i=0; i<columns; i++){
			start=stop;
			while(buffer[stop] != 9 && buffer[stop] != 10 && buffer[stop] != 13)
				stop++;
			try{
				column_names[i] = new String(buffer, start, stop-start, "US-ASCII");
			}catch(Exception e){
				return false;
			}	
			
			if(i==0){ 
				if(column_names[i].compareTo("") != 0){
					System.out.println("first column does not match empty string : "+column_names[i]);
					return false;
				}
				geneID_col = i;
			}
		
			if(column_names[i].endsWith("_Signal")){
				if(signal_col != -1){
					System.out.println("more than one column with suffix matching \"_Signal\" found at index "+i);
					return false;
				} 
				signal_col = i;
			}

			if(column_names[i].endsWith("_Detection p-value")){
				if(cv_col != -1){
					System.out.println("more than one column with suffix matching \"_Detection p-value\" found at index "+i);
					return false;
				} 
				cv_col = i;
			}
			stop++;
		}

		if(signal_col == -1 || cv_col == -1 || geneID_col == -1){
			System.out.println("column(s) not found");
			return false;
		}

		System.out.println("gene id found at column "+geneID_col);
		System.out.println("signal  found at column "+signal_col);
		System.out.println("MOQ     found at column "+cv_col);
		
		// merely used to control all steps beeing passed successful in applied section
		ready_read  = true;
		ready_write = true;

		cursor      = 0;
		filling     = 0;
		
		if( !readRow())
			return false;

		
		return true;
	} 
}
