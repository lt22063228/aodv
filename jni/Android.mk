LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE    := jnitask
LOCAL_SRC_FILES := adhoc_setup_AdHocNative.c 

include $(BUILD_SHARED_LIBRARY)
