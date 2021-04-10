package com.daoninhthai.chatapp.config;

import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class FileStorageConfig {

    private String uploadDir = "uploads";

    private long maxFileSize = 10 * 1024 * 1024; // 10MB

    private List<String> allowedTypes = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "application/pdf",
            "text/plain",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/zip",
            "application/x-rar-compressed"
    );

    private List<String> allowedExtensions = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp",
            "pdf", "txt", "doc", "docx", "zip", "rar"
    );

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public List<String> getAllowedTypes() {
        return allowedTypes;
    }

    public void setAllowedTypes(List<String> allowedTypes) {
        this.allowedTypes = allowedTypes;
    }

    public List<String> getAllowedExtensions() {
        return allowedExtensions;
    }

    public void setAllowedExtensions(List<String> allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }

    public boolean isFileTypeAllowed(String contentType) {
        return allowedTypes.contains(contentType);
    }

    public boolean isFileSizeAllowed(long size) {
        return size <= maxFileSize;
    }

    public boolean isExtensionAllowed(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return allowedExtensions.contains(extension);
    }

    public String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}
