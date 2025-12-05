package com.devnovus.oneBox.domain.file.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FileMoveRequest {
    private Long parentFolderId; // 상위폴더아이디
}