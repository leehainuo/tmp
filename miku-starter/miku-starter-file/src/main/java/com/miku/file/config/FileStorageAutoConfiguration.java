package com.miku.file.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.miku.file.service.FileStorage;
import com.miku.file.service.impl.LocalFileStorage;
import com.miku.file.service.impl.MinioFileStorage;
import com.miku.file.service.impl.OssFileStorage;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储自动配置
 *
 * @author miku
 * @date 2025-11-03
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(FileProperties.class)
@RequiredArgsConstructor
public class FileStorageAutoConfiguration {
    
    private final FileProperties properties;
    
    /**
     * 本地文件存储
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "miku.file.type", havingValue = "LOCAL", matchIfMissing = true)
    public FileStorage localFileStorage() {
        log.info("启用本地文件存储，路径: {}", properties.getLocal().getPath());
        return new LocalFileStorage(properties.getLocal());
    }
    
    /**
     * 阿里云 OSS 文件存储
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "miku.file.type", havingValue = "OSS")
    public FileStorage ossFileStorage() {
        log.info("启用阿里云 OSS 文件存储，Bucket: {}", properties.getOss().getBucketName());
        
        OSS ossClient = new OSSClientBuilder().build(
                properties.getOss().getEndpoint(),
                properties.getOss().getAccessKeyId(),
                properties.getOss().getAccessKeySecret());
        
        return new OssFileStorage(ossClient, properties.getOss());
    }
    
    /**
     * MinIO 文件存储
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "miku.file.type", havingValue = "MINIO")
    public FileStorage minioFileStorage() {
        log.info("启用 MinIO 文件存储，Endpoint: {}, Bucket: {}", 
                properties.getMinio().getEndpoint(), 
                properties.getMinio().getBucketName());
        
        MinioClient minioClient = MinioClient.builder()
                .endpoint(properties.getMinio().getEndpoint())
                .credentials(properties.getMinio().getAccessKey(), properties.getMinio().getSecretKey())
                .build();
        
        return new MinioFileStorage(minioClient, properties.getMinio());
    }
}

