package adhoc.constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import android.R.integer;

public class Constants {
	
	//Valid node address interval
	public static final int MAX_VALID_NODE_ADDRESS = 254;
	public static final int MIN_VALID_NODE_ADDRESS = 1;
	public static final int BROADCAST_ADDRESS = 255;
	
	//Broadcast ID
	public static final int MAX_BROADCAST_ID = Integer.MAX_VALUE;
	public static final int FIRST_BROADCAST_ID = 0;
	
	//Sequence Numbers
	public static final int MAX_SEQUENCE_NUMBER = Integer.MAX_VALUE;
    public static final int INVALID_SEQUENCE_NUMBER = -1;
    public static final int UNKNOWN_SEQUENCE_NUMBER = 0;
    public static final int FIRST_SEQUENCE_NUMBER = 1;
    public static final int SEQUENCE_NUMBER_INTERVAL = (Integer.MAX_VALUE / 2);
    
    //Package Identifier
    public static final int MAX_PACKAGE_IDENTIFIER = Integer.MAX_VALUE;
    public static final int FIRST_PACKAGE_IDENTIFIER = 1;
	
	// user package max size equivalent 54kb
	public static final int MAX_PACKAGE_SIZE = 54000;
    
	// user package type
	public static final byte USER_DATA_PACKET_PDU = 0;
	public static final byte USER_BROADCAST_DATA_PACKET_PDU = 7;
	
	// AODV PDU types
	public static final byte RERR_PDU = 1;
	public static final byte RREP_PDU = 2;
	public static final byte RREQ_PDU = 3;
	public static final byte RREQ_FAILURE_PDU = 4;
	public static final byte FORWARD_ROUTE_CREATED = 5;
	
	// hello package type
	public static final byte HELLO_PDU = 6; 
	
	//alive time for a route 
	public static final int ROUTE_ALIVETIME = 10000;
	
	//the time to wait between each hello message sent
	public static final int BROADCAST_INTERVAL = 1000;
	
	public static final int MAX_NUMBER_OF_RREQ_RETRIES = 2;
	
	//the amount of time to store a RREQ entry before the entry dies
	public static final int PATH_DESCOVERY_TIME = 3000;
	
	public static final int DHCP_RECEIVE_PORT = 8765;
	public static final int AODV_RECEIV_PORT = 8766;

	public static final int DIRLINK_LISTVIEW = 0;
	public static final int ALLLINK_LISTVIEW = 1;
	
	public static final int DHCP_TEST = 10;
	public static final int DHCP_REQUEST = 11;
	public static final int DHCP_REQUEST_REPLY = 12;
	public static final int DHCP_CONFIRM = 13;
	public static final int DHCP_CONFIRM_REPLY = 14;
	public static final int DHCP_RELEASE = 15;
	public static final int DHCP_RELEASE_REPLY = 16;
	public static final int DIRLINK_CONFIRM = 17;
	public static final int DIRLINK_CONFIRM_REPLY = 18;
	public static final int DIRLINK_SEARCH = 19;
	public static final int DIRLINK_SEARCH_REPLY = 20;
	
	//Add by Eric
	public static final int BANDWIDTH_TEST = 30;
	public static final int BANDWIDTH_TEST_ING_D = 31;
	public static final int BANDWIDTH_TEST_ING_U = 32;
	public static final int BANDWIDTH_TEST_CONFIRM =33;
	public static final int BANDWIDTH_TEST_START_D =34;
	public static final int BANDWIDTH_TEST_START_U =35;
	public static final int BANDWIDTH_TEST_END_U =36;
	public static final int BANDWIDTH_TEST_END =37;
	
	
	public static final int GIVE_PHONE_INFO = 40;
	//Add ends
	
	
//	public static final ArrayList<Boolean> ipPond = new ArrayList<Boolean>();
	//Boolean类型List，存储AdHoc模式启动过程中，从网络中获取的可用IP
	public static final ArrayList<Boolean> receivedIp = new ArrayList<Boolean>();
	//Boolean类型List，标记是否从某个IP收到消息的回复
	public static final ArrayList<Boolean> ackList = new ArrayList<Boolean>();
	//Integer类型List，存储当前节点的直连节点IP
	public static final ArrayList<Integer> dirlinkList = new ArrayList<Integer>();
//	public static final ArrayList<Integer> alllinkList = new ArrayList<Integer>();
	//byte数组，256个bit，用来存储网络中所有的节点IP
	public static final byte[] bitArray = new byte[32];

	//Add by Eric 
	
	public static String phoneInfo;
	public static ArrayList<String> phoneInfoList = new ArrayList<String>();
	//记录带宽测试是否正在使用
	public static boolean bandwidthTestIsUsed = false;
	//String 记录 本节点的下载带宽+上传带宽
	public static final HashMap<Integer, Double> mapBandwidth_U = new HashMap<Integer, Double>();
	public static final HashMap<Integer, Double> mapBandwidth_D = new HashMap<Integer, Double>();
	public static double bandwidthTestSize_U = 0;
	public static double bandwidthTestSize_D = 0;
	public static long startTime_D = 0;
	public static long endTime_D =0;
	public static long startTime_U =0;
	public static long endTime_U =0;
	
	public static long lastOverhearTime =0;
	public static long nowOverhearTime =0;
	//Add ends
	
	public static int byteArraytoInt(byte[] b) {
	    int value = 0; 
	    for (int i = 0; i < 4; i++) { 
	        value = (value << 8) | (b[i] & 0xFF); 
	    } 
	    return value; 
	}
	
	public static byte[] intToByteArray(int i) {
		byte[] result = new byte[4];
		result[0] = (byte)((i >> 24)& 0xFF);
		result[1] = (byte)((i >> 16)& 0xFF);
		result[2] = (byte)((i >> 8)& 0xFF);
		result[3] = (byte)(i & 0xFF);
		return result;
	}
	
	public static class BitArray {
		//静态BitArray类，对32为byte数组bitArray进行位操作
		private static byte[] zero = {(byte)0xfe,(byte)0xfd,(byte)0xfb,(byte)0xf7,(byte)0xef,(byte)0xdf,(byte)0xbf,(byte)0x7f};
		private static byte[] one = {(byte)0x01,(byte)0x02,(byte)0x04,(byte)0x08,(byte)0x10,(byte)0x20,(byte)0x40,(byte)0x80};
		//初始化每一位为0
		public static void init() {
			for(int i = 0; i < bitArray.length; i++) {
				bitArray[i] = 0x0;
			}
		}
		//设置bitArray数组256个bit位中某一位的值
		public static void set(int index, boolean value) {
			int byteIndex = index/8;
			int cursor = index%8;
			byte tmp = bitArray[byteIndex];
			if(value) {
				tmp = setOne(tmp, cursor);
			}
			else {
				tmp = setZero(tmp, cursor);
			}
			bitArray[byteIndex] = tmp;
		}
		//获取bitArray数组256个bit位中某一位的值
		public static boolean get(int index) {
			int byteIndex = index/8;
			int cursor = index%8;
			byte tmp = bitArray[byteIndex];
			int result = (byte)(tmp >> cursor) & 0x1;
			if(result == 1) {
				return true;
			}
			else {
				return false;
			}
		}
		//将bitArray和参数byte数组b按位或
		public static void join(byte[] b) {
			for(int i = 0; i < 32; i++) {
				byte tmp = 0x0;
				tmp = (byte)((byte) bitArray[i] | (byte) b[i]);
				bitArray[i] = tmp;
			}
		}
		
		private static byte setOne(byte b, int index) {
			return (byte) ((byte)b | one[index]);
		}
		
		private static byte setZero(byte b, int index) {
			return (byte) ((byte)b & zero[index]);
		}
	}
	
}
