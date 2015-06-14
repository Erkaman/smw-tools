package decoder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

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
		
		
		//decompressAllScripts();
		
		Log.i("done");	
	}
}

//size of the table is 0x0000812E

