# 文档转Notion处理器

一个基于Spring Boot的Web系统，支持上传多个文档（PDF/Word/TXT/MD），自动转换为纯文本、分段处理，并生成Notion页面。

## 功能特性

- 📄 **多格式支持**：PDF、Word（DOC/DOCX）、TXT、Markdown
- 📤 **批量上传**：一次可上传1-10个文档
- 📝 **智能分段**：按5000字自动分段，尽量在句子边界处切割
- 🔗 **Notion集成**：自动为每个文档创建Notion页面
- 💬 **Prompt支持**：可输入提示词，自动添加到每个页面开头

## 技术栈

- Java 8
- Spring Boot 2.7.18
- Apache Tika（文档解析）
- Apache POI（Word文档）
- PDFBox（PDF文档）
- Thymeleaf（前端模板）

## 快速开始

### 1. 配置Notion集成

在使用前，需要配置Notion API：

#### 创建Notion集成
1. 访问 [Notion Integrations](https://www.notion.so/my-integrations)
2. 点击 "New integration"
3. 填写集成名称，选择关联的工作空间
4. 复制 "Internal Integration Token"

#### 获取父页面ID
1. 在Notion中创建一个页面作为所有生成页面的父页面
2. 点击页面右上角 "..." → "Copy link"
3. 从链接中提取页面ID，例如：
   - 链接：`https://www.notion.so/workspace/My-Page-1234567890abcdef1234567890abcdef`
   - 页面ID：`1234567890abcdef1234567890abcdef`

#### 授权集成访问页面
1. 在Notion中打开父页面
2. 点击 "..." → "Add connections"
3. 选择你创建的集成

### 2. 配置应用

编辑 `src/main/resources/application.yml`：

```yaml
notion:
  api:
    token: "your_notion_integration_token_here"  # 替换为你的Token
  parent-page-id: "your_parent_page_id_here"      # 替换为父页面ID
```

或使用环境变量：
```bash
export NOTION_API_TOKEN=your_token_here
export NOTION_PARENT_PAGE_ID=your_page_id_here
```

### 3. 运行应用

```bash
# 使用Maven
mvn spring-boot:run

# 或打包后运行
mvn clean package
java -jar target/document-notion-processor-1.0.0.jar
```

### 4. 访问应用

打开浏览器访问：`http://localhost:8080`

## 使用流程

1. **上传文档**：点击或拖拽1-10个文档到上传区域
2. **输入Prompt**：（可选）输入提示词，会添加到每个Notion页面开头
3. **开始处理**：点击按钮，系统会自动：
   - 提取文档纯文本
   - 按5000字分段处理
   - 合并所有内容
   - 在Notion创建页面
4. **查看结果**：处理完成后，点击链接查看Notion页面

## 项目结构

```
src/main/java/com/example/documentnotion/
├── config/              # 配置类
│   ├── NotionConfig.java
│   └── ProcessingConfig.java
├── controller/          # 控制器
│   └── DocumentController.java
├── dto/                 # 数据传输对象
│   ├── ProcessResult.java
│   ├── TextChunk.java
│   └── UploadRequest.java
├── service/             # 业务逻辑
│   ├── DocumentProcessingService.java
│   └── NotionService.java
└── DocumentNotionProcessorApplication.java

src/main/resources/
├── templates/           # 前端页面
│   └── index.html
└── application.yml      # 配置文件
```

## API接口

### 上传并处理文档

```http
POST /api/documents/upload
Content-Type: multipart/form-data

Parameters:
- files: File[] (1-10个文件)
- prompt: String (可选，提示词)

Response:
[
  {
    "fileName": "document.pdf",
    "success": true,
    "notionPageId": "12345678-1234-1234-1234-123456789abc",
    "notionPageUrl": "https://notion.so/12345678123412341234123456789abc",
    "totalChunks": 3,
    "totalCharacters": 12500
  }
]
```

## 配置说明

### application.yml

```yaml
server:
  port: 8080  # 服务端口

spring:
  servlet:
    multipart:
      max-file-size: 50MB      # 单个文件最大大小
      max-request-size: 100MB  # 整个请求最大大小

notion:
  api:
    token: "your_token"        # Notion集成Token
    version: "2022-06-28"      # Notion API版本
    base-url: "https://api.notion.com/v1"
  parent-page-id: "your_id"    # 父页面ID

processing:
  chunk-size: 5000             # 每段最大字符数
```

## 注意事项

1. **Notion API限制**：
   - 每个块（block）最多2000字符，长文本会自动分割
   - API有速率限制，大量文档建议分批处理

2. **文档解析**：
   - 使用Apache Tika解析文档，支持大多数常见格式
   - 扫描版PDF（图片）无法提取文字

3. **中文支持**：
   - 已配置UTF-8编码
   - 支持中文文档的分段和上传

## 故障排除

### Notion页面创建失败
- 检查Token是否正确
- 确认集成已被授权访问父页面
- 查看日志中的详细错误信息

### 文档解析失败
- 确保文档不是加密或损坏的
- 扫描版PDF需要先进行OCR处理

### 内存不足
- 大文件处理可能需要更多内存
- 调整JVM参数：`-Xmx2g`

## 许可证

MIT License
