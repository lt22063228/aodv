package player.configuration;

import android.os.Environment;


public class CacheConfiguration {
	public static final int blocksize = 48*1024;		//in byte
	public static final String cachepath = Environment.getExternalStorageDirectory().getPath()+"/AdHocCache";
	public static final int maxNumberOfMemCache = 512;	//max number of file block in disk cache
	public static final int maxNumberOfDiskCache = 5573;	//max number of file block in disk cache
	public static final int replaceNumber = 256;		//the number of file block each deleted
	public static final int maxNotUsedNumber = 512;	//max number of cache not used
	public static final int PRIORITY_PREFETCH_MAX = 20;	//max priority of file block not used
	public static final int PRIORITY_PREFETCH_MIN = 10;	//min priority of file block not used
	public static final int PRIORITY_CACHE_MAX = 10;	//max priority of file block used
	public static final int PRIORITY_CACHE_MIN = 0;		//min priority of file block used
}
