package com.g07.controller;

import com.g07.common.R;
import com.g07.entity.QaRecord;
import com.g07.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
@CrossOrigin
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * 智能问答接口
     * POST /chat/completions
     */
    @PostMapping("/completions")
    public R<Map<String, Object>> completions(@RequestBody Map<String, String> body,
                                              @RequestHeader("X-User-Id") String userId,
                                              @RequestHeader("X-Tenant-Id") String tenantId) {
        String question = body.get("question");
        String sessionId = body.get("sessionId"); // 第一次请求时可为空

        if (question == null || question.trim().isEmpty()) {
            return R.error("问题不能为空");
        }

        try {
            // 调用 Service 获取 AI 回答
            Map<String, Object> result = chatService.chat(userId, tenantId, sessionId, question);
            return R.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("AI 服务暂时不可用: " + e.getMessage());
        }
    }

    /**
     * 获取会话历史记录
     * GET /chat/history?sessionId=xxx
     */
    @GetMapping("/history")
    public R<List<QaRecord>> history(@RequestParam String sessionId) {
        return R.ok(chatService.getHistory(sessionId));
    }
}