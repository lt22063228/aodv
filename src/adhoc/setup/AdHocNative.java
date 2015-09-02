package adhoc.setup;

import android.util.Log;

public class AdHocNative {
	//AdHocNative类，JNI调用
	public static final String MSG_TAG = "AdHoc -> AdHocNative";
	//声明JNI函数
    public static native String getProp(String name);
    public static native int runCommand(String command);
    //加载编译好的JNI库
	static {
        try {
            Log.i(MSG_TAG, "Trying to load libjnitask.so");
            System.loadLibrary("jnitask");
        }
        catch (UnsatisfiedLinkError ule) {
            Log.e(MSG_TAG, "Could not load libjnitask.so");
        }
    }
    //调用JNI函数runCommand()，定义runRootCommand方法
    public static boolean runRootCommand(String command) {
		Log.d(MSG_TAG, "Root-Command ==> su -c \""+command+"\"");
		int returncode = runCommand("su -c \""+command+"\"");
    	if (returncode == 0) {
			return true;
		}
    	Log.d(MSG_TAG, "Root-Command error, return code: " + returncode);
		return false;
    }
}
