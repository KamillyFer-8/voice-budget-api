# Voice Budget API

API REST para gerenciamento de transações financeiras utilizando inteligência artificial, reconhecimento de voz e síntese de áudio.

O projeto permite registrar e consultar despesas por meio de requisições HTTP, comandos em linguagem natural ou comandos de voz, utilizando ferramentas integradas ao Spring AI.

## Funcionalidades

- Registro de transações financeiras.
- Consulta de despesas por categoria.
- Interpretação de comandos em português com inteligência artificial.
- Tool Calling para executar operações financeiras.
- Transcrição de áudio com Whisper.cpp.
- Geração de respostas em áudio com Piper TTS.
- Processamento local de IA e áudio, sem cobrança por requisição a serviços externos.

## Tecnologias

| Tecnologia | Utilização |
|---|---|
| Java 21 | Linguagem principal |
| Spring Boot 4.2 | Framework da aplicação |
| Spring AI | Integração com inteligência artificial |
| Ollama | Execução local do modelo de IA |
| Qwen 2.5 3B | Modelo de linguagem |
| PostgreSQL | Banco de dados |
| Spring Data JPA | Persistência |
| Whisper.cpp | Speech-to-Text |
| Piper TTS | Text-to-Speech |
| JUnit 5 | Testes automatizados |
| Mockito | Simulação de dependências |
| Maven | Gerenciamento de dependências e build |

## Arquitetura

O projeto utiliza uma organização em camadas, separando regras de negócio, casos de uso e infraestrutura.

```text
src/
├── main/
│   ├── java/com/voicebudget/
│   │   ├── domain/
│   │   │   ├── Category.java
│   │   │   ├── Transaction.java
│   │   │   ├── TransactionId.java
│   │   │   └── TransactionRepository.java
│   │   │
│   │   ├── application/
│   │   │   └── usecase/
│   │   │       ├── PersistTransactionUseCase.java
│   │   │       └── ListTransactionsByCategoryUseCase.java
│   │   │
│   │   └── infrastructure/
│   │       ├── api/
│   │       ├── audio/
│   │       ├── config/
│   │       ├── persistence/
│   │       └── tool/
│   │
│   └── resources/
│       └── application.yaml
│
└── test/
    └── java/com/voicebudget/
```

### Fluxo de processamento por voz

```text
Arquivo de áudio
       |
       v
Whisper.cpp
       |
       v
Transcrição em texto
       |
       v
Spring AI - ChatClient
       |
       v
Tool Calling
       |
       v
Casos de uso financeiros
       |
       v
PostgreSQL
       |
       v
Resposta textual da IA
       |
       v
Piper TTS
       |
       v
Resposta em áudio WAV
```

## Endpoints

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/transactions` | Registrar transação |
| GET | `/transactions/{category}` | Consultar transações por categoria |
| POST | `/transactions/ai` | Processar comando textual com IA |
| POST | `/transactions/voice` | Processar comando por áudio |

### Exemplos

**Registrar uma despesa**

```json
{
  "description": "Compra no mercado",
  "amount": 80.00,
  "category": "GROCERIES"
}
```

**Consultar despesas**

```http
GET /transactions/PHARMA
```

**Enviar comando à IA**

```json
{
  "message": "Registre uma despesa de 23,90 reais na farmácia."
}
```

**Enviar áudio**

```powershell
curl.exe -X POST "http://localhost:8080/transactions/voice" -F "audio=@comando.wav"
```

O endpoint de voz retorna a transcrição, a resposta textual e o áudio WAV codificado em Base64.

Categorias disponíveis:

- `GROCERIES`: mercado e supermercado.
- `PHARMA`: farmácia e medicamentos.
- `AUTO`: despesas com automóveis.

A documentação completa dos parâmetros, exemplos e respostas está em [docs/API.md](docs/API.md).

## Requisitos

- Windows.
- Java JDK 21.
- Maven.
- PostgreSQL.
- Ollama instalado.
- Modelo `qwen2.5:3b` disponível no Ollama.
- Arquivos locais do Whisper.cpp e Piper TTS.

## Configuração do ambiente

### 1. Banco de dados

Crie um banco PostgreSQL chamado:

```sql
CREATE DATABASE voice_budget_db;
```

As credenciais são configuradas em `src/main/resources/application.yaml`.

A senha é fornecida por variável de ambiente, não devendo ser armazenada no código-fonte:

```powershell
$env:DB_PASSWORD = "SUA_SENHA"
```

### 2. Ollama

Baixe o modelo utilizado pelo projeto:

```powershell
ollama pull qwen2.5:3b
```

O endereço configurado para o serviço local é:

```text
http://localhost:11434
```

### 3. Whisper.cpp

O projeto utiliza os seguintes arquivos:

```text
tools/whisper/
├── Release/
│   └── whisper-cli.exe
└── models/
    └── ggml-tiny.bin
```

### 4. Piper TTS

O modelo de voz em português brasileiro está configurado em:

```text
tools/piper/models/
├── pt_BR-faber-medium.onnx
└── pt_BR-faber-medium.onnx.json
```

A integração Java utiliza a biblioteca Piper JNI.

## Executando a aplicação

Depois de configurar o banco de dados, o Ollama e os arquivos locais de áudio, acesse a pasta raiz do projeto.

Configure a variável de ambiente `DB_PASSWORD` e execute:

```powershell
.\scripts\windows.ps1 run
```

A API ficará disponível em:

```text
http://localhost:8080
```

O script `scripts/windows.ps1` também disponibiliza os comandos de compilação, testes e empacotamento:

```powershell
.\scripts\windows.ps1 compile
.\scripts\windows.ps1 test
.\scripts\windows.ps1 package
```

## Testes

O projeto utiliza JUnit 5 e Mockito para verificar os principais componentes.

A cobertura inclui:

- Casos de uso financeiros.
- Persistência com PostgreSQL.
- Integração com ChatClient e Tool Calling.
- Controladores REST.
- Validação das requisições.
- Transcrição com Whisper.
- Síntese de voz com Piper.
- Orquestração do fluxo de voz.
- Tratamento de falhas nos serviços de áudio e IA.

Para executar a suíte:

```powershell
.\scripts\windows.ps1 test
```

Os testes de integração que utilizam serviços reais dependem das respectivas configurações locais.

## Limitações conhecidas

- O processamento de IA depende do modelo local e de sua capacidade de interpretação.
- O endpoint de voz aceita arquivos WAV nesta versão.
- O tamanho máximo configurado para upload é de 10 MB.
- O áudio de resposta é retornado em Base64.
- A aplicação ainda não possui autenticação nem separação de transações por usuário.
- O projeto não substitui um sistema profissional de contabilidade ou consultoria financeira.

## Objetivo do projeto

Este projeto foi desenvolvido como aplicação prática de Java, Spring Boot, Spring AI, persistência de dados, Tool Calling e processamento local de áudio.

O foco é integrar inteligência artificial a operações financeiras reais, mantendo a separação entre domínio, casos de uso e infraestrutura.

---
