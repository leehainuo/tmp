package com.miku.file.service.impl;

import com.miku.file.config.FileProperties;
import com.miku.file.entity.FileInfo;
import com.miku.file.service.FileStorage;
import com.miku.file.util.FileUtils;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import com.miku.file.util.ContentHashUtil;

/**
 * MinIO 文件存储实现
 *
 * @author miku
 * @date 2025-11-03
 */
@Slf4j
@RequiredArgsConstructor
public class MinioFileStorage implements FileStorage {
    
    private final MinioClient minioClient;
    private final FileProperties.MinioConfig config;
    
    @Override
    public FileInfo upload(InputStream inputStream, String originalFileName, String contentType) {
        String fileName = FileUtils.generateFileName(originalFileName);
        String path = config.getPrefix() + FileUtils.getDatePath() + "/" + fileName;
        return upload(inputStream, path, originalFileName, contentType);
    }
    
    @Override
    public FileInfo upload(InputStream inputStream, String path, String originalFileName, String contentType) {
        try {
            // 确保 Bucket 存在
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(config.getBucketName()).build());
            
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(config.getBucketName()).build());
            }
            
            // 上传文件
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(config.getBucketName())
                    .object(path)
                    .stream(inputStream, -1, 10485760) // 10MB part size
                    .contentType(contentType)
                    .build();
            
            minioClient.putObject(putObjectArgs);
            
            // 获取文件信息
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(config.getBucketName())
                            .object(path)
                            .build());
            
            // 构建文件信息
            return FileInfo.builder()
                    .fileName(path.substring(path.lastIndexOf("/") + 1))
                    .originalFileName(originalFileName)
                    .extension(FileUtils.getExtension(originalFileName))
                    .size(stat.size())
                    .contentType(contentType)
                    .path(path)
                    .url(getUrl(path))
                    .storageType("minio")
                    .uploadTime(System.currentTimeMillis())
                    .build();
                    
        } catch (Exception e) {
            log.error("MinIO 文件上传失败: {}", path, e);
            throw new RuntimeException("文件上传失败", e);
        }
    }
    
    @Override
    public InputStream download(String path) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(config.getBucketName())
                            .object(path)
                            .build());
        } catch (Exception e) {
            log.error("MinIO 文件下载失败: {}", path, e);
            throw new RuntimeException("文件下载失败", e);
        }
    }
    
    @Override
    public boolean delete(String path) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(config.getBucketName())
                            .object(path)
                            .build());
            return true;
        } catch (Exception e) {
            log.error("MinIO 文件删除失败: {}", path, e);
            return false;
        }
    }
    
    @Override
    public boolean exists(String path) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(config.getBucketName())
                            .object(path)
                            .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public String getUrl(String path) {
        try {
            // 生成预签名 URL（7天有效）
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(config.getBucketName())
                            .object(path)
                            .expiry(7, TimeUnit.DAYS)
                            .build());
        } catch (Exception e) {
            log.error("MinIO 获取文件 URL 失败: {}", path, e);
            return path;
        }
    }
    
    @Override
    public FileInfo getFileInfo(String path) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(config.getBucketName())
                            .object(path)
                            .build());
            
            return FileInfo.builder()
                    .fileName(path.substring(path.lastIndexOf("/") + 1))
                    .extension(FileUtils.getExtension(path))
                    .size(stat.size())
                    .contentType(stat.contentType())
                    .path(path)
                    .url(getUrl(path))
                    .storageType("minio")
                    .build();
                    
        } catch (Exception e) {
            log.error("获取 MinIO 文件信息失败: {}", path, e);
            return null;
        }
    }

    @Override
    public FileInfo uploadWithHash(InputStream inputStream, String targetDir, String originalFileName, String contentType) {
        try {
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

            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(config.getBucketName()).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(config.getBucketName()).build());
            }

            long size = Files.size(tempFile);
            try (InputStream is = Files.newInputStream(tempFile)) {
                PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                        .bucket(config.getBucketName())
                        .object(finalPath)
                        .stream(is, size, 10485760)
                        .contentType(contentType)
                        .build();
                minioClient.putObject(putObjectArgs);
            }

            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder().bucket(config.getBucketName()).object(finalPath).build());

            Files.deleteIfExists(tempFile);

            return FileInfo.builder()
                    .fileName(finalPath.substring(finalPath.lastIndexOf("/") + 1))
                    .originalFileName(originalFileName)
                    .extension(ext)
                    .size(stat.size())
                    .contentType(contentType)
                    .path(finalPath)
                    .url(getUrl(finalPath))
                    .storageType("minio")
                    .uploadTime(System.currentTimeMillis())
                    .build();
        } catch (Exception e) {
            log.error("MinIO 文件上传失败 (hash): {}", originalFileName, e);
            throw new RuntimeException("文件上传失败", e);
        }
    }
}

