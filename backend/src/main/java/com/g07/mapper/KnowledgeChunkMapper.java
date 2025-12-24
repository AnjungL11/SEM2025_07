package com.g07.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.g07.entity.KnowledgeChunk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface KnowledgeChunkMapper extends BaseMapper<KnowledgeChunk> {

    /**
     * 简单的关键词检索 (基于 MySQL LIKE)
     * 限制 tenantId 确保数据隔离
     */
    @Select("SELECT * FROM knowledge_chunk " +
            "WHERE tenant_id = #{tenantId} " +
            "AND content LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY create_time DESC LIMIT 5")
    List<KnowledgeChunk> searchByKeyword(@Param("tenantId") String tenantId, @Param("keyword") String keyword);
}