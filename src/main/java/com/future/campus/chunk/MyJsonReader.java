package com.future.campus.chunk;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

// 从 classpath 下的 JSON 文件中读取文档
 @Component
 public class MyJsonReader {
     private final Resource resource;

     private final SpringAiKeyWordChunk springAiKeyWordChunk;

     private final Resource pdfResource;

     MyJsonReader(@Value("classpath:products.json") Resource resource,
                  SpringAiKeyWordChunk springAiKeyWordChunk,
                  @Value("classpath:test.pdf") Resource pdfResource) {
         this.resource = resource;
         this.springAiKeyWordChunk = springAiKeyWordChunk;
         this.pdfResource = pdfResource;
     }

     // 基本用法
    public  List<Document> loadBasicJsonDocuments() {

         JsonReader jsonReader = new JsonReader(this.resource);

        // 正确的方式：使用 Resource 对象创建 PDF 读取器
        // 可选：配置分页选项（每1页作为一个文档片段）

        DocumentReader pdfDocumentReader = new PagePdfDocumentReader(this.pdfResource);
        List<Document> pdfDocuments = pdfDocumentReader.get();
        
        // 使用 TokenTextSplitter 进行分割
        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter(800, 500, 5, 5000, true);
        List<Document> splitDocuments = tokenTextSplitter.apply(pdfDocuments);
        
        System.out.println("PDF 原始文档数量: " + pdfDocuments.size());
        System.out.println("PDF 分割后文档数量: " + splitDocuments.size());

        List<Document> documents = jsonReader.get();
//         springAiKeyWordChunk.enrichDocumentsByKeyword(documents);
//         springAiKeyWordChunk.splitCustomized(documents);
         return documents;
     }

     // 指定使用哪些 JSON 字段作为文档内容
     List<Document> loadJsonWithSpecificFields() {
         JsonReader jsonReader = new JsonReader(this.resource, "description", "features");
         return jsonReader.get();
     }

     // 使用 JSON 指针精确提取文档内容
     List<Document> loadJsonWithPointer() {
         JsonReader jsonReader = new JsonReader(this.resource);
         return jsonReader.get("/items"); // 提取 items 数组内的内容
     }
 }
