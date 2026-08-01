cmake_minimum_required(VERSION 3.22.1)

project(kiroku_whisper LANGUAGES C CXX)

set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
set(WHISPER_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(WHISPER_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(WHISPER_BUILD_SERVER OFF CACHE BOOL "" FORCE)
set(WHISPER_ALL_WARNINGS OFF CACHE BOOL "" FORCE)
set(GGML_ALL_WARNINGS OFF CACHE BOOL "" FORCE)
set(GGML_NATIVE OFF CACHE BOOL "" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "" FORCE)
set(GGML_CPU_KLEIDIAI OFF CACHE BOOL "" FORCE)

add_subdirectory(third_party/whisper.cpp whisper-build EXCLUDE_FROM_ALL)

add_library(
    kiroku_whisper
    SHARED
    whisper_jni.cpp
)

find_library(log_library log)

target_compile_features(kiroku_whisper PRIVATE cxx_std_17)
target_link_libraries(
    kiroku_whisper
    PRIVATE
    whisper
    ${log_library}
)
