package decoder;


public class Decoder {

	private int codeTable[];
	private int codeTableI;
	final int SEED;
	
	public Decoder(final int SEED) {this.SEED = SEED; }

	private char computeXorKey() {
		
		if(codeTable == null) {
			codeTable = computeInitialCodeTable(SEED);
			codeTableI = codeTable.length;
		}
		
		if(codeTableI == codeTable.length) {
			computeNextTable(codeTable);
			codeTableI = 0;
		}
		
		int ecx, eax, edx;

		ecx = codeTable[codeTableI++];
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

	public char decode(final char b) {
		char AL = computeXorKey();
		return (char) (b ^ AL);
	}

	private static int[] computeInitialCodeTable(final int SEED) {

		int[] initial = new int[0x270];

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
	
	private static char getCL(final int ecx) {
		return (char)(ecx & 0x000000FF);
	}
	
	/**
	 * Returns the new value of ecx. 
	 * @param ecx
	 * @param cl
	 * @return
	 */
	private static int setCL(final int ecx, final char cl) {
		return (cl  & 0x000000FF) | (ecx & 0xFFFFFF00);
	}

	private static void computeNextTable(int[] arr) {

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

}
