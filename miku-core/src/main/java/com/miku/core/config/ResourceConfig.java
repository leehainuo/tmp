package com.miku.core.config;

import com.miku.file.config.FileProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/**
 * Map file storage prefix (e.g. /files/**) to the local filesystem path configured in starter.
 */
@Configuration
@RequiredArgsConstructor
public class ResourceConfig implements WebMvcConfigurer {

    private final FileProperties fileProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // derive prefix from storage directory name (no explicit prefix config)
        String localPath = fileProperties.getLocal().getPath(); // relative or absolute
        String name = Path.of(localPath).getFileName().toString();
        String prefix = name.startsWith("/") ? name : "/" + name;
        // ensure prefix ends with /**
        String pattern = prefix.endsWith("/") ? prefix + "**" : prefix + "/**";
        // create file: URL (ensure trailing slash)
        String location = "file:" + Path.of(localPath).toAbsolutePath().toString() + "/";
        registry.addResourceHandler(pattern).addResourceLocations(location);
    }
}
