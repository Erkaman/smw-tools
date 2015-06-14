package gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class Settings {
	
	public static final String CHOOSER_DIR = "chooser_dir";
	private static final String PROP_FILE;
	
	static {
		PROP_FILE ="PackerGUI.properties";
			
	}
	
	private final Properties prop;

	public Settings() throws IOException {
		prop = new Properties();

		InputStream inputStream = null;

		try {
			
			if(new File(PROP_FILE).exists())
				inputStream = new FileInputStream(PROP_FILE);
					
			if(inputStream != null) {
				prop.load(inputStream);
			}
		} finally {
			if (inputStream != null) {
				try{
					inputStream.close();
				} finally { }
			}
		}
	}


	
	public String getProperty(final String key)  {
		return prop.getProperty(key);
	}
	
	public void setProperty(final String key, final String value)  {
		prop.setProperty(key, value);
	}
	
	public void save() throws IOException {
		
		OutputStream output = null;
		 
		try {
			
			output = new FileOutputStream(PROP_FILE.toString());
	
			prop.store(output, null);
	 
		} finally {
			if (output != null) {
				try {
					output.close();
				} finally {
			
				}
			}
	 
		}
	}
}
