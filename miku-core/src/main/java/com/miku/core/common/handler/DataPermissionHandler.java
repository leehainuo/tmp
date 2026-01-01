package com.miku.core.common.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import com.miku.core.common.aspect.DataScopeAspect;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import org.springframework.util.StringUtils;

/**
 * 自定义数据权限处理器
 *
 * <p>参考 MyBatis-Plus 官方数据权限插件实现</p>
 * "<a href="https://baomidou.com/plugins/data-permission/">文档地址</a>"
 *
 * <p>工作原理：</p>
 * <ul>
 *   <li>从 {@link DataScopeAspect} 获取当前请求的数据权限 SQL 片段</li>
 *   <li>使用 JSQLParser 将 SQL 片段解析为 Expression</li>
 *   <li>MyBatis-Plus 的 DataPermissionInterceptor 会自动将 Expression 添加到 SQL 的 WHERE 子句中</li>
 * </ul>
 *
 * @author lihainuo.com
 */
@Slf4j
public class DataPermissionHandler implements MultiDataPermissionHandler {

    /**
     * 获取数据权限 SQL 片段
     * 
     * <p>此方法会被 MyBatis-Plus 的 DataPermissionInterceptor 调用，
     * 针对 SQL 中的每个表（Table）都会调用一次。</p>
     * 
     * <p>实现逻辑：</p>
     * <ol>
     *   <li>从 {@link DataScopeAspect} 获取数据权限 SQL 片段（格式：AND tableAlias.column = value）</li>
     *   <li>检查 SQL 片段中的表别名是否与当前处理的表匹配</li>
     *   <li>如果匹配，解析并返回 Expression；否则返回 null</li>
     * </ol>
     * 
     * <p>示例：</p>
     * <ul>
     *   <li>SQL 片段：{@code "AND u.id = 2"}</li>
     *   <li>当前表：{@code sys_user AS u}</li>
     *   <li>结果：返回 {@code u.id = 2} 的 Expression</li>
     * </ul>
     * 
     * @param table 当前处理的表对象（包含表名和别名）
     * @param where 现有的 WHERE 条件（可能为 null）
     * @param mappedStatementId MyBatis Mapper 方法的完整路径（如：com.miku.core.module.user.mapper.SysUserMapper.selectUserPageWithDeptAndRoles）
     * @return 数据权限 SQL 片段对应的 Expression，如果不需要添加权限条件则返回 null
     */
    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        // 从 DataScopeAspect 获取数据权限 SQL 片段
        String dataScopeSql = DataScopeAspect.getDataScopeSql();
        
        // 如果没有数据权限 SQL，直接返回 null（不添加任何条件）
        if (!StringUtils.hasText(dataScopeSql)) {
            log.debug("[数据权限处理器] {} {} AS {} : 无权限限制", 
                mappedStatementId, table.getName(), table.getAlias());
            return null;
        }
        
        // 获取当前表的别名（用于匹配）
        String tableAlias = table.getAlias() != null ? table.getAlias().getName() : null;
        String tableName = table.getName();
        
        // 检查数据权限 SQL 是否适用于当前表
        // DataScopeAspect 生成的 SQL 格式：AND tableAlias.column = value 或 AND column = value
        // 策略：
        // 1. 如果 SQL 片段包含表别名（如 "u.id"），只应用到匹配的表
        // 2. 如果 SQL 片段不包含表别名（如 "id = 2"），应用到主表（通常是第一个表）
        boolean sqlHasTableAlias = dataScopeSql.matches(".*\\b[a-zA-Z_][a-zA-Z0-9_]*\\.[a-zA-Z_][a-zA-Z0-9_]*.*");
        
        if (sqlHasTableAlias) {
            // SQL 片段包含表别名，需要匹配
            if (tableAlias == null) {
                // 当前表没有别名，但 SQL 片段有别名，不匹配
                log.debug("[数据权限处理器] {} {} : 权限条件包含表别名，但当前表无别名（SQL片段: {}）", 
                    mappedStatementId, tableName, dataScopeSql);
                return null;
            }
            if (!dataScopeSql.contains(tableAlias + ".")) {
                // SQL 片段中的表别名与当前表不匹配
                log.debug("[数据权限处理器] {} {} AS {} : 权限条件不适用于此表（SQL片段: {}）", 
                    mappedStatementId, tableName, tableAlias, dataScopeSql);
                return null;
            }
        } else {
            // SQL 片段不包含表别名，只应用到主表（第一个表）
            // 这里简化处理：如果当前表有别名，且不是主表，则不应用
            // 注意：这个判断可能不够精确，但对于大多数场景已经足够
            if (tableAlias != null) {
                // 有别名通常不是主表，不应用无别名的权限条件
                log.debug("[数据权限处理器] {} {} AS {} : 权限条件无表别名，跳过非主表（SQL片段: {}）", 
                    mappedStatementId, tableName, tableAlias, dataScopeSql);
                return null;
            }
        }
        
        try {
            // 数据权限 SQL 格式通常是 "AND column = value" 或 "AND column IN (...)"
            // 需要去掉开头的 "AND "，因为 MyBatis-Plus 会自动处理 AND 连接
            String conditionSql = dataScopeSql.trim();
            if (conditionSql.startsWith("AND ")) {
                conditionSql = conditionSql.substring(4);
            } else if (conditionSql.startsWith("and ")) {
                conditionSql = conditionSql.substring(4);
            }
            
            // 使用 JSQLParser 将 SQL 片段解析为 Expression
            Expression expression = CCJSqlParserUtil.parseCondExpression(conditionSql);
            
            log.debug("[数据权限处理器] {} {} AS {} : {}", 
                mappedStatementId, table.getName(), table.getAlias(), expression.toString());
            
            return expression;
            
        } catch (JSQLParserException e) {
            log.error("[数据权限处理器] 解析数据权限 SQL 失败: {}, SQL片段: {}", e.getMessage(), dataScopeSql, e);
            // 解析失败时返回 null，避免影响业务
            return null;
        }
    }
}

