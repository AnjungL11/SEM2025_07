package com.g07.service;

import com.g07.entity.KnowledgeChunk;
import com.g07.mapper.KnowledgeChunkMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RAGService {

    @Autowired
    private KnowledgeChunkMapper chunkMapper;

    public List<String> retrieve(String question, String tenantId) {
        List<String> results = new ArrayList<>();
        
        if (question == null || question.trim().isEmpty()) {
            return results;
        }

        // 简单的关键词提取：暂时取前 4 个字作为关键词进行模糊匹配
        // 如果想更精确，可以将 question 原样传进去，在 Mapper 中做更复杂的 LIKE 查询
        String keyword = question.length() > 4 ? question.substring(0, 4) : question;

        // 调用 Mapper 查询数据库
        // 注意：KnowledgeChunkMapper 必须包含 searchByKeyword 方法
        List<KnowledgeChunk> chunks = chunkMapper.searchByKeyword(tenantId, keyword);

        if (chunks == null) return results;

        return chunks.stream()
                .map(KnowledgeChunk::getContent)
                .collect(Collectors.toList());
    }
}