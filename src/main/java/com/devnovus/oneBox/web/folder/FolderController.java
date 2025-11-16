package com.devnovus.oneBox.web.folder;

import com.devnovus.oneBox.aop.dto.BaseResponse;
import com.devnovus.oneBox.web.folder.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class FolderController {
    private final FolderService folderService;

    /** 폴더생성 */
    @PostMapping
    public ResponseEntity<BaseResponse<String>> createFolder(
            @Valid @RequestBody CreateFolderRequest request
    ) {
        folderService.createFolder(request);
        return ResponseEntity.status(201).body(BaseResponse.of("폴더생성완료"));
    }

    /** 폴더탐색 */
    @GetMapping("/list/{folderId}")
    public ResponseEntity<BaseResponse<List<MetadataResponse>>> getListInFolder(
            @PathVariable Long folderId
    ) {
        List<MetadataResponse> resources = folderService.getListInFolder(folderId);
        return ResponseEntity.ok().body(BaseResponse.of(resources));
    }

    /** 폴더수정 */
    @PutMapping("/{folderId}")
    public ResponseEntity<BaseResponse<String>> updateFolder(
            @PathVariable Long folderId,
            @RequestBody UpdateFolderRequest request
    ) {
        folderService.updateFolder(folderId, request);
        return ResponseEntity.ok().body(BaseResponse.of("폴더수정완료"));
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
