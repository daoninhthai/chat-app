package com.daoninhthai.chatapp.controller;

import com.daoninhthai.chatapp.entity.Bookmark;
import com.daoninhthai.chatapp.service.BookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    @Autowired
    private BookmarkService bookmarkService;

    @PostMapping
    public ResponseEntity<?> addBookmark(
            @RequestParam("messageId") Long messageId,
            @RequestParam("userId") Long userId,
            @RequestParam(value = "note", required = false) String note) {
        try {
            Bookmark bookmark = bookmarkService.addBookmark(messageId, userId, note);

            Map<String, Object> response = new HashMap<>();
            response.put("id", bookmark.getId());
            response.put("messageId", bookmark.getMessageId());
            response.put("userId", bookmark.getUserId());
            response.put("note", bookmark.getNote());
            response.put("createdAt", bookmark.getCreatedAt().toString());

            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping
    public ResponseEntity<Map<String, String>> removeBookmark(
            @RequestParam("messageId") Long messageId,
            @RequestParam("userId") Long userId) {
        bookmarkService.removeBookmark(messageId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Bookmark removed successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<Bookmark>> getBookmarks(
            @RequestParam("userId") Long userId) {
        List<Bookmark> bookmarks = bookmarkService.getBookmarks(userId);
        return ResponseEntity.ok(bookmarks);
    }

    @PutMapping
    public ResponseEntity<?> updateBookmarkNote(
            @RequestParam("messageId") Long messageId,
            @RequestParam("userId") Long userId,
            @RequestParam(value = "note", required = false) String note) {
        try {
            Bookmark updated = bookmarkService.updateBookmarkNote(messageId, userId, note);

            Map<String, Object> response = new HashMap<>();
            response.put("id", updated.getId());
            response.put("messageId", updated.getMessageId());
            response.put("note", updated.getNote());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
