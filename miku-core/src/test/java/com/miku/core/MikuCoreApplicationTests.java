package com.miku.core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = MikuCoreApplicationTests.TestApplication.class)
class MikuCoreApplicationTests {

    /**
     * 测试专用的最小启动类，提供 @SpringBootConfiguration，
     * 避免在 miku-core 模块中去依赖其他业务模块的启动类。
     */
    @SpringBootApplication
    static class TestApplication {
        // no-op
    }

    @Test
    void contextLoads() {
    }

}
