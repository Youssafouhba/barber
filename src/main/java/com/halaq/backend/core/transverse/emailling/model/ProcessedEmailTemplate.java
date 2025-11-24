package com.halaq.backend.core.transverse.emailling.model;

public class ProcessedEmailTemplate {
    private String name;
    private String subject;
    private String content;
    private String contentType;

    public ProcessedEmailTemplate(String name, String subject, String content, String contentType) {
        this.name = name;
        this.subject = subject;
        this.content = content;
        this.contentType = contentType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
