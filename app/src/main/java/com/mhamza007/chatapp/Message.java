package com.mhamza007.chatapp;

public class Message {
    String from, message, type, to, messageId, date, time, name;

    public Message() {
    }

    public Message(String from, String message, String type, String to, String messageId, String date, String time, String name) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.messageId = messageId;
        this.date = date;
        this.time = time;
        this.name = name;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFrom() {
        return from;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String getTo() {
        return to;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getName() {
        return name;
    }
}
