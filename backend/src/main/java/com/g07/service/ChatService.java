package com.g07.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.g07.entity.ChatSession;
import com.g07.entity.Document;
import com.g07.entity.QaRecord;
import com.g07.mapper.ChatSessionMapper;
import com.g07.mapper.DocumentMapper;
import com.g07.mapper.QaRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChatService {

    @Autowired private DeepSeekService deepSeekService;
    @Autowired private ChatSessionMapper sessionMapper;
    @Autowired private QaRecordMapper qaRecordMapper;
    @Autowired private DocumentMapper documentMapper;
    @Autowired private FileService fileService;

    @Transactional
    public Map<String, Object> chat(String userId, String tenantId, String sessionId, String question) {
        
        // 1. 初始化会话
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = UUID.randomUUID().toString().replace("-", "");
            ChatSession session = new ChatSession();
            session.setSessionId(sessionId);
            session.setUserId(userId);
            session.setTenantId(tenantId);
            session.setTitle(question.length() > 20 ? question.substring(0, 20) : question);
            session.setStartTime(LocalDateTime.now());
            sessionMapper.insert(session);
        }

        List<String> context = new ArrayList<>();
        List<String> citations = new ArrayList<>();

        System.out.println("========== AI 智能检索流程 ==========");
        System.out.println("用户原话: " + question);

        // =========================================================
        // 【核心优化】Step 1: 利用 AI 从长难句中提取核心关键词
        // =========================================================
        String extractedKeyword = question;
        
        // 只有当问题比较长（比如超过4个字）时，才调用 AI 提取，节省时间
        if (question.length() > 4) {
            extractedKeyword = deepSeekService.extractKeywords(question);
            // 清洗一下 AI 可能返回的标点
            extractedKeyword = extractedKeyword.replace("。", "").replace("：", "").trim();
            System.out.println(">>> AI 提取的关键词: [" + extractedKeyword + "]");
        }

        // =========================================================
        // Step 2: 使用提取出的关键词查库
        // =========================================================
        List<Document> matchedDocs = new ArrayList<>();
        
        // 策略 A: 优先用 AI 提取的精准关键词搜
        if (extractedKeyword != null && !extractedKeyword.isEmpty()) {
            matchedDocs = documentMapper.searchFuzzy(tenantId, extractedKeyword);
        }

        // 策略 B: 如果 AI 提取的词没搜到，尝试用原话模糊搜 (兜底)
        if (matchedDocs.isEmpty() && !extractedKeyword.equals(question)) {
            System.out.println(">>> AI 关键词未命中，降级使用原话模糊搜索");
            matchedDocs = documentMapper.searchFuzzy(tenantId, question);
        }

        System.out.println(">>> 最终找到相关文档数: " + matchedDocs.size());

        // =========================================================
        // Step 3: 读取文档内容并构建 Prompt
        // =========================================================
        for (Document doc : matchedDocs) {
            System.out.println(">>> 读取文件: " + doc.getDocName());
            String fileContent = fileService.extractTextFromMinio(doc.getFilePath());
            
            if (fileContent != null && !fileContent.trim().isEmpty()) {
                // 截取防止超长
                if (fileContent.length() > 6000) {
                    fileContent = fileContent.substring(0, 6000) + "...(下文省略)";
                }
                context.add("【参考文档：" + doc.getDocName() + "】\n" + fileContent);
                citations.add(doc.getDocName());
            }
        }

        if (citations.isEmpty()) {
            context.add("系统提示：在知识库中未找到与“" + extractedKeyword + "”相关的文档。请基于通用知识回答，并礼貌告知用户未引用企业文档。");
        }

        // =========================================================
        // Step 4: 将提取的内容 + 用户原话 发送给 AI 生成最终回答
        // =========================================================
        String answer = deepSeekService.getResponse(question, context);

        // 5. 保存记录
        QaRecord record = new QaRecord();
        record.setRecordId(UUID.randomUUID().toString().replace("-", ""));
        record.setSessionId(sessionId);
        record.setQuestion(question);
        record.setAnswer(answer);
        record.setQaTime(LocalDateTime.now());
        qaRecordMapper.insert(record);

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("answer", answer);
        result.put("citations", citations); 
        
        return result;
    }

    public List<QaRecord> getHistory(String sessionId) {
        QueryWrapper<QaRecord> query = new QueryWrapper<>();
        query.eq("session_id", sessionId).orderByAsc("qa_time");
        return qaRecordMapper.selectList(query);
    }
}