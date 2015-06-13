package decoder;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.security.NoSuchAlgorithmException;

public class Main {
	
	private static String toHex(final int val) {
		return String.format("%02X", val);
	}
	
	
	public static void main(String [ ] args) throws IOException, NoSuchAlgorithmException{
		
		//Archive imgDat = new Archive();
		
		//Log.i(imgDat.toString());
		
	//	Archive.unpack();
		
		Archive.pack();
		
	//	imgDat.dumpDat("Img.dat");
		//imgDat.dumpAllFiles();
		
		Log.i("done");	
	}
}

//size of the table is 0x0000812E

