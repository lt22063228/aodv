package adhoc.setup;

import android.util.Log;

public class AdHocNative {
	//AdHocNative�࣬JNI����
	public static final String MSG_TAG = "AdHoc -> AdHocNative";
	//����JNI����
    public static native String getProp(String name);
    public static native int runCommand(String command);
    //���ر���õ�JNI��
	static {
        try {
            Log.i(MSG_TAG, "Trying to load libjnitask.so");
            System.loadLibrary("jnitask");
        }
        catch (UnsatisfiedLinkError ule) {
            Log.e(MSG_TAG, "Could not load libjnitask.so");
        }
    }
    //����JNI����runCommand()������runRootCommand����
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
