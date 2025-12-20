package com.g07.queryservice.repository;

import com.g07.queryservice.entity.QaRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QaRecordRepository extends JpaRepository<QaRecord, Long> {
    List<QaRecord> findBySessionId(String sessionId);
    
    /**
     * 按会话ID查找并按创建时间倒序排列
     * @param sessionId 会话ID
     * @return 按时间倒序排列的对话记录
     */
    List<QaRecord> findBySessionIdOrderByCreatedAtDesc(String sessionId);
    
    /**
     * 按租户ID和会话ID查找并按创建时间倒序排列（多租户隔离）
     * @param tenantId 租户ID
     * @param sessionId 会话ID
     * @return 按时间倒序排列的对话记录
     */
    List<QaRecord> findByTenantIdAndSessionIdOrderByCreatedAtDesc(String tenantId, String sessionId);
    
    /**
     * 按租户ID查找（多租户隔离）
     * @param tenantId 租户ID
     * @return 该租户的所有对话记录
     */
    List<QaRecord> findByTenantId(String tenantId);
}