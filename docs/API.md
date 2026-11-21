# Voice Budget API

API REST para gerenciamento de transações financeiras com inteligência artificial e processamento de voz.

## 1. Tecnologias

- Java 21
- Spring Boot 4.2
- Spring AI
- Ollama — Qwen 2.5 3B
- PostgreSQL
- Whisper.cpp — Speech-to-Text
- Piper TTS — Text-to-Speech
- JUnit e Mockito

## 2. Categorias financeiras

| Categoria | Descrição |
|---|---|
| GROCERIES | Mercado e supermercado |
| PHARMA | Farmácia e medicamentos |
| AUTO | Despesas com automóveis |

## 3. Endpoints

### 3.1 Registrar transação

**POST /transactions**

Content-Type: application/json

Requisição:

    {
      "description": "Compra no mercado",
      "amount": 12.50,
      "category": "GROCERIES"
    }

Resposta: HTTP 201 Created.

    {
      "id": "UUID",
      "description": "Compra no mercado",
      "amount": 12.50,
      "category": "GROCERIES"
    }

O valor é informado em reais, com até duas casas decimais.

### 3.2 Consultar transações

**GET /transactions/{category}**

Categorias aceitas: GROCERIES, PHARMA e AUTO.

Exemplo:

    GET /transactions/PHARMA

Resposta: HTTP 200 OK, com uma lista de transações. Caso não existam registros, retorna uma lista vazia.

### 3.3 Processar comando com IA

**POST /transactions/ai**

Content-Type: application/json

Requisição:

    {
      "message": "Registre uma despesa de 23,90 reais na farmácia."
    }

Resposta: HTTP 200 OK.

    {
      "response": "A despesa foi registrada com sucesso."
    }

O ChatClient interpreta o comando e utiliza as ferramentas financeiras para executar registros ou consultas.

### 3.4 Processar comando por áudio

**POST /transactions/voice**

Content-Type: multipart/form-data

Campo obrigatório: `audio`.

Formato aceito: WAV, com extensão `.wav`.

Limite de upload: 10 MB.

Exemplo com cURL:

    curl.exe -X POST "http://localhost:8080/transactions/voice" -F "audio=@comando.wav"

Fluxo de processamento:

1. Recebimento do arquivo de áudio.
2. Transcrição pelo Whisper.cpp.
3. Interpretação pelo ChatClient.
4. Execução do Tool Calling.
5. Geração da resposta textual.
6. Síntese de voz pelo Piper.
7. Retorno do resultado ao cliente.

Resposta: HTTP 200 OK.

    {
      "transcription": "Registre uma despesa de 80 reais no mercado.",
      "aiResponse": "A despesa foi registrada.",
      "audioContentType": "audio/wav",
      "audioBase64": "UklGR..."
    }

O áudio é retornado em Base64 e deve ser decodificado pelo cliente.

## 4. Códigos HTTP

| Código | Significado |
|---|---|
| 200 | Requisição processada |
| 201 | Transação criada |
| 400 | Entrada inválida ou falha de validação |
| 500 | Falha interna |

## 5. Configuração do ambiente

### PostgreSQL

- Host: localhost
- Porta: 5432
- Banco: voice_budget_db
- Usuário: postgres
- Senha: variável de ambiente DB_PASSWORD

### Ollama

- Modelo: qwen2.5:3b
- Endereço: http://localhost:11434

### Whisper.cpp

- Executável: tools/whisper/Release/whisper-cli.exe
- Modelo: tools/whisper/models/ggml-tiny.bin
- Idioma: pt
- Threads: 2
- Timeout: 90 segundos

### Piper TTS

- Modelo: tools/piper/models/pt_BR-faber-medium.onnx
- Configuração: tools/piper/models/pt_BR-faber-medium.onnx.json

O processamento de IA e áudio é executado localmente, sem cobrança por requisição a serviços externos.

## 6. Execução

Requisitos: Java 21, Maven, PostgreSQL e Ollama configurados.

Na raiz do projeto, execute:

    .\scripts\windows.ps1 run

A API ficará disponível em:

    http://localhost:8080

## 7. Limitações conhecidas

- O endpoint de voz aceita WAV nesta versão.
- O limite de upload é 10 MB.
- A interpretação depende do modelo local.
- Não há autenticação ou separação de transações por usuário.
- A aplicação não substitui um sistema profissional de contabilidade.
- Não há tratamento global personalizado para todas as exceções.
