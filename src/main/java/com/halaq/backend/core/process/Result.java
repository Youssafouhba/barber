package com.halaq.backend.core.process;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.halaq.backend.core.process.Message;
import com.halaq.backend.core.process.MessageType;
import com.halaq.backend.core.process.Message;
import com.halaq.backend.core.process.MessageType;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<I, K,T> {
    private List<com.halaq.backend.core.process.Message> messages;
    private List<com.halaq.backend.core.process.Message> errors;
    private List<com.halaq.backend.core.process.Message> warnings;
    private List<com.halaq.backend.core.process.Message> infos;
    private HttpStatus status = HttpStatus.OK;
    private String message;
    private T item;
    private I input;
    private K output;


    public Result(HttpStatus status) {
        this.status = status;
    }

    public Result(I input, K output) {
        this.input = input;
        this.output = output;
    }

    public Result() {

    }

    public boolean hasNoError() {
        return getErrors().isEmpty();
    }

    public void addErrorMessage(String message) {
        addMessage(message, com.halaq.backend.core.process.MessageType.ERROR);
        this.status = HttpStatus.PRECONDITION_FAILED;
    }

    public void addInfoMessage(String message) {
        addMessage(message, com.halaq.backend.core.process.MessageType.INFO);
        this.status = HttpStatus.OK;
    }

    public void addWarningMessage(String message) {
        addMessage(message, com.halaq.backend.core.process.MessageType.WARN);
        this.status = HttpStatus.SEE_OTHER;
    }

    private void addMessage(String messageText, com.halaq.backend.core.process.MessageType type) {
        com.halaq.backend.core.process.Message myMessage = new com.halaq.backend.core.process.Message(messageText, type);
        if (type == com.halaq.backend.core.process.MessageType.ERROR) {
            getErrors().add(myMessage);
        } else if (type == com.halaq.backend.core.process.MessageType.WARN) {
            getWarnings().add(myMessage);
        } else if (type == MessageType.INFO) {
            getInfos().add(myMessage);
        }
        getMessages().add(myMessage);
    }

    public void constructTextMessage() {
        if (message == null) {
            message = "";
        }
        List<com.halaq.backend.core.process.Message> myMessages = getMessages();
        for (com.halaq.backend.core.process.Message myMessage : myMessages) {
            message += myMessage.getLabel() + ", ";
        }
        if (!message.isEmpty())
            message = message.substring(0, message.length() - 2);
    }

    public List<com.halaq.backend.core.process.Message> getMessages() {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        return messages;
    }

    public void setMessages(List<com.halaq.backend.core.process.Message> messages) {
        this.messages = messages;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public I getInput() {
        return input;
    }

    public void setInput(I input) {
        this.input = input;
    }

    public T getItem() {
        return item;
    }

    public void setItem(T item) {
        this.item = item;
    }


    public K getOutput() {
        return output;
    }

    public void setOutput(K output) {
        this.output = output;
    }

    public List<com.halaq.backend.core.process.Message> getErrors() {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        return errors;
    }

    public void setErrors(List<com.halaq.backend.core.process.Message> errors) {
        this.errors = errors;
    }

    public List<com.halaq.backend.core.process.Message> getWarnings() {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }
        return warnings;
    }

    public void setWarnings(List<com.halaq.backend.core.process.Message> warnings) {
        this.warnings = warnings;
    }

    public List<com.halaq.backend.core.process.Message> getInfos() {
        if (infos == null) {
            infos = new ArrayList<>();
        }
        return infos;
    }

    public void setInfos(List<Message> infos) {
        this.infos = infos;
    }

}

