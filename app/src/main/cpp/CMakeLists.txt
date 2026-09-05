cmake_minimum_required(VERSION 3.22.1)
project(py2d_host LANGUAGES CXX)

option(PY2D_ENABLE_PYBIND11 "Enable the optional pybind11 compute bridge" OFF)
option(PY2D_ENABLE_FREE_THREADED "Enable a free-threaded CPython runtime" OFF)

if(PY2D_ENABLE_FREE_THREADED AND PY2D_ENABLE_PYBIND11)
    message(FATAL_ERROR "Free-threaded CPython + pybind11 is not an approved py2DAdroid configuration")
endif()

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(py2d_host SHARED py2d_jni.cpp)

target_compile_definitions(
    py2d_host
    PRIVATE
    PY2D_PYTHON_VERSION="${PY2D_PYTHON_VERSION}"
    PY2D_PYTHON_VARIANT="${PY2D_PYTHON_VARIANT}"
)

if(PY2D_PYTHON_RUNTIME_ROOT)
    set(PY2D_PYTHON_INCLUDE "${PY2D_PYTHON_RUNTIME_ROOT}/include/python${PY2D_PYTHON_VERSION}")
    set(PY2D_PYTHON_LIBRARY "${PY2D_PYTHON_RUNTIME_ROOT}/lib/libpython${PY2D_PYTHON_VERSION}.so")

    if(NOT EXISTS "${PY2D_PYTHON_INCLUDE}/Python.h")
        message(FATAL_ERROR "Python.h not found in verified CPython runtime")
    endif()
    if(NOT EXISTS "${PY2D_PYTHON_LIBRARY}")
        message(FATAL_ERROR "libpython not found in verified CPython runtime")
    endif()

    add_library(py2d_python SHARED IMPORTED)
    set_target_properties(py2d_python PROPERTIES IMPORTED_LOCATION "${PY2D_PYTHON_LIBRARY}")
    target_include_directories(py2d_host PRIVATE "${PY2D_PYTHON_INCLUDE}")
    target_link_libraries(py2d_host PRIVATE py2d_python)
    target_compile_definitions(py2d_host PRIVATE PY2D_HAS_CPYTHON=1)
else()
    target_compile_definitions(py2d_host PRIVATE PY2D_HAS_CPYTHON=0)
endif()
