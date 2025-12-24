package com.g07.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class DeepSeekService {

    @Value("${ai.api.url}")
    private String aiApiUrl;

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.model}")
    private String model;

    private final RestTemplate restTemplate;

    public DeepSeekService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 原有的 RAG 问答方法 (保持不变，或内部调用下面的通用方法)
     */
    public String getResponse(String question, List<String> context) {
        String systemPrompt = buildSystemPrompt(context);
        return callDeepSeek(systemPrompt, question);
    }

    /**
     * 【新增】专门用于关键词提取的轻量级方法
     * 允许自定义 System Prompt
     */
    public String extractKeywords(String userQuestion) {
        // 专门的提示词，要求 AI 只输出关键词，不要啰嗦
        String systemPrompt = "你是一个文档检索关键词提取助手。请从用户的自然语言描述中，提取出最核心的文件名关键词。" +
                "规则：\n" +
                "1. 只输出关键词，不要包含任何标点符号、解释或多余的话。\n" +
                "2. 去除“请总结”、“帮我查找”、“里的内容”等指令性词汇。\n" +
                "3. 如果用户问题本身就很短（小于5个字），直接原样输出。\n" +
                "4. 举例：用户输入“帮我找一下金可的调研报告”，你应该输出“金可调研报告”。";
        
        return callDeepSeek(systemPrompt, userQuestion);
    }

    /**
     * 【新增】通用的底层调用方法
     */
    private String callDeepSeek(String systemPrompt, String userMessage) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);

            Map<String, Object> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.3); // 提取关键词时温度设低一点，更精准
            requestBody.put("stream", false);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            String endpoint = aiApiUrl.endsWith("/") ? aiApiUrl + "chat/completions" : aiApiUrl + "/chat/completions";

            ResponseEntity<Map> response = restTemplate.postForEntity(endpoint, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            System.err.println("AI 调用异常: " + e.getMessage());
            return userMessage; // 如果 AI 挂了，降级为返回原话
        }
        return userMessage;
    }

    // 原有的构建提示词方法
    private String buildSystemPrompt(List<String> context) {
        StringBuilder sb = new StringBuilder("你是一个专业的产业知识问答助手。");
        if (context != null && !context.isEmpty()) {
            sb.append("请优先基于以下参考文档内容回答问题，回答时请引用文档中的原话或数据：\n");
            for (int i = 0; i < context.size(); i++) {
                sb.append("【文档片段").append(i + 1).append("】：").append(context.get(i)).append("\n");
            }
        } else {
            sb.append("请基于你的通用知识库回答。");
        }
        return sb.toString();
    }
}