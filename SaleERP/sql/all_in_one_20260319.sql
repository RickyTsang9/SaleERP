-- ============================================================
-- SaleERP å¨é SQLï¼åå¹¶çï¼
-- çææ¶é´: 2026-03-19
-- æ¥æº:
--   1) sql/ry_20250522.sql
--   2) sql/quartz.sql
--   3) sql/business_new_tables_and_sample_data.sql
-- ============================================================
-- ----------------------------
-- 1ãé¨é¨è¡¨
-- ----------------------------
drop table if exists sys_dept;
create table sys_dept (
  dept_id           bigint(20)      not null auto_increment    comment 'é¨é¨id',
  parent_id         bigint(20)      default 0                  comment 'ç¶é¨é¨id',
  ancestors         varchar(50)     default ''                 comment 'ç¥çº§åè¡¨',
  dept_name         varchar(30)     default ''                 comment 'é¨é¨åç§°',
  order_num         int(4)          default 0                  comment 'æ¾ç¤ºé¡ºåº',
  leader            varchar(20)     default null               comment 'è´è´£äºº',
  phone             varchar(11)     default null               comment 'èç³»çµè¯',
  email             varchar(50)     default null               comment 'é®ç®±',
  status            char(1)         default '0'                comment 'é¨é¨ç¶æï¼0æ­£å¸¸ 1åç¨ï¼',
  del_flag          char(1)         default '0'                comment 'å é¤æ å¿ï¼0ä»£è¡¨å­å¨ 2ä»£è¡¨å é¤ï¼',
  create_by         varchar(64)     default ''                 comment 'åå»ºè',
  create_time 	    datetime                                   comment 'åå»ºæ¶é´',
  update_by         varchar(64)     default ''                 comment 'æ´æ°è',
  update_time       datetime                                   comment 'æ´æ°æ¶é´',
  primary key (dept_id)
) engine=innodb auto_increment=200 comment = 'é¨é¨è¡¨';

-- ----------------------------
-- åå§å-é¨é¨è¡¨æ°æ®
-- ----------------------------
insert into sys_dept values(100,  0,   '0',          'SaleERP科技',   0, 'SaleERP管理员', '15888888888', 'admin@saleerp.local', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(101,  100, '0,100',      '深圳总公司',   1, 'SaleERP管理员', '15888888888', 'admin@saleerp.local', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(102,  100, '0,100',      '长沙分公司',   2, 'SaleERP管理员', '15888888888', 'admin@saleerp.local', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(103,  101, '0,100,101',  '研发部门',     1, 'SaleERP管理员', '15888888888', 'admin@saleerp.local', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(104,  101, '0,100,101',  '市场部门',     2, 'SaleERP管理员', '15888888888', 'admin@saleerp.local', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(105,  101, '0,100,101',  '测试部门',     3, 'SaleERP管理员', '15888888888', 'admin@saleerp.local', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(106,  101, '0,100,101',  '财务部门',     4, 'SaleERP管理员', '15888888888', 'admin@saleerp.local', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(107,  101, '0,100,101',  '运维部门',     5, 'SaleERP管理员', '15888888888', 'admin@saleerp.local', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(108,  102, '0,100,102',  '市场部门',     1, 'SaleERP管理员', '15888888888', 'admin@saleerp.local', '0', '0', 'admin', sysdate(), '', null);
insert into sys_dept values(109,  102, '0,100,102',  '财务部门',     2, 'SaleERP管理员', '15888888888', 'admin@saleerp.local', '0', '0', 'admin', sysdate(), '', null);


-- ----------------------------
-- 2ãç¨æ·ä¿¡æ¯è¡¨
-- ----------------------------
drop table if exists sys_user;
create table sys_user (
  user_id           bigint(20)      not null auto_increment    comment 'ç¨æ·ID',
  dept_id           bigint(20)      default null               comment 'é¨é¨ID',
  user_name         varchar(30)     not null                   comment 'ç¨æ·è´¦å·',
  nick_name         varchar(30)     not null                   comment 'ç¨æ·æµç§°',
  user_type         varchar(2)      default '00'               comment 'ç¨æ·ç±»åï¼00ç³»ç»ç¨æ·ï¼',
  email             varchar(50)     default ''                 comment 'ç¨æ·é®ç®±',
  phonenumber       varchar(11)     default ''                 comment 'ææºå·ç ',
  sex               char(1)         default '0'                comment 'ç¨æ·æ§å«ï¼0ç· 1å¥³ 2æªç¥ï¼',
  avatar            varchar(100)    default ''                 comment 'å¤´åå°å',
  password          varchar(100)    default ''                 comment 'å¯ç ',
  status            char(1)         default '0'                comment 'è´¦å·ç¶æï¼0æ­£å¸¸ 1åç¨ï¼',
  del_flag          char(1)         default '0'                comment 'å é¤æ å¿ï¼0ä»£è¡¨å­å¨ 2ä»£è¡¨å é¤ï¼',
  login_ip          varchar(128)    default ''                 comment 'æåç»å½IP',
  login_date        datetime                                   comment 'æåç»å½æ¶é´',
  pwd_update_date   datetime                                   comment 'å¯ç æåæ´æ°æ¶é´',
  create_by         varchar(64)     default ''                 comment 'åå»ºè',
  create_time       datetime                                   comment 'åå»ºæ¶é´',
  update_by         varchar(64)     default ''                 comment 'æ´æ°è',
  update_time       datetime                                   comment 'æ´æ°æ¶é´',
  remark            varchar(500)    default null               comment 'å¤æ³¨',
  primary key (user_id)
) engine=innodb auto_increment=100 comment = 'ç¨æ·ä¿¡æ¯è¡¨';

-- ----------------------------
-- åå§å-ç¨æ·ä¿¡æ¯è¡¨æ°æ®
-- ----------------------------
insert into sys_user values(1,  103, 'admin', 'SaleERP管理员', '00', 'admin@saleerp.local',  '15888888888', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), sysdate(), 'admin', sysdate(), '', null, '管理员');
insert into sys_user values(2,  105, 'ry',    '系统测试员',     '00', 'tester@saleerp.local', '15666666666', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', sysdate(), sysdate(), 'admin', sysdate(), '', null, '测试员');


-- ----------------------------
-- 3ãå²ä½ä¿¡æ¯è¡¨
-- ----------------------------
drop table if exists sys_post;
create table sys_post
(
  post_id       bigint(20)      not null auto_increment    comment 'å²ä½ID',
  post_code     varchar(64)     not null                   comment 'å²ä½ç¼ç ',
  post_name     varchar(50)     not null                   comment 'å²ä½åç§°',
  post_sort     int(4)          not null                   comment 'æ¾ç¤ºé¡ºåº',
  status        char(1)         not null                   comment 'ç¶æï¼0æ­£å¸¸ 1åç¨ï¼',
  create_by     varchar(64)     default ''                 comment 'åå»ºè',
  create_time   datetime                                   comment 'åå»ºæ¶é´',
  update_by     varchar(64)     default ''			       comment 'æ´æ°è',
  update_time   datetime                                   comment 'æ´æ°æ¶é´',
  remark        varchar(500)    default null               comment 'å¤æ³¨',
  primary key (post_id)
) engine=innodb comment = 'å²ä½ä¿¡æ¯è¡¨';

-- ----------------------------
-- åå§å-å²ä½ä¿¡æ¯è¡¨æ°æ®
-- ----------------------------
insert into sys_post values(1, 'ceo',  'è£äºé¿',    1, '0', 'admin', sysdate(), '', null, '');
insert into sys_post values(2, 'se',   'é¡¹ç®ç»ç',  2, '0', 'admin', sysdate(), '', null, '');
insert into sys_post values(3, 'hr',   'äººåèµæº',  3, '0', 'admin', sysdate(), '', null, '');
insert into sys_post values(4, 'user', 'æ®éåå·¥',  4, '0', 'admin', sysdate(), '', null, '');


-- ----------------------------
-- 4ãè§è²ä¿¡æ¯è¡¨
-- ----------------------------
drop table if exists sys_role;
create table sys_role (
  role_id              bigint(20)      not null auto_increment    comment 'è§è²ID',
  role_name            varchar(30)     not null                   comment 'è§è²åç§°',
  role_key             varchar(100)    not null                   comment 'è§è²æéå­ç¬¦ä¸²',
  role_sort            int(4)          not null                   comment 'æ¾ç¤ºé¡ºåº',
  data_scope           char(1)         default '1'                comment 'æ°æ®èå´ï¼1ï¼å¨é¨æ°æ®æé 2ï¼èªå®æ°æ®æé 3ï¼æ¬é¨é¨æ°æ®æé 4ï¼æ¬é¨é¨åä»¥ä¸æ°æ®æéï¼',
  menu_check_strictly  tinyint(1)      default 1                  comment 'èåæ éæ©é¡¹æ¯å¦å³èæ¾ç¤º',
  dept_check_strictly  tinyint(1)      default 1                  comment 'é¨é¨æ éæ©é¡¹æ¯å¦å³èæ¾ç¤º',
  status               char(1)         not null                   comment 'è§è²ç¶æï¼0æ­£å¸¸ 1åç¨ï¼',
  del_flag             char(1)         default '0'                comment 'å é¤æ å¿ï¼0ä»£è¡¨å­å¨ 2ä»£è¡¨å é¤ï¼',
  create_by            varchar(64)     default ''                 comment 'åå»ºè',
  create_time          datetime                                   comment 'åå»ºæ¶é´',
  update_by            varchar(64)     default ''                 comment 'æ´æ°è',
  update_time          datetime                                   comment 'æ´æ°æ¶é´',
  remark               varchar(500)    default null               comment 'å¤æ³¨',
  primary key (role_id)
) engine=innodb auto_increment=100 comment = 'è§è²ä¿¡æ¯è¡¨';

-- ----------------------------
-- åå§å-è§è²ä¿¡æ¯è¡¨æ°æ®
-- ----------------------------
insert into sys_role values('1', 'è¶çº§ç®¡çå',  'admin',  1, 1, 1, 1, '0', '0', 'admin', sysdate(), '', null, 'è¶çº§ç®¡çå');
insert into sys_role values('2', 'æ®éè§è²',    'common', 2, 2, 1, 1, '0', '0', 'admin', sysdate(), '', null, 'æ®éè§è²');
insert into sys_role values('3', 'éå®è§è²',    'sale',   3, 2, 1, 1, '0', '0', 'admin', sysdate(), '', null, 'éå®è§è²');
insert into sys_role values('4', 'ç»çè§è²',    'manager',4, 2, 1, 1, '0', '0', 'admin', sysdate(), '', null, 'ç»çè§è²');
insert into sys_role values('5', 'è´¢å¡è§è²',    'finance',5, 2, 1, 1, '0', '0', 'admin', sysdate(), '', null, 'è´¢å¡è§è²');


-- ----------------------------
-- 5ãèåæéè¡¨
-- ----------------------------
drop table if exists sys_menu;
create table sys_menu (
  menu_id           bigint(20)      not null auto_increment    comment 'èåID',
  menu_name         varchar(50)     not null                   comment 'èååç§°',
  parent_id         bigint(20)      default 0                  comment 'ç¶èåID',
  order_num         int(4)          default 0                  comment 'æ¾ç¤ºé¡ºåº',
  path              varchar(200)    default ''                 comment 'è·¯ç±å°å',
  component         varchar(255)    default null               comment 'ç»ä»¶è·¯å¾',
  query             varchar(255)    default null               comment 'è·¯ç±åæ°',
  route_name        varchar(50)     default ''                 comment 'è·¯ç±åç§°',
  is_frame          int(1)          default 1                  comment 'æ¯å¦ä¸ºå¤é¾ï¼0æ¯ 1å¦ï¼',
  is_cache          int(1)          default 0                  comment 'æ¯å¦ç¼å­ï¼0ç¼å­ 1ä¸ç¼å­ï¼',
  menu_type         char(1)         default ''                 comment 'èåç±»åï¼Mç®å½ Cèå Fæé®ï¼',
  visible           char(1)         default 0                  comment 'èåç¶æï¼0æ¾ç¤º 1éèï¼',
  status            char(1)         default 0                  comment 'èåç¶æï¼0æ­£å¸¸ 1åç¨ï¼',
  perms             varchar(100)    default null               comment 'æéæ è¯',
  icon              varchar(100)    default '#'                comment 'èåå¾æ ',
  create_by         varchar(64)     default ''                 comment 'åå»ºè',
  create_time       datetime                                   comment 'åå»ºæ¶é´',
  update_by         varchar(64)     default ''                 comment 'æ´æ°è',
  update_time       datetime                                   comment 'æ´æ°æ¶é´',
  remark            varchar(500)    default ''                 comment 'å¤æ³¨',
  primary key (menu_id)
) engine=innodb auto_increment=2000 comment = 'èåæéè¡¨';

-- ----------------------------
-- åå§å-èåä¿¡æ¯è¡¨æ°æ®
-- ----------------------------
-- ä¸çº§èå
insert into sys_menu values('1', 'ç³»ç»ç®¡ç', '0', '1', 'system',           null, '', '', 1, 0, 'M', '0', '0', '', 'system',   'admin', sysdate(), '', null, 'ç³»ç»ç®¡çç®å½');
insert into sys_menu values('2', 'ç³»ç»çæ§', '1', '10', 'monitor',          null, '', '', 1, 0, 'M', '0', '0', '', 'monitor',  'admin', sysdate(), '', null, 'ç³»ç»çæ§ç®å½');
insert into sys_menu values('3', 'ç³»ç»å·¥å·', '1', '11', 'tool',             null, '', '', 1, 0, 'M', '0', '0', '', 'tool',     'admin', sysdate(), '', null, 'ç³»ç»å·¥å·ç®å½');
-- äºçº§èå
insert into sys_menu values('100',  'ç¨æ·ç®¡ç', '1',   '1', 'user',       'system/user/index',        '', '', 1, 0, 'C', '0', '0', 'system:user:list',        'user',          'admin', sysdate(), '', null, 'ç¨æ·ç®¡çèå');
insert into sys_menu values('101',  'è§è²ç®¡ç', '1',   '2', 'role',       'system/role/index',        '', '', 1, 0, 'C', '0', '0', 'system:role:list',        'peoples',       'admin', sysdate(), '', null, 'è§è²ç®¡çèå');
insert into sys_menu values('102',  'èåç®¡ç', '1',   '3', 'menu',       'system/menu/index',        '', '', 1, 0, 'C', '0', '0', 'system:menu:list',        'tree-table',    'admin', sysdate(), '', null, 'èåç®¡çèå');
insert into sys_menu values('103',  'é¨é¨ç®¡ç', '1',   '4', 'dept',       'system/dept/index',        '', '', 1, 0, 'C', '0', '0', 'system:dept:list',        'tree',          'admin', sysdate(), '', null, 'é¨é¨ç®¡çèå');
insert into sys_menu values('104',  'å²ä½ç®¡ç', '1',   '5', 'post',       'system/post/index',        '', '', 1, 0, 'C', '0', '0', 'system:post:list',        'post',          'admin', sysdate(), '', null, 'å²ä½ç®¡çèå');
insert into sys_menu values('105',  'å­å¸ç®¡ç', '1',   '6', 'dict',       'system/dict/index',        '', '', 1, 0, 'C', '0', '0', 'system:dict:list',        'dict',          'admin', sysdate(), '', null, 'å­å¸ç®¡çèå');
insert into sys_menu values('106',  'åæ°è®¾ç½®', '1',   '7', 'config',     'system/config/index',      '', '', 1, 0, 'C', '0', '0', 'system:config:list',      'edit',          'admin', sysdate(), '', null, 'åæ°è®¾ç½®èå');
insert into sys_menu values('107',  'éç¥å¬å', '1',   '8', 'notice',     'system/notice/index',      '', '', 1, 0, 'C', '0', '0', 'system:notice:list',      'message',       'admin', sysdate(), '', null, 'éç¥å¬åèå');
insert into sys_menu values('108',  'æ¥å¿ç®¡ç', '1',   '9', 'log',        '',                         '', '', 1, 0, 'M', '0', '0', '',                        'log',           'admin', sysdate(), '', null, 'æ¥å¿ç®¡çèå');
insert into sys_menu values('109',  'å¨çº¿ç¨æ·', '2',   '1', 'online',     'monitor/online/index',     '', '', 1, 0, 'C', '0', '0', 'monitor:online:list',     'online',        'admin', sysdate(), '', null, 'å¨çº¿ç¨æ·èå');
insert into sys_menu values('110',  'å®æ¶ä»»å¡', '2',   '2', 'job',        'monitor/job/index',        '', '', 1, 0, 'C', '0', '0', 'monitor:job:list',        'job',           'admin', sysdate(), '', null, 'å®æ¶ä»»å¡èå');
insert into sys_menu values('111',  'æ°æ®çæ§', '2',   '3', 'druid',      'monitor/druid/index',      '', '', 1, 0, 'C', '0', '0', 'monitor:druid:list',      'druid',         'admin', sysdate(), '', null, 'æ°æ®çæ§èå');
insert into sys_menu values('112',  'æå¡çæ§', '2',   '4', 'server',     'monitor/server/index',     '', '', 1, 0, 'C', '0', '0', 'monitor:server:list',     'server',        'admin', sysdate(), '', null, 'æå¡çæ§èå');
insert into sys_menu values('113',  'ç¼å­çæ§', '2',   '5', 'cache',      'monitor/cache/index',      '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list',      'redis',         'admin', sysdate(), '', null, 'ç¼å­çæ§èå');
insert into sys_menu values('114',  'ç¼å­åè¡¨', '2',   '6', 'cacheList',  'monitor/cache/list',       '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list',      'redis-list',    'admin', sysdate(), '', null, 'ç¼å­åè¡¨èå');
insert into sys_menu values('115',  'è¡¨åæå»º', '3',   '1', 'build',      'tool/build/index',         '', '', 1, 0, 'C', '0', '0', 'tool:build:list',         'build',         'admin', sysdate(), '', null, 'è¡¨åæå»ºèå');
insert into sys_menu values('116',  'ä»£ç çæ', '3',   '2', 'gen',        'tool/gen/index',           '', '', 1, 0, 'C', '0', '0', 'tool:gen:list',           'code',          'admin', sysdate(), '', null, 'ä»£ç çæèå');
insert into sys_menu values('117',  'ç³»ç»æ¥å£', '3',   '3', 'swagger',    'tool/swagger/index',       '', '', 1, 0, 'C', '0', '0', 'tool:swagger:list',       'swagger',       'admin', sysdate(), '', null, 'ç³»ç»æ¥å£èå');
-- ä¸çº§èå
insert into sys_menu values('500',  'æä½æ¥å¿', '108', '1', 'operlog',    'monitor/operlog/index',    '', '', 1, 0, 'C', '0', '0', 'monitor:operlog:list',    'form',          'admin', sysdate(), '', null, 'æä½æ¥å¿èå');
insert into sys_menu values('501',  'ç»å½æ¥å¿', '108', '2', 'logininfor', 'monitor/logininfor/index', '', '', 1, 0, 'C', '0', '0', 'monitor:logininfor:list', 'logininfor',    'admin', sysdate(), '', null, 'ç»å½æ¥å¿èå');
-- ç¨æ·ç®¡çæé®
insert into sys_menu values('1000', 'ç¨æ·æ¥è¯¢', '100', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1001', 'ç¨æ·æ°å¢', '100', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1002', 'ç¨æ·ä¿®æ¹', '100', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1003', 'ç¨æ·å é¤', '100', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1004', 'ç¨æ·å¯¼åº', '100', '5',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:export',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1005', 'ç¨æ·å¯¼å¥', '100', '6',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:import',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1006', 'éç½®å¯ç ', '100', '7',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd',       '#', 'admin', sysdate(), '', null, '');
-- è§è²ç®¡çæé®
insert into sys_menu values('1007', 'è§è²æ¥è¯¢', '101', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1008', 'è§è²æ°å¢', '101', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1009', 'è§è²ä¿®æ¹', '101', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1010', 'è§è²å é¤', '101', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1011', 'è§è²å¯¼åº', '101', '5',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:export',         '#', 'admin', sysdate(), '', null, '');
-- èåç®¡çæé®
insert into sys_menu values('1012', 'èåæ¥è¯¢', '102', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1013', 'èåæ°å¢', '102', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1014', 'èåä¿®æ¹', '102', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1015', 'èåå é¤', '102', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove',         '#', 'admin', sysdate(), '', null, '');
-- é¨é¨ç®¡çæé®
insert into sys_menu values('1016', 'é¨é¨æ¥è¯¢', '103', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1017', 'é¨é¨æ°å¢', '103', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1018', 'é¨é¨ä¿®æ¹', '103', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1019', 'é¨é¨å é¤', '103', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove',         '#', 'admin', sysdate(), '', null, '');
-- å²ä½ç®¡çæé®
insert into sys_menu values('1020', 'å²ä½æ¥è¯¢', '104', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1021', 'å²ä½æ°å¢', '104', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1022', 'å²ä½ä¿®æ¹', '104', '3',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1023', 'å²ä½å é¤', '104', '4',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1024', 'å²ä½å¯¼åº', '104', '5',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:export',         '#', 'admin', sysdate(), '', null, '');
-- å­å¸ç®¡çæé®
insert into sys_menu values('1025', 'å­å¸æ¥è¯¢', '105', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1026', 'å­å¸æ°å¢', '105', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1027', 'å­å¸ä¿®æ¹', '105', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1028', 'å­å¸å é¤', '105', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1029', 'å­å¸å¯¼åº', '105', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:export',         '#', 'admin', sysdate(), '', null, '');
-- åæ°è®¾ç½®æé®
insert into sys_menu values('1030', 'åæ°æ¥è¯¢', '106', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:query',        '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1031', 'åæ°æ°å¢', '106', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:add',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1032', 'åæ°ä¿®æ¹', '106', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:edit',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1033', 'åæ°å é¤', '106', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:remove',       '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1034', 'åæ°å¯¼åº', '106', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:export',       '#', 'admin', sysdate(), '', null, '');
-- éç¥å¬åæé®
insert into sys_menu values('1035', 'å¬åæ¥è¯¢', '107', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:query',        '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1036', 'å¬åæ°å¢', '107', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:add',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1037', 'å¬åä¿®æ¹', '107', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:edit',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1038', 'å¬åå é¤', '107', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:remove',       '#', 'admin', sysdate(), '', null, '');
-- æä½æ¥å¿æé®
insert into sys_menu values('1039', 'æä½æ¥è¯¢', '500', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query',      '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1040', 'æä½å é¤', '500', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove',     '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1041', 'æ¥å¿å¯¼åº', '500', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export',     '#', 'admin', sysdate(), '', null, '');
-- ç»å½æ¥å¿æé®
insert into sys_menu values('1042', 'ç»å½æ¥è¯¢', '501', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1043', 'ç»å½å é¤', '501', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1044', 'æ¥å¿å¯¼åº', '501', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1045', 'è´¦æ·è§£é', '501', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:unlock',  '#', 'admin', sysdate(), '', null, '');
-- å¨çº¿ç¨æ·æé®
insert into sys_menu values('1046', 'å¨çº¿æ¥è¯¢', '109', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:query',       '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1047', 'æ¹éå¼ºé', '109', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1048', 'åæ¡å¼ºé', '109', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin', sysdate(), '', null, '');
-- å®æ¶ä»»å¡æé®
insert into sys_menu values('1049', 'ä»»å¡æ¥è¯¢', '110', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:query',          '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1050', 'ä»»å¡æ°å¢', '110', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:add',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1051', 'ä»»å¡ä¿®æ¹', '110', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:edit',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1052', 'ä»»å¡å é¤', '110', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:remove',         '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1053', 'ç¶æä¿®æ¹', '110', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:changeStatus',   '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1054', 'ä»»å¡å¯¼åº', '110', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:export',         '#', 'admin', sysdate(), '', null, '');
-- ä»£ç çææé®
insert into sys_menu values('1055', 'çææ¥è¯¢', '116', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:query',             '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1056', 'çæä¿®æ¹', '116', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:edit',              '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1057', 'çæå é¤', '116', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:remove',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1058', 'å¯¼å¥ä»£ç ', '116', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:import',            '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1059', 'é¢è§ä»£ç ', '116', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:preview',           '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('1060', 'çæä»£ç ', '116', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:code',              '#', 'admin', sysdate(), '', null, '');


-- ----------------------------
-- 6ãç¨æ·åè§è²å³èè¡¨  ç¨æ·N-1è§è²
-- ----------------------------
drop table if exists sys_user_role;
create table sys_user_role (
  user_id   bigint(20) not null comment 'ç¨æ·ID',
  role_id   bigint(20) not null comment 'è§è²ID',
  primary key(user_id, role_id)
) engine=innodb comment = 'ç¨æ·åè§è²å³èè¡¨';

-- ----------------------------
-- åå§å-ç¨æ·åè§è²å³èè¡¨æ°æ®
-- ----------------------------
insert into sys_user_role values ('1', '1');
insert into sys_user_role values ('2', '2');


-- ----------------------------
-- 7ãè§è²åèåå³èè¡¨  è§è²1-Nèå
-- ----------------------------
drop table if exists sys_role_menu;
create table sys_role_menu (
  role_id   bigint(20) not null comment 'è§è²ID',
  menu_id   bigint(20) not null comment 'èåID',
  primary key(role_id, menu_id)
) engine=innodb comment = 'è§è²åèåå³èè¡¨';

-- ----------------------------
-- åå§å-è§è²åèåå³èè¡¨æ°æ®
-- ----------------------------
insert into sys_role_menu values ('2', '1');
insert into sys_role_menu values ('2', '2');
insert into sys_role_menu values ('2', '3');
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
-- 8ãè§è²åé¨é¨å³èè¡¨  è§è²1-Né¨é¨
-- ----------------------------
drop table if exists sys_role_dept;
create table sys_role_dept (
  role_id   bigint(20) not null comment 'è§è²ID',
  dept_id   bigint(20) not null comment 'é¨é¨ID',
  primary key(role_id, dept_id)
) engine=innodb comment = 'è§è²åé¨é¨å³èè¡¨';

-- ----------------------------
-- åå§å-è§è²åé¨é¨å³èè¡¨æ°æ®
-- ----------------------------
insert into sys_role_dept values ('2', '100');
insert into sys_role_dept values ('2', '101');
insert into sys_role_dept values ('2', '105');


-- ----------------------------
-- 9ãç¨æ·ä¸å²ä½å³èè¡¨  ç¨æ·1-Nå²ä½
-- ----------------------------
drop table if exists sys_user_post;
create table sys_user_post
(
  user_id   bigint(20) not null comment 'ç¨æ·ID',
  post_id   bigint(20) not null comment 'å²ä½ID',
  primary key (user_id, post_id)
) engine=innodb comment = 'ç¨æ·ä¸å²ä½å³èè¡¨';

-- ----------------------------
-- åå§å-ç¨æ·ä¸å²ä½å³èè¡¨æ°æ®
-- ----------------------------
insert into sys_user_post values ('1', '1');
insert into sys_user_post values ('2', '2');


-- ----------------------------
-- 10ãæä½æ¥å¿è®°å½
-- ----------------------------
drop table if exists sys_oper_log;
create table sys_oper_log (
  oper_id           bigint(20)      not null auto_increment    comment 'æ¥å¿ä¸»é®',
  title             varchar(50)     default ''                 comment 'æ¨¡åæ é¢',
  business_type     int(2)          default 0                  comment 'ä¸å¡ç±»åï¼0å¶å® 1æ°å¢ 2ä¿®æ¹ 3å é¤ï¼',
  method            varchar(200)    default ''                 comment 'æ¹æ³åç§°',
  request_method    varchar(10)     default ''                 comment 'è¯·æ±æ¹å¼',
  operator_type     int(1)          default 0                  comment 'æä½ç±»å«ï¼0å¶å® 1åå°ç¨æ· 2ææºç«¯ç¨æ·ï¼',
  oper_name         varchar(50)     default ''                 comment 'æä½äººå',
  dept_name         varchar(50)     default ''                 comment 'é¨é¨åç§°',
  oper_url          varchar(255)    default ''                 comment 'è¯·æ±URL',
  oper_ip           varchar(128)    default ''                 comment 'ä¸»æºå°å',
  oper_location     varchar(255)    default ''                 comment 'æä½å°ç¹',
  oper_param        varchar(2000)   default ''                 comment 'è¯·æ±åæ°',
  json_result       varchar(2000)   default ''                 comment 'è¿ååæ°',
  status            int(1)          default 0                  comment 'æä½ç¶æï¼0æ­£å¸¸ 1å¼å¸¸ï¼',
  error_msg         varchar(2000)   default ''                 comment 'éè¯¯æ¶æ¯',
  oper_time         datetime                                   comment 'æä½æ¶é´',
  cost_time         bigint(20)      default 0                  comment 'æ¶èæ¶é´',
  primary key (oper_id),
  key idx_sys_oper_log_bt (business_type),
  key idx_sys_oper_log_s  (status),
  key idx_sys_oper_log_ot (oper_time)
) engine=innodb auto_increment=100 comment = 'æä½æ¥å¿è®°å½';


-- ----------------------------
-- 11ãå­å¸ç±»åè¡¨
-- ----------------------------
drop table if exists sys_dict_type;
create table sys_dict_type
(
  dict_id          bigint(20)      not null auto_increment    comment 'å­å¸ä¸»é®',
  dict_name        varchar(100)    default ''                 comment 'å­å¸åç§°',
  dict_type        varchar(100)    default ''                 comment 'å­å¸ç±»å',
  status           char(1)         default '0'                comment 'ç¶æï¼0æ­£å¸¸ 1åç¨ï¼',
  create_by        varchar(64)     default ''                 comment 'åå»ºè',
  create_time      datetime                                   comment 'åå»ºæ¶é´',
  update_by        varchar(64)     default ''                 comment 'æ´æ°è',
  update_time      datetime                                   comment 'æ´æ°æ¶é´',
  remark           varchar(500)    default null               comment 'å¤æ³¨',
  primary key (dict_id),
  unique (dict_type)
) engine=innodb auto_increment=100 comment = 'å­å¸ç±»åè¡¨';

insert into sys_dict_type values(1,  'ç¨æ·æ§å«', 'sys_user_sex',        '0', 'admin', sysdate(), '', null, 'ç¨æ·æ§å«åè¡¨');
insert into sys_dict_type values(2,  'èåç¶æ', 'sys_show_hide',       '0', 'admin', sysdate(), '', null, 'èåç¶æåè¡¨');
insert into sys_dict_type values(3,  'ç³»ç»å¼å³', 'sys_normal_disable',  '0', 'admin', sysdate(), '', null, 'ç³»ç»å¼å³åè¡¨');
insert into sys_dict_type values(4,  'ä»»å¡ç¶æ', 'sys_job_status',      '0', 'admin', sysdate(), '', null, 'ä»»å¡ç¶æåè¡¨');
insert into sys_dict_type values(5,  'ä»»å¡åç»', 'sys_job_group',       '0', 'admin', sysdate(), '', null, 'ä»»å¡åç»åè¡¨');
insert into sys_dict_type values(6,  'ç³»ç»æ¯å¦', 'sys_yes_no',          '0', 'admin', sysdate(), '', null, 'ç³»ç»æ¯å¦åè¡¨');
insert into sys_dict_type values(7,  'éç¥ç±»å', 'sys_notice_type',     '0', 'admin', sysdate(), '', null, 'éç¥ç±»ååè¡¨');
insert into sys_dict_type values(8,  'éç¥ç¶æ', 'sys_notice_status',   '0', 'admin', sysdate(), '', null, 'éç¥ç¶æåè¡¨');
insert into sys_dict_type values(9,  'æä½ç±»å', 'sys_oper_type',       '0', 'admin', sysdate(), '', null, 'æä½ç±»ååè¡¨');
insert into sys_dict_type values(10, 'ç³»ç»ç¶æ', 'sys_common_status',   '0', 'admin', sysdate(), '', null, 'ç»å½ç¶æåè¡¨');


-- ----------------------------
-- 12ãå­å¸æ°æ®è¡¨
-- ----------------------------
drop table if exists sys_dict_data;
create table sys_dict_data
(
  dict_code        bigint(20)      not null auto_increment    comment 'å­å¸ç¼ç ',
  dict_sort        int(4)          default 0                  comment 'å­å¸æåº',
  dict_label       varchar(100)    default ''                 comment 'å­å¸æ ç­¾',
  dict_value       varchar(100)    default ''                 comment 'å­å¸é®å¼',
  dict_type        varchar(100)    default ''                 comment 'å­å¸ç±»å',
  css_class        varchar(100)    default null               comment 'æ ·å¼å±æ§ï¼å¶ä»æ ·å¼æ©å±ï¼',
  list_class       varchar(100)    default null               comment 'è¡¨æ ¼åæ¾æ ·å¼',
  is_default       char(1)         default 'N'                comment 'æ¯å¦é»è®¤ï¼Yæ¯ Nå¦ï¼',
  status           char(1)         default '0'                comment 'ç¶æï¼0æ­£å¸¸ 1åç¨ï¼',
  create_by        varchar(64)     default ''                 comment 'åå»ºè',
  create_time      datetime                                   comment 'åå»ºæ¶é´',
  update_by        varchar(64)     default ''                 comment 'æ´æ°è',
  update_time      datetime                                   comment 'æ´æ°æ¶é´',
  remark           varchar(500)    default null               comment 'å¤æ³¨',
  primary key (dict_code)
) engine=innodb auto_increment=100 comment = 'å­å¸æ°æ®è¡¨';

insert into sys_dict_data values(1,  1,  'ç·',       '0',       'sys_user_sex',        '',   '',        'Y', '0', 'admin', sysdate(), '', null, 'æ§å«ç·');
insert into sys_dict_data values(2,  2,  'å¥³',       '1',       'sys_user_sex',        '',   '',        'N', '0', 'admin', sysdate(), '', null, 'æ§å«å¥³');
insert into sys_dict_data values(3,  3,  'æªç¥',     '2',       'sys_user_sex',        '',   '',        'N', '0', 'admin', sysdate(), '', null, 'æ§å«æªç¥');
insert into sys_dict_data values(4,  1,  'æ¾ç¤º',     '0',       'sys_show_hide',       '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, 'æ¾ç¤ºèå');
insert into sys_dict_data values(5,  2,  'éè',     '1',       'sys_show_hide',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, 'éèèå');
insert into sys_dict_data values(6,  1,  'æ­£å¸¸',     '0',       'sys_normal_disable',  '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, 'æ­£å¸¸ç¶æ');
insert into sys_dict_data values(7,  2,  'åç¨',     '1',       'sys_normal_disable',  '',   'danger',  'N', '0', 'admin', sysdate(), '', null, 'åç¨ç¶æ');
insert into sys_dict_data values(8,  1,  'æ­£å¸¸',     '0',       'sys_job_status',      '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, 'æ­£å¸¸ç¶æ');
insert into sys_dict_data values(9,  2,  'æå',     '1',       'sys_job_status',      '',   'danger',  'N', '0', 'admin', sysdate(), '', null, 'åç¨ç¶æ');
insert into sys_dict_data values(10, 1,  'é»è®¤',     'DEFAULT', 'sys_job_group',       '',   '',        'Y', '0', 'admin', sysdate(), '', null, 'é»è®¤åç»');
insert into sys_dict_data values(11, 2,  'ç³»ç»',     'SYSTEM',  'sys_job_group',       '',   '',        'N', '0', 'admin', sysdate(), '', null, 'ç³»ç»åç»');
insert into sys_dict_data values(12, 1,  'æ¯',       'Y',       'sys_yes_no',          '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, 'ç³»ç»é»è®¤æ¯');
insert into sys_dict_data values(13, 2,  'å¦',       'N',       'sys_yes_no',          '',   'danger',  'N', '0', 'admin', sysdate(), '', null, 'ç³»ç»é»è®¤å¦');
insert into sys_dict_data values(14, 1,  'éç¥',     '1',       'sys_notice_type',     '',   'warning', 'Y', '0', 'admin', sysdate(), '', null, 'éç¥');
insert into sys_dict_data values(15, 2,  'å¬å',     '2',       'sys_notice_type',     '',   'success', 'N', '0', 'admin', sysdate(), '', null, 'å¬å');
insert into sys_dict_data values(16, 1,  'æ­£å¸¸',     '0',       'sys_notice_status',   '',   'primary', 'Y', '0', 'admin', sysdate(), '', null, 'æ­£å¸¸ç¶æ');
insert into sys_dict_data values(17, 2,  'å³é­',     '1',       'sys_notice_status',   '',   'danger',  'N', '0', 'admin', sysdate(), '', null, 'å³é­ç¶æ');
insert into sys_dict_data values(18, 99, 'å¶ä»',     '0',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, 'å¶ä»æä½');
insert into sys_dict_data values(19, 1,  'æ°å¢',     '1',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, 'æ°å¢æä½');
insert into sys_dict_data values(20, 2,  'ä¿®æ¹',     '2',       'sys_oper_type',       '',   'info',    'N', '0', 'admin', sysdate(), '', null, 'ä¿®æ¹æä½');
insert into sys_dict_data values(21, 3,  'å é¤',     '3',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, 'å é¤æä½');
insert into sys_dict_data values(22, 4,  'ææ',     '4',       'sys_oper_type',       '',   'primary', 'N', '0', 'admin', sysdate(), '', null, 'æææä½');
insert into sys_dict_data values(23, 5,  'å¯¼åº',     '5',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, 'å¯¼åºæä½');
insert into sys_dict_data values(24, 6,  'å¯¼å¥',     '6',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, 'å¯¼å¥æä½');
insert into sys_dict_data values(25, 7,  'å¼ºé',     '7',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, 'å¼ºéæä½');
insert into sys_dict_data values(26, 8,  'çæä»£ç ', '8',       'sys_oper_type',       '',   'warning', 'N', '0', 'admin', sysdate(), '', null, 'çææä½');
insert into sys_dict_data values(27, 9,  'æ¸ç©ºæ°æ®', '9',       'sys_oper_type',       '',   'danger',  'N', '0', 'admin', sysdate(), '', null, 'æ¸ç©ºæä½');
insert into sys_dict_data values(28, 1,  'æå',     '0',       'sys_common_status',   '',   'primary', 'N', '0', 'admin', sysdate(), '', null, 'æ­£å¸¸ç¶æ');
insert into sys_dict_data values(29, 2,  'å¤±è´¥',     '1',       'sys_common_status',   '',   'danger',  'N', '0', 'admin', sysdate(), '', null, 'åç¨ç¶æ');


-- ----------------------------
-- 13ãåæ°éç½®è¡¨
-- ----------------------------
drop table if exists sys_config;
create table sys_config (
  config_id         int(5)          not null auto_increment    comment 'åæ°ä¸»é®',
  config_name       varchar(100)    default ''                 comment 'åæ°åç§°',
  config_key        varchar(100)    default ''                 comment 'åæ°é®å',
  config_value      varchar(500)    default ''                 comment 'åæ°é®å¼',
  config_type       char(1)         default 'N'                comment 'ç³»ç»åç½®ï¼Yæ¯ Nå¦ï¼',
  create_by         varchar(64)     default ''                 comment 'åå»ºè',
  create_time       datetime                                   comment 'åå»ºæ¶é´',
  update_by         varchar(64)     default ''                 comment 'æ´æ°è',
  update_time       datetime                                   comment 'æ´æ°æ¶é´',
  remark            varchar(500)    default null               comment 'å¤æ³¨',
  primary key (config_id)
) engine=innodb auto_increment=100 comment = 'åæ°éç½®è¡¨';

insert into sys_config values(1, 'ä¸»æ¡æ¶é¡µ-é»è®¤ç®è¤æ ·å¼åç§°',     'sys.index.skinName',               'skin-blue',     'Y', 'admin', sysdate(), '', null, 'èè² skin-blueãç»¿è² skin-greenãç´«è² skin-purpleãçº¢è² skin-redãé»è² skin-yellow' );
insert into sys_config values(2, 'ç¨æ·ç®¡ç-è´¦å·åå§å¯ç ',         'sys.user.initPassword',            '123456',        'Y', 'admin', sysdate(), '', null, 'åå§åå¯ç  123456' );
insert into sys_config values(3, 'ä¸»æ¡æ¶é¡µ-ä¾§è¾¹æ ä¸»é¢',           'sys.index.sideTheme',              'theme-dark',    'Y', 'admin', sysdate(), '', null, 'æ·±è²ä¸»é¢theme-darkï¼æµè²ä¸»é¢theme-light' );
insert into sys_config values(4, 'è´¦å·èªå©-éªè¯ç å¼å³',           'sys.account.captchaEnabled',       'true',          'Y', 'admin', sysdate(), '', null, 'æ¯å¦å¼å¯éªè¯ç åè½ï¼trueå¼å¯ï¼falseå³é­ï¼');
insert into sys_config values(5, 'è´¦å·èªå©-æ¯å¦å¼å¯ç¨æ·æ³¨ååè½', 'sys.account.registerUser',         'false',         'Y', 'admin', sysdate(), '', null, 'æ¯å¦å¼å¯æ³¨åç¨æ·åè½ï¼trueå¼å¯ï¼falseå³é­ï¼');
insert into sys_config values(6, 'ç¨æ·ç»å½-é»åååè¡¨',           'sys.login.blackIPList',            '',              'Y', 'admin', sysdate(), '', null, 'è®¾ç½®ç»å½IPé»ååéå¶ï¼å¤ä¸ªå¹éé¡¹ä»¥;åéï¼æ¯æå¹éï¼*ééãç½æ®µï¼');
insert into sys_config values(7, 'ç¨æ·ç®¡ç-åå§å¯ç ä¿®æ¹ç­ç¥',     'sys.account.initPasswordModify',   '1',             'Y', 'admin', sysdate(), '', null, '0ï¼åå§å¯ç ä¿®æ¹ç­ç¥å³é­ï¼æ²¡æä»»ä½æç¤ºï¼1ï¼æéç¨æ·ï¼å¦ææªä¿®æ¹åå§å¯ç ï¼åå¨ç»å½æ¶å°±ä¼æéä¿®æ¹å¯ç å¯¹è¯æ¡');
insert into sys_config values(8, 'ç¨æ·ç®¡ç-è´¦å·å¯ç æ´æ°å¨æ',     'sys.account.passwordValidateDays', '0',             'Y', 'admin', sysdate(), '', null, 'å¯ç æ´æ°å¨æï¼å¡«åæ°å­ï¼æ°æ®åå§åå¼ä¸º0ä¸éå¶ï¼è¥ä¿®æ¹å¿é¡»ä¸ºå¤§äº0å°äº365çæ­£æ´æ°ï¼ï¼å¦æè¶è¿è¿ä¸ªå¨æç»å½ç³»ç»æ¶ï¼åå¨ç»å½æ¶å°±ä¼æéä¿®æ¹å¯ç å¯¹è¯æ¡');


-- ----------------------------
-- 14ãç³»ç»è®¿é®è®°å½
-- ----------------------------
drop table if exists sys_logininfor;
create table sys_logininfor (
  info_id        bigint(20)     not null auto_increment   comment 'è®¿é®ID',
  user_name      varchar(50)    default ''                comment 'ç¨æ·è´¦å·',
  ipaddr         varchar(128)   default ''                comment 'ç»å½IPå°å',
  login_location varchar(255)   default ''                comment 'ç»å½å°ç¹',
  browser        varchar(50)    default ''                comment 'æµè§å¨ç±»å',
  os             varchar(50)    default ''                comment 'æä½ç³»ç»',
  status         char(1)        default '0'               comment 'ç»å½ç¶æï¼0æå 1å¤±è´¥ï¼',
  msg            varchar(255)   default ''                comment 'æç¤ºæ¶æ¯',
  login_time     datetime                                 comment 'è®¿é®æ¶é´',
  primary key (info_id),
  key idx_sys_logininfor_s  (status),
  key idx_sys_logininfor_lt (login_time)
) engine=innodb auto_increment=100 comment = 'ç³»ç»è®¿é®è®°å½';


-- ----------------------------
-- 15ãå®æ¶ä»»å¡è°åº¦è¡¨
-- ----------------------------
drop table if exists sys_job;
create table sys_job (
  job_id              bigint(20)    not null auto_increment    comment 'ä»»å¡ID',
  job_name            varchar(64)   default ''                 comment 'ä»»å¡åç§°',
  job_group           varchar(64)   default 'DEFAULT'          comment 'ä»»å¡ç»å',
  invoke_target       varchar(500)  not null                   comment 'è°ç¨ç®æ å­ç¬¦ä¸²',
  cron_expression     varchar(255)  default ''                 comment 'cronæ§è¡è¡¨è¾¾å¼',
  misfire_policy      varchar(20)   default '3'                comment 'è®¡åæ§è¡éè¯¯ç­ç¥ï¼1ç«å³æ§è¡ 2æ§è¡ä¸æ¬¡ 3æ¾å¼æ§è¡ï¼',
  concurrent          char(1)       default '1'                comment 'æ¯å¦å¹¶åæ§è¡ï¼0åè®¸ 1ç¦æ­¢ï¼',
  status              char(1)       default '0'                comment 'ç¶æï¼0æ­£å¸¸ 1æåï¼',
  create_by           varchar(64)   default ''                 comment 'åå»ºè',
  create_time         datetime                                 comment 'åå»ºæ¶é´',
  update_by           varchar(64)   default ''                 comment 'æ´æ°è',
  update_time         datetime                                 comment 'æ´æ°æ¶é´',
  remark              varchar(500)  default ''                 comment 'å¤æ³¨ä¿¡æ¯',
  primary key (job_id, job_name, job_group)
) engine=innodb auto_increment=100 comment = 'å®æ¶ä»»å¡è°åº¦è¡¨';

insert into sys_job values(1, 'ç³»ç»é»è®¤ï¼æ åï¼', 'DEFAULT', 'ryTask.ryNoParams',        '0/10 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', null, '');
insert into sys_job values(2, 'ç³»ç»é»è®¤ï¼æåï¼', 'DEFAULT', 'ryTask.ryParams(\'ry\')',  '0/15 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', null, '');
insert into sys_job values(3, 'ç³»ç»é»è®¤ï¼å¤åï¼', 'DEFAULT', 'ryTask.ryMultipleParams(\'ry\', true, 2000L, 316.50D, 100)',  '0/20 * * * * ?', '3', '1', '1', 'admin', sysdate(), '', null, '');
insert into sys_job values(4, 'åºæ¶å°ææéæ¨é', 'DEFAULT', 'finReceivableRemindTask.pushDueReminder(\'7\')', '0 0 8 * * ?', '3', '1', '0', 'admin', sysdate(), '', null, 'æ¯å¤©08:00æ¨éåºæ¶å°æä¸é¾ææé');
insert into sys_job values(5, 'åºå­é¢è­¦æéæ¨é', 'DEFAULT', 'wmsStockWarningRemindTask.pushWarningReminder()', '0 30 8 * * ?', '3', '1', '0', 'admin', sysdate(), '', null, 'æ¯å¤©08:30æ¨éåºå­é¢è­¦æé');
insert into sys_job values(6, 'ä¸å¡æ°æ®èªå¨å¤ä»½', 'DEFAULT', 'bizDataBackupTask.autoBackup()', '0 0 2 * * ?', '3', '1', '0', 'admin', sysdate(), '', null, 'æ¯å¤©02:00èªå¨å¤ä»½ä¸å¡æ°æ®');


-- ----------------------------
-- 16ãå®æ¶ä»»å¡è°åº¦æ¥å¿è¡¨
-- ----------------------------
drop table if exists sys_job_log;
create table sys_job_log (
  job_log_id          bigint(20)     not null auto_increment    comment 'ä»»å¡æ¥å¿ID',
  job_name            varchar(64)    not null                   comment 'ä»»å¡åç§°',
  job_group           varchar(64)    not null                   comment 'ä»»å¡ç»å',
  invoke_target       varchar(500)   not null                   comment 'è°ç¨ç®æ å­ç¬¦ä¸²',
  job_message         varchar(500)                              comment 'æ¥å¿ä¿¡æ¯',
  status              char(1)        default '0'                comment 'æ§è¡ç¶æï¼0æ­£å¸¸ 1å¤±è´¥ï¼',
  exception_info      varchar(2000)  default ''                 comment 'å¼å¸¸ä¿¡æ¯',
  create_time         datetime                                  comment 'åå»ºæ¶é´',
  primary key (job_log_id)
) engine=innodb comment = 'å®æ¶ä»»å¡è°åº¦æ¥å¿è¡¨';


-- ----------------------------
-- 17ãéç¥å¬åè¡¨
-- ----------------------------
drop table if exists sys_notice;
create table sys_notice (
  notice_id         int(4)          not null auto_increment    comment 'å¬åID',
  notice_title      varchar(50)     not null                   comment 'å¬åæ é¢',
  notice_type       char(1)         not null                   comment 'å¬åç±»åï¼1éç¥ 2å¬åï¼',
  notice_content    longblob        default null               comment 'å¬ååå®¹',
  status            char(1)         default '0'                comment 'å¬åç¶æï¼0æ­£å¸¸ 1å³é­ï¼',
  create_by         varchar(64)     default ''                 comment 'åå»ºè',
  create_time       datetime                                   comment 'åå»ºæ¶é´',
  update_by         varchar(64)     default ''                 comment 'æ´æ°è',
  update_time       datetime                                   comment 'æ´æ°æ¶é´',
  remark            varchar(255)    default null               comment 'å¤æ³¨',
  primary key (notice_id)
) engine=innodb auto_increment=10 comment = 'éç¥å¬åè¡¨';

-- ----------------------------
-- åå§å-å¬åä¿¡æ¯è¡¨æ°æ®
-- ----------------------------
insert into sys_notice values('1', 'æ¸©é¦¨æéï¼2018-07-01 è¥ä¾æ°çæ¬åå¸å¦', '2', 'æ°çæ¬åå®¹', '0', 'admin', sysdate(), '', null, 'ç®¡çå');
insert into sys_notice values('2', 'ç»´æ¤éç¥ï¼2018-07-01 è¥ä¾ç³»ç»åæ¨ç»´æ¤', '1', 'ç»´æ¤åå®¹',   '0', 'admin', sysdate(), '', null, 'ç®¡çå');


-- ----------------------------
-- 18ãä»£ç çæä¸å¡è¡¨
-- ----------------------------
drop table if exists gen_table;
create table gen_table (
  table_id          bigint(20)      not null auto_increment    comment 'ç¼å·',
  table_name        varchar(200)    default ''                 comment 'è¡¨åç§°',
  table_comment     varchar(500)    default ''                 comment 'è¡¨æè¿°',
  sub_table_name    varchar(64)     default null               comment 'å³èå­è¡¨çè¡¨å',
  sub_table_fk_name varchar(64)     default null               comment 'å­è¡¨å³èçå¤é®å',
  class_name        varchar(100)    default ''                 comment 'å®ä½ç±»åç§°',
  tpl_category      varchar(200)    default 'crud'             comment 'ä½¿ç¨çæ¨¡æ¿ï¼crudåè¡¨æä½ treeæ è¡¨æä½ï¼',
  tpl_web_type      varchar(30)     default ''                 comment 'åç«¯æ¨¡æ¿ç±»åï¼element-uiæ¨¡ç element-plusæ¨¡çï¼',
  package_name      varchar(100)                               comment 'çæåè·¯å¾',
  module_name       varchar(30)                                comment 'çææ¨¡åå',
  business_name     varchar(30)                                comment 'çæä¸å¡å',
  function_name     varchar(50)                                comment 'çæåè½å',
  function_author   varchar(50)                                comment 'çæåè½ä½è',
  gen_type          char(1)         default '0'                comment 'çæä»£ç æ¹å¼ï¼0zipåç¼©å 1èªå®ä¹è·¯å¾ï¼',
  gen_path          varchar(200)    default '/'                comment 'çæè·¯å¾ï¼ä¸å¡«é»è®¤é¡¹ç®è·¯å¾ï¼',
  options           varchar(1000)                              comment 'å¶å®çæéé¡¹',
  create_by         varchar(64)     default ''                 comment 'åå»ºè',
  create_time 	    datetime                                   comment 'åå»ºæ¶é´',
  update_by         varchar(64)     default ''                 comment 'æ´æ°è',
  update_time       datetime                                   comment 'æ´æ°æ¶é´',
  remark            varchar(500)    default null               comment 'å¤æ³¨',
  primary key (table_id)
) engine=innodb auto_increment=1 comment = 'ä»£ç çæä¸å¡è¡¨';


-- ----------------------------
-- 19ãä»£ç çæä¸å¡è¡¨å­æ®µ
-- ----------------------------
drop table if exists gen_table_column;
create table gen_table_column (
  column_id         bigint(20)      not null auto_increment    comment 'ç¼å·',
  table_id          bigint(20)                                 comment 'å½å±è¡¨ç¼å·',
  column_name       varchar(200)                               comment 'ååç§°',
  column_comment    varchar(500)                               comment 'åæè¿°',
  column_type       varchar(100)                               comment 'åç±»å',
  java_type         varchar(500)                               comment 'JAVAç±»å',
  java_field        varchar(200)                               comment 'JAVAå­æ®µå',
  is_pk             char(1)                                    comment 'æ¯å¦ä¸»é®ï¼1æ¯ï¼',
  is_increment      char(1)                                    comment 'æ¯å¦èªå¢ï¼1æ¯ï¼',
  is_required       char(1)                                    comment 'æ¯å¦å¿å¡«ï¼1æ¯ï¼',
  is_insert         char(1)                                    comment 'æ¯å¦ä¸ºæå¥å­æ®µï¼1æ¯ï¼',
  is_edit           char(1)                                    comment 'æ¯å¦ç¼è¾å­æ®µï¼1æ¯ï¼',
  is_list           char(1)                                    comment 'æ¯å¦åè¡¨å­æ®µï¼1æ¯ï¼',
  is_query          char(1)                                    comment 'æ¯å¦æ¥è¯¢å­æ®µï¼1æ¯ï¼',
  query_type        varchar(200)    default 'EQ'               comment 'æ¥è¯¢æ¹å¼ï¼ç­äºãä¸ç­äºãå¤§äºãå°äºãèå´ï¼',
  html_type         varchar(200)                               comment 'æ¾ç¤ºç±»åï¼ææ¬æ¡ãææ¬åãä¸ææ¡ãå¤éæ¡ãåéæ¡ãæ¥ææ§ä»¶ï¼',
  dict_type         varchar(200)    default ''                 comment 'å­å¸ç±»å',
  sort              int                                        comment 'æåº',
  create_by         varchar(64)     default ''                 comment 'åå»ºè',
  create_time 	    datetime                                   comment 'åå»ºæ¶é´',
  update_by         varchar(64)     default ''                 comment 'æ´æ°è',
  update_time       datetime                                   comment 'æ´æ°æ¶é´',
  primary key (column_id)
) engine=innodb auto_increment=1 comment = 'ä»£ç çæä¸å¡è¡¨å­æ®µ';

-- ----------------------------
-- 20ãéå®è®¢åä¸»è¡¨
-- ----------------------------
drop table if exists wms_sale_order;
create table wms_sale_order (
  sale_order_id      bigint(20)      not null auto_increment comment 'éå®åä¸»é®',
  order_no           varchar(32)     not null                comment 'éå®åå·',
  customer_id        bigint(20)      not null                comment 'å®¢æ·ç¼å·',
  warehouse_id       bigint(20)      not null                comment 'ä»åºç¼å·',
  total_qty          decimal(18,2)   default 0               comment 'æ»æ°é',
  total_amount       decimal(18,2)   default 0               comment 'æ»éé¢',
  status             varchar(20)     default 'draft'         comment 'ç¶æ',
  payment_status     varchar(20)     default 'unpaid'        comment 'ä»æ¬¾ç¶æ',
  manager_audit_by   varchar(64)     default null            comment 'ç»çå®¡æ ¸äºº',
  manager_audit_time datetime                                  comment 'ç»çå®¡æ ¸æ¶é´',
  manager_audit_comment varchar(500) default null            comment 'ç»çå®¡æ ¸æè§',
  audit_by           varchar(64)     default null            comment 'å®¡æ ¸äºº',
  audit_time         datetime                                  comment 'å®¡æ ¸æ¶é´',
  finance_audit_comment varchar(500) default null            comment 'è´¢å¡å®¡æ ¸æè§',
  create_by          varchar(64)     default ''              comment 'åå»ºè',
  create_time        datetime                                  comment 'åå»ºæ¶é´',
  update_by          varchar(64)     default ''              comment 'æ´æ°è',
  update_time        datetime                                  comment 'æ´æ°æ¶é´',
  remark             varchar(255)    default null            comment 'å¤æ³¨',
  primary key (sale_order_id),
  unique key uk_wms_sale_order_no (order_no)
) engine=innodb auto_increment=1 comment='éå®è®¢åä¸»è¡¨';

-- ----------------------------
-- 21ãéå®è®¢åæç»è¡¨
-- ----------------------------
drop table if exists wms_sale_order_item;
create table wms_sale_order_item (
  sale_order_item_id bigint(20)      not null auto_increment comment 'éå®æç»ä¸»é®',
  sale_order_id      bigint(20)      not null                comment 'éå®åç¼å·',
  product_id         bigint(20)      not null                comment 'ååç¼å·',
  location_id        bigint(20)      default null            comment 'åºä½ç¼å·',
  batch_no           varchar(64)     default null            comment 'æ¹æ¬¡å·',
  quantity           decimal(18,2)   default 0               comment 'æ°é',
  price              decimal(18,2)   default 0               comment 'åä»·',
  amount             decimal(18,2)   default 0               comment 'éé¢',
  primary key (sale_order_item_id),
  key idx_wms_sale_order_item_order_id (sale_order_id)
) engine=innodb auto_increment=1 comment='éå®è®¢åæç»è¡¨';

-- ----------------------------
-- 21.1ãéå®è®¢åç¶æåå²è¡¨
-- ----------------------------
drop table if exists wms_sale_order_status_history;
create table wms_sale_order_status_history (
  history_id         bigint(20)      not null auto_increment comment 'ç¶æåå²ä¸»é®',
  sale_order_id      bigint(20)      not null                comment 'éå®åç¼å·',
  from_status        varchar(20)     default null            comment 'åç¶æ',
  to_status          varchar(20)     default null            comment 'æ°ç¶æ',
  operation_type     varchar(50)     default null            comment 'æä½ç±»å',
  audit_role         varchar(30)     default null            comment 'å®¡æ ¸è§è²',
  audit_by           varchar(64)     default null            comment 'æä½äºº',
  audit_comment      varchar(500)    default null            comment 'å®¡æ ¸æè§',
  operate_time       datetime                                  comment 'æä½æ¶é´',
  create_by          varchar(64)     default ''              comment 'åå»ºè',
  create_time        datetime                                  comment 'åå»ºæ¶é´',
  update_by          varchar(64)     default ''              comment 'æ´æ°è',
  update_time        datetime                                  comment 'æ´æ°æ¶é´',
  remark             varchar(255)    default null            comment 'å¤æ³¨',
  primary key (history_id),
  key idx_wms_sale_order_status_history_order_id (sale_order_id)
) engine=innodb auto_increment=1 comment='éå®è®¢åç¶æåå²è¡¨';

-- ----------------------------
-- 22ãåºæ¶å°è´¦è¡¨
-- ----------------------------
drop table if exists fin_receivable;
create table fin_receivable (
  receivable_id      bigint(20)      not null auto_increment comment 'åºæ¶ä¸»é®',
  sale_order_id      bigint(20)      not null                comment 'éå®åç¼å·',
  customer_id        bigint(20)      not null                comment 'å®¢æ·ç¼å·',
  amount_due         decimal(18,2)   default 0               comment 'åºæ¶éé¢',
  amount_paid        decimal(18,2)   default 0               comment 'å·²æ¶éé¢',
  status             varchar(20)     default 'unpaid'        comment 'ç¶æ',
  due_date           datetime                                  comment 'å°ææ¥æ',
  create_by          varchar(64)     default ''              comment 'åå»ºè',
  create_time        datetime                                  comment 'åå»ºæ¶é´',
  update_by          varchar(64)     default ''              comment 'æ´æ°è',
  update_time        datetime                                  comment 'æ´æ°æ¶é´',
  remark             varchar(255)    default null            comment 'å¤æ³¨',
  primary key (receivable_id),
  unique key uk_fin_receivable_sale_order_id (sale_order_id)
) engine=innodb auto_increment=1 comment='åºæ¶å°è´¦è¡¨';

-- ----------------------------
-- 23ãåæ¬¾ç»è®°è¡¨
-- ----------------------------
drop table if exists fin_receipt;
create table fin_receipt (
  receipt_id         bigint(20)      not null auto_increment comment 'åæ¬¾ä¸»é®',
  receivable_id      bigint(20)      not null                comment 'åºæ¶ç¼å·',
  sale_order_id      bigint(20)      not null                comment 'éå®åç¼å·',
  customer_id        bigint(20)      not null                comment 'å®¢æ·ç¼å·',
  amount             decimal(18,2)   default 0               comment 'åæ¬¾éé¢',
  receipt_date       datetime                                  comment 'åæ¬¾æ¥æ',
  remark             varchar(255)    default null            comment 'å¤æ³¨',
  create_by          varchar(64)     default ''              comment 'åå»ºè',
  create_time        datetime                                  comment 'åå»ºæ¶é´',
  update_by          varchar(64)     default ''              comment 'æ´æ°è',
  update_time        datetime                                  comment 'æ´æ°æ¶é´',
  primary key (receipt_id),
  key idx_fin_receipt_receivable_id (receivable_id)
) engine=innodb auto_increment=1 comment='åæ¬¾ç»è®°è¡¨';

-- ----------------------------
-- 24ãååèµæè¡¨
-- ----------------------------
drop table if exists md_product;
create table md_product (
  product_id         bigint(20)      not null auto_increment comment 'ååä¸»é®',
  product_code       varchar(64)     not null                comment 'ååç¼ç ',
  product_name       varchar(128)    not null                comment 'åååç§°',
  product_spec       varchar(128)    default null            comment 'è§æ ¼',
  unit_name          varchar(32)     default null            comment 'åä½',
  bar_code           varchar(64)     default null            comment 'æ¡ç ',
  category_name      varchar(64)     default null            comment 'åç±»',
  brand_name         varchar(64)     default null            comment 'åç',
  product_image      varchar(255)    default null            comment 'ååå¾ç',
  cost_price         decimal(18,2)   default 0               comment 'ææ¬ä»·',
  sale_price         decimal(18,2)   default 0               comment 'éå®ä»·',
  status             char(1)         default '0'             comment 'ç¶æ',
  del_flag           char(1)         default '0'             comment 'å é¤æ è®°',
  create_by          varchar(64)     default ''              comment 'åå»ºè',
  create_time        datetime                                  comment 'åå»ºæ¶é´',
  update_by          varchar(64)     default ''              comment 'æ´æ°è',
  update_time        datetime                                  comment 'æ´æ°æ¶é´',
  remark             varchar(255)    default null            comment 'å¤æ³¨',
  primary key (product_id),
  unique key uk_md_product_code (product_code)
) engine=innodb auto_increment=1 comment='ååèµæè¡¨';

-- ----------------------------
-- 24ãä»åºèµæè¡¨
-- ----------------------------
drop table if exists md_warehouse;
create table md_warehouse (
  warehouse_id       bigint(20)      not null auto_increment comment 'ä»åºä¸»é®',
  warehouse_code     varchar(64)     not null                comment 'ä»åºç¼ç ',
  warehouse_name     varchar(128)    not null                comment 'ä»åºåç§°',
  contact_name       varchar(64)     default null            comment 'èç³»äºº',
  contact_phone      varchar(32)     default null            comment 'èç³»çµè¯',
  address            varchar(255)    default null            comment 'å°å',
  status             char(1)         default '0'             comment 'ç¶æ',
  del_flag           char(1)         default '0'             comment 'å é¤æ è®°',
  create_by          varchar(64)     default ''              comment 'åå»ºè',
  create_time        datetime                                  comment 'åå»ºæ¶é´',
  update_by          varchar(64)     default ''              comment 'æ´æ°è',
  update_time        datetime                                  comment 'æ´æ°æ¶é´',
  remark             varchar(255)    default null            comment 'å¤æ³¨',
  primary key (warehouse_id),
  unique key uk_md_warehouse_code (warehouse_code)
) engine=innodb auto_increment=1 comment='ä»åºèµæè¡¨';

-- ----------------------------
-- 25ãåºä½èµæè¡¨
-- ----------------------------
drop table if exists md_location;
create table md_location (
  location_id        bigint(20)      not null auto_increment comment 'åºä½ä¸»é®',
  warehouse_id       bigint(20)      not null                comment 'ä»åºç¼å·',
  location_code      varchar(64)     not null                comment 'åºä½ç¼ç ',
  location_name      varchar(128)    not null                comment 'åºä½åç§°',
  status             char(1)         default '0'             comment 'ç¶æ',
  del_flag           char(1)         default '0'             comment 'å é¤æ è®°',
  create_by          varchar(64)     default ''              comment 'åå»ºè',
  create_time        datetime                                  comment 'åå»ºæ¶é´',
  update_by          varchar(64)     default ''              comment 'æ´æ°è',
  update_time        datetime                                  comment 'æ´æ°æ¶é´',
  remark             varchar(255)    default null            comment 'å¤æ³¨',
  primary key (location_id),
  unique key uk_md_location_code (location_code)
) engine=innodb auto_increment=1 comment='åºä½èµæè¡¨';

-- ----------------------------
-- 26ãéå®éè´§ä¸»è¡¨
-- ----------------------------
drop table if exists wms_sale_return;
create table wms_sale_return (
  sale_return_id     bigint(20)      not null auto_increment comment 'éå®éè´§ä¸»é®',
  return_no          varchar(32)     not null                comment 'éè´§åå·',
  return_type        varchar(20)     default null            comment 'éè´§ç±»å',
  customer_id        bigint(20)      not null                comment 'å®¢æ·ç¼å·',
  warehouse_id       bigint(20)      not null                comment 'ä»åºç¼å·',
  total_qty          decimal(18,2)   default 0               comment 'æ»æ°é',
  total_amount       decimal(18,2)   default 0               comment 'æ»éé¢',
  status             varchar(20)     default 'draft'         comment 'ç¶æ',
  audit_by           varchar(64)     default null            comment 'å®¡æ ¸äºº',
  audit_time         datetime                                  comment 'å®¡æ ¸æ¶é´',
  create_by          varchar(64)     default ''              comment 'åå»ºè',
  create_time        datetime                                  comment 'åå»ºæ¶é´',
  update_by          varchar(64)     default ''              comment 'æ´æ°è',
  update_time        datetime                                  comment 'æ´æ°æ¶é´',
  remark             varchar(255)    default null            comment 'å¤æ³¨',
  primary key (sale_return_id),
  unique key uk_wms_sale_return_no (return_no)
) engine=innodb auto_increment=1 comment='éå®éè´§ä¸»è¡¨';

-- ----------------------------
-- 27ãéå®éè´§æç»è¡¨
-- ----------------------------
drop table if exists wms_sale_return_item;
create table wms_sale_return_item (
  sale_return_item_id bigint(20)      not null auto_increment comment 'éå®éè´§æç»ä¸»é®',
  sale_return_id      bigint(20)      not null                comment 'éå®éè´§ç¼å·',
  product_id          bigint(20)      not null                comment 'ååç¼å·',
  quantity            decimal(18,2)   default 0               comment 'æ°é',
  price               decimal(18,2)   default 0               comment 'åä»·',
  amount              decimal(18,2)   default 0               comment 'éé¢',
  primary key (sale_return_item_id),
  key idx_wms_sale_return_item_return_id (sale_return_id)
) engine=innodb auto_increment=1 comment='éå®éè´§æç»è¡¨';

-- ----------------------------
-- 28ãåºå­çç¹ä¸»è¡¨
-- ----------------------------
drop table if exists wms_inventory_check;
create table wms_inventory_check (
  check_id            bigint(20)      not null auto_increment comment 'åºå­çç¹ä¸»é®',
  check_no            varchar(32)     not null                comment 'çç¹åå·',
  warehouse_id        bigint(20)      not null                comment 'ä»åºç¼å·',
  status              varchar(20)     default 'draft'         comment 'ç¶æ',
  total_diff_qty      decimal(18,2)   default 0               comment 'å·®å¼æ°é',
  total_diff_amount   decimal(18,2)   default 0               comment 'å·®å¼éé¢',
  audit_by            varchar(64)     default null            comment 'å®¡æ ¸äºº',
  audit_time          datetime                                  comment 'å®¡æ ¸æ¶é´',
  create_by           varchar(64)     default ''              comment 'åå»ºè',
  create_time         datetime                                  comment 'åå»ºæ¶é´',
  update_by           varchar(64)     default ''              comment 'æ´æ°è',
  update_time         datetime                                  comment 'æ´æ°æ¶é´',
  remark              varchar(255)    default null            comment 'å¤æ³¨',
  primary key (check_id),
  unique key uk_wms_inventory_check_no (check_no)
) engine=innodb auto_increment=1 comment='åºå­çç¹ä¸»è¡¨';

-- ----------------------------
-- 29ãåºå­çç¹æç»è¡¨
-- ----------------------------
drop table if exists wms_inventory_check_item;
create table wms_inventory_check_item (
  check_item_id       bigint(20)      not null auto_increment comment 'åºå­çç¹æç»ä¸»é®',
  check_id            bigint(20)      not null                comment 'çç¹åç¼å·',
  product_id          bigint(20)      not null                comment 'ååç¼å·',
  location_id         bigint(20)      default null            comment 'åºä½ç¼å·',
  batch_no            varchar(64)     default null            comment 'æ¹æ¬¡å·',
  stock_qty           decimal(18,2)   default 0               comment 'è´¦é¢æ°é',
  actual_qty          decimal(18,2)   default 0               comment 'å®çæ°é',
  diff_qty            decimal(18,2)   default 0               comment 'å·®å¼æ°é',
  price               decimal(18,2)   default 0               comment 'åä»·',
  diff_amount         decimal(18,2)   default 0               comment 'å·®å¼éé¢',
  primary key (check_item_id),
  key idx_wms_inventory_check_item_check_id (check_id)
) engine=innodb auto_increment=1 comment='åºå­çç¹æç»è¡¨';

-- ----------------------------
-- 30ãä¸å¡æ¶æ¯è¡¨
-- ----------------------------
drop table if exists biz_message;
create table biz_message (
  message_id          bigint(20)      not null auto_increment comment 'æ¶æ¯ä¸»é®',
  message_type        varchar(64)     not null                comment 'æ¶æ¯ç±»å',
  message_title       varchar(200)    not null                comment 'æ¶æ¯æ é¢',
  message_content     varchar(1000)   default ''              comment 'æ¶æ¯åå®¹',
  message_level       varchar(20)     default 'info'          comment 'æ¶æ¯çº§å«',
  business_type       varchar(64)     default null            comment 'ä¸å¡ç±»å',
  business_id         bigint(20)      default null            comment 'ä¸å¡ä¸»é®',
  status              char(1)         default '0'             comment 'ç¶æï¼0æ­£å¸¸ 1å³é­ï¼',
  create_by           varchar(64)     default ''              comment 'åå»ºè',
  create_time         datetime                                  comment 'åå»ºæ¶é´',
  update_by           varchar(64)     default ''              comment 'æ´æ°è',
  update_time         datetime                                  comment 'æ´æ°æ¶é´',
  remark              varchar(255)    default null            comment 'å¤æ³¨',
  primary key (message_id),
  key idx_biz_message_type (message_type),
  key idx_biz_message_status (status)
) engine=innodb auto_increment=1 comment='ä¸å¡æ¶æ¯è¡¨';

-- ----------------------------
-- 31ãä¸å¡æ¶æ¯å·²è¯»è¡¨
-- ----------------------------
drop table if exists biz_message_read;
create table biz_message_read (
  read_id             bigint(20)      not null auto_increment comment 'å·²è¯»ä¸»é®',
  message_id          bigint(20)      not null                comment 'æ¶æ¯ä¸»é®',
  user_id             bigint(20)      not null                comment 'ç¨æ·ä¸»é®',
  read_time           datetime                                  comment 'å·²è¯»æ¶é´',
  create_time         datetime                                  comment 'åå»ºæ¶é´',
  primary key (read_id),
  unique key uk_biz_message_read (message_id, user_id),
  key idx_biz_message_read_user (user_id)
) engine=innodb auto_increment=1 comment='ä¸å¡æ¶æ¯å·²è¯»è¡¨';

-- ----------------------------
-- 32ãä¸å¡æ°æ®å¤ä»½è¡¨
-- ----------------------------
drop table if exists biz_data_backup;
create table biz_data_backup (
  backup_id           bigint(20)      not null auto_increment comment 'å¤ä»½ä¸»é®',
  backup_name         varchar(100)    not null                comment 'å¤ä»½åç§°',
  backup_type         varchar(20)     not null                comment 'å¤ä»½ç±»åï¼manual/autoï¼',
  backup_status       varchar(20)     default 'success'       comment 'å¤ä»½ç¶æï¼success/failedï¼',
  table_count         int(11)         default 0               comment 'è¡¨æ°é',
  record_count        bigint(20)      default 0               comment 'è®°å½æ°é',
  backup_content      longtext                                 comment 'å¤ä»½åå®¹',
  restore_status      varchar(20)     default null            comment 'æ¢å¤ç¶æï¼success/failedï¼',
  restore_by          varchar(64)     default ''              comment 'æ¢å¤äºº',
  restore_time        datetime                                  comment 'æ¢å¤æ¶é´',
  create_by           varchar(64)     default ''              comment 'åå»ºè',
  create_time         datetime                                  comment 'åå»ºæ¶é´',
  update_by           varchar(64)     default ''              comment 'æ´æ°è',
  update_time         datetime                                  comment 'æ´æ°æ¶é´',
  remark              varchar(255)    default null            comment 'å¤æ³¨',
  primary key (backup_id),
  key idx_biz_data_backup_type (backup_type),
  key idx_biz_data_backup_status (backup_status)
) engine=innodb auto_increment=1 comment='ä¸å¡æ°æ®å¤ä»½è¡¨';

-- ----------------------------
-- 33ãä¸å¡èåä¸æé®ï¼businesså¨æ¨¡åï¼
-- ----------------------------
insert into sys_menu values('3023', 'åºç¡èµæ', '0', '5', 'base', null, '', '', 1, 0, 'M', '0', '0', '', 'dict', 'admin', sysdate(), '', null, 'åºç¡èµæç®å½');
insert into sys_menu values('3024', 'ä»å¨ç®¡ç', '0', '6', 'warehouseGroup', null, '', '', 1, 0, 'M', '0', '0', '', 'international', 'admin', sysdate(), '', null, 'ä»å¨ç®¡çç®å½');
insert into sys_menu values('3025', 'éè´­ç®¡ç', '0', '7', 'purchase', null, '', '', 1, 0, 'M', '0', '0', '', 'shopping-cart', 'admin', sysdate(), '', null, 'éè´­ç®¡çç®å½');
insert into sys_menu values('3026', 'éå®ç®¡ç', '0', '8', 'sales', null, '', '', 1, 0, 'M', '0', '0', '', 'tickets', 'admin', sysdate(), '', null, 'éå®ç®¡çç®å½');
insert into sys_menu values('3027', 'åºå­ç®¡ç', '0', '9', 'inventory', null, '', '', 1, 0, 'M', '0', '0', '', 'money', 'admin', sysdate(), '', null, 'åºå­ç®¡çç®å½');
insert into sys_menu values('3028', 'è´¢å¡åæ', '0', '10', 'finance', null, '', '', 1, 0, 'M', '0', '0', '', 'dashboard', 'admin', sysdate(), '', null, 'è´¢å¡åæç®å½');
insert into sys_menu values('3001', 'ååç®¡ç', '3023', '1', 'product', 'business/product/index', '', '', 1, 0, 'C', '0', '0', 'business:product:list', 'shopping', 'admin', sysdate(), '', null, 'ååç®¡çèå');
insert into sys_menu values('3002', 'å®¢æ·ç®¡ç', '3023', '2', 'customer', 'business/customer/index', '', '', 1, 0, 'C', '0', '0', 'business:customer:list', 'user', 'admin', sysdate(), '', null, 'å®¢æ·ç®¡çèå');
insert into sys_menu values('3003', 'ä»åºç®¡ç', '3024', '1', 'warehouse', 'business/warehouse/index', '', '', 1, 0, 'C', '0', '0', 'business:warehouse:list', 'international', 'admin', sysdate(), '', null, 'ä»åºç®¡çèå');
insert into sys_menu values('3004', 'åºä½ç®¡ç', '3024', '2', 'location', 'business/location/index', '', '', 1, 0, 'C', '0', '0', 'business:location:list', 'tree', 'admin', sysdate(), '', null, 'åºä½ç®¡çèå');
insert into sys_menu values('3005', 'éè´­å¥åº', '3025', '1', 'inbound', 'business/inbound/index', '', '', 1, 0, 'C', '0', '0', 'business:inbound:list', 'guide', 'admin', sysdate(), '', null, 'éè´­å¥åºèå');
insert into sys_menu values('3011', 'éè´­éè´§', '3025', '2', 'purchaseReturn', 'business/purchaseReturn/index', '', '', 1, 0, 'C', '0', '0', 'business:purchaseReturn:list', 'refresh-left', 'admin', sysdate(), '', null, 'éè´­éè´§èå');
insert into sys_menu values('3007', 'éå®åºåº', '3026', '1', 'outbound', 'business/outbound/index', '', '', 1, 0, 'C', '0', '0', 'business:outbound:list', 'shopping-cart', 'admin', sysdate(), '', null, 'éå®åºåºèå');
insert into sys_menu values('3013', 'éå®è®¢å', '3026', '2', 'saleOrder', 'business/saleOrder/index', '', '', 1, 0, 'C', '0', '0', 'business:saleOrder:list', 'tickets', 'admin', sysdate(), '', null, 'éå®è®¢åèå');
insert into sys_menu values('3015', 'éå®éè´§', '3026', '3', 'saleReturn', 'business/saleReturn/index', '', '', 1, 0, 'C', '0', '0', 'business:saleReturn:list', 'money', 'admin', sysdate(), '', null, 'éå®éè´§èå');
insert into sys_menu values('3009', 'åºå­å°è´¦', '3027', '1', 'stock', 'business/stock/index', '', '', 1, 0, 'C', '0', '0', 'business:stock:list', 'money', 'admin', sysdate(), '', null, 'åºå­å°è´¦èå');
insert into sys_menu values('3010', 'åºå­æµæ°´', '3027', '2', 'stockLog', 'business/stockLog/index', '', '', 1, 0, 'C', '0', '0', 'business:stockLog:list', 'form', 'admin', sysdate(), '', null, 'åºå­æµæ°´èå');
insert into sys_menu values('3020', 'åºå­çç¹', '3027', '3', 'inventoryCheck', 'business/inventoryCheck/index', '', '', 1, 0, 'C', '0', '0', 'business:inventoryCheck:list', 'tickets', 'admin', sysdate(), '', null, 'åºå­çç¹èå');
insert into sys_menu values('3017', 'åºæ¶å°è´¦', '3028', '1', 'receivable', 'business/receivable/index', '', '', 1, 0, 'C', '0', '0', 'business:receivable:list', 'postcard', 'admin', sysdate(), '', null, 'åºæ¶å°è´¦èå');
insert into sys_menu values('3018', 'åæ¬¾è®°å½', '3028', '2', 'receipt', 'business/receipt/index', '', '', 1, 0, 'C', '0', '0', 'business:receipt:list', 'wallet', 'admin', sysdate(), '', null, 'åæ¬¾è®°å½èå');
insert into sys_menu values('3019', 'ç»è¥çæ¿', '3028', '3', 'report', 'business/report/index', '', '', 1, 0, 'C', '0', '0', 'business:report:view', 'dashboard', 'admin', sysdate(), '', null, 'ç»è¥çæ¿èå');
insert into sys_menu values('3022', 'æ°æ®å¤ä»½', '3028', '4', 'dataBackup', 'business/dataBackup/index', '', '', 1, 0, 'C', '0', '0', 'business:dataBackup:list', 'set-up', 'admin', sysdate(), '', null, 'æ°æ®å¤ä»½èå');

insert into sys_menu values('3101', 'ååæ¥è¯¢', '3001', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:product:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3102', 'ååæ°å¢', '3001', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:product:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3103', 'ååä¿®æ¹', '3001', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:product:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3104', 'ååå é¤', '3001', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:product:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3105', 'ååå¯¼åº', '3001', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:product:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('3201', 'å®¢æ·æ¥è¯¢', '3002', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:customer:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3202', 'å®¢æ·æ°å¢', '3002', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:customer:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3203', 'å®¢æ·ä¿®æ¹', '3002', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:customer:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3204', 'å®¢æ·å é¤', '3002', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:customer:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3205', 'å®¢æ·å¯¼åº', '3002', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:customer:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('3301', 'ä»åºæ¥è¯¢', '3003', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:warehouse:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3302', 'ä»åºæ°å¢', '3003', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:warehouse:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3303', 'ä»åºä¿®æ¹', '3003', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:warehouse:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3304', 'ä»åºå é¤', '3003', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:warehouse:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3305', 'ä»åºå¯¼åº', '3003', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:warehouse:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('3401', 'åºä½æ¥è¯¢', '3004', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:location:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3402', 'åºä½æ°å¢', '3004', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:location:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3403', 'åºä½ä¿®æ¹', '3004', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:location:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3404', 'åºä½å é¤', '3004', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:location:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3405', 'åºä½å¯¼åº', '3004', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:location:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('3501', 'å¥åºæ¥è¯¢', '3005', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inbound:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3502', 'å¥åºæ°å¢', '3005', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inbound:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3503', 'å¥åºä¿®æ¹', '3005', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inbound:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3504', 'å¥åºå é¤', '3005', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inbound:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3505', 'å¥åºå¯¼åº', '3005', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inbound:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3506', 'å¥åºå®¡æ ¸', '3005', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inbound:audit', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('3701', 'åºåºæ¥è¯¢', '3007', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:outbound:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3702', 'åºåºæ°å¢', '3007', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:outbound:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3703', 'åºåºä¿®æ¹', '3007', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:outbound:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3704', 'åºåºå é¤', '3007', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:outbound:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3705', 'åºåºå¯¼åº', '3007', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:outbound:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3706', 'åºåºå®¡æ ¸', '3007', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:outbound:audit', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('3901', 'åºå­æ¥è¯¢', '3009', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:stock:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3902', 'åºå­æ°å¢', '3009', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:stock:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3903', 'åºå­ä¿®æ¹', '3009', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:stock:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3904', 'åºå­å é¤', '3009', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:stock:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('3905', 'åºå­å¯¼åº', '3009', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:stock:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('4001', 'æµæ°´æ¥è¯¢', '3010', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:stockLog:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4002', 'æµæ°´å¯¼åº', '3010', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:stockLog:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('4101', 'éè´­éè´§æ¥è¯¢', '3011', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:purchaseReturn:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4102', 'éè´­éè´§æ°å¢', '3011', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:purchaseReturn:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4103', 'éè´­éè´§ä¿®æ¹', '3011', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:purchaseReturn:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4104', 'éè´­éè´§å é¤', '3011', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:purchaseReturn:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4105', 'éè´­éè´§å¯¼åº', '3011', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:purchaseReturn:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4106', 'éè´­éè´§å®¡æ ¸', '3011', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:purchaseReturn:audit', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('4301', 'è®¢åæ¥è¯¢', '3013', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4302', 'è®¢åæ°å¢', '3013', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4303', 'è®¢åä¿®æ¹', '3013', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4304', 'è®¢åå é¤', '3013', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4305', 'è®¢åå¯¼åº', '3013', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4306', 'ç»çå®¡æ ¸', '3013', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:managerAudit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4307', 'è®¢åå¯¼å¥', '3013', '7', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:import', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4308', 'æå°æ¨¡æ¿', '3013', '8', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:print', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4309', 'è´¢å¡å®¡æ ¸', '3013', '9', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:financeAudit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4310', 'è®¢åæäº¤', '3013', '10', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:submit', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('4501', 'éè´§æ¥è¯¢', '3015', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleReturn:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4502', 'éè´§æ°å¢', '3015', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleReturn:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4503', 'éè´§ä¿®æ¹', '3015', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleReturn:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4504', 'éè´§å é¤', '3015', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleReturn:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4505', 'éè´§å¯¼åº', '3015', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleReturn:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4506', 'éè´§å®¡æ ¸', '3015', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleReturn:audit', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('4701', 'åºæ¶æ¥è¯¢', '3017', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receivable:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4702', 'åºæ¶æ°å¢', '3017', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receivable:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4703', 'åºæ¶ä¿®æ¹', '3017', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receivable:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4704', 'åºæ¶å é¤', '3017', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receivable:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4705', 'åºæ¶å¯¼åº', '3017', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receivable:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4706', 'å°ææéæ¥è¯¢', '3017', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receivable:due', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4707', 'é¾æè§å¾æ¥è¯¢', '3017', '7', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receivable:overdue', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('4801', 'åæ¬¾æ¥è¯¢', '3018', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receipt:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4802', 'åæ¬¾æ°å¢', '3018', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receipt:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4803', 'åæ¬¾ä¿®æ¹', '3018', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receipt:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4804', 'åæ¬¾å é¤', '3018', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receipt:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('4805', 'åæ¬¾å¯¼åº', '3018', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receipt:export', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('4901', 'çæ¿æ¥ç', '3019', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:report:view', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('5001', 'çç¹æ¥è¯¢', '3020', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inventoryCheck:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('5002', 'çç¹æ°å¢', '3020', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inventoryCheck:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('5003', 'çç¹ä¿®æ¹', '3020', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inventoryCheck:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('5004', 'çç¹å é¤', '3020', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inventoryCheck:remove', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('5005', 'çç¹å¯¼åº', '3020', '5', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inventoryCheck:export', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('5006', 'çç¹å®¡æ ¸', '3020', '6', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inventoryCheck:audit', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu values('5201', 'å¤ä»½æ¥è¯¢', '3022', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:dataBackup:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('5202', 'æå¨å¤ä»½', '3022', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:dataBackup:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('5203', 'æ°æ®æ¢å¤', '3022', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:dataBackup:restore', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('5204', 'è®°å½å é¤', '3022', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'business:dataBackup:remove', '#', 'admin', sysdate(), '', null, '');

insert into sys_role_menu values ('3', '3026');
insert into sys_role_menu values ('3', '3013');
insert into sys_role_menu values ('3', '4301');
insert into sys_role_menu values ('3', '4302');
insert into sys_role_menu values ('3', '4303');
insert into sys_role_menu values ('3', '4304');
insert into sys_role_menu values ('3', '4305');
insert into sys_role_menu values ('3', '4307');
insert into sys_role_menu values ('3', '4308');
insert into sys_role_menu values ('3', '4310');

insert into sys_role_menu values ('4', '3026');
insert into sys_role_menu values ('4', '3013');
insert into sys_role_menu values ('4', '4301');
insert into sys_role_menu values ('4', '4306');

insert into sys_role_menu values ('5', '3026');
insert into sys_role_menu values ('5', '3013');
insert into sys_role_menu values ('5', '4301');
insert into sys_role_menu values ('5', '4309');


DROP TABLE IF EXISTS QRTZ_FIRED_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_PAUSED_TRIGGER_GRPS;
DROP TABLE IF EXISTS QRTZ_SCHEDULER_STATE;
DROP TABLE IF EXISTS QRTZ_LOCKS;
DROP TABLE IF EXISTS QRTZ_SIMPLE_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_SIMPROP_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_CRON_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_BLOB_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_TRIGGERS;
DROP TABLE IF EXISTS QRTZ_JOB_DETAILS;
DROP TABLE IF EXISTS QRTZ_CALENDARS;

-- ----------------------------
-- 1ãå­å¨æ¯ä¸ä¸ªå·²éç½®ç jobDetail çè¯¦ç»ä¿¡æ¯
-- ----------------------------
create table QRTZ_JOB_DETAILS (
    sched_name           varchar(120)    not null            comment 'è°åº¦åç§°',
    job_name             varchar(200)    not null            comment 'ä»»å¡åç§°',
    job_group            varchar(200)    not null            comment 'ä»»å¡ç»å',
    description          varchar(250)    null                comment 'ç¸å³ä»ç»',
    job_class_name       varchar(250)    not null            comment 'æ§è¡ä»»å¡ç±»åç§°',
    is_durable           varchar(1)      not null            comment 'æ¯å¦æä¹å',
    is_nonconcurrent     varchar(1)      not null            comment 'æ¯å¦å¹¶å',
    is_update_data       varchar(1)      not null            comment 'æ¯å¦æ´æ°æ°æ®',
    requests_recovery    varchar(1)      not null            comment 'æ¯å¦æ¥åæ¢å¤æ§è¡',
    job_data             blob            null                comment 'å­æ¾æä¹åjobå¯¹è±¡',
    primary key (sched_name, job_name, job_group)
) engine=innodb comment = 'ä»»å¡è¯¦ç»ä¿¡æ¯è¡¨';

-- ----------------------------
-- 2ã å­å¨å·²éç½®ç Trigger çä¿¡æ¯
-- ----------------------------
create table QRTZ_TRIGGERS (
    sched_name           varchar(120)    not null            comment 'è°åº¦åç§°',
    trigger_name         varchar(200)    not null            comment 'è§¦åå¨çåå­',
    trigger_group        varchar(200)    not null            comment 'è§¦åå¨æå±ç»çåå­',
    job_name             varchar(200)    not null            comment 'qrtz_job_detailsè¡¨job_nameçå¤é®',
    job_group            varchar(200)    not null            comment 'qrtz_job_detailsè¡¨job_groupçå¤é®',
    description          varchar(250)    null                comment 'ç¸å³ä»ç»',
    next_fire_time       bigint(13)      null                comment 'ä¸ä¸æ¬¡è§¦åæ¶é´ï¼æ¯«ç§ï¼',
    prev_fire_time       bigint(13)      null                comment 'ä¸ä¸æ¬¡è§¦åæ¶é´ï¼é»è®¤ä¸º-1è¡¨ç¤ºä¸è§¦åï¼',
    priority             integer         null                comment 'ä¼åçº§',
    trigger_state        varchar(16)     not null            comment 'è§¦åå¨ç¶æ',
    trigger_type         varchar(8)      not null            comment 'è§¦åå¨çç±»å',
    start_time           bigint(13)      not null            comment 'å¼å§æ¶é´',
    end_time             bigint(13)      null                comment 'ç»ææ¶é´',
    calendar_name        varchar(200)    null                comment 'æ¥ç¨è¡¨åç§°',
    misfire_instr        smallint(2)     null                comment 'è¡¥å¿æ§è¡çç­ç¥',
    job_data             blob            null                comment 'å­æ¾æä¹åjobå¯¹è±¡',
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, job_name, job_group) references QRTZ_JOB_DETAILS(sched_name, job_name, job_group)
) engine=innodb comment = 'è§¦åå¨è¯¦ç»ä¿¡æ¯è¡¨';

-- ----------------------------
-- 3ã å­å¨ç®åç Triggerï¼åæ¬éå¤æ¬¡æ°ï¼é´éï¼ä»¥åå·²è§¦åçæ¬¡æ°
-- ----------------------------
create table QRTZ_SIMPLE_TRIGGERS (
    sched_name           varchar(120)    not null            comment 'è°åº¦åç§°',
    trigger_name         varchar(200)    not null            comment 'qrtz_triggersè¡¨trigger_nameçå¤é®',
    trigger_group        varchar(200)    not null            comment 'qrtz_triggersè¡¨trigger_groupçå¤é®',
    repeat_count         bigint(7)       not null            comment 'éå¤çæ¬¡æ°ç»è®¡',
    repeat_interval      bigint(12)      not null            comment 'éå¤çé´éæ¶é´',
    times_triggered      bigint(10)      not null            comment 'å·²ç»è§¦åçæ¬¡æ°',
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS(sched_name, trigger_name, trigger_group)
) engine=innodb comment = 'ç®åè§¦åå¨çä¿¡æ¯è¡¨';

-- ----------------------------
-- 4ã å­å¨ Cron Triggerï¼åæ¬ Cron è¡¨è¾¾å¼åæ¶åºä¿¡æ¯
-- ---------------------------- 
create table QRTZ_CRON_TRIGGERS (
    sched_name           varchar(120)    not null            comment 'è°åº¦åç§°',
    trigger_name         varchar(200)    not null            comment 'qrtz_triggersè¡¨trigger_nameçå¤é®',
    trigger_group        varchar(200)    not null            comment 'qrtz_triggersè¡¨trigger_groupçå¤é®',
    cron_expression      varchar(200)    not null            comment 'cronè¡¨è¾¾å¼',
    time_zone_id         varchar(80)                         comment 'æ¶åº',
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS(sched_name, trigger_name, trigger_group)
) engine=innodb comment = 'Cronç±»åçè§¦åå¨è¡¨';

-- ----------------------------
-- 5ã Trigger ä½ä¸º Blob ç±»åå­å¨(ç¨äº Quartz ç¨æ·ç¨ JDBC åå»ºä»ä»¬èªå·±å®å¶ç Trigger ç±»åï¼JobStore å¹¶ä¸ç¥éå¦ä½å­å¨å®ä¾çæ¶å)
-- ---------------------------- 
create table QRTZ_BLOB_TRIGGERS (
    sched_name           varchar(120)    not null            comment 'è°åº¦åç§°',
    trigger_name         varchar(200)    not null            comment 'qrtz_triggersè¡¨trigger_nameçå¤é®',
    trigger_group        varchar(200)    not null            comment 'qrtz_triggersè¡¨trigger_groupçå¤é®',
    blob_data            blob            null                comment 'å­æ¾æä¹åTriggerå¯¹è±¡',
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS(sched_name, trigger_name, trigger_group)
) engine=innodb comment = 'Blobç±»åçè§¦åå¨è¡¨';

-- ----------------------------
-- 6ã ä»¥ Blob ç±»åå­å¨å­æ¾æ¥åä¿¡æ¯ï¼ quartzå¯éç½®ä¸ä¸ªæ¥åæ¥æå®ä¸ä¸ªæ¶é´èå´
-- ---------------------------- 
create table QRTZ_CALENDARS (
    sched_name           varchar(120)    not null            comment 'è°åº¦åç§°',
    calendar_name        varchar(200)    not null            comment 'æ¥ååç§°',
    calendar             blob            not null            comment 'å­æ¾æä¹åcalendarå¯¹è±¡',
    primary key (sched_name, calendar_name)
) engine=innodb comment = 'æ¥åä¿¡æ¯è¡¨';

-- ----------------------------
-- 7ã å­å¨å·²æåç Trigger ç»çä¿¡æ¯
-- ---------------------------- 
create table QRTZ_PAUSED_TRIGGER_GRPS (
    sched_name           varchar(120)    not null            comment 'è°åº¦åç§°',
    trigger_group        varchar(200)    not null            comment 'qrtz_triggersè¡¨trigger_groupçå¤é®',
    primary key (sched_name, trigger_group)
) engine=innodb comment = 'æåçè§¦åå¨è¡¨';

-- ----------------------------
-- 8ã å­å¨ä¸å·²è§¦åç Trigger ç¸å³çç¶æä¿¡æ¯ï¼ä»¥åç¸è Job çæ§è¡ä¿¡æ¯
-- ---------------------------- 
create table QRTZ_FIRED_TRIGGERS (
    sched_name           varchar(120)    not null            comment 'è°åº¦åç§°',
    entry_id             varchar(95)     not null            comment 'è°åº¦å¨å®ä¾id',
    trigger_name         varchar(200)    not null            comment 'qrtz_triggersè¡¨trigger_nameçå¤é®',
    trigger_group        varchar(200)    not null            comment 'qrtz_triggersè¡¨trigger_groupçå¤é®',
    instance_name        varchar(200)    not null            comment 'è°åº¦å¨å®ä¾å',
    fired_time           bigint(13)      not null            comment 'è§¦åçæ¶é´',
    sched_time           bigint(13)      not null            comment 'å®æ¶å¨å¶å®çæ¶é´',
    priority             integer         not null            comment 'ä¼åçº§',
    state                varchar(16)     not null            comment 'ç¶æ',
    job_name             varchar(200)    null                comment 'ä»»å¡åç§°',
    job_group            varchar(200)    null                comment 'ä»»å¡ç»å',
    is_nonconcurrent     varchar(1)      null                comment 'æ¯å¦å¹¶å',
    requests_recovery    varchar(1)      null                comment 'æ¯å¦æ¥åæ¢å¤æ§è¡',
    primary key (sched_name, entry_id)
) engine=innodb comment = 'å·²è§¦åçè§¦åå¨è¡¨';

-- ----------------------------
-- 9ã å­å¨å°éçæå³ Scheduler çç¶æä¿¡æ¯ï¼åå¦æ¯ç¨äºéç¾¤ä¸­ï¼å¯ä»¥çå°å¶ä»ç Scheduler å®ä¾
-- ---------------------------- 
create table QRTZ_SCHEDULER_STATE (
    sched_name           varchar(120)    not null            comment 'è°åº¦åç§°',
    instance_name        varchar(200)    not null            comment 'å®ä¾åç§°',
    last_checkin_time    bigint(13)      not null            comment 'ä¸æ¬¡æ£æ¥æ¶é´',
    checkin_interval     bigint(13)      not null            comment 'æ£æ¥é´éæ¶é´',
    primary key (sched_name, instance_name)
) engine=innodb comment = 'è°åº¦å¨ç¶æè¡¨';

-- ----------------------------
-- 10ã å­å¨ç¨åºçæ²è§éçä¿¡æ¯(åå¦ä½¿ç¨äºæ²è§é)
-- ---------------------------- 
create table QRTZ_LOCKS (
    sched_name           varchar(120)    not null            comment 'è°åº¦åç§°',
    lock_name            varchar(40)     not null            comment 'æ²è§éåç§°',
    primary key (sched_name, lock_name)
) engine=innodb comment = 'å­å¨çæ²è§éä¿¡æ¯è¡¨';

-- ----------------------------
-- 11ã Quartzéç¾¤å®ç°åæ­¥æºå¶çè¡éè¡¨
-- ---------------------------- 
create table QRTZ_SIMPROP_TRIGGERS (
    sched_name           varchar(120)    not null            comment 'è°åº¦åç§°',
    trigger_name         varchar(200)    not null            comment 'qrtz_triggersè¡¨trigger_nameçå¤é®',
    trigger_group        varchar(200)    not null            comment 'qrtz_triggersè¡¨trigger_groupçå¤é®',
    str_prop_1           varchar(512)    null                comment 'Stringç±»åçtriggerçç¬¬ä¸ä¸ªåæ°',
    str_prop_2           varchar(512)    null                comment 'Stringç±»åçtriggerçç¬¬äºä¸ªåæ°',
    str_prop_3           varchar(512)    null                comment 'Stringç±»åçtriggerçç¬¬ä¸ä¸ªåæ°',
    int_prop_1           int             null                comment 'intç±»åçtriggerçç¬¬ä¸ä¸ªåæ°',
    int_prop_2           int             null                comment 'intç±»åçtriggerçç¬¬äºä¸ªåæ°',
    long_prop_1          bigint          null                comment 'longç±»åçtriggerçç¬¬ä¸ä¸ªåæ°',
    long_prop_2          bigint          null                comment 'longç±»åçtriggerçç¬¬äºä¸ªåæ°',
    dec_prop_1           numeric(13,4)   null                comment 'decimalç±»åçtriggerçç¬¬ä¸ä¸ªåæ°',
    dec_prop_2           numeric(13,4)   null                comment 'decimalç±»åçtriggerçç¬¬äºä¸ªåæ°',
    bool_prop_1          varchar(1)      null                comment 'Booleanç±»åçtriggerçç¬¬ä¸ä¸ªåæ°',
    bool_prop_2          varchar(1)      null                comment 'Booleanç±»åçtriggerçç¬¬äºä¸ªåæ°',
    primary key (sched_name, trigger_name, trigger_group),
    foreign key (sched_name, trigger_name, trigger_group) references QRTZ_TRIGGERS(sched_name, trigger_name, trigger_group)
) engine=innodb comment = 'åæ­¥æºå¶çè¡éè¡¨';

commit;

-- Business module missing tables (deduplicated)
-- This script only contains tables missing from sql/ry_20250522.sql
-- Safe to run multiple times.

CREATE TABLE IF NOT EXISTS md_customer (
  customer_id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Customer primary key',
  customer_code VARCHAR(64) NOT NULL COMMENT 'Customer code',
  customer_name VARCHAR(128) NOT NULL COMMENT 'Customer name',
  contact_person VARCHAR(64) DEFAULT NULL COMMENT 'Contact person',
  contact_phone VARCHAR(32) DEFAULT NULL COMMENT 'Contact phone',
  contact_email VARCHAR(128) DEFAULT NULL COMMENT 'Contact email',
  address VARCHAR(255) DEFAULT NULL COMMENT 'Address',
  credit_limit DECIMAL(18,2) DEFAULT 0 COMMENT 'Credit limit',
  payment_method VARCHAR(30) DEFAULT 'cash' COMMENT 'Payment method',
  customer_level VARCHAR(30) DEFAULT 'normal' COMMENT 'Customer level',
  status CHAR(1) DEFAULT '0' COMMENT 'Status',
  del_flag CHAR(1) DEFAULT '0' COMMENT 'Delete flag',
  create_by VARCHAR(64) DEFAULT '' COMMENT 'Created by',
  create_time DATETIME DEFAULT NULL COMMENT 'Created time',
  update_by VARCHAR(64) DEFAULT '' COMMENT 'Updated by',
  update_time DATETIME DEFAULT NULL COMMENT 'Updated time',
  remark VARCHAR(255) DEFAULT NULL COMMENT 'Remark',
  PRIMARY KEY (customer_id),
  UNIQUE KEY uk_md_customer_code (customer_code)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='Customer master';

CREATE TABLE IF NOT EXISTS wms_inbound (
  inbound_id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Inbound primary key',
  inbound_no VARCHAR(32) NOT NULL COMMENT 'Inbound number',
  inbound_type VARCHAR(20) DEFAULT NULL COMMENT 'Inbound type',
  purchase_order_id BIGINT(20) DEFAULT NULL COMMENT 'Source purchase order id',
  supplier_id BIGINT(20) DEFAULT NULL COMMENT 'Supplier id',
  warehouse_id BIGINT(20) DEFAULT NULL COMMENT 'Warehouse id',
  total_qty DECIMAL(18,2) DEFAULT 0 COMMENT 'Total quantity',
  total_amount DECIMAL(18,2) DEFAULT 0 COMMENT 'Total amount',
  status VARCHAR(20) DEFAULT 'draft' COMMENT 'Status',
  audit_by VARCHAR(64) DEFAULT NULL COMMENT 'Auditor',
  audit_time DATETIME DEFAULT NULL COMMENT 'Audit time',
  create_by VARCHAR(64) DEFAULT '' COMMENT 'Created by',
  create_time DATETIME DEFAULT NULL COMMENT 'Created time',
  update_by VARCHAR(64) DEFAULT '' COMMENT 'Updated by',
  update_time DATETIME DEFAULT NULL COMMENT 'Updated time',
  remark VARCHAR(255) DEFAULT NULL COMMENT 'Remark',
  PRIMARY KEY (inbound_id),
  UNIQUE KEY uk_wms_inbound_no (inbound_no),
  KEY idx_wms_inbound_purchase_order_id (purchase_order_id),
  KEY idx_wms_inbound_supplier_id (supplier_id),
  KEY idx_wms_inbound_warehouse_id (warehouse_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='Inbound order';

CREATE TABLE IF NOT EXISTS wms_inbound_item (
  inbound_item_id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Inbound item primary key',
  inbound_id BIGINT(20) NOT NULL COMMENT 'Inbound id',
  purchase_order_item_id BIGINT(20) DEFAULT NULL COMMENT 'Source purchase order item id',
  product_id BIGINT(20) NOT NULL COMMENT 'Product id',
  location_id BIGINT(20) DEFAULT NULL COMMENT 'Location id',
  batch_no VARCHAR(64) DEFAULT NULL COMMENT 'Batch no',
  quantity DECIMAL(18,2) DEFAULT 0 COMMENT 'Quantity',
  price DECIMAL(18,2) DEFAULT 0 COMMENT 'Price',
  amount DECIMAL(18,2) DEFAULT 0 COMMENT 'Amount',
  PRIMARY KEY (inbound_item_id),
  KEY idx_wms_inbound_item_inbound_id (inbound_id),
  KEY idx_wms_inbound_item_purchase_order_item_id (purchase_order_item_id),
  KEY idx_wms_inbound_item_product_id (product_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='Inbound order item';

CREATE TABLE IF NOT EXISTS wms_outbound (
  outbound_id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Outbound primary key',
  outbound_no VARCHAR(32) NOT NULL COMMENT 'Outbound number',
  outbound_type VARCHAR(20) DEFAULT NULL COMMENT 'Outbound type',
  customer_id BIGINT(20) DEFAULT NULL COMMENT 'Customer id',
  sale_order_id BIGINT(20) DEFAULT NULL COMMENT 'Sale order id',
  sale_order_no VARCHAR(32) DEFAULT NULL COMMENT 'Sale order no',
  warehouse_id BIGINT(20) DEFAULT NULL COMMENT 'Warehouse id',
  total_qty DECIMAL(18,2) DEFAULT 0 COMMENT 'Total quantity',
  total_amount DECIMAL(18,2) DEFAULT 0 COMMENT 'Total amount',
  status VARCHAR(20) DEFAULT 'draft' COMMENT 'Status',
  audit_by VARCHAR(64) DEFAULT NULL COMMENT 'Auditor',
  audit_time DATETIME DEFAULT NULL COMMENT 'Audit time',
  platform_type VARCHAR(20) DEFAULT NULL COMMENT 'Platform type',
  store_id BIGINT(20) DEFAULT NULL COMMENT 'Store id',
  source_order_no VARCHAR(64) DEFAULT NULL COMMENT 'Source order number',
  carrier VARCHAR(64) DEFAULT NULL COMMENT 'Carrier',
  waybill_no VARCHAR(64) DEFAULT NULL COMMENT 'Waybill number',
  freight_cost DECIMAL(18,2) DEFAULT 0 COMMENT 'Freight cost',
  delivery_status VARCHAR(20) DEFAULT NULL COMMENT 'Delivery status',
  create_by VARCHAR(64) DEFAULT '' COMMENT 'Created by',
  create_time DATETIME DEFAULT NULL COMMENT 'Created time',
  update_by VARCHAR(64) DEFAULT '' COMMENT 'Updated by',
  update_time DATETIME DEFAULT NULL COMMENT 'Updated time',
  remark VARCHAR(255) DEFAULT NULL COMMENT 'Remark',
  PRIMARY KEY (outbound_id),
  UNIQUE KEY uk_wms_outbound_no (outbound_no),
  KEY idx_wms_outbound_customer_id (customer_id),
  KEY idx_wms_outbound_sale_order_id (sale_order_id),
  KEY idx_wms_outbound_warehouse_id (warehouse_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='Outbound order';

CREATE TABLE IF NOT EXISTS wms_outbound_item (
  outbound_item_id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Outbound item primary key',
  outbound_id BIGINT(20) NOT NULL COMMENT 'Outbound id',
  sale_order_item_id BIGINT(20) DEFAULT NULL COMMENT 'Sale order item id',
  product_id BIGINT(20) NOT NULL COMMENT 'Product id',
  location_id BIGINT(20) DEFAULT NULL COMMENT 'Location id',
  batch_no VARCHAR(64) DEFAULT NULL COMMENT 'Batch no',
  quantity DECIMAL(18,2) DEFAULT 0 COMMENT 'Quantity',
  price DECIMAL(18,2) DEFAULT 0 COMMENT 'Price',
  amount DECIMAL(18,2) DEFAULT 0 COMMENT 'Amount',
  PRIMARY KEY (outbound_item_id),
  KEY idx_wms_outbound_item_outbound_id (outbound_id),
  KEY idx_wms_outbound_item_sale_order_item_id (sale_order_item_id),
  KEY idx_wms_outbound_item_product_id (product_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='Outbound order item';

CREATE TABLE IF NOT EXISTS wms_purchase_return (
  purchase_return_id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Purchase return primary key',
  return_no VARCHAR(32) NOT NULL COMMENT 'Return number',
  return_type VARCHAR(20) DEFAULT NULL COMMENT 'Return type',
  supplier_id BIGINT(20) DEFAULT NULL COMMENT 'Supplier id',
  warehouse_id BIGINT(20) DEFAULT NULL COMMENT 'Warehouse id',
  total_qty DECIMAL(18,2) DEFAULT 0 COMMENT 'Total quantity',
  total_amount DECIMAL(18,2) DEFAULT 0 COMMENT 'Total amount',
  status VARCHAR(20) DEFAULT 'draft' COMMENT 'Status',
  audit_by VARCHAR(64) DEFAULT NULL COMMENT 'Auditor',
  audit_time DATETIME DEFAULT NULL COMMENT 'Audit time',
  create_by VARCHAR(64) DEFAULT '' COMMENT 'Created by',
  create_time DATETIME DEFAULT NULL COMMENT 'Created time',
  update_by VARCHAR(64) DEFAULT '' COMMENT 'Updated by',
  update_time DATETIME DEFAULT NULL COMMENT 'Updated time',
  remark VARCHAR(255) DEFAULT NULL COMMENT 'Remark',
  PRIMARY KEY (purchase_return_id),
  UNIQUE KEY uk_wms_purchase_return_no (return_no),
  KEY idx_wms_purchase_return_supplier_id (supplier_id),
  KEY idx_wms_purchase_return_warehouse_id (warehouse_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='Purchase return order';

CREATE TABLE IF NOT EXISTS wms_purchase_return_item (
  purchase_return_item_id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Purchase return item primary key',
  purchase_return_id BIGINT(20) NOT NULL COMMENT 'Purchase return id',
  product_id BIGINT(20) NOT NULL COMMENT 'Product id',
  quantity DECIMAL(18,2) DEFAULT 0 COMMENT 'Quantity',
  price DECIMAL(18,2) DEFAULT 0 COMMENT 'Price',
  amount DECIMAL(18,2) DEFAULT 0 COMMENT 'Amount',
  PRIMARY KEY (purchase_return_item_id),
  KEY idx_wms_purchase_return_item_return_id (purchase_return_id),
  KEY idx_wms_purchase_return_item_product_id (product_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='Purchase return order item';

CREATE TABLE IF NOT EXISTS wms_stock (
  stock_id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Stock primary key',
  warehouse_id BIGINT(20) NOT NULL COMMENT 'Warehouse id',
  product_id BIGINT(20) NOT NULL COMMENT 'Product id',
  location_id BIGINT(20) DEFAULT NULL COMMENT 'Location id',
  batch_no VARCHAR(64) DEFAULT NULL COMMENT 'Batch no',
  quantity DECIMAL(18,2) DEFAULT 0 COMMENT 'Quantity',
  locked_quantity DECIMAL(18,2) DEFAULT 0 COMMENT 'Locked quantity',
  frozen_quantity DECIMAL(18,2) DEFAULT 0 COMMENT 'Frozen quantity',
  warning_min_qty DECIMAL(18,2) DEFAULT NULL COMMENT 'Minimum warning quantity',
  warning_max_qty DECIMAL(18,2) DEFAULT NULL COMMENT 'Maximum warning quantity',
  version INT(11) DEFAULT 0 COMMENT 'Version',
  create_by VARCHAR(64) DEFAULT '' COMMENT 'Created by',
  create_time DATETIME DEFAULT NULL COMMENT 'Created time',
  update_by VARCHAR(64) DEFAULT '' COMMENT 'Updated by',
  update_time DATETIME DEFAULT NULL COMMENT 'Updated time',
  PRIMARY KEY (stock_id),
  UNIQUE KEY uk_wms_stock_key (warehouse_id, product_id, location_id, batch_no),
  KEY idx_wms_stock_product_id (product_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='Stock ledger';

CREATE TABLE IF NOT EXISTS wms_stock_log (
  stock_log_id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'Stock log primary key',
  warehouse_id BIGINT(20) NOT NULL COMMENT 'Warehouse id',
  product_id BIGINT(20) NOT NULL COMMENT 'Product id',
  location_id BIGINT(20) DEFAULT NULL COMMENT 'Location id',
  batch_no VARCHAR(64) DEFAULT NULL COMMENT 'Batch no',
  bill_type VARCHAR(30) DEFAULT NULL COMMENT 'Bill type',
  bill_id BIGINT(20) DEFAULT NULL COMMENT 'Bill id',
  in_out VARCHAR(10) NOT NULL COMMENT 'In or out',
  quantity DECIMAL(18,2) DEFAULT 0 COMMENT 'Quantity',
  price DECIMAL(18,2) DEFAULT 0 COMMENT 'Price',
  amount DECIMAL(18,2) DEFAULT 0 COMMENT 'Amount',
  before_qty DECIMAL(18,2) DEFAULT 0 COMMENT 'Before quantity',
  after_qty DECIMAL(18,2) DEFAULT 0 COMMENT 'After quantity',
  create_by VARCHAR(64) DEFAULT '' COMMENT 'Created by',
  create_time DATETIME DEFAULT NULL COMMENT 'Created time',
  PRIMARY KEY (stock_log_id),
  KEY idx_wms_stock_log_stock_key (warehouse_id, product_id, location_id, batch_no),
  KEY idx_wms_stock_log_bill (bill_type, bill_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='Stock change log';

-- ============================================================
-- ä¸å¡æ ·ä¾æ°æ®ï¼å¯éå¤æ§è¡ï¼
-- ============================================================

delete from wms_stock_log where stock_log_id in (990001, 990002, 990003, 990004, 990005, 990006, 990007, 990008, 990009, 990010);
delete from wms_stock where stock_id in (990001, 990002, 990003, 990004, 990005);
delete from wms_purchase_return_item where purchase_return_item_id in (990001, 990002, 990003, 990004, 990005);
delete from wms_purchase_return where purchase_return_id in (990001, 990002, 990003, 990004, 990005);
delete from wms_outbound_item where outbound_item_id in (990001, 990002, 990003, 990004, 990005);
delete from wms_outbound where outbound_id in (990001, 990002, 990003, 990004, 990005);
delete from wms_inbound_item where inbound_item_id in (990001, 990002, 990003, 990004, 990005);
delete from wms_inbound where inbound_id in (990001, 990002, 990003, 990004, 990005);
delete from md_location where location_id in (990001, 990002, 990003, 990004, 990005);
delete from md_product where product_id in (990001, 990002, 990003, 990004, 990005);
delete from md_warehouse where warehouse_id in (990001, 990002, 990003, 990004, 990005);
delete from md_customer where customer_id in (990001, 990002, 990003, 990004, 990005);

insert into md_customer
(customer_id, customer_code, customer_name, contact_person, contact_phone, contact_email, address, credit_limit, payment_method, customer_level, status, del_flag, create_by, create_time, update_by, update_time, remark)
values
(990001, 'cust_demo_001', 'æ ·ä¾å®¢æ·ä¸', 'å¼ æ', '13800000001', 'demo001@example.com', 'æ·±å³å¸åå±±åºç§æå­1å·', 30000.00, 'bank', 'a', '0', '0', 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾å®¢æ·'),
(990002, 'cust_demo_002', 'æ ·ä¾å®¢æ·äº', 'æå¼º', '13800000002', 'demo002@example.com', 'æ·±å³å¸ç¦ç°åºæ·±åå¤§é2å·', 25000.00, 'cash', 'a', '0', '0', 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾å®¢æ·'),
(990003, 'cust_demo_003', 'æ ·ä¾å®¢æ·ä¸', 'çè³', '13800000003', 'demo003@example.com', 'æ·±å³å¸å®å®åºåä¸ä¸è·¯3å·', 20000.00, 'month', 'b', '0', '0', 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾å®¢æ·'),
(990004, 'cust_demo_004', 'æ ·ä¾å®¢æ·å', 'èµµé·', '13800000004', 'demo004@example.com', 'ä¸èå¸åååºé¸¿ç¦è·¯4å·', 18000.00, 'bank', 'b', '0', '0', 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾å®¢æ·'),
(990005, 'cust_demo_005', 'æ ·ä¾å®¢æ·äº', 'å¨æ', '13800000005', 'demo005@example.com', 'å¹¿å·å¸å¤©æ²³åºä½è²è¥¿è·¯5å·', 22000.00, 'cash', 'c', '0', '0', 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾å®¢æ·');

insert into md_warehouse
(warehouse_id, warehouse_code, warehouse_name, contact_name, contact_phone, address, status, del_flag, create_by, create_time, update_by, update_time, remark)
values
(990001, 'wh_demo_001', 'ååä¸å·ä»', 'éæ°', '13900000001', 'æ·±å³å¸å®å®åºä»å¨è·¯1å·', '0', '0', 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾ä»åº'),
(990002, 'wh_demo_002', 'ååäºå·ä»', 'åæ´', '13900000002', 'æ·±å³å¸é¾å²åºç©æµè·¯2å·', '0', '0', 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾ä»åº'),
(990003, 'wh_demo_003', 'ä¸èä¸­è½¬ä»', 'ä½å', '13900000003', 'ä¸èå¸å¯®æ­¥éä»å¨è·¯3å·', '0', '0', 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾ä»åº'),
(990004, 'wh_demo_004', 'å¹¿å·åç½®ä»', 'æ¨å¸', '13900000004', 'å¹¿å·å¸ç½äºåºæºåºè·¯4å·', '0', '0', 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾ä»åº'),
(990005, 'wh_demo_005', 'ä½å±±å¤è´§ä»', 'å­æ¶', '13900000005', 'ä½å±±å¸åæµ·åºæ¡åè·¯5å·', '0', '0', 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾ä»åº');

insert into md_location
(location_id, warehouse_id, location_code, location_name, status, del_flag, create_by, create_time, update_by, update_time, remark)
values
(990001, 990001, 'loc_demo_001', 'a-01-01', '0', '0', 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾åºä½'),
(990002, 990002, 'loc_demo_002', 'a-01-02', '0', '0', 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾åºä½'),
(990003, 990003, 'loc_demo_003', 'b-01-01', '0', '0', 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾åºä½'),
(990004, 990004, 'loc_demo_004', 'b-02-01', '0', '0', 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾åºä½'),
(990005, 990005, 'loc_demo_005', 'c-01-01', '0', '0', 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾åºä½');

insert into md_product
(product_id, product_code, product_name, product_spec, unit_name, bar_code, category_name, brand_name, product_image, cost_price, sale_price, status, del_flag, create_by, create_time, update_by, update_time, remark)
values
(990001, 'sku_demo_001', 'æ ·ä¾ç¿æ³æ°´', '550ml', 'ç¶', '6900000000010', 'é¥®æ', 'æ¸æ³', null, 2.50, 4.00, '0', '0', 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾åå'),
(990002, 'sku_demo_002', 'æ ·ä¾çº¢è¶', '500ml', 'ç¶', '6900000000027', 'é¥®æ', 'è¶è¯­', null, 3.20, 5.50, '0', '0', 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾åå'),
(990003, 'sku_demo_003', 'æ ·ä¾çº¸å·¾', '3å±*10å', 'æ', '6900000000034', 'æ¥ç¨å', 'æ´æ', null, 12.00, 18.00, '0', '0', 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾åå'),
(990004, 'sku_demo_004', 'æ ·ä¾æ´è¡£æ¶²', '2kg', 'æ¡¶', '6900000000041', 'æ¥å', 'åå½©', null, 22.00, 35.00, '0', '0', 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾åå'),
(990005, 'sku_demo_005', 'æ ·ä¾é¥¼å¹²', '320g', 'ç', '6900000000058', 'é£å', 'éº¦é¦', null, 6.50, 10.00, '0', '0', 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾åå');

insert into wms_inbound
(inbound_id, inbound_no, inbound_type, supplier_id, warehouse_id, total_qty, total_amount, status, audit_by, audit_time, create_by, create_time, update_by, update_time, remark)
values
(990001, 'in202603190001', 'purchase', null, 990001, 120.00, 300.00, 'audited', 'admin', now(), 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾å¥åºå'),
(990002, 'in202603190002', 'purchase', null, 990002, 90.00, 288.00, 'audited', 'admin', now(), 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾å¥åºå'),
(990003, 'in202603190003', 'purchase', null, 990003, 60.00, 720.00, 'audited', 'admin', now(), 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾å¥åºå'),
(990004, 'in202603190004', 'purchase', null, 990004, 45.00, 990.00, 'audited', 'admin', now(), 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾å¥åºå'),
(990005, 'in202603190005', 'purchase', null, 990005, 150.00, 975.00, 'audited', 'admin', now(), 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾å¥åºå');

insert into wms_inbound_item
(inbound_item_id, inbound_id, purchase_order_item_id, product_id, location_id, batch_no, quantity, price, amount)
values
(990001, 990001, null, 990001, 990001, 'batch-in-001', 120.00, 2.50, 300.00),
(990002, 990002, null, 990002, 990002, 'batch-in-002', 90.00, 3.20, 288.00),
(990003, 990003, null, 990003, 990003, 'batch-in-003', 60.00, 12.00, 720.00),
(990004, 990004, null, 990004, 990004, 'batch-in-004', 45.00, 22.00, 990.00),
(990005, 990005, null, 990005, 990005, 'batch-in-005', 150.00, 6.50, 975.00);

insert into wms_outbound
(outbound_id, outbound_no, outbound_type, customer_id, sale_order_id, sale_order_no, warehouse_id, total_qty, total_amount, status, audit_by, audit_time, platform_type, store_id, source_order_no, carrier, waybill_no, freight_cost, delivery_status, create_by, create_time, update_by, update_time, remark)
values
(990001, 'out202603190001', 'sale', 990001, null, null, 990001, 30.00, 120.00, 'audited', 'admin', now(), 'online', 1, 'so-demo-001', 'é¡ºä¸°', 'sf1234567801cn', 10.00, 'delivered', 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾åºåºå'),
(990002, 'out202603190002', 'sale', 990002, null, null, 990002, 20.00, 110.00, 'audited', 'admin', now(), 'online', 2, 'so-demo-002', 'äº¬ä¸', 'jd1234567802cn', 8.00, 'delivered', 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾åºåºå'),
(990003, 'out202603190003', 'sale', 990003, null, null, 990003, 10.00, 180.00, 'audited', 'admin', now(), 'offline', 3, 'so-demo-003', 'éµè¾¾', 'yd1234567803cn', 6.00, 'delivered', 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾åºåºå'),
(990004, 'out202603190004', 'sale', 990004, null, null, 990004, 8.00, 280.00, 'audited', 'admin', now(), 'offline', 4, 'so-demo-004', 'ä¸­é', 'zt1234567804cn', 5.00, 'delivered', 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾åºåºå'),
(990005, 'out202603190005', 'sale', 990005, null, null, 990005, 25.00, 250.00, 'audited', 'admin', now(), 'online', 5, 'so-demo-005', 'åé', 'yt1234567805cn', 9.00, 'delivered', 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾åºåºå');

insert into wms_outbound_item
(outbound_item_id, outbound_id, sale_order_item_id, product_id, location_id, batch_no, quantity, price, amount)
values
(990001, 990001, null, 990001, 990001, 'batch-in-001', 30.00, 4.00, 120.00),
(990002, 990002, null, 990002, 990002, 'batch-in-002', 20.00, 5.50, 110.00),
(990003, 990003, null, 990003, 990003, 'batch-in-003', 10.00, 18.00, 180.00),
(990004, 990004, null, 990004, 990004, 'batch-in-004', 8.00, 35.00, 280.00),
(990005, 990005, null, 990005, 990005, 'batch-in-005', 25.00, 10.00, 250.00);

insert into wms_purchase_return
(purchase_return_id, return_no, return_type, supplier_id, warehouse_id, total_qty, total_amount, status, audit_by, audit_time, create_by, create_time, update_by, update_time, remark)
values
(990001, 'pr202603190001', 'purchase_return', null, 990001, 5.00, 12.50, 'audited', 'admin', now(), 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾éè´­éè´§å'),
(990002, 'pr202603190002', 'purchase_return', null, 990002, 3.00, 9.60, 'audited', 'admin', now(), 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾éè´­éè´§å'),
(990003, 'pr202603190003', 'purchase_return', null, 990003, 2.00, 24.00, 'audited', 'admin', now(), 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾éè´­éè´§å'),
(990004, 'pr202603190004', 'purchase_return', null, 990004, 1.00, 22.00, 'audited', 'admin', now(), 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾éè´­éè´§å'),
(990005, 'pr202603190005', 'purchase_return', null, 990005, 4.00, 26.00, 'audited', 'admin', now(), 'admin', now(), 'admin', now(), 'ä¸å¡æ ·ä¾éè´­éè´§å');

insert into wms_purchase_return_item
(purchase_return_item_id, purchase_return_id, product_id, quantity, price, amount)
values
(990001, 990001, 990001, 5.00, 2.50, 12.50),
(990002, 990002, 990002, 3.00, 3.20, 9.60),
(990003, 990003, 990003, 2.00, 12.00, 24.00),
(990004, 990004, 990004, 1.00, 22.00, 22.00),
(990005, 990005, 990005, 4.00, 6.50, 26.00);

insert into wms_stock
(stock_id, warehouse_id, product_id, location_id, batch_no, quantity, locked_quantity, frozen_quantity, warning_min_qty, warning_max_qty, version, create_by, create_time, update_by, update_time)
values
(990001, 990001, 990001, 990001, 'batch-in-001', 85.00, 3.00, 0.00, 10.00, 500.00, 1, 'admin', now(), 'admin', now()),
(990002, 990002, 990002, 990002, 'batch-in-002', 67.00, 2.00, 0.00, 8.00, 400.00, 1, 'admin', now(), 'admin', now()),
(990003, 990003, 990003, 990003, 'batch-in-003', 48.00, 1.00, 0.00, 6.00, 300.00, 1, 'admin', now(), 'admin', now()),
(990004, 990004, 990004, 990004, 'batch-in-004', 36.00, 1.00, 0.00, 4.00, 200.00, 1, 'admin', now(), 'admin', now()),
(990005, 990005, 990005, 990005, 'batch-in-005', 121.00, 5.00, 0.00, 12.00, 600.00, 1, 'admin', now(), 'admin', now());

insert into wms_stock_log
(stock_log_id, warehouse_id, product_id, location_id, batch_no, bill_type, bill_id, in_out, quantity, price, amount, before_qty, after_qty, create_by, create_time)
values
(990001, 990001, 990001, 990001, 'batch-in-001', 'inbound', 990001, 'in', 120.00, 2.50, 300.00, 0.00, 120.00, 'admin', now()),
(990002, 990001, 990001, 990001, 'batch-in-001', 'outbound', 990001, 'out', 30.00, 4.00, 120.00, 120.00, 90.00, 'admin', now()),
(990003, 990001, 990001, 990001, 'batch-in-001', 'purchase_return', 990001, 'out', 5.00, 2.50, 12.50, 90.00, 85.00, 'admin', now()),
(990004, 990002, 990002, 990002, 'batch-in-002', 'inbound', 990002, 'in', 90.00, 3.20, 288.00, 0.00, 90.00, 'admin', now()),
(990005, 990002, 990002, 990002, 'batch-in-002', 'outbound', 990002, 'out', 20.00, 5.50, 110.00, 90.00, 70.00, 'admin', now()),
(990006, 990002, 990002, 990002, 'batch-in-002', 'purchase_return', 990002, 'out', 3.00, 3.20, 9.60, 70.00, 67.00, 'admin', now()),
(990007, 990003, 990003, 990003, 'batch-in-003', 'inbound', 990003, 'in', 60.00, 12.00, 720.00, 0.00, 60.00, 'admin', now()),
(990008, 990003, 990003, 990003, 'batch-in-003', 'outbound', 990003, 'out', 10.00, 18.00, 180.00, 60.00, 50.00, 'admin', now()),
(990009, 990004, 990004, 990004, 'batch-in-004', 'inbound', 990004, 'in', 45.00, 22.00, 990.00, 0.00, 45.00, 'admin', now()),
(990010, 990005, 990005, 990005, 'batch-in-005', 'inbound', 990005, 'in', 150.00, 6.50, 975.00, 0.00, 150.00, 'admin', now());

-- 创建供应商表
create table if not exists `md_supplier` (
  `supplier_id` bigint(20) not null auto_increment comment '供应商ID',
  `supplier_code` varchar(64) not null comment '供应商编码',
  `supplier_name` varchar(100) not null comment '供应商名称',
  `contact_person` varchar(50) default null comment '联系人',
  `contact_phone` varchar(20) default null comment '联系电话',
  `contact_email` varchar(50) default null comment '联系邮箱',
  `address` varchar(200) default null comment '地址',
  `status` char(1) default '0' comment '状态（0正常 1停用）',
  `del_flag` char(1) default '0' comment '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) default '' comment '创建者',
  `create_time` datetime default null comment '创建时间',
  `update_by` varchar(64) default '' comment '更新者',
  `update_time` datetime default null comment '更新时间',
  `remark` varchar(500) default null comment '备注',
  primary key (`supplier_id`)
) engine=innodb auto_increment=100 default charset=utf8mb4 comment='供应商表';

-- 插入供应商菜单和按钮权限
insert into `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
values('供应商管理', 2000, 2, 'supplier', 'business/supplier/index', 1, 0, 'c', '0', '0', 'business:supplier:list', 'peoples', 'admin', sysdate(), '', null, '供应商菜单');

select @parentId := last_insert_id();

insert into `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
values
('供应商查询', @parentId, 1, '#', '', 1, 0, 'f', '0', '0', 'business:supplier:query', '#', 'admin', sysdate(), '', null, ''),
('供应商新增', @parentId, 2, '#', '', 1, 0, 'f', '0', '0', 'business:supplier:add', '#', 'admin', sysdate(), '', null, ''),
('供应商修改', @parentId, 3, '#', '', 1, 0, 'f', '0', '0', 'business:supplier:edit', '#', 'admin', sysdate(), '', null, ''),
('供应商删除', @parentId, 4, '#', '', 1, 0, 'f', '0', '0', 'business:supplier:remove', '#', 'admin', sysdate(), '', null, ''),
('供应商导出', @parentId, 5, '#', '', 1, 0, 'f', '0', '0', 'business:supplier:export', '#', 'admin', sysdate(), '', null, '');

-- 创建采购订单表
create table if not exists `wms_purchase_order` (
  `purchase_order_id` bigint(20) not null auto_increment comment '采购订单ID',
  `order_no` varchar(64) not null comment '采购单号',
  `supplier_id` bigint(20) not null comment '供应商ID',
  `purchase_date` datetime default null comment '采购日期',
  `total_amount` decimal(10,2) default '0.00' comment '总金额',
  `status` char(1) default '0' comment '状态（0待审核 1已审核 2部分入库 3已完成 4已作废）',
  `remark` varchar(500) default null comment '备注',
  `create_by` varchar(64) default '' comment '创建者',
  `create_time` datetime default null comment '创建时间',
  `update_by` varchar(64) default '' comment '更新者',
  `update_time` datetime default null comment '更新时间',
  primary key (`purchase_order_id`)
) engine=innodb auto_increment=100 default charset=utf8mb4 comment='采购订单表';

-- 创建采购订单明细表
create table if not exists `wms_purchase_order_item` (
  `purchase_order_item_id` bigint(20) not null auto_increment comment '明细ID',
  `purchase_order_id` bigint(20) not null comment '采购订单ID',
  `product_id` bigint(20) not null comment '商品ID',
  `quantity` decimal(10,2) not null comment '采购数量',
  `price` decimal(10,2) not null comment '单价',
  `amount` decimal(10,2) not null comment '金额',
  `remark` varchar(500) default null comment '备注',
  primary key (`purchase_order_item_id`)
) engine=innodb auto_increment=100 default charset=utf8mb4 comment='采购订单明细表';

-- 插入采购订单菜单和按钮权限
insert into `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
values('采购订单', 3025, 1, 'purchaseOrder', 'business/purchaseOrder/index', 1, 0, 'c', '0', '0', 'business:purchaseOrder:list', 'shopping', 'admin', sysdate(), '', null, '采购订单菜单');

select @parentId := last_insert_id();

insert into `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
values
('采购订单查询', @parentId, 1, '#', '', 1, 0, 'f', '0', '0', 'business:purchaseOrder:query', '#', 'admin', sysdate(), '', null, ''),
('采购订单新增', @parentId, 2, '#', '', 1, 0, 'f', '0', '0', 'business:purchaseOrder:add', '#', 'admin', sysdate(), '', null, ''),
('采购订单修改', @parentId, 3, '#', '', 1, 0, 'f', '0', '0', 'business:purchaseOrder:edit', '#', 'admin', sysdate(), '', null, ''),
('采购订单删除', @parentId, 4, '#', '', 1, 0, 'f', '0', '0', 'business:purchaseOrder:remove', '#', 'admin', sysdate(), '', null, ''),
('采购订单审核', @parentId, 5, '#', '', 1, 0, 'f', '0', '0', 'business:purchaseOrder:audit', '#', 'admin', sysdate(), '', null, ''),
('采购订单导出', @parentId, 6, '#', '', 1, 0, 'f', '0', '0', 'business:purchaseOrder:export', '#', 'admin', sysdate(), '', null, '');

-- 创建应付账款表
create table if not exists `fin_payable` (
  `payable_id` bigint(20) not null auto_increment comment '应付账款ID',
  `purchase_order_id` bigint(20) not null comment '采购订单ID',
  `supplier_id` bigint(20) not null comment '供应商ID',
  `amount_due` decimal(10,2) not null default '0.00' comment '应付金额',
  `amount_paid` decimal(10,2) not null default '0.00' comment '已付金额',
  `remain_amount` decimal(10,2) not null default '0.00' comment '未付金额',
  `status` char(1) default '0' comment '状态（0未付款 1部分付款 2已付款）',
  `due_date` datetime default null comment '应付日期',
  `overdue_days` int(11) default '0' comment '逾期天数',
  `remark` varchar(500) default null comment '备注',
  `create_by` varchar(64) default '' comment '创建者',
  `create_time` datetime default null comment '创建时间',
  `update_by` varchar(64) default '' comment '更新者',
  `update_time` datetime default null comment '更新时间',
  primary key (`payable_id`)
) engine=innodb auto_increment=100 default charset=utf8mb4 comment='应付账款表';

-- 创建付款记录表
create table if not exists `fin_payment` (
  `payment_id` bigint(20) not null auto_increment comment '付款记录ID',
  `payable_id` bigint(20) not null comment '应付账款ID',
  `purchase_order_id` bigint(20) not null comment '采购订单ID',
  `supplier_id` bigint(20) not null comment '供应商ID',
  `amount` decimal(10,2) not null default '0.00' comment '付款金额',
  `payment_time` datetime default null comment '付款时间',
  `payment_method` varchar(50) default null comment '付款方式',
  `remark` varchar(500) default null comment '备注',
  `create_by` varchar(64) default '' comment '创建者',
  `create_time` datetime default null comment '创建时间',
  `update_by` varchar(64) default '' comment '更新者',
  `update_time` datetime default null comment '更新时间',
  primary key (`payment_id`)
) engine=innodb auto_increment=100 default charset=utf8mb4 comment='付款记录表';

-- 插入应付和付款菜单
insert into `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
values('应付台账', 3028, 3, 'payable', 'business/payable/index', 1, 0, 'c', '0', '0', 'business:payable:list', 'money', 'admin', sysdate(), '', null, '应付台账菜单');

select @parentId := last_insert_id();

insert into `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
values
('应付台账查询', @parentId, 1, '#', '', 1, 0, 'f', '0', '0', 'business:payable:query', '#', 'admin', sysdate(), '', null, ''),
('应付台账新增', @parentId, 2, '#', '', 1, 0, 'f', '0', '0', 'business:payable:add', '#', 'admin', sysdate(), '', null, ''),
('应付台账修改', @parentId, 3, '#', '', 1, 0, 'f', '0', '0', 'business:payable:edit', '#', 'admin', sysdate(), '', null, ''),
('应付台账删除', @parentId, 4, '#', '', 1, 0, 'f', '0', '0', 'business:payable:remove', '#', 'admin', sysdate(), '', null, ''),
('应付台账导出', @parentId, 5, '#', '', 1, 0, 'f', '0', '0', 'business:payable:export', '#', 'admin', sysdate(), '', null, '');

insert into `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
values('付款流水', 3028, 4, 'payment', 'business/payment/index', 1, 0, 'c', '0', '0', 'business:payment:list', 'money', 'admin', sysdate(), '', null, '付款流水菜单');

select @paymentId := last_insert_id();

insert into `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
values
('付款流水查询', @paymentId, 1, '#', '', 1, 0, 'f', '0', '0', 'business:payment:query', '#', 'admin', sysdate(), '', null, ''),
('付款流水新增', @paymentId, 2, '#', '', 1, 0, 'f', '0', '0', 'business:payment:add', '#', 'admin', sysdate(), '', null, ''),
('付款流水修改', @paymentId, 3, '#', '', 1, 0, 'f', '0', '0', 'business:payment:edit', '#', 'admin', sysdate(), '', null, ''),
('付款流水删除', @paymentId, 4, '#', '', 1, 0, 'f', '0', '0', 'business:payment:remove', '#', 'admin', sysdate(), '', null, ''),
('付款流水导出', @paymentId, 5, '#', '', 1, 0, 'f', '0', '0', 'business:payment:export', '#', 'admin', sysdate(), '', null, '');

-- 创建库存调拨主表
create table if not exists `wms_transfer` (
  `transfer_id` bigint(20) not null auto_increment comment '调拨单ID',
  `transfer_no` varchar(64) not null comment '调拨单号',
  `out_warehouse_id` bigint(20) not null comment '调出仓库ID',
  `in_warehouse_id` bigint(20) not null comment '调入仓库ID',
  `transfer_date` datetime default null comment '调拨日期',
  `status` char(1) default '0' comment '状态（0待审核 1已审核 2已作废）',
  `total_quantity` decimal(10,2) default '0.00' comment '总数量',
  `remark` varchar(500) default null comment '备注',
  `create_by` varchar(64) default '' comment '创建者',
  `create_time` datetime default null comment '创建时间',
  `update_by` varchar(64) default '' comment '更新者',
  `update_time` datetime default null comment '更新时间',
  primary key (`transfer_id`)
) engine=innodb auto_increment=100 default charset=utf8mb4 comment='库存调拨表';

-- 创建库存调拨明细表
create table if not exists `wms_transfer_item` (
  `transfer_item_id` bigint(20) not null auto_increment comment '明细ID',
  `transfer_id` bigint(20) not null comment '调拨单ID',
  `product_id` bigint(20) not null comment '商品ID',
  `quantity` decimal(10,2) not null comment '调拨数量',
  `remark` varchar(500) default null comment '备注',
  primary key (`transfer_item_id`)
) engine=innodb auto_increment=100 default charset=utf8mb4 comment='库存调拨明细表';

-- 插入库存调拨菜单和按钮权限
insert into `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
values('库存调拨', 2001, 5, 'transfer', 'business/transfer/index', 1, 0, 'c', '0', '0', 'business:transfer:list', 'guide', 'admin', sysdate(), '', null, '库存调拨菜单');

select @transferId := last_insert_id();

insert into `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
values
('调拨单查询', @transferId, 1, '#', '', 1, 0, 'f', '0', '0', 'business:transfer:query', '#', 'admin', sysdate(), '', null, ''),
('调拨单新增', @transferId, 2, '#', '', 1, 0, 'f', '0', '0', 'business:transfer:add', '#', 'admin', sysdate(), '', null, ''),
('调拨单修改', @transferId, 3, '#', '', 1, 0, 'f', '0', '0', 'business:transfer:edit', '#', 'admin', sysdate(), '', null, ''),
('调拨单删除', @transferId, 4, '#', '', 1, 0, 'f', '0', '0', 'business:transfer:remove', '#', 'admin', sysdate(), '', null, ''),
('调拨单审核', @transferId, 5, '#', '', 1, 0, 'f', '0', '0', 'business:transfer:audit', '#', 'admin', sysdate(), '', null, ''),
('调拨单导出', @transferId, 6, '#', '', 1, 0, 'f', '0', '0', 'business:transfer:export', '#', 'admin', sysdate(), '', null, '');

-- 创建客户跟进记录表
create table if not exists `crm_customer_follow` (
  `follow_id` bigint(20) not null auto_increment comment '跟进记录ID',
  `customer_id` bigint(20) not null comment '客户ID',
  `follow_type` varchar(50) default null comment '跟进方式（如电话、上门、微信等）',
  `follow_time` datetime default null comment '跟进时间',
  `follow_content` varchar(1000) not null comment '跟进内容',
  `next_follow_time` datetime default null comment '下次跟进时间',
  `status` char(1) default '0' comment '跟进状态（0进行中 1已转化 2已流失）',
  `create_by` varchar(64) default '' comment '创建者（跟进人）',
  `create_time` datetime default null comment '创建时间',
  `update_by` varchar(64) default '' comment '更新者',
  `update_time` datetime default null comment '更新时间',
  primary key (`follow_id`)
) engine=innodb auto_increment=100 default charset=utf8mb4 comment='客户跟进记录表';

-- 插入客户跟进菜单和按钮权限
insert into `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
values('客户跟进', 2002, 2, 'customerFollow', 'business/customerFollow/index', 1, 0, 'c', '0', '0', 'business:customerFollow:list', 'user', 'admin', sysdate(), '', null, '客户跟进菜单');

select @followId := last_insert_id();

insert into `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
values
('跟进记录查询', @followId, 1, '#', '', 1, 0, 'f', '0', '0', 'business:customerFollow:query', '#', 'admin', sysdate(), '', null, ''),
('跟进记录新增', @followId, 2, '#', '', 1, 0, 'f', '0', '0', 'business:customerFollow:add', '#', 'admin', sysdate(), '', null, ''),
('跟进记录修改', @followId, 3, '#', '', 1, 0, 'f', '0', '0', 'business:customerFollow:edit', '#', 'admin', sysdate(), '', null, ''),
('跟进记录删除', @followId, 4, '#', '', 1, 0, 'f', '0', '0', 'business:customerFollow:remove', '#', 'admin', sysdate(), '', null, ''),
('跟进记录导出', @followId, 5, '#', '', 1, 0, 'f', '0', '0', 'business:customerFollow:export', '#', 'admin', sysdate(), '', null, '');
