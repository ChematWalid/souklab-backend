package com.project.souklab.dto.chat;

import com.project.souklab.model.EnumValue;

/** Stable keys used by the structured payloads sent with chat events. */
public final class ChatMetadata {
    private ChatMetadata() {
    }

    public interface Key extends EnumValue {
    }

    public enum Presence implements Key {
        USERNAME("username"), ONLINE("online");

        private final String value;

        Presence(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }
    }

    public enum Typing implements Key {
        USERNAME("username"), TYPING("typing");

        private final String value;

        Typing(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }
    }

    public enum Read implements Key {
        READER("reader"), MESSAGE_ID("messageId");

        private final String value;

        Read(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }
    }

    public enum Command implements Key {
        MESSAGE("message");

        private final String value;

        Command(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }
    }
}
