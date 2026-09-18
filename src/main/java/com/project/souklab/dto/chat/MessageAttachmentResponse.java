package com.project.souklab.dto.chat;
public record MessageAttachmentResponse(String id, String filename, String contentType, long size, String downloadUrl) {}
