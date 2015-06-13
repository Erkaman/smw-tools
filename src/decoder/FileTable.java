package decoder;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

public class FileTable implements Iterable<FileTableEntry> {
	
	public final static int HASH_SIZE = 128; // the size of the file table hash in bytes

	private final List<FileTableEntry> fileTable;
	private final int SEED;

	public FileTable(final List<FileTableEntry> fileTable, final int seed) {
		this.fileTable = fileTable;
		this.SEED = seed;
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
}
