package decoder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Util {

	public static List<String> getAllFiles(final String dir){

		final List<String> files = new ArrayList<String>();
		
		getAllFilesHelper(dir, files);
		
		return files;	
	}
	
	private static void getAllFilesHelper(final String dir, final List<String> files){
	
		final File[] listedFiles = new File(dir).listFiles();
		for(final File file: listedFiles){
			
			if(file.isDirectory()){
				getAllFilesHelper(file.getAbsolutePath(), files);
			} else if(file.isFile()) {
				files.add(file.getAbsolutePath());
			}
		}
	}
	
	public static byte[] toBytes(final int i) {

		byte[] bytes = new byte[4];

		bytes[3] =(byte)((i >> 24) & 0xFF);
		bytes[2] = (byte)((i >> 16) & 0xFF);
		bytes[1] = (byte)((i >> 8) & 0xFF);
		bytes[0] = (byte)((i) & 0xFF);

		return bytes;
	}
}
