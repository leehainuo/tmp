package com.miku.pkg.constants;

/**
 * 系统常量统一管理
 * <p>
 * 使用内部接口的方式组织常量，便于管理和使用
 * </p>
 *
 * @author lihainuo.com
 */
public interface Constants {

    /**
     * 缓存相关常量
     */
    interface Cache {
        /**
         * 用户缓存前缀
         */
        String USER_PREFIX = "permission:user:";

        /**
         * 角色缓存前缀
         */
        String ROLE_PREFIX = "permission:role:";

        /**
         * 权限缓存前缀
         */
        String PERMISSION_PREFIX = "permission:perms:";

        /**
         * 缓存过期时间（分钟）
         */
        long EXPIRE_TIME = 30;
    }

    /**
     * 状态相关常量
     */
    interface Status {
        /**
         * 启用/正常
         */
        int ENABLE = 1;

        /**
         * 停用/禁用
         */
        int DISABLE = 0;

        /**
         * 可见
         */
        int VISIBLE = 1;

        /**
         * 隐藏
         */
        int HIDDEN = 0;
    }

    /**
     * 权限相关常量
     */
    interface Permission {
        /**
         * 全部权限标识（拥有此权限表示拥有所有权限）
         */
        String ALL_PERMISSION = "*:*:*";

        /**
         * 超级管理员角色标识
         */
        String SUPER_ADMIN_ROLE = "ROLE_ADMIN";

        /**
         * 权限类型：目录
         */
        int TYPE_DIRECTORY = 1;

        /**
         * 权限类型：菜单
         */
        int TYPE_MENU = 2;

        /**
         * 权限类型：按钮
         */
        int TYPE_BUTTON = 3;

        /**
         * 权限类型：接口
         */
        int TYPE_API = 4;
    }

    /**
     * 数据权限相关常量
     * <p>
     * 数据权限范围说明：
     * <ul>
     *   <li>全部数据权限（1）：可查看所有部门的数据</li>
     *   <li>自定义数据权限（2）：只能查看角色配置的指定部门数据</li>
     *   <li>本部门数据权限（3）：只能查看用户所属部门的数据</li>
     *   <li>本部门及以下数据权限（4）：可查看用户所属部门及其所有子部门的数据</li>
     *   <li>仅本人数据权限（5）：只能查看自己创建的数据</li>
     * </ul>
     */
    interface DataScope {
        /**
         * 全部数据权限：可查看所有部门的数据
         */
        int ALL = 1;

        /**
         * 自定义数据权限：只能查看角色配置的指定部门数据
         */
        int CUSTOM = 2;

        /**
         * 本部门数据权限：只能查看用户所属部门的数据
         */
        int DEPT = 3;

        /**
         * 本部门及以下数据权限：可查看用户所属部门及其所有子部门的数据
         */
        int DEPT_AND_CHILD = 4;

        /**
         * 仅本人数据权限：只能查看自己创建的数据
         */
        int SELF = 5;
    }
}

