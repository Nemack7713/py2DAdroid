#include <jni.h>

extern "C" JNIEXPORT jstring JNICALL
Java_com_py2dadroid_app_nativebridge_CpythonNativeBridge_probeRuntime(
    JNIEnv* env,
    jobject
) {
    return env->NewStringUTF(
        "Verified direct CPython Android runtime is not wired yet"
    );
}
