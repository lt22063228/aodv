package player.cacheManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import player.configuration.CacheConfiguration;
import player.model.URI;

public class DiskCacheManager {

	public boolean put(URI uri, byte[] data){
		String dirname = CacheConfiguration.cachepath + "/" +uri.identifier;
		File dir = new File(dirname);
		if(!dir.exists()){
			dir.mkdirs();
		}
		String filename = dir + "/" + uri.offset;
		File file = new File(filename);
		if(!file.exists()){
			try {
				file.createNewFile();
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(data);
				fos.close();
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace(); 
				return false;
			}	
		}
		return false;
	}

	public byte[] get(URI uri) {
		String filename = CacheConfiguration.cachepath + "/" + uri.identifier + "/" + uri.offset;
		File file = new File(filename);
		if(file.exists()){
			try {
				byte[] dataTemp = new byte[CacheConfiguration.blocksize];
				FileInputStream fis = new FileInputStream(file);
				int fileSize = fis.read(dataTemp);
				if(fileSize < CacheConfiguration.blocksize){
					byte[] data = new byte[fileSize];
					System.arraycopy(dataTemp, 0, data, 0, fileSize);
					return data;
				}else{
					return dataTemp;
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}else{
			return null;
		}
	}

	public boolean query(URI uri){
		File file = new File(CacheConfiguration.cachepath+"/"+uri.identifier+"/"+uri.offset);
		return file.exists();
	}
}
