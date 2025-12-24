package com.g07.controller;

import com.g07.common.R;
import com.g07.entity.User;
import com.g07.entity.Tenant;
import com.g07.mapper.UserMapper;
import com.g07.mapper.TenantMapper;
import com.g07.utils.JwtUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TenantMapper tenantMapper;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 登录接口：下发用户信息及 Token
     */
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        
        if (user != null && encoder.matches(password, user.getPasswordHash())) {
            String token = JwtUtils.createToken(user.getUserId(), user.getRole());
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("role", user.getRole());
            data.put("username", user.getUsername());
            data.put("userId", user.getUserId());
            data.put("tenantId", user.getTenantId());
            data.put("email", user.getEmail());
            data.put("createTime", user.getCreateTime() != null ? user.getCreateTime().toString() : "未知");
            
            return R.ok(data);
        }
        return R.error("用户名或密码错误");
    }

    /**
     * 注册接口：支持加入已有企业或创建新企业
     */
    @PostMapping("/register")
    @Transactional
    public R<String> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String tenantName = body.get("tenantName");

        // 1. 检查用户名是否已占用
        User existingUser = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (existingUser != null) {
            return R.error("该用户名已被注册");
        }

        // 2. 处理企业（租户）逻辑
        // 尝试根据名称寻找已存在的企业
        Tenant existingTenant = tenantMapper.selectOne(new QueryWrapper<Tenant>().eq("tenant_name", tenantName));
        String targetTenantId;

        if (existingTenant != null) {
            // 场景 A: 企业已存在，获取其 ID，实现“加入企业”
            targetTenantId = existingTenant.getTenantId();
        } else {
            // 场景 B: 企业不存在，创建新企业
            targetTenantId = UUID.randomUUID().toString().replace("-", "");
            Tenant newTenant = new Tenant();
            newTenant.setTenantId(targetTenantId);
            newTenant.setTenantName(tenantName);
            newTenant.setStatus("active");
            newTenant.setCreateTime(LocalDateTime.now());
            tenantMapper.insert(newTenant);
        }

        // 3. 创建新用户并绑定到该企业 ID
        User user = new User();
        user.setUserId(UUID.randomUUID().toString().replace("-", ""));
        user.setTenantId(targetTenantId); // 关键：同一企业的用户共享此 ID
        user.setUsername(username);
        user.setPasswordHash(encoder.encode(body.get("password")));
        user.setEmail(body.get("email"));

        // ================== 修改点开始 ==================
        // 判断企业名称是否为 admin，如果是，则设置角色为 admin，否则默认为 researcher
        if ("admin".equals(tenantName)) {
            user.setRole("admin");
        } else {
            user.setRole("researcher");
        }
        // ================== 修改点结束 ==================

        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);

        return R.ok(existingTenant != null ? "成功加入企业：" + tenantName : "注册成功，已创建新企业");
    }
}