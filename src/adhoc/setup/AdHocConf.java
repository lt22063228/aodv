package adhoc.setup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

public class AdHocConf extends HashMap<String, String> {
	
	public static final String MSG_TAG = "AdHoc -> AdHocNative";
	
	private static final long serialVersionUID = 1L;
	//将AdHoc.conf配置文件，每行以=作为分隔符，将文件内容读出，并存储为HashMap键值对
	public HashMap<String, String> read() {
		String filename = AdHocSetup.DATA_FILE_PATH + "/conf/adhoc.conf";
		this.clear();
		for (String line : readLinesFromFile(filename)) {
			if (line.startsWith("#"))
				continue;
			if (!line.contains("="))
				continue;
			String[] data = line.split("=");
			if (data.length > 1) {
				this.put(data[0], data[1]);
			} 
			else {
				this.put(data[0], "");
			}
		}
		return this;
	}
	//将所有的HashMap键值对写入AdHoc.conf文件
	public boolean write() {
		String lines = new String();
		for (String key : this.keySet()) {
			lines += key + "=" + this.get(key) + "\n";
		}
		return writeLinesToFile(AdHocSetup.DATA_FILE_PATH + "/conf/adhoc.conf", lines);
	}
	
    public ArrayList<String> readLinesFromFile(String filename) {
    	String line = null;
    	BufferedReader br = null;
    	InputStream ins = null;
    	ArrayList<String> lines = new ArrayList<String>();
    	File file = new File(filename);
    	if (file.canRead() == false)
    		return lines;
    	try {
    		ins = new FileInputStream(file);
    		br = new BufferedReader(new InputStreamReader(ins), 8192);
    		while((line = br.readLine())!=null) {
    			lines.add(line.trim());
    		}
    	} catch (Exception e) {
    		Log.d(MSG_TAG, "Unexpected error - Here is what I know: "+e.getMessage());
    	}
    	finally {
    		try {
    			ins.close();
    			br.close();
    		} catch (Exception e) {
    			// Nothing.
    		}
    	}
    	return lines;
    }
    
    public boolean writeLinesToFile(String filename, String lines) {
		OutputStream out = null;
		boolean returnStatus = false;
		Log.d(MSG_TAG, "Writing " + lines.length() + " bytes to file: " + filename);
		try {
			out = new FileOutputStream(filename);
        	out.write(lines.getBytes());
        	out.flush();
		} catch (Exception e) {
			Log.d(MSG_TAG, "Unexpected error - Here is what I know: "+e.getMessage());
		}
		finally {
        	try {
        		if (out != null)
        			out.close();
        		returnStatus = true;
			} catch (IOException e) {
				returnStatus = false;
			}
		}
		return returnStatus;
    }
}
