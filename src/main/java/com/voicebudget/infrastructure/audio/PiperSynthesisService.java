
package com.voicebudget.infrastructure.audio;

import io.github.jvoiceproject.piperjni.PiperJNI;
import io.github.jvoiceproject.piperjni.PiperVoice;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class PiperSynthesisService
        implements InitializingBean, DisposableBean {

    private final Path modelPath;
    private final Path configPath;

    private volatile PiperVoice voice;

    public PiperSynthesisService(
            @Value("${voice.audio.piper.model}") Path modelPath,
            @Value("${voice.audio.piper.config}") Path configPath
    ) {
        this.modelPath = modelPath;
        this.configPath = configPath;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        Path projectDirectory = Path.of("")
                .toAbsolutePath()
                .normalize();

        Path model = resolve(projectDirectory, modelPath);
        Path config = resolve(projectDirectory, configPath);

        if (!Files.isRegularFile(model)) {
            throw new FileNotFoundException(
                    "Modelo do Piper não encontrado: " + model
            );
        }

        if (!Files.isRegularFile(config)) {
            throw new FileNotFoundException(
                    "Configuração do Piper não encontrada: " + config
            );
        }

        // Carrega a biblioteca nativa incluída no JAR.
        PiperJNI.loadLibrary();

        // O eSpeak NG é localizado automaticamente pela biblioteca.
        this.voice = PiperJNI.loadVoice(model, config);
    }

    public Path synthesize(String text, Path outputAudioFile) {

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "O texto para sintetizar não pode estar vazio."
            );
        }

        if (outputAudioFile == null) {
            throw new IllegalArgumentException(
                    "O caminho do áudio é obrigatório."
            );
        }

        PiperVoice currentVoice = voice;

        if (currentVoice == null || currentVoice.isClosed()) {
            throw new IllegalStateException(
                    "O mecanismo de síntese de voz não está disponível."
            );
        }

        Path projectDirectory = Path.of("")
                .toAbsolutePath()
                .normalize();

        Path output = outputAudioFile.isAbsolute()
                ? outputAudioFile.normalize()
                : projectDirectory.resolve(outputAudioFile).normalize();

        if (!output.startsWith(projectDirectory)) {
            throw new IllegalArgumentException(
                    "O arquivo de áudio deve ser salvo dentro do projeto."
            );
        }

        Path parent = output.getParent();

        if (parent == null) {
            throw new IllegalArgumentException(
                    "O diretório de saída do áudio é inválido."
            );
        }

        Path temporaryOutput = null;

        try {
            // O Piper retorna áudio PCM de 16 bits em mono.
            short[] samples = currentVoice.textToAudio(text);

            if (samples == null || samples.length == 0) {
                throw new IllegalStateException(
                        "O Piper não gerou amostras de áudio."
                );
            }

            int sampleRate = currentVoice.getSampleRate();

            Files.createDirectories(parent);

            temporaryOutput = Files.createTempFile(
                    parent,
                    "piper-output-",
                    ".wav"
            );

            writeWav(samples, sampleRate, temporaryOutput);

            validateWav(temporaryOutput);

            try {
                Files.move(
                        temporaryOutput,
                        output,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(
                        temporaryOutput,
                        output,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            return output;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Falha ao sintetizar ou salvar o áudio com Piper.",
                    e
            );

        } finally {
            delete(temporaryOutput);
        }
    }

    private void writeWav(
            short[] samples,
            int sampleRate,
            Path output
    ) throws IOException {

        ByteBuffer pcmBuffer = ByteBuffer
                .allocate(Math.multiplyExact(samples.length, Short.BYTES))
                .order(ByteOrder.LITTLE_ENDIAN);

        for (short sample : samples) {
            pcmBuffer.putShort(sample);
        }

        AudioFormat format = new AudioFormat(
                sampleRate,
                16,
                1,
                true,
                false
        );

        try (
                ByteArrayInputStream rawAudio =
                        new ByteArrayInputStream(pcmBuffer.array());

                AudioInputStream audioStream =
                        new AudioInputStream(
                                rawAudio,
                                format,
                                samples.length
                        )
        ) {
            int bytesWritten = AudioSystem.write(
                    audioStream,
                    AudioFileFormat.Type.WAVE,
                    output.toFile()
            );

            if (bytesWritten <= 0) {
                throw new IOException(
                        "Não foi possível gravar o arquivo WAV."
                );
            }
        }
    }

    private Path resolve(
            Path projectDirectory,
            Path configuredPath
    ) {
        if (configuredPath == null) {
            throw new IllegalStateException(
                    "Um caminho do modelo Piper não foi configurado."
            );
        }

        return configuredPath.isAbsolute()
                ? configuredPath.normalize()
                : projectDirectory.resolve(configuredPath).normalize();
    }

    private void validateWav(Path output) throws IOException {

        if (!Files.isRegularFile(output) || Files.size(output) <= 44) {
            throw new IOException(
                    "O arquivo WAV gerado está vazio ou é inválido."
            );
        }

        byte[] header;

        try (var input = Files.newInputStream(output)) {
            header = input.readNBytes(12);
        }

        if (header.length != 12
                || !"RIFF".equals(
                        new String(header, 0, 4, java.nio.charset.StandardCharsets.US_ASCII)
                )
                || !"WAVE".equals(
                        new String(header, 8, 4, java.nio.charset.StandardCharsets.US_ASCII)
                )) {
            throw new IOException(
                    "O arquivo gerado não possui cabeçalho WAV válido."
            );
        }
    }

    private void delete(Path file) {
        if (file != null) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                System.err.println(
                        "Não foi possível remover arquivo temporário: "
                                + e.getMessage()
                );
            }
        }
    }

    @Override
    public void destroy() {
        PiperVoice currentVoice = voice;

        if (currentVoice != null) {
            currentVoice.close();
            voice = null;
        }
    }
}
