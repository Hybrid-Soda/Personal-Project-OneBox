package com.devnovus.oneBox.web.util;

import java.util.Map;

public class MimeTypeResolver {
    private static final Map<String, String> MIME_TYPES = Map.ofEntries(
            // 애플리케이션
            Map.entry("json", "application/json"),
            Map.entry("pdf", "application/pdf"),
            Map.entry("zip", "application/zip"),
            Map.entry("gz", "application/gzip"),
            Map.entry("tar", "application/x-tar"),
            Map.entry("xml", "application/xml"),
            // 오디오
            Map.entry("mp3", "audio/mpeg"),
            Map.entry("aac", "audio/aac"),
            Map.entry("ogg", "audio/ogg"),
            // 이미지
            Map.entry("jpg", "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("png", "image/png"),
            Map.entry("gif", "image/gif"),
            Map.entry("webp", "image/webp"),
            Map.entry("bmp", "image/bmp"),
            // 텍스트
            Map.entry("txt", "text/plain"),
            Map.entry("html", "text/html"),
            Map.entry("css", "text/css"),
            Map.entry("csv", "text/csv"),
            // 비디오
            Map.entry("mp4", "video/mp4"),
            Map.entry("avi", "video/x-msvideo")
    );

    private static final String DEFAULT_MIME = "application/octet-stream";

    public static String getMimeType(String ext) {
        if (ext == null || ext.isEmpty()) {
            return DEFAULT_MIME;
        }
        return MIME_TYPES.getOrDefault(ext.toLowerCase(), DEFAULT_MIME);
    }
}
