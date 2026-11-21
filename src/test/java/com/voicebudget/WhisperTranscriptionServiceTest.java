
package com.voicebudget;

import com.voicebudget.infrastructure.audio.WhisperTranscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WhisperTranscriptionServiceTest {

    @Autowired
    private WhisperTranscriptionService service;

    @Test
    void shouldTranscribePortugueseAudio() {

        String transcription = service.transcribe(
                Path.of("tools", "whisper", "teste.wav")
        );

        assertNotNull(transcription);
        assertFalse(transcription.isBlank());

        assertTrue(
                transcription.toLowerCase().contains("mercado"),
                "A transcrição deveria reconhecer a palavra mercado."
        );

        System.out.println("\nTranscrição realizada pelo Java:");
        System.out.println(transcription);
    }
}
