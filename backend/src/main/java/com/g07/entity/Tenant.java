package com.g07.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("tenant")
public class Tenant {
    @TableId
    private String tenantId;
    private String tenantName;
    private String status;
    private Long maxStorage;
    private LocalDateTime createTime;

    // --- 手动添加 Getters 和 Setters ---

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getMaxStorage() { return maxStorage; }
    public void setMaxStorage(Long maxStorage) { this.maxStorage = maxStorage; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}