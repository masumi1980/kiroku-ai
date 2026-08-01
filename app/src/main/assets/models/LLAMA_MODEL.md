# Bundled llama.cpp model

Kiroku AI bundles exactly one GGUF model for offline text generation:

- File: `stories15M-q4_0.gguf`
- Source: `ggml-org/tiny-llamas` (`stories15M-q4_0.gguf`)
- SHA-256: `6151b1929d7f5aa3385d9ddef3393e55587c0a55de661562322bc51dfda93a04`
- License: Apache License 2.0

The model is copied from assets into app-private storage and verified before llama.cpp loads it.
No download or model-selection path is implemented.
