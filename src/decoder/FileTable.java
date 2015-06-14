package decoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FileTable implements Iterable<FileTableEntry> {

	public final static int HASH_SIZE = 128; // the size of the file table hash in bytes

	private final List<FileTableEntry> fileTable;
	private final int SEED;

	public FileTable(final RandomAccessFile inputStream) throws NoSuchAlgorithmException, IOException {

		// first read the seed and, at the same time, table name. 
		this.SEED = Util.readInt(inputStream);

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

			entry.compressedSize = Util.toInt(fileTableData[dataI++],fileTableData[dataI++],fileTableData[dataI++],fileTableData[dataI++]);
			entry.originalSize = Util.toInt(fileTableData[dataI++],fileTableData[dataI++],fileTableData[dataI++],fileTableData[dataI++]);
			entry.offset = Util.toInt(fileTableData[dataI++],fileTableData[dataI++],fileTableData[dataI++],fileTableData[dataI++]);
			entry.isNotCompressed = fileTableData[dataI++];
			entry.seed = Util.toInt(fileTableData[dataI++],fileTableData[dataI++],fileTableData[dataI++],fileTableData[dataI++]);

			fileTable.add(entry);
		}

		this.fileTable = fileTable;
	}

	public FileTable(final List<FileTableEntry> fileTable) {
		this.fileTable = fileTable;

		// compute the seed.

		int seed = 4; // the seed is counted in the size.

		seed += HASH_SIZE;

		for(FileTableEntry entry : fileTable) {
			seed += entry.getSize();
		}
		this.SEED = seed;
	}

	public int getSeed() {
		return SEED;
	}

	@Override
	public Iterator<FileTableEntry> iterator() {
		return fileTable.iterator();
	}

	@Override
	public String toString() {
		String str = "";

		str += "seed: " + Integer.toHexString(this.getSeed()) + "\n";

		for (FileTableEntry s : fileTable)
		{
			str += s + "\n";
		}

		return str;
	}


	public byte[] encodeAsByteArray() throws IOException, NoSuchAlgorithmException {
		final int SEED = this.getSeed();

		final ByteArrayOutputStream fileTableOutputStream = new ByteArrayOutputStream();
		for(final FileTableEntry entry : fileTable) {
			entry.writeToStream(fileTableOutputStream);			
		}

		// contains the file table minus the hash. 
		final byte[] fileTableBytes = fileTableOutputStream.toByteArray();

		// compute the hash from the table.
		final String hashCode = computeHash(fileTableBytes, 0, fileTableBytes.length);

		final ByteArrayOutputStream entireFileTableOutputStream = new ByteArrayOutputStream();

		// now write the hash AND the file table.
		for(final char ch : hashCode.toCharArray()) {
			entireFileTableOutputStream.write(ch);
		}
		entireFileTableOutputStream.write(fileTableBytes);

		final ByteArrayOutputStream encryptedFileTableOutputStream = new ByteArrayOutputStream();

		// next encrypt the hash AND the file table. 
		final byte[] out = entireFileTableOutputStream.toByteArray();
		Decoder decoder = new Decoder(SEED);
		for(final byte b : out) { // inclusive size. 
			final byte encoded = decoder.decode(b);

			encryptedFileTableOutputStream.write(encoded);
		}

		return encryptedFileTableOutputStream.toByteArray();
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
}
