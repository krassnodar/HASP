/*
 *
 * rus.c: LDK Java native interface used by RUS
 * Copyright (C) 2021 Thales Group. All rights reserved.
 *
 */

#include <jni.h>
#include <hasp_api.h>

#define METHODNAME(x)   Java_com_safenet_patch_Product_##x

// please replace vendor code with your vendor code
unsigned char vendorCode[] = 
	"AzIceaqfA1hX5wS+M8cGnYh5ceevUnOZIzJBbXFD6dgf3tBkb9cvUF/Tkd/iKu2fsg9wAysYKw7RMA"
	"sVvIp4KcXle/v1RaXrLVnNBJ2H2DmrbUMOZbQUFXe698qmJsqNpLXRA367xpZ54i8kC5DTXwDhfxWT"
	"OZrBrh5sRKHcoVLumztIQjgWh37AzmSd1bLOfUGI0xjAL9zJWO3fRaeB0NS2KlmoKaVT5Y04zZEc06"
	"waU2r6AU2Dc4uipJqJmObqKM+tfNKAS0rZr5IudRiC7pUwnmtaHRe5fgSI8M7yvypvm+13Wm4Gwd4V"
	"nYiZvSxf8ImN3ZOG9wEzfyMIlH2+rKPUVHI+igsqla0Wd9m7ZUR9vFotj1uYV0OzG7hX0+huN2E/Id"
	"gLDjbiapj1e2fKHrMmGFaIvI6xzzJIQJF9GiRZ7+0jNFLKSyzX/K3JAyFrIPObfwM+y+zAgE1sWcZ1"
	"YnuBhICyRHBhaJDKIZL8MywrEfB2yF+R3k9wFG1oN48gSLyfrfEKuB/qgNp+BeTruWUk0AwRE9XVMU"
	"uRbjpxa4YA67SKunFEgFGgUfHBeHJTivvUl0u4Dki1UKAT973P+nXy2O0u239If/kRpNUVhMg8kpk7"
	"s8i6Arp7l/705/bLCx4kN5hHHSXIqkiG9tHdeNV8VYo5+72hgaCx3/uVoVLmtvxbOIvo120uTJbuLV"
	"TvT8KtsOlb3DxwUrwLzaEMoAQAFk6Q9bNipHxfkRQER4kR7IYTMzSoW5mxh3H9O8Ge5BqVeYMEW36q"
	"9wnOYfxOLNw6yQMf8f9sJN4KhZty02xm707S7VEfJJ1KNq7b5pP/3RjE0IKtB2gE6vAPRvRLzEohu0"
	"m7q1aUp8wAvSiqjZy7FLaTtLEApXYvLvz6PEJdj4TegCZugj7c8bIOEqLXmloZ6EgVnjQ7/ttys7VF"
	"ITB3mazzFiyQuKf4J6+b/a/Y";

// getinfo
jbyteArray METHODNAME(getinfo)(JNIEnv *env, jclass obj, jstring scope,
                jstring format, jintArray status)
{
    char *native_info=0;
    const char *ptrByte_format = (*env)->GetStringUTFChars(env, format, 0);
    const char *ptrByte_scope = (*env)->GetStringUTFChars(env, scope, 0);
    jint *ptrInt_status = (*env)->GetIntArrayElements(env, status, 0);

    jbyte *ptrByte_info = 0;
    jbyteArray java_info = 0;

    int result = 0;

    (void)obj;

    result = hasp_get_info((const char *)ptrByte_scope,
                  (const char *)ptrByte_format, 
                  (hasp_vendor_code_t)vendorCode, &native_info);  

    ptrInt_status[0] = result;

    if (native_info) {
        java_info = (*env)->NewByteArray(env, (jsize)strlen(native_info));
        ptrByte_info = (*env)->GetByteArrayElements(env, java_info, 0);
        memcpy(ptrByte_info, native_info, strlen(native_info));
        hasp_free(native_info);
        (*env)->ReleaseByteArrayElements(env, java_info, ptrByte_info, 0);
    }

    (*env)->ReleaseStringUTFChars(env, scope, ptrByte_scope);
    (*env)->ReleaseStringUTFChars(env, format, ptrByte_format);
    (*env)->ReleaseIntArrayElements(env, status, ptrInt_status, 0);
  
    return java_info; 
}

// update
jstring METHODNAME(update)(JNIEnv *env, jclass obj, jstring update_data, jintArray status)
{
    const char *ptr_update_data = 
            (*env)->GetStringUTFChars(env, update_data, 0);
    jint *ptrInt_status = (*env)->GetIntArrayElements(env, status, 0);
    char *native_ack_data = 0;
    jstring java_ack_data = 0;

    hasp_status_t result = 0;

    (void)obj;

    result = hasp_update(ptr_update_data, &native_ack_data);

    ptrInt_status[0] = result;

    if (native_ack_data) {
        java_ack_data = (*env)->NewStringUTF(env, native_ack_data);
        hasp_free(native_ack_data);
    }

    (*env)->ReleaseIntArrayElements(env, status, ptrInt_status, 0);
    (*env)->ReleaseStringUTFChars(env, update_data, ptr_update_data);
  
    return java_ack_data;
}

extern int HASP_CALLCONV hasp_onload(void* vm, void* res);
extern void HASP_CALLCONV hasp_onunload(void* vm, void* res);

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* res)
{
    return hasp_onload(vm, res);
}

JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* res)
{
    hasp_onunload(vm, res);
}
