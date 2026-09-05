#include <jni.h>

#include <cstdlib>
#include <mutex>
#include <string>

#if PY2D_HAS_CPYTHON
#include <Python.h>
#endif

namespace {

std::mutex runtime_mutex;

#if PY2D_HAS_CPYTHON
bool initialized = false;

std::string python_error() {
    if (!PyErr_Occurred()) {
        return "Unknown Python error";
    }

    PyObject* type = nullptr;
    PyObject* value = nullptr;
    PyObject* traceback = nullptr;
    PyErr_Fetch(&type, &value, &traceback);
    PyErr_NormalizeException(&type, &value, &traceback);

    std::string message = "Python error";
    PyObject* text = value != nullptr ? PyObject_Str(value) : nullptr;
    if (text != nullptr) {
        const char* utf8 = PyUnicode_AsUTF8(text);
        if (utf8 != nullptr) {
            message = utf8;
        }
        Py_DECREF(text);
    }

    Py_XDECREF(type);
    Py_XDECREF(value);
    Py_XDECREF(traceback);
    return message;
}
#endif

std::string to_utf8(JNIEnv* env, jstring value) {
    if (value == nullptr) {
        return {};
    }
    const char* chars = env->GetStringUTFChars(value, nullptr);
    if (chars == nullptr) {
        return {};
    }
    std::string result(chars);
    env->ReleaseStringUTFChars(value, chars);
    return result;
}

jstring as_jstring(JNIEnv* env, const std::string& value) {
    return env->NewStringUTF(value.c_str());
}

}  // namespace

extern "C" JNIEXPORT jstring JNICALL
Java_com_py2dadroid_app_nativebridge_CpythonNativeBridge_initialize(
    JNIEnv* env,
    jobject,
    jstring python_home,
    jstring cache_dir
) {
    std::lock_guard<std::mutex> lock(runtime_mutex);

#if !PY2D_HAS_CPYTHON
    return as_jstring(
        env,
        "Verified direct CPython Android runtime was not packaged into this build"
    );
#else
    if (initialized) {
        return as_jstring(env, "");
    }

    const std::string home = to_utf8(env, python_home);
    const std::string cache = to_utf8(env, cache_dir);
    if (home.empty()) {
        return as_jstring(env, "Python home is empty");
    }

    if (!cache.empty()) {
        setenv("TMPDIR", cache.c_str(), 1);
    }

    PyConfig config;
    PyConfig_InitPythonConfig(&config);

    char executable[] = "py2DAdroid";
    char* argv[] = {executable};

    PyStatus status = PyConfig_SetBytesArgv(&config, 1, argv);
    if (PyStatus_Exception(status)) {
        const std::string message =
            status.err_msg != nullptr
                ? status.err_msg
                : "PyConfig_SetBytesArgv failed";
        PyConfig_Clear(&config);
        return as_jstring(env, message);
    }

    status = PyConfig_SetBytesString(&config, &config.home, home.c_str());
    if (PyStatus_Exception(status)) {
        const std::string message =
            status.err_msg != nullptr
                ? status.err_msg
                : "Unable to configure Python home";
        PyConfig_Clear(&config);
        return as_jstring(env, message);
    }

    status = Py_InitializeFromConfig(&config);
    PyConfig_Clear(&config);
    if (PyStatus_Exception(status)) {
        return as_jstring(
            env,
            status.err_msg != nullptr
                ? status.err_msg
                : "Py_InitializeFromConfig failed"
        );
    }

    initialized = true;
    return as_jstring(env, "");
#endif
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_py2dadroid_app_nativebridge_CpythonNativeBridge_evaluate(
    JNIEnv* env,
    jobject,
    jstring expression
) {
    std::lock_guard<std::mutex> lock(runtime_mutex);

#if !PY2D_HAS_CPYTHON
    return as_jstring(env, "ERROR:CPython runtime is unavailable");
#else
    if (!initialized) {
        return as_jstring(env, "ERROR:CPython runtime is not initialized");
    }

    const std::string code = to_utf8(env, expression);
    PyGILState_STATE gil = PyGILState_Ensure();

    PyObject* globals = PyDict_New();
    if (globals == nullptr) {
        const std::string error = python_error();
        PyGILState_Release(gil);
        return as_jstring(env, "ERROR:" + error);
    }

    PyDict_SetItemString(globals, "__builtins__", PyEval_GetBuiltins());
    PyObject* result = PyRun_StringFlags(
        code.c_str(),
        Py_eval_input,
        globals,
        globals,
        nullptr
    );

    if (result == nullptr) {
        const std::string error = python_error();
        Py_DECREF(globals);
        PyGILState_Release(gil);
        return as_jstring(env, "ERROR:" + error);
    }

    PyObject* text = PyObject_Str(result);
    std::string value;
    if (text == nullptr) {
        value = "ERROR:" + python_error();
    } else {
        const char* utf8 = PyUnicode_AsUTF8(text);
        value =
            utf8 != nullptr
                ? utf8
                : "ERROR:Unable to encode Python result";
        Py_DECREF(text);
    }

    Py_DECREF(result);
    Py_DECREF(globals);
    PyGILState_Release(gil);
    return as_jstring(env, value);
#endif
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_py2dadroid_app_nativebridge_CpythonNativeBridge_shutdown(
    JNIEnv* env,
    jobject
) {
    std::lock_guard<std::mutex> lock(runtime_mutex);

#if !PY2D_HAS_CPYTHON
    return as_jstring(env, "");
#else
    if (!initialized) {
        return as_jstring(env, "");
    }

    const int result = Py_FinalizeEx();
    initialized = false;
    return as_jstring(
        env,
        result == 0 ? "" : "Py_FinalizeEx reported an error"
    );
#endif
}
