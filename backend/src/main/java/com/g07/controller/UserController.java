package com.g07.controller;

import com.g07.common.R;
import com.g07.entity.AuditLog;
import com.g07.entity.User;
import com.g07.mapper.AuditLogMapper;
import com.g07.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AuditLogMapper auditLogMapper;

    /**
     * 辅助方法：记录审计日志
     * 【修改】增加记录 username
     */
    private void recordLog(User operator, String type, String target, String detail) {
        try {
            AuditLog log = new AuditLog();
            
            String tenantId = (operator != null && operator.getTenantId() != null) ? operator.getTenantId() : "000000";
            String userId = (operator != null) ? operator.getUserId() : "unknown";
            String username = (operator != null) ? operator.getUsername() : "unknown"; // 获取用户名

            log.setTenantId(tenantId);
            log.setUserId(userId);
            log.setUsername(username); // 设置用户名
            
            log.setOperationType(type);
            log.setTargetResource(target);
            log.setOperationDetail(detail); 
            log.setTimestamp(LocalDateTime.now());
            log.setStatus("SUCCESS");
            
            auditLogMapper.insert(log);
        } catch (Exception e) {
            System.err.println("审计日志记录失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户列表 (仅管理员可见)
     * 【修改】调用自定义 Mapper 方法获取企业名称
     */
    @GetMapping("/list")
    public R<List<User>> getUserList() {
        // 使用联表查询，获取 tenantName
        return R.ok(userMapper.selectListWithTenantName());
    }

    /**
     * 修改用户角色
     */
    @PostMapping("/updateRole")
    @Transactional(rollbackFor = Exception.class)
    public R<String> updateUserRole(@RequestBody Map<String, String> body) {
        String adminId = body.get("adminId"); 
        String targetUserId = body.get("targetUserId");
        String newRole = body.get("newRole"); 

        User admin = userMapper.selectById(adminId);
        if (admin == null || !"admin".equals(admin.getRole())) {
            return R.error("权限不足");
        }

        User targetUser = userMapper.selectById(targetUserId);
        if (targetUser != null) {
            targetUser.setRole(newRole);
            userMapper.updateById(targetUser);
            
            recordLog(admin, "UPDATE_ROLE", targetUser.getUsername(), "修改角色为: " + newRole);
            return R.ok("角色修改成功");
        }
        return R.error("目标用户不存在");
    }

    /**
     * 注销/删除账户
     */
    @DeleteMapping("/delete")
    @Transactional(rollbackFor = Exception.class)
    public R<String> deleteUser(@RequestParam String adminId, @RequestParam String targetUserId) {
        User admin = userMapper.selectById(adminId);
        if (admin == null || !"admin".equals(admin.getRole())) {
            return R.error("权限不足");
        }

        User targetUser = userMapper.selectById(targetUserId);
        if (targetUser != null) {
            userMapper.deleteById(targetUserId);
            recordLog(admin, "DELETE_USER", targetUser.getUsername(), "注销用户账户");
            return R.ok("用户已注销");
        }
        return R.error("用户不存在");
    }
}