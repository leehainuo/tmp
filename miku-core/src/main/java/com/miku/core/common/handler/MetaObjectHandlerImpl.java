package com.miku.core.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.miku.core.security.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 元数据处理器
 * 自动填充创建时间、创建人、更新时间、更新人
 */
@Slf4j
@Component
public class MetaObjectHandlerImpl implements MetaObjectHandler {
    
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("MyBatis Plus: 开始插入填充...");
        
        // 创建时间
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        
        // 创建人
        String username = SecurityUtil.getUsername();
        if (username != null) {
            this.strictInsertFill(metaObject, "createBy", String.class, username);
        }
        
        // 更新时间
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        
        // 更新人
        if (username != null) {
            this.strictInsertFill(metaObject, "updateBy", String.class, username);
        }
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("MyBatis Plus: 开始更新填充...");
        
        // 更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        
        // 更新人
        String username = SecurityUtil.getUsername();
        if (username != null) {
            this.strictUpdateFill(metaObject, "updateBy", String.class, username);
        }
    }
}

