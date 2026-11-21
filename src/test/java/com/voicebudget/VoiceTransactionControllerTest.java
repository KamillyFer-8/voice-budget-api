
package com.voicebudget;

import com.voicebudget.infrastructure.api.VoiceTransactionController;
import com.voicebudget.infrastructure.api.dto.VoiceCommandResponse;
import com.voicebudget.infrastructure.audio.VoiceCommandOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class VoiceTransactionControllerTest {

    @Mock
    private VoiceCommandOrchestrator orchestrator;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new VoiceTransactionController(orchestrator)
                )
                .build();
    }

    @Test
    void shouldReceiveAudioAndReturnVoiceResponse() throws Exception {
        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "comando.wav",
                "audio/wav",
                new byte[]{1, 2, 3, 4}
        );

        when(orchestrator.process(any(MultipartFile.class)))
                .thenReturn(new VoiceCommandResponse(
                        "Registre uma despesa de 80 reais no mercado.",
                        "Despesa registrada com sucesso.",
                        "audio/wav",
                        "UklGRg=="
                ));

        mockMvc.perform(multipart("/transactions/voice")
                        .file(audio))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.transcription")
                        .value("Registre uma despesa de 80 reais no mercado."))
                .andExpect(jsonPath("$.aiResponse")
                        .value("Despesa registrada com sucesso."))
                .andExpect(jsonPath("$.audioContentType")
                        .value("audio/wav"))
                .andExpect(jsonPath("$.audioBase64")
                        .value("UklGRg=="));

        verify(orchestrator).process(any(MultipartFile.class));
    }
}