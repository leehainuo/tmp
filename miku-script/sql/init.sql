-- ====================================================
-- 数据库初始化脚本
-- ====================================================
DROP DATABASE IF EXISTS `miku`;
CREATE DATABASE IF NOT EXISTS `miku` DEFAULT CHARACTER SET utf8mb4;

USE `miku`;
-- ----------------------------
-- 1. 部门表
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`
(
    `id`          BIGINT(20)  NOT NULL AUTO_INCREMENT COMMENT '部门ID',
    `parent_id`   BIGINT(20)   DEFAULT 0 COMMENT '父部门ID',
    `ancestors`   VARCHAR(500) DEFAULT '' COMMENT '祖级列表',
    `dept_name`   VARCHAR(50) NOT NULL COMMENT '部门名称',
    `order_num`   INT(4)       DEFAULT 0 COMMENT '显示顺序',
    `leader`      VARCHAR(50)  DEFAULT NULL COMMENT '负责人',
    `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    `email`       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `status`      TINYINT(1)   DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `del_flag`    TINYINT(1)   DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    `create_by`   VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_parent_status_del` (`parent_id`, `status`, `del_flag`),
    INDEX `idx_ancestors` (`ancestors`(100)),
    INDEX `idx_order_num` (`order_num`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='部门表';

-- ----------------------------
-- 2. 用户表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`
(
    `id`          BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户账号',
    `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '用户昵称',
    `password`    VARCHAR(128) NOT NULL COMMENT '密码',
    `dept_id`     BIGINT(20)   DEFAULT NULL COMMENT '部门ID',
    `email`       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `sex`         TINYINT(1)   DEFAULT 0 COMMENT '性别（0未知 1男 2女）',
    `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像路径',
    `status`      TINYINT(1)   DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `del_flag`    TINYINT(1)   DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    `login_ip`    VARCHAR(128) DEFAULT NULL COMMENT '最后登录IP',
    `login_time`  DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    `create_by`   VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    INDEX `idx_dept_id` (`dept_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_status_del_flag` (`status`, `del_flag`),
    INDEX `idx_dept_status_del` (`dept_id`, `status`, `del_flag`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- ----------------------------
-- 3. 角色表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`
(
    `id`          BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name`   VARCHAR(50)  NOT NULL COMMENT '角色名称',
    `role_code`   VARCHAR(100) NOT NULL COMMENT '角色权限字符串',
    `role_sort`   INT(4)       DEFAULT 0 COMMENT '显示顺序',
    `data_scope`  TINYINT(1)   DEFAULT 1 COMMENT '数据权限（1全部 2自定义 3本部门 4本部门及以下 5仅本人）',
    `status`      TINYINT(1)   DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `del_flag`    TINYINT(1)   DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    `create_by`   VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`      VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`),
    INDEX `idx_status` (`status`),
    INDEX `idx_status_del_flag` (`status`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='角色表';

-- ----------------------------
-- 4. 权限表（菜单+按钮+接口）
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`
(
    `id`              BIGINT(20)  NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `parent_id`       BIGINT(20)   DEFAULT 0 COMMENT '父权限ID',
    `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `permission_type` TINYINT(1)  NOT NULL COMMENT '权限类型（1目录 2菜单 3按钮 4接口）',
    `perms`           VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
    `path`            VARCHAR(200) DEFAULT NULL COMMENT '路由地址',
    `component`       VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
    `icon`            VARCHAR(100) DEFAULT NULL COMMENT '图标',
    `order_num`       INT(4)       DEFAULT 0 COMMENT '显示顺序',
    `visible`         TINYINT(1)   DEFAULT 1 COMMENT '是否可见（1是 0否）',
    `status`          TINYINT(1)   DEFAULT 1 COMMENT '状态（1正常 0停用）',
    `del_flag`        TINYINT(1)   DEFAULT 0 COMMENT '删除标志（0存在 1删除）',
    `api_method`      VARCHAR(10)  DEFAULT NULL COMMENT 'API方法（GET/POST/PUT/DELETE）',
    `api_path`        VARCHAR(200) DEFAULT NULL COMMENT 'API路径',
    `create_by`       VARCHAR(64)  DEFAULT '' COMMENT '创建者',
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`       VARCHAR(64)  DEFAULT '' COMMENT '更新者',
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `remark`          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (`id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_perms` (`perms`),
    INDEX `idx_status` (`status`),
    INDEX `idx_status_del_flag` (`status`, `del_flag`),
    INDEX `idx_type_status` (`permission_type`, `status`),
    INDEX `idx_parent_type_status` (`parent_id`, `permission_type`, `status`),
    INDEX `idx_parent_status_del` (`parent_id`, `status`, `del_flag`),
    INDEX `idx_order_num` (`order_num`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='权限表';

-- ----------------------------
-- 5. 用户-角色关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`
(
    `id`          BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`     BIGINT(20) NOT NULL COMMENT '用户ID',
    `role_id`     BIGINT(20) NOT NULL COMMENT '角色ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_role_id` (`role_id`),
    INDEX `idx_user_create_time` (`user_id`, `create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户-角色关联表';

-- ----------------------------
-- 6. 角色-权限关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`
(
    `id`            BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `role_id`       BIGINT(20) NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT(20) NOT NULL COMMENT '权限ID',
    `create_time`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    INDEX `idx_role_id` (`role_id`),
    INDEX `idx_permission_id` (`permission_id`),
    INDEX `idx_role_create_time` (`role_id`, `create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='角色-权限关联表';

-- ----------------------------
-- 7. 角色-部门关联表（自定义数据权限）
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept`
(
    `id`          BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `role_id`     BIGINT(20) NOT NULL COMMENT '角色ID',
    `dept_id`     BIGINT(20) NOT NULL COMMENT '部门ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_dept` (`role_id`, `dept_id`),
    INDEX `idx_role_id` (`role_id`),
    INDEX `idx_dept_id` (`dept_id`),
    INDEX `idx_role_create_time` (`role_id`, `create_time`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='角色-部门关联表';

-- ----------------------------
-- 8. 操作日志表
-- ----------------------------
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log`
(
    `id`             BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `title`          VARCHAR(50)   DEFAULT '' COMMENT '模块标题',
    `business_type`  TINYINT(2)    DEFAULT 0 COMMENT '业务类型（0其它 1新增 2修改 3删除）',
    `method`         VARCHAR(100)  DEFAULT '' COMMENT '方法名称',
    `request_method` VARCHAR(10)   DEFAULT '' COMMENT '请求方式',
    `operator_type`  TINYINT(1)    DEFAULT 0 COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
    `oper_name`      VARCHAR(50)   DEFAULT '' COMMENT '操作人员',
    `dept_name`      VARCHAR(50)   DEFAULT '' COMMENT '部门名称',
    `oper_url`       VARCHAR(255)  DEFAULT '' COMMENT '请求URL',
    `oper_ip`        VARCHAR(128)  DEFAULT '' COMMENT '主机地址',
    `oper_location`  VARCHAR(255)  DEFAULT '' COMMENT '操作地点',
    `oper_param`     TEXT          COMMENT '请求参数',
    `json_result`    TEXT          COMMENT '返回参数',
    `status`         TINYINT(1)    DEFAULT 1 COMMENT '操作状态（1正常 0异常）',
    `error_msg`      TEXT          COMMENT '错误消息',
    `oper_time`      DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    INDEX `idx_oper_name` (`oper_name`),
    INDEX `idx_oper_time` (`oper_time`),
    INDEX `idx_oper_time_status` (`oper_time`, `status`),
    INDEX `idx_oper_name_time` (`oper_name`, `oper_time`),
    INDEX `idx_business_type` (`business_type`),
    INDEX `idx_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='操作日志表';

-- ====================================================
-- 初始化数据
-- ====================================================

-- 初始化部门数据
INSERT INTO `sys_dept`
VALUES (1, 0, '0', '总公司', 0, '管理员', '51888888880', 'miku@example.com', 1, 0, 'miku', NOW(), '', NOW(), ''),
       (2, 1, '0,1', '研发部', 1, '研发负责人', '51888888881', 'miku@example.com', 1, 0, 'miku', NOW(), '', NOW(), ''),
       (3, 1, '0,1', '市场部', 2, '市场负责人', '51888888882', 'miku@example.com', 1, 0, 'miku', NOW(), '', NOW(), ''),
       (4, 2, '0,1,2', '后端组', 1, '后端组长', '51888888883', 'miku@example.com', 1, 0, 'miku', NOW(), '', NOW(), ''),
       (5, 2, '0,1,2', '前端组', 2, '前端组长', '51888888884', 'miku@example.com', 1, 0, 'miku', NOW(), '', NOW(), '');

-- 初始化用户数据（密码都是：username的值，BCrypt加密）
INSERT INTO `sys_user`
VALUES (1, 'miku', '超级管理员', '$2a$10$3KDhfSRW35L5Nq0GcpAGduLAgaxF3GpL5rm2p2XwBiGIAQ1z95LCK', 1, 'miku@example.com',
        '51888888880', 1, 'https://avatars.githubusercontent.com/u/182531394?s=400&u=e0978646fe94d8eefd06e6d55d8ebb23772c17b5&v=4', 1, 0, '127.0.0.1', NOW(), 'miku', NOW(), '', NOW(), '超级管理员'),
       (2, 'dev', '开发人员', '$2a$10$qMwVcfTEZMbky.vrgpmK.e/f9v6LYDLIj.HjWQq6Owb5YVXBdLBxy', 2, 'miku@example.com',
        '51888888881', 1, '', 1, 0, '127.0.0.1', NOW(), 'miku', NOW(), '', NOW(), '开发人员'),
       (3, 'test', '测试人员', '$2a$10$xldE28axYTmK4ycAZUtipeXJRnrB9T2o/xZ6R3mSqQfi1S9VerSYu', 2, 'miku@example.com',
        '51888888882', 2, '', 1, 0, '127.0.0.1', NOW(), 'miku', NOW(), '', NOW(), '测试人员');

-- 初始化角色数据
INSERT INTO `sys_role`
VALUES (1, '超级管理员', 'ROLE_ADMIN', 1, 1, 1, 0, 'miku', NOW(), '', NOW(), '超级管理员'),
       (2, '普通角色', 'ROLE_USER', 2, 5, 1, 0, 'miku', NOW(), '', NOW(), '普通角色'),
       (3, '部门管理员', 'ROLE_DEPT_ADMIN', 3, 4, 1, 0, 'miku', NOW(), '', NOW(), '部门管理员');

-- 初始化权限数据
INSERT INTO `sys_permission`
VALUES
-- 系统管理
(1, 0, '系统管理', 1, NULL, '/system', NULL, 'settings', 1, 1, 1, 0, NULL, NULL, 'admin', NOW(), '', NOW(), '系统管理目录'),
(2, 1, '用户管理', 2, 'system:user:list', '/system/user', 'system/user/index', 'userCog', 1, 1, 1, 0, NULL, NULL, 'admin',
 NOW(), '', NOW(), '用户管理菜单'),
(3, 2, '用户查询', 3, 'system:user:query', NULL, NULL, '#', 1, 1, 1, 0, 'GET', '/system/user/*', 'admin', NOW(), '', NOW(),
 '用户查询按钮'),
(4, 2, '用户新增', 3, 'system:user:add', NULL, NULL, '#', 2, 1, 1, 0, 'POST', '/system/user', 'admin', NOW(), '', NOW(),
 '用户新增按钮'),
(5, 2, '用户修改', 3, 'system:user:edit', NULL, NULL, '#', 3, 1, 1, 0, 'PUT', '/system/user', 'admin', NOW(), '', NOW(),
 '用户修改按钮'),
(6, 2, '用户删除', 3, 'system:user:remove', NULL, NULL, '#', 4, 1, 1, 0, 'DELETE', '/system/user/*', 'admin', NOW(), '',
 NOW(), '用户删除按钮'),
(7, 1, '角色管理', 2, 'system:role:list', '/system/role', 'system/role/index', 'userRoundCog', 2, 1, 1, 0, NULL, NULL, 'admin',
 NOW(), '', NOW(), '角色管理菜单'),
(8, 7, '角色查询', 3, 'system:role:query', NULL, NULL, '#', 1, 1, 1, 0, 'GET', '/system/role/*', 'admin', NOW(), '', NOW(),
 '角色查询按钮'),
(9, 7, '角色新增', 3, 'system:role:add', NULL, NULL, '#', 2, 1, 1, 0, 'POST', '/system/role', 'admin', NOW(), '', NOW(),
 '角色新增按钮'),
(10, 7, '角色修改', 3, 'system:role:edit', NULL, NULL, '#', 3, 1, 1, 0, 'PUT', '/system/role', 'admin', NOW(), '', NOW(),
 '角色修改按钮'),
(11, 7, '角色删除', 3, 'system:role:remove', NULL, NULL, '#', 4, 1, 1, 0, 'DELETE', '/system/role/*', 'admin', NOW(), '',
 NOW(), '角色删除按钮'),
(12, 1, '部门管理', 2, 'system:dept:list', '/system/dept', 'system/dept/index', 'columnsCog', 3, 1, 1, 0, NULL, NULL, 'admin',
 NOW(), '', NOW(), '部门管理菜单'),
(13, 12, '部门查询', 3, 'system:dept:query', NULL, NULL, '#', 1, 1, 1, 0, 'GET', '/system/dept/*', 'admin', NOW(), '',
 NOW(), '部门查询按钮'),
(14, 12, '部门新增', 3, 'system:dept:add', NULL, NULL, '#', 2, 1, 1, 0, 'POST', '/system/dept', 'admin', NOW(), '', NOW(),
 '部门新增按钮'),
(15, 12, '部门修改', 3, 'system:dept:edit', NULL, NULL, '#', 3, 1, 1, 0, 'PUT', '/system/dept', 'admin', NOW(), '', NOW(),
 '部门修改按钮'),
(16, 12, '部门删除', 3, 'system:dept:remove', NULL, NULL, '#', 4, 1, 1, 0, 'DELETE', '/system/dept/*', 'admin', NOW(), '',
 NOW(), '部门删除按钮'),
(17, 1, '权限管理', 2, 'system:permission:list', '/system/permission', 'system/permission/index', 'folderCog', 4, 1, 1, 0,
 NULL, NULL, 'admin', NOW(), '', NOW(), '权限管理菜单'),
(18, 17, '权限查询', 3, 'system:permission:query', NULL, NULL, '#', 1, 1, 1, 0, 'GET', '/system/permission/*', 'admin',
 NOW(), '', NOW(), '权限查询按钮'),
(19, 17, '权限新增', 3, 'system:permission:add', NULL, NULL, '#', 2, 1, 1, 0, 'POST', '/system/permission', 'admin', NOW(),
 '', NOW(), '权限新增按钮'),
(20, 17, '权限修改', 3, 'system:permission:edit', NULL, NULL, '#', 3, 1, 1, 0, 'PUT', '/system/permission', 'admin', NOW(),
 '', NOW(), '权限修改按钮'),
(21, 17, '权限删除', 3, 'system:permission:remove', NULL, NULL, '#', 4, 1, 1, 0, 'DELETE', '/system/permission/*', 'admin',
 NOW(), '', NOW(), '权限删除按钮'),
-- 系统日志
(22, 1, '系统日志', 2, 'system:log:list', '/system/log', 'system/log/index', 'fileCog', 5, 1, 1, 0, NULL, NULL, 'admin',
 NOW(), '', NOW(), '系统日志菜单'),
(23, 22, '日志查询', 3, 'system:log:query', NULL, NULL, '#', 1, 1, 1, 0, 'GET', '/system/log/*', 'admin', NOW(), '', NOW(),
 '日志查询按钮'),
(24, 22, '日志删除', 3, 'system:log:remove', NULL, NULL, '#', 2, 1, 1, 0, 'DELETE', '/system/log/*', 'admin', NOW(), '', NOW(),
 '日志删除按钮');

-- 初始化用户-角色关联
INSERT INTO `sys_user_role`
VALUES (1, 1, 1, NOW()),
       (2, 2, 2, NOW()),
       (3, 3, 2, NOW());

-- 初始化角色-权限关联（超级管理员拥有所有权限）
INSERT INTO `sys_role_permission`
VALUES (1, 1, 1, NOW()),
       (2, 1, 2, NOW()),
       (3, 1, 3, NOW()),
       (4, 1, 4, NOW()),
       (5, 1, 5, NOW()),
       (6, 1, 6, NOW()),
       (7, 1, 7, NOW()),
       (8, 1, 8, NOW()),
       (9, 1, 9, NOW()),
       (10, 1, 10, NOW()),
       (11, 1, 11, NOW()),
       (12, 1, 12, NOW()),
       (13, 1, 13, NOW()),
       (14, 1, 14, NOW()),
       (15, 1, 15, NOW()),
       (16, 1, 16, NOW()),
       (17, 1, 17, NOW()),
       (18, 1, 18, NOW()),
       (19, 1, 19, NOW()),
       (20, 1, 20, NOW()),
       (21, 1, 21, NOW()),
       (22, 1, 22, NOW()),
       (23, 1, 23, NOW()),
       (24, 1, 24, NOW()),
-- 普通角色只有查询权限
       (25, 2, 1, NOW()),
       (26, 2, 2, NOW()),
       (27, 2, 3, NOW()),
       (28, 2, 7, NOW()),
       (29, 2, 8, NOW());

