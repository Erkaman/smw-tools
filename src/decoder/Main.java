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
	
	private static void readLogo() throws IOException {
		String s =  "../../Downloads/Super Marisa World/Img.dat";

		Decoder decoder = new Decoder(0x3EDB9C30);

		RandomAccessFile inputStream = new RandomAccessFile(s, "r");

		OutputStream outputStream = new BufferedOutputStream(new FileOutputStream("out.png"));
		
		// char.png - 01cb6a9e
		
		long offset = 0x01CC87CA;
		inputStream.seek(offset);
	
		for(int i = 0; i <23472; ++i) { // 0x5BB0

			final byte b = inputStream.readByte();	

			final byte decoded = decoder.decode(b);
			
			outputStream.write(decoded);

		}
		
		outputStream.close();
		inputStream.close();		
	}
	
	public static void main(String [ ] args) throws IOException, NoSuchAlgorithmException{
		readLogo();
		
		//Archive imgDat = new Archive();
		
		//Log.i(imgDat.toString());
		
		Archive.unpack();
		
	//	imgDat.dumpDat("Img.dat");
		//imgDat.dumpAllFiles();
		
		Log.i("done");	
	}
}

//size of the table is 0x0000812E

