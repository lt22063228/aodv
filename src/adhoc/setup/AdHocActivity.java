package adhoc.setup;

//changed by Eric in 06/01/2014
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.BindException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;
import com.google.protobuf.InvalidProtocolBufferException;

import player.cacheManager.CacheInputStream;
import player.cacheManager.CacheManager;
import player.configuration.AppConfiguration;
import player.configuration.NetworkConfiguration;
import player.http.NanoHTTPD;
import player.messageManager.MessageReceiver;
import player.model.MessageProtos;
import player.model.MessageProtos.Message;
import player.taskManager.Diubaolv;
import player.taskManager.TaskManager;
import adhoc.aodv.Node;
import adhoc.aodv.Node.MessageToObserver;
import adhoc.aodv.Node.PacketToObserver;
import adhoc.aodv.ObserverConst;
import adhoc.aodv.exception.InvalidNodeAddressException;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.VideoView;

public class AdHocActivity extends Activity implements Observer{
	/** Called when the activity is first created. */

	private ListView listView = null;
	private List<Map<String, Object>> statusList = new ArrayList<Map<String, Object>>();;
	private SimpleAdapter adapter;

	private TextView countFrom3G = null;
	private TextView countFromWifi = null;
	private TextView cacheCount = null;
	private TextView cacheCountOfNotUsed = null;
	private TextView localIp = null;

	public AdHocSetup setup = null;

	public static final String MSG_TAG = "AdHoc -> AdHocActivity";

	Timer timer = new Timer(); 

	Handler handler; 

	TimerTask task = new TimerTask(){  

		public void run() {  
			android.os.Message message = new android.os.Message();      
			message.what = 1;      
			handler.sendMessage(message);    
		}  

	}; 

	private int index = 0;
	private String nodeVelocity[] =null; 
	private Date date = null;
	private SimpleDateFormat format = null;
	private String time = null;
	private String fileName = null;
	private File file = null;
	private FileWriter writer = null;
	private String newLine = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// hide titlebar of application
		// must be before setting the layout
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.main);
		
		CacheInputStream cacheInputStream = new CacheInputStream();
		cacheInputStream.setContext(this);
		
		setup = new AdHocSetup(this);
		setup.init();
		Log.d(MSG_TAG, "init complete !");
		setup.startAdhoc();      
		try {
			
			AppConfiguration.sharedNode = new Node(setup.getIpLastField());
			AppConfiguration.sharedNode.addObserver(this);
			AppConfiguration.sharedNode.startThread();
			AppConfiguration.myAddress = setup.getIpLastField();
			
	//		AppConfiguration.sharedNode.sendBroadcastData(data, type)
		} catch (BindException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidNodeAddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			Log.d("AdHocActivity", "create new NanoHTTPD");
			NanoHTTPD httpd = new NanoHTTPD(11000, new File("/"));
			//ForwardSendTest forwardSendTest=new ForwardSendTest();
			//	forwardSendTest.start();//10.27
//			MessageReceiver receiver = new MessageReceiver();
//     		receiver.startThread();//10.27
			httpd.start();
			
//			Diubaolv diubaolv=new Diubaolv();
//			diubaolv.start();//7.2
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		localIp = (TextView) findViewById(R.id.localIp);
		localIp.setText("local ip:"+NetworkConfiguration.SUBNET+setup.getIpLastField());

		VideoView view = (VideoView)findViewById(R.id.videoView1);
		Uri uri = Uri.parse("http://127.0.0.1:11000/test.mp4");
		MediaController controller = new MediaController(this);
		view.setMediaController(controller);
		view.setVideoURI(uri);
		view.start();

//		FilePrefetching filePrefetching = new FilePrefetching(view);
//		filePrefetching.startPrefetch();

		listView = (ListView) findViewById(R.id.listview);
		adapter = new SimpleAdapter(this, statusList, R.layout.item,
				new String[]{"ip","info","velocity"},
				new int[]{R.id.ip,R.id.UploadAndDownload,R.id.Velocity});
		listView.setAdapter(adapter);

		countFrom3G = (TextView) findViewById(R.id.countFrom3G);
		countFromWifi = (TextView) findViewById(R.id.countFromWifi);
		countFrom3G.setText("3G:"+CacheManager.sharedInstance().totalCountFrom3G/1024+"KB");
		countFromWifi.setText("WiFi:"+CacheManager.sharedInstance().totalCountFromWifi/1024+"KB");
		cacheCount = (TextView) findViewById(R.id.cacheCount);
		cacheCountOfNotUsed = (TextView) findViewById(R.id.cacheCountOfNotUsed);
		cacheCount.setText("Total:"+CacheManager.sharedInstance().cacheCount);
		cacheCountOfNotUsed.setText("Not used:"+CacheManager.sharedInstance().cacheCountOfNotUsed);

		nodeVelocity = new String [1024];
		date = new Date();
		format = new SimpleDateFormat("hh-mm-ss",Locale.CHINA);
		time = format.format(date);
		fileName = Environment.getExternalStorageDirectory().getPath() + "/NodeVelocityAndDataCount_" + setup.getIpLastField() + "_" + time + ".txt";
		file = new File(fileName);
		newLine = System.getProperty("line.separator");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			writer = new FileWriter(file, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		handler = new Handler(){

			public void handleMessage(android.os.Message msg) {  
				switch (msg.what) {      
				case 1:
					getData();
					adapter.notifyDataSetChanged();
					countFrom3G.setText("3G:"+CacheManager.sharedInstance().totalCountFrom3G/1024+"KB");
					countFromWifi.setText("WiFI:"+CacheManager.sharedInstance().totalCountFromWifi/1024+"KB");
					cacheCount.setText("Total:"+CacheManager.sharedInstance().cacheCount);
					cacheCountOfNotUsed.setText("Not used:"+CacheManager.sharedInstance().cacheCountOfNotUsed);
					if(index == 1023){
						writeBuffer(index);
						index = 0;
					}
					break;
				}      

				super.handleMessage(msg);  
			}  

		};

		timer.scheduleAtFixedRate(task, 1000, 1000);
	}

	private void getData() {
		synchronized (CacheManager.sharedInstance().downloadCount) {
			statusList.clear();
			Iterator<Entry<String, Long>> iterator = CacheManager.sharedInstance().downloadCount.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, Long> currentIp = iterator.next();
				Map<String, Object> map = new HashMap<String, Object>();

				String ip = currentIp.getKey();
				long downloadCount = currentIp.getValue().longValue();
				long lastDownloadCount = CacheManager.sharedInstance().lastDownloadCount.get(ip);
				long velocity = (downloadCount - lastDownloadCount) / 1024;
				CacheManager.sharedInstance().lastDownloadCount.put(ip, downloadCount);
				map.put("ip", ip);
				map.put("info", "D:" + downloadCount/1024 + "KB");
				map.put("velocity", velocity+"KB/S");
				if(velocity != 0){
					nodeVelocity[index] = ip + "," + velocity;
					index ++;
				}
				statusList.add(map);
			}
		}
	}

	public void writeBuffer(int length) {
		try {
			if(writer != null){
				for(int i = 0; i < length; i++) {
					writer.write(nodeVelocity[i] + newLine);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		setup.stopAdhoc();
		AppConfiguration.sharedNode.stopThread();
		writeBuffer(index);	
		try {
			if(writer != null){
				writer.write(newLine);
				writer.write("countFrom3G:"+CacheManager.sharedInstance().totalCountFrom3G/1024+"KB"+newLine);
				writer.write("countFromWifi:"+CacheManager.sharedInstance().totalCountFromWifi/1024+"KB"+newLine);
				writer.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.onDestroy();
	}

	@Override
	public void update(Observable o, Object org) {
		// TODO Auto-generated method stub
		MessageToObserver msg = (MessageToObserver)org;
		//		int userPacketID;
		//		int destination;
		int type = msg.getMessageType();
		switch(type) {
		case ObserverConst.ROUTE_ESTABLISHMENT_FAILURE:
			Log.d(MSG_TAG, "route establishment failure !!!");
			//				System.out.println("route establishment failure !!!");
			break;
			//************************
		case ObserverConst.DATA_RECEIVED:
			PacketToObserver packet = (PacketToObserver)org;
			int senderNode = packet.getSenderNodeAddress();
			byte[] data = (byte[])packet.getContainedData();
			Message newMessage = null;
			try {
				newMessage = MessageProtos.Message.parseFrom(data);
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (null != newMessage) {
				TaskManager.sharedInstance().processMessage(newMessage);
			}
			break;
		case ObserverConst.INVALID_DESTINATION_ADDRESS:
			Log.d(MSG_TAG, "invalid destination address");
			//				System.out.println("invalid destination address");
			break;
		case ObserverConst.DATA_SIZE_EXCEEDES_MAX:
			Log.d(MSG_TAG, "data size exceedes max ...");
			//				System.out.println("data size exceedes max ...");
			break;
		case ObserverConst.ROUTE_INVALID:
			Log.d(MSG_TAG, "route invalid");
			//				System.out.println("route invalid");
			break;
		case ObserverConst.ROUTE_CREATED:
			Log.d(MSG_TAG, "route created ...");
			//				System.out.println("route created ...");
			break;
		case ObserverConst.BROADCAST_DATA_RECEIVED:
			//				System.out.println("broadcast data received !");
			packet = (PacketToObserver)org;
			senderNode = packet.getSenderNodeAddress();
			data = (byte[])packet.getContainedData();
			//				System.out.println("broadcast data is : " + new String(data));
			System.out.println("got broadcast from node : " + senderNode);
			newMessage = null;
			try {
				newMessage = MessageProtos.Message.parseFrom(data);
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (null != newMessage) {
				TaskManager.sharedInstance().processMessage(newMessage);
			}
			break;
		default:
			break;
		}
	}
}