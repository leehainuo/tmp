package com.miku.file.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件信息
 *
 * @author miku
 * @date 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {
    
    /**
     * 文件名
     */
    private String fileName;
    
    /**
     * 原始文件名
     */
    private String originalFileName;
    
    /**
     * 文件扩展名
     */
    private String extension;
    
    /**
     * 文件大小（字节）
     */
    private Long size;
    
    /**
     * 内容类型
     */
    private String contentType;
    
    /**
     * 存储路径
     */
    private String path;
    
    /**
     * 访问 URL
     */
    private String url;
    
    /**
     * 存储类型
     */
    private String storageType;
    
    /**
     * 上传时间
     */
    private Long uploadTime;
}

