package com.example.docproc.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextSplitService {

    private static final int CHUNK_SIZE = 5000;

    public List<String> splitText(String text) {
        List<String> chunks = new ArrayList<>();
        
        if (text == null || text.isEmpty()) {
            return chunks;
        }

        text = text.replaceAll("\r\n", "\n").replaceAll("\r", "\n");
        
        int length = text.length();
        int start = 0;

        while (start < length) {
            int end = Math.min(start + CHUNK_SIZE, length);
            
            if (end < length) {
                int lastNewline = text.lastIndexOf('\n', end);
                if (lastNewline > start && lastNewline > end - 1000) {
                    end = lastNewline + 1;
                }
            }

            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }
            start = end;
        }

        return chunks;
    }

    public String mergeChunks(List<String> chunks, String prompt) {
        StringBuilder merged = new StringBuilder();
        
        if (prompt != null && !prompt.trim().isEmpty()) {
            merged.append("【提示词】\n").append(prompt).append("\n\n");
            merged.append("══════════════════════════════════════════════════\n\n");
        }

        for (int i = 0; i < chunks.size(); i++) {
            merged.append("【段落 ").append(i + 1).append("】\n");
            merged.append("──────────────────────────────────────\n");
            merged.append(chunks.get(i)).append("\n\n");
        }

        return merged.toString();
    }
}
