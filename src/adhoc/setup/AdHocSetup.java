package adhoc.setup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.BindException;
import java.net.SocketException;
import java.net.UnknownHostException;


import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

public class AdHocSetup {

	// ������Թ���Log�����ǩ
	private static final String MSG_TAG = "AdHoc -> AdHocSetup";
	// ��������Ŀ¼
	public static String DATA_FILE_PATH = null;

	private Context context;

	// ad-hocģʽĬ�ϲ���
	private final String DEFAULT_IP = "192.168.2.254";
	private final String DEFAULT_PASSPHRASE = "1234567890123";
	private final String DEFAULT_SSID = "AdHoc";
	private final String DEFAULT_CHANNEL = "2";
	private final String DEFAULT_DEVICETYPE = "generic";

	// ad-hoc������ʶ
	public boolean startupPerformed = false;
	// ��¼��������ǰWIFI����״̬
	private boolean origWifiState = false;
	public WifiManager wifiManager = null;
	// wifiInterface �����в���WIFI�Ľӿ�
	private String wifiInterface = null;	
	// ����adhoc.conf���࣬����read��write����
	public AdHocConf adhocConf = null;
	
	//udp��Ϣ�����̣߳�����setup�׶ε�����udp��Ϣ
	private AdHocBroadcastReceiver receiver = null;
	//udp��Ϣ�����̣߳�����setup�׶ν��յ�������udp��Ϣ
	private AdHocMessageHandler handler = null;
	//AdHocDhcp�࣬���ڻ�ȡAdHoc�����п��õ�IP��ַ���ͷ�IP��ַ
	private AdHocDhcp dhcp = null;
//	private AdHocLinkOperator linkOp = null;
//
	private AdHocBandwidthMonitor bandwidthMonitor = null;
	
	private String currentIp;
	private int ipLastField;

	public AdHocSetup(Context context) {
		this.context = context;
	}
	public Context getContext(){
		return this.context;
	}
	public boolean init() {
		Log.d(MSG_TAG, "calling AdHocApplication onCreate()");
		// ��ȡ�����Ŀ¼
		DATA_FILE_PATH = context.getApplicationContext().getFilesDir()
				.getParent();
		Log.d(MSG_TAG, "got application root path : " + DATA_FILE_PATH);
		// ����Ŀ����Դ�ļ��У�û���򴴽�
		this.checkDirs();
		Log.d(MSG_TAG, "application folders created");
		// ��װ��Դ�ļ�
		this.installFiles();
		Log.d(MSG_TAG, "application files installed");
		this.adhocConf = new AdHocConf();
		// ��ȡWIFI�ӿ�
		this.wifiInterface = AdHocNative.getProp("wifi.interface");
		Log.d(MSG_TAG, "wifi interface is " + wifiInterface);
		this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		// this.wifiInfo = wifiManager.getConnectionInfo();
		// this.macAddress = wifiInfo.getMacAddress();
		this.origWifiState = false;
		this.startupPerformed = false;
		this.currentIp = "192.168.2.254";
		this.ipLastField = 254;
		
        try {
			this.handler = new AdHocMessageHandler(this);
	        this.receiver = new AdHocBroadcastReceiver(handler);
	        this.dhcp = new AdHocDhcp(this);
//	        this.bandwidthMonitor = new AdHocBandwidthMonitor(this);
//			this.linkOp = new AdHocLinkOperator(this);
		} catch (BindException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return true;
	}
	
	public boolean startAdhoc() {
	    this.updateConf();
		this.disableWifi();
    	if(AdHocNative.runRootCommand(DATA_FILE_PATH+"/bin/adhoc start 1") == true) {
    		
			//����AdHocģʽ�ɹ�
    		Log.d(MSG_TAG, "ad-hoc start complete !");
    		//����AdHocģʽ����״̬ΪTRUE
			this.startupPerformed = true;

			try {
				Thread.sleep(1000);
				//����UDP��Ϣ�����߳�
				handler.startThread();
				Log.d(MSG_TAG, "start adhoc broadcast listening ...");
				//����UDP��Ϣ�����߳�
				receiver.startBroadcastListening();	
				Log.d(MSG_TAG, "send dhcp DHCP_CONFIRM ...");
				//����IP֮����AdHoc���緢��IPȷ�ϣ�������IP��ʹ��
								
				Log.d(MSG_TAG, "start dhcp ...");
				this.resetIp(this.dhcp.getIp());				
				Thread.sleep(200);				
				dhcp.confirmIp(this.ipLastField);
				
				Thread.sleep(2000);
//				Log.d(MSG_TAG, "bandwidthMonitor start...");
//				bandwidthMonitor.startThread();
//				Log.d(MSG_TAG, "bandwidthMonitor end...");
				
				
			} catch (BindException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		return true;
    	}
    	else {
			//AdHocģʽ����ʧ��
			Log.d(MSG_TAG, "ad-hoc start error !");
			//AdHoc����״̬��Ϊfalse
			this.startupPerformed = false;
    		return false;
    	}

	}
	
	public boolean stopAdhoc() {
		try {
			dhcp.releaseIp(this.ipLastField);
		} catch (BindException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	AdHocNative.runRootCommand(DATA_FILE_PATH+"/bin/adhoc stop 1");
    	this.enableWifi();

		//AdHocģʽ�رճɹ�
	    Log.d(MSG_TAG, "AdHoc stop complete !");
	    this.startupPerformed = false;
	    receiver.stopBroadcastListening();
		//ֹͣUDP��Ϣ�����߳�
	    handler.stopThread();
	    return true;
	}
	
    private boolean restartAdhoc() {
    	//����AdHocģʽ������resetIp��
    	Log.d(MSG_TAG, "go to restart adhoc ...");
        if(AdHocNative.runRootCommand(DATA_FILE_PATH+"/bin/adhoc restart 1") == true) {
        	Log.d(MSG_TAG, "ad hoc restart complete ...");
        	return true;
        }
    	Log.d(MSG_TAG, "ad hoc restart failed ...");
    	return false;

    }
    
    private void resetIp(int ip) {
    	// ��������IP��ַ
    	String newIp = "192.168.2." + ip;
    	//��ȡadhoc.conf�����ļ�
        this.adhocConf.read();
        //�޸�ip.network��ֵ�ԣ�����IP
        this.adhocConf.put("ip.network", newIp);
        Log.d(MSG_TAG, "new ip.network : " + newIp);
        //�޸Ĺ�������д���ļ�
        if (this.adhocConf.write() == false) {
			Log.e(MSG_TAG, "Unable to rewrite ip to adhoc.conf!");
		}
    	this.currentIp = newIp;
        if(this.restartAdhoc() == true) {
        	Log.d(MSG_TAG, "reset ip done ! ad hoc restart complete ...");
        	this.ipLastField = ip;
        	this.currentIp = "192.168.2." + ip;
        	return;
        }
        Log.d(MSG_TAG, "reset ip failed ! unable to restart ad hoc ...");
    }

	private void checkDirs() {
		File dir = new File(DATA_FILE_PATH);
		if (dir.exists() == false) {
			Log.d(MSG_TAG, "Application data-dir does not exist!");
		} else {
			String[] dirs = { "/bin", "/var", "/conf" };
			for (String dirname : dirs) {
				dir = new File(DATA_FILE_PATH + dirname);
				if (dir.exists() == false) {
					if (!dir.mkdir()) {
						Log.d(MSG_TAG, "Couldn't create " + dirname
								+ " directory!");
					}
				} else {
					Log.d(MSG_TAG, "Directory '" + dir.getAbsolutePath()
							+ "' already exists!");
				}
			}
		}
	}

	public void installFiles() {
		// ����Դ�ļ������������Ŀ¼�µĸ�����Դ�ļ�����
		String message = null;
		// copy ifconfig
		if (message == null) {
			message = this.copyFile(DATA_FILE_PATH + "/bin/ifconfig", "0755",
					R.raw.ifconfig);
		}
		// copy iwconfig
		if (message == null) {
			message = this.copyFile(DATA_FILE_PATH + "/bin/iwconfig", "0755",
					R.raw.iwconfig);
		}
		// copy iwlist
		if (message == null) {
			message = this.copyFile(DATA_FILE_PATH + "/bin/iwlist", "0755",
					R.raw.iwlist);
		}
		// copy AdHoc
		if (message == null) {
			message = this.copyFile(DATA_FILE_PATH + "/bin/adhoc", "0755",
					R.raw.adhoc);
		}
		// copy AdHoc.conf
		if (message == null) {
			message = this.copyFile(DATA_FILE_PATH + "/conf/adhoc.conf",
					"0644", R.raw.adhoc_conf);
		}
		// copy AdHoc.edify
		if (message == null) {
			message = this.copyFile(DATA_FILE_PATH + "/conf/adhoc.edify",
					"0644", R.raw.adhoc_edify);
		}

		this.chmod(DATA_FILE_PATH + "/conf/", "0755");

		if (message == null) {
			Log.d(MSG_TAG, "copy files to application folder complete");
			return;
		} else {
			Log.d(MSG_TAG, message);
		}
	}

	private String copyFile(String filename, String permission, int ressource) {
		String result = this.copyFile(filename, ressource);
		if (result != null) {
			return result;
		}
		if (this.chmod(filename, permission) != true) {
			result = "Can't change file-permission for '" + filename + "'!";
		}
		return result;
	}

	private String copyFile(String filename, int ressource) {
		File outFile = new File(filename);
		Log.d(MSG_TAG, "Copying file '" + filename + "' ...");
		InputStream is = context.getResources().openRawResource(ressource);
		byte buf[] = new byte[1024];
		int len;
		try {
			OutputStream out = new FileOutputStream(outFile);
			while ((len = is.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.close();
			is.close();
		} catch (IOException e) {
			return "Couldn't install file - " + filename + "!";
		}
		return null;
	}
	
    private void updateConf() {
	    
    	//��Ĭ������д�������ļ�adhoc.conf
        this.adhocConf.read();
        this.adhocConf.put("wifi.essid", this.DEFAULT_SSID);
        Log.d(MSG_TAG, "wifi.essid : " + this.DEFAULT_SSID);
        this.adhocConf.put("wifi.channel", this.DEFAULT_CHANNEL);
        Log.d(MSG_TAG, "wifi.channel : " + this.DEFAULT_CHANNEL);
        this.adhocConf.put("ip.network", this.DEFAULT_IP);
        Log.d(MSG_TAG, "ip.network : " + this.DEFAULT_IP);
        this.adhocConf.put("wifi.interface", this.wifiInterface);
        Log.d(MSG_TAG, "wifi.interface : " + this.wifiInterface);
        this.adhocConf.put("device.type", this.DEFAULT_DEVICETYPE);
        Log.d(MSG_TAG, "device.type : " + this.DEFAULT_DEVICETYPE);
        this.adhocConf.put("wifi.encryption.key", this.DEFAULT_PASSPHRASE);
        Log.d(MSG_TAG, "wifi wep key is : " + this.DEFAULT_PASSPHRASE);
        
        if (this.adhocConf.write() == false) {
			Log.e(MSG_TAG, "Unable to update adhoc.conf!");
		}
        this.currentIp = this.DEFAULT_IP;
    }
	
	private boolean chmod(String file, String mode) {
    	if (AdHocNative.runCommand("chmod "+ mode + " " + file) == 0) {
    		return true;
    	}
    	return false;
    }
	
	private void disableWifi() {
    	Log.d(MSG_TAG, "wifi state is : " + this.origWifiState);
    	if (this.wifiManager.isWifiEnabled()) {
    		origWifiState = true;
    		this.wifiManager.setWifiEnabled(false);
    		Log.d(MSG_TAG, "Wifi disabled!");
        	// Waiting for interface-shutdown
    		try {
    			Thread.sleep(6000);
    		} catch (InterruptedException e) {
    			// nothing
    		}
    	}
    }
	
    private void enableWifi() {
    	Log.d(MSG_TAG, "wifi state is : " + this.origWifiState);
    	if (origWifiState) {
        	// Waiting for interface-restart
    		this.wifiManager.setWifiEnabled(true);
    		try {
    			Thread.sleep(6000);
    		} catch (InterruptedException e) {
    			// nothing
    		}
    		Log.d(MSG_TAG, "Wifi started!");
    	}
    }
    
    public int getIpLastField() {
    	//��ȡIP��ַ����ֶΣ�����תΪINT��
    	return this.ipLastField;
    }
    
    public String getCurrentIp() {
    	return this.currentIp;
    }
    
}
