package player.cacheManager;

import java.util.LinkedHashMap;
import java.util.Map;

import android.util.Log;

import player.configuration.CacheConfiguration;
import player.model.URI;

public class MemCacheLinkedHashMap extends LinkedHashMap<String, byte[]> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private static final int MAX_ENTRIES = CacheConfiguration.maxNumberOfMemCache;
	
	private DiskCacheManager diskCacheManager = new DiskCacheManager();

	protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
		if(size() > MAX_ENTRIES){
			Log.d("MemCacheLink----remove", "removeEldestEntry!!!");
			String key = eldest.getKey();
			String[] info = key.split("/");
			String identifier = info[0];
			long offset = Long.parseLong(info[1]);
			URI uri = new URI(identifier, offset);
			byte[] data = eldest.getValue();
			diskCacheManager.put(uri, data);
		}
		return size() > MAX_ENTRIES;
	}
}
