/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class adhoc_setup_Jni */

#ifndef _Included_adhoc_setup_Jni
#define _Included_adhoc_setup_Jni
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_wmn_Jni
 * Method:    runCommand
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_adhoc_setup_AdHocNative_runCommand
  (JNIEnv * env, jclass class, jstring command);

/*
 * Class:     com_wmn_Jni
 * Method:    getProp
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_adhoc_setup_AdHocNative_getProp
  (JNIEnv * env, jclass class, jstring name);

#ifdef __cplusplus
}
#endif
#endif
