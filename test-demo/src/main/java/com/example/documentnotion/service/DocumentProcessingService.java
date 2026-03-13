package com.example.documentnotion.service;

import com.example.documentnotion.config.ProcessingConfig;
import com.example.documentnotion.dto.ProcessResult;
import com.example.documentnotion.dto.TextChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProcessingService {

    private final ProcessingConfig processingConfig;
    private final NotionService notionService;
    private final Tika tika = new Tika();

    public ProcessResult processDocument(MultipartFile file, String prompt) throws Exception {
        String fileName = file.getOriginalFilename();
        log.info("开始处理文档: {}", fileName);
        
        // 1. 提取纯文本
        String plainText = extractText(file);
        log.info("文档 {} 提取完成，共 {} 字符", fileName, plainText.length());
        
        // 2. 按5000字分段
        List<TextChunk> chunks = splitIntoChunks(plainText, processingConfig.getChunkSize());
        log.info("文档 {} 被分成 {} 段", fileName, chunks.size());
        
        // 3. 合并所有段落（在开头添加Prompt）
        String mergedContent = mergeChunksWithPrompt(chunks, prompt);
        
        // 4. 创建Notion页面
        String pageTitle = getFileNameWithoutExtension(fileName);
        String notionPageId = notionService.createPage(pageTitle, mergedContent);
        String notionPageUrl = "https://notion.so/" + notionPageId.replace("-", "");
        
        log.info("文档 {} 处理完成，Notion页面: {}", fileName, notionPageUrl);
        
        return ProcessResult.builder()
                .fileName(fileName)
                .success(true)
                .notionPageId(notionPageId)
                .notionPageUrl(notionPageUrl)
                .totalChunks(chunks.size())
                .totalCharacters(plainText.length())
                .chunks(chunks)
                .prompt(prompt)
                .build();
    }

    private String extractText(MultipartFile file) throws Exception {
        try (InputStream inputStream = file.getInputStream()) {
            BodyContentHandler handler = new BodyContentHandler(-1); // -1 means no limit
            Metadata metadata = new Metadata();
            ParseContext parseContext = new ParseContext();
            
            Parser parser = new AutoDetectParser();
            parser.parse(inputStream, handler, metadata, parseContext);
            
            return handler.toString();
        }
    }

    private List<TextChunk> splitIntoChunks(String text, int chunkSize) {
        List<TextChunk> chunks = new ArrayList<>();
        
        if (text.length() <= chunkSize) {
            chunks.add(TextChunk.builder()
                    .index(0)
                    .content(text)
                    .characterCount(text.length())
                    .build());
            return chunks;
        }
        
        int index = 0;
        int chunkIndex = 0;
        
        while (index < text.length()) {
            int endIndex = Math.min(index + chunkSize, text.length());
            
            // 尝试在句子或段落边界处切割
            if (endIndex < text.length()) {
                // 寻找最近的句号、问号、感叹号或换行符
                int lastPeriod = text.lastIndexOf("。", endIndex);
                int lastQuestion = text.lastIndexOf("？", endIndex);
                int lastExclaim = text.lastIndexOf("！", endIndex);
                int lastNewline = text.lastIndexOf("\n", endIndex);
                
                int bestBreak = Math.max(lastPeriod, Math.max(lastQuestion, Math.max(lastExclaim, lastNewline)));
                
                if (bestBreak > index && bestBreak > endIndex - 200) {
                    endIndex = bestBreak + 1;
                }
            }
            
            String chunkContent = text.substring(index, endIndex).trim();
            if (!chunkContent.isEmpty()) {
                chunks.add(TextChunk.builder()
                        .index(chunkIndex++)
                        .content(chunkContent)
                        .characterCount(chunkContent.length())
                        .build());
            }
            
            index = endIndex;
        }
        
        return chunks;
    }

    private String mergeChunksWithPrompt(List<TextChunk> chunks, String prompt) {
        StringBuilder merged = new StringBuilder();
        
        // 添加Prompt作为开头
        if (prompt != null && !prompt.trim().isEmpty()) {
            merged.append("## 提示词 (Prompt)\n\n");
            merged.append(prompt.trim());
            merged.append("\n\n");
            merged.append("---\n\n");
        }
        
        merged.append("## 文档内容\n\n");
        
        // 合并所有段落
        for (TextChunk chunk : chunks) {
            merged.append(chunk.getContent());
            merged.append("\n\n");
        }
        
        return merged.toString().trim();
    }

    private String getFileNameWithoutExtension(String fileName) {
        if (fileName == null) return "Untitled";
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }
}
