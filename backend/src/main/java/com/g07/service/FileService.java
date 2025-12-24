package com.g07.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String bucketName;

    // Tika 实例，用于自动检测类型并提取文字
    private final Tika tika = new Tika();

    /**
     * 核心功能：从 MinIO 下载文件 -> 提取纯文本
     * 支持 .txt, .pdf, .docx, .doc 等常见格式
     */
    public String extractTextFromMinio(String objectName) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build())) {
            
            // Tika 会自动检测文件流的类型并解析内容
            // 对于扫描版 PDF (纯图片)，这里提取不到文字，需要 OCR (稍后可扩展)
            return tika.parseToString(stream);
            
        } catch (Exception e) {
            System.err.println("文件解析失败 [" + objectName + "]: " + e.getMessage());
            return ""; // 解析失败返回空字符串，避免报错中断主流程
        }
    }

    /**
     * 文本切片 (Chunking)
     * 将长文本切分为较小的段落，以便存入数据库和发送给 AI (避免超出 Token 限制)
     * @param text 原始全文
     * @param chunkSize 每个分片的最大字符数 (建议 500-1000)
     * @param overlap 重叠字符数 (建议 50-100，防止上下文在切分处丢失)
     */
    public List<String> splitTextIntoChunks(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) return chunks;

        // 1. 简单清洗：去除多余的空白字符和换行
        String cleanText = text.replaceAll("\\s+", " ").trim();
        int length = cleanText.length();
        
        int start = 0;
        while (start < length) {
            int end = Math.min(start + chunkSize, length);
            chunks.add(cleanText.substring(start, end));
            
            // 移动步长 = 块大小 - 重叠部分
            start += (chunkSize - overlap);
        }
        
        return chunks;
    }
}