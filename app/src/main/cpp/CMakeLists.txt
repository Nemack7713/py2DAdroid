cmake_minimum_required(VERSION 3.22.1)
project(py2d_host LANGUAGES CXX)

option(
    PY2D_ENABLE_PYBIND11
    "Enable the optional pybind11 compute bridge"
    OFF
)

option(
    PY2D_ENABLE_FREE_THREADED
    "Enable a free-threaded CPython runtime"
    OFF
)

if(PY2D_ENABLE_FREE_THREADED AND PY2D_ENABLE_PYBIND11)
    message(
        FATAL_ERROR
        "Free-threaded CPython + pybind11 is not an approved py2DAdroid configuration"
    )
endif()

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(
    py2d_host
    SHARED
    py2d_jni.cpp
)

target_compile_definitions(
    py2d_host
    PRIVATE
    PY2D_PYTHON_VERSION="${PY2D_PYTHON_VERSION}"
    PY2D_PYTHON_VARIANT="${PY2D_PYTHON_VARIANT}"
    PY2D_PYTHON_RUNTIME_ROOT="${PY2D_PYTHON_RUNTIME_ROOT}"
)
