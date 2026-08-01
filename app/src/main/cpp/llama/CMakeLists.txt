add_library(
    kiroku_llama
    SHARED
    llama_jni.cpp
)

find_library(android_log_library log)

target_compile_features(kiroku_llama PRIVATE cxx_std_17)
target_link_libraries(
    kiroku_llama
    PRIVATE
    llama
    ${android_log_library}
)
