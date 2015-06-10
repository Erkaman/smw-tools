package decoder;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class Main {
	
	private static String toHex(final int val) {
		return String.format("%02X", val);
	}
	
	private static void readLogo() throws IOException {
		String s =  "../../Downloads/Super Marisa World/Img.dat";

		Decoder decoder = new Decoder(0x3EDB9C30);

		RandomAccessFile inputStream = new RandomAccessFile(s, "r");

		OutputStream outputStream = new BufferedOutputStream(new FileOutputStream("out.png"));
		
		long offset = 0x01CC87CA;
		inputStream.seek(offset);

		for(int i = 0; i <23472; ++i) {

			final byte b = inputStream.readByte();	

			final byte decoded = decoder.decode(b);
			
			outputStream.write(decoded);

		}
		
		outputStream.close();
		inputStream.close();		
	}
	
	
	

	
	public static void main(String [ ] args) throws IOException{
		readLogo();
		
		ImgDat imgDat = new ImgDat();
		
		Log.i(imgDat.toString());
			
		String s = "ウィキペディアは誰でも編集できるフリー百科事典です";
		Log.i("s: "+ s);
		
	}
}


//size of the table is 0x0000812E
