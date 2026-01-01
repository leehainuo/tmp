package com.miku.core.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.miku.core.common.handler.DataPermissionHandler;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * MyBatis-Plus配置
 *
 * @author lihainuo.com
 */
@Configuration
@MapperScan("com.miku.**.mapper")
public class MyBatisPlusConfig {

    /**
     * MyBatis-Plus拦截器
     * 
     * <p>拦截器执行顺序（从外到内）：</p>
     * <ol>
     *   <li>数据权限插件（DataPermissionInterceptor）</li>
     *   <li>分页插件（PaginationInnerInterceptor）</li>
     *   <li>乐观锁插件（OptimisticLockerInnerInterceptor）</li>
     * </ol>
     * 
     * <p>注意：数据权限插件需要在分页插件之前执行，确保分页查询时能正确应用权限过滤</p>
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 数据权限插件 - 必须在分页插件之前
        // 参考：https://baomidou.com/plugins/data-permission/
        DataPermissionInterceptor dataPermissionInterceptor = new DataPermissionInterceptor(
            new DataPermissionHandler()
        );
        interceptor.addInnerInterceptor(dataPermissionInterceptor);
        
        // 分页插件 - 优化分页查询性能
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 设置最大单页限制数量，默认 500 条，-1 不受限制
        paginationInterceptor.setMaxLimit(1000L);
        // 开启 count 的 join 优化,只针对部分 left join
        paginationInterceptor.setOptimizeJoin(true);
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        
        return interceptor;
    }

}

