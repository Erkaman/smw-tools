package decoder;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Decoder {
	
	private char xorKeys[];
	private int codeTable[];
	private int i;
	
	public Decoder() {
		xorKeys = new char[]{0xA7, 0x7A, 0x7B, 0x50};
			
		codeTable = new int[] {0x73237571, 0xE9412F29, 0x3BAB14DB, 0x6BD7D2CD};
		
		i = 0;
	}
	
	private static void log(final String str) {
		System.out.println("LOG: " + str);	
	}
	
	private static String toHex(final int val) {
		return String.format("%02X", val);
	}
	
	char computeXorKey() {
		int ecx, eax, edx;
		
		ecx = codeTable[i++];
		eax = ecx;
		eax >>>= 0x0B  ;  
						
		ecx ^= eax;
		
		edx = ecx;
		
		edx &=  0xFF3A58AD;
		
		 edx <<= 7;
		 
		 ecx ^= edx;
		 
		 eax = ecx;
		 
		 eax &= 0xFFFFDF8C;
		 
		 eax <<= 0x0F;
		 
		 ecx ^= eax;
		 
		 eax = ecx;
		
		 eax >>>= 0x12;
		 
		 eax ^= ecx;     
		 
		 return (char)(eax & 0x000000FF);
	}
	
	private char decode(final char b) {
		char AL = computeXorKey();
		
		//log("AL: " + AL);
		
		return (char) (b ^ AL);
	}
	
	public static void main(String [ ] args) throws IOException{
		String s =  "../../Downloads/Super Marisa World/Img.dat";
		
		RandomAccessFile inputStream = new RandomAccessFile(s, "r");
		
		long offset = 0x01CC87CA;
		inputStream.seek(offset);
		
		Decoder decoder = new Decoder();
		
		for(int i = 0; i <4; ++i) {

			final char b = (char)inputStream.read();	
			
			final char decoded = decoder.decode(b);
			
			log(toHex(decoded) + " " + decoded);
		}
		
	}
}
