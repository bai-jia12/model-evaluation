package com.example.documentnotion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessResult {
    private String fileName;
    private boolean success;
    private String notionPageId;
    private String notionPageUrl;
    private String errorMessage;
    private int totalChunks;
    private int totalCharacters;
    private List<TextChunk> chunks;
    private String prompt;
}
