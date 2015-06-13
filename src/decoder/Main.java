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

	private static void decompressAllScripts() throws IOException {
		final List<String> scripts = Util.getAllFiles("Data/");
		for(final String script : scripts) {

			if(!script.endsWith(".unc")) {

				Log.i("decompressing " + script);

				try {

					decompressFile(script, script + ".unc");
				}catch(ZipException ex) {
					Log.i("Could not decompress " + script);
				}

			}

		}
	}


	private static void decompressFile(final String inFile, final String outFile) throws IOException {
		final InputStream in =
				new InflaterInputStream(new FileInputStream(inFile));

		final OutputStream out = new FileOutputStream(outFile);

		byte[] buffer = new byte[1024];
		int len;
		while((len = in.read(buffer)) > 0) {
			out.write(buffer, 0, len);
		}

		in.close();
		out.close();
	}

	public static void main(String [ ] args) throws IOException, NoSuchAlgorithmException{
		
		//Archive imgDat = new Archive();
		
		//Log.i(imgDat.toString());
		
		//Archive.unpack();
		
	//	Archive.pack();
		
	//	imgDat.dumpDat("Img.dat");
		//imgDat.dumpAllFiles();
		
		
		decompressAllScripts();
		
		Log.i("done");	
	}
}

//size of the table is 0x0000812E

