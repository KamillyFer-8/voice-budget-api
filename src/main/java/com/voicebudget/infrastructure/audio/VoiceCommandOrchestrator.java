package com.voicebudget.infrastructure.audio;

import com.voicebudget.infrastructure.api.dto.VoiceCommandResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.Locale;

@Service
public class VoiceCommandOrchestrator {

    private final WhisperTranscriptionService whisperService;
    private final PiperSynthesisService piperService;
    private final ChatClient chatClient;

    public VoiceCommandOrchestrator(
            WhisperTranscriptionService whisperService,
            PiperSynthesisService piperService,
            ChatClient chatClient
    ) {
        this.whisperService = whisperService;
        this.piperService = piperService;
        this.chatClient = chatClient;
    }

    public VoiceCommandResponse process(MultipartFile audio) {

        if (audio == null || audio.isEmpty()) {
            throw new IllegalArgumentException(
                    "Envie um arquivo de áudio."
            );
        }

        String filename = audio.getOriginalFilename();

        if (filename == null
                || !filename.toLowerCase(Locale.ROOT).endsWith(".wav")) {
            throw new IllegalArgumentException(
                    "O formato aceito nesta versão é WAV."
            );
        }

        Path projectDirectory = Path.of("")
                .toAbsolutePath()
                .normalize();

        Path targetDirectory = projectDirectory.resolve("target");

        Path inputFile = null;
        Path outputFile = null;

        try {
            Files.createDirectories(targetDirectory);

            inputFile = Files.createTempFile(
                    targetDirectory,
                    "voice-input-",
                    ".wav"
            );

            try (var inputStream = audio.getInputStream()) {
                Files.copy(
                        inputStream,
                        inputFile,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            // 1. Converte o áudio em texto.
            String transcription = whisperService.transcribe(inputFile);

            // 2. Interpreta o comando e executa as ferramentas necessárias.
            String aiResponse = chatClient.prompt()
                    .user(transcription)
                    .call()
                    .content();

            if (aiResponse == null || aiResponse.isBlank()) {
                throw new IllegalStateException(
                        "A IA não retornou uma resposta."
                );
            }

            // 3. Converte a resposta textual em áudio.
            outputFile = Files.createTempFile(
                    targetDirectory,
                    "voice-response-",
                    ".wav"
            );

            piperService.synthesize(aiResponse, outputFile);

            // 4. Prepara o áudio para a resposta HTTP.
            byte[] audioBytes = Files.readAllBytes(outputFile);

            if (audioBytes.length == 0) {
                throw new IllegalStateException(
                        "O áudio de resposta está vazio."
                );
            }

            return new VoiceCommandResponse(
                    transcription,
                    aiResponse,
                    "audio/wav",
                    Base64.getEncoder().encodeToString(audioBytes)
            );

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Não foi possível processar o arquivo de áudio.",
                    e
            );

        } finally {
            delete(inputFile);
            delete(outputFile);
        }
    }

    private void delete(Path file) {

        if (file != null) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                System.err.println(
                        "Falha ao remover arquivo temporário: "
                                + e.getMessage()
                );
            }
        }
    }
}