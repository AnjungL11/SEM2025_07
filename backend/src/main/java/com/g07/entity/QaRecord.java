package com.g07.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("qa_record")
public class QaRecord {
    @TableId
    private String recordId;
    private String sessionId;
    private String question;
    private String answer;
    private LocalDateTime qaTime;

    // --- 手动添加 Getters 和 Setters ---

    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public LocalDateTime getQaTime() { return qaTime; }
    public void setQaTime(LocalDateTime qaTime) { this.qaTime = qaTime; }
}