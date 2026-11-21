
package com.voicebudget;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ChatClientIntegrationTest {

    @Autowired
    private ChatClient chatClient;

    @Test
    void shouldGenerateFinancialResponseInPortuguese() {

        String response = chatClient.prompt()
                .user("Explique em uma frase por que é importante controlar os gastos mensais.")
                .call()
                .content();

        assertNotNull(response);
        assertFalse(response.isBlank());

        System.out.println("\nResposta do Qwen2.5:3b:");
        System.out.println(response);
    }
}
