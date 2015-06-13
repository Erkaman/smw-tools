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
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Decoder for ImgDat File. 
 * @author eric
 *
 */
public class Archive {

	private static final String IMG_DAT = 
			"../../Downloads/Super Marisa World/Data.dat";; 
			//"../../Downloads/Super Marisa World/Sound.dat";; 
			//"../../Downloads/Super Marisa World/Music.dat";; 

		//	"../../Downloads/Super Marisa World/Img.dat";

	public static void unpack() throws IOException, NoSuchAlgorithmException {	
		final RandomAccessFile inputStream = new RandomAccessFile( IMG_DAT, "r");
		final FileTable fileTable = readFileTable(inputStream);
		
		Log.i(fileTable.toString());
		
	//	dumpDat("Img.dat", fileTable);
		
	//	dumpAllFiles(inputStream, fileTable);
	}
	
	public static void pack() throws NoSuchAlgorithmException, IOException {	
		
		final String prefix = "Img\\";
		List<String> files = Util.getAllFiles(prefix);
		
		
		int beg = -1;
		
		// remove the beginning of the path
		for(int i = 0; i < files.size(); ++i) {
			String str = files.get(i);
			
			if(beg == -1) {
				beg = str.indexOf(prefix);
			}
			
			files.set(i, str.substring(beg));
		}
		Collections.sort(files);
		
		List<FileTableEntry> fileTableList = new ArrayList<FileTableEntry>();
		
		Random rng = new Random();
		
		int offset = 0;
		for(final String file : files) {
			FileTableEntry entry = new FileTableEntry();
			
			byte[] filenameBytes = file.getBytes("Shift_JIS");
			
			entry.filename = new byte[filenameBytes.length];
			int i = 0;
			for(byte b : filenameBytes) {
				entry.filename[i++] = b;
			}
			
			final int filesize = (int)new File(file).length();
			
			entry.size1 = filesize;
			entry.size2 = filesize;
			
			entry.offset = offset;
			
			if(offset == 0x1ced0f2) {
				Log.i("haro");
			}
			
			offset += filesize;
			
			entry.magic = computeMagic(entry.filename);
			entry.seed = rng.nextInt();
			
			
			fileTableList.add(entry);
		}
		
		FileTable fileTable = new FileTable(fileTableList);
		
		//Log.i(fileTable.toString());
		
		dumpDat("Img.dat", fileTable);
	}
	
	private static byte computeMagic(byte[] filename) {
		final int LEN = filename.length;
		
		return
				filename[LEN-1] == (byte)'b'  &&
				filename[LEN-2] == (byte)'d'  &&			
				filename[LEN-3] == (byte)'.'
								
			
				? (byte)0  : (byte)1;
		
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


	private static String hashToHexString(byte[] hashBytes) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < hashBytes.length; i++) {
			sb.append(Integer.toString((hashBytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}

	private static String computeHash(byte[] buffer, int offset, int len) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-512");

		md.update( buffer, offset, len);
		byte[] computedHashBytes =md.digest();

		return hashToHexString(computedHashBytes);

	}

	private static FileTable readFileTable(final RandomAccessFile inputStream) throws IOException, NoSuchAlgorithmException {	
		// first read the seed and, at the same time, table name. 
		final int SEED = readInt(inputStream);
		
		// array that contains the file table. 
		final byte fileTableData[] = new byte[SEED-4];

		final Decoder decoder = new Decoder(SEED);

		// decode the file table.
		for(int i = 0; i <(SEED-4); ++i) { // inclusive size.
			final byte b = inputStream.readByte();
			final byte decoded = decoder.decode(b);
			fileTableData[i] = decoded;
		}


		// the hash of the file table.
		byte[] fileTableHash = new byte[FileTable.HASH_SIZE];

		List<FileTableEntry>  fileTable = new ArrayList<FileTableEntry>();
		fileTable = new ArrayList<FileTableEntry>();

		// read in the file table hash.
		int dataI;
		for(dataI = 0; dataI < 128; ++dataI ) {
			fileTableHash[dataI] += fileTableData[dataI];
		}


		// compute the file table hash.
		int fileTableSize = fileTableData.length - FileTable.HASH_SIZE;
		String computedHash = computeHash(fileTableData, FileTable.HASH_SIZE, fileTableSize);

		// now compare with the hash stored in the file.
		if(!computedHash.equals(new String(fileTableHash))) {
			Log.i("wrong hash: " + computedHash);
			System.exit(1);
		}

		// next parse the file table. 
		while(dataI < fileTableData.length-10) {

			FileTableEntry entry = new FileTableEntry();

			int begI = dataI;

			// first parse the file name.
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

		return new FileTable(fileTable, SEED);
	}

	private static void dumpAllFiles(final RandomAccessFile inputStream, final FileTable fileTable) throws IOException {

		final int SEED = fileTable.getSeed();
		
		for(FileTableEntry entry : fileTable) {

			Log.i("dumping " + entry.getFilename());

			Decoder decoder = new Decoder(entry.seed);

			File file = new File(entry.getFilename());
			file.getParentFile().mkdirs();

			OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(entry.getFilename()));

			inputStream.seek(SEED + entry.offset);

			for(int i = 0; i <entry.size1; ++i) { // 0x5BB0

				final byte b = inputStream.readByte();	

				final byte decoded = decoder.decode(b);

				outputStream.write(decoded);
			}

			outputStream.close();
		}
	}

	
	private static void dumpDat(final String filename, final FileTable fileTable) throws IOException, NoSuchAlgorithmException {
		OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filename));

		final int SEED = fileTable.getSeed();
		
		Decoder decoder = new Decoder(SEED);

		outputStream.write(Util.toBytes(SEED));

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
