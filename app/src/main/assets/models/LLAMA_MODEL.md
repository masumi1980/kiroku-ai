# Bundled llama.cpp model

Kiroku AI bundles exactly one GGUF model for offline text generation:

- Runtime file: `qwen3-4b-instruct-q4_k_m.gguf`
- Bundled asset parts: `qwen3-4b-instruct-q4_k_m.gguf.part0` and `.part1`
- Source: `Qwen/Qwen3-4B-GGUF` (`Qwen3-4B-Q4_K_M.gguf`)
- Quantization: `Q4_K_M`
- SHA-256: `7485fe6f11af29433bc51cab58009521f205840f5b4ae3a32fa7f92e8534fdf5`
- License: Apache License 2.0

The asset parts are joined into one GGUF file in app-private storage and verified before llama.cpp loads it. Splitting is only a source-control transport detail; the runtime model is the original, byte-identical GGUF.
No download or model-selection path is implemented.
