package com.example.documentnotion.service;

import com.example.documentnotion.config.NotionConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotionService {

    private final NotionConfig notionConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${notion.parent-page-id}")
    private String parentPageId;
    
    private CloseableHttpClient httpClient;
    
    @PostConstruct
    public void init() {
        httpClient = HttpClients.createDefault();
    }

    public String createPage(String title, String content) throws Exception {
        String url = notionConfig.getBaseUrl() + "/pages";
        
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Authorization", "Bearer " + notionConfig.getToken());
        httpPost.setHeader("Notion-Version", notionConfig.getVersion());
        httpPost.setHeader("Content-Type", "application/json");
        
        // 构建请求体
        ObjectNode requestBody = objectMapper.createObjectNode();
        
        // 父页面
        ObjectNode parent = objectMapper.createObjectNode();
        parent.put("page_id", parentPageId);
        requestBody.set("parent", parent);
        
        // 页面属性（标题）
        ObjectNode properties = objectMapper.createObjectNode();
        ObjectNode titleProperty = objectMapper.createObjectNode();
        ArrayNode titleContent = objectMapper.createArrayNode();
        ObjectNode titleText = objectMapper.createObjectNode();
        ObjectNode textContent = objectMapper.createObjectNode();
        textContent.put("content", title);
        titleText.set("text", textContent);
        titleContent.add(titleText);
        titleProperty.set("title", titleContent);
        properties.set("title", titleProperty);
        requestBody.set("properties", properties);
        
        // 页面内容
        ArrayNode children = objectMapper.createArrayNode();
        
        // 将内容分割成多个段落（Notion API有单个块大小限制）
        String[] paragraphs = content.split("\n\n");
        
        for (String paragraph : paragraphs) {
            if (paragraph.trim().isEmpty()) continue;
            
            // 处理标题
            if (paragraph.startsWith("## ")) {
                String headingText = paragraph.substring(3).trim();
                ObjectNode headingBlock = objectMapper.createObjectNode();
                headingBlock.put("object", "block");
                headingBlock.put("type", "heading_2");
                ObjectNode heading2 = objectMapper.createObjectNode();
                ArrayNode richText = createRichTextArray(headingText);
                heading2.set("rich_text", richText);
                headingBlock.set("heading_2", heading2);
                children.add(headingBlock);
            } else if (paragraph.startsWith("---")) {
                // 分隔线
                ObjectNode dividerBlock = objectMapper.createObjectNode();
                dividerBlock.put("object", "block");
                dividerBlock.put("type", "divider");
                dividerBlock.set("divider", objectMapper.createObjectNode());
                children.add(dividerBlock);
            } else {
                // 普通段落，如果太长需要分割
                addParagraphBlocks(children, paragraph);
            }
        }
        
        requestBody.set("children", children);
        
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        httpPost.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));
        
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            
            if (response.getStatusLine().getStatusCode() == 200) {
                JsonNode jsonResponse = objectMapper.readTree(responseBody);
                String pageId = jsonResponse.get("id").asText();
                log.info("Notion页面创建成功，ID: {}", pageId);
                return pageId;
            } else {
                log.error("创建Notion页面失败: {}", responseBody);
                throw new RuntimeException("创建Notion页面失败: " + responseBody);
            }
        }
    }
    
    private void addParagraphBlocks(ArrayNode children, String text) {
        // Notion API限制每个text对象最多2000字符
        int maxChunkSize = 1800;
        
        if (text.length() <= maxChunkSize) {
            ObjectNode paragraphBlock = createParagraphBlock(text);
            children.add(paragraphBlock);
        } else {
            // 分割长文本
            int index = 0;
            while (index < text.length()) {
                int endIndex = Math.min(index + maxChunkSize, text.length());
                String chunk = text.substring(index, endIndex);
                ObjectNode paragraphBlock = createParagraphBlock(chunk);
                children.add(paragraphBlock);
                index = endIndex;
            }
        }
    }
    
    private ObjectNode createParagraphBlock(String text) {
        ObjectNode paragraphBlock = objectMapper.createObjectNode();
        paragraphBlock.put("object", "block");
        paragraphBlock.put("type", "paragraph");
        ObjectNode paragraph = objectMapper.createObjectNode();
        ArrayNode richText = createRichTextArray(text);
        paragraph.set("rich_text", richText);
        paragraphBlock.set("paragraph", paragraph);
        return paragraphBlock;
    }
    
    private ArrayNode createRichTextArray(String text) {
        ArrayNode richText = objectMapper.createArrayNode();
        ObjectNode textObj = objectMapper.createObjectNode();
        ObjectNode textContent = objectMapper.createObjectNode();
        textContent.put("content", text);
        textObj.set("text", textContent);
        richText.add(textObj);
        return richText;
    }
}
