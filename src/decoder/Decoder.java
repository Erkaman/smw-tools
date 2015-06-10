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

	public int[] computeInitial() {

		int[] initial = new int[0x270];

		final int SEED = 0x3EDB9C30;

		initial[0] = SEED;

		// start loop here. 

		for(int eax = 1; eax < 0x270; eax+=7) {
			int esi = initial[eax-1];

			int edi = esi;
			edi >>>= 0x1E;

		edi ^= esi;	
		edi *= 0x6C078965;

		esi = edi+eax;
		edi = esi;
		edi >>>= 0x1E;
		edi ^= esi;
		edi *= 0x6C078965;

		initial[eax+0] = esi;


		esi = edi+eax+1;		
		edi = esi;		 
		edi >>>= 0x1E;
		edi ^= esi;		
		edi *= 0x6C078965;
		initial[eax+1] = esi; // third value
		//	write esi

		esi = edi+eax+2;		
		edi = esi;		
		edi >>>= 0x1E;		
		edi ^= esi;		
		edi *= 0x6C078965;
		initial[eax+2] = esi; // fourth value.

		esi = edi+eax+3;
		edi = esi;
		edi >>>= 0x1E;
		edi ^= esi;
		edi *= 0x6C078965;
		initial[eax+3] = esi; //;  write! fifth value

		esi = edi+eax+4;
		edi = esi;
		edi >>>= 0x1E;
		edi ^= esi;
		edi *= 0x6C078965;
		initial[eax+4] = esi; 	//	    ;  write! sixth value

		esi = edi+eax+5;
		edi = esi;
		edi >>>= 0x1E;
		edi ^= esi;
		edi *= 0x6C078965;
		initial[eax+5] = esi; //  ;  write! seventh value


		esi = edi+eax+6;
		initial[eax+6] = esi; // write eight value. 

		}

		return initial;

	}
	
	char getCL(final int ecx) {
		return (char)(ecx & 0x000000FF);
	}
	
	/**
	 * Returns the new value of ecx. 
	 * @param ecx
	 * @param cl
	 * @return
	 */
	int setCL(final int ecx, final char cl) {
		return (cl  & 0x000000FF) | (ecx & 0xFFFFFF00);
	}

	void computeNextTable(int[] arr) {

		char cl,cf;
		int eax, ecx,edi, ebx, edx;

		for(eax = 0, edx = 0; edx < 0x0E3; ++eax, ++edx) {

			ecx = arr[eax+1];
			edi = arr[eax+0];
			ebx = arr[eax+0];
			edi ^= ecx;
			edi &= 0x7FFFFFFE;
			edi ^= ebx;

			ecx &= 0xFFFFFF01; // AND CL,1
			edi >>>= 1;
			
			cl = getCL(ecx);
			cf = (char)(cl == 0 ? 0 : 1);
			ecx = setCL(ecx, (char)(-cl)); // NEG CL
			ecx = ecx - (ecx+cl); // SBB ECX, ECX
						
			 ecx &= 0x9908B0DF;
			 
			 edi ^= ecx;
			 
			 edi ^= arr[eax+397];
			
			 arr[eax] = edi;
		}
		
		for(edx = 0; edx < 0x18C; ++eax, ++edx) {
			ecx = arr[eax+1];
			edi = arr[eax+0];
			ebx = arr[eax+0];
			edi ^= ecx;
			edi &= 0x7FFFFFFE;
			edi ^= ebx;
			
			// in here array.
			
			ebx =  arr[eax-227];

			
			ecx &= 0xFFFFFF01; // AND CL,1
			edi >>>= 1;
			
			cl = getCL(ecx);
			cf = (char)(cl == 0 ? 0 : 1);
			ecx = setCL(ecx, (char)(-cl)); // NEG CL
			
			ecx = ecx - (ecx+cl); // SBB ECX, ECX
						
			 ecx &= 0x9908B0DF;
			 
			 edi ^= ecx;
			edi ^= ebx;
			
			arr[eax] = edi;
		}

		ecx = arr[0];
		edx = arr[eax];
		ebx = arr[eax];

		edx ^= ecx;
		edx &= 0x7FFFFFFE;	
		edx ^= ebx;


		ecx &= 0xFFFFFF01; // AND CL,1
		edx >>>= 1;

		cl = getCL(ecx);
		cf = (char)(cl == 0 ? 0 : 1);
		ecx = setCL(ecx, (char)(-cl)); // NEG CL
		
		
		ecx = ecx - (ecx+cl); // SBB ECX, ECX


		ecx &= 0x9908B0DF;
		
		edx ^= ecx;
		
		edx ^= arr[eax-227];
		arr[eax] = edx;
		
		
	}

	public static void main(String [ ] args) throws IOException{
		String s =  "../../Downloads/Super Marisa World/Img.dat";

		Decoder decoder = new Decoder();

		int[] initial = decoder.computeInitial();

		log("first array");
		
		
		// C66EE2D6
		for(int i = 0; i < initial.length; ++i) {
			log("i: " + i + " " + toHex(initial[i]));
		}
		
		log("");
		
		decoder.computeNextTable(initial);
		
		log("second array");
		
		for(int i = 0; i < initial.length; ++i) {
			log("i: " + i + " " + toHex(initial[i]));
		}
		
		// last value should be 6A5F29BE. 
		
		System.exit(1);
		

		RandomAccessFile inputStream = new RandomAccessFile(s, "r");

		long offset = 0x01CC87CA;
		inputStream.seek(offset);


		for(int i = 0; i <4; ++i) {

			final char b = (char)inputStream.read();	

			final char decoded = decoder.decode(b);

			log(toHex(decoded) + " " + decoded);
		}
		
		

	}
	// to produce the second array we use the same technique, but the seed is instead 0x3EDB9C30
}
