package com.healthfamily.web.controller;

import com.healthfamily.domain.entity.KnowledgeDocument;
import com.healthfamily.service.KnowledgeService;
import com.healthfamily.web.dto.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/admin/knowledge")
@RequiredArgsConstructor
public class AdminKnowledgeController {

    private final KnowledgeService knowledgeService;

    @PostMapping("/upload")
    public Result<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            String title = file.getOriginalFilename();
            knowledgeService.createDocument(title, content);
            return Result.success("Upload and chunking successful");
        } catch (IOException e) {
            return Result.error(500, "Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<Page<KnowledgeDocument>> getDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(knowledgeService.getParentDocuments(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/{id}/chunks")
    public Result<List<KnowledgeDocument>> getDocumentChunks(@PathVariable Long id) {
        return Result.success(knowledgeService.getChunks(id));
    }

    @PutMapping("/chunk/{id}")
    public Result<String> updateChunk(@PathVariable Long id, @RequestBody ChunkUpdateRequest request) {
        knowledgeService.updateChunk(id, request.getContent());
        return Result.success("Chunk updated");
    }

    @PostMapping("/sync")
    public Result<String> syncToVectorStore() {
        knowledgeService.syncToVectorStore();
        return Result.success("Vector store sync triggered");
    }

    // Existing endpoints kept for compatibility or frontend usage, redirecting to new logic where possible
    @GetMapping("/documents")
    public Result<Page<KnowledgeDocument>> getDocumentsAlias(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return getDocuments(page, size);
    }
    
    @DeleteMapping("/documents/{id}")
    public Result<String> deleteDocument(@PathVariable Long id) {
        knowledgeService.deleteDocument(id);
        return Result.success("Deleted successfully");
    }

    @PostMapping("/documents/{id}/re-embed")
    public Result<String> reEmbedDocument(@PathVariable Long id) {
        knowledgeService.reEmbedDocument(id);
        return Result.success("Re-embed triggered");
    }

    public static class ChunkUpdateRequest {
        private String content;
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
