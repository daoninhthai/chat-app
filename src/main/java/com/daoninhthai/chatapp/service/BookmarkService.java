package com.daoninhthai.chatapp.service;

import com.daoninhthai.chatapp.entity.Bookmark;
import com.daoninhthai.chatapp.repository.BookmarkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookmarkService {

    private static final Logger logger = LoggerFactory.getLogger(BookmarkService.class);

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Transactional
    public Bookmark addBookmark(Long messageId, Long userId, String note) {
        // Check if already bookmarked
        if (bookmarkRepository.existsByMessageIdAndUserId(messageId, userId)) {
            throw new IllegalStateException("Message is already bookmarked");
        }

        Bookmark bookmark = Bookmark.builder()
                .messageId(messageId)
                .userId(userId)
                .note(note != null ? note.trim() : null)
                .createdAt(LocalDateTime.now())
                .build();

        Bookmark saved = bookmarkRepository.save(bookmark);
        logger.info("User {} bookmarked message {}", userId, messageId);
        return saved;
    }

    @Transactional
    public void removeBookmark(Long messageId, Long userId) {
        bookmarkRepository.deleteByMessageIdAndUserId(messageId, userId);
        logger.info("User {} removed bookmark for message {}", userId, messageId);
    }

    public List<Bookmark> getBookmarks(Long userId) {
        return bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Bookmark updateBookmarkNote(Long messageId, Long userId, String note) {
        Bookmark bookmark = bookmarkRepository.findByMessageIdAndUserId(messageId, userId)
                .orElseThrow(() -> new RuntimeException("Bookmark not found"));

        bookmark.setNote(note != null ? note.trim() : null);
        return bookmarkRepository.save(bookmark);
    }

    public boolean isBookmarked(Long messageId, Long userId) {
        return bookmarkRepository.existsByMessageIdAndUserId(messageId, userId);
    }

    public long getBookmarkCount(Long userId) {
        return bookmarkRepository.countByUserId(userId);
    }
}
