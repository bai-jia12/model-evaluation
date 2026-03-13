package com.example.docproc.dto;

import java.util.List;

public class DocumentResult {
    private String fileName;
    private String fileSize;
    private String originalText;
    private int charCount;
    private List<String> chunks;
    private int chunkCount;
    private String mergedContent;
    private String error;

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileSize() { return fileSize; }
    public void setFileSize(String fileSize) { this.fileSize = fileSize; }
    public String getOriginalText() { return originalText; }
    public void setOriginalText(String originalText) { this.originalText = originalText; }
    public int getCharCount() { return charCount; }
    public void setCharCount(int charCount) { this.charCount = charCount; }
    public List<String> getChunks() { return chunks; }
    public void setChunks(List<String> chunks) { this.chunks = chunks; }
    public int getChunkCount() { return chunkCount; }
    public void setChunkCount(int chunkCount) { this.chunkCount = chunkCount; }
    public String getMergedContent() { return mergedContent; }
    public void setMergedContent(String mergedContent) { this.mergedContent = mergedContent; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
