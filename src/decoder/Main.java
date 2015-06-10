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

	private static final String IMG_DAT =  "../../Downloads/Super Marisa World/Img.dat";
	
	private static void readLogo() throws IOException {
		String s = IMG_DAT;

		Decoder decoder = new Decoder(0x3EDB9C30);

		RandomAccessFile inputStream = new RandomAccessFile(s, "r");

		OutputStream outputStream = new BufferedOutputStream(new FileOutputStream("out.png"));
		
		long offset = 0x01CC87CA;
		inputStream.seek(offset);

		for(int i = 0; i <23472; ++i) {

			final char b = (char)inputStream.read();	

			final char decoded = decoder.decode(b);
			
			outputStream.write(decoded);

		}
		
		outputStream.close();
		inputStream.close();		
	}
	
	private static int readInt(final RandomAccessFile inputStream) throws IOException {
		final byte b1 = inputStream.readByte();
		final byte b2 = inputStream.readByte();
		final byte b3 = inputStream.readByte();
		final byte b4 = inputStream.readByte();
			
		return ((b4&0xFF) << 24) | ((b3&0xFF) << 16) | ((b2&0xFF) << 8) | (b1&0xFF);
	}
	
	private static void readFileTable() throws IOException {
		String s = IMG_DAT;
		RandomAccessFile inputStream = new RandomAccessFile(s, "r");

		OutputStream outputStream = new BufferedOutputStream(new FileOutputStream("_filetable.dat"));
		
		final int SEED = readInt(inputStream);
		
		Decoder decoder = new Decoder(SEED);

		for(int i = 0; i <SEED; ++i) {

			final char b = (char)inputStream.read();	

			final char decoded = decoder.decode(b);
			
			outputStream.write(decoded);
//42 4b  b7 63
		}
		
		outputStream.close();
		inputStream.close();		
	}
	
	public static void main(String [ ] args) throws IOException{
	//	readLogo();
		
		readFileTable();
		
	}
}


//size of the table is 0x0000812E

