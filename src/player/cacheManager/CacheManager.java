package player.cacheManager;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import player.configuration.CacheConfiguration;
import player.model.URI;

public class CacheManager {
	private static CacheManager sharedInstance = null;
	private static DiskCacheManager diskCacheManager = null;
	private static MemCacheLinkedHashMap memCache = null;

	public HashMap<String, Long> downloadCount = new HashMap<String, Long>();
	public HashMap<String, Long> lastDownloadCount = new HashMap<String, Long>();
	public HashMap<String, Long> uploadCount = new HashMap<String, Long>();
	public long totalCountFrom3G = 0;
	public long totalCountFromWifi = 0;
	public long cacheCount = 0;
	public long cacheCountOfNotUsed = 0;
	
	public HashSet<URI> uriSet = new HashSet<URI>();

	private LinkedList<URI> uriList = new LinkedList<URI>();		//URI index

	CacheManager() {
		if (null == diskCacheManager) {
			diskCacheManager = new DiskCacheManager();
		}
		if (null == memCache) {
			memCache = new MemCacheLinkedHashMap();
		}
		initUriIndex();
		URIIndexManager uriIndexManager = new URIIndexManager();
		uriIndexManager.start();
	}

	public synchronized static CacheManager sharedInstance() {
		if (null == sharedInstance) {
			sharedInstance = new CacheManager();
		}
		return sharedInstance;
	}

	public boolean put(URI uri, byte[] data) {
		synchronized (uriList) {
			URI tempUri = new URI("", 0);
			tempUri.identifier = uri.identifier;
			tempUri.offset =  uri.offset;
			uriList.add(tempUri);
		}
		synchronized (memCache) {
			memCache.put(uri.identifier+"/"+uri.offset, data);
			return true;
		}
	}

	public byte[] get(URI uri){
		if(memCache.containsKey(uri.identifier+"/"+uri.offset)){
			setUsed(uri);
			synchronized (uriSet) {
				URI tempUri = new URI("", 0);
				tempUri.identifier = uri.identifier;
				tempUri.offset = uri.offset;
				uriSet.add(tempUri);
			}
			byte[] data = memCache.get(uri.identifier+"/"+uri.offset);
			return data;
		}else if(diskCacheManager.query(uri)){
			setUsed(uri);
			synchronized (uriSet) {
				URI tempUri = new URI("", 0);
				tempUri.identifier = uri.identifier;
				tempUri.offset = uri.offset;
				uriSet.add(tempUri);
			}
			byte[] data = diskCacheManager.get(uri);
			return data;
		}
		return null;
	}

	public boolean query(URI uri){
		if(diskCacheManager.query(uri) || memCache.containsKey(uri.identifier+"/"+uri.offset))
			return true;
		return false;
	}

	//initialize URI index through scanning AdHocCache
	private void initUriIndex(){

		System.out.println("init URI index...");

		File rootFile = new File(CacheConfiguration.cachepath);
		if(rootFile.exists()&&rootFile.isDirectory()){
			if(0 == rootFile.listFiles().length){
				//no cache
			}else{
				File[] identifiers = rootFile.listFiles();
				for(int i=0;i<identifiers.length;++i){
					if(identifiers[i].isDirectory()){
						String identifier = identifiers[i].getName();
						File[] offsets = identifiers[i].listFiles();
						for(int j=0;j<offsets.length;++j){
							long offset = Long.parseLong(offsets[j].getName());
							URI uri = new URI(identifier, offset);
							synchronized (uriList) {
								uriList.add(uri);
							}
						}
					}
				}
			}
		}
	}

	public void setUsed(URI uri){
		synchronized (uriList) {
			int index = uriList.indexOf(uri);
			if(index != -1){
				URI tempUri = new URI("", 0);
				tempUri.identifier = uri.identifier;
				tempUri.offset = uri.offset;
				tempUri.isUsed = true;
				uriList.set(index, tempUri);
			}
		}
	}

	public int getCountOfNotUsed(){
		synchronized (uriList) {
			int count = 0;
			Iterator<URI> iterator = uriList.iterator();
			while(iterator.hasNext()){
				if(!iterator.next().isUsed)
					++count;
			}
			this.cacheCountOfNotUsed = count;
			return count;
		}
	}

	private class URIIndexManager extends Thread{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			System.out.println("CacheManager.URIIndexManager.run()");
			while(true){
				int currentLength = uriList.size();
				cacheCount = currentLength;
				int maxNumber = CacheConfiguration.maxNumberOfDiskCache + CacheConfiguration.maxNumberOfMemCache;
				if(maxNumber < currentLength){

					System.out.println("cal the pri of uri...");
					synchronized (uriList) {
						Iterator<URI> iterator = uriList.iterator();
						while(iterator.hasNext()){
							chpri(iterator.next());
						}
					}

					//sort
					System.out.println("sort...");
					URI[] uris = new URI[currentLength];
					for(int i=0;i<currentLength;++i){
						uris[i] = uriList.get(i);
					}
					Arrays.sort(uris);
					
					for(int i=0;i<currentLength;++i)
						System.out.println(uris[i]+":"+uris[i].priority);

					//replace
					System.out.println("replace...");
					for(int j=0;j<CacheConfiguration.replaceNumber;++j){
						URI uri = uris[j];
						synchronized (uriList) {
							uriList.remove(uri);
						}
						if(memCache.containsKey(uri.identifier+"/"+uri.offset)){
							memCache.remove(uri.identifier+"/"+uri.offset);
						}else{
							String filename = CacheConfiguration.cachepath + "/" + uri.identifier + "/" + uri.offset;
							File file = new File(filename);
							if(file.exists())
								file.delete();
						}
					}
					System.out.println("complete...");
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	//calculate the priority of URI
	private void chpri(URI uri){
		Random random = new Random();
		if(uri.isUsed){
			uri.priority = random.nextInt(CacheConfiguration.PRIORITY_CACHE_MAX - 
					CacheConfiguration.PRIORITY_CACHE_MIN) + CacheConfiguration.PRIORITY_CACHE_MIN;
		}else{
			uri.priority = random.nextInt(CacheConfiguration.PRIORITY_PREFETCH_MAX - 
					CacheConfiguration.PRIORITY_PREFETCH_MIN) + CacheConfiguration.PRIORITY_PREFETCH_MIN;
		}
	}
}
