package com.voicebudget.infrastructure.audio;

import com.voicebudget.infrastructure.api.dto.VoiceCommandResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoiceCommandOrchestratorTest {

    @Mock
    private WhisperTranscriptionService whisperService;

    @Mock
    private PiperSynthesisService piperService;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    private VoiceCommandOrchestrator orchestrator;

    private static final String TRANSCRIPTION =
            "Registre uma despesa de 80 reais no mercado.";

    private static final String AI_RESPONSE =
            "Despesa registrada com sucesso.";

    private static final byte[] GENERATED_AUDIO =
            new byte[]{1, 2, 3, 4, 5, 6};

    @BeforeEach
    void setUp() {
        orchestrator = new VoiceCommandOrchestrator(
                whisperService,
                piperService,
                chatClient
        );
    }

    @Test
    void shouldRejectNullAudio() {
        assertThrows(
                IllegalArgumentException.class,
                () -> orchestrator.process(null)
        );

        verifyNoInteractions(
                whisperService,
                piperService,
                chatClient
        );
    }

    @Test
    void shouldRejectEmptyAudio() {
        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "comando.wav",
                "audio/wav",
                new byte[0]
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> orchestrator.process(audio)
        );

        verifyNoInteractions(
                whisperService,
                piperService,
                chatClient
        );
    }

    @Test
    void shouldRejectUnsupportedAudioFormat() {
        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "comando.mp3",
                "audio/mpeg",
                new byte[]{1, 2, 3}
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> orchestrator.process(audio)
        );

        verifyNoInteractions(
                whisperService,
                piperService,
                chatClient
        );
    }

    @Test
    void shouldProcessAudioAndGenerateResponse() throws Exception {
        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "comando.wav",
                "audio/wav",
                new byte[]{10, 20, 30, 40}
        );

        when(whisperService.transcribe(any(Path.class)))
                .thenReturn(TRANSCRIPTION);

        configureChatClient(AI_RESPONSE);

        doAnswer(invocation -> {
            Path output = invocation.getArgument(1);
            Files.write(output, GENERATED_AUDIO);
            return output;
        }).when(piperService).synthesize(
                eq(AI_RESPONSE),
                any(Path.class)
        );

        VoiceCommandResponse result = orchestrator.process(audio);

        assertNotNull(result);
        assertEquals(TRANSCRIPTION, result.transcription());
        assertEquals(AI_RESPONSE, result.aiResponse());
        assertEquals("audio/wav", result.audioContentType());

        byte[] decodedAudio = Base64.getDecoder()
                .decode(result.audioBase64());

        assertArrayEquals(GENERATED_AUDIO, decodedAudio);

        verify(whisperService).transcribe(any(Path.class));
        verify(chatClient).prompt();
        verify(piperService).synthesize(
                eq(AI_RESPONSE),
                any(Path.class)
        );
    }

    @Test
    void shouldPropagateWhisperFailure() {
        MockMultipartFile audio = createValidAudio();

        when(whisperService.transcribe(any(Path.class)))
                .thenThrow(new IllegalStateException(
                        "Falha na transcrição"
                ));

        assertThrows(
                IllegalStateException.class,
                () -> orchestrator.process(audio)
        );

        verifyNoInteractions(chatClient, piperService);
    }

    @Test
    void shouldRejectEmptyAiResponse() {
        MockMultipartFile audio = createValidAudio();

        when(whisperService.transcribe(any(Path.class)))
                .thenReturn(TRANSCRIPTION);

        configureChatClient(" ");

        assertThrows(
                IllegalStateException.class,
                () -> orchestrator.process(audio)
        );

        verifyNoInteractions(piperService);
    }

    @Test
    void shouldPropagatePiperFailure() {
        MockMultipartFile audio = createValidAudio();

        when(whisperService.transcribe(any(Path.class)))
                .thenReturn(TRANSCRIPTION);

        configureChatClient(AI_RESPONSE);

        doThrow(new IllegalStateException(
                "Falha na síntese de voz"
        )).when(piperService).synthesize(
                eq(AI_RESPONSE),
                any(Path.class)
        );

        assertThrows(
                IllegalStateException.class,
                () -> orchestrator.process(audio)
        );
    }

    private void configureChatClient(String response) {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn(response);
    }

    private MockMultipartFile createValidAudio() {
        return new MockMultipartFile(
                "audio",
                "comando.wav",
                "audio/wav",
                new byte[]{10, 20, 30, 40}
        );
    }
}