package com.daoninhthai.chatapp.controller;

import com.daoninhthai.chatapp.entity.ChatAttachment;
import com.daoninhthai.chatapp.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "messageId", required = false) Long messageId) {
        try {
            ChatAttachment attachment = fileStorageService.storeFile(file, messageId);

            Map<String, Object> response = new HashMap<>();
            response.put("id", attachment.getId());
            response.put("fileName", attachment.getFileName());
            response.put("originalName", attachment.getOriginalName());
            response.put("fileType", attachment.getFileType());
            response.put("fileSize", attachment.getFileSize());
            response.put("downloadUrl", attachment.getDownloadUrl());
            response.put("uploadedAt", attachment.getUploadedAt().toString());

            logger.info("File uploaded successfully: {}", attachment.getOriginalName());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Error uploading file: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to upload file");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            Resource resource = fileStorageService.getFileAsResource(filename);

            String contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            logger.error("Error downloading file {}: {}", filename, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{filename}")
    public ResponseEntity<Map<String, String>> deleteFile(@PathVariable String filename) {
        try {
            fileStorageService.deleteFile(filename);

            Map<String, String> response = new HashMap<>();
            response.put("message", "File deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deleting file {}: {}", filename, e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete file");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/message/{messageId}")
    public ResponseEntity<List<ChatAttachment>> getAttachmentsByMessage(
            @PathVariable Long messageId) {
        List<ChatAttachment> attachments = fileStorageService.getAttachmentsByMessageId(messageId);
        return ResponseEntity.ok(attachments);
    }
}
