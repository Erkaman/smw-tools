package decoder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
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
import java.util.zip.DeflaterInputStream;
import java.util.zip.InflaterInputStream;

/**
 * Decoder for ImgDat File. 
 * @author eric
 *
 */
public class Archive {

	private static final String IMG_DAT = 
			//"../../Downloads/Super Marisa World/Data.dat";; 
			//"../../Downloads/Super Marisa World/Sound.dat";; 
			//"../../Downloads/Super Marisa World/Music.dat";; 

			"../../Downloads/Super Marisa World/Img.dat";

	public static void unpack() throws IOException, NoSuchAlgorithmException {	
		final RandomAccessFile inputStream = new RandomAccessFile( IMG_DAT, "r");
		final FileTable fileTable = readFileTable(inputStream);
		
		Log.i(fileTable.toString());
		

		dumpAllFiles(inputStream, fileTable);
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
		
		for(final String file : files) {
			FileTableEntry entry = new FileTableEntry();
			
			byte[] filenameBytes = file.getBytes("Shift_JIS");
			
			entry.filename = new byte[filenameBytes.length];
			int i = 0;
			for(byte b : filenameBytes) {
				entry.filename[i++] = b;
			}
			
			final int filesize = (int)new File(file).length();
			
			entry.isNotCompressed = shouldCompressFile(entry.filename);	
			
			entry.compressedSize = filesize;
			entry.originalSize = filesize;
			
			entry.offset = 0;
			
			//entry.offset = offset;
			
			/*if(offset == 0x1ced0f2) {
				Log.i("haro");
			}*/
			
			//offset += filesize;
			
			entry.seed = rng.nextInt();
			
			
			fileTableList.add(entry);
		}
		
		FileTable fileTable = new FileTable(fileTableList);
		
		//Log.i(fileTable.toString());
		
		dumpDat("Img.dat", fileTable);
	}
	
	private static byte shouldCompressFile(byte[] filename) {
		final int LEN = filename.length;
		
		return
				
				(filename[LEN-3] == (byte)'.' &&				
				filename[LEN-2] == (byte)'d'  &&			
				filename[LEN-1] == (byte)'b' ) ||
				
				(filename[LEN-4] == (byte)'.' &&	
				filename[LEN-3] == (byte)'d'  &&					
				filename[LEN-2] == (byte)'a'  &&			
				filename[LEN-1] == (byte)'t' ) ||
				
				(filename[LEN-4] == (byte)'.' &&	
				filename[LEN-3] == (byte)'t'  &&					
				filename[LEN-2] == (byte)'x'  &&			
				filename[LEN-1] == (byte)'t' ) ||
				
				(filename[LEN-4] == (byte)'.' &&	
				filename[LEN-3] == (byte)'t'  &&					
				filename[LEN-2] == (byte)'i'  &&			
				filename[LEN-1] == (byte)'m' )
				
				
			
				? (byte)0  : (byte)1;
	}
		

	private static FileTable readFileTable(final RandomAccessFile inputStream) throws IOException, NoSuchAlgorithmException {	
		return new FileTable(inputStream);
	}

	private static void dumpAllFiles(final RandomAccessFile inputStream, final FileTable fileTable) throws IOException {

		final int SEED = fileTable.getSeed();
		
		for(FileTableEntry entry : fileTable) {

			Log.i("dumping " + entry.getFilename());


			File file = new File(entry.getFilename());
			file.getParentFile().mkdirs();
			
			Decoder decoder = new Decoder(entry.seed);

			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
					
					//new BufferedOutputStream(new FileOutputStream(entry.getFilename()));

			inputStream.seek(SEED + entry.offset);

			for(int i = 0; i <entry.compressedSize; ++i) { // 0x5BB0

				final byte b = inputStream.readByte();	

				final byte decoded = decoder.decode(b);

				byteArrayOutputStream.write(decoded);
			}
			byteArrayOutputStream.close();
			
			byte[] byteArray = byteArrayOutputStream.toByteArray();
			final String outFile = entry.getFilename();
			
			if(entry.isNotCompressed == 1) {
				// not compressed, so just write the byte array.
				final OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile));

				outputStream.write(byteArray);
				
				outputStream.close();
			} else {
				decompressArray(byteArray, outFile);
			}
		}
	}
	
	private static void decompressArray(final byte[] inBuffer, final String outFile) throws IOException {
		final InputStream in =
				new InflaterInputStream(new ByteArrayInputStream(inBuffer));

		final OutputStream out = new FileOutputStream(outFile);

		final byte[] buffer = new byte[1024];
		int len;
		while((len = in.read(buffer)) > 0) {
			out.write(buffer, 0, len);
		}

		in.close();
		out.close();
	}

	
	private static void dumpDat(final String filename, final FileTable fileTable) throws IOException, NoSuchAlgorithmException {
		final OutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(filename));

		// write the seed.
		final int SEED = fileTable.getSeed();
		fileOutputStream.write(Util.toBytes(SEED));
		fileOutputStream.write(fileTable.encodeAsByteArray());

		int offset = 0;
		for(final FileTableEntry entry : fileTable) {

			final int seed = entry.seed;

			Decoder decoder = new Decoder(seed);

			Log.i("encoding file: " + entry.getFilename());

			InputStream inputStream = new BufferedInputStream(new FileInputStream(entry.getFilename()));
			
			if(entry.isNotCompressed == 0) {
				 inputStream = new DeflaterInputStream(inputStream);
			}

			int compressedSize = 0;
			while(true) {
				final int i = inputStream.read();

				if(i == -1)
					break;

				++compressedSize;
				final byte b = (byte)i;
				final byte encoded = decoder.decode(b);

				fileOutputStream.write(encoded);
			}
			inputStream.close();		
			
			entry.compressedSize = compressedSize;
			
			
			entry.offset = offset;
			
			/*if(offset == 0x1ced0f2) {
				Log.i("haro");
			}*/
			
			offset += entry.compressedSize;
		}

		fileOutputStream.close();

		
		// we now know the compressed sizes. So patch the file table. 
		final RandomAccessFile out = new RandomAccessFile(filename, "rw");
		out.seek(4);
		out.write(fileTable.encodeAsByteArray());
		out.close();

	}
}
