package decoder;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Decoder for ImgDat File. 
 * @author eric
 *
 */
public class ImgDat {
	
	private final static Charset SHIFT_JIS_CHARSET;
	
	static {
		 SHIFT_JIS_CHARSET = Charset.availableCharsets().get("Shift_JIS");
	}

	private static final String IMG_DAT =  "../../Downloads/Super Marisa World/Img.dat";
	
	private RandomAccessFile inputStream;
	
	private List<FileTableEntry> fileTable;
	private String fileTableMagic;
	
	public ImgDat() throws IOException {
		
		this.inputStream = new RandomAccessFile( IMG_DAT, "r");

		readFileTable();
		
	}
	
	
	
	private static int readInt(final RandomAccessFile inputStream) throws IOException {
		final byte b1 = inputStream.readByte();
		final byte b2 = inputStream.readByte();
		final byte b3 = inputStream.readByte();
		final byte b4 = inputStream.readByte();
			
		return ((b4&0xFF) << 24) | ((b3&0xFF) << 16) | ((b2&0xFF) << 8) | (b1&0xFF);
	}
	
	private void readFileTable() throws IOException {

		//OutputStream outputStream = new BufferedOutputStream(new FileOutputStream("_filetable.dat"));
		
		final int SEED = readInt(inputStream);
		byte fileTableData[] = new byte[SEED];
		
		Decoder decoder = new Decoder(SEED);

		for(int i = 0; i <SEED; ++i) {

			final byte b = inputStream.readByte();

			final byte decoded = decoder.decode(b);
			
			fileTableData[i] = decoded;
		}
		
		int dataI = 0;
		
		this.fileTableMagic = "";
		
		this.fileTable = new ArrayList<FileTableEntry>();
		
		for(; dataI < 128; ++dataI ) {
			fileTableMagic += fileTableData[dataI];
		}
		
		while(dataI < fileTableData.length-10) {
			
			FileTableEntry entry = new FileTableEntry();
			
			// first parse the file name.
			
			int begI = dataI;
			
			for(; fileTableData[dataI] != '\0'; ++dataI) {}
			entry.filename = new byte[dataI-begI];
			for(int i = begI, j = 0; i < dataI ; ++i,++j) {
				entry.filename[j] = fileTableData[i];
			}
			
			
			dataI += 18;
			
			fileTable.add(entry);
		}
		
	}
	
	private class FileTableEntry {
		
		public byte[] filename;
		
		
		
		public String toString() {
			
			return "{filename:" + new String(filename, SHIFT_JIS_CHARSET)+  "}";
		}
		
	}
	
	public String toString() {
		
		String str = "";
		
		str += this.fileTableMagic + "\n";
			
		for (FileTableEntry s : fileTable)
		{
		    str += s + "\n";
		}
		
		return str;
		
	}
	
}
