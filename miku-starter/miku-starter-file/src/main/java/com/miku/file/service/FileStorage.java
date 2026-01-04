package com.miku.file.service;

import com.miku.file.entity.FileInfo;

import java.io.InputStream;

/**
 * 文件存储接口
 *
 * @author miku
 * @date 2025-11-03
 */
public interface FileStorage {
    
    /**
     * 上传文件
     *
     * @param inputStream 文件输入流
     * @param originalFileName 原始文件名
     * @param contentType 内容类型
     * @return 文件信息
     */
    FileInfo upload(InputStream inputStream, String originalFileName, String contentType);
    
    /**
     * 上传文件（指定路径）
     *
     * @param inputStream 文件输入流
     * @param path 存储路径
     * @param originalFileName 原始文件名
     * @param contentType 内容类型
     * @return 文件信息
     */
    FileInfo upload(InputStream inputStream, String path, String originalFileName, String contentType);

    /**
     * 上传并按内容哈希命名（用于去重，如头像）。targetDir 指定目录（如 "avatars"），
     * 最终文件名为 {hash}.{ext}，返回 FileInfo，若已存在则复用。
     */
    FileInfo uploadWithHash(InputStream inputStream, String targetDir, String originalFileName, String contentType);
    
    /**
     * 下载文件
     *
     * @param path 文件路径
     * @return 文件输入流
     */
    InputStream download(String path);
    
    /**
     * 删除文件
     *
     * @param path 文件路径
     * @return 是否成功
     */
    boolean delete(String path);
    
    /**
     * 检查文件是否存在
     *
     * @param path 文件路径
     * @return 是否存在
     */
    boolean exists(String path);
    
    /**
     * 获取文件访问 URL
     *
     * @param path 文件路径
     * @return 访问 URL
     */
    String getUrl(String path);
    
    /**
     * 获取文件信息
     *
     * @param path 文件路径
     * @return 文件信息
     */
    FileInfo getFileInfo(String path);
}

