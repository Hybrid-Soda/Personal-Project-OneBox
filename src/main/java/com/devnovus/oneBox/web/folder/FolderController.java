package com.devnovus.oneBox.web.folder;

import com.devnovus.oneBox.aop.dto.BaseResponse;
import com.devnovus.oneBox.web.folder.dto.CreateFolderRequest;
import com.devnovus.oneBox.web.folder.dto.UpdateFolderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/folder")
@RequiredArgsConstructor
public class FolderController {
    private final FolderService folderService;

    /** 폴더생성 */
    @PostMapping
    public ResponseEntity<BaseResponse<String>> createFolder(
            @RequestBody CreateFolderRequest request
    ) {
        folderService.createFolder(request);
        return ResponseEntity.status(201).body(BaseResponse.of("폴더생성완료"));
    }

    /** 폴더조회 */
    @GetMapping("/{folderId}")
    public ResponseEntity<BaseResponse<String>> getFolder(
            @PathVariable Long folderId
    ) {
        folderService.getFolder(folderId);
        return ResponseEntity.ok().body(BaseResponse.of("s"));
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
            @PathVariable Long folderId
    ) {
        folderService.deleteFolder(folderId);
        return ResponseEntity.noContent().build();
    }
}
