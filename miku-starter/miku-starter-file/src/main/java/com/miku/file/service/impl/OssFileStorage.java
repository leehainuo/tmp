package com.miku.file.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.miku.file.config.FileProperties;
import com.miku.file.entity.FileInfo;
import com.miku.file.service.FileStorage;
import com.miku.file.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import com.miku.file.util.ContentHashUtil;

/**
 * 阿里云 OSS 文件存储实现
 *
 * @author miku
 * @date 2025-11-03
 */
@Slf4j
@RequiredArgsConstructor
public class OssFileStorage implements FileStorage {
    
    private final OSS ossClient;
    private final FileProperties.OssConfig config;
    
    @Override
    public FileInfo upload(InputStream inputStream, String originalFileName, String contentType) {
        String fileName = FileUtils.generateFileName(originalFileName);
        String path = config.getPrefix() + FileUtils.getDatePath() + "/" + fileName;
        return upload(inputStream, path, originalFileName, contentType);
    }
    
    @Override
    public FileInfo upload(InputStream inputStream, String path, String originalFileName, String contentType) {
        try {
            // 设置元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            
            // 上传文件
            ossClient.putObject(config.getBucketName(), path, inputStream, metadata);
            
            // 获取文件大小
            OSSObject ossObject = ossClient.getObject(config.getBucketName(), path);
            long size = ossObject.getObjectMetadata().getContentLength();
            ossObject.close();
            
            // 构建文件信息
            return FileInfo.builder()
                    .fileName(path.substring(path.lastIndexOf("/") + 1))
                    .originalFileName(originalFileName)
                    .extension(FileUtils.getExtension(originalFileName))
                    .size(size)
                    .contentType(contentType)
                    .path(path)
                    .url(getUrl(path))
                    .storageType("oss")
                    .uploadTime(System.currentTimeMillis())
                    .build();
                    
        } catch (Exception e) {
            log.error("OSS 文件上传失败: {}", path, e);
            throw new RuntimeException("文件上传失败", e);
        }
    }
    
    @Override
    public InputStream download(String path) {
        try {
            OSSObject ossObject = ossClient.getObject(config.getBucketName(), path);
            return ossObject.getObjectContent();
        } catch (Exception e) {
            log.error("OSS 文件下载失败: {}", path, e);
            throw new RuntimeException("文件下载失败", e);
        }
    }
    
    @Override
    public boolean delete(String path) {
        try {
            ossClient.deleteObject(config.getBucketName(), path);
            return true;
        } catch (Exception e) {
            log.error("OSS 文件删除失败: {}", path, e);
            return false;
        }
    }
    
    @Override
    public boolean exists(String path) {
        try {
            return ossClient.doesObjectExist(config.getBucketName(), path);
        } catch (Exception e) {
            log.error("OSS 检查文件是否存在失败: {}", path, e);
            return false;
        }
    }
    
    @Override
    public String getUrl(String path) {
        // 如果配置了自定义域名，使用自定义域名
        if (config.getCustomDomain() != null && !config.getCustomDomain().isEmpty()) {
            return config.getCustomDomain() + "/" + path;
        }
        
        // 使用默认域名
        return "https://" + config.getBucketName() + "." + config.getEndpoint() + "/" + path;
    }
    
    @Override
    public FileInfo getFileInfo(String path) {
        try {
            if (!exists(path)) {
                return null;
            }
            
            OSSObject ossObject = ossClient.getObject(config.getBucketName(), path);
            ObjectMetadata metadata = ossObject.getObjectMetadata();
            
            FileInfo fileInfo = FileInfo.builder()
                    .fileName(path.substring(path.lastIndexOf("/") + 1))
                    .extension(FileUtils.getExtension(path))
                    .size(metadata.getContentLength())
                    .contentType(metadata.getContentType())
                    .path(path)
                    .url(getUrl(path))
                    .storageType("oss")
                    .build();
            
            ossObject.close();
            return fileInfo;
            
        } catch (Exception e) {
            log.error("获取 OSS 文件信息失败: {}", path, e);
            return null;
        }
    }

    @Override
    public FileInfo uploadWithHash(InputStream inputStream, String targetDir, String originalFileName, String contentType) {
        try {
            // write to temp and compute hash
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "miku-file-upload");
            Files.createDirectories(tempDir);
            Path tempFile = Files.createTempFile(tempDir, "up-", ".tmp");
            String hash = ContentHashUtil.computeHashAndWriteTemp(inputStream, tempFile);

            String ext = FileUtils.getExtension(originalFileName);
            String fileName = (ext != null && !ext.isEmpty()) ? (hash + "." + ext) : hash;
            String finalPath = (targetDir == null || targetDir.isEmpty()) ? fileName : (targetDir + "/" + fileName);

            if (exists(finalPath)) {
                Files.deleteIfExists(tempFile);
                return getFileInfo(finalPath);
            }

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);

            try (InputStream is = Files.newInputStream(tempFile)) {
                ossClient.putObject(config.getBucketName(), finalPath, is, metadata);
            }

            // get size
            OSSObject ossObject = ossClient.getObject(config.getBucketName(), finalPath);
            long size = ossObject.getObjectMetadata().getContentLength();
            ossObject.close();

            Files.deleteIfExists(tempFile);

            return FileInfo.builder()
                    .fileName(finalPath.substring(finalPath.lastIndexOf("/") + 1))
                    .originalFileName(originalFileName)
                    .extension(ext)
                    .size(size)
                    .contentType(contentType)
                    .path(finalPath)
                    .url(getUrl(finalPath))
                    .storageType("oss")
                    .uploadTime(System.currentTimeMillis())
                    .build();
        } catch (Exception e) {
            log.error("OSS 文件上传失败 (hash): {}", originalFileName, e);
            throw new RuntimeException("文件上传失败", e);
        }
    }
}

