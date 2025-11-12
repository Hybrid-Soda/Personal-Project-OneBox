package com.devnovus.oneBox.web.folder.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateFolderRequest {
    private Long userId;         // 유저아이디
    private Long parentFolderId; // 상위폴더아이디
    private String folderName;   // 폴더이름
}
