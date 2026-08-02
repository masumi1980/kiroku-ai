#include <jni.h>

#include <algorithm>
#include <memory>
#include <mutex>
#include <stdexcept>
#include <string>
#include <vector>

#include "llama.h"

namespace {

constexpr int32_t kContextSize = 4096;
constexpr int32_t kMaxGeneratedTokens = 512;
constexpr int32_t kRepeatPenaltyWindow = 64;
constexpr float kTemperature = 0.2F;
constexpr float kTopP = 0.9F;
constexpr float kRepeatPenalty = 1.1F;
constexpr uint32_t kSeed = 42;

using ModelPointer = std::unique_ptr<llama_model, decltype(&llama_model_free)>;
using ContextPointer = std::unique_ptr<llama_context, decltype(&llama_free)>;
using SamplerPointer = std::unique_ptr<llama_sampler, decltype(&llama_sampler_free)>;

struct NativeSession final {
    explicit NativeSession(llama_model * loaded_model)
        : model(loaded_model, llama_model_free) {}

    ModelPointer model;
    std::mutex mutex;
};

class JniString final {
public:
    JniString(JNIEnv * env, jstring value) : env_(env), value_(value) {
        chars_ = env_->GetStringUTFChars(value_, nullptr);
        if (chars_ == nullptr) {
            throw std::runtime_error("Unable to read Java string");
        }
    }

    ~JniString() {
        if (chars_ != nullptr) {
            env_->ReleaseStringUTFChars(value_, chars_);
        }
    }

    const char * get() const { return chars_; }

private:
    JNIEnv * env_;
    jstring value_;
    const char * chars_ = nullptr;
};

void ensure_backend_initialized() {
    static std::once_flag initialization_flag;
    std::call_once(initialization_flag, [] { llama_backend_init(); });
}

std::vector<llama_token> tokenize(const llama_vocab * vocab, const std::string & prompt) {
    const int32_t token_count = -llama_tokenize(
        vocab, prompt.c_str(), static_cast<int32_t>(prompt.size()), nullptr, 0, true, true);
    if (token_count <= 0) {
        throw std::runtime_error("Prompt could not be tokenized");
    }

    std::vector<llama_token> tokens(static_cast<size_t>(token_count));
    const int32_t result = llama_tokenize(
        vocab,
        prompt.c_str(),
        static_cast<int32_t>(prompt.size()),
        tokens.data(),
        static_cast<int32_t>(tokens.size()),
        true,
        true);
    if (result < 0) {
        throw std::runtime_error("Prompt tokenization failed");
    }
    tokens.resize(static_cast<size_t>(result));
    return tokens;
}

std::string token_piece(const llama_vocab * vocab, llama_token token) {
    std::vector<char> buffer(128);
    int32_t length = llama_token_to_piece(
        vocab, token, buffer.data(), static_cast<int32_t>(buffer.size()), 0, true);
    if (length < 0) {
        buffer.resize(static_cast<size_t>(-length));
        length = llama_token_to_piece(
            vocab, token, buffer.data(), static_cast<int32_t>(buffer.size()), 0, true);
    }
    if (length < 0) {
        throw std::runtime_error("Generated token could not be decoded");
    }
    return std::string(buffer.data(), static_cast<size_t>(length));
}

std::string generate(NativeSession * session, const char * prompt_text) {
    if (session == nullptr) {
        throw std::runtime_error("Llama model is not loaded");
    }
    std::lock_guard<std::mutex> lock(session->mutex);
    const llama_vocab * vocab = llama_model_get_vocab(session->model.get());
    const auto prompt_tokens = tokenize(vocab, prompt_text);
    if (prompt_tokens.size() >= static_cast<size_t>(kContextSize)) {
        throw std::invalid_argument("Prompt exceeds the supported context size");
    }

    llama_context_params context_params = llama_context_default_params();
    context_params.n_ctx = kContextSize;
    context_params.n_batch = kContextSize;
    context_params.no_perf = true;
    ContextPointer context(
        llama_init_from_model(session->model.get(), context_params),
        llama_free);
    if (!context) {
        throw std::runtime_error("Llama context could not be initialized");
    }

    SamplerPointer sampler(
        llama_sampler_chain_init(llama_sampler_chain_default_params()),
        llama_sampler_free);
    if (!sampler) {
        throw std::runtime_error("Llama sampler could not be initialized");
    }
    llama_sampler_chain_add(
        sampler.get(),
        llama_sampler_init_penalties(kRepeatPenaltyWindow, kRepeatPenalty, 0.0F, 0.0F));
    llama_sampler_chain_add(sampler.get(), llama_sampler_init_temp(kTemperature));
    llama_sampler_chain_add(sampler.get(), llama_sampler_init_top_p(kTopP, 1));
    llama_sampler_chain_add(sampler.get(), llama_sampler_init_dist(kSeed));

    std::string output;
    llama_batch batch = llama_batch_get_one(
        const_cast<llama_token *>(prompt_tokens.data()),
        static_cast<int32_t>(prompt_tokens.size()));
    int32_t processed_tokens = static_cast<int32_t>(prompt_tokens.size());
    llama_token generated_token = 0;

    for (int32_t generated = 0;
         generated < kMaxGeneratedTokens && processed_tokens < kContextSize;
         ++generated) {
        if (llama_decode(context.get(), batch) != 0) {
            throw std::runtime_error("Llama token evaluation failed");
        }
        generated_token = llama_sampler_sample(sampler.get(), context.get(), -1);
        if (llama_vocab_is_eog(vocab, generated_token)) {
            break;
        }
        output += token_piece(vocab, generated_token);
        batch = llama_batch_get_one(&generated_token, 1);
        ++processed_tokens;
    }

    return output;
}

NativeSession * load_model(const char * model_path) {
    ensure_backend_initialized();
    llama_model_params model_params = llama_model_default_params();
    model_params.n_gpu_layers = 0;
    ModelPointer model(llama_model_load_from_file(model_path, model_params), llama_model_free);
    if (!model) {
        throw std::runtime_error("GGUF model could not be loaded");
    }
    return new NativeSession(model.release());
}

void throw_java_exception(
    JNIEnv * env,
    const char * message,
    const char * class_name = "java/lang/IllegalStateException") {
    jclass exception_class = env->FindClass(class_name);
    if (exception_class != nullptr) {
        env->ThrowNew(exception_class, message);
    }
}

}  // namespace

extern "C" JNIEXPORT jstring JNICALL
Java_jp_co_kirokuai_app_ai_llm_LlamaNativeBridge_generateNative(
    JNIEnv * env,
    jobject,
    jlong handle,
    jstring prompt) {
    if (handle == 0 || prompt == nullptr) {
        throw_java_exception(env, "Loaded model and prompt are required");
        return nullptr;
    }

    try {
        const JniString native_prompt(env, prompt);
        auto * session = reinterpret_cast<NativeSession *>(handle);
        const std::string result = generate(session, native_prompt.get());
        return env->NewStringUTF(result.c_str());
    } catch (const std::bad_alloc &) {
        throw_java_exception(env, "Out of memory while generating text");
    } catch (const std::invalid_argument & error) {
        throw_java_exception(env, error.what(), "java/lang/IllegalArgumentException");
    } catch (const std::exception & error) {
        throw_java_exception(env, error.what());
    } catch (...) {
        throw_java_exception(env, "Unknown native text generation error");
    }
    return nullptr;
}

extern "C" JNIEXPORT jlong JNICALL
Java_jp_co_kirokuai_app_ai_llm_LlamaNativeBridge_loadNative(
    JNIEnv * env,
    jobject,
    jstring model_path) {
    if (model_path == nullptr) {
        throw_java_exception(env, "Model path is required");
        return 0;
    }
    try {
        const JniString native_model_path(env, model_path);
        return reinterpret_cast<jlong>(load_model(native_model_path.get()));
    } catch (const std::bad_alloc &) {
        throw_java_exception(env, "Out of memory while loading model");
    } catch (const std::exception & error) {
        throw_java_exception(env, error.what());
    } catch (...) {
        throw_java_exception(env, "Unknown native model loading error");
    }
    return 0;
}

extern "C" JNIEXPORT void JNICALL
Java_jp_co_kirokuai_app_ai_llm_LlamaNativeBridge_closeNative(
    JNIEnv *,
    jobject,
    jlong handle) {
    delete reinterpret_cast<NativeSession *>(handle);
}
