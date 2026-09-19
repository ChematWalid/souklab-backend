package com.project.souklab.dto.chat;

import com.project.souklab.model.EnumValue;

/** Canonical grouped taxonomy for chat and presence WebSocket events. */
public final class ChatEventType {
    private ChatEventType() { }
    public interface Type extends EnumValue { }
    public enum Message implements Type {
        CREATED("MESSAGE_CREATED"), UPDATED("MESSAGE_UPDATED"), DELETED("MESSAGE_DELETED");
        private final String value; Message(String value) { this.value = value; }
        @Override public String value() { return value; }
    }
    public enum Presence implements Type {
        ONLINE("PRESENCE_ONLINE"), OFFLINE("PRESENCE_OFFLINE");
        private final String value; Presence(String value) { this.value = value; }
        @Override public String value() { return value; }
    }
    public enum Typing implements Type {
        STARTED("TYPING_STARTED"), STOPPED("TYPING_STOPPED");
        private final String value; Typing(String value) { this.value = value; }
        @Override public String value() { return value; }
    }
    public enum Command implements Type {
        ACKNOWLEDGED("COMMAND_ACKNOWLEDGED"), ERROR("COMMAND_ERROR");
        private final String value; Command(String value) { this.value = value; }
        @Override public String value() { return value; }
    }
    public enum Read implements Type {
        UP_TO("READ_UP_TO");
        private final String value; Read(String value) { this.value = value; }
        @Override public String value() { return value; }
    }
}
