package com.example.docprocessor.dto;

import java.util.ArrayList;
import java.util.List;

public class DocumentResult {
    private String fileName;
    private List<String> chunks;
    private String prompt;
    private int totalChars;
    private int chunkCount;

    public DocumentResult(String fileName, List<String> chunks, String prompt) {
        this.fileName = fileName;
        this.chunks = chunks != null ? chunks : new ArrayList<>();
        this.prompt = prompt;
        this.chunkCount = this.chunks.size();
        this.totalChars = this.chunks.stream().mapToInt(String::length).sum();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<String> getChunks() {
        return chunks;
    }

    public void setChunks(List<String> chunks) {
        this.chunks = chunks;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public int getTotalChars() {
        return totalChars;
    }

    public int getChunkCount() {
        return chunkCount;
    }

    public void setTotalChars(int totalChars) {
        this.totalChars = totalChars;
    }

    public void setChunkCount(int chunkCount) {
        this.chunkCount = chunkCount;
    }
}
