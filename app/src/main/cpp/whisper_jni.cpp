#include <jni.h>

#include <algorithm>
#include <cstdint>
#include <cstring>
#include <fstream>
#include <memory>
#include <stdexcept>
#include <string>
#include <thread>
#include <vector>

#include "whisper.h"

namespace {

constexpr std::size_t kRiffHeaderSize = 12;
constexpr std::size_t kChunkHeaderSize = 8;
constexpr std::size_t kWaveFormatSize = 16;
constexpr uint16_t kPcmFormat = 1;
constexpr uint16_t kFloatFormat = 3;
constexpr uint16_t kPcmBitsPerSample = 16;
constexpr uint16_t kFloatBitsPerSample = 32;
constexpr int kMaximumThreadCount = 4;

struct WaveFormat {
    uint16_t format;
    uint16_t channels;
    uint32_t sample_rate;
    uint16_t block_align;
    uint16_t bits_per_sample;
};

uint16_t read_u16(const uint8_t * data) {
    return static_cast<uint16_t>(data[0]) |
           static_cast<uint16_t>(data[1] << 8);
}

uint32_t read_u32(const uint8_t * data) {
    return static_cast<uint32_t>(data[0]) |
           (static_cast<uint32_t>(data[1]) << 8) |
           (static_cast<uint32_t>(data[2]) << 16) |
           (static_cast<uint32_t>(data[3]) << 24);
}

bool has_id(const uint8_t * data, const char * id) {
    return std::memcmp(data, id, 4) == 0;
}

std::vector<uint8_t> read_file(const std::string & path) {
    std::ifstream stream(path, std::ios::binary | std::ios::ate);
    if (!stream) {
        throw std::runtime_error("Unable to open WAV file");
    }

    const std::streamsize size = stream.tellg();
    if (size <= 0) {
        throw std::runtime_error("WAV file is empty");
    }

    stream.seekg(0, std::ios::beg);
    std::vector<uint8_t> data(static_cast<std::size_t>(size));
    if (!stream.read(reinterpret_cast<char *>(data.data()), size)) {
        throw std::runtime_error("Unable to read WAV file");
    }
    return data;
}

std::vector<float> decode_wave(const std::string & path) {
    const std::vector<uint8_t> bytes = read_file(path);
    if (bytes.size() < kRiffHeaderSize ||
        !has_id(bytes.data(), "RIFF") ||
        !has_id(bytes.data() + 8, "WAVE")) {
        throw std::runtime_error("Invalid WAV header");
    }

    WaveFormat wave_format{};
    bool has_format = false;
    const uint8_t * audio_data = nullptr;
    std::size_t audio_size = 0;

    std::size_t offset = kRiffHeaderSize;
    while (offset + kChunkHeaderSize <= bytes.size()) {
        const uint8_t * chunk = bytes.data() + offset;
        const std::size_t chunk_size = read_u32(chunk + 4);
        const std::size_t data_offset = offset + kChunkHeaderSize;
        if (chunk_size > bytes.size() - data_offset) {
            throw std::runtime_error("Invalid WAV chunk size");
        }

        if (has_id(chunk, "fmt ")) {
            if (chunk_size < kWaveFormatSize) {
                throw std::runtime_error("Invalid WAV format chunk");
            }
            wave_format = {
                read_u16(bytes.data() + data_offset),
                read_u16(bytes.data() + data_offset + 2),
                read_u32(bytes.data() + data_offset + 4),
                read_u16(bytes.data() + data_offset + 12),
                read_u16(bytes.data() + data_offset + 14),
            };
            has_format = true;
        } else if (has_id(chunk, "data")) {
            audio_data = bytes.data() + data_offset;
            audio_size = chunk_size;
        }

        const std::size_t padded_size = chunk_size + (chunk_size % 2);
        if (padded_size > bytes.size() - data_offset) {
            break;
        }
        offset = data_offset + padded_size;
    }

    if (!has_format || audio_data == nullptr || audio_size == 0) {
        throw std::runtime_error("WAV file has no audio data");
    }
    if (wave_format.channels == 0 || wave_format.block_align == 0) {
        throw std::runtime_error("Invalid WAV channel configuration");
    }
    if (wave_format.sample_rate != WHISPER_SAMPLE_RATE) {
        throw std::runtime_error("WAV sample rate must be 16000 Hz");
    }

    const bool is_pcm16 = wave_format.format == kPcmFormat &&
                          wave_format.bits_per_sample == kPcmBitsPerSample;
    const bool is_float32 = wave_format.format == kFloatFormat &&
                            wave_format.bits_per_sample == kFloatBitsPerSample;
    if (!is_pcm16 && !is_float32) {
        throw std::runtime_error("WAV must contain PCM 16-bit or float 32-bit audio");
    }

    const std::size_t frame_count = audio_size / wave_format.block_align;
    if (frame_count == 0) {
        throw std::runtime_error("WAV file has no complete audio frames");
    }

    const std::size_t bytes_per_sample = wave_format.bits_per_sample / 8;
    std::vector<float> samples(frame_count);
    for (std::size_t frame = 0; frame < frame_count; ++frame) {
        float mixed_sample = 0.0f;
        for (uint16_t channel = 0; channel < wave_format.channels; ++channel) {
            const std::size_t sample_offset =
                frame * wave_format.block_align + channel * bytes_per_sample;
            if (is_pcm16) {
                const int16_t value = static_cast<int16_t>(read_u16(audio_data + sample_offset));
                mixed_sample += static_cast<float>(value) / 32768.0f;
            } else {
                float value;
                std::memcpy(&value, audio_data + sample_offset, sizeof(float));
                mixed_sample += value;
            }
        }
        samples[frame] = std::clamp(
            mixed_sample / static_cast<float>(wave_format.channels),
            -1.0f,
            1.0f
        );
    }
    return samples;
}

class JniString {
public:
    JniString(JNIEnv * env, jstring value) : env_(env), value_(value) {
        chars_ = env_->GetStringUTFChars(value_, nullptr);
        if (chars_ == nullptr) {
            throw std::runtime_error("Unable to read JNI string");
        }
    }

    ~JniString() {
        if (chars_ != nullptr) {
            env_->ReleaseStringUTFChars(value_, chars_);
        }
    }

    JniString(const JniString &) = delete;
    JniString & operator=(const JniString &) = delete;

    const char * get() const {
        return chars_;
    }

private:
    JNIEnv * env_;
    jstring value_;
    const char * chars_ = nullptr;
};

void throw_native_error(JNIEnv * env, const std::string & message) {
    jclass exception_class = env->FindClass("java/lang/IllegalStateException");
    if (exception_class != nullptr) {
        env->ThrowNew(exception_class, message.c_str());
    }
}

std::string transcribe(const std::string & model_path, const std::string & audio_path) {
    whisper_context_params context_params = whisper_context_default_params();
    context_params.use_gpu = false;

    std::unique_ptr<whisper_context, decltype(&whisper_free)> context(
        whisper_init_from_file_with_params(model_path.c_str(), context_params),
        whisper_free
    );
    if (!context) {
        throw std::runtime_error("Unable to initialize Whisper model");
    }

    const std::vector<float> samples = decode_wave(audio_path);
    whisper_full_params params = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    params.language = "en";
    params.translate = false;
    params.no_context = true;
    params.print_progress = false;
    params.print_realtime = false;
    params.print_timestamps = false;
    const unsigned int hardware_threads = std::thread::hardware_concurrency();
    params.n_threads = static_cast<int>(std::min(
        hardware_threads == 0 ? 1U : hardware_threads,
        static_cast<unsigned int>(kMaximumThreadCount)
    ));

    if (whisper_full(context.get(), params, samples.data(), samples.size()) != 0) {
        throw std::runtime_error("Whisper transcription failed");
    }

    std::string transcript;
    const int segment_count = whisper_full_n_segments(context.get());
    for (int index = 0; index < segment_count; ++index) {
        const char * segment = whisper_full_get_segment_text(context.get(), index);
        if (segment != nullptr) {
            transcript += segment;
        }
    }
    return transcript;
}

}  // namespace

extern "C" JNIEXPORT jstring JNICALL
Java_jp_co_kirokuai_app_ai_speech_WhisperNativeBridge_transcribeNative(
    JNIEnv * env,
    jobject,
    jstring model_path,
    jstring audio_path
) {
    if (model_path == nullptr || audio_path == nullptr) {
        throw_native_error(env, "Model and audio paths are required");
        return nullptr;
    }

    try {
        const JniString model(env, model_path);
        const JniString audio(env, audio_path);
        const std::string result = transcribe(model.get(), audio.get());
        return env->NewStringUTF(result.c_str());
    } catch (const std::exception & error) {
        throw_native_error(env, error.what());
        return nullptr;
    }
}
