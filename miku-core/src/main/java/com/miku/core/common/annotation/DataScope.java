package com.miku.core.common.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * 
 * <p>用于标记需要进行数据权限过滤的方法，自动根据用户角色的数据权限范围生成SQL过滤条件。</p>
 * 
 * <h3>使用场景</h3>
 * <ul>
 *   <li><b>用户管理</b>：按用户所属部门过滤用户列表</li>
 *   <li><b>部门管理</b>：按用户权限范围过滤部门列表</li>
 *   <li><b>业务数据</b>：按部门或创建人过滤业务数据（订单、合同等）</li>
 * </ul>
 * 
 * <h3>快速开始</h3>
 * <pre>{@code
 * // 场景1：用户管理（最常用）
 * @DataScope(table = "u", deptColumn = "dept_id")
 * public Result<PageResult<SysUser>> list(UserQueryRequest request) {
 *     // 自动过滤：u.dept_id IN (...)
 * }
 * 
 * // 场景2：部门管理
 * @DataScope(table = "d", deptColumn = "id")
 * public Result<List<SysDept>> listTree() {
 *     // 自动过滤：d.id IN (...)
 * }
 * 
 * // 场景3：业务数据（订单、合同等）
 * @DataScope(table = "o", deptColumn = "dept_id", userColumn = "create_by")
 * public Result<PageResult<Order>> list(OrderQueryRequest request) {
 *     // 自动过滤：o.dept_id IN (...) 或 o.create_by = 'xxx'
 * }
 * }</pre>
 * 
 * <h3>数据权限范围说明</h3>
 * <ul>
 *   <li><b>全部数据权限（1）</b>：不添加过滤条件，可查看所有数据</li>
 *   <li><b>自定义数据权限（2）</b>：按角色配置的部门列表过滤</li>
 *   <li><b>本部门数据权限（3）</b>：只能查看用户所属部门的数据</li>
 *   <li><b>本部门及以下数据权限（4）</b>：可查看用户所属部门及其所有子部门的数据</li>
 *   <li><b>仅本人数据权限（5）</b>：
 *     <ul>
 *       <li>如果配置了 {@code userColumn}，按创建人过滤（适用于用户管理、订单管理等）</li>
 *       <li>如果未配置 {@code userColumn}，按用户所属部门过滤（适用于部门管理等）</li>
 *     </ul>
 *   </li>
 * </ul>
 * 
 * <h3>参数说明</h3>
 * <ul>
 *   <li><b>table</b>：主表别名（SQL中的表别名），必填</li>
 *   <li><b>deptColumn</b>：部门ID字段名，默认 "dept_id"</li>
 *   <li><b>userColumn</b>：用户字段名（用于"仅本人数据权限"），默认不设置（按部门过滤）</li>
 * </ul>
 * 
 * @author lihainuo.com
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {
    
    /**
     * 主表别名（SQL中的表别名）
     * 
     * <p>用于指定数据权限过滤条件应该添加到哪个表的字段上。</p>
     * 
     * <p><b>使用规则：</b></p>
     * <ul>
     *   <li>如果SQL中有表别名，使用表别名（如：{@code "u"}、{@code "d"}）</li>
     *   <li>如果SQL中没有表别名（MyBatis-Plus自动生成），使用空字符串 {@code ""}</li>
     * </ul>
     * 
     * <p><b>示例：</b></p>
     * <ul>
     *   <li>用户管理（XML Mapper）：{@code table = "u"}（对应 {@code FROM sys_user u}）</li>
     *   <li>部门管理（XML Mapper）：{@code table = "d"}（对应 {@code FROM sys_dept d}）</li>
     *   <li>部门管理（MyBatis-Plus）：{@code table = ""}（对应 {@code FROM sys_dept}，无别名）</li>
     *   <li>订单管理：{@code table = "o"}（对应 {@code FROM sys_order o}）</li>
     * </ul>
     * 
     * @return 表别名，空字符串表示没有表别名
     */
    String table() default "";
    
    /**
     * 部门ID字段名
     * 
     * <p>用于数据权限过滤的部门ID字段名。生成的SQL格式：{@code {table}.{deptColumn} IN (...)}</p>
     * 
     * <p><b>默认值：</b>{@code "dept_id"}</p>
     * 
     * <p><b>常见值：</b></p>
     * <ul>
     *   <li>用户表：{@code "dept_id"}</li>
     *   <li>部门表：{@code "id"}</li>
     *   <li>订单表：{@code "dept_id"}</li>
     * </ul>
     * 
     * @return 部门ID字段名
     */
    String deptColumn() default "dept_id";
    
    /**
     * 用户字段名（用于"仅本人数据权限"）
     * 
     * <p>当用户的数据权限范围是"仅本人数据权限"时，如果配置了此参数，将按此字段过滤。</p>
     * 
     * <p><b>使用场景：</b></p>
     * <ul>
     *   <li>用户管理：{@code userColumn = "id"}（按用户ID过滤）</li>
     *   <li>订单管理：{@code userColumn = "create_by"}（按创建人过滤）</li>
     * </ul>
     * 
     * <p><b>默认行为：</b>如果不配置此参数，对于"仅本人数据权限"，将按用户所属部门过滤（适用于部门管理等场景）</p>
     * 
     * @return 用户字段名，默认空字符串（表示不按用户过滤，按部门过滤）
     */
    String userColumn() default "";
}
