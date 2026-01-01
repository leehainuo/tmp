package com.miku.core.common.aspect;

import com.miku.core.common.annotation.DataScope;
import com.miku.core.security.util.SecurityUtil;
import com.miku.pkg.constants.Constants;
import com.miku.core.security.model.LoginUser;
import com.miku.core.module.dept.service.ISysDeptService;
import com.miku.core.module.role.entity.SysRole;
import com.miku.core.module.role.service.ISysRoleService;
import com.miku.core.module.user.entity.SysUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据权限AOP
 *
 * @author lihainuo.com
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DataScopeAspect {

    private final ISysDeptService deptService;
    private final ISysRoleService roleService;

    /**
     * 数据权限SQL片段ThreadLocal
     */
    private static final ThreadLocal<String> DATA_SCOPE_SQL = new ThreadLocal<>();

    @Before("@annotation(dataScope)")
    public void doBefore(JoinPoint point, DataScope dataScope) {
        handleDataScope(point, dataScope);
    }

    @After("@annotation(dataScope)")
    public void doAfter(JoinPoint point, DataScope dataScope) {
        // 请求结束后清除ThreadLocal，避免内存泄漏
        clearDataScopeSql();
    }

    /**
     * 处理数据权限
     */
    protected void handleDataScope(JoinPoint point, DataScope dataScope) {
        LoginUser loginUser = SecurityUtil.getLoginUser();
        if (loginUser == null) {
            return;
        }

        SysUser user = loginUser.getUser();

        // 超级管理员不过滤数据
        if (SecurityUtil.isSuperAdmin()) {
            log.debug("[数据权限] 超级管理员 {}，不过滤数据", user.getUsername());
            return;
        }

        // 重新查询用户角色（确保获取最新的 dataScope）
        List<SysRole> roles = roleService.getRolesByUserId(user.getId());
        if (roles == null || roles.isEmpty()) {
            log.warn("[数据权限] 用户 {} 没有角色，不进行数据过滤", user.getUsername());
            return;
        }

        // 获取最大的数据权限范围
        int maxDataScope = getMaxDataScope(roles);
        
        log.debug("[数据权限] 用户: {}, 部门ID: {}, 角色: {}, 最大数据权限范围: {}", 
            user.getUsername(), 
            user.getDeptId(), 
            roles.stream().map(SysRole::getRoleName).collect(Collectors.toList()), 
            maxDataScope);

        // 获取注解参数（新设计：更简洁直观）
        String table = dataScope.table();
        String deptColumnName = dataScope.deptColumn();
        String userColumnName = dataScope.userColumn();

        log.debug("[数据权限] 注解参数: table={}, deptColumn={}, userColumn={}", 
                table, deptColumnName, userColumnName);

        // 拼接完整的列名（表别名.字段名）
        // 如果 table 为空，表示没有表别名，直接使用字段名
        String deptColumn = table.isEmpty() ? deptColumnName : table + "." + deptColumnName;
        String userColumn = userColumnName.isEmpty() ? null : 
                (table.isEmpty() ? userColumnName : table + "." + userColumnName);
        String userIdColumn = table.isEmpty() ? "id" : table + ".id";
        
        log.debug("[数据权限] 拼接后的列名: deptColumn={}, userColumn={}, userIdColumn={}", 
                deptColumn, userColumn, userIdColumn);

        StringBuilder sqlString = new StringBuilder();

        switch (maxDataScope) {
            case Constants.DataScope.ALL: // 全部数据权限
                // 不需要过滤
                break;

            case Constants.DataScope.CUSTOM: // 自定义数据权限
                List<Long> deptIds = deptService.getDataScopeDeptIds(user.getId());
                if (!deptIds.isEmpty()) {
                    // 如果配置了 userColumn 且是用户ID字段（用户管理场景），
                    // 需要确保用户能看到自己，即使自己的部门不在权限范围内
                    if (userColumn != null && ("id".equals(userColumnName) || userColumnName.endsWith("_id"))) {
                        // 生成：AND (dept_id IN (...) OR id = {当前用户ID})
                        sqlString.append(" AND (").append(deptColumn)
                                .append(" IN (")
                                .append(String.join(",", deptIds.stream()
                                        .map(String::valueOf)
                                        .toArray(String[]::new)))
                                .append(") OR ").append(userIdColumn)
                                .append(" = ").append(user.getId())
                                .append(")");
                    } else {
                        // 其他场景（如部门管理、业务数据等），只按部门过滤
                        sqlString.append(" AND ").append(deptColumn)
                                .append(" IN (")
                                .append(String.join(",", deptIds.stream()
                                        .map(String::valueOf)
                                        .toArray(String[]::new)))
                                .append(")");
                    }
                } else {
                    // 如果没有可访问的部门，但配置了 userColumn，至少让用户能看到自己
                    if (userColumn != null && ("id".equals(userColumnName) || userColumnName.endsWith("_id"))) {
                        sqlString.append(" AND ").append(userIdColumn)
                                .append(" = ").append(user.getId());
                    } else {
                        // 没有可访问的部门，且不是用户管理场景，不显示任何数据
                        sqlString.append(" AND 1=0");
                    }
                }
                break;

            case Constants.DataScope.DEPT: // 本部门数据权限
                if (user.getDeptId() != null) {
                    sqlString.append(" AND ").append(deptColumn)
                            .append(" = ").append(user.getDeptId());
                }
                break;

            case Constants.DataScope.DEPT_AND_CHILD: // 本部门及以下数据权限
                if (user.getDeptId() != null) {
                    List<Long> childDeptIds = deptService.getDeptAndChildrenIds(user.getDeptId());
                    if (!childDeptIds.isEmpty()) {
                        sqlString.append(" AND ").append(deptColumn)
                                .append(" IN (")
                                .append(String.join(",", childDeptIds.stream()
                                        .map(String::valueOf)
                                        .toArray(String[]::new)))
                                .append(")");
                    }
                }
                break;

            case Constants.DataScope.SELF: // 仅本人数据权限
                // 智能判断：如果配置了 userColumn，按用户字段过滤；否则按部门过滤
                if (userColumn != null) {
                    // 按用户字段过滤：适用于用户管理、订单管理等需要按创建人过滤的场景
                    // 判断 userColumn 是 id 还是其他字段（如 create_by）
                    if ("id".equals(userColumnName) || userColumnName.endsWith("_id")) {
                        // 按用户ID过滤
                        sqlString.append(" AND ").append(userColumn)
                                .append(" = ").append(user.getId());
                    } else {
                        // 按用户名字段过滤（如 create_by）
                        sqlString.append(" AND ").append(userColumn)
                                .append(" = '").append(user.getUsername()).append("'");
                    }
                } else {
                    // 按部门ID过滤：适用于部门管理、部门数据等需要按用户所属部门过滤的场景
                    // 显示用户所属部门及其所有子部门，保持层级结构完整
                    if (user.getDeptId() != null) {
                        List<Long> userDeptIds = deptService.getDeptAndChildrenIds(user.getDeptId());
                        if (!userDeptIds.isEmpty()) {
                            sqlString.append(" AND ").append(deptColumn)
                                    .append(" IN (")
                                    .append(String.join(",", userDeptIds.stream()
                                            .map(String::valueOf)
                                            .toArray(String[]::new)))
                                    .append(")");
                        } else {
                            // 没有可访问的部门
                            sqlString.append(" AND 1=0");
                        }
                    } else {
                        // 用户没有部门，不显示任何数据
                        sqlString.append(" AND 1=0");
                    }
                }
                break;

            default:
                break;
        }

        DATA_SCOPE_SQL.set(sqlString.toString());
        log.debug("[数据权限] 生成的SQL: {}", sqlString);
    }

    /**
     * 获取最大的数据权限范围（数字越小权限越大）
     */
    private int getMaxDataScope(List<SysRole> roles) {
        int maxDataScope = Constants.DataScope.SELF; // 默认仅本人
        for (SysRole role : roles) {
            if (role.getDataScope() != null && role.getDataScope() < maxDataScope) {
                maxDataScope = role.getDataScope();
            }
        }
        return maxDataScope;
    }

    /**
     * 获取数据权限SQL（不清除，支持多次调用）
     */
    public static String getDataScopeSql() {
        return DATA_SCOPE_SQL.get();
    }

    /**
     * 清除数据权限SQL（在请求结束时调用）
     */
    public static void clearDataScopeSql() {
        DATA_SCOPE_SQL.remove();
    }
}

