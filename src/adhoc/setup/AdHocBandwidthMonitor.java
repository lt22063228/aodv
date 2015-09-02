package adhoc.setup;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;

import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Locale;

//import com.sun.xml.internal.messaging.saaj.util.LogDomainConstants;

//import sun.util.logging.resources.logging;


import adhoc.constants.Constants;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.net.TrafficStats;
import android.util.Log;
import adhoc.constants.Constants;
public class AdHocBandwidthMonitor implements Runnable {
	
//	private static final String MSG_TAG = "AdHocLinkOperator --> ";
	private String msgTag = "-->BandwidthMonitor";
	private AdHocBroadcastSender sender;
	private AdHocSetup application;
	private Thread operatorThread;
	private Boolean keepRunning;
	private DatagramSocket socket;
	private DatagramPacket packet;
	private Context context;

	public AdHocBandwidthMonitor(AdHocSetup app) {
		this.application = app;
		this.sender = new AdHocBroadcastSender(application);
		this.keepRunning = true;
		this.context = app.getContext();

	}
	
	public void startThread(){
		keepRunning = true;
		operatorThread = new Thread(this);
		operatorThread.start();
	}

	public void stopThread() {
		keepRunning = false;
		operatorThread.interrupt();
	}
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(keepRunning) {
			try {	
	/*			 //获得MemoryInfo对象  
		        MemoryInfo memoryInfo = new MemoryInfo() ;  
		
				//获得系统可用内存，保存在MemoryInfo对象上  
		        ActivityManager mActivityManager= (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);;
		        mActivityManager.getMemoryInfo(memoryInfo);
		        long memSize = memoryInfo.availMem ;
		        Log.d(msgTag, ""+memSize);*/
		
				long receive1 = (TrafficStats.getTotalRxBytes() 
						- TrafficStats.getMobileRxBytes());
						
				long send1 = (TrafficStats.getTotalTxBytes() 
						- TrafficStats.getMobileTxBytes());
						
				
				try { Thread.sleep(1000); } catch (InterruptedException e) {}
				
				long receive2 = (TrafficStats.getTotalRxBytes() 
						- TrafficStats.getMobileRxBytes());
					
				long send2 = (TrafficStats.getTotalTxBytes() 
						- TrafficStats.getMobileTxBytes());
										
				long rec = receive2 - receive1;
				long sed = send2 - send1;
				Log.d(msgTag,"download bandwidth : " +receive2);
				Log.d(msgTag,"upload bandwidth : "+send2);
				
				long all = rec+sed;
				String phoneInfoString = all +"";
				Constants.phoneInfo = phoneInfoString;
				
				Log.d(msgTag, "phone info : "+Constants.phoneInfo);
				
				
				
				Log.d(msgTag, "Start send to monitor...post");
				
				
				String request = "http://218.104.84.121:8091/fz2/index.php";
				String urlParameters = "postInfo="+phoneInfoString;
				sendPost(request, urlParameters);
				
/*				
                String urlstr="http://218.3.42.111:9090/AdHocMonitor/index.php";
  
				try {
					String getURL = urlstr + "?getInfo=" + phoneInfoString;
					Log.d(msgTag,getURL);
				    URL getUrl = new URL(getURL);
				    HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
				    connection.connect();
				    
				    // 取得输入流，并使用Reader读取
			        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));//设置编码,否则中文乱码
			        Log.d(msgTag,"=============================");
			        Log.d(msgTag,"Contents of get request");
			        Log.d(msgTag,"=============================");
			        String lines;
			        while ((lines = reader.readLine()) != null){
			        	//lines = new String(lines.getBytes(), "utf-8");
			        	Log.d(msgTag,lines);
			        }
			        reader.close();
			        // 断开连接
			        connection.disconnect();
			        Log.d(msgTag,"=============================");
			        Log.d(msgTag,"Contents of get request ends");
			        Log.d(msgTag,"=============================");
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
     */           
				Log.d(msgTag, "send ended...");
		
				
			/*	
				sender.startNoAckAreaBroadcast(Constants.BANDWIDTH_TEST, "" + application.getIpLastField());						
				Thread.sleep(2000);			
				Log.d(msgTag,"mapBandwidth_U size:"+Constants.mapBandwidth_U.size() );
				Log.d(msgTag,"mapBandwidth_D size:"+Constants.mapBandwidth_D.size() );
				for (Iterator iter = Constants.mapBandwidth_U.keySet().iterator(); iter.hasNext();) {
				      Object key = iter.next();
				      Object val = Constants.mapBandwidth_U.get(key);
				      Log.d(msgTag, "U_key:"+key);
				      Log.d(msgTag,"U_value:"+val);
				   }
				for (Iterator iter = Constants.mapBandwidth_D.keySet().iterator(); iter.hasNext();) {
				      Object key = iter.next();
				      Object val = Constants.mapBandwidth_D.get(key);
				      Log.d(msgTag, "D_key:"+key);
				      Log.d(msgTag,"D_value:"+val);
				   }*/
				Thread.sleep(500);				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void sendPost(String request,String urlParameters){
		URL url;
		try {
			url = new URL(request);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();    
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setInstanceFollowRedirects(true); 
			connection.setRequestMethod("POST"); 
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
			connection.setRequestProperty("charset", "utf-8");
			connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
			connection.setUseCaches (false);
			connection.connect();
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));//设置编码,否则中文乱码
		    String line="";
		    Log.d(msgTag,"=============================");
		    Log.d(msgTag,"Contents of post request");
		    Log.d(msgTag,"=============================");
		     while ((line = reader.readLine()) != null){
		        //line = new String(line.getBytes(), "utf-8");
		        Log.d(msgTag,line);
		     }
		    Log.d(msgTag,"=============================");
		    Log.d(msgTag,"Contents of post request ends");
		    Log.d(msgTag,"=============================");
		    reader.close();
		      
			connection.disconnect();
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
}

