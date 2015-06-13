package decoder;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class FileTableEntry {

	private final static Charset SHIFT_JIS_CHARSET;
	
	static {
		SHIFT_JIS_CHARSET = Charset.availableCharsets().get("Shift_JIS");
	}
	
	public byte[] filename; //the shift-jis encoded filename. 
	public int size1; // we should probably use this one. 
	public int size2; // we should probably ignore this one.
	public int offset;
	public byte magic;
	public int seed;

	public String getFilename() {
		return new String(filename, SHIFT_JIS_CHARSET);
	}
	
	/**
	 * Gets the size in bytes of this entry.
	 * @return
	 */
	public int getSize() {
		return
				filename.length + 1 + // plus null-terminator
				4 + // size1
				4 + // size2
				4 + //offset
				1 + // magic
				4; // seed
		
	}
	
	public void writeToStream(final OutputStream outputStream) throws IOException {

		outputStream.write(filename);
		outputStream.write( '\0'); // terminate the string with null.

		outputStream.write(Util.toBytes(size1));
		outputStream.write(Util.toBytes(size2));
		outputStream.write(Util.toBytes(offset));
		outputStream.write(magic);
		outputStream.write(Util.toBytes(seed));
	}

	public String toString() {

		return 
				"{filename:" + getFilename() +
				", size1: " + size1 + 
				", size2: " + size2 +  				
				", offset: " + Integer.toHexString(offset) +  
				", magic: " + magic +  		
				", seed: " + Integer.toHexString(seed) +  	
				"}";
	}
	


}