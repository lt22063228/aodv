package adhoc.setup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;

import java.util.Enumeration;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class AdHocApplication extends Application {
	//������Թ���Log�����ǩ
	public static final String MSG_TAG = "AdHoc -> Application";
	//��������Ŀ¼
	public static String DATA_FILE_PATH = null;
	//ad-hocģʽĬ�ϲ���
	private final String DEFAULT_IP = "192.168.2.254";
	private final String DEFAULT_PASSPHRASE = "1234567890123";
	private final String DEFAULT_SSID = "AdHoc";
	private final String DEFAULT_CHANNEL = "2";
	private final String DEFAULT_DEVICETYPE = "generic";
	
	public String currentIp;
	public String macAddress;
	//ad-hoc������ʶ
	public boolean startupPerformed = false;
	//��¼��������ǰWIFI����״̬
	private boolean origWifiState = false;
	
	public WifiManager wifiManager = null;
	public WifiInfo wifiInfo = null;
	
	//wifiInterface �����в���WIFI�Ľӿ�
	private String wifiInterface = null;
	//����adhoc.conf���࣬����read��write����
	public AdHocConf adhocConf = null;

	// Preferences ��ʱû��
	public SharedPreferences settings = null;
	public SharedPreferences.Editor preferenceEditor = null;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		Log.d(MSG_TAG, "calling AdHocApplication onCreate()");
		//��ȡ�����Ŀ¼
		DATA_FILE_PATH = this.getApplicationContext().getFilesDir().getParent();       
        Log.d(MSG_TAG, "got application root path : " + DATA_FILE_PATH);
        //����Ŀ����Դ�ļ��У�û���򴴽�    
        this.checkDirs();        
        Log.d(MSG_TAG, "application folders created");
        // Preferences(�����)
        this.settings = PreferenceManager.getDefaultSharedPreferences(this);
        this.preferenceEditor = settings.edit();
        //��װ��Դ�ļ�
        this.installFiles();
        Log.d(MSG_TAG, "application files installed");
        this.adhocConf = new AdHocConf();
        //��ȡWIFI�ӿ�
        this.wifiInterface = AdHocNative.getProp("wifi.interface");
        Log.d(MSG_TAG, "wifi interface is " + wifiInterface);
        this.wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);  
        this.wifiInfo = wifiManager.getConnectionInfo();
        this.macAddress = wifiInfo.getMacAddress();
        this.origWifiState = false;
        this.startupPerformed = false;
	}
	
	
    public void updateConf() {
	    
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
    
    public void resetIp(int ip) {
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
        	return;
        }
        Log.d(MSG_TAG, "reset ip failed ! unable to restart ad hoc ...");
    }
   
    public int getIpLastField() {
    	//��ȡIP��ַ����ֶΣ�����תΪINT��
    	String str[] = this.currentIp.split("\\.");
    	return Integer.parseInt(str[str.length-1]);
    }
    
    public boolean restartAdhoc() {
    	//����AdHocģʽ������resetIp��
    	Log.d(MSG_TAG, "go to restart adhoc ...");
        if(AdHocNative.runRootCommand(AdHocApplication.DATA_FILE_PATH+"/bin/adhoc restart 1") == true) {
        	Log.d(MSG_TAG, "ad hoc restart complete ...");
        	return true;
        }
    	Log.d(MSG_TAG, "ad hoc restart failed ...");
    	return false;

    }
	
	public boolean startAdhoc() {
	    updateConf();
		disableWifi();
    	if(AdHocNative.runRootCommand(AdHocApplication.DATA_FILE_PATH+"/bin/adhoc start 1") == true) {
    		return true;
    	}
		return false;
	}
	
	public boolean stopAdhoc() {
    	if(AdHocNative.runRootCommand(AdHocApplication.DATA_FILE_PATH+"/bin/adhoc stop 1") == true) {
    		enableWifi();
    		return true;
    	}
    	enableWifi();
		return false;
	}
	
	public void disableWifi() {
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
    
    public void enableWifi() {
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
	
    private void checkDirs() {
    	File dir = new File(DATA_FILE_PATH);
    	if (dir.exists() == false) {
    		Log.d(MSG_TAG, "Application data-dir does not exist!");
    	}
    	else {
    		String[] dirs = { "/bin", "/var", "/conf" };
    		for (String dirname : dirs) {
    			dir = new File(DATA_FILE_PATH + dirname);
    	    	if (dir.exists() == false) {
    	    		if (!dir.mkdir()) {
    	    			Log.d(MSG_TAG, "Couldn't create " + dirname + " directory!");
    	    		}
    	    	}
    	    	else {
    	    		Log.d(MSG_TAG, "Directory '"+dir.getAbsolutePath()+"' already exists!");
    	    	}
    		}
    	}
    }
    
    public void installFiles () {
    	//����Դ�ļ������������Ŀ¼�µĸ�����Դ�ļ�����
        String message = null;
		// copy ifconfig
    	if (message == null) {
	    	message = this.copyFile(DATA_FILE_PATH+"/bin/ifconfig", "0755", R.raw.ifconfig);
    	}
    	// copy iwconfig
    	if (message == null) {
	    	message = this.copyFile(DATA_FILE_PATH+"/bin/iwconfig", "0755", R.raw.iwconfig);
    	}
    	// copy iwlist
    	if (message == null) {
	    	message = this.copyFile(DATA_FILE_PATH+"/bin/iwlist", "0755", R.raw.iwlist);
    	}
    	// copy AdHoc
    	if (message == null) {
	    	message = this.copyFile(DATA_FILE_PATH+"/bin/adhoc", "0755", R.raw.adhoc);
    	}
    	// copy AdHoc.conf
    	if (message == null) {
	    	message = this.copyFile(DATA_FILE_PATH+"/conf/adhoc.conf", "0644", R.raw.adhoc_conf);
    	}
    	// copy AdHoc.edify
    	if (message == null) {
	    	message = this.copyFile(DATA_FILE_PATH+"/conf/adhoc.edify", "0644", R.raw.adhoc_edify);
    	}
    	
    	
    	this.chmod(AdHocApplication.DATA_FILE_PATH+"/conf/", "0755");
    	
    	if (message == null) {
    		Log.d(MSG_TAG, "copy files to application folder complete");
    		return;
    	}
    	else {
    		Log.d(MSG_TAG, message);
    	}
    	
    }
    
    private String copyFile(String filename, String permission, int ressource) {
    	String result = this.copyFile(filename, ressource);
    	if (result != null) {
    		return result;
    	}
    	if (this.chmod(filename, permission) != true) {
    		result = "Can't change file-permission for '"+filename+"'!";
    	}
    	return result;
    }
    
    private String copyFile(String filename, int ressource) {
    	File outFile = new File(filename);
    	Log.d(MSG_TAG, "Copying file '"+filename+"' ...");
    	InputStream is = this.getResources().openRawResource(ressource);
    	byte buf[] = new byte[1024];
        int len;
        try {
        	OutputStream out = new FileOutputStream(outFile);
        	while((len = is.read(buf))>0) {
				out.write(buf,0,len);
			}
        	out.close();
        	is.close();
		} catch (IOException e) {
			return "Couldn't install file - "+filename+"!";
		}
		return null;
    }
    
    public String getIpAdress() {
    	//��ȡ��ǰ�ڵ�Ip��ַ
    	try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(MSG_TAG, ex.toString());
        }
        return null;   
    }  
    
    public boolean chmod(String file, String mode) {
    	if (AdHocNative.runCommand("chmod "+ mode + " " + file) == 0) {
    		return true;
    	}
    	return false;
    }
    
    
}
