package com.daoninhthai.chatapp.service;

import com.daoninhthai.chatapp.config.FileStorageConfig;
import com.daoninhthai.chatapp.entity.ChatAttachment;
import com.daoninhthai.chatapp.repository.ChatAttachmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Autowired
    private FileStorageConfig fileStorageConfig;

    @Autowired
    private ChatAttachmentRepository attachmentRepository;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        uploadPath = Paths.get(fileStorageConfig.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadPath);
            logger.info("Upload directory created at: {}", uploadPath);
        } catch (IOException e) {
            logger.error("Could not create upload directory: {}", e.getMessage());
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public ChatAttachment storeFile(MultipartFile file, Long messageId) {
        validateFile(file);

        String uniqueFilename = generateUniqueFilename(file.getOriginalFilename());

        try {
            Path targetLocation = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            ChatAttachment attachment = ChatAttachment.builder()
                    .messageId(messageId)
                    .fileName(uniqueFilename)
                    .originalName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .downloadUrl("/api/files/" + uniqueFilename)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            return attachmentRepository.save(attachment);
        } catch (IOException e) {
            logger.error("Could not store file {}: {}", file.getOriginalFilename(), e.getMessage());
            throw new RuntimeException("Could not store file " + file.getOriginalFilename(), e);
        }
    }

    public String generateUniqueFilename(String originalFilename) {
        String extension = fileStorageConfig.getFileExtension(originalFilename);
        String baseName = UUID.randomUUID().toString();
        return extension.isEmpty() ? baseName : baseName + "." + extension;
    }

    public Resource getFileAsResource(String filename) {
        try {
            Path filePath = uploadPath.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                logger.warn("File not found or not readable: {}", filename);
                throw new RuntimeException("File not found: " + filename);
            }
        } catch (MalformedURLException e) {
            logger.error("Malformed URL for file {}: {}", filename, e.getMessage());
            throw new RuntimeException("File not found: " + filename, e);
        }
    }

    public void deleteFile(String filename) {
        try {
            Path filePath = uploadPath.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
            attachmentRepository.deleteByFileName(filename);
            logger.info("File deleted: {}", filename);
        } catch (IOException e) {
            logger.error("Could not delete file {}: {}", filename, e.getMessage());
            throw new RuntimeException("Could not delete file: " + filename, e);
        }
    }

    public void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }

        if (!fileStorageConfig.isFileSizeAllowed(file.getSize())) {
            throw new IllegalArgumentException(
                    "File size exceeds maximum allowed size of " +
                    (fileStorageConfig.getMaxFileSize() / (1024 * 1024)) + "MB");
        }

        if (file.getContentType() != null && !fileStorageConfig.isFileTypeAllowed(file.getContentType())) {
            throw new IllegalArgumentException("File type " + file.getContentType() + " is not allowed");
        }

        if (file.getOriginalFilename() != null && !fileStorageConfig.isExtensionAllowed(file.getOriginalFilename())) {
            throw new IllegalArgumentException("File extension is not allowed");
        }
    }

    public List<ChatAttachment> getAttachmentsByMessageId(Long messageId) {
        return attachmentRepository.findByMessageId(messageId);
    }
}
