package com.halaq.backend.core.transverse.emailling.dto;

public class SaveTextRequest {
    private String key;
    private String value;
    private String language;
    private String category;

    // Getters et Setters
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}