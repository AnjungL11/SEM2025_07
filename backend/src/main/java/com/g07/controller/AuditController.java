package com.g07.controller;

import com.g07.common.R;
import com.g07.entity.AuditLog;
import com.g07.mapper.AuditLogMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/audit")
@CrossOrigin
public class AuditController {

    @Autowired
    private AuditLogMapper auditLogMapper;

    /**
     * 获取所有日志
     */
    @GetMapping("/logs")
    public R<List<AuditLog>> getAllLogs() {
        QueryWrapper<AuditLog> query = new QueryWrapper<>();
        query.orderByDesc("timestamp"); // 按时间倒序
        return R.ok(auditLogMapper.selectList(query));
    }
}