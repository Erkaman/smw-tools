package decoder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Decoder for ImgDat File. 
 * @author eric
 *
 */
public class ImgDat {
	
	private final static Charset SHIFT_JIS_CHARSET;
	
	private final static int FILE_TABLE_HASH_SIZE = 128;
	
	private int SEED;
	
	static {
		 SHIFT_JIS_CHARSET = Charset.availableCharsets().get("Shift_JIS");
	}

	private static final String IMG_DAT = 
			//"../../Downloads/Super Marisa World/Data.dat";; 
			//"../../Downloads/Super Marisa World/Sound.dat";; 
			//"../../Downloads/Super Marisa World/Music.dat";; 
			
			"../../Downloads/Super Marisa World/Img.dat";
	
	private RandomAccessFile inputStream;
	
	private List<FileTableEntry> fileTable;
	
	public ImgDat() throws IOException, NoSuchAlgorithmException {
		
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
	
	private static int toInt(final byte b1, final byte b2, final byte b3, final byte b4) {
		
		return ((b4&0xFF) << 24) | ((b3&0xFF) << 16) | ((b2&0xFF) << 8) | (b1&0xFF);
	}
	
	private static byte[] toBytes(final int i) {
		
		byte[] bytes = new byte[4];
		
		bytes[3] =(byte)((i >> 24) & 0xFF);
		bytes[2] = (byte)((i >> 16) & 0xFF);
		bytes[1] = (byte)((i >> 8) & 0xFF);
		bytes[0] = (byte)((i) & 0xFF);
		
		return bytes;
	}
	
	private static String hashToHexString(byte[] hashBytes) {
		StringBuffer sb = new StringBuffer();
        for (int i = 0; i < hashBytes.length; i++) {
          sb.append(Integer.toString((hashBytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
	}
	
	
	String computeHash(byte[] buffer, int offset, int len) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		
		 md.update( buffer, offset, len);
		 byte[] computedHashBytes =md.digest();
		 
		 return hashToHexString(computedHashBytes);
		
	}

	
	private void readFileTable() throws IOException, NoSuchAlgorithmException {	
		SEED = readInt(inputStream);
		byte fileTableData[] = new byte[SEED-4];
		
		Decoder decoder = new Decoder(SEED);

		for(int i = 0; i <(SEED-4); ++i) { // inclusive size. 

			final byte b = inputStream.readByte();

			final byte decoded = decoder.decode(b);
			
			fileTableData[i] = decoded;
		}
		
		int dataI = 0;
		
		
		byte[] fileTableHash = new byte[FILE_TABLE_HASH_SIZE];
		
		this.fileTable = new ArrayList<FileTableEntry>();
		
		for(; dataI < 128; ++dataI ) {
			fileTableHash[dataI] += fileTableData[dataI];
		}
		
		String hash = new String(fileTableHash);
		
		int fileTableSize = fileTableData.length - FILE_TABLE_HASH_SIZE;
				
		 String computedHash = computeHash(fileTableData, FILE_TABLE_HASH_SIZE, fileTableSize);
		 
		 if(!computedHash.equals(new String(fileTableHash))) {
			 Log.i("wrong hash: " + computedHash);
			 System.exit(1);
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
			
			entry.offset = toInt(fileTableData[dataI++],fileTableData[dataI++],fileTableData[dataI++],fileTableData[dataI++]);
			
			entry.magic = fileTableData[dataI++];
			
			entry.seed = toInt(fileTableData[dataI++],fileTableData[dataI++],fileTableData[dataI++],fileTableData[dataI++]);
			
			fileTable.add(entry);
		}
	}
	
	private class FileTableEntry {
		
		public byte[] filename;
		public int size1; // we should probably use this one. 
		public int size2; // we should probably ignore this one.
		public int offset;
		public byte magic;
		public int seed;
		
		public String getFilename() {
			return new String(filename, SHIFT_JIS_CHARSET);
		}
		
		public void writeToStream(final OutputStream outputStream) throws IOException {
	
			outputStream.write(filename);
			outputStream.write( '\0'); // terminate the string with null.
			
			outputStream.write(toBytes(size1));
			outputStream.write(toBytes(size2));
			outputStream.write(toBytes(offset));
			outputStream.write(magic);
			outputStream.write(toBytes(seed));
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
	
	public String toString() {
		
		String str = "";
		
		//str += this.fileTableHash + "\n";
			
		for (FileTableEntry s : fileTable)
		{
		    str += s + "\n";
		}
		
		return str;
		
	}
	
	public void dumpAllFiles() throws IOException {
		
		for(FileTableEntry entry : fileTable) {
			
			Log.i("dumping " + entry.getFilename());

			Decoder decoder = new Decoder(entry.seed);

			File file = new File(entry.getFilename());
			file.getParentFile().mkdirs();
			
			OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(entry.getFilename()));
			
			this.inputStream.seek(SEED + entry.offset);
			
			for(int i = 0; i <entry.size1; ++i) { // 0x5BB0

				final byte b = inputStream.readByte();	

				final byte decoded = decoder.decode(b);
				
				outputStream.write(decoded);
			}
			
			outputStream.close();
			
			//break;
		}
		
	}
	
	public void dumpDat(final String filename) throws IOException, NoSuchAlgorithmException {
		OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filename));
		
		
		Decoder decoder = new Decoder(SEED);
		
		outputStream.write(toBytes(SEED));
		
		ByteArrayOutputStream fileTableOutputStream = new ByteArrayOutputStream();
		
		for(FileTableEntry entry : fileTable) {
			entry.writeToStream(fileTableOutputStream);			
		}
		
		byte[] fileTableBytes = fileTableOutputStream.toByteArray();
		
		String hashCode = computeHash(fileTableBytes, 0, fileTableBytes.length);
		
			
		ByteArrayOutputStream entireFileTableOutputStream = new ByteArrayOutputStream();
		
		for(char ch : hashCode.toCharArray()) {
			entireFileTableOutputStream.write(ch);
		}
		entireFileTableOutputStream.write(fileTableBytes);
		
		byte[] out = entireFileTableOutputStream.toByteArray();
		
		for(byte b : out) { // inclusive size. 
			final byte encoded = decoder.decode(b);
			
			outputStream.write(encoded	);
		}
		
		for(FileTableEntry entry : fileTable) {
			
			final int seed = entry.seed;
			
			decoder = new Decoder(seed);
			
			Log.i("encoding file: " + entry.getFilename());
			
			final InputStream inputStream = new BufferedInputStream(new FileInputStream(entry.getFilename()));

			while(true) {
				int i = inputStream.read();
				
				if(i == -1)
					break;
				
				byte b = (byte)i;
				
				final byte encoded = decoder.decode(b);
				
				outputStream.write(encoded);
			
			}
			inputStream.close();			
		}

		outputStream.close();
	}
}
