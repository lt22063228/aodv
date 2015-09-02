package player.cacheManager;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.util.Log;

import player.configuration.CacheConfiguration;
import player.model.URI;
import player.taskManager.TaskManager;

public class CacheInputStream extends InputStream {

	static Context context = null;
	int number_not=0;//计数，没有从其他节点收到的，即从本地获取的
	long skip = 0;
	URI uri = new URI("test", 0);
	Socket socket = null;
	Properties header = new Properties();

	public void setContext(Context c){
		context = c;
	}

	/*public CacheInputStream(){
		super();
		try {
			socket = new Socket("218.3.42.118", 8181);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}*/

	@Override
	public int read() throws IOException {
		//System.out.println("test in read");
		Log.d("CacheInputStream", "in read()!!!");
		synchronized (this) {
			if (skip>273888220) {
				return -1;
			}
			else {
				//to be revised.
				long whichBlock = skip/CacheConfiguration.blocksize;
				int offsetInCurrentBlock = (int)(skip%CacheConfiguration.blocksize);
				uri.offset = whichBlock;
				while (null == CacheManager.sharedInstance().get(uri)) {
					TaskManager.sharedInstance().addTask(uri, this);
					try {
						this.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				byte[] data = CacheManager.sharedInstance().get(uri);
				skip ++;
				byte currentByte = data[offsetInCurrentBlock];
				Byte iO = new Byte(currentByte);
				return iO.intValue();
			}	
		}


	}
	

/*	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (null == b) {
			throw new NullPointerException();
		}
		if (off<0 || len<0 || off+len>b.length) {
			throw new IndexOutOfBoundsException();
		}
		if (len == 0) {
			return 0;
		}

		synchronized (this) {
			long startingBlock = skip/CacheConfiguration.blocksize;
			long endingBlock = (skip+len-1)/CacheConfiguration.blocksize;
			int blocksToGet = (int)(endingBlock-startingBlock+1);
			byte[] temp = new byte[blocksToGet*CacheConfiguration.blocksize];
			for (int i = 0; i < blocksToGet; i++) {
				uri.offset = startingBlock+i;
				byte[] data = null;
				while (null == (data = CacheManager.sharedInstance().get(uri))) {
					
			//		System.out.println("URI.offset:"+uri.offset);
					
					TaskManager.sharedInstance().addTask(uri, this);
					try {
						this.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch offset
						e.printStackTrace();
					}

					//check network state
					State mobile = null;
					if(context != null){
						ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
						mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
				//		System.out.println("network state is "+mobile.toString());
					}
					
					while(null == CacheManager.sharedInstance().get(uri) && mobile == State.CONNECTED) {
						
						//Does not get data from p2p and network is available
						if(null == socket){
							System.out.println("socket is null...");
							socket = new Socket("218.3.42.118", 8181);
						}else{

							//Cycle get data from 3G
							System.out.println("-----Get data form 3G-----");

							//analysis offset
							long start = uri.offset * CacheConfiguration.blocksize;
							long end = (uri.offset + 1) * CacheConfiguration.blocksize - 1;
							if(end > 273888219)
								end = 273888219;
							long predictSize = end - start + 1;
							System.out.println("The request file block: "+start+"-"+end);
							System.out.println("The predict size is "+predictSize);

							//send HTTP request
							OutputStream os = socket.getOutputStream();
							StringBuffer sb = new StringBuffer();   
							sb.append("GET /test.mp4 HTTP/1.1\r\n");
							sb.append("Connection: Keep-Alive\r\n");   
							sb.append("User-Agent: stagefright/1.2 (Linux;Android 4.0.4)\r\n");   
							sb.append("Host: 218.3.42.118\r\n");   
							sb.append("Accept-Encoding: gzip,deflate\r\n");   
							sb.append("Range: bytes="+start+"-"+end+"\r\n");   
							sb.append("\r\n");   
							os.write(sb.toString().getBytes());  
							os.flush();

							//Get HTTP reply's header
							byte[] buf = new byte[8192];   
							InputStream is = socket.getInputStream();
							int rlen = is.read(buf, 0, 8192);

							if(rlen < 0){
								System.out.println("Did not receive a reply, create a new socket");
								socket.close();
								socket = new Socket("218.3.42.118", 8181);
							}else{
								header.clear();
								ByteArrayInputStream hbis = new ByteArrayInputStream(buf, 0, rlen);
								BufferedReader hin = new BufferedReader(new InputStreamReader(hbis));
								String inLine = hin.readLine();
								while (inLine != null && inLine.trim().length() > 0 )
								{
									//System.out.println(inLine);
									int p = inLine.indexOf( ':' );
									if ( p >= 0 )
										header.put(inLine.substring(0,p).trim().toLowerCase(), inLine.substring(p+1).trim());
									inLine = hin.readLine();
								}

								//Analysis content range
								if(header.containsKey("content-range")){
									String contentRange = header.getProperty("content-range");
									int n1 = contentRange.indexOf("-");
									String tempStart = contentRange.substring(0, n1);
									String tempEnd = contentRange.substring(n1+1);
									int n2 = tempStart.indexOf(" ");
									long startOfRange = Long.parseLong(tempStart.substring(n2+1));
									int n3 = tempEnd.indexOf("/");
									long endOfRange = Long.parseLong(tempEnd.substring(0, n3));
									System.out.println("The range of response file block: "+startOfRange+"-"+endOfRange);
									if(startOfRange != start || endOfRange != end){
										System.out.println("Reply delay or error, create a new socket.");
										socket.close();
										socket = new Socket("218.3.42.118", 8181);
									}else{
										//Get data
										long dataSize = 0;
										if(header.containsKey("content-length"))
											dataSize = Long.parseLong(header.getProperty("content-length"));
										int splitbyte = 0;
										boolean sbfound = false;
										while (splitbyte < rlen)
										{
											if (buf[splitbyte] == '\r' && buf[++splitbyte] == '\n' && buf[++splitbyte] == '\r' && buf[++splitbyte] == '\n') {
												sbfound = true;
												break;
											}
											splitbyte++;
										}
										splitbyte++;
										ByteArrayOutputStream f = new ByteArrayOutputStream();
										if (splitbyte < rlen) {
											f.write(buf, splitbyte, rlen-splitbyte);
											dataSize -= rlen - splitbyte +1;
										}else if(!sbfound){
											dataSize = 0;
										}
										buf = new byte[512];
										while ( rlen >= 0 && dataSize > 0 )
										{
											rlen = is.read(buf, 0, 512);
											dataSize -= rlen;
											if (rlen > 0)
												f.write(buf, 0, rlen);
										}

										byte [] fbuf = f.toByteArray();
										dataSize = fbuf.length;
										System.out.println("The response data's length is "+dataSize);
										if(dataSize != predictSize){
											System.out.println("Get data error.");
											socket.close();
											socket = new Socket("218.3.42.118", 8181);
										}else{
											//Put data into cache
											CacheManager.sharedInstance().put(uri, fbuf);
											CacheManager.sharedInstance().totalCountFrom3G += fbuf.length;

											//Analysis connection availability
											if(header.containsKey("connection") && header.getProperty("connection").equals("close")){
												System.out.println("Socket is closed by sever, create a new socket...");
												socket.close();
												socket = new Socket("218.3.42.118", 8181);
											}
										}
									}
								}else{
									//Do not contain content-range property
									System.out.println("Do not contain content-range property");
									socket.close();
									socket = new Socket("218.3.42.118", 8181);
								}
							}
						}
					}
				}
				int actualSize = CacheConfiguration.blocksize>data.length?data.length:CacheConfiguration.blocksize;
				System.arraycopy(data, 0, temp, i*CacheConfiguration.blocksize, actualSize);
			}
			System.arraycopy(temp, (int)(skip%CacheConfiguration.blocksize), b, off, len);
			skip += len;
		}
		return b.length;
	}*/

	
	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		
		
		if (null == b) {
			Log.d("CacheInputStream", "null == b");
			throw new NullPointerException();
		}
		Log.d("CacheInputStream", "in read.. len : "+len);
		Log.d("CacheInputStream", "in read.. b.len : "+b.length);
		
		
		if (off<0 || len<0 || off+len>b.length) {
			Log.d("CacheInputStream", "off<0 || len < 0 || off+len>b.length");
			throw new IndexOutOfBoundsException();
		}
		if (len == 0) {
			Log.d("CacheInputStream", "len == 0");
			return 0;
		}
		/*
		for (int i = off; i < len+off; i++) {
			Integer iO = new Integer(read());
			b[i] = iO.byteValue();
		}
		 */
		synchronized (this) {
			long startingBlock = skip/CacheConfiguration.blocksize;
			long endingBlock = (skip+len-1)/CacheConfiguration.blocksize;
			int blocksToGet = (int)(endingBlock-startingBlock+1);
			Log.d("CacheInputStream", "skip : "+ skip);
			Log.d("CacheInputStream", "start : "+ startingBlock +";end : "+ endingBlock);
			Log.d("CacheInputStream", "blocksToGet : "+ blocksToGet);
			Log.d("CacheInputStream", "Request offset : "+ uri.offset);
			byte[] temp = new byte[blocksToGet*CacheConfiguration.blocksize];
			for (int i = 0; i < blocksToGet; i++) {
				uri.offset = startingBlock+i;
				byte[] data = null;
				while (null == (data = CacheManager.sharedInstance().get(uri))) {
					//向周围节点请求数据
					Log.d("CacheInputStream", "start find data from other nodes");
					TaskManager.sharedInstance().addTask(uri, this);
					try {
						this.wait(); 
					//	Thread.sleep(4000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch offset
						e.printStackTrace();
					}
					if(null == CacheManager.sharedInstance().get(uri)) {
						number_not++;
						Log.d("CacheInputStream", "Can't get data from other nodes"+number_not);
						String sss="Can't get data from other nodes"+number_not;
						TaskManager.sharedInstance().writeLogtofile(sss);
						
						File file = new File("/mnt/sdcard/test.mp4");
						long whichBlock = uri.offset;
						byte[] dataInCache = new byte[CacheConfiguration.blocksize];
						BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
						inputStream.skip(whichBlock*CacheConfiguration.blocksize);
						int bytes = inputStream.read(dataInCache);
						if (bytes<CacheConfiguration.blocksize) {
							byte[] newData = new byte[bytes];
							System.arraycopy(dataInCache, 0, newData, 0, bytes);
							CacheManager.sharedInstance().put(uri, newData);
							CacheManager.sharedInstance().totalCountFrom3G += newData.length;
							dataInCache = null;
						}else{
							CacheManager.sharedInstance().put(uri, dataInCache);
							CacheManager.sharedInstance().totalCountFrom3G += dataInCache.length;
						}
						inputStream.close();
					}
				}
				int actualSize = CacheConfiguration.blocksize>data.length?data.length:CacheConfiguration.blocksize;
				System.arraycopy(data, 0, temp, i*CacheConfiguration.blocksize, actualSize);
			}
			System.arraycopy(temp, (int)(skip%CacheConfiguration.blocksize), b, off, len);
			Log.d("CacheInputStream", "now len : "+ len);
			Log.d("CacheInputStream", "now skip : "+skip);
			skip += len;
		}
		return b.length;
	}
	
	
	@Override
	public long skip(long n) throws IOException {
		Log.d("CacheInputStream", "skip is n :" + n);
		skip = n;
		return skip;
	}

	@Override
	public int available() throws IOException {
		return (int) (273888220-skip);
	}

}
