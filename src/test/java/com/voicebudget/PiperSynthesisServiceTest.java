
package com.voicebudget;

import com.voicebudget.infrastructure.audio.PiperSynthesisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PiperSynthesisServiceTest {

    @Autowired
    private PiperSynthesisService service;

    @Test
    void shouldGeneratePortugueseAudio() throws IOException {

        Path output = Path.of("target", "piper-test.wav");

        String text =
                "Olá! Sua despesa de oitenta reais foi registrada com sucesso.";

        Path generatedAudio = service.synthesize(text, output);

        assertNotNull(generatedAudio);
        assertTrue(Files.isRegularFile(generatedAudio));
        assertTrue(Files.size(generatedAudio) > 44);

        byte[] header = new byte[12];

        try (var stream = Files.newInputStream(generatedAudio)) {
            assertEquals(12, stream.read(header));
        }

        String signature = new String(
                header,
                StandardCharsets.US_ASCII
        );

        assertTrue(signature.startsWith("RIFF"));
        assertTrue(signature.substring(8).startsWith("WAVE"));

        System.out.println("\nÁudio gerado pelo Piper:");
        System.out.println(generatedAudio.toAbsolutePath());
        System.out.println(
                "Tamanho: " + Files.size(generatedAudio) + " bytes"
        );
    }
}
