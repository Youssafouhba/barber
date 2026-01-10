package com.halaq.backend.core.process;

import com.halaq.backend.core.process.MessageType;
import com.halaq.backend.core.process.MessageType;

public class Message {
    private String label;
    private String code;
    private MessageType messageType;

    public Message() {
    }

    public Message(String code, MessageType messageType) {
        this.code = code;
        this.messageType = messageType;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "Message{" +
            ", code='" + code + '\'' +
            "label='" + label + '\'' +
            ", messageType=" + messageType +
        '}';
    }
}
