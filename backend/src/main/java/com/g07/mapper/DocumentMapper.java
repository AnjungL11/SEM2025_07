package com.g07.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.g07.entity.Document;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface DocumentMapper extends BaseMapper<Document> {

    /**
     * 【关键方法】searchFuzzy
     * 混合模糊搜索：
     * 1. 匹配文件名包含关键词 (doc_name LIKE %keyword%)
     * 2. 匹配关键词包含文件名 (keyword LIKE %doc_name%) - 用于处理用户直接说文件名的情况
     */
    @Select("SELECT * FROM document " +
            "WHERE tenant_id = #{tenantId} " +
            "AND (" +
            "    doc_name LIKE CONCAT('%', #{keyword}, '%') " +
            "    OR #{keyword} LIKE CONCAT('%', REPLACE(doc_name, substring_index(doc_name, '.', -1), ''), '%') " +
            ") " +
            "ORDER BY upload_time DESC LIMIT 3")
    List<Document> searchFuzzy(@Param("tenantId") String tenantId, @Param("keyword") String keyword);
}