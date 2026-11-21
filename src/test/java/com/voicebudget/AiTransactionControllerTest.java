package com.voicebudget;

import com.voicebudget.infrastructure.api.AiTransactionController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AiTransactionControllerTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AiTransactionController controller =
                new AiTransactionController(chatClient);

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setValidator(validator)
                .build();
    }

    @Test
    void shouldProcessAiCommand() throws Exception {

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content())
                .thenReturn("Despesa registrada com sucesso.");

        mockMvc.perform(post("/transactions/ai")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "Registre uma despesa de 80 reais no mercado."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response")
                        .value("Despesa registrada com sucesso."));

        verify(chatClient).prompt();
        verify(requestSpec).user(
                "Registre uma despesa de 80 reais no mercado."
        );
        verify(requestSpec).call();
        verify(responseSpec).content();
    }

    @Test
    void shouldRejectEmptyAiCommand() throws Exception {

        mockMvc.perform(post("/transactions/ai")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(chatClient);
    }
}