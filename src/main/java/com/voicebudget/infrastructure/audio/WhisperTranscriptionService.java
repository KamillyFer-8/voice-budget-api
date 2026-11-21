
package com.voicebudget.infrastructure.audio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class WhisperTranscriptionService {

    private final Path executablePath;
    private final Path modelPath;
    private final String language;
    private final int threads;
    private final Duration timeout;

    public WhisperTranscriptionService(
            @Value("${voice.audio.whisper.executable}") Path executablePath,
            @Value("${voice.audio.whisper.model}") Path modelPath,
            @Value("${voice.audio.whisper.language:pt}") String language,
            @Value("${voice.audio.whisper.threads:2}") int threads,
            @Value("${voice.audio.whisper.timeout:90s}") Duration timeout
    ) {
        this.executablePath = executablePath;
        this.modelPath = modelPath;
        this.language = language;
        this.threads = threads;
        this.timeout = timeout;
    }

    public String transcribe(Path audioFile) {

        if (audioFile == null) {
            throw new IllegalArgumentException(
                    "Arquivo de áudio não encontrado."
            );
        }

        Path project = Path.of("").toAbsolutePath().normalize();

        Path audio = audioFile.isAbsolute()
                ? audioFile.normalize()
                : project.resolve(audioFile).normalize();

        if (!Files.isRegularFile(audio)) {
            throw new IllegalArgumentException(
                    "Arquivo de áudio não encontrado: " + audio
            );
        }

        Path executable = resolve(project, executablePath);
        Path model = resolve(project, modelPath);

        if (!Files.isRegularFile(executable)) {
            throw new IllegalStateException(
                    "Executável do Whisper não encontrado: " + executable
            );
        }

        if (!Files.isRegularFile(model)) {
            throw new IllegalStateException(
                    "Modelo do Whisper não encontrado: " + model
            );
        }

        if (threads < 1 || timeout == null
                || timeout.isZero() || timeout.isNegative()) {
            throw new IllegalStateException(
                    "Configuração de threads ou timeout inválida."
            );
        }

        Path workingDirectory = executable.getParent();

        if (workingDirectory == null) {
            throw new IllegalStateException(
                    "Não foi possível identificar a pasta do Whisper."
            );
        }

        // O executável nativo recebe somente caminhos relativos ASCII.
        String cliExecutable = ".\\" + executable.getFileName();
        String cliModel = toCliPath(workingDirectory, model, project);
        String cliAudio = toCliPath(workingDirectory, audio, project);

        Path target = project.resolve("target");
        Path outputFile = null;
        Path scriptFile = null;

        try {
            Files.createDirectories(target);

            outputFile = Files.createTempFile(
                    target, "whisper-output-", ".txt"
            );

            scriptFile = Files.createTempFile(
                    target, "whisper-run-", ".ps1"
            );

            String script = """
                    $ErrorActionPreference = 'Stop'

                    & $args[0] `
                        -m $args[1] `
                        -f $args[2] `
                        -l $args[3] `
                        -t $args[4] `
                        -nt -np

                    exit $LASTEXITCODE
                    """;

            Files.writeString(
                    scriptFile,
                    script,
                    StandardCharsets.UTF_8
            );

            ProcessBuilder builder = new ProcessBuilder(
                    "powershell.exe",
                    "-NoProfile",
                    "-NonInteractive",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-File",
                    scriptFile.toString(),
                    cliExecutable,
                    cliModel,
                    cliAudio,
                    language,
                    String.valueOf(threads)
            );

            // O Whisper interpreta os caminhos relativos a esta pasta.
            builder.directory(workingDirectory.toFile());
            builder.redirectErrorStream(true);
            builder.redirectOutput(outputFile.toFile());

            Process process = builder.start();

            boolean finished = process.waitFor(
                    timeout.toMillis(),
                    TimeUnit.MILLISECONDS
            );

            if (!finished) {
                process.descendants().forEach(
                        handle -> handle.destroyForcibly()
                );

                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);

                throw new IllegalStateException(
                        "O reconhecimento de voz excedeu o limite de "
                                + timeout.toSeconds() + " segundos."
                );
            }

            String output = Files.readString(
                    outputFile,
                    StandardCharsets.UTF_8
            );

            if (process.exitValue() != 0) {
                throw new IllegalStateException(
                        "Whisper encerrou com código "
                                + process.exitValue()
                                + ". Saída: " + output
                );
            }

            // Processa a transcrição sem referência de método
            // que provoque o aviso de null safety do VS Code.
            StringBuilder transcriptionBuilder = new StringBuilder();

            for (String rawLine : output.split("\\R")) {

                if (rawLine == null) {
                    continue;
                }

                String line = rawLine.trim();

                if (!line.isBlank()
                        && !line.startsWith("read_audio_data:")) {

                    if (transcriptionBuilder.length() > 0) {
                        transcriptionBuilder.append(' ');
                    }

                    transcriptionBuilder.append(line);
                }
            }

            String transcription = transcriptionBuilder.toString();

            if (transcription.isBlank()) {
                throw new IllegalStateException(
                        "O Whisper não retornou uma transcrição."
                );
            }

            return transcription;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Não foi possível executar o Whisper.",
                    e
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "O reconhecimento de voz foi interrompido.",
                    e
            );

        } finally {
            delete(outputFile);
            delete(scriptFile);
        }
    }

    private Path resolve(Path project, Path configuredPath) {

        if (configuredPath == null) {
            throw new IllegalStateException(
                    "Uma configuração de caminho do Whisper não foi definida."
            );
        }

        return configuredPath.isAbsolute()
                ? configuredPath.normalize()
                : project.resolve(configuredPath).normalize();
    }

    private String toCliPath(
            Path workingDirectory,
            Path target,
            Path project
    ) {

        Path normalized = target.toAbsolutePath().normalize();

        if (!normalized.startsWith(project)) {
            throw new IllegalArgumentException(
                    "O arquivo utilizado pelo Whisper deve estar "
                            + "dentro do projeto."
            );
        }

        String relativePath = workingDirectory
                .relativize(normalized)
                .toString();

        if (!StandardCharsets.US_ASCII.newEncoder()
                .canEncode(relativePath)) {

            throw new IllegalArgumentException(
                    "O caminho utilizado pelo Whisper deve conter "
                            + "somente caracteres ASCII."
            );
        }

        return relativePath;
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
