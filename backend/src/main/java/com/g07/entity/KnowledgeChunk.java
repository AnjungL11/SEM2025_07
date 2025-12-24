package com.g07.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("knowledge_chunk")
public class KnowledgeChunk {
    @TableId
    private String chunkId;
    private String tenantId;
    private String docId;
    private String contentType; // 'text', 'image'
    private String content;     // 核心内容
    private String vectorId;
    private Integer pageNumber;
    private Integer chunkIndex;
    private Integer tokenCount;
    private LocalDateTime createTime;
}