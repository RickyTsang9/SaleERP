-- ----------------------------
-- 1、部门表
-- ----------------------------
drop table if exists sys_dept;
create table sys_dept (
  dept_id           bigint(20)      not null auto_increment    comment '部门id',
  parent_id         bigint(20)      default 0                  comment '父部门id',
  ancestors         varchar(50)     default ''                 comment '祖级列表',
  dept_name         varchar(30)     default ''                 comment '部门名称',
  order_num         int(4)          default 0                  comment '显示顺序',
  leader            varchar(20)     default null               comment '负责人',
  phone             varchar(11)     default null               comment '联系电话',
  email             varchar(50)     default null               comment '邮箱',
  status            char(1)         default '0'                comment '部门状态（0正常 1停用）',
  del_flag          char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time 	    datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (dept_id)
) engine=innodb auto_increment=200 comment = '部门表';

-- ----------------------------
-- 初始化-部门表数据
-- ----------------------------
insert into sys_dept values(100,  0,   '0',          '若依科技',   0, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(101,  100, '0,100',      '深圳总公司', 1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(102,  100, '0,100',      '长沙分公司', 2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(103,  101, '0,100,101',  '研发部门',   1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(104,  101, '0,100,101',  '市场部门',   2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(105,  101, '0,100,101',  '测试部门',   3, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(106,  101, '0,100,101',  '财务部门',   4, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(107,  101, '0,100,101',  '运维部门',   5, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(108,  102, '0,100,102',  '市场部门',   1, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(109,  102, '0,100,102',  '财务部门',   2, '若依', '15888888888', 'ry@qq.com', '0', '0', 'admin', sysdate(), '', null);


-- ----------------------------
-- 2、用户信息表
-- ----------------------------
drop table if exists sys_user;
create table sys_user (
  user_id           bigint(20)      not null auto_increment    comment '用户ID',
  dept_id           bigint(20)      default null               comment '部门ID',
  user_name         varchar(30)     not null                   comment '用户账号',
  nick_name         varchar(30)     not null                   comment '用户昵称',
  user_type         varchar(2)      default '00'               comment '用户类型（00系统用户）',
  email             varchar(50)     default ''                 comment '用户邮箱',
  phonenumber       varchar(11)     default ''                 comment '手机号码',
  sex               char(1)         default '0'                comment '用户性别（0男 1女 2未知）',
  avatar            varchar(100)    default ''                 comment '头像地址',
  password          varchar(100)    default ''                 comment '密码',
  status            char(1)         default '0'                comment '账号状态（0正常 1停用）',
  del_flag          char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  login_ip          varchar(128)    default ''                 comment '最后登录IP',
  login_date        datetime                                   comment '最后登录时间',
  pwd_update_date   datetime                                   comment '密码最后更新时间',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (user_id)
) engine=innodb auto_increment=100 comment = '用户信息表';

-- ----------------------------
-- 初始化-用户信息表数据
-- ----------------------------
insert into sys_user values(1,  103, 'admin', '若依', '00', 'ry@163.com', '15888888888', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), sysdate(), 'admin', sysdate(), '', null, '管理员');
insert into sys_user values(2,  105, 'ry',    '若依', '00', 'ry@qq.com',  '15666666666', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), sysdate(), 'admin', sysdate(), '', null, '测试员');


-- ----------------------------
-- 3、岗位信息表
-- ----------------------------
drop table if exists sys_post;
create table sys_post
(
  post_id       bigint(20)      not null auto_increment    comment '岗位ID',
  post_code     varchar(64)     not null                   comment '岗位编码',
  post_name     varchar(50)     not null                   comment '岗位名称',
  post_sort     int(4)          not null                   comment '显示顺序',
  status        char(1)         not null                   comment '状态（0正常 1停用）',
  create_by     varchar(64)     default ''                 comment '创建者',
  create_time   datetime                                   comment '创建时间',
  update_by     varchar(64)     default ''			       comment '更新者',
  update_time   datetime                                   comment '更新时间',
  remark        varchar(500)    default null               comment '备注',
  primary key (post_id)
) engine=innodb comment = '岗位信息表';

-- ----------------------------
-- 初始化-岗位信息表数据
-- ----------------------------
insert into sys_post values(1, 'ceo',  '董事长',    1, '0', 'admin', sysdate(), '', null, '');
insert into sys_post values(2, 'se',   '项目经理',  2, '0', 'admin', sysdate(), '', null, '');
insert into sys_post values(3, 'hr',   '人力资源',  3, '0', 'admin', sysdate(), '', null, '');
insert into sys_post values(4, 'user', '普通员工',  4, '0', 'admin', sysdate(), '', null, '');


-- ----------------------------
-- 4、角色信息表
-- ----------------------------
drop table if exists sys_role;
create table sys_role (
  role_id              bigint(20)      not null auto_increment    comment '角色ID',
  role_name            varchar(30)     not null                   comment '角色名称',
  role_key             varchar(100)    not null                   comment '角色权限字符串',
  role_sort            int(4)          not null                   comment '显示顺序',
  data_scope           char(1)         default '1'                comment '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  menu_check_strictly  tinyint(1)      default 1                  comment '菜单树选择项是否关联显示',
  dept_check_strictly  tinyint(1)      default 1                  comment '部门树选择项是否关联显示',
  status               char(1)         not null                   comment '角色状态（0正常 1停用）',
  del_flag             char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  create_by            varchar(64)     default ''                 comment '创建者',
  create_time          datetime                                   comment '创建时间',
  update_by            varchar(64)     default ''                 comment '更新者',
  update_time          datetime                                   comment '更新时间',
  remark               varchar(500)    default null               comment '备注',
  primary key (role_id)
) engine=innodb auto_increment=100 comment = '角色信息表';

-- ----------------------------
-- 初始化-角色信息表数据
-- ----------------------------
insert into sys_role values('1', '超级管理员',  'admin',  1, 1, 1, 1, '0', '0', 'admin', sysdate(), '', null, '超级管理员');
insert into sys_role values('2', '普通角色',    'common', 2, 2, 1, 1, '0', '0', 'admin', sysdate(), '', null, '普通角色');
insert into sys_role values('3', '销售角色',    'sale',   3, 2, 1, 1, '0', '0', 'admin', sysdate(), '', null, '销售角色');
insert into sys_role values('4', '经理角色',    'manager',4, 2, 1, 1, '0', '0', 'admin', sysdate(), '', null, '经理角色');
insert into sys_role values('5', '财务角色',    'finance',5, 2, 1, 1, '0', '0', 'admin', sysdate(), '', null, '财务角色');


-- ----------------------------
-- 5、菜单权限表
-- ----------------------------
drop table if exists sys_menu;
create table sys_menu (
  menu_id           bigint(20)      not null auto_increment    comment '菜单ID',
  menu_name         varchar(50)     not null                   comment '菜单名称',
  parent_id         bigint(20)      default 0                  comment '父菜单ID',
  order_num         int(4)          default 0                  comment '显示顺序',
  path              varchar(200)    default ''                 comment '路由地址',
  component         varchar(255)    default null               comment '组件路径',
  query             varchar(255)    default null               comment '路由参数',
  route_name        varchar(50)     default ''                 comment '路由名称',
  is_frame          int(1)          default 1                  comment '是否为外链（0是 1否）',
  is_cache          int(1)          default 0                  comment '是否缓存（0缓存 1不缓存）',
  menu_type         char(1)         default ''                 comment '菜单类型（M目录 C菜单 F按钮）',
  visible           char(1)         default 0                  comment '菜单状态（0显示 1隐藏）',
  status            char(1)         default 0                  comment '菜单状态（0正常 1停用）',
  perms             varchar(100)    default null               comment '权限标识',
  icon              varchar(100)    default '#'                comment '菜单图标',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default ''                 comment '备注',
  primary key (menu_id)
) engine=innodb auto_increment=2000 comment = '菜单权限表';

-- ----------------------------
-- 初始化-菜单信息表数据
-- ----------------------------
-- 一级菜单
insert into sys_menu values('1', '系统管理', '0', '1', 'system',           null, '', '', 1, 0, 'M', '0', '0', '', 'system',   'admin', sysdate(), '', null, '系统管理目录');
insert into sys_menu values('2', '系统监控', '0', '2', 'monitor',          null, '', '', 1, 0, 'M', '0', '0', '', 'monitor',  'admin', sysdate(), '', null, '系统监控目录');
insert into sys_menu values('3', '系统工具', '0', '3', 'tool',             null, '', '', 1, 0, 'M', '0', '0', '', 'tool',     'admin', sysdate(), '', null, '系统工具目录');
insert into sys_menu values('4', '若依官网', '0', '4', 'http://ruoyi.vip', null, '', '', 0, 0, 'M', '0', '0', '', 'guide',    'admin', sysdate(), '', null, '若依官网地址');
-- 二级菜单
insert into sys_menu values('100',  '用户管理', '1',   '1', 'user',       'system/user/index',        '', '', 1, 0, 'C', '0', '0', 'system:user:list',        'user',          'admin', sysdate(), '', null, '用户管理菜单');
insert into sys_menu values('101',  '角色管理', '1',   '2', 'role',       'system/role/index',        '', '', 1, 0, 'C', '0', '0', 'system:role:list',        'peoples',       'admin', sysdate(), '', null, '角色管理菜单');
insert into sys_menu values('102',  '菜单管理', '1',   '3', 'menu',       'system/menu/index',        '', '', 1, 0, 'C', '0', '0', 'system:menu:list',        'tree-table',    'admin', sysdate(), '', null, '菜单管理菜单');
insert into sys_menu values('103',  '部门管理', '1',   '4', 'dept',       'system/dept/index',        '', '', 1, 0, 'C', '0', '0', 'system:dept:list',        'tree',          'admin', sysdate(), '', null, '部门管理菜单');
insert into sys_menu values('104',  '岗位管理', '1',   '5', 'post',       'system/post/index',        '', '', 1, 0, 'C', '0', '0', 'system:post:list',        'post',          'admin', sysdate(), '', null, '岗位管理菜单');
insert into sys_menu values('105',  '字典管理', '1',   '6', 'dict',       'system/dict/index',        '', '', 1, 0, 'C', '0', '0', 'system:dict:list',        'dict',          'admin', sysdate(), '', null, '字典管理菜单');
insert into sys_menu values('106',  '参数设置', '1',   '7', 'config',     'system/config/index',      '', '', 1, 0, 'C', '0', '0', 'system:config:list',      'edit',          'admin', sysdate(), '', null, '参数设置菜单');
insert into sys_menu values('107',  '通知公告', '1',   '8', 'notice',     'system/notice/index',      '', '', 1, 0, 'C', '0', '0', 'system:notice:list',      'message',       'admin', sysdate(), '', null, '通知公告菜单');
insert into sys_menu values('108',  '日志管理', '1',   '9', 'log',        '',                         '', '', 1, 0, 'M', '0', '0', '',                        'log',           'admin', sysdate(), '', null, '日志管理菜单');
insert into sys_menu values('109',  '在线用户', '2',   '1', 'online',     'monitor/online/index',     '', '', 1, 0, 'C', '0', '0', 'monitor:online:list',     'online',        'admin', sysdate(), '', null, '在线用户菜单');
insert into sys_menu values('110',  '定时任务', '2',   '2', 'job',        'monitor/job/index',        '', '', 1, 0, 'C', '0', '0', 'monitor:job:list',        'job',           'admin', sysdate(), '', null, '定时任务菜单');
insert into sys_menu values('111',  '数据监控', '2',   '3', 'druid',      'monitor/druid/index',      '', '', 1, 0, 'C', '0', '0', 'monitor:druid:list',      'druid',         'admin', sysdate(), '', null, '数据监控菜单');
insert into sys_menu values('112',  '服务监控', '2',   '4', 'server',     'monitor/server/index',     '', '', 1, 0, 'C', '0', '0', 'monitor:server:list',     'server',        'admin', sysdate(), '', null, '服务监控菜单');
insert into sys_menu values('113',  '缓存监控', '2',   '5', 'cache',      'monitor/cache/index',      '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list',      'redis',         'admin', sysdate(), '', null, '缓存监控菜单');
insert into sys_menu values('114',  '缓存列表', '2',   '6', 'cacheList',  'monitor/cache/list',       '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list',      'redis-list',    'admin', sysdate(), '', null, '缓存列表菜单');
insert into sys_menu values('115',  '表单构建', '3',   '1', 'build',      'tool/build/index',         '', '', 1, 0, 'C', '0', '0', 'tool:build:list',         'build',         'admin', sysdate(), '', null, '表单构建菜单');
insert into sys_menu values('116',  '代码生成', '3',   '2', 'gen',        'tool/gen/index',           '', '', 1, 0, 'C', '0', '0', 'tool:gen:list',           'code',          'admin', sysdate(), '', null, '代码生成菜单');
insert into sys_menu values('117',  '系统接口', '3',   '3', 'swagger',    'tool/swagger/index',       '', '', 1, 0, 'C', '0', '0', 'tool:swagger:list',       'swagger',       'admin', sysdate(), '', null, '系统接口菜单');
-- 三级菜单
insert into sys_menu values('500',  '操作日志', '108', '1', 'operlog',    'monitor/operlog/index',    '', '', 1, 0, 'C', '0', '0', 'monitor:operlog:list',    'form',          'admin', sysdate(), '', null, '操作日志菜单');
insert into sys_menu values('501',  '登录日志', '108', '2', 'logininfor', 'monitor/logininfor/index', '', '', 1, 0, 'C', '0', '0', 'monitor:logininfor:list', 'logininfor',    'admin', sysdate(), '', null, '登录日志菜单');
-- 用户管理按钮
insert into sys_menu values('1000', '用户查询', '100', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1001', '用户新增', '100', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1002', '用户修改', '100', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1003', '用户删除', '100', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1004', '用户导出', '100', '5',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:export',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1005', '用户导入', '100', '6',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:import',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1006', '重置密码', '100', '7',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd',       '#', 'admin', sysdate(), '', null, '');
-- 角色管理按钮
insert into sys_menu values('1007', '角色查询', '101', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1008', '角色新增', '101', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1009', '角色修改', '101', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1010', '角色删除', '101', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1011', '角色导出', '101', '5',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:export',         '#', 'admin', sysdate(), '', null, '');
-- 菜单管理按钮
insert into sys_menu values('1012', '菜单查询', '102', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1013', '菜单新增', '102', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1014', '菜单修改', '102', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1015', '菜单删除', '102', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove',         '#', 'admin', sysdate(), '', null, '');
-- 部门管理按钮
insert into sys_menu values('1016', '部门查询', '103', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1017', '部门新增', '103', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1018', '部门修改', '103', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1019', '部门删除', '103', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove',         '#', 'admin', sysdate(), '', null, '');
-- 岗位管理按钮
insert into sys_menu values('1020', '岗位查询', '104', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1021', '岗位新增', '104', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1022', '岗位修改', '104', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1023', '岗位删除', '104', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1024', '岗位导出', '104', '5',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:export',         '#', 'admin', sysdate(), '', null, '');
-- 字典管理按钮
insert into sys_menu values('1025', '字典查询', '105', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1026', '字典新增', '105', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1027', '字典修改', '105', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1028', '字典删除', '105', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1029', '字典导出', '105', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:export',         '#', 'admin', sysdate(), '', null, '');
-- 参数设置按钮
insert into sys_menu values('1030', '参数查询', '106', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:query',        '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1031', '参数新增', '106', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:add',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1032', '参数修改', '106', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:edit',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1033', '参数删除', '106', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:remove',       '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1034', '参数导出', '106', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:export',       '#', 'admin', sysdate(), '', null, '');
-- 通知公告按钮
insert into sys_menu values('1035', '公告查询', '107', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:query',        '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1036', '公告新增', '107', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:add',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1037', '公告修改', '107', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:edit',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1038', '公告删除', '107', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:remove',       '#', 'admin', sysdate(), '', null, '');
-- 操作日志按钮
insert into sys_menu values('1039', '操作查询', '500', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query',      '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1040', '操作删除', '500', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove',     '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1041', '日志导出', '500', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export',     '#', 'admin', sysdate(), '', null, '');
-- 登录日志按钮
insert into sys_menu values('1042', '登录查询', '501', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1043', '登录删除', '501', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1044', '日志导出', '501', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1045', '账户解锁', '501', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:unlock',  '#', 'admin', sysdate(), '', null, '');
-- 在线用户按钮
insert into sys_menu values('1046', '在线查询', '109', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:query',       '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1047', '批量强退', '109', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1048', '单条强退', '109', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin', sysdate(), '', null, '');
-- 定时任务按钮
insert into sys_menu values('1049', '任务查询', '110', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1050', '任务新增', '110', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1051', '任务修改', '110', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1052', '任务删除', '110', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1053', '状态修改', '110', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:changeStatus',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1054', '任务导出', '110', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:export',         '#', 'admin', sysdate(), '', null, '');
-- 代码生成按钮
insert into sys_menu values('1055', '生成查询', '116', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:query',             '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1056', '生成修改', '116', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:edit',              '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1057', '生成删除', '116', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:remove',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1058', '导入代码', '116', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:import',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1059', '预览代码', '116', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:preview',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1060', '生成代码', '116', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:code',              '#', 'admin', sysdate(), '', null, '');


-- ----------------------------
-- 6、用户和角色关联表  用户N-1角色
-- ----------------------------
drop table if exists sys_user_role;
create table sys_user_role (
  user_id   bigint(20) not null comment '用户ID',
  role_id   bigint(20) not null comment '角色ID',
  primary key(user_id, role_id)
) engine=innodb comment = '用户和角色关联表';

-- ----------------------------
-- 初始化-用户和角色关联表数据
-- ----------------------------
insert into sys_user_role values ('1', '1');
insert into sys_user_role values ('2', '2');


-- ----------------------------
-- 7、角色和菜单关联表  角色1-N菜单
-- ----------------------------
drop table if exists sys_role_menu;
create table sys_role_menu (
  role_id   bigint(20) not null comment '角色ID',
  menu_id   bigint(20) not null comment '菜单ID',
  primary key(role_id, menu_id)
) engine=innodb comment = '角色和菜单关联表';

-- ----------------------------
-- 初始化-角色和菜单关联表数据
-- ----------------------------
insert into sys_role_menu values ('2', '1');
insert into sys_role_menu values ('2', '2');
insert into sys_role_menu values ('2', '3');
insert into sys_role_menu values ('2', '4');
insert into sys_role_menu values ('2', '100');
insert into sys_role_menu values ('2', '101');
insert into sys_role_menu values ('2', '102');
insert into sys_role_menu values ('2', '103');
insert into sys_role_menu values ('2', '104');
insert into sys_role_menu values ('2', '105');
insert into sys_role_menu values ('2', '106');
insert into sys_role_menu values ('2', '107');
insert into sys_role_menu values ('2', '108');
insert into sys_role_menu values ('2', '109');
insert into sys_role_menu values ('2', '110');
insert into sys_role_menu values ('2', '111');
insert into sys_role_menu values ('2', '112');
insert into sys_role_menu values ('2', '113');
insert into sys_role_menu values ('2', '114');
insert into sys_role_menu values ('2', '115');
insert into sys_role_menu values ('2', '116');
insert into sys_role_menu values ('2', '117');
insert into sys_role_menu values ('2', '500');
insert into sys_role_menu values ('2', '501');
insert into sys_role_menu values ('2', '1000');
insert into sys_role_menu values ('2', '1001');
insert into sys_role_menu values ('2', '1002');
insert into sys_role_menu values ('2', '1003');
insert into sys_role_menu values ('2', '1004');
insert into sys_role_menu values ('2', '1005');
insert into sys_role_menu values ('2', '1006');
insert into sys_role_menu values ('2', '1007');
insert into sys_role_menu values ('2', '1008');
insert into sys_role_menu values ('2', '1009');
insert into sys_role_menu values ('2', '1010');
insert into sys_role_menu values ('2', '1011');
insert into sys_role_menu values ('2', '1012');
insert into sys_role_menu values ('2', '1013');
insert into sys_role_menu values ('2', '1014');
insert into sys_role_menu values ('2', '1015');
insert into sys_role_menu values ('2', '1016');
insert into sys_role_menu values ('2', '1017');
insert into sys_role_menu values ('2', '1018');
insert into sys_role_menu values ('2', '1019');
insert into sys_role_menu values ('2', '1020');
insert into sys_role_menu values ('2', '1021');
insert into sys_role_menu values ('2', '1022');
insert into sys_role_menu values ('2', '1023');
insert into sys_role_menu values ('2', '1024');
insert into sys_role_menu values ('2', '1025');
insert into sys_role_menu values ('2', '1026');
insert into sys_role_menu values ('2', '1027');
insert into sys_role_menu values ('2', '1028');
insert into sys_role_menu values ('2', '1029');
insert into sys_role_menu values ('2', '1030');
insert into sys_role_menu values ('2', '1031');
insert into sys_role_menu values ('2', '1032');
insert into sys_role_menu values ('2', '1033');
insert into sys_role_menu values ('2', '1034');
insert into sys_role_menu values ('2', '1035');
insert into sys_role_menu values ('2', '1036');
insert into sys_role_menu values ('2', '1037');
insert into sys_role_menu values ('2', '1038');
insert into sys_role_menu values ('2', '1039');
insert into sys_role_menu values ('2', '1040');
insert into sys_role_menu values ('2', '1041');
insert into sys_role_menu values ('2', '1042');
insert into sys_role_menu values ('2', '1043');
insert into sys_role_menu values ('2', '1044');
insert into sys_role_menu values ('2', '1045');
insert into sys_role_menu values ('2', '1046');
insert into sys_role_menu values ('2', '1047');
insert into sys_role_menu values ('2', '1048');
insert into sys_role_menu values ('2', '1049');
insert into sys_role_menu values ('2', '1050');
insert into sys_role_menu values ('2', '1051');
insert into sys_role_menu values ('2', '1052');
insert into sys_role_menu values ('2', '1053');
insert into sys_role_menu values ('2', '1054');
insert into sys_role_menu values ('2', '1055');
insert into sys_role_menu values ('2', '1056');
insert into sys_role_menu values ('2', '1057');
insert into sys_role_menu values ('2', '1058');
insert into sys_role_menu values ('2', '1059');
insert into sys_role_menu values ('2', '1060');

-- ----------------------------
-- 8、角色和部门关联表  角色1-N部门
-- ----------------------------
drop table if exists sys_role_dept;
create table sys_role_dept (
  role_id   bigint(20) not null comment '角色ID',
  dept_id   bigint(20) not null comment '部门ID',
  primary key(role_id, dept_id)
) engine=innodb comment = '角色和部门关联表';

-- ----------------------------
-- 初始化-角色和部门关联表数据
-- ----------------------------
insert into sys_role_dept values ('2', '100');
insert into sys_role_dept values ('2', '101');
insert into sys_role_dept values ('2', '105');


-- ----------------------------
-- 9、用户与岗位关联表  用户1-N岗位
-- ----------------------------
drop table if exists sys_user_post;
create table sys_user_post
(
  user_id   bigint(20) not null comment '用户ID',
  post_id   bigint(20) not null comment '岗位ID',
  primary key (user_id, post_id)
) engine=innodb comment = '用户与岗位关联表';

-- ----------------------------
-- 初始化-用户与岗位关联表数据
-- ----------------------------
insert into sys_user_post values ('1', '1');
insert into sys_user_post values ('2', '2');


-- ----------------------------
-- 10、操作日志记录
-- ----------------------------
drop table if exists sys_oper_log;
create table sys_oper_log (
  oper_id           bigint(20)      not null auto_increment    comment '日志主键',
  title             varchar(50)     default ''                 comment '模块标题',
  business_type     int(2)          default 0                  comment '业务类型（0其它 1新增 2修改 3删除）',
  method            varchar(200)    default ''                 comment '方法名称',
  request_method    varchar(10)     default ''                 comment '请求方式',
  operator_type     int(1)          default 0                  comment '操作类别（0其它 1后台用户 2手机端用户）',
  oper_name         varchar(50)     default ''                 comment '操作人员',
  dept_name         varchar(50)     default ''                 comment '部门名称',
  oper_url          varchar(255)    default ''                 comment '请求URL',
  oper_ip           varchar(128)    default ''                 comment '主机地址',
  oper_location     varchar(255)    default ''                 comment '操作地点',
  oper_param        varchar(2000)   default ''                 comment '请求参数',
  json_result       varchar(2000)   default ''                 comment '返回参数',
  status            int(1)          default 0                  comment '操作状态（0正常 1异常）',
  error_msg         varchar(2000)   default ''                 comment '错误消息',
  oper_time         datetime                                   comment '操作时间',
  cost_time         bigint(20)      default 0                  comment '消耗时间',
  primary key (oper_id),
  key idx_sys_oper_log_bt (business_type),
  key idx_sys_oper_log_s  (status),
  key idx_sys_oper_log_ot (oper_time)
) engine=innodb auto_increment=100 comment = '操作日志记录';


-- ----------------------------
-- 11、字典类型表
-- ----------------------------
drop table if exists sys_dict_type;
create table sys_dict_type
(
  dict_id          bigint(20)      not null auto_increment    comment '字典主键',
  dict_name        varchar(100)    default ''                 comment '字典名称',
  dict_type        varchar(100)    default ''                 comment '字典类型',
  status           char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by        varchar(64)     default ''                 comment '创建者',
  create_time      datetime                                   comment '创建时间',
  update_by        varchar(64)     default ''                 comment '更新者',
  update_time      datetime                                   comment '更新时间',
  remark           varchar(500)    default null               comment '备注',
  primary key (dict_id),
  unique (dict_type)
) engine=innodb auto_increment=100 comment = '字典类型表';

insert into sys_dict_type values(1,  '用户性别', 'sys_user_sex',        '0', 'admin', sysdate(), '', null, '用户性别列表');
insert into sys_dict_type values(2,  '菜单状态', 'sys_show_hide',       '0', 'admin', sysdate(), '', null, '菜单状态列表');
insert into sys_dict_type values(3,  '系统开关', 'sys_normal_disable',  '0', 'admin', sysdate(), '', null, '系统开关列表');
insert into sys_dict_type values(4,  '任务状态', 'sys_job_status',      '0', 'admin', sysdate(), '', null, '任务状态列表');
insert into sys_dict_type values(5,  '任务分组', 'sys_job_group',       '0', 'admin', sysdate(), '', null, '任务分组列表');
insert into sys_dict_type values(6,  '系统是否', 'sys_yes_no',          '0', 'admin', sysdate(), '', null, '系统是否列表');
insert into sys_dict_type values(7,  '通知类型', 'sys_notice_type',     '0', 'admin', sysdate(), '', null, '通知类型列表');
insert into sys_dict_type values(8,  '通知状态', 'sys_notice_status',   '0', 'admin', sysdate(), '', null, '通知状态列表');
insert into sys_dict_type values(9,  '操作类型', 'sys_oper_type',       '0', 'admin', sysdate(), '', null, '操作类型列表');
insert into sys_dict_type values(10, '系统状态', 'sys_common_status',   '0', 'admin', sysdate(), '', null, '登录状态列表');


-- ----------------------------
-- 12、字典数据表
-- ----------------------------
drop table if exists sys_dict_data;
create table sys_dict_data
(
  dict_code        bigint(20)      not null auto_increment    comment '字典编码',
  dict_sort        int(4)          default 0                  comment '字典排序',
  dict_label       varchar(100)    default ''                 comment '字典标签',
  dict_value       varchar(100)    default ''                 comment '字典键值',
  dict_type        varchar(100)    default ''                 comment '字典类型',
  css_class        varchar(100)    default null               comment '样式属性（其他样式扩展）',
  list_class       varchar(100)    default null               comment '表格回显样式',
  is_default       char(1)         default 'N'                comment '是否默认（Y是 N否）',
  status           char(1)         default '0'                comment '状态（0正常 1停用）',
  create_by        varchar(64)     default ''                 comment '创建者',
  create_time      datetime                                   comment '创建时间',
  update_by        varchar(64)     default ''                 comment '更新者',
  update_time      datetime                                   comment '更新时间',
  remark           varchar(500)    default null               comment '备注',
  primary key (dict_code)
) engine=innodb auto_increment=100 comment = '字典数据表';

insert into sys_dict_data values(1,  1,  '男',       '0',       'sys_user_sex',        '',   '',        'Y', '0', 'admin', sysdate(), '', null, '性别男');
insert into sys_dict_data values(2,  2,  '女',       '1',       'sys_user_sex',        '',   '',        'N', '0', 'admin', sysdate(), '', null, '性别女');
insert into sys_dict_data values(3,  3,  '未知',     '2',       'sys_user_sex',        '',   '',        'N', '0', 'admin', sysdate(), '', null, '性别未知');
insert into sys_dict_data values(4,  1,  '显示',     '0',       'sys_show_hide',       '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '显示菜单');
insert into sys_dict_data values(5,  2,  '隐藏',     '1',       'sys_show_hide',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '隐藏菜单');
insert into sys_dict_data values(6,  1,  '正常',     '0',       'sys_normal_disable',  '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(7,  2,  '停用',     '1',       'sys_normal_disable',  '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '停用状态');
insert into sys_dict_data values(8,  1,  '正常',     '0',       'sys_job_status',      '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(9,  2,  '暂停',     '1',       'sys_job_status',      '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '停用状态');
insert into sys_dict_data values(10, 1,  '默认',     'DEFAULT', 'sys_job_group',       '',   '',        'Y', '0', 'admin', sysdate(), '', null, '默认分组');
insert into sys_dict_data values(11, 2,  '系统',     'SYSTEM',  'sys_job_group',       '',   '',        'N', '0', 'admin', sysdate(), '', null, '系统分组');
insert into sys_dict_data values(12, 1,  '是',       'Y',       'sys_yes_no',          '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '系统默认是');
insert into sys_dict_data values(13, 2,  '否',       'N',       'sys_yes_no',          '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '系统默认否');
insert into sys_dict_data values(14, 1,  '通知',     '1',       'sys_notice_type',     '',   'warning', 'Y', '0', 'admin', sysdate(), '', null, '通知');
insert into sys_dict_data values(15, 2,  '公告',     '2',       'sys_notice_type',     '',   'success', 'N', '0', 'admin', sysdate(), '', null, '公告');
insert into sys_dict_data values(16, 1,  '正常',     '0',       'sys_notice_status',   '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(17, 2,  '关闭',     '1',       'sys_notice_status',   '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '关闭状态');
insert into sys_dict_data values(18, 99, '其他',     '0',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, '其他操作');
insert into sys_dict_data values(19, 1,  '新增',     '1',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, '新增操作');
insert into sys_dict_data values(20, 2,  '修改',     '2',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, '修改操作');
insert into sys_dict_data values(21, 3,  '删除',     '3',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '删除操作');
insert into sys_dict_data values(22, 4,  '授权',     '4',       'sys_oper_type',       '',   'primary', 'N', '0', 'admin', sysdate(), '', null, '授权操作');
insert into sys_dict_data values(23, 5,  '导出',     '5',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, '导出操作');
insert into sys_dict_data values(24, 6,  '导入',     '6',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, '导入操作');
insert into sys_dict_data values(25, 7,  '强退',     '7',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '强退操作');
insert into sys_dict_data values(26, 8,  '生成代码', '8',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, '生成操作');
insert into sys_dict_data values(27, 9,  '清空数据', '9',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '清空操作');
insert into sys_dict_data values(28, 1,  '成功',     '0',       'sys_common_status',   '',   'primary', 'N', '0', 'admin', sysdate(), '', null, '正常状态');
insert into sys_dict_data values(29, 2,  '失败',     '1',       'sys_common_status',   '',   'danger',  'N', '0', 'admin', sysdate(), '', null, '停用状态');


-- ----------------------------
-- 13、参数配置表
-- ----------------------------
drop table if exists sys_config;
create table sys_config (
  config_id         int(5)          not null auto_increment    comment '参数主键',
  config_name       varchar(100)    default ''                 comment '参数名称',
  config_key        varchar(100)    default ''                 comment '参数键名',
  config_value      varchar(500)    default ''                 comment '参数键值',
  config_type       char(1)         default 'N'                comment '系统内置（Y是 N否）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (config_id)
) engine=innodb auto_increment=100 comment = '参数配置表';

insert into sys_config values(1, '主框架页-默认皮肤样式名称',     'sys.index.skinName',               'skin-blue',     'Y', 'admin', sysdate(), '', null, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow' );
insert into sys_config values(2, '用户管理-账号初始密码',         'sys.user.initPassword',            '123456',        'Y', 'admin', sysdate(), '', null, '初始化密码 123456' );
insert into sys_config values(3, '主框架页-侧边栏主题',           'sys.index.sideTheme',              'theme-dark',    'Y', 'admin', sysdate(), '', null, '深色主题theme-dark，浅色主题theme-light' );
insert into sys_config values(4, '账号自助-验证码开关',           'sys.account.captchaEnabled',       'true',          'Y', 'admin', sysdate(), '', null, '是否开启验证码功能（true开启，false关闭）');
insert into sys_config values(5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser',         'false',         'Y', 'admin', sysdate(), '', null, '是否开启注册用户功能（true开启，false关闭）');
insert into sys_config values(6, '用户登录-黑名单列表',           'sys.login.blackIPList',            '',              'Y', 'admin', sysdate(), '', null, '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）');
insert into sys_config values(7, '用户管理-初始密码修改策略',     'sys.account.initPasswordModify',   '1',             'Y', 'admin', sysdate(), '', null, '0：初始密码修改策略关闭，没有任何提示，1：提醒用户，如果未修改初始密码，则在登录时就会提醒修改密码对话框');
insert into sys_config values(8, '用户管理-账号密码更新周期',     'sys.account.passwordValidateDays', '0',             'Y', 'admin', sysdate(), '', null, '密码更新周期（填写数字，数据初始化值为0不限制，若修改必须为大于0小于365的正整数），如果超过这个周期登录系统时，则在登录时就会提醒修改密码对话框');


-- ----------------------------
-- 14、系统访问记录
-- ----------------------------
drop table if exists sys_logininfor;
create table sys_logininfor (
  info_id        bigint(20)     not null auto_increment   comment '访问ID',
  user_name      varchar(50)    default ''                comment '用户账号',
  ipaddr         varchar(128)   default ''                comment '登录IP地址',
  login_location varchar(255)   default ''                comment '登录地点',
  browser        varchar(50)    default ''                comment '浏览器类型',
  os             varchar(50)    default ''                comment '操作系统',
  status         char(1)        default '0'               comment '登录状态（0成功 1失败）',
  msg            varchar(255)   default ''                comment '提示消息',
  login_time     datetime                                 comment '访问时间',
  primary key (info_id),
  key idx_sys_logininfor_s  (status),
  key idx_sys_logininfor_lt (login_time)
) engine=innodb auto_increment=100 comment = '系统访问记录';


-- ----------------------------
-- 15、定时任务调度表
-- ----------------------------
drop table if exists sys_job;
create table sys_job (
  job_id              bigint(20)    not null auto_increment    comment '任务ID',
  job_name            varchar(64)   default ''                 comment '任务名称',
  job_group           varchar(64)   default 'DEFAULT'          comment '任务组名',
  invoke_target       varchar(500)  not null                   comment '调用目标字符串',
  cron_expression     varchar(255)  default ''                 comment 'cron执行表达式',
  misfire_policy      varchar(20)   default '3'                comment '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  concurrent          char(1)       default '1'                comment '是否并发执行（0允许 1禁止）',
  status              char(1)       default '0'                comment '状态（0正常 1暂停）',
  create_by           varchar(64)   default ''                 comment '创建者',
  create_time         datetime                                 comment '创建时间',
  update_by           varchar(64)   default ''                 comment '更新者',
  update_time         datetime                                 comment '更新时间',
  remark              varchar(500)  default ''                 comment '备注信息',
  primary key (job_id, job_name, job_group)
) engine=innodb auto_increment=100 comment = '定时任务调度表';

insert into sys_job values(1, '系统默认（无参）', 'DEFAULT', 'ryTask.ryNoParams',        '0/10 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', null, '');
insert into sys_job values(2, '系统默认（有参）', 'DEFAULT', 'ryTask.ryParams(\'ry\')',  '0/15 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', null, '');
insert into sys_job values(3, '系统默认（多参）', 'DEFAULT', 'ryTask.ryMultipleParams(\'ry\', true, 2000L, 316.50D, 100)',  '0/20 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', null, '');
insert into sys_job values(4, '应收到期提醒推送', 'DEFAULT', 'finReceivableRemindTask.pushDueReminder(\'7\')', '0 0 8 * * ?', '3', '1', '0', 'admin', sysdate(), '', null, '每天08:00推送应收到期与逾期提醒');
insert into sys_job values(5, '库存预警提醒推送', 'DEFAULT', 'wmsStockWarningRemindTask.pushWarningReminder()', '0 30 8 * * ?', '3', '1', '0', 'admin', sysdate(), '', null, '每天08:30推送库存预警提醒');
insert into sys_job values(6, '业务数据自动备份', 'DEFAULT', 'bizDataBackupTask.autoBackup()', '0 0 2 * * ?', '3', '1', '0', 'admin', sysdate(), '', null, '每天02:00自动备份业务数据');


-- ----------------------------
-- 16、定时任务调度日志表
-- ----------------------------
drop table if exists sys_job_log;
create table sys_job_log (
  job_log_id          bigint(20)     not null auto_increment    comment '任务日志ID',
  job_name            varchar(64)    not null                   comment '任务名称',
  job_group           varchar(64)    not null                   comment '任务组名',
  invoke_target       varchar(500)   not null                   comment '调用目标字符串',
  job_message         varchar(500)                              comment '日志信息',
  status              char(1)        default '0'                comment '执行状态（0正常 1失败）',
  exception_info      varchar(2000)  default ''                 comment '异常信息',
  create_time         datetime                                  comment '创建时间',
  primary key (job_log_id)
) engine=innodb comment = '定时任务调度日志表';


-- ----------------------------
-- 17、通知公告表
-- ----------------------------
drop table if exists sys_notice;
create table sys_notice (
  notice_id         int(4)          not null auto_increment    comment '公告ID',
  notice_title      varchar(50)     not null                   comment '公告标题',
  notice_type       char(1)         not null                   comment '公告类型（1通知 2公告）',
  notice_content    longblob        default null               comment '公告内容',
  status            char(1)         default '0'                comment '公告状态（0正常 1关闭）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(255)    default null               comment '备注',
  primary key (notice_id)
) engine=innodb auto_increment=10 comment = '通知公告表';

-- ----------------------------
-- 初始化-公告信息表数据
-- ----------------------------
insert into sys_notice values('1', '温馨提醒：2018-07-01 若依新版本发布啦', '2', '新版本内容', '0', 'admin', sysdate(), '', null, '管理员');
insert into sys_notice values('2', '维护通知：2018-07-01 若依系统凌晨维护', '1', '维护内容',   '0', 'admin', sysdate(), '', null, '管理员');


-- ----------------------------
-- 18、代码生成业务表
-- ----------------------------
drop table if exists gen_table;
create table gen_table (
  table_id          bigint(20)      not null auto_increment    comment '编号',
  table_name        varchar(200)    default ''                 comment '表名称',
  table_comment     varchar(500)    default ''                 comment '表描述',
  sub_table_name    varchar(64)     default null               comment '关联子表的表名',
  sub_table_fk_name varchar(64)     default null               comment '子表关联的外键名',
  class_name        varchar(100)    default ''                 comment '实体类名称',
  tpl_category      varchar(200)    default 'crud'             comment '使用的模板（crud单表操作 tree树表操作）',
  tpl_web_type      varchar(30)     default ''                 comment '前端模板类型（element-ui模版 element-plus模版）',
  package_name      varchar(100)                               comment '生成包路径',
  module_name       varchar(30)                                comment '生成模块名',
  business_name     varchar(30)                                comment '生成业务名',
  function_name     varchar(50)                                comment '生成功能名',
  function_author   varchar(50)                                comment '生成功能作者',
  gen_type          char(1)         default '0'                comment '生成代码方式（0zip压缩包 1自定义路径）',
  gen_path          varchar(200)    default '/'                comment '生成路径（不填默认项目路径）',
  options           varchar(1000)                              comment '其它生成选项',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time 	    datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (table_id)
) engine=innodb auto_increment=1 comment = '代码生成业务表';


-- ----------------------------
-- 19、代码生成业务表字段
-- ----------------------------
drop table if exists gen_table_column;
create table gen_table_column (
  column_id         bigint(20)      not null auto_increment    comment '编号',
  table_id          bigint(20)                                 comment '归属表编号',
  column_name       varchar(200)                               comment '列名称',
  column_comment    varchar(500)                               comment '列描述',
  column_type       varchar(100)                               comment '列类型',
  java_type         varchar(500)                               comment 'JAVA类型',
  java_field        varchar(200)                               comment 'JAVA字段名',
  is_pk             char(1)                                    comment '是否主键（1是）',
  is_increment      char(1)                                    comment '是否自增（1是）',
  is_required       char(1)                                    comment '是否必填（1是）',
  is_insert         char(1)                                    comment '是否为插入字段（1是）',
  is_edit           char(1)                                    comment '是否编辑字段（1是）',
  is_list           char(1)                                    comment '是否列表字段（1是）',
  is_query          char(1)                                    comment '是否查询字段（1是）',
  query_type        varchar(200)    default 'EQ'               comment '查询方式（等于、不等于、大于、小于、范围）',
  html_type         varchar(200)                               comment '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  dict_type         varchar(200)    default ''                 comment '字典类型',
  sort              int                                        comment '排序',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time 	    datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  primary key (column_id)
) engine=innodb auto_increment=1 comment = '代码生成业务表字段';

-- ----------------------------
-- 20、销售订单主表
-- ----------------------------
drop table if exists wms_sale_order;
create table wms_sale_order (
  sale_order_id      bigint(20)      not null auto_increment comment '销售单主键',
  order_no           varchar(32)     not null                comment '销售单号',
  customer_id        bigint(20)      not null                comment '客户编号',
  warehouse_id       bigint(20)      not null                comment '仓库编号',
  total_qty          decimal(18,2)   default 0               comment '总数量',
  total_amount       decimal(18,2)   default 0               comment '总金额',
  status             varchar(20)     default 'draft'         comment '状态',
  payment_status     varchar(20)     default 'unpaid'        comment '付款状态',
  manager_audit_by   varchar(64)     default null            comment '经理审核人',
  manager_audit_time datetime                                  comment '经理审核时间',
  manager_audit_comment varchar(500) default null            comment '经理审核意见',
  audit_by           varchar(64)     default null            comment '审核人',
  audit_time         datetime                                  comment '审核时间',
  finance_audit_comment varchar(500) default null            comment '财务审核意见',
  create_by          varchar(64)     default ''              comment '创建者',
  create_time        datetime                                  comment '创建时间',
  update_by          varchar(64)     default ''              comment '更新者',
  update_time        datetime                                  comment '更新时间',
  remark             varchar(255)    default null            comment '备注',
  primary key (sale_order_id),
  unique key uk_wms_sale_order_no (order_no)
) engine=innodb auto_increment=1 comment='销售订单主表';

-- ----------------------------
-- 21、销售订单明细表
-- ----------------------------
drop table if exists wms_sale_order_item;
create table wms_sale_order_item (
  sale_order_item_id bigint(20)      not null auto_increment comment '销售明细主键',
  sale_order_id      bigint(20)      not null                comment '销售单编号',
  product_id         bigint(20)      not null                comment '商品编号',
  location_id        bigint(20)      default null            comment '库位编号',
  batch_no           varchar(64)     default null            comment '批次号',
  quantity           decimal(18,2)   default 0               comment '数量',
  price              decimal(18,2)   default 0               comment '单价',
  amount             decimal(18,2)   default 0               comment '金额',
  primary key (sale_order_item_id),
  key idx_wms_sale_order_item_order_id (sale_order_id)
) engine=innodb auto_increment=1 comment='销售订单明细表';

-- ----------------------------
-- 21.1、销售订单状态历史表
-- ----------------------------
drop table if exists wms_sale_order_status_history;
create table wms_sale_order_status_history (
  history_id         bigint(20)      not null auto_increment comment '状态历史主键',
  sale_order_id      bigint(20)      not null                comment '销售单编号',
  from_status        varchar(20)     default null            comment '原状态',
  to_status          varchar(20)     default null            comment '新状态',
  operation_type     varchar(50)     default null            comment '操作类型',
  audit_role         varchar(30)     default null            comment '审核角色',
  audit_by           varchar(64)     default null            comment '操作人',
  audit_comment      varchar(500)    default null            comment '审核意见',
  operate_time       datetime                                  comment '操作时间',
  create_by          varchar(64)     default ''              comment '创建者',
  create_time        datetime                                  comment '创建时间',
  update_by          varchar(64)     default ''              comment '更新者',
  update_time        datetime                                  comment '更新时间',
  remark             varchar(255)    default null            comment '备注',
  primary key (history_id),
  key idx_wms_sale_order_status_history_order_id (sale_order_id)
) engine=innodb auto_increment=1 comment='销售订单状态历史表';

-- ----------------------------
-- 22、应收台账表
-- ----------------------------
drop table if exists fin_receivable;
create table fin_receivable (
  receivable_id      bigint(20)      not null auto_increment comment '应收主键',
  sale_order_id      bigint(20)      not null                comment '销售单编号',
  customer_id        bigint(20)      not null                comment '客户编号',
  amount_due         decimal(18,2)   default 0               comment '应收金额',
  amount_paid        decimal(18,2)   default 0               comment '已收金额',
  status             varchar(20)     default 'unpaid'        comment '状态',
  due_date           datetime                                  comment '到期日期',
  create_by          varchar(64)     default ''              comment '创建者',
  create_time        datetime                                  comment '创建时间',
  update_by          varchar(64)     default ''              comment '更新者',
  update_time        datetime                                  comment '更新时间',
  remark             varchar(255)    default null            comment '备注',
  primary key (receivable_id),
  unique key uk_fin_receivable_sale_order_id (sale_order_id)
) engine=innodb auto_increment=1 comment='应收台账表';

-- ----------------------------
-- 23、回款登记表
-- ----------------------------
drop table if exists fin_receipt;
create table fin_receipt (
  receipt_id         bigint(20)      not null auto_increment comment '回款主键',
  receivable_id      bigint(20)      not null                comment '应收编号',
  sale_order_id      bigint(20)      not null                comment '销售单编号',
  customer_id        bigint(20)      not null                comment '客户编号',
  amount             decimal(18,2)   default 0               comment '回款金额',
  receipt_date       datetime                                  comment '回款日期',
  remark             varchar(255)    default null            comment '备注',
  create_by          varchar(64)     default ''              comment '创建者',
  create_time        datetime                                  comment '创建时间',
  update_by          varchar(64)     default ''              comment '更新者',
  update_time        datetime                                  comment '更新时间',
  primary key (receipt_id),
  key idx_fin_receipt_receivable_id (receivable_id)
) engine=innodb auto_increment=1 comment='回款登记表';

-- ----------------------------
-- 24、仓库资料表
-- ----------------------------
drop table if exists md_warehouse;
create table md_warehouse (
  warehouse_id       bigint(20)      not null auto_increment comment '仓库主键',
  warehouse_code     varchar(64)     not null                comment '仓库编码',
  warehouse_name     varchar(128)    not null                comment '仓库名称',
  contact_name       varchar(64)     default null            comment '联系人',
  contact_phone      varchar(32)     default null            comment '联系电话',
  address            varchar(255)    default null            comment '地址',
  status             char(1)         default '0'             comment '状态',
  del_flag           char(1)         default '0'             comment '删除标记',
  create_by          varchar(64)     default ''              comment '创建者',
  create_time        datetime                                  comment '创建时间',
  update_by          varchar(64)     default ''              comment '更新者',
  update_time        datetime                                  comment '更新时间',
  remark             varchar(255)    default null            comment '备注',
  primary key (warehouse_id),
  unique key uk_md_warehouse_code (warehouse_code)
) engine=innodb auto_increment=1 comment='仓库资料表';

-- ----------------------------
-- 25、库位资料表
-- ----------------------------
drop table if exists md_location;
create table md_location (
  location_id        bigint(20)      not null auto_increment comment '库位主键',
  warehouse_id       bigint(20)      not null                comment '仓库编号',
  location_code      varchar(64)     not null                comment '库位编码',
  location_name      varchar(128)    not null                comment '库位名称',
  status             char(1)         default '0'             comment '状态',
  del_flag           char(1)         default '0'             comment '删除标记',
  create_by          varchar(64)     default ''              comment '创建者',
  create_time        datetime                                  comment '创建时间',
  update_by          varchar(64)     default ''              comment '更新者',
  update_time        datetime                                  comment '更新时间',
  remark             varchar(255)    default null            comment '备注',
  primary key (location_id),
  unique key uk_md_location_code (location_code)
) engine=innodb auto_increment=1 comment='库位资料表';

-- ----------------------------
-- 26、销售退货主表
-- ----------------------------
drop table if exists wms_sale_return;
create table wms_sale_return (
  sale_return_id     bigint(20)      not null auto_increment comment '销售退货主键',
  return_no          varchar(32)     not null                comment '退货单号',
  return_type        varchar(20)     default null            comment '退货类型',
  customer_id        bigint(20)      not null                comment '客户编号',
  warehouse_id       bigint(20)      not null                comment '仓库编号',
  total_qty          decimal(18,2)   default 0               comment '总数量',
  total_amount       decimal(18,2)   default 0               comment '总金额',
  status             varchar(20)     default 'draft'         comment '状态',
  audit_by           varchar(64)     default null            comment '审核人',
  audit_time         datetime                                  comment '审核时间',
  create_by          varchar(64)     default ''              comment '创建者',
  create_time        datetime                                  comment '创建时间',
  update_by          varchar(64)     default ''              comment '更新者',
  update_time        datetime                                  comment '更新时间',
  remark             varchar(255)    default null            comment '备注',
  primary key (sale_return_id),
  unique key uk_wms_sale_return_no (return_no)
) engine=innodb auto_increment=1 comment='销售退货主表';

-- ----------------------------
-- 27、销售退货明细表
-- ----------------------------
drop table if exists wms_sale_return_item;
create table wms_sale_return_item (
  sale_return_item_id bigint(20)      not null auto_increment comment '销售退货明细主键',
  sale_return_id      bigint(20)      not null                comment '销售退货编号',
  product_id          bigint(20)      not null                comment '商品编号',
  quantity            decimal(18,2)   default 0               comment '数量',
  price               decimal(18,2)   default 0               comment '单价',
  amount              decimal(18,2)   default 0               comment '金额',
  primary key (sale_return_item_id),
  key idx_wms_sale_return_item_return_id (sale_return_id)
) engine=innodb auto_increment=1 comment='销售退货明细表';

-- ----------------------------
-- 28、库存盘点主表
-- ----------------------------
drop table if exists wms_inventory_check;
create table wms_inventory_check (
  check_id            bigint(20)      not null auto_increment comment '库存盘点主键',
  check_no            varchar(32)     not null                comment '盘点单号',
  warehouse_id        bigint(20)      not null                comment '仓库编号',
  status              varchar(20)     default 'draft'         comment '状态',
  total_diff_qty      decimal(18,2)   default 0               comment '差异数量',
  total_diff_amount   decimal(18,2)   default 0               comment '差异金额',
  audit_by            varchar(64)     default null            comment '审核人',
  audit_time          datetime                                  comment '审核时间',
  create_by           varchar(64)     default ''              comment '创建者',
  create_time         datetime                                  comment '创建时间',
  update_by           varchar(64)     default ''              comment '更新者',
  update_time         datetime                                  comment '更新时间',
  remark              varchar(255)    default null            comment '备注',
  primary key (check_id),
  unique key uk_wms_inventory_check_no (check_no)
) engine=innodb auto_increment=1 comment='库存盘点主表';

-- ----------------------------
-- 29、库存盘点明细表
-- ----------------------------
drop table if exists wms_inventory_check_item;
create table wms_inventory_check_item (
  check_item_id       bigint(20)      not null auto_increment comment '库存盘点明细主键',
  check_id            bigint(20)      not null                comment '盘点单编号',
  product_id          bigint(20)      not null                comment '商品编号',
  location_id         bigint(20)      default null            comment '库位编号',
  batch_no            varchar(64)     default null            comment '批次号',
  stock_qty           decimal(18,2)   default 0               comment '账面数量',
  actual_qty          decimal(18,2)   default 0               comment '实盘数量',
  diff_qty            decimal(18,2)   default 0               comment '差异数量',
  price               decimal(18,2)   default 0               comment '单价',
  diff_amount         decimal(18,2)   default 0               comment '差异金额',
  primary key (check_item_id),
  key idx_wms_inventory_check_item_check_id (check_id)
) engine=innodb auto_increment=1 comment='库存盘点明细表';

-- ----------------------------
-- 30、业务消息表
-- ----------------------------
drop table if exists biz_message;
create table biz_message (
  message_id          bigint(20)      not null auto_increment comment '消息主键',
  message_type        varchar(64)     not null                comment '消息类型',
  message_title       varchar(200)    not null                comment '消息标题',
  message_content     varchar(1000)   default ''              comment '消息内容',
  message_level       varchar(20)     default 'info'          comment '消息级别',
  business_type       varchar(64)     default null            comment '业务类型',
  business_id         bigint(20)      default null            comment '业务主键',
  status              char(1)         default '0'             comment '状态（0正常 1关闭）',
  create_by           varchar(64)     default ''              comment '创建者',
  create_time         datetime                                  comment '创建时间',
  update_by           varchar(64)     default ''              comment '更新者',
  update_time         datetime                                  comment '更新时间',
  remark              varchar(255)    default null            comment '备注',
  primary key (message_id),
  key idx_biz_message_type (message_type),
  key idx_biz_message_status (status)
) engine=innodb auto_increment=1 comment='业务消息表';

-- ----------------------------
-- 31、业务消息已读表
-- ----------------------------
drop table if exists biz_message_read;
create table biz_message_read (
  read_id             bigint(20)      not null auto_increment comment '已读主键',
  message_id          bigint(20)      not null                comment '消息主键',
  user_id             bigint(20)      not null                comment '用户主键',
  read_time           datetime                                  comment '已读时间',
  create_time         datetime                                  comment '创建时间',
  primary key (read_id),
  unique key uk_biz_message_read (message_id, user_id),
  key idx_biz_message_read_user (user_id)
) engine=innodb auto_increment=1 comment='业务消息已读表';

-- ----------------------------
-- 32、业务数据备份表
-- ----------------------------
drop table if exists biz_data_backup;
create table biz_data_backup (
  backup_id           bigint(20)      not null auto_increment comment '备份主键',
  backup_name         varchar(100)    not null                comment '备份名称',
  backup_type         varchar(20)     not null                comment '备份类型（manual/auto）',
  backup_status       varchar(20)     default 'success'       comment '备份状态（success/failed）',
  table_count         int(11)         default 0               comment '表数量',
  record_count        bigint(20)      default 0               comment '记录数量',
  backup_content      longtext                                 comment '备份内容',
  restore_status      varchar(20)     default null            comment '恢复状态（success/failed）',
  restore_by          varchar(64)     default ''              comment '恢复人',
  restore_time        datetime                                  comment '恢复时间',
  create_by           varchar(64)     default ''              comment '创建者',
  create_time         datetime                                  comment '创建时间',
  update_by           varchar(64)     default ''              comment '更新者',
  update_time         datetime                                  comment '更新时间',
  remark              varchar(255)    default null            comment '备注',
  primary key (backup_id),
  key idx_biz_data_backup_type (backup_type),
  key idx_biz_data_backup_status (backup_status)
) engine=innodb auto_increment=1 comment='业务数据备份表';

-- ----------------------------
-- 33、业务菜单与按钮（business全模块）
-- ----------------------------
insert into sys_menu values('3000', '业务管理', '0', '5', 'business', null, '', '', 1, 0, 'M', '0', '0', '', 'shopping', 'admin', sysdate(), '', null, '业务管理目录');

insert into sys_menu values('3001', '商品管理', '3000', '1', 'product', 'business/product/index', '', '', 1, 0, 'C', '0', '0', 'business:product:list', 'shopping', 'admin', sysdate(), '', null, '商品管理菜单');
insert into sys_menu values('3002', '客户管理', '3000', '2', 'customer', 'business/customer/index', '', '', 1, 0, 'C', '0', '0', 'business:customer:list', 'user', 'admin', sysdate(), '', null, '客户管理菜单');
insert into sys_menu values('3003', '仓库管理', '3000', '3', 'warehouse', 'business/warehouse/index', '', '', 1, 0, 'C', '0', '0', 'business:warehouse:list', 'international', 'admin', sysdate(), '', null, '仓库管理菜单');
insert into sys_menu values('3004', '库位管理', '3000', '4', 'location', 'business/location/index', '', '', 1, 0, 'C', '0', '0', 'business:location:list', 'tree', 'admin', sysdate(), '', null, '库位管理菜单');
insert into sys_menu values('3005', '采购入库', '3000', '5', 'inbound', 'business/inbound/index', '', '', 1, 0, 'C', '0', '0', 'business:inbound:list', 'guide', 'admin', sysdate(), '', null, '采购入库菜单');
insert into sys_menu values('3006', '入库明细', '3000', '6', 'inboundItem', 'business/inboundItem/index', '', '', 1, 0, 'C', '0', '0', 'business:inboundItem:list', 'list', 'admin', sysdate(), '', null, '入库明细菜单');
insert into sys_menu values('3007', '销售出库', '3000', '7', 'outbound', 'business/outbound/index', '', '', 1, 0, 'C', '0', '0', 'business:outbound:list', 'shopping-cart', 'admin', sysdate(), '', null, '销售出库菜单');
insert into sys_menu values('3008', '出库明细', '3000', '8', 'outboundItem', 'business/outboundItem/index', '', '', 1, 0, 'C', '0', '0', 'business:outboundItem:list', 'list', 'admin', sysdate(), '', null, '出库明细菜单');
insert into sys_menu values('3009', '库存台账', '3000', '9', 'stock', 'business/stock/index', '', '', 1, 0, 'C', '0', '0', 'business:stock:list', 'money', 'admin', sysdate(), '', null, '库存台账菜单');
insert into sys_menu values('3010', '库存流水', '3000', '10', 'stockLog', 'business/stockLog/index', '', '', 1, 0, 'C', '0', '0', 'business:stockLog:list', 'form', 'admin', sysdate(), '', null, '库存流水菜单');
insert into sys_menu values('3011', '采购退货', '3000', '11', 'purchaseReturn', 'business/purchaseReturn/index', '', '', 1, 0, 'C', '0', '0', 'business:purchaseReturn:list', 'refresh-left', 'admin', sysdate(), '', null, '采购退货菜单');
insert into sys_menu values('3012', '采购退货明细', '3000', '12', 'purchaseReturnItem', 'business/purchaseReturnItem/index', '', '', 1, 0, 'C', '0', '0', 'business:purchaseReturnItem:list', 'list', 'admin', sysdate(), '', null, '采购退货明细菜单');
insert into sys_menu values('3013', '销售订单', '3000', '13', 'saleOrder', 'business/saleOrder/index', '', '', 1, 0, 'C', '0', '0', 'business:saleOrder:list', 'tickets', 'admin', sysdate(), '', null, '销售订单菜单');
insert into sys_menu values('3014', '销售订单明细', '3000', '14', 'saleOrderItem', 'business/saleOrderItem/index', '', '', 1, 0, 'C', '0', '0', 'business:saleOrderItem:list', 'list', 'admin', sysdate(), '', null, '销售订单明细菜单');
insert into sys_menu values('3015', '销售退货', '3000', '15', 'saleReturn', 'business/saleReturn/index', '', '', 1, 0, 'C', '0', '0', 'business:saleReturn:list', 'money', 'admin', sysdate(), '', null, '销售退货菜单');
insert into sys_menu values('3016', '销售退货明细', '3000', '16', 'saleReturnItem', 'business/saleReturnItem/index', '', '', 1, 0, 'C', '0', '0', 'business:saleReturnItem:list', 'list', 'admin', sysdate(), '', null, '销售退货明细菜单');
insert into sys_menu values('3017', '应收台账', '3000', '17', 'receivable', 'business/receivable/index', '', '', 1, 0, 'C', '0', '0', 'business:receivable:list', 'postcard', 'admin', sysdate(), '', null, '应收台账菜单');
insert into sys_menu values('3018', '回款记录', '3000', '18', 'receipt', 'business/receipt/index', '', '', 1, 0, 'C', '0', '0', 'business:receipt:list', 'wallet', 'admin', sysdate(), '', null, '回款记录菜单');
insert into sys_menu values('3019', '经营看板', '3000', '19', 'report', 'business/report/index', '', '', 1, 0, 'C', '0', '0', 'business:report:view', 'dashboard', 'admin', sysdate(), '', null, '经营看板菜单');
insert into sys_menu values('3020', '库存盘点', '3000', '20', 'inventoryCheck', 'business/inventoryCheck/index', '', '', 1, 0, 'C', '0', '0', 'business:inventoryCheck:list', 'tickets', 'admin', sysdate(), '', null, '库存盘点菜单');
insert into sys_menu values('3021', '盘点明细', '3000', '21', 'inventoryCheckItem', 'business/inventoryCheckItem/index', '', '', 1, 0, 'C', '0', '0', 'business:inventoryCheckItem:list', 'list', 'admin', sysdate(), '', null, '盘点明细菜单');
insert into sys_menu values('3022', '数据备份', '3000', '22', 'dataBackup', 'business/dataBackup/index', '', '', 1, 0, 'C', '0', '0', 'business:dataBackup:list', 'set-up', 'admin', sysdate(), '', null, '数据备份菜单');

insert into sys_menu values('3101', '商品查询', '3001', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:product:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3102', '商品新增', '3001', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:product:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3103', '商品修改', '3001', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:product:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3104', '商品删除', '3001', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:product:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3105', '商品导出', '3001', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:product:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('3201', '客户查询', '3002', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:customer:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3202', '客户新增', '3002', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:customer:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3203', '客户修改', '3002', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:customer:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3204', '客户删除', '3002', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:customer:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3205', '客户导出', '3002', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:customer:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('3301', '仓库查询', '3003', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:warehouse:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3302', '仓库新增', '3003', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:warehouse:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3303', '仓库修改', '3003', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:warehouse:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3304', '仓库删除', '3003', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:warehouse:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3305', '仓库导出', '3003', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:warehouse:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('3401', '库位查询', '3004', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:location:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3402', '库位新增', '3004', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:location:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3403', '库位修改', '3004', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:location:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3404', '库位删除', '3004', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:location:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3405', '库位导出', '3004', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:location:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('3501', '入库查询', '3005', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inbound:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3502', '入库新增', '3005', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inbound:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3503', '入库修改', '3005', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inbound:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3504', '入库删除', '3005', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inbound:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3505', '入库导出', '3005', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inbound:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3506', '入库审核', '3005', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inbound:audit', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('3601', '入库明细查询', '3006', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inboundItem:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3602', '入库明细新增', '3006', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inboundItem:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3603', '入库明细修改', '3006', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inboundItem:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3604', '入库明细删除', '3006', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inboundItem:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3605', '入库明细导出', '3006', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inboundItem:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('3701', '出库查询', '3007', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:outbound:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3702', '出库新增', '3007', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:outbound:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3703', '出库修改', '3007', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:outbound:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3704', '出库删除', '3007', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:outbound:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3705', '出库导出', '3007', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:outbound:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3706', '出库审核', '3007', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:outbound:audit', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('3801', '出库明细查询', '3008', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:outboundItem:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3802', '出库明细新增', '3008', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:outboundItem:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3803', '出库明细修改', '3008', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:outboundItem:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3804', '出库明细删除', '3008', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:outboundItem:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3805', '出库明细导出', '3008', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:outboundItem:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('3901', '库存查询', '3009', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:stock:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3902', '库存新增', '3009', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:stock:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3903', '库存修改', '3009', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:stock:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3904', '库存删除', '3009', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:stock:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3905', '库存导出', '3009', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:stock:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('4001', '流水查询', '3010', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:stockLog:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4002', '流水导出', '3010', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:stockLog:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('4101', '采购退货查询', '3011', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:purchaseReturn:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4102', '采购退货新增', '3011', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:purchaseReturn:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4103', '采购退货修改', '3011', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:purchaseReturn:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4104', '采购退货删除', '3011', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:purchaseReturn:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4105', '采购退货导出', '3011', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:purchaseReturn:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4106', '采购退货审核', '3011', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:purchaseReturn:audit', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('4201', '采购退货明细查询', '3012', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:purchaseReturnItem:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4202', '采购退货明细新增', '3012', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:purchaseReturnItem:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4203', '采购退货明细修改', '3012', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:purchaseReturnItem:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4204', '采购退货明细删除', '3012', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:purchaseReturnItem:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4205', '采购退货明细导出', '3012', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:purchaseReturnItem:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('4301', '订单查询', '3013', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4302', '订单新增', '3013', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4303', '订单修改', '3013', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4304', '订单删除', '3013', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4305', '订单导出', '3013', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4306', '经理审核', '3013', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:managerAudit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4307', '订单导入', '3013', '7', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:import', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4308', '打印模板', '3013', '8', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:print', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4309', '财务审核', '3013', '9', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:financeAudit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4310', '订单提交', '3013', '10', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:submit', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('4401', '订单明细查询', '3014', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrderItem:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4402', '订单明细新增', '3014', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrderItem:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4403', '订单明细修改', '3014', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrderItem:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4404', '订单明细删除', '3014', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrderItem:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4405', '订单明细导出', '3014', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrderItem:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('4501', '退货查询', '3015', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleReturn:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4502', '退货新增', '3015', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleReturn:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4503', '退货修改', '3015', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleReturn:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4504', '退货删除', '3015', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleReturn:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4505', '退货导出', '3015', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleReturn:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4506', '退货审核', '3015', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleReturn:audit', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('4601', '退货明细查询', '3016', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleReturnItem:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4602', '退货明细新增', '3016', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleReturnItem:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4603', '退货明细修改', '3016', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleReturnItem:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4604', '退货明细删除', '3016', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleReturnItem:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4605', '退货明细导出', '3016', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleReturnItem:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('4701', '应收查询', '3017', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receivable:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4702', '应收新增', '3017', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receivable:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4703', '应收修改', '3017', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receivable:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4704', '应收删除', '3017', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receivable:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4705', '应收导出', '3017', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receivable:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4706', '到期提醒查询', '3017', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receivable:due', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4707', '逾期视图查询', '3017', '7', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receivable:overdue', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('4801', '回款查询', '3018', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receipt:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4802', '回款新增', '3018', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receipt:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4803', '回款修改', '3018', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receipt:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4804', '回款删除', '3018', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receipt:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4805', '回款导出', '3018', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receipt:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('4901', '看板查看', '3019', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:report:view', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('5001', '盘点查询', '3020', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inventoryCheck:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('5002', '盘点新增', '3020', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inventoryCheck:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('5003', '盘点修改', '3020', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inventoryCheck:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('5004', '盘点删除', '3020', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inventoryCheck:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('5005', '盘点导出', '3020', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inventoryCheck:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('5006', '盘点审核', '3020', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inventoryCheck:audit', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('5101', '盘点明细查询', '3021', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inventoryCheckItem:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('5102', '盘点明细新增', '3021', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inventoryCheckItem:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('5103', '盘点明细修改', '3021', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inventoryCheckItem:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('5104', '盘点明细删除', '3021', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inventoryCheckItem:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('5105', '盘点明细导出', '3021', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inventoryCheckItem:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('5201', '备份查询', '3022', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:dataBackup:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('5202', '手动备份', '3022', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:dataBackup:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('5203', '数据恢复', '3022', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:dataBackup:restore', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('5204', '记录删除', '3022', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:dataBackup:remove', '#', 'admin', sysdate(), '', null, '');

insert into sys_role_menu values ('3', '3000');
insert into sys_role_menu values ('3', '3013');
insert into sys_role_menu values ('3', '4301');
insert into sys_role_menu values ('3', '4302');
insert into sys_role_menu values ('3', '4303');
insert into sys_role_menu values ('3', '4304');
insert into sys_role_menu values ('3', '4305');
insert into sys_role_menu values ('3', '4307');
insert into sys_role_menu values ('3', '4308');
insert into sys_role_menu values ('3', '4310');

insert into sys_role_menu values ('4', '3000');
insert into sys_role_menu values ('4', '3013');
insert into sys_role_menu values ('4', '4301');
insert into sys_role_menu values ('4', '4306');

insert into sys_role_menu values ('5', '3000');
insert into sys_role_menu values ('5', '3013');
insert into sys_role_menu values ('5', '4301');
insert into sys_role_menu values ('5', '4309');
