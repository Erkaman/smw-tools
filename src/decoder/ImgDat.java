package decoder;

import java.io.BufferedOutputStream;
import java.io.File;
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
		
		return toInt(b1,b2,b3,b4);
	}
	
	private static int toInt(final byte b1, final byte b2, final byte b3, final byte b4) throws IOException {
		
		return ((b4&0xFF) << 24) | ((b3&0xFF) << 16) | ((b2&0xFF) << 8) | (b1&0xFF);
	}
	
	private void readFileTable() throws IOException {	
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
			
			++dataI; // skip the null terminator.
			
			
			entry.size1 = toInt(fileTableData[dataI++],fileTableData[dataI++],fileTableData[dataI++],fileTableData[dataI++]);
			entry.size2 = toInt(fileTableData[dataI++],fileTableData[dataI++],fileTableData[dataI++],fileTableData[dataI++]);
			
			dataI += 5;
			
			/*
			if(entry.size1 != entry.size2) {
				Log.i("WTF: " + entry.getFilename());		
			}*/
			
			entry.seed = toInt(fileTableData[dataI++],fileTableData[dataI++],fileTableData[dataI++],fileTableData[dataI++]);
			
			fileTable.add(entry);
		}
		
	}
	
	private class FileTableEntry {
		
		public byte[] filename;
		public int size1; // we should probably use this one. 
		public int size2; // we should probably ignore this one.
		
		public int seed;
		
		public String getFilename() {
			return new String(filename, SHIFT_JIS_CHARSET);
		}
		
		public String toString() {
			
			return 
					"{filename:" + getFilename() +
					", size1: " + size1 + 
					", size2: " + size2 +  
					", seed: " + Integer.toHexString(seed) +  
					
					"}";
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
	
	public void dumpAllFiles() throws IOException {
		
		for(FileTableEntry entry : fileTable) {

			Decoder decoder = new Decoder(entry.seed);

			File file = new File(entry.getFilename());
			file.getParentFile().mkdirs();
			
			OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(entry.getFilename()));
			
		//	long offset = 0x01CC87CA;
			//inputStream.seek(offset);
		
			for(int i = 0; i <entry.size1; ++i) { // 0x5BB0

				final byte b = inputStream.readByte();	

				final byte decoded = decoder.decode(b);
				
				outputStream.write(decoded);

			}
			outputStream.close();
			
			break;
		}
		
	}
	
	
}
