package com.devnovus.oneBox.domain.folder.controller;

import com.devnovus.oneBox.domain.folder.dto.CreateFolderRequest;
import com.devnovus.oneBox.domain.folder.dto.DeleteFolderRequest;
import com.devnovus.oneBox.domain.folder.dto.MoveFolderRequest;
import com.devnovus.oneBox.domain.folder.dto.RenameFolderRequest;
import com.devnovus.oneBox.domain.folder.service.FolderServiceV1;
import com.devnovus.oneBox.domain.folder.service.FolderServiceV2;
import com.devnovus.oneBox.domain.metadata.dto.MetadataResponse;
import com.devnovus.oneBox.global.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/folders/v2")
@RequiredArgsConstructor
public class FolderControllerV2 {
    private final FolderServiceV2 folderService;

    /** 폴더생성 */
    @PostMapping
    public ResponseEntity<BaseResponse<Map<String, Long>>> createFolder(
            @Valid @RequestBody CreateFolderRequest request
    ) {
        Long id = folderService.createFolder(request);
        return ResponseEntity.status(201).body(BaseResponse.of(Map.of("id", id)));
    }

    /** 폴더탐색 */
    @GetMapping("/list/{folderId}")
    public ResponseEntity<BaseResponse<List<MetadataResponse>>> getListInFolder(
            @PathVariable Long folderId
    ) {
        List<MetadataResponse> resources = folderService.getListInFolder(folderId);
        return ResponseEntity.ok().body(BaseResponse.of(resources));
    }

    /** 폴더이동 */
    @PatchMapping("/{folderId}/move")
    public ResponseEntity<BaseResponse<String>> moveFolder(
            @PathVariable Long folderId,
            @RequestBody MoveFolderRequest request
    ) {
        folderService.moveFolder(folderId, request);
        return ResponseEntity.ok().body(BaseResponse.of("폴더이동완료"));
    }

    /** 폴더이름수정 */
    @PatchMapping("/{folderId}/name")
    public ResponseEntity<BaseResponse<String>> renameFolder(
            @PathVariable Long folderId,
            @RequestBody RenameFolderRequest request
    ) {
        folderService.renameFolder(folderId, request);
        return ResponseEntity.ok().body(BaseResponse.of("폴더이름수정완료"));
    }

    /** 폴더삭제 */
    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(
            @PathVariable Long folderId,
            @RequestBody DeleteFolderRequest request
    ) {
        folderService.deleteFolder(folderId, request);
        return ResponseEntity.noContent().build();
    }
}
