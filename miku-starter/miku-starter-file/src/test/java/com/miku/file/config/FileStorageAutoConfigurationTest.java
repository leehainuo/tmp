package com.miku.file.config;

import com.miku.file.service.FileStorage;
import com.miku.file.service.impl.LocalFileStorage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 文件存储自动配置测试
 */
@SpringBootTest(
        classes = FileStorageAutoConfigurationTest.LocalStorageConfig.class,
        properties = {
                "miku.file.type=LOCAL",
                "miku.file.local.path=./build/files"
        }
)
class FileStorageAutoConfigurationTest {

    @Configuration
    @ImportAutoConfiguration(FileStorageAutoConfiguration.class)
    static class LocalStorageConfig {

        @Bean
        FileProperties fileProperties() {
            FileProperties properties = new FileProperties();
            FileProperties.LocalConfig local = new FileProperties.LocalConfig();
            local.setPath("./build/files");
            properties.setLocal(local);
            return properties;
        }
    }

    @Autowired
    private FileStorage fileStorage;

    @Test
    void shouldCreateLocalFileStorageByDefault() {
        assertThat(fileStorage).isInstanceOf(LocalFileStorage.class);
    }
}


