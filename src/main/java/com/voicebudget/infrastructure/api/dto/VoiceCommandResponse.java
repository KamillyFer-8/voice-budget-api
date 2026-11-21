package com.voicebudget.infrastructure.api.dto;

public record VoiceCommandResponse(
        String transcription,
        String aiResponse,
        String audioContentType,
        String audioBase64
) {}