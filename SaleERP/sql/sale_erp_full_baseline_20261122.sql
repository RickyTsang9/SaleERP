-- =========================================================
-- SaleERP (销售管理系统) 全量数据库正式投产基线脚本
-- 导出日期: 2026-11-22 (含生产级上线 16 项 + 第一/二/三批全量闭环 28 项升级与当月经营基线)
-- 包含: 系统表、主数据、进销存、资金流水(fin_account_journal)、发票回写、成本快照、月结与当月闭环演示数据
-- =========================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for biz_data_backup
-- ----------------------------
DROP TABLE IF EXISTS `biz_data_backup`;
CREATE TABLE `biz_data_backup` (
  `backup_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'å¤ä»½ä¸»é®',
  `backup_name` varchar(100) NOT NULL COMMENT 'å¤ä»½åç§°',
  `backup_type` varchar(20) NOT NULL COMMENT 'å¤ä»½ç±»åï¼manual/autoï¼',
  `backup_status` varchar(20) DEFAULT 'success' COMMENT 'å¤ä»½ç¶æï¼success/failedï¼',
  `table_count` int DEFAULT '0' COMMENT 'è¡¨æ°é',
  `record_count` bigint DEFAULT '0' COMMENT 'è®°å½æ°é',
  `file_path` varchar(255) DEFAULT NULL COMMENT '磁盘备份压缩包相对路径',
  `file_size` bigint DEFAULT '0' COMMENT '备份文件字节大小',
  `backup_content` longtext COMMENT 'å¤ä»½åå®¹',
  `restore_status` varchar(20) DEFAULT NULL COMMENT 'æ¢å¤ç¶æï¼success/failedï¼',
  `restore_by` varchar(64) DEFAULT '' COMMENT 'æ¢å¤äºº',
  `restore_time` datetime DEFAULT NULL COMMENT 'æ¢å¤æ¶é´',
  `create_by` varchar(64) DEFAULT '' COMMENT 'åå»ºè',
  `create_time` datetime DEFAULT NULL COMMENT 'åå»ºæ¶é´',
  `update_by` varchar(64) DEFAULT '' COMMENT 'æ´æ°è',
  `update_time` datetime DEFAULT NULL COMMENT 'æ´æ°æ¶é´',
  `remark` varchar(255) DEFAULT NULL COMMENT 'å¤æ³¨',
  PRIMARY KEY (`backup_id`),
  KEY `idx_biz_data_backup_type` (`backup_type`),
  KEY `idx_biz_data_backup_status` (`backup_status`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ä¸å¡æ°æ®å¤ä»½è¡¨';

-- ----------------------------
-- Records of biz_data_backup
-- ----------------------------
INSERT INTO `biz_data_backup` (`backup_id`, `backup_name`, `backup_type`, `backup_status`, `table_count`, `record_count`, `file_path`, `file_size`, `backup_content`, `restore_status`, `restore_by`, `restore_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, 'manual_20261122174122', 'manual', 'success', 40, 173, 'manual_20261122174122.json.gz', 9427, '{"format":"gzip-json-v2","fileName":"manual_20261122174122.json.gz","tableRowSummary":{"biz_decision_budget_plan":1,"biz_executive_action_item":2,"biz_executive_brief_record":1,"biz_message":0,"biz_message_read":0,"biz_operation_snapshot":1,"crm_customer_follow":3,"fin_account":3,"fin_expense":0,"fin_invoice":4,"fin_payable":2,"fin_payment":1,"fin_period_close":1,"fin_receipt":2,"fin_receivable":3,"md_customer":11,"md_customer_price":2,"md_location":5,"md_product":10,"md_supplier":5,"md_warehouse":5,"wms_inbound":7,"wms_inbound_item":7,"wms_inventory_check":1,"wms_inventory_check_item":1,"wms_outbound":7,"wms_outbound_item":7,"wms_purchase_order":2,"wms_purchase_order_item":2,"wms_purchase_return":5,"wms_purchase_return_item":5,"wms_sale_order":9,"wms_sale_order_item":9,"wms_sale_order_status_history":25,"wms_sale_return":1,"wms_sale_return_item":1,"wms_stock":10,"wms_stock_log":10,"wms_transfer":1,"wms_transfer_item":1}}', NULL, '', NULL, 'admin', '2026-11-22 17:41:20', 'admin', NULL, NULL);

-- ----------------------------
-- Table structure for biz_decision_budget_plan
-- ----------------------------
DROP TABLE IF EXISTS `biz_decision_budget_plan`;
CREATE TABLE `biz_decision_budget_plan` (
  `plan_id` bigint NOT NULL AUTO_INCREMENT COMMENT '预算计划id',
  `budget_year` int NOT NULL COMMENT '预算年度',
  `plan_name` varchar(100) NOT NULL COMMENT '预算计划名称',
  `version_no` int NOT NULL DEFAULT '1' COMMENT '版本号',
  `version_label` varchar(50) DEFAULT 'v1' COMMENT '版本标签',
  `base_plan_id` bigint DEFAULT NULL COMMENT '基线计划id',
  `effective_flag` char(1) NOT NULL DEFAULT 'n' COMMENT '是否为当前生效版本',
  `adjustment_reason` varchar(500) DEFAULT NULL COMMENT '调整原因',
  `plan_status` varchar(20) NOT NULL DEFAULT 'draft' COMMENT '计划状态',
  `sale_budget_amount` decimal(16,2) DEFAULT '0.00' COMMENT '年度销售预算',
  `gross_profit_budget_amount` decimal(16,2) DEFAULT '0.00' COMMENT '年度毛利预算',
  `collection_budget_amount` decimal(16,2) DEFAULT '0.00' COMMENT '年度回款预算',
  `purchase_budget_amount` decimal(16,2) DEFAULT '0.00' COMMENT '年度采购预算',
  `net_cash_budget_amount` decimal(16,2) DEFAULT '0.00' COMMENT '年度净现金流预算',
  `sale_monthly_plan_text` text COMMENT '销售月度预算json',
  `gross_profit_monthly_plan_text` text COMMENT '毛利月度预算json',
  `collection_monthly_plan_text` text COMMENT '回款月度预算json',
  `purchase_monthly_plan_text` text COMMENT '采购月度预算json',
  `net_cash_monthly_plan_text` text COMMENT '净现金流月度预算json',
  `submit_by` varchar(64) DEFAULT '' COMMENT '提交人',
  `submit_time` datetime DEFAULT NULL COMMENT '提交时间',
  `approve_by` varchar(64) DEFAULT '' COMMENT '审批人',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `approve_remark` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`plan_id`),
  KEY `idx_budget_year_version_no` (`budget_year`,`version_no`),
  KEY `idx_budget_year_effective_flag` (`budget_year`,`effective_flag`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理层年度预算计划';

-- ----------------------------
-- Records of biz_decision_budget_plan
-- ----------------------------
INSERT INTO `biz_decision_budget_plan` (`plan_id`, `budget_year`, `plan_name`, `version_no`, `version_label`, `base_plan_id`, `effective_flag`, `adjustment_reason`, `plan_status`, `sale_budget_amount`, `gross_profit_budget_amount`, `collection_budget_amount`, `purchase_budget_amount`, `net_cash_budget_amount`, `sale_monthly_plan_text`, `gross_profit_monthly_plan_text`, `collection_monthly_plan_text`, `purchase_monthly_plan_text`, `net_cash_monthly_plan_text`, `submit_by`, `submit_time`, `approve_by`, `approve_time`, `approve_remark`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (1, 2026, '2026年度销售与资金经营预算（V1基线）', 1, 'V1', NULL, 'y', NULL, 'approved', 4800000.00, 960000.00, 4200000.00, 3200000.00, 1000000.00, '[350000,350000,380000,400000,400000,420000,400000,400000,420000,420000,430000,430000]', '[70000,70000,76000,80000,80000,84000,80000,80000,84000,84000,86000,86000]', '[300000,300000,330000,350000,350000,370000,350000,350000,370000,370000,380000,380000]', '[240000,240000,260000,270000,270000,280000,260000,260000,280000,280000,280000,280000]', '[60000,60000,70000,80000,80000,90000,90000,90000,90000,90000,100000,100000]', 'admin', '2026-11-22 11:30:38', 'admin', '2026-11-22 11:30:38', '年度经营预算评审通过并生效', '2026年度正式基线预算', 'admin', '2026-11-22 11:30:38', '', NULL);

-- ----------------------------
-- Table structure for biz_executive_action_item
-- ----------------------------
DROP TABLE IF EXISTS `biz_executive_action_item`;
CREATE TABLE `biz_executive_action_item` (
  `action_item_id` bigint NOT NULL AUTO_INCREMENT COMMENT '事项id',
  `brief_id` bigint DEFAULT NULL COMMENT '简报id',
  `brief_title_snapshot` varchar(100) DEFAULT NULL COMMENT '简报标题快照',
  `action_title` varchar(255) NOT NULL COMMENT '决议事项',
  `owner_name` varchar(64) DEFAULT '' COMMENT '负责人',
  `due_date` datetime DEFAULT NULL COMMENT '到期日期',
  `action_status` varchar(20) NOT NULL DEFAULT 'todo' COMMENT '事项状态',
  `priority_level` varchar(20) NOT NULL DEFAULT 'medium' COMMENT '优先级',
  `progress_remark` varchar(500) DEFAULT NULL COMMENT '进度备注',
  `completed_time` datetime DEFAULT NULL COMMENT '完成时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`action_item_id`),
  KEY `idx_action_status_due_date` (`action_status`,`due_date`),
  KEY `idx_brief_id` (`brief_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理层经营决议事项';

-- ----------------------------
-- Records of biz_executive_action_item
-- ----------------------------
INSERT INTO `biz_executive_action_item` (`action_item_id`, `brief_id`, `brief_title_snapshot`, `action_title`, `owner_name`, `due_date`, `action_status`, `priority_level`, `progress_remark`, `completed_time`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (1, 1, 'SaleERP 阶段经营复盘简报', '专项跟进广州智联网络科技 ￥71,992.00 逾期应收回款', '张经理', '2026-11-25 00:00:00', 'in_progress', 'high', '已电话联系客户财务，预计本周五前安排对公转账', NULL, '销售与财务联合跟进', 'admin', '2026-11-22 11:30:38', '', NULL);
INSERT INTO `biz_executive_action_item` (`action_item_id`, `brief_id`, `brief_title_snapshot`, `action_title`, `owner_name`, `due_date`, `action_status`, `priority_level`, `progress_remark`, `completed_time`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (2, 1, 'SaleERP 阶段经营复盘简报', '推进华为 Mate 60 Pro 及 MacBook Air M2 采购入库补货', '陈主管', '2026-11-27 00:00:00', 'todo', 'medium', '待采购订单审核通过后安排华南一号仓/二号仓收货上架', NULL, '保障热销机型不断货', 'admin', '2026-11-22 11:30:38', '', NULL);

-- ----------------------------
-- Table structure for biz_executive_brief_record
-- ----------------------------
DROP TABLE IF EXISTS `biz_executive_brief_record`;
CREATE TABLE `biz_executive_brief_record` (
  `brief_id` bigint NOT NULL AUTO_INCREMENT COMMENT '简报id',
  `brief_title` varchar(100) NOT NULL COMMENT '简报标题',
  `brief_date` datetime DEFAULT NULL COMMENT '简报日期',
  `brief_status` varchar(20) NOT NULL DEFAULT 'archived' COMMENT '简报状态',
  `generated_time` datetime DEFAULT NULL COMMENT '生成时间',
  `summary_content` text COMMENT '经营结论',
  `highlight_content` text COMMENT '亮点内容json',
  `risk_content` text COMMENT '风险内容json',
  `action_content` text COMMENT '建议决策json',
  `plain_text_content` longtext COMMENT '纯文本简报',
  `source_mode` varchar(20) DEFAULT 'dashboard' COMMENT '生成来源',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`brief_id`),
  KEY `idx_brief_date` (`brief_date`),
  KEY `idx_biz_executive_brief_record_brief_date_brief_id` (`brief_date`,`brief_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理层经营简报归档';

-- ----------------------------
-- Records of biz_executive_brief_record
-- ----------------------------
INSERT INTO `biz_executive_brief_record` (`brief_id`, `brief_title`, `brief_date`, `brief_status`, `generated_time`, `summary_content`, `highlight_content`, `risk_content`, `action_content`, `plain_text_content`, `source_mode`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (1, 'SaleERP 阶段经营复盘简报', '2026-11-22 00:00:00', 'archived', '2026-11-22 11:30:38', '当前累计已审销售总额 ￥340,447.00，已收款 ￥89,990.00，整体毛利结构健康，但逾期应收与重点热销机型低库存需要立即协同跟进。', '["头部客户北京华数科技与上海天创商贸贡献稳定复购","手机通讯与电脑办公双品类毛利率保持良好水平"]', '["广州智联网络科技存在 ￥71,992.00 逾期应收待催收","华为 Mate 60 Pro 与 MacBook Air M2 库存低于安全下限"]', '["销售与财务联动于本周内落实逾期回款计划","采购部加速推进 PO20231024001/002 审核与入库上架"]', '【SaleERP 阶段经营复盘简报】\n一、经营概览：累计已审销售 ￥340,447.00，应收 ￥186,977.00，已收 ￥89,990.00。\n二、核心风险：1) 逾期应收 ￥71,992.00；2) 2项热销商品低于安全库存下限。\n三、决议建议：优先跟进催收并加快采购入库。', 'dashboard', '系统初始化示例简报', 'admin', '2026-11-22 11:30:38', '', NULL);

-- ----------------------------
-- Table structure for biz_message
-- ----------------------------
DROP TABLE IF EXISTS `biz_message`;
CREATE TABLE `biz_message` (
  `message_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'æ¶æ¯ä¸»é®',
  `message_type` varchar(64) NOT NULL COMMENT 'æ¶æ¯ç±»å',
  `message_title` varchar(200) NOT NULL COMMENT 'æ¶æ¯æ é¢',
  `message_content` varchar(1000) DEFAULT '' COMMENT 'æ¶æ¯åå®¹',
  `message_level` varchar(20) DEFAULT 'info' COMMENT 'æ¶æ¯çº§å«',
  `business_type` varchar(64) DEFAULT NULL COMMENT 'ä¸å¡ç±»å',
  `business_id` bigint DEFAULT NULL COMMENT 'ä¸å¡ä¸»é®',
  `status` char(1) DEFAULT '0' COMMENT 'ç¶æï¼0æ­£å¸¸ 1å³é­ï¼',
  `create_by` varchar(64) DEFAULT '' COMMENT 'åå»ºè',
  `create_time` datetime DEFAULT NULL COMMENT 'åå»ºæ¶é´',
  `update_by` varchar(64) DEFAULT '' COMMENT 'æ´æ°è',
  `update_time` datetime DEFAULT NULL COMMENT 'æ´æ°æ¶é´',
  `remark` varchar(255) DEFAULT NULL COMMENT 'å¤æ³¨',
  PRIMARY KEY (`message_id`),
  KEY `idx_biz_message_type` (`message_type`),
  KEY `idx_biz_message_status` (`status`),
  KEY `idx_biz_message_status_type_id` (`status`,`message_type`,`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ä¸å¡æ¶æ¯è¡¨';

-- ----------------------------
-- Table structure for biz_message_read
-- ----------------------------
DROP TABLE IF EXISTS `biz_message_read`;
CREATE TABLE `biz_message_read` (
  `read_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'å·²è¯»ä¸»é®',
  `message_id` bigint NOT NULL COMMENT 'æ¶æ¯ä¸»é®',
  `user_id` bigint NOT NULL COMMENT 'ç¨æ·ä¸»é®',
  `read_time` datetime DEFAULT NULL COMMENT 'å·²è¯»æ¶é´',
  `create_time` datetime DEFAULT NULL COMMENT 'åå»ºæ¶é´',
  PRIMARY KEY (`read_id`),
  UNIQUE KEY `uk_biz_message_read` (`message_id`,`user_id`),
  KEY `idx_biz_message_read_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ä¸å¡æ¶æ¯å·²è¯»è¡¨';

-- ----------------------------
-- Table structure for biz_operation_snapshot
-- ----------------------------
DROP TABLE IF EXISTS `biz_operation_snapshot`;
CREATE TABLE `biz_operation_snapshot` (
  `snapshot_id` bigint NOT NULL AUTO_INCREMENT COMMENT '快照id',
  `snapshot_title` varchar(100) NOT NULL COMMENT '快照标题',
  `snapshot_date` datetime DEFAULT NULL COMMENT '快照日期',
  `snapshot_type` varchar(20) NOT NULL DEFAULT 'weekly' COMMENT '快照类型',
  `sale_amount` decimal(16,2) DEFAULT '0.00' COMMENT '销售额',
  `gross_profit_amount` decimal(16,2) DEFAULT '0.00' COMMENT '毛利额',
  `collection_rate` decimal(10,2) DEFAULT '0.00' COMMENT '回款率',
  `overdue_count` int DEFAULT '0' COMMENT '逾期笔数',
  `stock_warning_count` int DEFAULT '0' COMMENT '库存预警数',
  `pending_action_count` int DEFAULT '0' COMMENT '待办事项数',
  `summary_content` text COMMENT '经营结论',
  `plain_text_content` longtext COMMENT '快照纯文本',
  `source_mode` varchar(20) DEFAULT 'dashboard' COMMENT '生成来源',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`snapshot_id`),
  KEY `idx_snapshot_date` (`snapshot_date`),
  KEY `idx_snapshot_type` (`snapshot_type`),
  KEY `idx_biz_operation_snapshot_snapshot_date_snapshot_id` (`snapshot_date`,`snapshot_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理层经营快照';

-- ----------------------------
-- Records of biz_operation_snapshot
-- ----------------------------
INSERT INTO `biz_operation_snapshot` (`snapshot_id`, `snapshot_title`, `snapshot_date`, `snapshot_type`, `sale_amount`, `gross_profit_amount`, `collection_rate`, `overdue_count`, `stock_warning_count`, `pending_action_count`, `summary_content`, `plain_text_content`, `source_mode`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (1, '2026阶段经营基线快照', '2026-11-22 00:00:00', 'weekly', 340447.00, 46447.00, 48.13, 1, 3, 2, '阶段基线经营快照：销售稳步推进，重点控制应收逾期与安全库存水位。', '快照日期：当前周期 | 销售额：￥340,447.00 | 回款率：48.13% | 逾期应收：1笔 | 库存预警：3项', 'dashboard', '阶段对比基线', 'admin', '2026-11-22 11:30:38', '', NULL);

-- ----------------------------
-- Table structure for crm_customer_follow
-- ----------------------------
DROP TABLE IF EXISTS `crm_customer_follow`;
CREATE TABLE `crm_customer_follow` (
  `follow_id` bigint NOT NULL AUTO_INCREMENT COMMENT '跟进记录ID',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `follow_type` varchar(50) DEFAULT NULL COMMENT '跟进方式（如电话、上门、微信等）',
  `follow_time` datetime DEFAULT NULL COMMENT '跟进时间',
  `follow_content` varchar(1000) NOT NULL COMMENT '跟进内容',
  `next_follow_time` datetime DEFAULT NULL COMMENT '下次跟进时间',
  `status` char(1) DEFAULT '0' COMMENT '跟进状态（0进行中 1已转化 2已流失）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者(跟进人)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`follow_id`),
  KEY `idx_crm_customer_follow_customer_id` (`customer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1004 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户跟进记录表';

-- ----------------------------
-- Records of crm_customer_follow
-- ----------------------------
INSERT INTO `crm_customer_follow` (`follow_id`, `customer_id`, `follow_type`, `follow_time`, `follow_content`, `next_follow_time`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (1001, 990020, 'phone', '2026-11-20 10:50:53', '沟通逾期应收账款 ￥71,992.00 回款安排，客户承诺本周五前安排财务打款。', '2026-11-24 00:00:00', '0', 'admin', '2026-11-22 10:50:53', '', NULL);
INSERT INTO `crm_customer_follow` (`follow_id`, `customer_id`, `follow_type`, `follow_time`, `follow_content`, `next_follow_time`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (1002, 990018, 'visit', '2026-11-17 10:50:53', '上门拜访张经理，确认下季度终端设备集采框架与交付批次。', '2026-11-22 00:00:00', '0', 'admin', '2026-11-22 10:50:53', '', NULL);
INSERT INTO `crm_customer_follow` (`follow_id`, `customer_id`, `follow_type`, `follow_time`, `follow_content`, `next_follow_time`, `status`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (1003, 990019, 'wechat', '2026-11-15 10:50:53', '确认订单 SO20231026001 到货签收情况，客户反馈设备运行良好。', NULL, '1', 'admin', '2026-11-22 10:50:53', '', NULL);

-- ----------------------------
-- Table structure for fin_account
-- ----------------------------
DROP TABLE IF EXISTS `fin_account`;
CREATE TABLE `fin_account` (
  `account_id` bigint NOT NULL AUTO_INCREMENT COMMENT '账户主键',
  `account_code` varchar(32) NOT NULL COMMENT '账户编码',
  `account_name` varchar(100) NOT NULL COMMENT '账户名称',
  `account_type` varchar(20) NOT NULL DEFAULT 'bank' COMMENT '账户类型(bank银行户 wechat微信商户 alipay支付宝 cash现金)',
  `bank_name` varchar(100) DEFAULT NULL COMMENT '开户行/平台名称',
  `bank_no` varchar(64) DEFAULT NULL COMMENT '银行账号/商户号',
  `opening_balance` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '期初余额',
  `current_balance` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '当前实时余额',
  `is_default` char(1) NOT NULL DEFAULT 'N' COMMENT '是否默认结算户(Y/N)',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态(0正常 1停用)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`account_id`),
  UNIQUE KEY `uk_fin_account_code` (`account_code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资金结算账户表';

-- ----------------------------
-- Records of fin_account
-- ----------------------------
INSERT INTO `fin_account` (`account_id`, `account_code`, `account_name`, `account_type`, `bank_name`, `bank_no`, `opening_balance`, `current_balance`, `is_default`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (1, 'ACC-BANK-01', '招商银行基本存款户', 'bank', '招商银行深圳高新园支行', '755900188810901', 500000.00, 559990.00, 'Y', '0', '对公收付款主力基本户', 'admin', '2026-11-22 16:34:57', 'admin', '2026-11-22 22:34:13');
INSERT INTO `fin_account` (`account_id`, `account_code`, `account_name`, `account_type`, `bank_name`, `bank_no`, `opening_balance`, `current_balance`, `is_default`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (2, 'ACC-WX-01', '企业微信商户收款户', 'wechat', '微信支付商户平台', 'MCH-160889901', 50000.00, 50000.00, 'N', '0', '终端门店与小额扫码收款户', 'admin', '2026-11-22 16:34:57', 'admin', '2026-11-22 22:00:25');
INSERT INTO `fin_account` (`account_id`, `account_code`, `account_name`, `account_type`, `bank_name`, `bank_no`, `opening_balance`, `current_balance`, `is_default`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (3, 'ACC-CASH-01', '财务日常备用金现金户', 'cash', '总部财务保险柜', '-', 20000.00, 20000.00, 'N', '0', '零星采购与运费报销备用金', 'admin', '2026-11-22 16:34:57', '', NULL);

-- ----------------------------
-- Table structure for fin_account_journal
-- ----------------------------
DROP TABLE IF EXISTS `fin_account_journal`;
CREATE TABLE `fin_account_journal` (
  `journal_id` bigint NOT NULL AUTO_INCREMENT COMMENT '资金流水主键',
  `journal_no` varchar(64) NOT NULL COMMENT '资金流水号',
  `account_id` bigint NOT NULL COMMENT '资金账户ID',
  `account_name` varchar(100) DEFAULT NULL COMMENT '资金账户名称快照',
  `direction` varchar(16) NOT NULL COMMENT '收支方向(in收入 out支出)',
  `biz_type` varchar(32) NOT NULL COMMENT '业务类型(opening期初 receipt销售回款 receipt_void回款冲红 payment采购付款 payment_void付款冲红 sale_refund客户退款 purchase_refund供应商退款 transfer_in内部转入 transfer_out内部转出 other_income其他收入 other_expense日常费用)',
  `biz_id` bigint DEFAULT NULL COMMENT '关联业务主键',
  `biz_no` varchar(64) DEFAULT NULL COMMENT '关联业务单号',
  `target_name` varchar(128) DEFAULT NULL COMMENT '往来单位或对手账户',
  `amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '变动金额(正数)',
  `before_balance` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '变动前余额',
  `after_balance` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '变动后余额',
  `occur_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '业务发生时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '摘要备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '操作人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`journal_id`),
  UNIQUE KEY `uk_fin_account_journal_no` (`journal_no`),
  KEY `idx_fin_journal_account_time` (`account_id`,`occur_time`),
  KEY `idx_fin_journal_biz` (`biz_type`,`biz_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资金结算账户收支流水明细表';

-- ----------------------------
-- Records of fin_account_journal
-- ----------------------------
INSERT INTO `fin_account_journal` (`journal_id`, `journal_no`, `account_id`, `account_name`, `direction`, `biz_type`, `biz_id`, `biz_no`, `target_name`, `amount`, `before_balance`, `after_balance`, `occur_time`, `remark`, `create_by`, `create_time`) VALUES (1, 'ZJ-OPEN-1', 1, '招商银行基本存款户', 'in', 'opening', 1, 'ACC-BANK-01', '期初建账', 539990.00, 0.00, 539990.00, '2026-11-22 16:34:57', '系统账户初始化期初资金结余', 'admin', '2026-11-22 20:55:10');
INSERT INTO `fin_account_journal` (`journal_id`, `journal_no`, `account_id`, `account_name`, `direction`, `biz_type`, `biz_id`, `biz_no`, `target_name`, `amount`, `before_balance`, `after_balance`, `occur_time`, `remark`, `create_by`, `create_time`) VALUES (2, 'ZJ-OPEN-2', 2, '企业微信商户收款户', 'in', 'opening', 2, 'ACC-WX-01', '期初建账', 50000.00, 0.00, 50000.00, '2026-11-22 16:34:57', '系统账户初始化期初资金结余', 'admin', '2026-11-22 20:55:10');
INSERT INTO `fin_account_journal` (`journal_id`, `journal_no`, `account_id`, `account_name`, `direction`, `biz_type`, `biz_id`, `biz_no`, `target_name`, `amount`, `before_balance`, `after_balance`, `occur_time`, `remark`, `create_by`, `create_time`) VALUES (3, 'ZJ-OPEN-3', 3, '财务日常备用金现金户', 'in', 'opening', 3, 'ACC-CASH-01', '期初建账', 20000.00, 0.00, 20000.00, '2026-11-22 16:34:57', '系统账户初始化期初资金结余', 'admin', '2026-11-22 20:55:10');
INSERT INTO `fin_account_journal` (`journal_id`, `journal_no`, `account_id`, `account_name`, `direction`, `biz_type`, `biz_id`, `biz_no`, `target_name`, `amount`, `before_balance`, `after_balance`, `occur_time`, `remark`, `create_by`, `create_time`) VALUES (4, 'ZJ-RC-20261120-001', 1, '招商银行基本存款户', 'in', 'receipt', 3, 'SO20261118001', '北京华数科技有限公司', 50000.00, 539990.00, 589990.00, '2026-11-20 15:00:00', 'SO20261118001首期回款', 'admin', '2026-11-20 15:00:00');
INSERT INTO `fin_account_journal` (`journal_id`, `journal_no`, `account_id`, `account_name`, `direction`, `biz_type`, `biz_id`, `biz_no`, `target_name`, `amount`, `before_balance`, `after_balance`, `occur_time`, `remark`, `create_by`, `create_time`) VALUES (5, 'ZJ-PM-20261121-001', 1, '招商银行基本存款户', 'out', 'payment', 1002, 'PO20261115001', '深圳华强数码供应链有限公司', 30000.00, 589990.00, 559990.00, '2026-11-21 14:00:00', 'PO20261115001首期货款', 'admin', '2026-11-21 14:00:00');

-- ----------------------------
-- Table structure for fin_expense
-- ----------------------------
DROP TABLE IF EXISTS `fin_expense`;
CREATE TABLE `fin_expense` (
  `expense_id` bigint NOT NULL AUTO_INCREMENT COMMENT '璐圭敤id',
  `expense_no` varchar(64) NOT NULL COMMENT '璐圭敤鍗曞彿',
  `expense_type` varchar(64) DEFAULT NULL COMMENT '璐圭敤绫诲瀷',
  `amount` decimal(18,2) DEFAULT '0.00' COMMENT '璐圭敤閲戦',
  `expense_date` date DEFAULT NULL COMMENT '璐圭敤鏃ユ湡',
  `pay_channel` varchar(32) DEFAULT NULL COMMENT '鏀粯娓犻亾',
  `fund_account_id` bigint DEFAULT NULL COMMENT '璧勯噾璐︽埛id',
  `status` char(1) DEFAULT '0' COMMENT '鐘舵��',
  `create_by` varchar(64) DEFAULT '' COMMENT '鍒涘缓鑰�',
  `create_time` datetime DEFAULT NULL COMMENT '鍒涘缓鏃堕棿',
  `update_by` varchar(64) DEFAULT '' COMMENT '鏇存柊鑰�',
  `update_time` datetime DEFAULT NULL COMMENT '鏇存柊鏃堕棿',
  `remark` varchar(500) DEFAULT NULL COMMENT '澶囨敞',
  PRIMARY KEY (`expense_id`),
  UNIQUE KEY `uk_fin_expense_no` (`expense_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='璐圭敤鍗曡〃';

-- ----------------------------
-- Table structure for fin_invoice
-- ----------------------------
DROP TABLE IF EXISTS `fin_invoice`;
CREATE TABLE `fin_invoice` (
  `invoice_id` bigint NOT NULL AUTO_INCREMENT COMMENT '发票主键',
  `invoice_no` varchar(64) NOT NULL COMMENT '发票号码',
  `invoice_code` varchar(64) DEFAULT NULL COMMENT '发票代码',
  `invoice_direction` varchar(20) NOT NULL COMMENT '发票方向(sale销项 purchase进项)',
  `invoice_type` varchar(24) NOT NULL DEFAULT 'electronic' COMMENT '发票类型(special专票 normal普票 electronic数电票)',
  `source_order_id` bigint DEFAULT NULL COMMENT '关联销售单/采购单ID',
  `source_order_no` varchar(64) DEFAULT NULL COMMENT '关联业务单号',
  `target_id` bigint NOT NULL COMMENT '客户ID或供应商ID',
  `target_name` varchar(128) DEFAULT NULL COMMENT '往来单位名称快照',
  `tax_rate` decimal(5,2) NOT NULL DEFAULT '13.00' COMMENT '税率(%)',
  `total_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '价税合计金额',
  `amount_without_tax` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '不含税金额',
  `tax_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '税额',
  `invoice_date` date NOT NULL COMMENT '开票/收票日期',
  `status` varchar(20) NOT NULL DEFAULT 'issued' COMMENT '状态(draft草稿 issued已开具 red_void已红冲)',
  `void_reason` varchar(255) DEFAULT NULL COMMENT '红冲作废原因',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`invoice_id`),
  UNIQUE KEY `uk_fin_invoice_no` (`invoice_no`),
  KEY `idx_fin_invoice_dir_date` (`invoice_direction`,`invoice_date`),
  KEY `idx_fin_invoice_source` (`invoice_direction`,`source_order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='财务销项与进项发票台账表';

-- ----------------------------
-- Records of fin_invoice
-- ----------------------------
INSERT INTO `fin_invoice` (`invoice_id`, `invoice_no`, `invoice_code`, `invoice_direction`, `invoice_type`, `source_order_id`, `source_order_no`, `target_id`, `target_name`, `tax_rate`, `total_amount`, `amount_without_tax`, `tax_amount`, `invoice_date`, `status`, `void_reason`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (1, '2631200000010001', '031002600111', 'sale', 'electronic', 1, 'SO20231020001', 990018, '北京华数科技有限公司', 13.00, 79990.00, 70787.61, 9202.39, '2026-11-05', 'issued', NULL, '首批iPhone与配件销项数电专票', 'admin', '2026-11-22 16:34:57', '', '2026-11-22 20:55:10');
INSERT INTO `fin_invoice` (`invoice_id`, `invoice_no`, `invoice_code`, `invoice_direction`, `invoice_type`, `source_order_id`, `source_order_no`, `target_id`, `target_name`, `tax_rate`, `total_amount`, `amount_without_tax`, `tax_amount`, `invoice_date`, `status`, `void_reason`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (2, '2631200000010002', '031002600111', 'sale', 'special', 2, 'SO20231021001', 990019, '上海天创商贸有限公司', 13.00, 34995.00, 30969.03, 4025.97, '2026-11-08', 'issued', NULL, '天创商贸首批办公设备专票', 'admin', '2026-11-22 16:34:57', '', '2026-11-22 20:55:10');
INSERT INTO `fin_invoice` (`invoice_id`, `invoice_no`, `invoice_code`, `invoice_direction`, `invoice_type`, `source_order_id`, `source_order_no`, `target_id`, `target_name`, `tax_rate`, `total_amount`, `amount_without_tax`, `tax_amount`, `invoice_date`, `status`, `void_reason`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (3, '2644200000020001', '044002600211', 'purchase', 'special', 102, 'PO20231024001', 1, '深圳华强数码供应链有限公司', 13.00, 124000.00, 109734.51, 14265.49, '2026-11-02', 'issued', NULL, '华强供应链进项抵扣专票', 'admin', '2026-11-22 16:34:57', '', '2026-11-22 20:55:10');
INSERT INTO `fin_invoice` (`invoice_id`, `invoice_no`, `invoice_code`, `invoice_direction`, `invoice_type`, `source_order_id`, `source_order_no`, `target_id`, `target_name`, `tax_rate`, `total_amount`, `amount_without_tax`, `tax_amount`, `invoice_date`, `status`, `void_reason`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (4, '2644200000020002', '044002600211', 'purchase', 'electronic', 103, 'PO20231024002', 2, '上海智汇信息设备有限公司', 9.00, 75000.00, 68807.34, 6192.66, '2026-11-10', 'issued', NULL, '智汇设备及运输服务混合进项票', 'admin', '2026-11-22 16:34:57', '', '2026-11-22 20:55:10');
INSERT INTO `fin_invoice` (`invoice_id`, `invoice_no`, `invoice_code`, `invoice_direction`, `invoice_type`, `source_order_id`, `source_order_no`, `target_id`, `target_name`, `tax_rate`, `total_amount`, `amount_without_tax`, `tax_amount`, `invoice_date`, `status`, `void_reason`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (5, '26442000000111500001', '', 'purchase', 'special', 104, 'PO20261115001', 1, '深圳华强数码供应链有限公司', 13.00, 54400.00, 48141.59, 6258.41, '2026-11-16', 'issued', NULL, '2026年11月采购进项专票', 'admin', '2026-11-16 14:00:00', 'admin', '2026-11-16 14:00:00');
INSERT INTO `fin_invoice` (`invoice_id`, `invoice_no`, `invoice_code`, `invoice_direction`, `invoice_type`, `source_order_id`, `source_order_no`, `target_id`, `target_name`, `tax_rate`, `total_amount`, `amount_without_tax`, `tax_amount`, `invoice_date`, `status`, `void_reason`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (6, '26112000000111900002', '', 'sale', 'electronic', 10, 'SO20261118001', 990018, '北京华数科技有限公司', 13.00, 79990.00, 70787.61, 9202.39, '2026-11-19', 'issued', NULL, '2026年11月销项全电数电票', 'admin', '2026-11-19 16:00:00', 'admin', '2026-11-19 16:00:00');

-- ----------------------------
-- Table structure for fin_payable
-- ----------------------------
DROP TABLE IF EXISTS `fin_payable`;
CREATE TABLE `fin_payable` (
  `payable_id` bigint NOT NULL AUTO_INCREMENT COMMENT '应付账款ID',
  `purchase_order_id` bigint NOT NULL COMMENT '采购订单ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `amount_due` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '应付金额',
  `amount_paid` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '已付金额',
  `remain_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '未付金额',
  `status` char(1) DEFAULT '0' COMMENT '状态（0未付款 1部分付款 2已付款）',
  `due_date` datetime DEFAULT NULL COMMENT '应付日期',
  `overdue_days` int DEFAULT '0' COMMENT '逾期天数',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `amount` decimal(18,2) DEFAULT '0.00' COMMENT '搴斾粯閲戦',
  `paid_amount` decimal(18,2) DEFAULT '0.00' COMMENT '宸蹭粯閲戦',
  PRIMARY KEY (`payable_id`),
  KEY `idx_fin_payable_supplier_status_payable_id` (`supplier_id`,`status`,`payable_id`),
  KEY `idx_fin_payable_purchase_order_id` (`purchase_order_id`),
  KEY `idx_fin_payable_supplier_status_due_date` (`supplier_id`,`status`,`due_date`)
) ENGINE=InnoDB AUTO_INCREMENT=1004 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='应付账款表';

-- ----------------------------
-- Records of fin_payable
-- ----------------------------
INSERT INTO `fin_payable` (`payable_id`, `purchase_order_id`, `supplier_id`, `amount_due`, `amount_paid`, `remain_amount`, `status`, `due_date`, `overdue_days`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `amount`, `paid_amount`) VALUES (1001, 102, 1, 124000.00, 50000.00, 74000.00, '1', '2026-11-27 00:00:00', 0, 'Mate 60 Pro 采购首批结算', 'admin', '2026-11-22 10:50:53', '', NULL, 124000.00, 50000.00);
INSERT INTO `fin_payable` (`payable_id`, `purchase_order_id`, `supplier_id`, `amount_due`, `amount_paid`, `remain_amount`, `status`, `due_date`, `overdue_days`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `amount`, `paid_amount`) VALUES (1002, 103, 2, 75000.00, 0.00, 75000.00, '0', '2026-12-04 00:00:00', 0, 'MacBook Air M2 账期应付', 'admin', '2026-11-22 10:50:53', '', NULL, 75000.00, 0.00);
INSERT INTO `fin_payable` (`payable_id`, `purchase_order_id`, `supplier_id`, `amount_due`, `amount_paid`, `remain_amount`, `status`, `due_date`, `overdue_days`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `amount`, `paid_amount`) VALUES (1003, 104, 1, 54400.00, 30000.00, 24400.00, '1', '2026-12-15 00:00:00', 0, 'PO20261115001应付账款', 'admin', '2026-11-15 10:00:00', 'admin', '2026-11-21 14:00:00', 54400.00, 30000.00);

-- ----------------------------
-- Table structure for fin_payment
-- ----------------------------
DROP TABLE IF EXISTS `fin_payment`;
CREATE TABLE `fin_payment` (
  `payment_id` bigint NOT NULL AUTO_INCREMENT COMMENT '付款记录ID',
  `payable_id` bigint NOT NULL COMMENT '应付账款ID',
  `purchase_order_id` bigint NOT NULL COMMENT '采购订单ID',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '付款金额',
  `payment_time` datetime DEFAULT NULL COMMENT '付款时间',
  `payment_method` varchar(50) DEFAULT NULL COMMENT '付款方式',
  `account_id` bigint DEFAULT NULL COMMENT '资金结算账户ID',
  `status` varchar(20) NOT NULL DEFAULT 'normal' COMMENT '状态(normal正常 voided已冲红)',
  `void_reason` varchar(255) DEFAULT NULL COMMENT '冲红作废原因',
  `void_by` varchar(64) DEFAULT NULL COMMENT '冲红操作人',
  `void_time` datetime DEFAULT NULL COMMENT '冲红时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `payment_date` datetime DEFAULT NULL COMMENT '浠樻鏃ユ湡',
  PRIMARY KEY (`payment_id`),
  KEY `idx_fin_payment_payable_id` (`payable_id`),
  KEY `idx_fin_payment_supplier_payment_time` (`supplier_id`,`payment_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1003 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='付款记录表';

-- ----------------------------
-- Records of fin_payment
-- ----------------------------
INSERT INTO `fin_payment` (`payment_id`, `payable_id`, `purchase_order_id`, `supplier_id`, `amount`, `payment_time`, `payment_method`, `account_id`, `status`, `void_reason`, `void_by`, `void_time`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `payment_date`) VALUES (1001, 1001, 102, 1, 50000.00, '2026-11-22 10:50:53', 'bank_transfer', 1, 'normal', NULL, NULL, NULL, '采购首付款银行转账', 'admin', '2026-11-22 10:50:53', '', NULL, '2026-11-22 10:50:53');
INSERT INTO `fin_payment` (`payment_id`, `payable_id`, `purchase_order_id`, `supplier_id`, `amount`, `payment_time`, `payment_method`, `account_id`, `status`, `void_reason`, `void_by`, `void_time`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `payment_date`) VALUES (1002, 1003, 104, 1, 30000.00, '2026-11-21 14:00:00', 'bank_transfer', 1, 'normal', NULL, NULL, NULL, 'PO20261115001首期货款', 'admin', '2026-11-21 14:00:00', 'admin', '2026-11-21 14:00:00', '2026-11-21 14:00:00');

-- ----------------------------
-- Table structure for fin_period_close
-- ----------------------------
DROP TABLE IF EXISTS `fin_period_close`;
CREATE TABLE `fin_period_close` (
  `period_id` bigint NOT NULL AUTO_INCREMENT COMMENT '月结主键',
  `period_month` varchar(7) NOT NULL COMMENT '会计期间(YYYY-MM)',
  `close_status` varchar(20) NOT NULL DEFAULT 'closed' COMMENT '状态(closed已关账 reopened已反结账)',
  `sale_amount_snapshot` decimal(18,2) DEFAULT '0.00' COMMENT '关账时月度销售额快照',
  `purchase_amount_snapshot` decimal(18,2) DEFAULT '0.00' COMMENT '关账时月度采购额快照',
  `closed_by` varchar(64) DEFAULT NULL COMMENT '关账操作人',
  `closed_time` datetime DEFAULT NULL COMMENT '关账时间',
  `reopen_reason` varchar(255) DEFAULT NULL COMMENT '反结账原因',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`period_id`),
  UNIQUE KEY `uk_fin_period_month` (`period_month`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='财务会计期间月结关账表';

-- ----------------------------
-- Records of fin_period_close
-- ----------------------------
INSERT INTO `fin_period_close` (`period_id`, `period_month`, `close_status`, `sale_amount_snapshot`, `purchase_amount_snapshot`, `closed_by`, `closed_time`, `reopen_reason`, `remark`, `create_time`, `update_time`) VALUES (1, '2026-09', 'closed', 168000.00, 96000.00, 'admin', '2026-10-03 18:00:00', NULL, '2026年9月账实核对无误，已完成月结锁定', '2026-11-22 16:34:57', NULL);

-- ----------------------------
-- Table structure for fin_receipt
-- ----------------------------
DROP TABLE IF EXISTS `fin_receipt`;
CREATE TABLE `fin_receipt` (
  `receipt_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'åæ¬¾ä¸»é®',
  `receivable_id` bigint NOT NULL COMMENT 'åºæ¶ç¼å·',
  `sale_order_id` bigint NOT NULL COMMENT 'éå®åç¼å·',
  `customer_id` bigint NOT NULL COMMENT 'å®¢æ·ç¼å·',
  `amount` decimal(18,2) DEFAULT '0.00' COMMENT 'åæ¬¾éé¢',
  `payment_time` datetime DEFAULT NULL COMMENT '回款时间',
  `payment_method` varchar(50) DEFAULT NULL COMMENT '回款方式',
  `account_id` bigint DEFAULT NULL COMMENT '资金结算账户ID',
  `status` varchar(20) NOT NULL DEFAULT 'normal' COMMENT '状态(normal正常 voided已冲红)',
  `void_reason` varchar(255) DEFAULT NULL COMMENT '冲红作废原因',
  `void_by` varchar(64) DEFAULT NULL COMMENT '冲红操作人',
  `void_time` datetime DEFAULT NULL COMMENT '冲红时间',
  `receipt_date` datetime DEFAULT NULL COMMENT 'åæ¬¾æ¥æ',
  `remark` varchar(255) DEFAULT NULL COMMENT 'å¤æ³¨',
  `create_by` varchar(64) DEFAULT '' COMMENT 'åå»ºè',
  `create_time` datetime DEFAULT NULL COMMENT 'åå»ºæ¶é´',
  `update_by` varchar(64) DEFAULT '' COMMENT 'æ´æ°è',
  `update_time` datetime DEFAULT NULL COMMENT 'æ´æ°æ¶é´',
  PRIMARY KEY (`receipt_id`),
  KEY `idx_fin_receipt_receivable_id` (`receivable_id`),
  KEY `idx_fin_receipt_sale_order_id` (`sale_order_id`),
  KEY `idx_fin_receipt_customer_receipt_date` (`customer_id`,`receipt_date`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='åæ¬¾ç»è®°è¡¨';

-- ----------------------------
-- Records of fin_receipt
-- ----------------------------
INSERT INTO `fin_receipt` (`receipt_id`, `receivable_id`, `sale_order_id`, `customer_id`, `amount`, `payment_time`, `payment_method`, `account_id`, `status`, `void_reason`, `void_by`, `void_time`, `receipt_date`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (1, 1, 1, 990018, 79990.00, '2026-03-19 00:00:00', 'bank_transfer', 1, 'normal', NULL, NULL, NULL, '2026-03-19 00:00:00', NULL, 'admin', '2026-03-19 00:55:03', '', NULL);
INSERT INTO `fin_receipt` (`receipt_id`, `receivable_id`, `sale_order_id`, `customer_id`, `amount`, `payment_time`, `payment_method`, `account_id`, `status`, `void_reason`, `void_by`, `void_time`, `receipt_date`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (2, 2, 2, 990019, 10000.00, '2026-03-21 00:00:00', 'bank_transfer', 1, 'normal', NULL, NULL, NULL, '2026-03-21 00:00:00', NULL, 'admin', '2026-03-21 00:55:03', '', NULL);
INSERT INTO `fin_receipt` (`receipt_id`, `receivable_id`, `sale_order_id`, `customer_id`, `amount`, `payment_time`, `payment_method`, `account_id`, `status`, `void_reason`, `void_by`, `void_time`, `receipt_date`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (3, 4, 10, 990018, 50000.00, '2026-11-20 15:00:00', 'bank_transfer', 1, 'normal', NULL, NULL, NULL, '2026-11-20 15:00:00', 'SO20261118001首期回款', 'admin', '2026-11-20 15:00:00', 'admin', '2026-11-20 15:00:00');

-- ----------------------------
-- Table structure for fin_receivable
-- ----------------------------
DROP TABLE IF EXISTS `fin_receivable`;
CREATE TABLE `fin_receivable` (
  `receivable_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'åºæ¶ä¸»é®',
  `sale_order_id` bigint NOT NULL COMMENT 'éå®åç¼å·',
  `customer_id` bigint NOT NULL COMMENT 'å®¢æ·ç¼å·',
  `amount_due` decimal(18,2) DEFAULT '0.00' COMMENT 'åºæ¶éé¢',
  `amount_paid` decimal(18,2) DEFAULT '0.00' COMMENT 'å·²æ¶éé¢',
  `status` varchar(20) DEFAULT 'unpaid' COMMENT 'ç¶æ',
  `due_date` datetime DEFAULT NULL COMMENT 'å°ææ¥æ',
  `create_by` varchar(64) DEFAULT '' COMMENT 'åå»ºè',
  `create_time` datetime DEFAULT NULL COMMENT 'åå»ºæ¶é´',
  `update_by` varchar(64) DEFAULT '' COMMENT 'æ´æ°è',
  `update_time` datetime DEFAULT NULL COMMENT 'æ´æ°æ¶é´',
  `remark` varchar(255) DEFAULT NULL COMMENT 'å¤æ³¨',
  `amount` decimal(18,2) DEFAULT '0.00' COMMENT '搴旀敹閲戦',
  `received_amount` decimal(18,2) DEFAULT '0.00' COMMENT '宸叉敹閲戦',
  PRIMARY KEY (`receivable_id`),
  UNIQUE KEY `uk_fin_receivable_sale_order_id` (`sale_order_id`),
  KEY `idx_fin_receivable_customer_status_receivable_id` (`customer_id`,`status`,`receivable_id`),
  KEY `idx_fin_receivable_status_due_date` (`status`,`due_date`),
  KEY `idx_fin_receivable_customer_status` (`customer_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='åºæ¶å°è´¦è¡¨';

-- ----------------------------
-- Records of fin_receivable
-- ----------------------------
INSERT INTO `fin_receivable` (`receivable_id`, `sale_order_id`, `customer_id`, `amount_due`, `amount_paid`, `status`, `due_date`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `amount`, `received_amount`) VALUES (1, 1, 990018, 79990.00, 79990.00, 'paid', '2026-03-31 00:00:00', 'admin', '2026-03-18 00:55:03', '', NULL, NULL, 0.00, 0.00);
INSERT INTO `fin_receivable` (`receivable_id`, `sale_order_id`, `customer_id`, `amount_due`, `amount_paid`, `status`, `due_date`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `amount`, `received_amount`) VALUES (2, 2, 990019, 34995.00, 10000.00, 'partial', '2026-04-03 00:00:00', 'admin', '2026-03-19 00:55:03', '', NULL, NULL, 0.00, 0.00);
INSERT INTO `fin_receivable` (`receivable_id`, `sale_order_id`, `customer_id`, `amount_due`, `amount_paid`, `status`, `due_date`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `amount`, `received_amount`) VALUES (3, 3, 990020, 71992.00, 0.00, 'unpaid', '2026-03-23 00:00:00', 'admin', '2026-02-22 00:55:03', '', NULL, NULL, 0.00, 0.00);
INSERT INTO `fin_receivable` (`receivable_id`, `sale_order_id`, `customer_id`, `amount_due`, `amount_paid`, `status`, `due_date`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `amount`, `received_amount`) VALUES (4, 10, 990018, 79990.00, 50000.00, 'partial', '2026-12-18 00:00:00', 'admin', '2026-11-18 14:30:00', 'admin', '2026-11-20 15:00:00', 'SO20261118001应收台账', 79990.00, 50000.00);

-- ----------------------------
-- Table structure for gen_table
-- ----------------------------
DROP TABLE IF EXISTS `gen_table`;
CREATE TABLE `gen_table` (
  `table_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ç¼å·',
  `table_name` varchar(200) DEFAULT '' COMMENT 'è¡¨åç§°',
  `table_comment` varchar(500) DEFAULT '' COMMENT 'è¡¨æè¿°',
  `sub_table_name` varchar(64) DEFAULT NULL COMMENT 'å³èå­è¡¨çè¡¨å',
  `sub_table_fk_name` varchar(64) DEFAULT NULL COMMENT 'å­è¡¨å³èçå¤é®å',
  `class_name` varchar(100) DEFAULT '' COMMENT 'å®ä½ç±»åç§°',
  `tpl_category` varchar(200) DEFAULT 'crud' COMMENT 'ä½¿ç¨çæ¨¡æ¿ï¼crudåè¡¨æä½ treeæ è¡¨æä½ï¼',
  `tpl_web_type` varchar(30) DEFAULT '' COMMENT 'åç«¯æ¨¡æ¿ç±»åï¼element-uiæ¨¡ç element-plusæ¨¡çï¼',
  `package_name` varchar(100) DEFAULT NULL COMMENT 'çæåè·¯å¾',
  `module_name` varchar(30) DEFAULT NULL COMMENT 'çææ¨¡åå',
  `business_name` varchar(30) DEFAULT NULL COMMENT 'çæä¸å¡å',
  `function_name` varchar(50) DEFAULT NULL COMMENT 'çæåè½å',
  `function_author` varchar(50) DEFAULT NULL COMMENT 'çæåè½ä½è',
  `gen_type` char(1) DEFAULT '0' COMMENT 'çæä»£ç æ¹å¼ï¼0zipåç¼©å 1èªå®ä¹è·¯å¾ï¼',
  `gen_path` varchar(200) DEFAULT '/' COMMENT 'çæè·¯å¾ï¼ä¸å¡«é»è®¤é¡¹ç®è·¯å¾ï¼',
  `options` varchar(1000) DEFAULT NULL COMMENT 'å¶å®çæéé¡¹',
  `create_by` varchar(64) DEFAULT '' COMMENT 'åå»ºè',
  `create_time` datetime DEFAULT NULL COMMENT 'åå»ºæ¶é´',
  `update_by` varchar(64) DEFAULT '' COMMENT 'æ´æ°è',
  `update_time` datetime DEFAULT NULL COMMENT 'æ´æ°æ¶é´',
  `remark` varchar(500) DEFAULT NULL COMMENT 'å¤æ³¨',
  PRIMARY KEY (`table_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ä»£ç çæä¸å¡è¡¨';

-- ----------------------------
-- Table structure for gen_table_column
-- ----------------------------
DROP TABLE IF EXISTS `gen_table_column`;
CREATE TABLE `gen_table_column` (
  `column_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ç¼å·',
  `table_id` bigint DEFAULT NULL COMMENT 'å½å±è¡¨ç¼å·',
  `column_name` varchar(200) DEFAULT NULL COMMENT 'ååç§°',
  `column_comment` varchar(500) DEFAULT NULL COMMENT 'åæè¿°',
  `column_type` varchar(100) DEFAULT NULL COMMENT 'åç±»å',
  `java_type` varchar(500) DEFAULT NULL COMMENT 'JAVAç±»å',
  `java_field` varchar(200) DEFAULT NULL COMMENT 'JAVAå­æ®µå',
  `is_pk` char(1) DEFAULT NULL COMMENT 'æ¯å¦ä¸»é®ï¼1æ¯ï¼',
  `is_increment` char(1) DEFAULT NULL COMMENT 'æ¯å¦èªå¢ï¼1æ¯ï¼',
  `is_required` char(1) DEFAULT NULL COMMENT 'æ¯å¦å¿å¡«ï¼1æ¯ï¼',
  `is_insert` char(1) DEFAULT NULL COMMENT 'æ¯å¦ä¸ºæå¥å­æ®µï¼1æ¯ï¼',
  `is_edit` char(1) DEFAULT NULL COMMENT 'æ¯å¦ç¼è¾å­æ®µï¼1æ¯ï¼',
  `is_list` char(1) DEFAULT NULL COMMENT 'æ¯å¦åè¡¨å­æ®µï¼1æ¯ï¼',
  `is_query` char(1) DEFAULT NULL COMMENT 'æ¯å¦æ¥è¯¢å­æ®µï¼1æ¯ï¼',
  `query_type` varchar(200) DEFAULT 'EQ' COMMENT 'æ¥è¯¢æ¹å¼ï¼ç­äºãä¸ç­äºãå¤§äºãå°äºãèå´ï¼',
  `html_type` varchar(200) DEFAULT NULL COMMENT 'æ¾ç¤ºç±»åï¼ææ¬æ¡ãææ¬åãä¸ææ¡ãå¤éæ¡ãåéæ¡ãæ¥ææ§ä»¶ï¼',
  `dict_type` varchar(200) DEFAULT '' COMMENT 'å­å¸ç±»å',
  `sort` int DEFAULT NULL COMMENT 'æåº',
  `create_by` varchar(64) DEFAULT '' COMMENT 'åå»ºè',
  `create_time` datetime DEFAULT NULL COMMENT 'åå»ºæ¶é´',
  `update_by` varchar(64) DEFAULT '' COMMENT 'æ´æ°è',
  `update_time` datetime DEFAULT NULL COMMENT 'æ´æ°æ¶é´',
  PRIMARY KEY (`column_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ä»£ç çæä¸å¡è¡¨å­æ®µ';

-- ----------------------------
-- Table structure for md_customer
-- ----------------------------
DROP TABLE IF EXISTS `md_customer`;
CREATE TABLE `md_customer` (
  `customer_id` bigint NOT NULL AUTO_INCREMENT COMMENT '客户主键',
  `customer_code` varchar(64) NOT NULL COMMENT '客户编码',
  `customer_name` varchar(128) NOT NULL COMMENT '客户名称',
  `contact_person` varchar(64) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(32) DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(128) DEFAULT NULL COMMENT '邮箱',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `credit_limit` decimal(18,2) DEFAULT '0.00' COMMENT '信用额度',
  `payment_method` varchar(30) DEFAULT 'cash' COMMENT '付款方式',
  `customer_level` varchar(30) DEFAULT 'normal' COMMENT '客户等级',
  `status` char(1) DEFAULT '0' COMMENT '状态',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标记',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`customer_id`),
  UNIQUE KEY `uk_md_customer_code` (`customer_code`),
  KEY `idx_md_customer_customer_code` (`customer_code`)
) ENGINE=InnoDB AUTO_INCREMENT=990024 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户资料表';

-- ----------------------------
-- Records of md_customer
-- ----------------------------
INSERT INTO `md_customer` (`customer_id`, `customer_code`, `customer_name`, `contact_person`, `contact_phone`, `contact_email`, `address`, `credit_limit`, `payment_method`, `customer_level`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990001, 'cust_demo_001', '样例客户一', '张明', '13800000001', 'demo001@example.com', '深圳市南山区科技园1号', 30000.00, 'bank', 'a', '0', '0', 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例客户');
INSERT INTO `md_customer` (`customer_id`, `customer_code`, `customer_name`, `contact_person`, `contact_phone`, `contact_email`, `address`, `credit_limit`, `payment_method`, `customer_level`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990002, 'cust_demo_002', '样例客户二', '李强', '13800000002', 'demo002@example.com', '深圳市福田区深南大道2号', 25000.00, 'cash', 'a', '0', '0', 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例客户');
INSERT INTO `md_customer` (`customer_id`, `customer_code`, `customer_name`, `contact_person`, `contact_phone`, `contact_email`, `address`, `credit_limit`, `payment_method`, `customer_level`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990003, 'cust_demo_003', '样例客户三', '王芳', '13800000003', 'demo003@example.com', '深圳市宝安区创业一路3号', 20000.00, 'month', 'b', '0', '0', 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例客户');
INSERT INTO `md_customer` (`customer_id`, `customer_code`, `customer_name`, `contact_person`, `contact_phone`, `contact_email`, `address`, `credit_limit`, `payment_method`, `customer_level`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990004, 'cust_demo_004', '样例客户四', '赵雷', '13800000004', 'demo004@example.com', '东莞市南城区鸿福路4号', 18000.00, 'bank', 'b', '0', '0', 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例客户');
INSERT INTO `md_customer` (`customer_id`, `customer_code`, `customer_name`, `contact_person`, `contact_phone`, `contact_email`, `address`, `credit_limit`, `payment_method`, `customer_level`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990005, 'cust_demo_005', '样例客户五', '周敏', '13800000005', 'demo005@example.com', '广州市天河区体育西路5号', 22000.00, 'cash', 'c', '0', '0', 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例客户');
INSERT INTO `md_customer` (`customer_id`, `customer_code`, `customer_name`, `contact_person`, `contact_phone`, `contact_email`, `address`, `credit_limit`, `payment_method`, `customer_level`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990018, 'C1001', '北京华数科技有限公司', '张经理', '13800138000', NULL, '北京市海淀区中关村软件园1号楼', 100000.00, 'bank_transfer', 'vip', '0', '0', 'admin', '2026-03-24 00:55:03', '', NULL, NULL);
INSERT INTO `md_customer` (`customer_id`, `customer_code`, `customer_name`, `contact_person`, `contact_phone`, `contact_email`, `address`, `credit_limit`, `payment_method`, `customer_level`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990019, 'C1002', '上海天创商贸有限公司', '李总', '13900139000', NULL, '上海市浦东新区张江高科技园区', 50000.00, 'credit', 'gold', '0', '0', 'admin', '2026-03-24 00:55:03', '', NULL, NULL);
INSERT INTO `md_customer` (`customer_id`, `customer_code`, `customer_name`, `contact_person`, `contact_phone`, `contact_email`, `address`, `credit_limit`, `payment_method`, `customer_level`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990020, 'C1003', '广州智联网络科技', '王总监', '13700137000', NULL, '广州市天河区天河软件园', 20000.00, 'cash', 'silver', '0', '0', 'admin', '2026-03-24 00:55:03', '', NULL, NULL);
INSERT INTO `md_customer` (`customer_id`, `customer_code`, `customer_name`, `contact_person`, `contact_phone`, `contact_email`, `address`, `credit_limit`, `payment_method`, `customer_level`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990021, 'C1004', '深圳微客电子通讯', '赵老板', '13600136000', NULL, '深圳市南山区科技园南区', 0.00, 'cash', 'normal', '0', '0', 'admin', '2026-03-24 00:55:03', '', NULL, NULL);
INSERT INTO `md_customer` (`customer_id`, `customer_code`, `customer_name`, `contact_person`, `contact_phone`, `contact_email`, `address`, `credit_limit`, `payment_method`, `customer_level`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990022, 'C1005', '杭州云创数据服务', '孙主管', '13500135000', NULL, '杭州市滨江区网商路', 80000.00, 'bank_transfer', 'vip', '0', '0', 'admin', '2026-03-24 00:55:03', '', NULL, NULL);
INSERT INTO `md_customer` (`customer_id`, `customer_code`, `customer_name`, `contact_person`, `contact_phone`, `contact_email`, `address`, `credit_limit`, `payment_method`, `customer_level`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990023, 'C1006', '成都飞跃电子科技', '周总', '13400134000', NULL, '成都市高新区天府软件园', 30000.00, 'credit', 'gold', '0', '0', 'admin', '2026-03-24 00:55:03', '', NULL, NULL);

-- ----------------------------
-- Table structure for md_customer_price
-- ----------------------------
DROP TABLE IF EXISTS `md_customer_price`;
CREATE TABLE `md_customer_price` (
  `price_id` bigint NOT NULL AUTO_INCREMENT COMMENT '协议价主键',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `agreed_price` decimal(18,2) NOT NULL COMMENT '客户专属协议单价',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '状态(0有效 1停用)',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`price_id`),
  UNIQUE KEY `uk_customer_product_price` (`customer_id`,`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户商品专属协议价表';

-- ----------------------------
-- Records of md_customer_price
-- ----------------------------
INSERT INTO `md_customer_price` (`price_id`, `customer_id`, `product_id`, `agreed_price`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (1, 1, 1, 7850.00, '0', '战略大客户年度框架协议价', 'admin', '2026-11-22 16:34:57', '', NULL);
INSERT INTO `md_customer_price` (`price_id`, `customer_id`, `product_id`, `agreed_price`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (2, 2, 3, 8650.00, '0', '上海天创核心机型协议价', 'admin', '2026-11-22 16:34:57', '', NULL);

-- ----------------------------
-- Table structure for md_location
-- ----------------------------
DROP TABLE IF EXISTS `md_location`;
CREATE TABLE `md_location` (
  `location_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'åºä½ä¸»é®',
  `warehouse_id` bigint NOT NULL COMMENT 'ä»åºç¼å·',
  `location_code` varchar(64) NOT NULL COMMENT 'åºä½ç¼ç ',
  `location_name` varchar(128) NOT NULL COMMENT 'åºä½åç§°',
  `status` char(1) DEFAULT '0' COMMENT 'ç¶æ',
  `del_flag` char(1) DEFAULT '0' COMMENT 'å é¤æ è®°',
  `create_by` varchar(64) DEFAULT '' COMMENT 'åå»ºè',
  `create_time` datetime DEFAULT NULL COMMENT 'åå»ºæ¶é´',
  `update_by` varchar(64) DEFAULT '' COMMENT 'æ´æ°è',
  `update_time` datetime DEFAULT NULL COMMENT 'æ´æ°æ¶é´',
  `remark` varchar(255) DEFAULT NULL COMMENT 'å¤æ³¨',
  PRIMARY KEY (`location_id`),
  UNIQUE KEY `uk_md_location_code` (`location_code`),
  KEY `idx_md_location_warehouse_id` (`warehouse_id`)
) ENGINE=InnoDB AUTO_INCREMENT=990006 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='åºä½èµæè¡¨';

-- ----------------------------
-- Records of md_location
-- ----------------------------
INSERT INTO `md_location` (`location_id`, `warehouse_id`, `location_code`, `location_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990001, 990001, 'loc_demo_001', 'a-01-01', '0', '0', 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例库位');
INSERT INTO `md_location` (`location_id`, `warehouse_id`, `location_code`, `location_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990002, 990002, 'loc_demo_002', 'a-01-02', '0', '0', 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例库位');
INSERT INTO `md_location` (`location_id`, `warehouse_id`, `location_code`, `location_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990003, 990003, 'loc_demo_003', 'b-01-01', '0', '0', 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例库位');
INSERT INTO `md_location` (`location_id`, `warehouse_id`, `location_code`, `location_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990004, 990004, 'loc_demo_004', 'b-02-01', '0', '0', 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例库位');
INSERT INTO `md_location` (`location_id`, `warehouse_id`, `location_code`, `location_name`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990005, 990005, 'loc_demo_005', 'c-01-01', '0', '0', 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例库位');

-- ----------------------------
-- Table structure for md_product
-- ----------------------------
DROP TABLE IF EXISTS `md_product`;
CREATE TABLE `md_product` (
  `product_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ååä¸»é®',
  `product_code` varchar(64) NOT NULL COMMENT 'ååç¼ç ',
  `product_name` varchar(128) NOT NULL COMMENT 'åååç§°',
  `product_spec` varchar(128) DEFAULT NULL COMMENT 'è§æ ¼',
  `unit_name` varchar(32) DEFAULT NULL COMMENT 'åä½',
  `bar_code` varchar(64) DEFAULT NULL COMMENT 'æ¡ç ',
  `category_name` varchar(64) DEFAULT NULL COMMENT 'åç±»',
  `brand_name` varchar(64) DEFAULT NULL COMMENT 'åç',
  `product_image` varchar(255) DEFAULT NULL COMMENT 'ååå¾ç',
  `cost_price` decimal(18,2) DEFAULT '0.00' COMMENT 'ææ¬ä»·',
  `sale_price` decimal(18,2) DEFAULT '0.00' COMMENT 'éå®ä»·',
  `status` char(1) DEFAULT '0' COMMENT 'ç¶æ',
  `del_flag` char(1) DEFAULT '0' COMMENT 'å é¤æ è®°',
  `create_by` varchar(64) DEFAULT '' COMMENT 'åå»ºè',
  `create_time` datetime DEFAULT NULL COMMENT 'åå»ºæ¶é´',
  `update_by` varchar(64) DEFAULT '' COMMENT 'æ´æ°è',
  `update_time` datetime DEFAULT NULL COMMENT 'æ´æ°æ¶é´',
  `remark` varchar(255) DEFAULT NULL COMMENT 'å¤æ³¨',
  PRIMARY KEY (`product_id`),
  UNIQUE KEY `uk_md_product_code` (`product_code`),
  KEY `idx_md_product_product_code` (`product_code`)
) ENGINE=InnoDB AUTO_INCREMENT=990016 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ååèµæè¡¨';

-- ----------------------------
-- Records of md_product
-- ----------------------------
INSERT INTO `md_product` (`product_id`, `product_code`, `product_name`, `product_spec`, `unit_name`, `bar_code`, `category_name`, `brand_name`, `product_image`, `cost_price`, `sale_price`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990001, 'sku_demo_001', '样例矿泉水', '550ml', '瓶', '6900000000010', '饮料', '清泉', NULL, 2.50, 4.00, '0', '0', 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例商品');
INSERT INTO `md_product` (`product_id`, `product_code`, `product_name`, `product_spec`, `unit_name`, `bar_code`, `category_name`, `brand_name`, `product_image`, `cost_price`, `sale_price`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990002, 'sku_demo_002', '样例红茶', '500ml', '瓶', '6900000000027', '饮料', '茶语', NULL, 3.20, 5.50, '0', '0', 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例商品');
INSERT INTO `md_product` (`product_id`, `product_code`, `product_name`, `product_spec`, `unit_name`, `bar_code`, `category_name`, `brand_name`, `product_image`, `cost_price`, `sale_price`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990003, 'sku_demo_003', '样例纸巾', '3层*10包', '提', '6900000000034', '日用品', '洁柔', NULL, 12.00, 18.00, '0', '0', 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例商品');
INSERT INTO `md_product` (`product_id`, `product_code`, `product_name`, `product_spec`, `unit_name`, `bar_code`, `category_name`, `brand_name`, `product_image`, `cost_price`, `sale_price`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990004, 'sku_demo_004', '样例洗衣液', '2kg', '桶', '6900000000041', '日化', '净彩', NULL, 22.00, 35.00, '0', '0', 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例商品');
INSERT INTO `md_product` (`product_id`, `product_code`, `product_name`, `product_spec`, `unit_name`, `bar_code`, `category_name`, `brand_name`, `product_image`, `cost_price`, `sale_price`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990005, 'sku_demo_005', '样例饼干', '320g', '盒', '6900000000058', '食品', '麦香', NULL, 6.50, 10.00, '0', '0', 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例商品');
INSERT INTO `md_product` (`product_id`, `product_code`, `product_name`, `product_spec`, `unit_name`, `bar_code`, `category_name`, `brand_name`, `product_image`, `cost_price`, `sale_price`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990011, 'P10001', 'iPhone 15 Pro', '256GB', '台', '6901234567890', '手机通讯', 'Apple', NULL, 6800.00, 7999.00, '0', '0', 'admin', '2026-03-24 00:55:03', '', NULL, NULL);
INSERT INTO `md_product` (`product_id`, `product_code`, `product_name`, `product_spec`, `unit_name`, `bar_code`, `category_name`, `brand_name`, `product_image`, `cost_price`, `sale_price`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990012, 'P10002', '华为 Mate 60 Pro', '512GB', '台', '6901234567891', '手机通讯', '华为', NULL, 6200.00, 6999.00, '0', '0', 'admin', '2026-03-24 00:55:03', '', NULL, NULL);
INSERT INTO `md_product` (`product_id`, `product_code`, `product_name`, `product_spec`, `unit_name`, `bar_code`, `category_name`, `brand_name`, `product_image`, `cost_price`, `sale_price`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990013, 'P20001', 'MacBook Air M2', '8G 256G', '台', '6901234567892', '电脑办公', 'Apple', NULL, 7500.00, 8999.00, '0', '0', 'admin', '2026-03-24 00:55:03', '', NULL, NULL);
INSERT INTO `md_product` (`product_id`, `product_code`, `product_name`, `product_spec`, `unit_name`, `bar_code`, `category_name`, `brand_name`, `product_image`, `cost_price`, `sale_price`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990014, 'P20002', 'ThinkPad X1 Carbon', '16G 512G', '台', '6901234567893', '电脑办公', '联想', NULL, 8500.00, 9999.00, '0', '0', 'admin', '2026-03-24 00:55:03', '', NULL, NULL);
INSERT INTO `md_product` (`product_id`, `product_code`, `product_name`, `product_spec`, `unit_name`, `bar_code`, `category_name`, `brand_name`, `product_image`, `cost_price`, `sale_price`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990015, 'P30001', 'AirPods Pro', '二代', '副', '6901234567894', '数码配件', 'Apple', NULL, 1400.00, 1899.00, '0', '0', 'admin', '2026-03-24 00:55:03', '', NULL, NULL);

-- ----------------------------
-- Table structure for md_supplier
-- ----------------------------
DROP TABLE IF EXISTS `md_supplier`;
CREATE TABLE `md_supplier` (
  `supplier_id` bigint NOT NULL AUTO_INCREMENT COMMENT '供应商ID',
  `supplier_code` varchar(64) NOT NULL COMMENT '供应商编码',
  `supplier_name` varchar(100) NOT NULL COMMENT '供应商名称',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(50) DEFAULT NULL COMMENT '联系邮箱',
  `address` varchar(200) DEFAULT NULL COMMENT '地址',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`supplier_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='供应商表';

-- ----------------------------
-- Records of md_supplier
-- ----------------------------
INSERT INTO `md_supplier` (`supplier_id`, `supplier_code`, `supplier_name`, `contact_person`, `contact_phone`, `contact_email`, `address`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, 'SUP1001', '深圳华强数码供应链有限公司', '陈海波', '13823100001', 'chenhb@hqsupply.cn', '深圳市福田区华强北路赛格科技园2栋', '0', '0', 'admin', '2026-11-22 10:50:53', '', NULL, '核心数码终端供应商');
INSERT INTO `md_supplier` (`supplier_id`, `supplier_code`, `supplier_name`, `contact_person`, `contact_phone`, `contact_email`, `address`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, 'SUP1002', '上海智汇信息设备有限公司', '林立群', '13918200002', 'linlq@zhihui-it.cn', '上海市漕河泾新兴技术开发区宜山路889号', '0', '0', 'admin', '2026-11-22 10:50:53', '', NULL, '电脑及办公设备供应商');
INSERT INTO `md_supplier` (`supplier_id`, `supplier_code`, `supplier_name`, `contact_person`, `contact_phone`, `contact_email`, `address`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3, 'SUP1003', '广东清泉饮品实业有限公司', '梁国栋', '13726300003', 'lianggd@qingquan.cn', '广州市黄埔区科学城开源大道18号', '0', '0', 'admin', '2026-11-22 10:50:53', '', NULL, '食品饮料供应商');
INSERT INTO `md_supplier` (`supplier_id`, `supplier_code`, `supplier_name`, `contact_person`, `contact_phone`, `contact_email`, `address`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4, 'SUP1004', '东莞洁雅日用品制造厂', '黄敏华', '13635400004', 'huangmh@jieya-dg.cn', '东莞市长安镇振安东路66号', '0', '0', 'admin', '2026-11-22 10:50:53', '', NULL, '纸品与家居百货供应商');
INSERT INTO `md_supplier` (`supplier_id`, `supplier_code`, `supplier_name`, `contact_person`, `contact_phone`, `contact_email`, `address`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5, 'SUP1005', '佛山净彩日化科技有限公司', '罗志强', '13544500005', 'luozq@jingcai-chem.cn', '佛山市顺德区大良工业园兴业路12号', '0', '0', 'admin', '2026-11-22 10:50:53', '', NULL, '洗护日化供应商');

-- ----------------------------
-- Table structure for md_warehouse
-- ----------------------------
DROP TABLE IF EXISTS `md_warehouse`;
CREATE TABLE `md_warehouse` (
  `warehouse_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä»åºä¸»é®',
  `warehouse_code` varchar(64) NOT NULL COMMENT 'ä»åºç¼ç ',
  `warehouse_name` varchar(128) NOT NULL COMMENT 'ä»åºåç§°',
  `contact_name` varchar(64) DEFAULT NULL COMMENT 'èç³»äºº',
  `contact_phone` varchar(32) DEFAULT NULL COMMENT 'èç³»çµè¯',
  `address` varchar(255) DEFAULT NULL COMMENT 'å°å',
  `status` char(1) DEFAULT '0' COMMENT 'ç¶æ',
  `del_flag` char(1) DEFAULT '0' COMMENT 'å é¤æ è®°',
  `create_by` varchar(64) DEFAULT '' COMMENT 'åå»ºè',
  `create_time` datetime DEFAULT NULL COMMENT 'åå»ºæ¶é´',
  `update_by` varchar(64) DEFAULT '' COMMENT 'æ´æ°è',
  `update_time` datetime DEFAULT NULL COMMENT 'æ´æ°æ¶é´',
  `remark` varchar(255) DEFAULT NULL COMMENT 'å¤æ³¨',
  PRIMARY KEY (`warehouse_id`),
  UNIQUE KEY `uk_md_warehouse_code` (`warehouse_code`)
) ENGINE=InnoDB AUTO_INCREMENT=990006 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ä»åºèµæè¡¨';

-- ----------------------------
-- Records of md_warehouse
-- ----------------------------
INSERT INTO `md_warehouse` (`warehouse_id`, `warehouse_code`, `warehouse_name`, `contact_name`, `contact_phone`, `address`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990001, 'wh_demo_001', '华南一号仓', '陈杰', '13900000001', '深圳市宝安区仓储路1号', '0', '0', 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例仓库');
INSERT INTO `md_warehouse` (`warehouse_id`, `warehouse_code`, `warehouse_name`, `contact_name`, `contact_phone`, `address`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990002, 'wh_demo_002', '华南二号仓', '刘洋', '13900000002', '深圳市龙岗区物流路2号', '0', '0', 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例仓库');
INSERT INTO `md_warehouse` (`warehouse_id`, `warehouse_code`, `warehouse_name`, `contact_name`, `contact_phone`, `address`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990003, 'wh_demo_003', '东莞中转仓', '何勇', '13900000003', '东莞市寮步镇仓储路3号', '0', '0', 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例仓库');
INSERT INTO `md_warehouse` (`warehouse_id`, `warehouse_code`, `warehouse_name`, `contact_name`, `contact_phone`, `address`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990004, 'wh_demo_004', '广州前置仓', '杨帆', '13900000004', '广州市白云区机场路4号', '0', '0', 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例仓库');
INSERT INTO `md_warehouse` (`warehouse_id`, `warehouse_code`, `warehouse_name`, `contact_name`, `contact_phone`, `address`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990005, 'wh_demo_005', '佛山备货仓', '孙涛', '13900000005', '佛山市南海区桂城路5号', '0', '0', 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例仓库');

-- ----------------------------
-- Table structure for qrtz_blob_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_blob_triggers`;
CREATE TABLE `qrtz_blob_triggers` (
  `sched_name` varchar(120) NOT NULL COMMENT 'è°åº¦åç§°',
  `trigger_name` varchar(200) NOT NULL COMMENT 'qrtz_triggersè¡¨trigger_nameçå¤é®',
  `trigger_group` varchar(200) NOT NULL COMMENT 'qrtz_triggersè¡¨trigger_groupçå¤é®',
  `blob_data` blob COMMENT 'å­æ¾æä¹åTriggerå¯¹è±¡',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `qrtz_blob_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Blobç±»åçè§¦åå¨è¡¨';

-- ----------------------------
-- Table structure for qrtz_calendars
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_calendars`;
CREATE TABLE `qrtz_calendars` (
  `sched_name` varchar(120) NOT NULL COMMENT 'è°åº¦åç§°',
  `calendar_name` varchar(200) NOT NULL COMMENT 'æ¥ååç§°',
  `calendar` blob NOT NULL COMMENT 'å­æ¾æä¹åcalendarå¯¹è±¡',
  PRIMARY KEY (`sched_name`,`calendar_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æ¥åä¿¡æ¯è¡¨';

-- ----------------------------
-- Table structure for qrtz_cron_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_cron_triggers`;
CREATE TABLE `qrtz_cron_triggers` (
  `sched_name` varchar(120) NOT NULL COMMENT 'è°åº¦åç§°',
  `trigger_name` varchar(200) NOT NULL COMMENT 'qrtz_triggersè¡¨trigger_nameçå¤é®',
  `trigger_group` varchar(200) NOT NULL COMMENT 'qrtz_triggersè¡¨trigger_groupçå¤é®',
  `cron_expression` varchar(200) NOT NULL COMMENT 'cronè¡¨è¾¾å¼',
  `time_zone_id` varchar(80) DEFAULT NULL COMMENT 'æ¶åº',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `qrtz_cron_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Cronç±»åçè§¦åå¨è¡¨';

-- ----------------------------
-- Table structure for qrtz_fired_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_fired_triggers`;
CREATE TABLE `qrtz_fired_triggers` (
  `sched_name` varchar(120) NOT NULL COMMENT 'è°åº¦åç§°',
  `entry_id` varchar(95) NOT NULL COMMENT 'è°åº¦å¨å®ä¾id',
  `trigger_name` varchar(200) NOT NULL COMMENT 'qrtz_triggersè¡¨trigger_nameçå¤é®',
  `trigger_group` varchar(200) NOT NULL COMMENT 'qrtz_triggersè¡¨trigger_groupçå¤é®',
  `instance_name` varchar(200) NOT NULL COMMENT 'è°åº¦å¨å®ä¾å',
  `fired_time` bigint NOT NULL COMMENT 'è§¦åçæ¶é´',
  `sched_time` bigint NOT NULL COMMENT 'å®æ¶å¨å¶å®çæ¶é´',
  `priority` int NOT NULL COMMENT 'ä¼åçº§',
  `state` varchar(16) NOT NULL COMMENT 'ç¶æ',
  `job_name` varchar(200) DEFAULT NULL COMMENT 'ä»»å¡åç§°',
  `job_group` varchar(200) DEFAULT NULL COMMENT 'ä»»å¡ç»å',
  `is_nonconcurrent` varchar(1) DEFAULT NULL COMMENT 'æ¯å¦å¹¶å',
  `requests_recovery` varchar(1) DEFAULT NULL COMMENT 'æ¯å¦æ¥åæ¢å¤æ§è¡',
  PRIMARY KEY (`sched_name`,`entry_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='å·²è§¦åçè§¦åå¨è¡¨';

-- ----------------------------
-- Table structure for qrtz_job_details
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_job_details`;
CREATE TABLE `qrtz_job_details` (
  `sched_name` varchar(120) NOT NULL COMMENT 'è°åº¦åç§°',
  `job_name` varchar(200) NOT NULL COMMENT 'ä»»å¡åç§°',
  `job_group` varchar(200) NOT NULL COMMENT 'ä»»å¡ç»å',
  `description` varchar(250) DEFAULT NULL COMMENT 'ç¸å³ä»ç»',
  `job_class_name` varchar(250) NOT NULL COMMENT 'æ§è¡ä»»å¡ç±»åç§°',
  `is_durable` varchar(1) NOT NULL COMMENT 'æ¯å¦æä¹å',
  `is_nonconcurrent` varchar(1) NOT NULL COMMENT 'æ¯å¦å¹¶å',
  `is_update_data` varchar(1) NOT NULL COMMENT 'æ¯å¦æ´æ°æ°æ®',
  `requests_recovery` varchar(1) NOT NULL COMMENT 'æ¯å¦æ¥åæ¢å¤æ§è¡',
  `job_data` blob COMMENT 'å­æ¾æä¹åjobå¯¹è±¡',
  PRIMARY KEY (`sched_name`,`job_name`,`job_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ä»»å¡è¯¦ç»ä¿¡æ¯è¡¨';

-- ----------------------------
-- Table structure for qrtz_locks
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_locks`;
CREATE TABLE `qrtz_locks` (
  `sched_name` varchar(120) NOT NULL COMMENT 'è°åº¦åç§°',
  `lock_name` varchar(40) NOT NULL COMMENT 'æ²è§éåç§°',
  PRIMARY KEY (`sched_name`,`lock_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='å­å¨çæ²è§éä¿¡æ¯è¡¨';

-- ----------------------------
-- Table structure for qrtz_paused_trigger_grps
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_paused_trigger_grps`;
CREATE TABLE `qrtz_paused_trigger_grps` (
  `sched_name` varchar(120) NOT NULL COMMENT 'è°åº¦åç§°',
  `trigger_group` varchar(200) NOT NULL COMMENT 'qrtz_triggersè¡¨trigger_groupçå¤é®',
  PRIMARY KEY (`sched_name`,`trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æåçè§¦åå¨è¡¨';

-- ----------------------------
-- Table structure for qrtz_scheduler_state
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_scheduler_state`;
CREATE TABLE `qrtz_scheduler_state` (
  `sched_name` varchar(120) NOT NULL COMMENT 'è°åº¦åç§°',
  `instance_name` varchar(200) NOT NULL COMMENT 'å®ä¾åç§°',
  `last_checkin_time` bigint NOT NULL COMMENT 'ä¸æ¬¡æ£æ¥æ¶é´',
  `checkin_interval` bigint NOT NULL COMMENT 'æ£æ¥é´éæ¶é´',
  PRIMARY KEY (`sched_name`,`instance_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='è°åº¦å¨ç¶æè¡¨';

-- ----------------------------
-- Table structure for qrtz_simple_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simple_triggers`;
CREATE TABLE `qrtz_simple_triggers` (
  `sched_name` varchar(120) NOT NULL COMMENT 'è°åº¦åç§°',
  `trigger_name` varchar(200) NOT NULL COMMENT 'qrtz_triggersè¡¨trigger_nameçå¤é®',
  `trigger_group` varchar(200) NOT NULL COMMENT 'qrtz_triggersè¡¨trigger_groupçå¤é®',
  `repeat_count` bigint NOT NULL COMMENT 'éå¤çæ¬¡æ°ç»è®¡',
  `repeat_interval` bigint NOT NULL COMMENT 'éå¤çé´éæ¶é´',
  `times_triggered` bigint NOT NULL COMMENT 'å·²ç»è§¦åçæ¬¡æ°',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `qrtz_simple_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç®åè§¦åå¨çä¿¡æ¯è¡¨';

-- ----------------------------
-- Table structure for qrtz_simprop_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simprop_triggers`;
CREATE TABLE `qrtz_simprop_triggers` (
  `sched_name` varchar(120) NOT NULL COMMENT 'è°åº¦åç§°',
  `trigger_name` varchar(200) NOT NULL COMMENT 'qrtz_triggersè¡¨trigger_nameçå¤é®',
  `trigger_group` varchar(200) NOT NULL COMMENT 'qrtz_triggersè¡¨trigger_groupçå¤é®',
  `str_prop_1` varchar(512) DEFAULT NULL COMMENT 'Stringç±»åçtriggerçç¬¬ä¸ä¸ªåæ°',
  `str_prop_2` varchar(512) DEFAULT NULL COMMENT 'Stringç±»åçtriggerçç¬¬äºä¸ªåæ°',
  `str_prop_3` varchar(512) DEFAULT NULL COMMENT 'Stringç±»åçtriggerçç¬¬ä¸ä¸ªåæ°',
  `int_prop_1` int DEFAULT NULL COMMENT 'intç±»åçtriggerçç¬¬ä¸ä¸ªåæ°',
  `int_prop_2` int DEFAULT NULL COMMENT 'intç±»åçtriggerçç¬¬äºä¸ªåæ°',
  `long_prop_1` bigint DEFAULT NULL COMMENT 'longç±»åçtriggerçç¬¬ä¸ä¸ªåæ°',
  `long_prop_2` bigint DEFAULT NULL COMMENT 'longç±»åçtriggerçç¬¬äºä¸ªåæ°',
  `dec_prop_1` decimal(13,4) DEFAULT NULL COMMENT 'decimalç±»åçtriggerçç¬¬ä¸ä¸ªåæ°',
  `dec_prop_2` decimal(13,4) DEFAULT NULL COMMENT 'decimalç±»åçtriggerçç¬¬äºä¸ªåæ°',
  `bool_prop_1` varchar(1) DEFAULT NULL COMMENT 'Booleanç±»åçtriggerçç¬¬ä¸ä¸ªåæ°',
  `bool_prop_2` varchar(1) DEFAULT NULL COMMENT 'Booleanç±»åçtriggerçç¬¬äºä¸ªåæ°',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  CONSTRAINT `qrtz_simprop_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='åæ­¥æºå¶çè¡éè¡¨';

-- ----------------------------
-- Table structure for qrtz_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_triggers`;
CREATE TABLE `qrtz_triggers` (
  `sched_name` varchar(120) NOT NULL COMMENT 'è°åº¦åç§°',
  `trigger_name` varchar(200) NOT NULL COMMENT 'è§¦åå¨çåå­',
  `trigger_group` varchar(200) NOT NULL COMMENT 'è§¦åå¨æå±ç»çåå­',
  `job_name` varchar(200) NOT NULL COMMENT 'qrtz_job_detailsè¡¨job_nameçå¤é®',
  `job_group` varchar(200) NOT NULL COMMENT 'qrtz_job_detailsè¡¨job_groupçå¤é®',
  `description` varchar(250) DEFAULT NULL COMMENT 'ç¸å³ä»ç»',
  `next_fire_time` bigint DEFAULT NULL COMMENT 'ä¸ä¸æ¬¡è§¦åæ¶é´ï¼æ¯«ç§ï¼',
  `prev_fire_time` bigint DEFAULT NULL COMMENT 'ä¸ä¸æ¬¡è§¦åæ¶é´ï¼é»è®¤ä¸º-1è¡¨ç¤ºä¸è§¦åï¼',
  `priority` int DEFAULT NULL COMMENT 'ä¼åçº§',
  `trigger_state` varchar(16) NOT NULL COMMENT 'è§¦åå¨ç¶æ',
  `trigger_type` varchar(8) NOT NULL COMMENT 'è§¦åå¨çç±»å',
  `start_time` bigint NOT NULL COMMENT 'å¼å§æ¶é´',
  `end_time` bigint DEFAULT NULL COMMENT 'ç»ææ¶é´',
  `calendar_name` varchar(200) DEFAULT NULL COMMENT 'æ¥ç¨è¡¨åç§°',
  `misfire_instr` smallint DEFAULT NULL COMMENT 'è¡¥å¿æ§è¡çç­ç¥',
  `job_data` blob COMMENT 'å­æ¾æä¹åjobå¯¹è±¡',
  PRIMARY KEY (`sched_name`,`trigger_name`,`trigger_group`),
  KEY `sched_name` (`sched_name`,`job_name`,`job_group`),
  CONSTRAINT `qrtz_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `job_name`, `job_group`) REFERENCES `qrtz_job_details` (`sched_name`, `job_name`, `job_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='è§¦åå¨è¯¦ç»ä¿¡æ¯è¡¨';

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `config_id` int NOT NULL AUTO_INCREMENT COMMENT 'åæ°ä¸»é®',
  `config_name` varchar(100) DEFAULT '' COMMENT 'åæ°åç§°',
  `config_key` varchar(100) DEFAULT '' COMMENT 'åæ°é®å',
  `config_value` varchar(500) DEFAULT '' COMMENT 'åæ°é®å¼',
  `config_type` char(1) DEFAULT 'N' COMMENT 'ç³»ç»åç½®ï¼Yæ¯ Nå¦ï¼',
  `create_by` varchar(64) DEFAULT '' COMMENT 'åå»ºè',
  `create_time` datetime DEFAULT NULL COMMENT 'åå»ºæ¶é´',
  `update_by` varchar(64) DEFAULT '' COMMENT 'æ´æ°è',
  `update_time` datetime DEFAULT NULL COMMENT 'æ´æ°æ¶é´',
  `remark` varchar(500) DEFAULT NULL COMMENT 'å¤æ³¨',
  PRIMARY KEY (`config_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='åæ°éç½®è¡¨';

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` (`config_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, '主框架页-默认皮肤样式名称', 'sys.index.skinName', 'skin-blue', 'Y', 'admin', '2026-03-23 23:04:03', '', NULL, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow');
INSERT INTO `sys_config` (`config_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y', 'admin', '2026-03-23 23:04:03', '', NULL, '初始化密码 123456');
INSERT INTO `sys_config` (`config_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3, '主框架页-侧边栏主题', 'sys.index.sideTheme', 'theme-dark', 'Y', 'admin', '2026-03-23 23:04:03', '', NULL, '深色主题theme-dark，浅色主题theme-light');
INSERT INTO `sys_config` (`config_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4, '账号自助-验证码开关', 'sys.account.captchaEnabled', 'false', 'Y', 'admin', '2026-03-23 23:04:03', 'admin', '2026-11-22 11:30:38', '开发阶段默认关闭验证码功能（true开启，false关闭）');
INSERT INTO `sys_config` (`config_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'false', 'Y', 'admin', '2026-03-23 23:04:03', '', NULL, '是否开启注册用户功能（true开启，false关闭）');
INSERT INTO `sys_config` (`config_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (6, '用户登录-黑名单列表', 'sys.login.blackIPList', '', 'Y', 'admin', '2026-03-23 23:04:03', '', NULL, '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）');
INSERT INTO `sys_config` (`config_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (7, '用户管理-初始密码修改策略', 'sys.account.initPasswordModify', '1', 'Y', 'admin', '2026-03-23 23:04:03', '', NULL, '0：初始密码修改策略关闭，没有任何提示，1：提醒用户，如果未修改初始密码，则在登录时就会提醒修改密码对话框');
INSERT INTO `sys_config` (`config_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (8, '用户管理-账号密码更新周期', 'sys.account.passwordValidateDays', '0', 'Y', 'admin', '2026-03-23 23:04:03', '', NULL, '密码更新周期（填写数字，数据初始化值为0不限制，若修改必须为大于0小于365的正整数），如果超过这个周期登录系统时，则在登录时就会提醒修改密码对话框');
INSERT INTO `sys_config` (`config_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (20, '单据审批是否强制制审分离', 'biz.audit.preventSelfApprove', 'false', 'Y', 'admin', '2026-11-22 16:34:57', '', NULL, '开启(true)后禁止制单人审核本人提交的业务单据；开发环境默认false');

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
  `dept_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'é¨é¨id',
  `parent_id` bigint DEFAULT '0' COMMENT 'ç¶é¨é¨id',
  `ancestors` varchar(50) DEFAULT '' COMMENT 'ç¥çº§åè¡¨',
  `dept_name` varchar(30) DEFAULT '' COMMENT 'é¨é¨åç§°',
  `order_num` int DEFAULT '0' COMMENT 'æ¾ç¤ºé¡ºåº',
  `leader` varchar(20) DEFAULT NULL COMMENT 'è´è´£äºº',
  `phone` varchar(11) DEFAULT NULL COMMENT 'èç³»çµè¯',
  `email` varchar(50) DEFAULT NULL COMMENT 'é®ç®±',
  `status` char(1) DEFAULT '0' COMMENT 'é¨é¨ç¶æï¼0æ­£å¸¸ 1åç¨ï¼',
  `del_flag` char(1) DEFAULT '0' COMMENT 'å é¤æ å¿ï¼0ä»£è¡¨å­å¨ 2ä»£è¡¨å é¤ï¼',
  `create_by` varchar(64) DEFAULT '' COMMENT 'åå»ºè',
  `create_time` datetime DEFAULT NULL COMMENT 'åå»ºæ¶é´',
  `update_by` varchar(64) DEFAULT '' COMMENT 'æ´æ°è',
  `update_time` datetime DEFAULT NULL COMMENT 'æ´æ°æ¶é´',
  PRIMARY KEY (`dept_id`)
) ENGINE=InnoDB AUTO_INCREMENT=200 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='é¨é¨è¡¨';

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (100, 0, '0', 'SaleERP科技', 0, 'SaleERP管理员', '15888888888', 'admin@saleerp.local', '0', '0', 'admin', '2026-03-23 23:04:01', 'admin', '2026-04-16 21:36:26');
INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (101, 100, '0,100', '深圳总公司', 1, 'SaleERP管理员', '15888888888', 'admin@saleerp.local', '0', '0', 'admin', '2026-03-23 23:04:01', 'admin', '2026-04-16 21:36:26');
INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (102, 100, '0,100', '长沙分公司', 2, 'SaleERP管理员', '15888888888', 'admin@saleerp.local', '0', '0', 'admin', '2026-03-23 23:04:01', 'admin', '2026-04-16 21:36:26');
INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (103, 101, '0,100,101', '研发部门', 1, 'SaleERP管理员', '15888888888', 'admin@saleerp.local', '0', '0', 'admin', '2026-03-23 23:04:01', 'admin', '2026-04-16 21:36:26');
INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (104, 101, '0,100,101', '市场部门', 2, 'SaleERP管理员', '15888888888', 'admin@saleerp.local', '0', '0', 'admin', '2026-03-23 23:04:01', 'admin', '2026-04-16 21:36:26');
INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (105, 101, '0,100,101', '测试部门', 3, 'SaleERP管理员', '15888888888', 'admin@saleerp.local', '0', '0', 'admin', '2026-03-23 23:04:01', 'admin', '2026-04-16 21:36:26');
INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (106, 101, '0,100,101', '财务部门', 4, 'SaleERP管理员', '15888888888', 'admin@saleerp.local', '0', '0', 'admin', '2026-03-23 23:04:01', 'admin', '2026-04-16 21:36:26');
INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (107, 101, '0,100,101', '运维部门', 5, 'SaleERP管理员', '15888888888', 'admin@saleerp.local', '0', '0', 'admin', '2026-03-23 23:04:01', 'admin', '2026-04-16 21:36:26');
INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (108, 102, '0,100,102', '市场部门', 1, 'SaleERP管理员', '15888888888', 'admin@saleerp.local', '0', '0', 'admin', '2026-03-23 23:04:01', 'admin', '2026-04-16 21:36:26');
INSERT INTO `sys_dept` (`dept_id`, `parent_id`, `ancestors`, `dept_name`, `order_num`, `leader`, `phone`, `email`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (109, 102, '0,100,102', '财务部门', 2, 'SaleERP管理员', '15888888888', 'admin@saleerp.local', '0', '0', 'admin', '2026-03-23 23:04:01', 'admin', '2026-04-16 21:36:26');

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data` (
  `dict_code` bigint NOT NULL AUTO_INCREMENT COMMENT 'å­å¸ç¼ç ',
  `dict_sort` int DEFAULT '0' COMMENT 'å­å¸æåº',
  `dict_label` varchar(100) DEFAULT '' COMMENT 'å­å¸æ ç­¾',
  `dict_value` varchar(100) DEFAULT '' COMMENT 'å­å¸é®å¼',
  `dict_type` varchar(100) DEFAULT '' COMMENT 'å­å¸ç±»å',
  `css_class` varchar(100) DEFAULT NULL COMMENT 'æ ·å¼å±æ§ï¼å¶ä»æ ·å¼æ©å±ï¼',
  `list_class` varchar(100) DEFAULT NULL COMMENT 'è¡¨æ ¼åæ¾æ ·å¼',
  `is_default` char(1) DEFAULT 'N' COMMENT 'æ¯å¦é»è®¤ï¼Yæ¯ Nå¦ï¼',
  `status` char(1) DEFAULT '0' COMMENT 'ç¶æï¼0æ­£å¸¸ 1åç¨ï¼',
  `create_by` varchar(64) DEFAULT '' COMMENT 'åå»ºè',
  `create_time` datetime DEFAULT NULL COMMENT 'åå»ºæ¶é´',
  `update_by` varchar(64) DEFAULT '' COMMENT 'æ´æ°è',
  `update_time` datetime DEFAULT NULL COMMENT 'æ´æ°æ¶é´',
  `remark` varchar(500) DEFAULT NULL COMMENT 'å¤æ³¨',
  PRIMARY KEY (`dict_code`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='å­å¸æ°æ®è¡¨';

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, 1, '男', '0', 'sys_user_sex', '', '', 'Y', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '性别男');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, 2, '女', '1', 'sys_user_sex', '', '', 'N', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '性别女');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3, 3, '未知', '2', 'sys_user_sex', '', '', 'N', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '性别未知');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4, 1, '显示', '0', 'sys_show_hide', '', 'primary', 'Y', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '显示菜单');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5, 2, '隐藏', '1', 'sys_show_hide', '', 'danger', 'N', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '隐藏菜单');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (6, 1, '正常', '0', 'sys_normal_disable', '', 'primary', 'Y', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (7, 2, '停用', '1', 'sys_normal_disable', '', 'danger', 'N', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (8, 1, '正常', '0', 'sys_job_status', '', 'primary', 'Y', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9, 2, '暂停', '1', 'sys_job_status', '', 'danger', 'N', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (10, 1, '默认', 'DEFAULT', 'sys_job_group', '', '', 'Y', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '默认分组');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (11, 2, '系统', 'SYSTEM', 'sys_job_group', '', '', 'N', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '系统分组');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (12, 1, '是', 'Y', 'sys_yes_no', '', 'primary', 'Y', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '系统默认是');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (13, 2, '否', 'N', 'sys_yes_no', '', 'danger', 'N', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '系统默认否');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (14, 1, '通知', '1', 'sys_notice_type', '', 'warning', 'Y', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '通知');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (15, 2, '公告', '2', 'sys_notice_type', '', 'success', 'N', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '公告');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (16, 1, '正常', '0', 'sys_notice_status', '', 'primary', 'Y', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (17, 2, '关闭', '1', 'sys_notice_status', '', 'danger', 'N', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '关闭状态');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (18, 99, '其他', '0', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '其他操作');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (19, 1, '新增', '1', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '新增操作');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (20, 2, '修改', '2', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '修改操作');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (21, 3, '删除', '3', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '删除操作');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (22, 4, '授权', '4', 'sys_oper_type', '', 'primary', 'N', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '授权操作');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (23, 5, '导出', '5', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '导出操作');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (24, 6, '导入', '6', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '导入操作');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (25, 7, '强退', '7', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '强退操作');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (26, 8, '生成代码', '8', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '生成操作');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (27, 9, '清空数据', '9', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '清空操作');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (28, 1, '成功', '0', 'sys_common_status', '', 'primary', 'N', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` (`dict_code`, `dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (29, 2, '失败', '1', 'sys_common_status', '', 'danger', 'N', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '停用状态');

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type` (
  `dict_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'å­å¸ä¸»é®',
  `dict_name` varchar(100) DEFAULT '' COMMENT 'å­å¸åç§°',
  `dict_type` varchar(100) DEFAULT '' COMMENT 'å­å¸ç±»å',
  `status` char(1) DEFAULT '0' COMMENT 'ç¶æï¼0æ­£å¸¸ 1åç¨ï¼',
  `create_by` varchar(64) DEFAULT '' COMMENT 'åå»ºè',
  `create_time` datetime DEFAULT NULL COMMENT 'åå»ºæ¶é´',
  `update_by` varchar(64) DEFAULT '' COMMENT 'æ´æ°è',
  `update_time` datetime DEFAULT NULL COMMENT 'æ´æ°æ¶é´',
  `remark` varchar(500) DEFAULT NULL COMMENT 'å¤æ³¨',
  PRIMARY KEY (`dict_id`),
  UNIQUE KEY `dict_type` (`dict_type`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='å­å¸ç±»åè¡¨';

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, '用户性别', 'sys_user_sex', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '用户性别列表');
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, '菜单状态', 'sys_show_hide', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '菜单状态列表');
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3, '系统开关', 'sys_normal_disable', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '系统开关列表');
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4, '任务状态', 'sys_job_status', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '任务状态列表');
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5, '任务分组', 'sys_job_group', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '任务分组列表');
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (6, '系统是否', 'sys_yes_no', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '系统是否列表');
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (7, '通知类型', 'sys_notice_type', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '通知类型列表');
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (8, '通知状态', 'sys_notice_status', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '通知状态列表');
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9, '操作类型', 'sys_oper_type', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '操作类型列表');
INSERT INTO `sys_dict_type` (`dict_id`, `dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (10, '系统状态', 'sys_common_status', '0', 'admin', '2026-03-23 23:04:03', '', NULL, '登录状态列表');

-- ----------------------------
-- Table structure for sys_job
-- ----------------------------
DROP TABLE IF EXISTS `sys_job`;
CREATE TABLE `sys_job` (
  `job_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä»»å¡ID',
  `job_name` varchar(64) NOT NULL DEFAULT '' COMMENT 'ä»»å¡åç§°',
  `job_group` varchar(64) NOT NULL DEFAULT 'DEFAULT' COMMENT 'ä»»å¡ç»å',
  `invoke_target` varchar(500) NOT NULL COMMENT 'è°ç¨ç®æ å­ç¬¦ä¸²',
  `cron_expression` varchar(255) DEFAULT '' COMMENT 'cronæ§è¡è¡¨è¾¾å¼',
  `misfire_policy` varchar(20) DEFAULT '3' COMMENT 'è®¡åæ§è¡éè¯¯ç­ç¥ï¼1ç«å³æ§è¡ 2æ§è¡ä¸æ¬¡ 3æ¾å¼æ§è¡ï¼',
  `concurrent` char(1) DEFAULT '1' COMMENT 'æ¯å¦å¹¶åæ§è¡ï¼0åè®¸ 1ç¦æ­¢ï¼',
  `status` char(1) DEFAULT '0' COMMENT 'ç¶æï¼0æ­£å¸¸ 1æåï¼',
  `create_by` varchar(64) DEFAULT '' COMMENT 'åå»ºè',
  `create_time` datetime DEFAULT NULL COMMENT 'åå»ºæ¶é´',
  `update_by` varchar(64) DEFAULT '' COMMENT 'æ´æ°è',
  `update_time` datetime DEFAULT NULL COMMENT 'æ´æ°æ¶é´',
  `remark` varchar(500) DEFAULT '' COMMENT 'å¤æ³¨ä¿¡æ¯',
  PRIMARY KEY (`job_id`,`job_name`,`job_group`)
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='å®æ¶ä»»å¡è°åº¦è¡¨';

-- ----------------------------
-- Records of sys_job
-- ----------------------------
INSERT INTO `sys_job` (`job_id`, `job_name`, `job_group`, `invoke_target`, `cron_expression`, `misfire_policy`, `concurrent`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4, '应收到期提醒推送', 'DEFAULT', 'finReceivableRemindTask.pushDueReminder(\'7\')', '0 0 8 * * ?', '3', '1', '0', 'admin', '2026-03-23 23:04:04', '', NULL, '每天08:00推送应收到期与逾期提醒');
INSERT INTO `sys_job` (`job_id`, `job_name`, `job_group`, `invoke_target`, `cron_expression`, `misfire_policy`, `concurrent`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5, '库存预警提醒推送', 'DEFAULT', 'wmsStockWarningRemindTask.pushWarningReminder()', '0 30 8 * * ?', '3', '1', '0', 'admin', '2026-03-23 23:04:04', '', NULL, '每天08:30推送库存预警提醒');
INSERT INTO `sys_job` (`job_id`, `job_name`, `job_group`, `invoke_target`, `cron_expression`, `misfire_policy`, `concurrent`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (6, '业务数据自动备份', 'DEFAULT', 'bizDataBackupTask.autoBackup()', '0 0 2 * * ?', '3', '1', '0', 'admin', '2026-03-23 23:04:04', '', NULL, '每天02:00自动备份业务数据');
INSERT INTO `sys_job` (`job_id`, `job_name`, `job_group`, `invoke_target`, `cron_expression`, `misfire_policy`, `concurrent`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (100, '经营周报快照自动归档', 'DEFAULT', 'bizOperationReportTask.generateWeeklySnapshot()', '0 0 8 ? * MON', '3', '1', '1', 'admin', '2026-11-22 15:07:07', '', NULL, '每周一早8点自动汇总经营指标并归档周经营快照');

-- ----------------------------
-- Table structure for sys_job_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_job_log`;
CREATE TABLE `sys_job_log` (
  `job_log_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ä»»å¡æ¥å¿ID',
  `job_name` varchar(64) NOT NULL COMMENT 'ä»»å¡åç§°',
  `job_group` varchar(64) NOT NULL COMMENT 'ä»»å¡ç»å',
  `invoke_target` varchar(500) NOT NULL COMMENT 'è°ç¨ç®æ å­ç¬¦ä¸²',
  `job_message` varchar(500) DEFAULT NULL COMMENT 'æ¥å¿ä¿¡æ¯',
  `status` char(1) DEFAULT '0' COMMENT 'æ§è¡ç¶æï¼0æ­£å¸¸ 1å¤±è´¥ï¼',
  `exception_info` varchar(2000) DEFAULT '' COMMENT 'å¼å¸¸ä¿¡æ¯',
  `create_time` datetime DEFAULT NULL COMMENT 'åå»ºæ¶é´',
  PRIMARY KEY (`job_log_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='å®æ¶ä»»å¡è°åº¦æ¥å¿è¡¨';

-- ----------------------------
-- Table structure for sys_logininfor
-- ----------------------------
DROP TABLE IF EXISTS `sys_logininfor`;
CREATE TABLE `sys_logininfor` (
  `info_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'è®¿é®ID',
  `user_name` varchar(50) DEFAULT '' COMMENT 'ç¨æ·è´¦å·',
  `ipaddr` varchar(128) DEFAULT '' COMMENT 'ç»å½IPå°å',
  `login_location` varchar(255) DEFAULT '' COMMENT 'ç»å½å°ç¹',
  `browser` varchar(50) DEFAULT '' COMMENT 'æµè§å¨ç±»å',
  `os` varchar(50) DEFAULT '' COMMENT 'æä½ç³»ç»',
  `status` char(1) DEFAULT '0' COMMENT 'ç»å½ç¶æï¼0æå 1å¤±è´¥ï¼',
  `msg` varchar(255) DEFAULT '' COMMENT 'æç¤ºæ¶æ¯',
  `login_time` datetime DEFAULT NULL COMMENT 'è®¿é®æ¶é´',
  PRIMARY KEY (`info_id`),
  KEY `idx_sys_logininfor_s` (`status`),
  KEY `idx_sys_logininfor_lt` (`login_time`)
) ENGINE=InnoDB AUTO_INCREMENT=188 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç³»ç»è®¿é®è®°å½';

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'èåID',
  `menu_name` varchar(50) NOT NULL COMMENT 'èååç§°',
  `parent_id` bigint DEFAULT '0' COMMENT 'ç¶èåID',
  `order_num` int DEFAULT '0' COMMENT 'æ¾ç¤ºé¡ºåº',
  `path` varchar(200) DEFAULT '' COMMENT 'è·¯ç±å°å',
  `component` varchar(255) DEFAULT NULL COMMENT 'ç»ä»¶è·¯å¾',
  `query` varchar(255) DEFAULT NULL COMMENT 'è·¯ç±åæ°',
  `route_name` varchar(50) DEFAULT '' COMMENT 'è·¯ç±åç§°',
  `is_frame` int DEFAULT '1' COMMENT 'æ¯å¦ä¸ºå¤é¾ï¼0æ¯ 1å¦ï¼',
  `is_cache` int DEFAULT '0' COMMENT 'æ¯å¦ç¼å­ï¼0ç¼å­ 1ä¸ç¼å­ï¼',
  `menu_type` char(1) DEFAULT '' COMMENT 'èåç±»åï¼Mç®å½ Cèå Fæé®ï¼',
  `visible` char(1) DEFAULT '0' COMMENT 'èåç¶æï¼0æ¾ç¤º 1éèï¼',
  `status` char(1) DEFAULT '0' COMMENT 'èåç¶æï¼0æ­£å¸¸ 1åç¨ï¼',
  `perms` varchar(100) DEFAULT NULL COMMENT 'æéæ è¯',
  `icon` varchar(100) DEFAULT '#' COMMENT 'èåå¾æ ',
  `create_by` varchar(64) DEFAULT '' COMMENT 'åå»ºè',
  `create_time` datetime DEFAULT NULL COMMENT 'åå»ºæ¶é´',
  `update_by` varchar(64) DEFAULT '' COMMENT 'æ´æ°è',
  `update_time` datetime DEFAULT NULL COMMENT 'æ´æ°æ¶é´',
  `remark` varchar(500) DEFAULT '' COMMENT 'å¤æ³¨',
  PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5343 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='èåæéè¡¨';

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, '系统管理', 0, 1, 'system', NULL, '', '', 1, 0, 'M', '0', '0', '', 'system', 'admin', '2026-03-23 23:04:02', '', NULL, '系统管理目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, '系统监控', 1, 10, 'monitor', NULL, '', '', 1, 0, 'M', '0', '0', '', 'monitor', 'admin', '2026-03-23 23:04:02', '', NULL, '系统监控目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3, '系统工具', 1, 11, 'tool', NULL, '', '', 1, 0, 'M', '0', '0', '', 'tool', 'admin', '2026-03-23 23:04:02', '', NULL, '系统工具目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (100, '用户管理', 1, 1, 'user', 'system/user/index', '', '', 1, 0, 'C', '0', '0', 'system:user:list', 'user', 'admin', '2026-03-23 23:04:02', '', NULL, '用户管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (101, '角色管理', 1, 2, 'role', 'system/role/index', '', '', 1, 0, 'C', '0', '0', 'system:role:list', 'peoples', 'admin', '2026-03-23 23:04:02', '', NULL, '角色管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (102, '菜单管理', 1, 3, 'menu', 'system/menu/index', '', '', 1, 0, 'C', '0', '0', 'system:menu:list', 'tree-table', 'admin', '2026-03-23 23:04:02', '', NULL, '菜单管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (103, '部门管理', 1, 4, 'dept', 'system/dept/index', '', '', 1, 0, 'C', '0', '0', 'system:dept:list', 'tree', 'admin', '2026-03-23 23:04:02', '', NULL, '部门管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (104, '岗位管理', 1, 5, 'post', 'system/post/index', '', '', 1, 0, 'C', '0', '0', 'system:post:list', 'post', 'admin', '2026-03-23 23:04:02', '', NULL, '岗位管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (105, '字典管理', 1, 6, 'dict', 'system/dict/index', '', '', 1, 0, 'C', '0', '0', 'system:dict:list', 'dict', 'admin', '2026-03-23 23:04:02', '', NULL, '字典管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (106, '参数设置', 1, 7, 'config', 'system/config/index', '', '', 1, 0, 'C', '0', '0', 'system:config:list', 'edit', 'admin', '2026-03-23 23:04:02', '', NULL, '参数设置菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (107, '通知公告', 1, 8, 'notice', 'system/notice/index', '', '', 1, 0, 'C', '0', '0', 'system:notice:list', 'message', 'admin', '2026-03-23 23:04:02', '', NULL, '通知公告菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (108, '日志管理', 1, 9, 'log', '', '', '', 1, 0, 'M', '0', '0', '', 'log', 'admin', '2026-03-23 23:04:02', '', NULL, '日志管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (109, '在线用户', 2, 1, 'online', 'monitor/online/index', '', '', 1, 0, 'C', '0', '0', 'monitor:online:list', 'online', 'admin', '2026-03-23 23:04:02', '', NULL, '在线用户菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (110, '定时任务', 2, 2, 'job', 'monitor/job/index', '', '', 1, 0, 'C', '0', '0', 'monitor:job:list', 'job', 'admin', '2026-03-23 23:04:02', '', NULL, '定时任务菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (111, '数据监控', 2, 3, 'druid', 'monitor/druid/index', '', '', 1, 0, 'C', '0', '0', 'monitor:druid:list', 'druid', 'admin', '2026-03-23 23:04:02', '', NULL, '数据监控菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (112, '服务监控', 2, 4, 'server', 'monitor/server/index', '', '', 1, 0, 'C', '0', '0', 'monitor:server:list', 'server', 'admin', '2026-03-23 23:04:02', '', NULL, '服务监控菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (113, '缓存监控', 2, 5, 'cache', 'monitor/cache/index', '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list', 'redis', 'admin', '2026-03-23 23:04:02', '', NULL, '缓存监控菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (114, '缓存列表', 2, 6, 'cacheList', 'monitor/cache/list', '', '', 1, 0, 'C', '0', '0', 'monitor:cache:list', 'redis-list', 'admin', '2026-03-23 23:04:02', '', NULL, '缓存列表菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (115, '表单构建', 3, 1, 'build', 'tool/build/index', '', '', 1, 0, 'C', '0', '0', 'tool:build:list', 'build', 'admin', '2026-03-23 23:04:02', '', NULL, '表单构建菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (116, '代码生成', 3, 2, 'gen', 'tool/gen/index', '', '', 1, 0, 'C', '0', '0', 'tool:gen:list', 'code', 'admin', '2026-03-23 23:04:02', '', NULL, '代码生成菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (117, '系统接口', 3, 3, 'swagger', 'tool/swagger/index', '', '', 1, 0, 'C', '0', '0', 'tool:swagger:list', 'swagger', 'admin', '2026-03-23 23:04:02', '', NULL, '系统接口菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (500, '操作日志', 108, 1, 'operlog', 'monitor/operlog/index', '', '', 1, 0, 'C', '0', '0', 'monitor:operlog:list', 'form', 'admin', '2026-03-23 23:04:02', '', NULL, '操作日志菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (501, '登录日志', 108, 2, 'logininfor', 'monitor/logininfor/index', '', '', 1, 0, 'C', '0', '0', 'monitor:logininfor:list', 'logininfor', 'admin', '2026-03-23 23:04:02', '', NULL, '登录日志菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1000, '用户查询', 100, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:query', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1001, '用户新增', 100, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:add', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1002, '用户修改', 100, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:edit', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1003, '用户删除', 100, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:remove', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1004, '用户导出', 100, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:export', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1005, '用户导入', 100, 6, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:import', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1006, '重置密码', 100, 7, '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1007, '角色查询', 101, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:query', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1008, '角色新增', 101, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:add', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1009, '角色修改', 101, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:edit', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1010, '角色删除', 101, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:remove', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1011, '角色导出', 101, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:role:export', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1012, '菜单查询', 102, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:query', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1013, '菜单新增', 102, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:add', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1014, '菜单修改', 102, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:edit', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1015, '菜单删除', 102, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:menu:remove', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1016, '部门查询', 103, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:query', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1017, '部门新增', 103, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:add', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1018, '部门修改', 103, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:edit', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1019, '部门删除', 103, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:dept:remove', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1020, '岗位查询', 104, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:query', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1021, '岗位新增', 104, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:add', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1022, '岗位修改', 104, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:edit', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1023, '岗位删除', 104, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:remove', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1024, '岗位导出', 104, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'system:post:export', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1025, '字典查询', 105, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:query', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1026, '字典新增', 105, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:add', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1027, '字典修改', 105, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:edit', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1028, '字典删除', 105, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:remove', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1029, '字典导出', 105, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:dict:export', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1030, '参数查询', 106, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:query', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1031, '参数新增', 106, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:add', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1032, '参数修改', 106, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:edit', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1033, '参数删除', 106, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:remove', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1034, '参数导出', 106, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:config:export', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1035, '公告查询', 107, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:query', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1036, '公告新增', 107, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:add', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1037, '公告修改', 107, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:edit', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1038, '公告删除', 107, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'system:notice:remove', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1039, '操作查询', 500, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:query', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1040, '操作删除', 500, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:remove', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1041, '日志导出', 500, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:operlog:export', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1042, '登录查询', 501, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:query', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1043, '登录删除', 501, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:remove', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1044, '日志导出', 501, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:export', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1045, '账户解锁', 501, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:logininfor:unlock', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1046, '在线查询', 109, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:query', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1047, '批量强退', 109, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:batchLogout', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1048, '单条强退', 109, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:online:forceLogout', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1049, '任务查询', 110, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:query', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1050, '任务新增', 110, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:add', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1051, '任务修改', 110, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:edit', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1052, '任务删除', 110, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:remove', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1053, '状态修改', 110, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:changeStatus', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1054, '任务导出', 110, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'monitor:job:export', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1055, '生成查询', 116, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:query', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1056, '生成修改', 116, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:edit', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1057, '生成删除', 116, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:remove', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1058, '导入代码', 116, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:import', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1059, '预览代码', 116, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:preview', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1060, '生成代码', 116, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'tool:gen:code', '#', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2364, '核销确认', 3017, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:writeOff:confirm', '#', 'admin', '2026-04-16 22:28:40', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2470, '业务链路查询', 3013, 40, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:trace:query', '#', 'admin', '2026-05-12 17:25:38', '', NULL, '涓氬姟閾捐矾鏌ヨ鏉冮檺');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2471, '商品导入', 3001, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:product:import', '#', 'admin', '2026-05-12 17:25:38', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2472, '客户导入', 3002, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:customer:import', '#', 'admin', '2026-05-12 17:25:38', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2473, '供应商导入', 5205, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:supplier:import', '#', 'admin', '2026-05-12 17:25:38', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2474, '库存预警阈值设置', 3009, 20, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:stock:edit', '#', 'admin', '2026-05-12 17:25:38', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3001, '商品管理', 3023, 1, 'product', 'business/product/index', '', '', 1, 0, 'C', '0', '0', 'business:product:list', 'list', 'admin', '2026-03-23 23:04:06', '', NULL, '商品管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3002, '客户管理', 3023, 2, 'customer', 'business/customer/index', '', '', 1, 0, 'C', '0', '0', 'business:customer:list', 'user', 'admin', '2026-03-23 23:04:06', '', NULL, '客户管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3003, '仓库管理', 3024, 1, 'warehouse', 'business/warehouse/index', '', '', 1, 0, 'C', '0', '0', 'business:warehouse:list', 'tree-table', 'admin', '2026-03-23 23:04:06', '', NULL, '仓库管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3004, '库位管理', 3024, 2, 'location', 'business/location/index', '', '', 1, 0, 'C', '0', '0', 'business:location:list', 'guide', 'admin', '2026-03-23 23:04:06', '', NULL, '库位管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3005, '采购入库', 3025, 2, 'inbound', 'business/inbound/index', '', '', 1, 0, 'C', '0', '0', 'business:inbound:list', 'download', 'admin', '2026-03-23 23:04:06', '', NULL, '采购入库菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3007, '销售出库', 3026, 1, 'outbound', 'business/outbound/index', '', '', 1, 0, 'C', '0', '0', 'business:outbound:list', 'upload', 'admin', '2026-03-23 23:04:06', '', NULL, '销售出库菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3009, '库存台账', 3027, 1, 'stock', 'business/stock/index', '', '', 1, 0, 'C', '0', '0', 'business:stock:list', 'table', 'admin', '2026-03-23 23:04:06', '', NULL, '库存台账菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3010, '库存流水', 3027, 2, 'stockLog', 'business/stockLog/index', '', '', 1, 0, 'C', '0', '0', 'business:stockLog:list', 'time', 'admin', '2026-03-23 23:04:06', '', NULL, '库存流水菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3011, '采购退货', 3025, 3, 'purchaseReturn', 'business/purchaseReturn/index', '', '', 1, 0, 'C', '0', '0', 'business:purchaseReturn:list', 'more-up', 'admin', '2026-03-23 23:04:06', '', NULL, '采购退货菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3013, '销售订单', 3026, 2, 'saleOrder', 'business/saleOrder/index', '', '', 1, 0, 'C', '0', '0', 'business:saleOrder:list', 'clipboard', 'admin', '2026-03-23 23:04:06', '', NULL, '销售订单菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3015, '销售退货', 3026, 3, 'saleReturn', 'business/saleReturn/index', '', '', 1, 0, 'C', '0', '0', 'business:saleReturn:list', 'more-up', 'admin', '2026-03-23 23:04:06', '', NULL, '销售退货菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3017, '应收台账', 3028, 1, 'receivable', 'business/receivable/index', '', '', 1, 0, 'C', '0', '0', 'business:receivable:list', 'money', 'admin', '2026-03-23 23:04:06', '', NULL, '应收台账菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3018, '回款记录', 3028, 2, 'receipt', 'business/receipt/index', '', '', 1, 0, 'C', '0', '0', 'business:receipt:list', 'download', 'admin', '2026-03-23 23:04:06', '', NULL, '回款记录菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3019, '经营看板', 3028, 5, 'report', 'business/report/index', '', '', 1, 0, 'C', '0', '0', 'business:report:view', 'chart', 'admin', '2026-03-23 23:04:06', '', NULL, '经营看板菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3020, '库存盘点', 3027, 3, 'inventoryCheck', 'business/inventoryCheck/index', '', '', 1, 0, 'C', '0', '0', 'business:inventoryCheck:list', 'date', 'admin', '2026-03-23 23:04:06', '', NULL, '库存盘点菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3022, '数据备份', 3028, 6, 'dataBackup', 'business/dataBackup/index', '', '', 1, 0, 'C', '0', '0', 'business:dataBackup:list', 'server', 'admin', '2026-03-23 23:04:06', '', NULL, '数据备份菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3023, '基础资料', 0, 5, 'base', NULL, '', '', 1, 0, 'M', '0', '0', '', 'tree', 'admin', '2026-03-23 23:04:06', '', NULL, '基础资料目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3024, '仓储管理', 0, 6, 'warehouseGroup', NULL, '', '', 1, 0, 'M', '0', '0', '', 'guide', 'admin', '2026-03-23 23:04:06', '', NULL, '仓储管理目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3025, '采购管理', 0, 7, 'purchase', NULL, '', '', 1, 0, 'M', '0', '0', '', 'shopping', 'admin', '2026-03-23 23:04:06', '', NULL, '采购管理目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3026, '销售管理', 0, 8, 'sales', NULL, '', '', 1, 0, 'M', '0', '0', '', 'chart', 'admin', '2026-03-23 23:04:06', '', NULL, '销售管理目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3027, '库存管理', 0, 9, 'inventory', NULL, '', '', 1, 0, 'M', '0', '0', '', 'table', 'admin', '2026-03-23 23:04:06', '', NULL, '库存管理目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3028, '财务分析', 0, 10, 'finance', NULL, '', '', 1, 0, 'M', '0', '0', '', 'money', 'admin', '2026-03-23 23:04:06', '', NULL, '财务分析目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3101, '商品查询', 3001, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:product:query', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3102, '商品新增', 3001, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:product:add', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3103, '商品修改', 3001, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:product:edit', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3104, '商品删除', 3001, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:product:remove', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3105, '商品导出', 3001, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:product:export', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3201, '客户查询', 3002, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:customer:query', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3202, '客户新增', 3002, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:customer:add', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3203, '客户修改', 3002, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:customer:edit', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3204, '客户删除', 3002, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:customer:remove', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3205, '客户导出', 3002, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:customer:export', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3301, '仓库查询', 3003, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:warehouse:query', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3302, '仓库新增', 3003, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:warehouse:add', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3303, '仓库修改', 3003, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:warehouse:edit', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3304, '仓库删除', 3003, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:warehouse:remove', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3305, '仓库导出', 3003, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:warehouse:export', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3401, '库位查询', 3004, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:location:query', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3402, '库位新增', 3004, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:location:add', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3403, '库位修改', 3004, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:location:edit', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3404, '库位删除', 3004, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:location:remove', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3405, '库位导出', 3004, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:location:export', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3501, '入库查询', 3005, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inbound:query', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3502, '入库新增', 3005, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inbound:add', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3503, '入库修改', 3005, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inbound:edit', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3504, '入库删除', 3005, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inbound:remove', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3505, '入库导出', 3005, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inbound:export', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3506, '入库审核', 3005, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inbound:audit', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3701, '出库查询', 3007, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:outbound:query', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3702, '出库新增', 3007, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:outbound:add', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3703, '出库修改', 3007, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:outbound:edit', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3704, '出库删除', 3007, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:outbound:remove', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3705, '出库导出', 3007, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:outbound:export', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3706, '出库审核', 3007, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:outbound:audit', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3901, '库存查询', 3009, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:stock:query', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3902, '库存新增', 3009, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:stock:add', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3903, '库存修改', 3009, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:stock:edit', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3904, '库存删除', 3009, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:stock:remove', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3905, '库存导出', 3009, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:stock:export', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4001, '流水查询', 3010, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:stockLog:query', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4002, '流水导出', 3010, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:stockLog:export', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4101, '采购退货查询', 3011, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:purchaseReturn:query', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4102, '采购退货新增', 3011, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:purchaseReturn:add', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4103, '采购退货修改', 3011, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:purchaseReturn:edit', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4104, '采购退货删除', 3011, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:purchaseReturn:remove', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4105, '采购退货导出', 3011, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:purchaseReturn:export', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4106, '采购退货审核', 3011, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:purchaseReturn:audit', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4301, '订单查询', 3013, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:query', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4302, '订单新增', 3013, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:add', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4303, '订单修改', 3013, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:edit', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4304, '订单删除', 3013, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:remove', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4305, '订单导出', 3013, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:export', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4306, '经理审核', 3013, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:managerAudit', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4307, '订单导入', 3013, 7, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:import', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4308, '打印模板', 3013, 8, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:print', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4309, '财务审核', 3013, 9, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:financeAudit', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4310, '订单提交', 3013, 10, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleOrder:submit', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4501, '退货查询', 3015, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleReturn:query', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4502, '退货新增', 3015, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleReturn:add', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4503, '退货修改', 3015, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleReturn:edit', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4504, '退货删除', 3015, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleReturn:remove', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4505, '退货导出', 3015, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleReturn:export', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4506, '退货审核', 3015, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:saleReturn:audit', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4701, '应收查询', 3017, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receivable:query', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4702, '应收新增', 3017, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receivable:add', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4703, '应收修改', 3017, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receivable:edit', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4704, '应收删除', 3017, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receivable:remove', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4705, '应收导出', 3017, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receivable:export', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4706, '到期提醒查询', 3017, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receivable:due', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4707, '逾期视图查询', 3017, 7, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receivable:overdue', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4801, '回款查询', 3018, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receipt:query', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4802, '回款新增', 3018, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receipt:add', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4803, '回款修改', 3018, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receipt:edit', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4804, '回款删除', 3018, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receipt:remove', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4805, '回款导出', 3018, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:receipt:export', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4901, '看板查看', 3019, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:report:view', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4902, '看板编辑', 3019, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:report:edit', '#', 'admin', '2026-04-13 00:22:51', '', NULL, '经营看板编辑权限');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4903, '看板审批', 3019, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:report:approve', '#', 'admin', '2026-04-13 00:22:51', '', NULL, '经营看板审批权限');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4904, '简报归档', 3019, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:report:brief', '#', 'admin', '2026-04-13 00:22:51', '', NULL, '经营简报归档权限');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5001, '盘点查询', 3020, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inventoryCheck:query', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5002, '盘点新增', 3020, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inventoryCheck:add', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5003, '盘点修改', 3020, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inventoryCheck:edit', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5004, '盘点删除', 3020, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inventoryCheck:remove', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5005, '盘点导出', 3020, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inventoryCheck:export', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5006, '盘点审核', 3020, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:inventoryCheck:audit', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5201, '备份查询', 3022, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:dataBackup:query', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5202, '手动备份', 3022, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:dataBackup:add', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5203, '数据恢复', 3022, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:dataBackup:restore', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5204, '记录删除', 3022, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:dataBackup:remove', '#', 'admin', '2026-03-23 23:04:06', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5205, '供应商管理', 3023, 3, 'supplier', 'business/supplier/index', NULL, '', 1, 0, 'C', '0', '0', 'business:supplier:list', 'peoples', 'admin', '2026-03-23 23:04:08', '', NULL, '供应商管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5206, '供应商查询', 5205, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:supplier:query', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5207, '供应商新增', 5205, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:supplier:add', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5208, '供应商修改', 5205, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:supplier:edit', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5209, '供应商删除', 5205, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:supplier:remove', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5210, '供应商导出', 5205, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:supplier:export', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5211, '采购订单', 3025, 1, 'purchaseOrder', 'business/purchaseOrder/index', NULL, '', 1, 0, 'C', '0', '0', 'business:purchaseOrder:list', 'shopping', 'admin', '2026-03-23 23:04:08', '', NULL, '采购订单菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5212, '采购订单查询', 5211, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:purchaseOrder:query', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5213, '采购订单新增', 5211, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:purchaseOrder:add', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5214, '采购订单修改', 5211, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:purchaseOrder:edit', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5215, '采购订单删除', 5211, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:purchaseOrder:remove', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5216, '采购订单审核', 5211, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:purchaseOrder:audit', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5217, '采购订单导出', 5211, 6, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:purchaseOrder:export', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5218, '应付台账', 3028, 3, 'payable', 'business/payable/index', NULL, '', 1, 0, 'C', '0', '0', 'business:payable:list', 'money', 'admin', '2026-03-23 23:04:08', '', NULL, '应付台账菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5219, '应付台账查询', 5218, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:payable:query', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5220, '应付台账新增', 5218, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:payable:add', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5221, '应付台账修改', 5218, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:payable:edit', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5222, '应付台账删除', 5218, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:payable:remove', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5223, '应付台账导出', 5218, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:payable:export', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5224, '付款流水', 3028, 4, 'payment', 'business/payment/index', NULL, '', 1, 0, 'C', '0', '0', 'business:payment:list', 'upload', 'admin', '2026-03-23 23:04:08', '', NULL, '付款流水菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5225, '付款流水查询', 5224, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:payment:query', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5226, '付款流水新增', 5224, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:payment:add', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5227, '付款流水修改', 5224, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:payment:edit', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5228, '付款流水删除', 5224, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:payment:remove', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5229, '付款流水导出', 5224, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:payment:export', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5230, '库存调拨', 3027, 4, 'transfer', 'business/transfer/index', NULL, '', 1, 0, 'C', '0', '0', 'business:transfer:list', 'drag', 'admin', '2026-03-23 23:04:08', '', NULL, '库存调拨菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5231, '调拨单查询', 5230, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:transfer:query', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5232, '调拨单新增', 5230, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:transfer:add', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5233, '调拨单修改', 5230, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:transfer:edit', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5234, '调拨单删除', 5230, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:transfer:remove', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5235, '调拨单审核', 5230, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:transfer:audit', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5236, '调拨单导出', 5230, 6, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:transfer:export', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5237, '客户跟进', 3023, 4, 'customerFollow', 'business/customerFollow/index', NULL, '', 1, 0, 'C', '0', '0', 'business:customerFollow:list', 'people', 'admin', '2026-03-23 23:04:08', '', NULL, '客户跟进菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5238, '跟进记录查询', 5237, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:customerFollow:query', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5239, '跟进记录新增', 5237, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:customerFollow:add', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5240, '跟进记录修改', 5237, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:customerFollow:edit', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5241, '跟进记录删除', 5237, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:customerFollow:remove', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5242, '跟进记录导出', 5237, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:customerFollow:export', '#', 'admin', '2026-03-23 23:04:08', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5300, '统一待办中心', 0, 0, 'todoCenter', 'business/todoCenter/index', NULL, '', 1, 0, 'C', '0', '0', 'business:todoCenter:list', 'checkbox', 'admin', '2026-05-12 16:43:39', '', NULL, '统一待办中心菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5301, '待办中心查询', 5300, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'business:todoCenter:list', '#', 'admin', '2026-05-12 16:43:39', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5310, '发票管理', 3028, 5, 'invoice', 'business/invoice/index', '', '', 1, 0, 'C', '0', '0', 'business:invoice:list', 'documentation', 'admin', '2026-11-22 16:34:57', '', NULL, '销项与进项发票登记与红冲台账');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5311, '发票查询', 5310, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'business:invoice:query', '#', 'admin', '2026-11-22 16:34:57', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5312, '发票新增', 5310, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'business:invoice:add', '#', 'admin', '2026-11-22 16:34:57', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5313, '发票修改', 5310, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'business:invoice:edit', '#', 'admin', '2026-11-22 16:34:57', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5314, '发票红冲', 5310, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'business:invoice:void', '#', 'admin', '2026-11-22 16:34:57', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5315, '发票导出', 5310, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'business:invoice:export', '#', 'admin', '2026-11-22 16:34:57', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5320, '资金与月结', 3028, 7, 'account', 'business/account/index', '', '', 1, 0, 'C', '0', '0', 'business:account:list', 'money', 'admin', '2026-11-22 16:34:57', '', NULL, '资金结算账户余额与财务月结关账');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5321, '账户维护', 5320, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'business:account:edit', '#', 'admin', '2026-11-22 16:34:57', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5322, '财务月结关账', 5320, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'business:periodClose:manage', '#', 'admin', '2026-11-22 16:34:57', '', NULL, '');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5330, '移动仓配作业', 3027, 6, 'mobileWms', 'business/mobileWms/index', '', '', 1, 0, 'C', '0', '0', 'business:stock:list', 'phone', 'admin', '2026-11-22 16:34:57', '', NULL, '手机/PDA现场收货、拣货与扫码盘点工作台');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5335, '成本与毛利查看', 3021, 10, '', '', '', '', 1, 0, 'F', '0', '0', 'business:report:profit', '#', 'admin', '2026-11-22 16:34:57', '', NULL, '控制商品成本价与毛利指标可见性');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5336, '备份包下载', 3022, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'business:dataBackup:export', '#', 'admin', '2026-11-22 16:34:57', '', NULL, '下载离线压缩备份文件');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5340, '销售明细报表', 3028, 6, 'saleReport', 'business/report/saleReport', '', '', 1, 0, 'C', '0', '0', 'business:report:view', 'chart', 'admin', '2026-11-22 20:55:10', '', NULL, '销售明细统计报表');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5341, '库位导入', 3004, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:location:import', '#', 'admin', '2026-11-22 20:55:10', '', NULL, '库位批量导入权限');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5342, '库存冻结解冻', 3009, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'business:stock:freeze', '#', 'admin', '2026-11-22 20:55:10', '', NULL, '库存冻结与解冻操作权限');

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice` (
  `notice_id` int NOT NULL AUTO_INCREMENT COMMENT 'å¬åID',
  `notice_title` varchar(50) NOT NULL COMMENT 'å¬åæ é¢',
  `notice_type` char(1) NOT NULL COMMENT 'å¬åç±»åï¼1éç¥ 2å¬åï¼',
  `notice_content` longblob COMMENT 'å¬ååå®¹',
  `status` char(1) DEFAULT '0' COMMENT 'å¬åç¶æï¼0æ­£å¸¸ 1å³é­ï¼',
  `create_by` varchar(64) DEFAULT '' COMMENT 'åå»ºè',
  `create_time` datetime DEFAULT NULL COMMENT 'åå»ºæ¶é´',
  `update_by` varchar(64) DEFAULT '' COMMENT 'æ´æ°è',
  `update_time` datetime DEFAULT NULL COMMENT 'æ´æ°æ¶é´',
  `remark` varchar(255) DEFAULT NULL COMMENT 'å¤æ³¨',
  PRIMARY KEY (`notice_id`),
  KEY `idx_sys_notice_status_notice_id` (`status`,`notice_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='éç¥å¬åè¡¨';

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
INSERT INTO `sys_notice` (`notice_id`, `notice_title`, `notice_type`, `notice_content`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, '系统通知：SaleERP 统一待办中心与全链路追踪已上线', '2', X'e59084e983a8e997a8e5908ce4ba8be8afb7e6b3a8e6848fefbc9ae7b3bbe7bb9fe5b7b2e6ada3e5bc8fe590afe794a8e2809ce7bb9fe4b880e5be85e58a9ee4b8ade5bf83e2809de58f8ae98787e8b4ad2fe99480e594aee2809ce993bee8b7afe8bfbde8b8aae2809de8a786e59bbeefbc8ce8afb7e58f8ae697b6e5a484e79086e5908de4b88be5be85e5aea1e58d95e68daee4b88ee5ba93e5ad98e9a284e8ada6e4ba8be9a1b9e38082', '0', 'admin', '2026-03-23 23:04:04', 'admin', '2026-11-22 15:11:34', '管理员');
INSERT INTO `sys_notice` (`notice_id`, `notice_title`, `notice_type`, `notice_content`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, '经营提醒：月末仓库盘点与逾期应收账款集中核对', '1', X'e8afb7e4bb93e582a8e983a8e4ba8ee6af8fe69c88e69c80e5908ee4b880e4b8aae5b7a5e4bd9ce697a5e5898de5ae8ce68890e9878de782b9e5ba93e4bd8de79b98e782b9efbc8ce99480e594aee983a8e58d8fe5908ce8b4a2e58aa1e983a8e59ca8e2809ce5ba94e694b6e58fb0e8b4a6e2809de4b88ee2809ce5aea2e688b7e8b79fe8bf9be2809de4b8ade8b79fe8bf9be980bee69c9fe8b4a6e6acbee59b9ee6acbee8bf9be5baa6e38082', '0', 'admin', '2026-03-23 23:04:04', 'admin', '2026-11-22 15:11:34', '管理员');

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log` (
  `oper_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'æ¥å¿ä¸»é®',
  `title` varchar(50) DEFAULT '' COMMENT 'æ¨¡åæ é¢',
  `business_type` int DEFAULT '0' COMMENT 'ä¸å¡ç±»åï¼0å¶å® 1æ°å¢ 2ä¿®æ¹ 3å é¤ï¼',
  `method` varchar(200) DEFAULT '' COMMENT 'æ¹æ³åç§°',
  `request_method` varchar(10) DEFAULT '' COMMENT 'è¯·æ±æ¹å¼',
  `operator_type` int DEFAULT '0' COMMENT 'æä½ç±»å«ï¼0å¶å® 1åå°ç¨æ· 2ææºç«¯ç¨æ·ï¼',
  `oper_name` varchar(50) DEFAULT '' COMMENT 'æä½äººå',
  `dept_name` varchar(50) DEFAULT '' COMMENT 'é¨é¨åç§°',
  `oper_url` varchar(255) DEFAULT '' COMMENT 'è¯·æ±URL',
  `oper_ip` varchar(128) DEFAULT '' COMMENT 'ä¸»æºå°å',
  `oper_location` varchar(255) DEFAULT '' COMMENT 'æä½å°ç¹',
  `oper_param` varchar(2000) DEFAULT '' COMMENT 'è¯·æ±åæ°',
  `json_result` varchar(2000) DEFAULT '' COMMENT 'è¿ååæ°',
  `status` int DEFAULT '0' COMMENT 'æä½ç¶æï¼0æ­£å¸¸ 1å¼å¸¸ï¼',
  `error_msg` varchar(2000) DEFAULT '' COMMENT 'éè¯¯æ¶æ¯',
  `oper_time` datetime DEFAULT NULL COMMENT 'æä½æ¶é´',
  `cost_time` bigint DEFAULT '0' COMMENT 'æ¶èæ¶é´',
  PRIMARY KEY (`oper_id`),
  KEY `idx_sys_oper_log_bt` (`business_type`),
  KEY `idx_sys_oper_log_s` (`status`),
  KEY `idx_sys_oper_log_ot` (`oper_time`)
) ENGINE=InnoDB AUTO_INCREMENT=146 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='æä½æ¥å¿è®°å½';

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post` (
  `post_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'å²ä½ID',
  `post_code` varchar(64) NOT NULL COMMENT 'å²ä½ç¼ç ',
  `post_name` varchar(50) NOT NULL COMMENT 'å²ä½åç§°',
  `post_sort` int NOT NULL COMMENT 'æ¾ç¤ºé¡ºåº',
  `status` char(1) NOT NULL COMMENT 'ç¶æï¼0æ­£å¸¸ 1åç¨ï¼',
  `create_by` varchar(64) DEFAULT '' COMMENT 'åå»ºè',
  `create_time` datetime DEFAULT NULL COMMENT 'åå»ºæ¶é´',
  `update_by` varchar(64) DEFAULT '' COMMENT 'æ´æ°è',
  `update_time` datetime DEFAULT NULL COMMENT 'æ´æ°æ¶é´',
  `remark` varchar(500) DEFAULT NULL COMMENT 'å¤æ³¨',
  PRIMARY KEY (`post_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='å²ä½ä¿¡æ¯è¡¨';

-- ----------------------------
-- Records of sys_post
-- ----------------------------
INSERT INTO `sys_post` (`post_id`, `post_code`, `post_name`, `post_sort`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, 'ceo', '董事长', 1, '0', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_post` (`post_id`, `post_code`, `post_name`, `post_sort`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, 'se', '项目经理', 2, '0', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_post` (`post_id`, `post_code`, `post_name`, `post_sort`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3, 'hr', '人力资源', 3, '0', 'admin', '2026-03-23 23:04:02', '', NULL, '');
INSERT INTO `sys_post` (`post_id`, `post_code`, `post_name`, `post_sort`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4, 'user', '普通员工', 4, '0', 'admin', '2026-03-23 23:04:02', '', NULL, '');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'è§è²ID',
  `role_name` varchar(30) NOT NULL COMMENT 'è§è²åç§°',
  `role_key` varchar(100) NOT NULL COMMENT 'è§è²æéå­ç¬¦ä¸²',
  `role_sort` int NOT NULL COMMENT 'æ¾ç¤ºé¡ºåº',
  `data_scope` char(1) DEFAULT '1' COMMENT 'æ°æ®èå´ï¼1ï¼å¨é¨æ°æ®æé 2ï¼èªå®æ°æ®æé 3ï¼æ¬é¨é¨æ°æ®æé 4ï¼æ¬é¨é¨åä»¥ä¸æ°æ®æéï¼',
  `menu_check_strictly` tinyint(1) DEFAULT '1' COMMENT 'èåæ éæ©é¡¹æ¯å¦å³èæ¾ç¤º',
  `dept_check_strictly` tinyint(1) DEFAULT '1' COMMENT 'é¨é¨æ éæ©é¡¹æ¯å¦å³èæ¾ç¤º',
  `status` char(1) NOT NULL COMMENT 'è§è²ç¶æï¼0æ­£å¸¸ 1åç¨ï¼',
  `del_flag` char(1) DEFAULT '0' COMMENT 'å é¤æ å¿ï¼0ä»£è¡¨å­å¨ 2ä»£è¡¨å é¤ï¼',
  `create_by` varchar(64) DEFAULT '' COMMENT 'åå»ºè',
  `create_time` datetime DEFAULT NULL COMMENT 'åå»ºæ¶é´',
  `update_by` varchar(64) DEFAULT '' COMMENT 'æ´æ°è',
  `update_time` datetime DEFAULT NULL COMMENT 'æ´æ°æ¶é´',
  `remark` varchar(500) DEFAULT NULL COMMENT 'å¤æ³¨',
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='è§è²ä¿¡æ¯è¡¨';

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` (`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, '超级管理员', 'admin', 1, '1', 1, 1, '0', '0', 'admin', '2026-03-23 23:04:02', '', NULL, '超级管理员');
INSERT INTO `sys_role` (`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, '普通角色', 'common', 2, '2', 1, 1, '0', '0', 'admin', '2026-03-23 23:04:02', '', NULL, '普通角色');
INSERT INTO `sys_role` (`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3, '销售角色', 'sale', 3, '2', 1, 1, '0', '0', 'admin', '2026-03-23 23:04:02', '', NULL, '销售角色');
INSERT INTO `sys_role` (`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4, '经理角色', 'manager', 4, '2', 1, 1, '0', '0', 'admin', '2026-03-23 23:04:02', '', NULL, '经理角色');
INSERT INTO `sys_role` (`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5, '财务角色', 'finance', 5, '2', 1, 1, '0', '0', 'admin', '2026-03-23 23:04:02', '', NULL, '财务角色');

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept` (
  `role_id` bigint NOT NULL COMMENT 'è§è²ID',
  `dept_id` bigint NOT NULL COMMENT 'é¨é¨ID',
  PRIMARY KEY (`role_id`,`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='è§è²åé¨é¨å³èè¡¨';

-- ----------------------------
-- Records of sys_role_dept
-- ----------------------------
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`) VALUES (2, 100);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`) VALUES (2, 101);
INSERT INTO `sys_role_dept` (`role_id`, `dept_id`) VALUES (2, 105);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `role_id` bigint NOT NULL COMMENT 'è§è²ID',
  `menu_id` bigint NOT NULL COMMENT 'èåID',
  PRIMARY KEY (`role_id`,`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='è§è²åèåå³èè¡¨';

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 5340);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 5341);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 5342);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 3);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 100);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 101);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 102);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 103);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 104);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 105);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 106);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 107);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 108);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 109);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 110);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 111);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 112);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 113);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 114);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 115);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 116);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 117);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 500);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 501);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1000);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1001);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1002);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1003);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1004);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1005);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1006);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1007);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1008);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1009);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1010);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1011);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1012);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1013);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1014);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1015);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1016);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1017);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1018);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1019);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1020);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1021);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1022);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1023);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1024);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1025);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1026);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1027);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1028);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1029);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1030);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1031);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1032);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1033);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1034);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1035);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1036);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1037);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1038);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1039);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1040);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1041);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1042);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1043);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1044);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1045);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1046);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1047);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1048);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1049);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1050);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1051);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1052);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1053);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1054);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1055);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1056);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1057);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1058);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1059);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 1060);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2364);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2470);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2471);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2472);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2473);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 2474);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 5205);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 5206);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 5207);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 5208);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 5209);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 5210);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 5230);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 5231);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 5232);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 5233);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 5234);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 5235);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 5236);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 5237);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 5238);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 5239);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 5240);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 5241);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 5242);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 5300);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 5301);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 5330);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 5340);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 5341);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (2, 5342);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (3, 3013);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (3, 3026);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (3, 4301);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (3, 4302);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (3, 4303);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (3, 4304);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (3, 4305);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (3, 4307);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (3, 4308);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (3, 4310);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (4, 3013);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (4, 3026);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (4, 4301);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (4, 4306);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (4, 5310);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (4, 5311);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (4, 5320);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (4, 5330);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (4, 5335);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (5, 3013);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (5, 3026);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (5, 4301);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (5, 4309);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (5, 5310);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (5, 5311);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (5, 5312);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (5, 5313);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (5, 5314);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (5, 5315);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (5, 5320);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (5, 5321);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (5, 5322);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (5, 5335);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (5, 5336);
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (6, 2364);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ç¨æ·ID',
  `dept_id` bigint DEFAULT NULL COMMENT 'é¨é¨ID',
  `user_name` varchar(30) NOT NULL COMMENT 'ç¨æ·è´¦å·',
  `nick_name` varchar(30) NOT NULL COMMENT 'ç¨æ·æµç§°',
  `user_type` varchar(2) DEFAULT '00' COMMENT 'ç¨æ·ç±»åï¼00ç³»ç»ç¨æ·ï¼',
  `email` varchar(50) DEFAULT '' COMMENT 'ç¨æ·é®ç®±',
  `phonenumber` varchar(11) DEFAULT '' COMMENT 'ææºå·ç ',
  `sex` char(1) DEFAULT '0' COMMENT 'ç¨æ·æ§å«ï¼0ç· 1å¥³ 2æªç¥ï¼',
  `avatar` varchar(100) DEFAULT '' COMMENT 'å¤´åå°å',
  `password` varchar(100) DEFAULT '' COMMENT 'å¯ç ',
  `status` char(1) DEFAULT '0' COMMENT 'è´¦å·ç¶æï¼0æ­£å¸¸ 1åç¨ï¼',
  `del_flag` char(1) DEFAULT '0' COMMENT 'å é¤æ å¿ï¼0ä»£è¡¨å­å¨ 2ä»£è¡¨å é¤ï¼',
  `login_ip` varchar(128) DEFAULT '' COMMENT 'æåç»å½IP',
  `login_date` datetime DEFAULT NULL COMMENT 'æåç»å½æ¶é´',
  `pwd_update_date` datetime DEFAULT NULL COMMENT 'å¯ç æåæ´æ°æ¶é´',
  `create_by` varchar(64) DEFAULT '' COMMENT 'åå»ºè',
  `create_time` datetime DEFAULT NULL COMMENT 'åå»ºæ¶é´',
  `update_by` varchar(64) DEFAULT '' COMMENT 'æ´æ°è',
  `update_time` datetime DEFAULT NULL COMMENT 'æ´æ°æ¶é´',
  `remark` varchar(500) DEFAULT NULL COMMENT 'å¤æ³¨',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç¨æ·ä¿¡æ¯è¡¨';

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` (`user_id`, `dept_id`, `user_name`, `nick_name`, `user_type`, `email`, `phonenumber`, `sex`, `avatar`, `password`, `status`, `del_flag`, `login_ip`, `login_date`, `pwd_update_date`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, 103, 'admin', 'SaleERP管理员', '00', 'admin@saleerp.local', '15888888888', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', '2026-11-22 21:46:59', '2026-03-23 23:04:02', 'admin', '2026-03-23 23:04:02', '', '2026-04-16 21:36:26', '管理员');
INSERT INTO `sys_user` (`user_id`, `dept_id`, `user_name`, `nick_name`, `user_type`, `email`, `phonenumber`, `sex`, `avatar`, `password`, `status`, `del_flag`, `login_ip`, `login_date`, `pwd_update_date`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, 105, 'tester', '系统测试员', '00', 'tester@saleerp.local', '15666666666', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', '2026-03-23 23:04:02', '2026-03-23 23:04:02', 'admin', '2026-03-23 23:04:02', 'admin', '2026-11-22 11:30:38', '测试员');

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post` (
  `user_id` bigint NOT NULL COMMENT 'ç¨æ·ID',
  `post_id` bigint NOT NULL COMMENT 'å²ä½ID',
  PRIMARY KEY (`user_id`,`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç¨æ·ä¸å²ä½å³èè¡¨';

-- ----------------------------
-- Records of sys_user_post
-- ----------------------------
INSERT INTO `sys_user_post` (`user_id`, `post_id`) VALUES (1, 1);
INSERT INTO `sys_user_post` (`user_id`, `post_id`) VALUES (2, 2);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL COMMENT 'ç¨æ·ID',
  `role_id` bigint NOT NULL COMMENT 'è§è²ID',
  PRIMARY KEY (`user_id`,`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ç¨æ·åè§è²å³èè¡¨';

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (2, 2);

-- ----------------------------
-- Table structure for wms_inbound
-- ----------------------------
DROP TABLE IF EXISTS `wms_inbound`;
CREATE TABLE `wms_inbound` (
  `inbound_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Inbound primary key',
  `inbound_no` varchar(32) NOT NULL COMMENT 'Inbound number',
  `inbound_type` varchar(20) DEFAULT NULL COMMENT 'Inbound type',
  `purchase_order_id` bigint DEFAULT NULL COMMENT 'source purchase order id',
  `supplier_id` bigint DEFAULT NULL COMMENT 'Supplier id',
  `warehouse_id` bigint DEFAULT NULL COMMENT 'Warehouse id',
  `total_qty` decimal(18,2) DEFAULT '0.00' COMMENT 'Total quantity',
  `total_amount` decimal(18,2) DEFAULT '0.00' COMMENT 'Total amount',
  `status` varchar(20) DEFAULT 'draft' COMMENT 'Status',
  `audit_by` varchar(64) DEFAULT NULL COMMENT 'Auditor',
  `audit_time` datetime DEFAULT NULL COMMENT 'Audit time',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '审核驳回原因',
  `create_by` varchar(64) DEFAULT '' COMMENT 'Created by',
  `create_time` datetime DEFAULT NULL COMMENT 'Created time',
  `update_by` varchar(64) DEFAULT '' COMMENT 'Updated by',
  `update_time` datetime DEFAULT NULL COMMENT 'Updated time',
  `remark` varchar(255) DEFAULT NULL COMMENT 'Remark',
  PRIMARY KEY (`inbound_id`),
  UNIQUE KEY `uk_wms_inbound_no` (`inbound_no`),
  KEY `idx_wms_inbound_supplier_id` (`supplier_id`),
  KEY `idx_wms_inbound_warehouse_id` (`warehouse_id`),
  KEY `idx_wms_inbound_purchase_order_id` (`purchase_order_id`),
  KEY `idx_wms_inbound_status_create_time` (`status`,`create_time`),
  KEY `idx_wms_inbound_status_purchase_order_id` (`status`,`purchase_order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=990008 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Inbound order';

-- ----------------------------
-- Records of wms_inbound
-- ----------------------------
INSERT INTO `wms_inbound` (`inbound_id`, `inbound_no`, `inbound_type`, `purchase_order_id`, `supplier_id`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `audit_by`, `audit_time`, `reject_reason`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990001, 'in202603190001', 'purchase', NULL, 3, 990001, 120.00, 300.00, 'audited', 'admin', '2026-03-23 23:04:08', NULL, 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例入库单');
INSERT INTO `wms_inbound` (`inbound_id`, `inbound_no`, `inbound_type`, `purchase_order_id`, `supplier_id`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `audit_by`, `audit_time`, `reject_reason`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990002, 'in202603190002', 'purchase', NULL, 3, 990002, 90.00, 288.00, 'audited', 'admin', '2026-03-23 23:04:08', NULL, 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例入库单');
INSERT INTO `wms_inbound` (`inbound_id`, `inbound_no`, `inbound_type`, `purchase_order_id`, `supplier_id`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `audit_by`, `audit_time`, `reject_reason`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990003, 'in202603190003', 'purchase', NULL, 4, 990003, 60.00, 720.00, 'audited', 'admin', '2026-03-23 23:04:08', NULL, 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例入库单');
INSERT INTO `wms_inbound` (`inbound_id`, `inbound_no`, `inbound_type`, `purchase_order_id`, `supplier_id`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `audit_by`, `audit_time`, `reject_reason`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990004, 'in202603190004', 'purchase', NULL, 5, 990004, 45.00, 990.00, 'audited', 'admin', '2026-03-23 23:04:08', NULL, 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例入库单');
INSERT INTO `wms_inbound` (`inbound_id`, `inbound_no`, `inbound_type`, `purchase_order_id`, `supplier_id`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `audit_by`, `audit_time`, `reject_reason`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990005, 'in202603190005', 'purchase', NULL, 3, 990005, 150.00, 975.00, 'audited', 'admin', '2026-03-23 23:04:08', NULL, 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例入库单');
INSERT INTO `wms_inbound` (`inbound_id`, `inbound_no`, `inbound_type`, `purchase_order_id`, `supplier_id`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `audit_by`, `audit_time`, `reject_reason`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990006, 'IN20231026001', 'purchase', 102, 1, 990001, 20.00, 124000.00, 'submitted', NULL, NULL, NULL, 'admin', '2026-03-24 00:55:03', '', NULL, NULL);
INSERT INTO `wms_inbound` (`inbound_id`, `inbound_no`, `inbound_type`, `purchase_order_id`, `supplier_id`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `audit_by`, `audit_time`, `reject_reason`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990007, 'IN20231026002', 'purchase', 103, 2, 990002, 10.00, 75000.00, 'draft', NULL, NULL, NULL, 'admin', '2026-03-24 00:55:03', '', NULL, NULL);

-- ----------------------------
-- Table structure for wms_inbound_item
-- ----------------------------
DROP TABLE IF EXISTS `wms_inbound_item`;
CREATE TABLE `wms_inbound_item` (
  `inbound_item_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Inbound item primary key',
  `inbound_id` bigint NOT NULL COMMENT 'Inbound id',
  `purchase_order_item_id` bigint DEFAULT NULL COMMENT 'source purchase order item id',
  `product_id` bigint NOT NULL COMMENT 'Product id',
  `location_id` bigint DEFAULT NULL COMMENT 'Location id',
  `batch_no` varchar(64) DEFAULT NULL COMMENT 'Batch no',
  `expire_date` date DEFAULT NULL COMMENT '批次效期/到期日',
  `quantity` decimal(18,2) DEFAULT '0.00' COMMENT 'Quantity',
  `price` decimal(18,2) DEFAULT '0.00' COMMENT 'Price',
  `amount` decimal(18,2) DEFAULT '0.00' COMMENT 'Amount',
  PRIMARY KEY (`inbound_item_id`),
  KEY `idx_wms_inbound_item_inbound_id` (`inbound_id`),
  KEY `idx_wms_inbound_item_product_id` (`product_id`),
  KEY `idx_wms_inbound_item_purchase_order_item_id` (`purchase_order_item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=990008 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Inbound order item';

-- ----------------------------
-- Records of wms_inbound_item
-- ----------------------------
INSERT INTO `wms_inbound_item` (`inbound_item_id`, `inbound_id`, `purchase_order_item_id`, `product_id`, `location_id`, `batch_no`, `expire_date`, `quantity`, `price`, `amount`) VALUES (990001, 990001, NULL, 990001, 990001, 'batch-in-001', NULL, 120.00, 2.50, 300.00);
INSERT INTO `wms_inbound_item` (`inbound_item_id`, `inbound_id`, `purchase_order_item_id`, `product_id`, `location_id`, `batch_no`, `expire_date`, `quantity`, `price`, `amount`) VALUES (990002, 990002, NULL, 990002, 990002, 'batch-in-002', NULL, 90.00, 3.20, 288.00);
INSERT INTO `wms_inbound_item` (`inbound_item_id`, `inbound_id`, `purchase_order_item_id`, `product_id`, `location_id`, `batch_no`, `expire_date`, `quantity`, `price`, `amount`) VALUES (990003, 990003, NULL, 990003, 990003, 'batch-in-003', NULL, 60.00, 12.00, 720.00);
INSERT INTO `wms_inbound_item` (`inbound_item_id`, `inbound_id`, `purchase_order_item_id`, `product_id`, `location_id`, `batch_no`, `expire_date`, `quantity`, `price`, `amount`) VALUES (990004, 990004, NULL, 990004, 990004, 'batch-in-004', NULL, 45.00, 22.00, 990.00);
INSERT INTO `wms_inbound_item` (`inbound_item_id`, `inbound_id`, `purchase_order_item_id`, `product_id`, `location_id`, `batch_no`, `expire_date`, `quantity`, `price`, `amount`) VALUES (990005, 990005, NULL, 990005, 990005, 'batch-in-005', NULL, 150.00, 6.50, 975.00);
INSERT INTO `wms_inbound_item` (`inbound_item_id`, `inbound_id`, `purchase_order_item_id`, `product_id`, `location_id`, `batch_no`, `expire_date`, `quantity`, `price`, `amount`) VALUES (990006, 990006, 1001, 990012, 990001, 'B20231001', NULL, 20.00, 6200.00, 124000.00);
INSERT INTO `wms_inbound_item` (`inbound_item_id`, `inbound_id`, `purchase_order_item_id`, `product_id`, `location_id`, `batch_no`, `expire_date`, `quantity`, `price`, `amount`) VALUES (990007, 990007, 1002, 990013, 990002, 'B20231002', NULL, 10.00, 7500.00, 75000.00);

-- ----------------------------
-- Table structure for wms_inventory_check
-- ----------------------------
DROP TABLE IF EXISTS `wms_inventory_check`;
CREATE TABLE `wms_inventory_check` (
  `check_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'åºå­çç¹ä¸»é®',
  `check_no` varchar(32) NOT NULL COMMENT 'çç¹åå·',
  `warehouse_id` bigint NOT NULL COMMENT 'ä»åºç¼å·',
  `status` varchar(20) DEFAULT 'draft' COMMENT 'ç¶æ',
  `total_diff_qty` decimal(18,2) DEFAULT '0.00' COMMENT 'å·®å¼æ°é',
  `total_diff_amount` decimal(18,2) DEFAULT '0.00' COMMENT 'å·®å¼éé¢',
  `audit_by` varchar(64) DEFAULT NULL COMMENT 'å®¡æ ¸äºº',
  `audit_time` datetime DEFAULT NULL COMMENT 'å®¡æ ¸æ¶é´',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '审核驳回原因',
  `create_by` varchar(64) DEFAULT '' COMMENT 'åå»ºè',
  `create_time` datetime DEFAULT NULL COMMENT 'åå»ºæ¶é´',
  `update_by` varchar(64) DEFAULT '' COMMENT 'æ´æ°è',
  `update_time` datetime DEFAULT NULL COMMENT 'æ´æ°æ¶é´',
  `remark` varchar(255) DEFAULT NULL COMMENT 'å¤æ³¨',
  PRIMARY KEY (`check_id`),
  UNIQUE KEY `uk_wms_inventory_check_no` (`check_no`),
  KEY `idx_wms_inventory_check_warehouse_id` (`warehouse_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='åºå­çç¹ä¸»è¡¨';

-- ----------------------------
-- Records of wms_inventory_check
-- ----------------------------
INSERT INTO `wms_inventory_check` (`check_id`, `check_no`, `warehouse_id`, `status`, `total_diff_qty`, `total_diff_amount`, `audit_by`, `audit_time`, `reject_reason`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1001, 'IC202605010001', 990001, 'draft', 0.00, 0.00, NULL, NULL, NULL, 'admin', '2026-11-22 10:50:53', '', NULL, '华南一号仓月度例行抽盘单');

-- ----------------------------
-- Table structure for wms_inventory_check_item
-- ----------------------------
DROP TABLE IF EXISTS `wms_inventory_check_item`;
CREATE TABLE `wms_inventory_check_item` (
  `check_item_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'åºå­çç¹æç»ä¸»é®',
  `check_id` bigint NOT NULL COMMENT 'çç¹åç¼å·',
  `product_id` bigint NOT NULL COMMENT 'ååç¼å·',
  `location_id` bigint DEFAULT NULL COMMENT 'åºä½ç¼å·',
  `batch_no` varchar(64) DEFAULT NULL COMMENT 'æ¹æ¬¡å·',
  `stock_qty` decimal(18,2) DEFAULT '0.00' COMMENT 'è´¦é¢æ°é',
  `actual_qty` decimal(18,2) DEFAULT '0.00' COMMENT 'å®çæ°é',
  `diff_qty` decimal(18,2) DEFAULT '0.00' COMMENT 'å·®å¼æ°é',
  `price` decimal(18,2) DEFAULT '0.00' COMMENT 'åä»·',
  `diff_amount` decimal(18,2) DEFAULT '0.00' COMMENT 'å·®å¼éé¢',
  PRIMARY KEY (`check_item_id`),
  KEY `idx_wms_inventory_check_item_check_id` (`check_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='åºå­çç¹æç»è¡¨';

-- ----------------------------
-- Records of wms_inventory_check_item
-- ----------------------------
INSERT INTO `wms_inventory_check_item` (`check_item_id`, `check_id`, `product_id`, `location_id`, `batch_no`, `stock_qty`, `actual_qty`, `diff_qty`, `price`, `diff_amount`) VALUES (1001, 1001, 990011, 990001, 'B20231001', 50.00, 50.00, 0.00, 6800.00, 0.00);

-- ----------------------------
-- Table structure for wms_outbound
-- ----------------------------
DROP TABLE IF EXISTS `wms_outbound`;
CREATE TABLE `wms_outbound` (
  `outbound_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Outbound primary key',
  `outbound_no` varchar(32) NOT NULL COMMENT 'Outbound number',
  `outbound_type` varchar(20) DEFAULT NULL COMMENT 'Outbound type',
  `customer_id` bigint DEFAULT NULL COMMENT 'Customer id',
  `sale_order_id` bigint DEFAULT NULL COMMENT '来源销售单编号',
  `sale_order_no` varchar(32) DEFAULT NULL COMMENT '来源销售单号',
  `warehouse_id` bigint DEFAULT NULL COMMENT 'Warehouse id',
  `total_qty` decimal(18,2) DEFAULT '0.00' COMMENT 'Total quantity',
  `total_amount` decimal(18,2) DEFAULT '0.00' COMMENT 'Total amount',
  `status` varchar(20) DEFAULT 'draft' COMMENT 'Status',
  `audit_by` varchar(64) DEFAULT NULL COMMENT 'Auditor',
  `audit_time` datetime DEFAULT NULL COMMENT 'Audit time',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '审核驳回原因',
  `platform_type` varchar(20) DEFAULT NULL COMMENT 'Platform type',
  `store_id` bigint DEFAULT NULL COMMENT 'Store id',
  `source_order_no` varchar(64) DEFAULT NULL COMMENT 'Source order number',
  `carrier` varchar(64) DEFAULT NULL COMMENT 'Carrier',
  `waybill_no` varchar(64) DEFAULT NULL COMMENT 'Waybill number',
  `freight_cost` decimal(18,2) DEFAULT '0.00' COMMENT 'Freight cost',
  `delivery_status` varchar(20) DEFAULT NULL COMMENT 'Delivery status',
  `create_by` varchar(64) DEFAULT '' COMMENT 'Created by',
  `create_time` datetime DEFAULT NULL COMMENT 'Created time',
  `update_by` varchar(64) DEFAULT '' COMMENT 'Updated by',
  `update_time` datetime DEFAULT NULL COMMENT 'Updated time',
  `remark` varchar(255) DEFAULT NULL COMMENT 'Remark',
  PRIMARY KEY (`outbound_id`),
  UNIQUE KEY `uk_wms_outbound_no` (`outbound_no`),
  KEY `idx_wms_outbound_customer_id` (`customer_id`),
  KEY `idx_wms_outbound_warehouse_id` (`warehouse_id`),
  KEY `idx_wms_outbound_sale_order_id` (`sale_order_id`),
  KEY `idx_wms_outbound_status_create_time` (`status`,`create_time`),
  KEY `idx_wms_outbound_status_sale_order_id` (`status`,`sale_order_id`),
  KEY `idx_wms_outbound_status_source_order_no` (`status`,`source_order_no`)
) ENGINE=InnoDB AUTO_INCREMENT=990010 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Outbound order';

-- ----------------------------
-- Records of wms_outbound
-- ----------------------------
INSERT INTO `wms_outbound` (`outbound_id`, `outbound_no`, `outbound_type`, `customer_id`, `sale_order_id`, `sale_order_no`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `audit_by`, `audit_time`, `reject_reason`, `platform_type`, `store_id`, `source_order_no`, `carrier`, `waybill_no`, `freight_cost`, `delivery_status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990001, 'out202603190001', 'sale', 990001, NULL, NULL, 990001, 30.00, 120.00, 'audited', 'admin', '2026-03-23 23:04:08', NULL, 'online', 1, 'so-demo-001', '顺丰', 'sf1234567801cn', 10.00, 'delivered', 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例出库单');
INSERT INTO `wms_outbound` (`outbound_id`, `outbound_no`, `outbound_type`, `customer_id`, `sale_order_id`, `sale_order_no`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `audit_by`, `audit_time`, `reject_reason`, `platform_type`, `store_id`, `source_order_no`, `carrier`, `waybill_no`, `freight_cost`, `delivery_status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990002, 'out202603190002', 'sale', 990002, NULL, NULL, 990002, 20.00, 110.00, 'audited', 'admin', '2026-03-23 23:04:08', NULL, 'online', 2, 'so-demo-002', '京东', 'jd1234567802cn', 8.00, 'delivered', 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例出库单');
INSERT INTO `wms_outbound` (`outbound_id`, `outbound_no`, `outbound_type`, `customer_id`, `sale_order_id`, `sale_order_no`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `audit_by`, `audit_time`, `reject_reason`, `platform_type`, `store_id`, `source_order_no`, `carrier`, `waybill_no`, `freight_cost`, `delivery_status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990003, 'out202603190003', 'sale', 990003, NULL, NULL, 990003, 10.00, 180.00, 'audited', 'admin', '2026-03-23 23:04:08', NULL, 'offline', 3, 'so-demo-003', '韵达', 'yd1234567803cn', 6.00, 'delivered', 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例出库单');
INSERT INTO `wms_outbound` (`outbound_id`, `outbound_no`, `outbound_type`, `customer_id`, `sale_order_id`, `sale_order_no`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `audit_by`, `audit_time`, `reject_reason`, `platform_type`, `store_id`, `source_order_no`, `carrier`, `waybill_no`, `freight_cost`, `delivery_status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990004, 'out202603190004', 'sale', 990004, NULL, NULL, 990004, 8.00, 280.00, 'audited', 'admin', '2026-03-23 23:04:08', NULL, 'offline', 4, 'so-demo-004', '中通', 'zt1234567804cn', 5.00, 'delivered', 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例出库单');
INSERT INTO `wms_outbound` (`outbound_id`, `outbound_no`, `outbound_type`, `customer_id`, `sale_order_id`, `sale_order_no`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `audit_by`, `audit_time`, `reject_reason`, `platform_type`, `store_id`, `source_order_no`, `carrier`, `waybill_no`, `freight_cost`, `delivery_status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990005, 'out202603190005', 'sale', 990005, NULL, NULL, 990005, 25.00, 250.00, 'audited', 'admin', '2026-03-23 23:04:08', NULL, 'online', 5, 'so-demo-005', '圆通', 'yt1234567805cn', 9.00, 'delivered', 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例出库单');
INSERT INTO `wms_outbound` (`outbound_id`, `outbound_no`, `outbound_type`, `customer_id`, `sale_order_id`, `sale_order_no`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `audit_by`, `audit_time`, `reject_reason`, `platform_type`, `store_id`, `source_order_no`, `carrier`, `waybill_no`, `freight_cost`, `delivery_status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990006, 'OUT20231026001', 'sale', 990018, 9, 'SO20231026002', 990001, 5.00, 39995.00, 'submitted', NULL, NULL, NULL, NULL, NULL, 'SO20231026002', NULL, NULL, 0.00, 'pending', 'admin', '2026-03-24 00:55:03', '', NULL, NULL);
INSERT INTO `wms_outbound` (`outbound_id`, `outbound_no`, `outbound_type`, `customer_id`, `sale_order_id`, `sale_order_no`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `audit_by`, `audit_time`, `reject_reason`, `platform_type`, `store_id`, `source_order_no`, `carrier`, `waybill_no`, `freight_cost`, `delivery_status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990008, 'CK202604100001', 'sale', 990019, 8, 'SO20231026001', 990001, 3.00, 20997.00, 'audited', 'admin', '2026-04-10 14:20:06', NULL, NULL, NULL, 'SO20231026001', NULL, NULL, 0.00, 'pending', 'admin', '2026-04-10 14:19:19', 'admin', '2026-11-22 21:58:40', NULL);
INSERT INTO `wms_outbound` (`outbound_id`, `outbound_no`, `outbound_type`, `customer_id`, `sale_order_id`, `sale_order_no`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `audit_by`, `audit_time`, `reject_reason`, `platform_type`, `store_id`, `source_order_no`, `carrier`, `waybill_no`, `freight_cost`, `delivery_status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990009, 'CK20261119001', 'sale', 990018, 10, 'SO20261118001', 990001, 10.00, 79990.00, 'audited', 'admin', '2026-11-19 10:15:00', NULL, NULL, NULL, 'SO20261118001', '顺丰速运', 'SF202611190088', 65.00, 'delivered', 'admin', '2026-11-19 09:00:00', 'admin', '2026-11-20 09:30:00', '2026年11月顺丰签收出库');

-- ----------------------------
-- Table structure for wms_outbound_item
-- ----------------------------
DROP TABLE IF EXISTS `wms_outbound_item`;
CREATE TABLE `wms_outbound_item` (
  `outbound_item_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Outbound item primary key',
  `outbound_id` bigint NOT NULL COMMENT 'Outbound id',
  `sale_order_item_id` bigint DEFAULT NULL COMMENT '来源销售明细编号',
  `product_id` bigint NOT NULL COMMENT 'Product id',
  `location_id` bigint DEFAULT NULL COMMENT 'Location id',
  `batch_no` varchar(64) DEFAULT NULL COMMENT 'Batch no',
  `quantity` decimal(18,2) DEFAULT '0.00' COMMENT 'Quantity',
  `price` decimal(18,2) DEFAULT '0.00' COMMENT 'Price',
  `cost_price_snapshot` decimal(18,4) DEFAULT NULL COMMENT '出库审核瞬时单位成本快照',
  `amount` decimal(18,2) DEFAULT '0.00' COMMENT 'Amount',
  PRIMARY KEY (`outbound_item_id`),
  KEY `idx_wms_outbound_item_outbound_id` (`outbound_id`),
  KEY `idx_wms_outbound_item_product_id` (`product_id`),
  KEY `idx_wms_outbound_item_sale_order_item_id` (`sale_order_item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=990009 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Outbound order item';

-- ----------------------------
-- Records of wms_outbound_item
-- ----------------------------
INSERT INTO `wms_outbound_item` (`outbound_item_id`, `outbound_id`, `sale_order_item_id`, `product_id`, `location_id`, `batch_no`, `quantity`, `price`, `cost_price_snapshot`, `amount`) VALUES (990001, 990001, NULL, 990001, 990001, 'batch-in-001', 30.00, 4.00, 2.5000, 120.00);
INSERT INTO `wms_outbound_item` (`outbound_item_id`, `outbound_id`, `sale_order_item_id`, `product_id`, `location_id`, `batch_no`, `quantity`, `price`, `cost_price_snapshot`, `amount`) VALUES (990002, 990002, NULL, 990002, 990002, 'batch-in-002', 20.00, 5.50, 3.2000, 110.00);
INSERT INTO `wms_outbound_item` (`outbound_item_id`, `outbound_id`, `sale_order_item_id`, `product_id`, `location_id`, `batch_no`, `quantity`, `price`, `cost_price_snapshot`, `amount`) VALUES (990003, 990003, NULL, 990003, 990003, 'batch-in-003', 10.00, 18.00, 12.0000, 180.00);
INSERT INTO `wms_outbound_item` (`outbound_item_id`, `outbound_id`, `sale_order_item_id`, `product_id`, `location_id`, `batch_no`, `quantity`, `price`, `cost_price_snapshot`, `amount`) VALUES (990004, 990004, NULL, 990004, 990004, 'batch-in-004', 8.00, 35.00, 22.0000, 280.00);
INSERT INTO `wms_outbound_item` (`outbound_item_id`, `outbound_id`, `sale_order_item_id`, `product_id`, `location_id`, `batch_no`, `quantity`, `price`, `cost_price_snapshot`, `amount`) VALUES (990005, 990005, NULL, 990005, 990005, 'batch-in-005', 25.00, 10.00, 6.5000, 250.00);
INSERT INTO `wms_outbound_item` (`outbound_item_id`, `outbound_id`, `sale_order_item_id`, `product_id`, `location_id`, `batch_no`, `quantity`, `price`, `cost_price_snapshot`, `amount`) VALUES (990006, 990006, 9, 990011, 990001, 'B20231001', 5.00, 7999.00, 6800.0000, 39995.00);
INSERT INTO `wms_outbound_item` (`outbound_item_id`, `outbound_id`, `sale_order_item_id`, `product_id`, `location_id`, `batch_no`, `quantity`, `price`, `cost_price_snapshot`, `amount`) VALUES (990007, 990008, 8, 990012, 990001, 'B20231001', 3.00, 6999.00, 6200.0000, 20997.00);
INSERT INTO `wms_outbound_item` (`outbound_item_id`, `outbound_id`, `sale_order_item_id`, `product_id`, `location_id`, `batch_no`, `quantity`, `price`, `cost_price_snapshot`, `amount`) VALUES (990008, 990009, 10, 990011, 990001, 'B20261115', 10.00, 7999.00, 6800.0000, 79990.00);

-- ----------------------------
-- Table structure for wms_purchase_order
-- ----------------------------
DROP TABLE IF EXISTS `wms_purchase_order`;
CREATE TABLE `wms_purchase_order` (
  `purchase_order_id` bigint NOT NULL AUTO_INCREMENT COMMENT '采购订单ID',
  `order_no` varchar(64) NOT NULL COMMENT '采购单号',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID',
  `purchase_date` datetime DEFAULT NULL COMMENT '采购日期',
  `total_amount` decimal(10,2) DEFAULT '0.00' COMMENT '总金额',
  `invoiced_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '已收票金额',
  `invoice_status` varchar(20) NOT NULL DEFAULT 'uninvoiced' COMMENT '收票状态(uninvoiced未收票 partial部分收票 invoiced已收票)',
  `status` char(1) DEFAULT '0' COMMENT '状态（0待审核 1已审核 2部分入库 3已完成 4已作废）',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '审核驳回原因',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`purchase_order_id`),
  KEY `idx_wms_purchase_order_supplier_status` (`supplier_id`,`status`),
  KEY `idx_wms_purchase_order_status_create_time` (`status`,`create_time`),
  KEY `idx_wms_purchase_order_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=105 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购订单表';

-- ----------------------------
-- Records of wms_purchase_order
-- ----------------------------
INSERT INTO `wms_purchase_order` (`purchase_order_id`, `order_no`, `supplier_id`, `purchase_date`, `total_amount`, `invoiced_amount`, `invoice_status`, `status`, `reject_reason`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (102, 'PO20231024001', 1, '2026-03-23 00:00:00', 124000.00, 124000.00, 'invoiced', '0', NULL, '演示采购单-待审核', 'admin', '2026-03-23 00:55:03', '', NULL);
INSERT INTO `wms_purchase_order` (`purchase_order_id`, `order_no`, `supplier_id`, `purchase_date`, `total_amount`, `invoiced_amount`, `invoice_status`, `status`, `reject_reason`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (103, 'PO20231024002', 2, '2026-03-24 00:00:00', 75000.00, 75000.00, 'invoiced', '0', NULL, '演示采购单-待审核', 'admin', '2026-03-24 00:55:03', '', NULL);
INSERT INTO `wms_purchase_order` (`purchase_order_id`, `order_no`, `supplier_id`, `purchase_date`, `total_amount`, `invoiced_amount`, `invoice_status`, `status`, `reject_reason`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (104, 'PO20261115001', 1, '2026-11-15 09:30:00', 54400.00, 54400.00, 'invoiced', '3', NULL, '2026年11月主力机型补货采购', 'admin', '2026-11-15 09:30:00', 'admin', '2026-11-16 11:00:00');

-- ----------------------------
-- Table structure for wms_purchase_order_item
-- ----------------------------
DROP TABLE IF EXISTS `wms_purchase_order_item`;
CREATE TABLE `wms_purchase_order_item` (
  `purchase_order_item_id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `purchase_order_id` bigint NOT NULL COMMENT '采购订单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `quantity` decimal(10,2) NOT NULL COMMENT '采购数量',
  `price` decimal(10,2) NOT NULL COMMENT '单价',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`purchase_order_item_id`),
  KEY `idx_wms_purchase_order_item_order_product` (`purchase_order_id`,`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1004 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='采购订单明细表';

-- ----------------------------
-- Records of wms_purchase_order_item
-- ----------------------------
INSERT INTO `wms_purchase_order_item` (`purchase_order_item_id`, `purchase_order_id`, `product_id`, `quantity`, `price`, `amount`, `remark`) VALUES (1001, 102, 990012, 20.00, 6200.00, 124000.00, '华为 Mate 60 Pro 首批采购');
INSERT INTO `wms_purchase_order_item` (`purchase_order_item_id`, `purchase_order_id`, `product_id`, `quantity`, `price`, `amount`, `remark`) VALUES (1002, 103, 990013, 10.00, 7500.00, 75000.00, 'MacBook Air M2 季度备货');
INSERT INTO `wms_purchase_order_item` (`purchase_order_item_id`, `purchase_order_id`, `product_id`, `quantity`, `price`, `amount`, `remark`) VALUES (1003, 104, 990011, 8.00, 6800.00, 54400.00, '2026年11月采购补货');

-- ----------------------------
-- Table structure for wms_purchase_return
-- ----------------------------
DROP TABLE IF EXISTS `wms_purchase_return`;
CREATE TABLE `wms_purchase_return` (
  `purchase_return_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Purchase return primary key',
  `return_no` varchar(32) NOT NULL COMMENT 'Return number',
  `return_type` varchar(20) DEFAULT NULL COMMENT 'Return type',
  `supplier_id` bigint DEFAULT NULL COMMENT 'Supplier id',
  `purchase_order_id` bigint DEFAULT NULL COMMENT '关联采购订单ID',
  `warehouse_id` bigint DEFAULT NULL COMMENT 'Warehouse id',
  `total_qty` decimal(18,2) DEFAULT '0.00' COMMENT 'Total quantity',
  `total_amount` decimal(18,2) DEFAULT '0.00' COMMENT 'Total amount',
  `status` varchar(20) DEFAULT 'draft' COMMENT 'Status',
  `audit_by` varchar(64) DEFAULT NULL COMMENT 'Auditor',
  `audit_time` datetime DEFAULT NULL COMMENT 'Audit time',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '审核驳回原因',
  `create_by` varchar(64) DEFAULT '' COMMENT 'Created by',
  `create_time` datetime DEFAULT NULL COMMENT 'Created time',
  `update_by` varchar(64) DEFAULT '' COMMENT 'Updated by',
  `update_time` datetime DEFAULT NULL COMMENT 'Updated time',
  `remark` varchar(255) DEFAULT NULL COMMENT 'Remark',
  PRIMARY KEY (`purchase_return_id`),
  UNIQUE KEY `uk_wms_purchase_return_no` (`return_no`),
  KEY `idx_wms_purchase_return_supplier_id` (`supplier_id`),
  KEY `idx_wms_purchase_return_warehouse_id` (`warehouse_id`)
) ENGINE=InnoDB AUTO_INCREMENT=990006 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Purchase return order';

-- ----------------------------
-- Records of wms_purchase_return
-- ----------------------------
INSERT INTO `wms_purchase_return` (`purchase_return_id`, `return_no`, `return_type`, `supplier_id`, `purchase_order_id`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `audit_by`, `audit_time`, `reject_reason`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990001, 'pr202603190001', 'purchase_return', 3, NULL, 990001, 5.00, 12.50, 'audited', 'admin', '2026-03-23 23:04:08', NULL, 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例采购退货单');
INSERT INTO `wms_purchase_return` (`purchase_return_id`, `return_no`, `return_type`, `supplier_id`, `purchase_order_id`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `audit_by`, `audit_time`, `reject_reason`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990002, 'pr202603190002', 'purchase_return', 3, NULL, 990002, 3.00, 9.60, 'audited', 'admin', '2026-03-23 23:04:08', NULL, 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例采购退货单');
INSERT INTO `wms_purchase_return` (`purchase_return_id`, `return_no`, `return_type`, `supplier_id`, `purchase_order_id`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `audit_by`, `audit_time`, `reject_reason`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990003, 'pr202603190003', 'purchase_return', 4, NULL, 990003, 2.00, 24.00, 'audited', 'admin', '2026-03-23 23:04:08', NULL, 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例采购退货单');
INSERT INTO `wms_purchase_return` (`purchase_return_id`, `return_no`, `return_type`, `supplier_id`, `purchase_order_id`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `audit_by`, `audit_time`, `reject_reason`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990004, 'pr202603190004', 'purchase_return', 5, NULL, 990004, 1.00, 22.00, 'audited', 'admin', '2026-03-23 23:04:08', NULL, 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例采购退货单');
INSERT INTO `wms_purchase_return` (`purchase_return_id`, `return_no`, `return_type`, `supplier_id`, `purchase_order_id`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `audit_by`, `audit_time`, `reject_reason`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (990005, 'pr202603190005', 'purchase_return', 3, NULL, 990005, 4.00, 26.00, 'audited', 'admin', '2026-03-23 23:04:08', NULL, 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08', '业务样例采购退货单');

-- ----------------------------
-- Table structure for wms_purchase_return_item
-- ----------------------------
DROP TABLE IF EXISTS `wms_purchase_return_item`;
CREATE TABLE `wms_purchase_return_item` (
  `purchase_return_item_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Purchase return item primary key',
  `purchase_return_id` bigint NOT NULL COMMENT 'Purchase return id',
  `product_id` bigint NOT NULL COMMENT 'Product id',
  `location_id` bigint DEFAULT NULL COMMENT '退货出库库位ID',
  `batch_no` varchar(64) DEFAULT NULL COMMENT '退货批次号',
  `quantity` decimal(18,2) DEFAULT '0.00' COMMENT 'Quantity',
  `price` decimal(18,2) DEFAULT '0.00' COMMENT 'Price',
  `amount` decimal(18,2) DEFAULT '0.00' COMMENT 'Amount',
  PRIMARY KEY (`purchase_return_item_id`),
  KEY `idx_wms_purchase_return_item_return_id` (`purchase_return_id`),
  KEY `idx_wms_purchase_return_item_product_id` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=990006 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Purchase return order item';

-- ----------------------------
-- Records of wms_purchase_return_item
-- ----------------------------
INSERT INTO `wms_purchase_return_item` (`purchase_return_item_id`, `purchase_return_id`, `product_id`, `location_id`, `batch_no`, `quantity`, `price`, `amount`) VALUES (990001, 990001, 990001, NULL, NULL, 5.00, 2.50, 12.50);
INSERT INTO `wms_purchase_return_item` (`purchase_return_item_id`, `purchase_return_id`, `product_id`, `location_id`, `batch_no`, `quantity`, `price`, `amount`) VALUES (990002, 990002, 990002, NULL, NULL, 3.00, 3.20, 9.60);
INSERT INTO `wms_purchase_return_item` (`purchase_return_item_id`, `purchase_return_id`, `product_id`, `location_id`, `batch_no`, `quantity`, `price`, `amount`) VALUES (990003, 990003, 990003, NULL, NULL, 2.00, 12.00, 24.00);
INSERT INTO `wms_purchase_return_item` (`purchase_return_item_id`, `purchase_return_id`, `product_id`, `location_id`, `batch_no`, `quantity`, `price`, `amount`) VALUES (990004, 990004, 990004, NULL, NULL, 1.00, 22.00, 22.00);
INSERT INTO `wms_purchase_return_item` (`purchase_return_item_id`, `purchase_return_id`, `product_id`, `location_id`, `batch_no`, `quantity`, `price`, `amount`) VALUES (990005, 990005, 990005, NULL, NULL, 4.00, 6.50, 26.00);

-- ----------------------------
-- Table structure for wms_sale_order
-- ----------------------------
DROP TABLE IF EXISTS `wms_sale_order`;
CREATE TABLE `wms_sale_order` (
  `sale_order_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'éå®åä¸»é®',
  `order_no` varchar(32) NOT NULL COMMENT 'éå®åå·',
  `customer_id` bigint NOT NULL COMMENT 'å®¢æ·ç¼å·',
  `warehouse_id` bigint NOT NULL COMMENT 'ä»åºç¼å·',
  `total_qty` decimal(18,2) DEFAULT '0.00' COMMENT 'æ»æ°é',
  `total_amount` decimal(18,2) DEFAULT '0.00' COMMENT 'æ»éé¢',
  `status` varchar(20) DEFAULT 'draft' COMMENT 'ç¶æ',
  `payment_status` varchar(20) DEFAULT 'unpaid' COMMENT 'ä»æ¬¾ç¶æ',
  `invoiced_amount` decimal(18,2) NOT NULL DEFAULT '0.00' COMMENT '已开票金额',
  `invoice_status` varchar(20) NOT NULL DEFAULT 'uninvoiced' COMMENT '开票状态(uninvoiced未开票 partial部分开票 invoiced已开票)',
  `stock_deduct_mode` varchar(24) NOT NULL DEFAULT 'lock_on_audit' COMMENT '库存扣减模式(lock_on_audit锁库出库扣减 legacy_deducted历史审单已扣)',
  `manager_audit_by` varchar(64) DEFAULT NULL COMMENT 'ç»çå®¡æ ¸äºº',
  `manager_audit_time` datetime DEFAULT NULL COMMENT 'ç»çå®¡æ ¸æ¶é´',
  `manager_audit_comment` varchar(500) DEFAULT NULL COMMENT 'ç»çå®¡æ ¸æè§',
  `audit_by` varchar(64) DEFAULT NULL COMMENT 'å®¡æ ¸äºº',
  `audit_time` datetime DEFAULT NULL COMMENT 'å®¡æ ¸æ¶é´',
  `finance_audit_comment` varchar(500) DEFAULT NULL COMMENT 'è´¢å¡å®¡æ ¸æè§',
  `create_by` varchar(64) DEFAULT '' COMMENT 'åå»ºè',
  `create_time` datetime DEFAULT NULL COMMENT 'åå»ºæ¶é´',
  `update_by` varchar(64) DEFAULT '' COMMENT 'æ´æ°è',
  `update_time` datetime DEFAULT NULL COMMENT 'æ´æ°æ¶é´',
  `remark` varchar(255) DEFAULT NULL COMMENT 'å¤æ³¨',
  PRIMARY KEY (`sale_order_id`),
  UNIQUE KEY `uk_wms_sale_order_no` (`order_no`),
  KEY `idx_wms_sale_order_order_no` (`order_no`),
  KEY `idx_wms_sale_order_customer_status` (`customer_id`,`status`),
  KEY `idx_wms_sale_order_warehouse_status` (`warehouse_id`,`status`),
  KEY `idx_wms_sale_order_status_create_time` (`status`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='éå®è®¢åä¸»è¡¨';

-- ----------------------------
-- Records of wms_sale_order
-- ----------------------------
INSERT INTO `wms_sale_order` (`sale_order_id`, `order_no`, `customer_id`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `payment_status`, `invoiced_amount`, `invoice_status`, `stock_deduct_mode`, `manager_audit_by`, `manager_audit_time`, `manager_audit_comment`, `audit_by`, `audit_time`, `finance_audit_comment`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, 'SO20231020001', 990018, 990001, 10.00, 79990.00, 'audited', 'paid', 79990.00, 'invoiced', 'legacy_deducted', 'admin', '2026-03-18 01:55:03', '销售经理审核通过', 'admin', '2026-03-18 02:55:03', '财务复核通过', 'admin', '2026-03-18 00:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order` (`sale_order_id`, `order_no`, `customer_id`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `payment_status`, `invoiced_amount`, `invoice_status`, `stock_deduct_mode`, `manager_audit_by`, `manager_audit_time`, `manager_audit_comment`, `audit_by`, `audit_time`, `finance_audit_comment`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, 'SO20231021001', 990019, 990001, 5.00, 34995.00, 'audited', 'partial', 34995.00, 'invoiced', 'legacy_deducted', 'admin', '2026-03-19 01:55:03', '销售经理审核通过', 'admin', '2026-03-19 02:55:03', '财务复核通过', 'admin', '2026-03-19 00:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order` (`sale_order_id`, `order_no`, `customer_id`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `payment_status`, `invoiced_amount`, `invoice_status`, `stock_deduct_mode`, `manager_audit_by`, `manager_audit_time`, `manager_audit_comment`, `audit_by`, `audit_time`, `finance_audit_comment`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3, 'SO20231022001', 990020, 990002, 8.00, 71992.00, 'audited', 'unpaid', 0.00, 'uninvoiced', 'legacy_deducted', 'admin', '2026-03-20 01:55:03', '销售经理审核通过', 'admin', '2026-03-20 02:55:03', '财务复核通过', 'admin', '2026-03-20 00:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order` (`sale_order_id`, `order_no`, `customer_id`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `payment_status`, `invoiced_amount`, `invoice_status`, `stock_deduct_mode`, `manager_audit_by`, `manager_audit_time`, `manager_audit_comment`, `audit_by`, `audit_time`, `finance_audit_comment`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4, 'SO20231022002', 990021, 990002, 2.00, 19998.00, 'audited', 'unpaid', 0.00, 'uninvoiced', 'legacy_deducted', 'admin', '2026-03-20 01:55:03', '销售经理审核通过', 'admin', '2026-03-20 02:55:03', '财务复核通过', 'admin', '2026-03-20 00:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order` (`sale_order_id`, `order_no`, `customer_id`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `payment_status`, `invoiced_amount`, `invoice_status`, `stock_deduct_mode`, `manager_audit_by`, `manager_audit_time`, `manager_audit_comment`, `audit_by`, `audit_time`, `finance_audit_comment`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5, 'SO20231023001', 990022, 990003, 15.00, 28485.00, 'audited', 'unpaid', 0.00, 'uninvoiced', 'legacy_deducted', 'admin', '2026-03-21 01:55:03', '销售经理审核通过', 'admin', '2026-03-21 02:55:03', '财务复核通过', 'admin', '2026-03-21 00:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order` (`sale_order_id`, `order_no`, `customer_id`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `payment_status`, `invoiced_amount`, `invoice_status`, `stock_deduct_mode`, `manager_audit_by`, `manager_audit_time`, `manager_audit_comment`, `audit_by`, `audit_time`, `finance_audit_comment`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (6, 'SO20231024001', 990018, 990001, 6.00, 47994.00, 'audited', 'unpaid', 0.00, 'uninvoiced', 'legacy_deducted', 'admin', '2026-03-22 01:55:03', '销售经理审核通过', 'admin', '2026-03-22 02:55:03', '财务复核通过', 'admin', '2026-03-22 00:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order` (`sale_order_id`, `order_no`, `customer_id`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `payment_status`, `invoiced_amount`, `invoice_status`, `stock_deduct_mode`, `manager_audit_by`, `manager_audit_time`, `manager_audit_comment`, `audit_by`, `audit_time`, `finance_audit_comment`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (7, 'SO20231025001', 990023, 990002, 4.00, 35996.00, 'audited', 'unpaid', 0.00, 'uninvoiced', 'legacy_deducted', 'admin', '2026-03-23 01:55:03', '销售经理审核通过', 'admin', '2026-03-23 02:55:03', '财务复核通过', 'admin', '2026-03-23 00:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order` (`sale_order_id`, `order_no`, `customer_id`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `payment_status`, `invoiced_amount`, `invoice_status`, `stock_deduct_mode`, `manager_audit_by`, `manager_audit_time`, `manager_audit_comment`, `audit_by`, `audit_time`, `finance_audit_comment`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (8, 'SO20231026001', 990019, 990001, 3.00, 20997.00, 'audited', 'unpaid', 0.00, 'uninvoiced', 'legacy_deducted', 'admin', '2026-03-24 01:55:03', '销售经理审核通过', 'admin', '2026-03-24 02:55:03', '财务复核通过', 'admin', '2026-03-24 00:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order` (`sale_order_id`, `order_no`, `customer_id`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `payment_status`, `invoiced_amount`, `invoice_status`, `stock_deduct_mode`, `manager_audit_by`, `manager_audit_time`, `manager_audit_comment`, `audit_by`, `audit_time`, `finance_audit_comment`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9, 'SO20231026002', 990018, 990001, 5.00, 39995.00, 'submitted', 'unpaid', 0.00, 'uninvoiced', 'lock_on_audit', NULL, NULL, NULL, NULL, NULL, NULL, 'admin', '2026-03-24 00:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order` (`sale_order_id`, `order_no`, `customer_id`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `payment_status`, `invoiced_amount`, `invoice_status`, `stock_deduct_mode`, `manager_audit_by`, `manager_audit_time`, `manager_audit_comment`, `audit_by`, `audit_time`, `finance_audit_comment`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (10, 'SO20261118001', 990018, 990001, 10.00, 79990.00, 'audited', 'partial', 79990.00, 'invoiced', 'lock_on_audit', 'admin', '2026-11-18 11:00:00', '老客户授信正常，同意发货', 'admin', '2026-11-18 14:30:00', '财务审核通过', 'admin', '2026-11-18 09:30:00', 'admin', '2026-11-20 15:00:00', '2026年11月重点客户集采订单');

-- ----------------------------
-- Table structure for wms_sale_order_item
-- ----------------------------
DROP TABLE IF EXISTS `wms_sale_order_item`;
CREATE TABLE `wms_sale_order_item` (
  `sale_order_item_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'éå®æç»ä¸»é®',
  `sale_order_id` bigint NOT NULL COMMENT 'éå®åç¼å·',
  `product_id` bigint NOT NULL COMMENT 'ååç¼å·',
  `location_id` bigint DEFAULT NULL COMMENT 'åºä½ç¼å·',
  `batch_no` varchar(64) DEFAULT NULL COMMENT 'æ¹æ¬¡å·',
  `quantity` decimal(18,2) DEFAULT '0.00' COMMENT 'æ°é',
  `price` decimal(18,2) DEFAULT '0.00' COMMENT 'åä»·',
  `amount` decimal(18,2) DEFAULT '0.00' COMMENT 'éé¢',
  PRIMARY KEY (`sale_order_item_id`),
  KEY `idx_wms_sale_order_item_order_id` (`sale_order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='éå®è®¢åæç»è¡¨';

-- ----------------------------
-- Records of wms_sale_order_item
-- ----------------------------
INSERT INTO `wms_sale_order_item` (`sale_order_item_id`, `sale_order_id`, `product_id`, `location_id`, `batch_no`, `quantity`, `price`, `amount`) VALUES (1, 1, 990011, 990001, 'B20231001', 10.00, 7999.00, 79990.00);
INSERT INTO `wms_sale_order_item` (`sale_order_item_id`, `sale_order_id`, `product_id`, `location_id`, `batch_no`, `quantity`, `price`, `amount`) VALUES (2, 2, 990012, 990001, 'B20231001', 5.00, 6999.00, 34995.00);
INSERT INTO `wms_sale_order_item` (`sale_order_item_id`, `sale_order_id`, `product_id`, `location_id`, `batch_no`, `quantity`, `price`, `amount`) VALUES (3, 3, 990013, 990002, 'B20231002', 8.00, 8999.00, 71992.00);
INSERT INTO `wms_sale_order_item` (`sale_order_item_id`, `sale_order_id`, `product_id`, `location_id`, `batch_no`, `quantity`, `price`, `amount`) VALUES (4, 4, 990014, 990002, 'B20231002', 2.00, 9999.00, 19998.00);
INSERT INTO `wms_sale_order_item` (`sale_order_item_id`, `sale_order_id`, `product_id`, `location_id`, `batch_no`, `quantity`, `price`, `amount`) VALUES (5, 5, 990015, 990003, 'B20231003', 15.00, 1899.00, 28485.00);
INSERT INTO `wms_sale_order_item` (`sale_order_item_id`, `sale_order_id`, `product_id`, `location_id`, `batch_no`, `quantity`, `price`, `amount`) VALUES (6, 6, 990011, 990001, 'B20231001', 6.00, 7999.00, 47994.00);
INSERT INTO `wms_sale_order_item` (`sale_order_item_id`, `sale_order_id`, `product_id`, `location_id`, `batch_no`, `quantity`, `price`, `amount`) VALUES (7, 7, 990013, 990002, 'B20231002', 4.00, 8999.00, 35996.00);
INSERT INTO `wms_sale_order_item` (`sale_order_item_id`, `sale_order_id`, `product_id`, `location_id`, `batch_no`, `quantity`, `price`, `amount`) VALUES (8, 8, 990012, 990001, 'B20231001', 3.00, 6999.00, 20997.00);
INSERT INTO `wms_sale_order_item` (`sale_order_item_id`, `sale_order_id`, `product_id`, `location_id`, `batch_no`, `quantity`, `price`, `amount`) VALUES (9, 9, 990011, 990001, 'B20231001', 5.00, 7999.00, 39995.00);
INSERT INTO `wms_sale_order_item` (`sale_order_item_id`, `sale_order_id`, `product_id`, `location_id`, `batch_no`, `quantity`, `price`, `amount`) VALUES (10, 10, 990011, 990001, 'B20261115', 10.00, 7999.00, 79990.00);

-- ----------------------------
-- Table structure for wms_sale_order_status_history
-- ----------------------------
DROP TABLE IF EXISTS `wms_sale_order_status_history`;
CREATE TABLE `wms_sale_order_status_history` (
  `history_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ç¶æåå²ä¸»é®',
  `sale_order_id` bigint NOT NULL COMMENT 'éå®åç¼å·',
  `from_status` varchar(20) DEFAULT NULL COMMENT 'åç¶æ',
  `to_status` varchar(20) DEFAULT NULL COMMENT 'æ°ç¶æ',
  `operation_type` varchar(50) DEFAULT NULL COMMENT 'æä½ç±»å',
  `audit_role` varchar(30) DEFAULT NULL COMMENT 'å®¡æ ¸è§è²',
  `audit_by` varchar(64) DEFAULT NULL COMMENT 'æä½äºº',
  `audit_comment` varchar(500) DEFAULT NULL COMMENT 'å®¡æ ¸æè§',
  `operate_time` datetime DEFAULT NULL COMMENT 'æä½æ¶é´',
  `create_by` varchar(64) DEFAULT '' COMMENT 'åå»ºè',
  `create_time` datetime DEFAULT NULL COMMENT 'åå»ºæ¶é´',
  `update_by` varchar(64) DEFAULT '' COMMENT 'æ´æ°è',
  `update_time` datetime DEFAULT NULL COMMENT 'æ´æ°æ¶é´',
  `remark` varchar(255) DEFAULT NULL COMMENT 'å¤æ³¨',
  PRIMARY KEY (`history_id`),
  KEY `idx_wms_sale_order_status_history_order_id` (`sale_order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='éå®è®¢åç¶æåå²è¡¨';

-- ----------------------------
-- Records of wms_sale_order_status_history
-- ----------------------------
INSERT INTO `wms_sale_order_status_history` (`history_id`, `sale_order_id`, `from_status`, `to_status`, `operation_type`, `audit_role`, `audit_by`, `audit_comment`, `operate_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1, 1, 'draft', 'submitted', 'submit', 'sales', 'admin', '销售员提交订单', '2026-03-18 00:55:03', 'admin', '2026-03-18 00:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order_status_history` (`history_id`, `sale_order_id`, `from_status`, `to_status`, `operation_type`, `audit_role`, `audit_by`, `audit_comment`, `operate_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (2, 2, 'draft', 'submitted', 'submit', 'sales', 'admin', '销售员提交订单', '2026-03-19 00:55:03', 'admin', '2026-03-19 00:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order_status_history` (`history_id`, `sale_order_id`, `from_status`, `to_status`, `operation_type`, `audit_role`, `audit_by`, `audit_comment`, `operate_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (3, 3, 'draft', 'submitted', 'submit', 'sales', 'admin', '销售员提交订单', '2026-03-20 00:55:03', 'admin', '2026-03-20 00:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order_status_history` (`history_id`, `sale_order_id`, `from_status`, `to_status`, `operation_type`, `audit_role`, `audit_by`, `audit_comment`, `operate_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (4, 4, 'draft', 'submitted', 'submit', 'sales', 'admin', '销售员提交订单', '2026-03-20 00:55:03', 'admin', '2026-03-20 00:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order_status_history` (`history_id`, `sale_order_id`, `from_status`, `to_status`, `operation_type`, `audit_role`, `audit_by`, `audit_comment`, `operate_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (5, 5, 'draft', 'submitted', 'submit', 'sales', 'admin', '销售员提交订单', '2026-03-21 00:55:03', 'admin', '2026-03-21 00:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order_status_history` (`history_id`, `sale_order_id`, `from_status`, `to_status`, `operation_type`, `audit_role`, `audit_by`, `audit_comment`, `operate_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (6, 6, 'draft', 'submitted', 'submit', 'sales', 'admin', '销售员提交订单', '2026-03-22 00:55:03', 'admin', '2026-03-22 00:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order_status_history` (`history_id`, `sale_order_id`, `from_status`, `to_status`, `operation_type`, `audit_role`, `audit_by`, `audit_comment`, `operate_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (7, 7, 'draft', 'submitted', 'submit', 'sales', 'admin', '销售员提交订单', '2026-03-23 00:55:03', 'admin', '2026-03-23 00:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order_status_history` (`history_id`, `sale_order_id`, `from_status`, `to_status`, `operation_type`, `audit_role`, `audit_by`, `audit_comment`, `operate_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (8, 8, 'draft', 'submitted', 'submit', 'sales', 'admin', '销售员提交订单', '2026-03-24 00:55:03', 'admin', '2026-03-24 00:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order_status_history` (`history_id`, `sale_order_id`, `from_status`, `to_status`, `operation_type`, `audit_role`, `audit_by`, `audit_comment`, `operate_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (9, 9, 'draft', 'submitted', 'submit', 'sales', 'admin', '销售员提交订单', '2026-03-24 00:55:03', 'admin', '2026-03-24 00:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order_status_history` (`history_id`, `sale_order_id`, `from_status`, `to_status`, `operation_type`, `audit_role`, `audit_by`, `audit_comment`, `operate_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (16, 1, 'submitted', 'manager_approved', 'manager_approve', 'manager', 'admin', '销售经理审核通过', '2026-03-18 01:55:03', 'admin', '2026-03-18 01:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order_status_history` (`history_id`, `sale_order_id`, `from_status`, `to_status`, `operation_type`, `audit_role`, `audit_by`, `audit_comment`, `operate_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (17, 2, 'submitted', 'manager_approved', 'manager_approve', 'manager', 'admin', '销售经理审核通过', '2026-03-19 01:55:03', 'admin', '2026-03-19 01:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order_status_history` (`history_id`, `sale_order_id`, `from_status`, `to_status`, `operation_type`, `audit_role`, `audit_by`, `audit_comment`, `operate_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (18, 3, 'submitted', 'manager_approved', 'manager_approve', 'manager', 'admin', '销售经理审核通过', '2026-03-20 01:55:03', 'admin', '2026-03-20 01:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order_status_history` (`history_id`, `sale_order_id`, `from_status`, `to_status`, `operation_type`, `audit_role`, `audit_by`, `audit_comment`, `operate_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (19, 4, 'submitted', 'manager_approved', 'manager_approve', 'manager', 'admin', '销售经理审核通过', '2026-03-20 01:55:03', 'admin', '2026-03-20 01:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order_status_history` (`history_id`, `sale_order_id`, `from_status`, `to_status`, `operation_type`, `audit_role`, `audit_by`, `audit_comment`, `operate_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (20, 5, 'submitted', 'manager_approved', 'manager_approve', 'manager', 'admin', '销售经理审核通过', '2026-03-21 01:55:03', 'admin', '2026-03-21 01:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order_status_history` (`history_id`, `sale_order_id`, `from_status`, `to_status`, `operation_type`, `audit_role`, `audit_by`, `audit_comment`, `operate_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (21, 6, 'submitted', 'manager_approved', 'manager_approve', 'manager', 'admin', '销售经理审核通过', '2026-03-22 01:55:03', 'admin', '2026-03-22 01:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order_status_history` (`history_id`, `sale_order_id`, `from_status`, `to_status`, `operation_type`, `audit_role`, `audit_by`, `audit_comment`, `operate_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (22, 7, 'submitted', 'manager_approved', 'manager_approve', 'manager', 'admin', '销售经理审核通过', '2026-03-23 01:55:03', 'admin', '2026-03-23 01:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order_status_history` (`history_id`, `sale_order_id`, `from_status`, `to_status`, `operation_type`, `audit_role`, `audit_by`, `audit_comment`, `operate_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (23, 8, 'submitted', 'manager_approved', 'manager_approve', 'manager', 'admin', '销售经理审核通过', '2026-03-24 01:55:03', 'admin', '2026-03-24 01:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order_status_history` (`history_id`, `sale_order_id`, `from_status`, `to_status`, `operation_type`, `audit_role`, `audit_by`, `audit_comment`, `operate_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (31, 1, 'manager_approved', 'audited', 'finance_approve', 'finance', 'admin', '财务审核通过并确认额度', '2026-03-18 02:55:03', 'admin', '2026-03-18 02:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order_status_history` (`history_id`, `sale_order_id`, `from_status`, `to_status`, `operation_type`, `audit_role`, `audit_by`, `audit_comment`, `operate_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (32, 2, 'manager_approved', 'audited', 'finance_approve', 'finance', 'admin', '财务审核通过并确认额度', '2026-03-19 02:55:03', 'admin', '2026-03-19 02:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order_status_history` (`history_id`, `sale_order_id`, `from_status`, `to_status`, `operation_type`, `audit_role`, `audit_by`, `audit_comment`, `operate_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (33, 3, 'manager_approved', 'audited', 'finance_approve', 'finance', 'admin', '财务审核通过并确认额度', '2026-03-20 02:55:03', 'admin', '2026-03-20 02:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order_status_history` (`history_id`, `sale_order_id`, `from_status`, `to_status`, `operation_type`, `audit_role`, `audit_by`, `audit_comment`, `operate_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (34, 4, 'manager_approved', 'audited', 'finance_approve', 'finance', 'admin', '财务审核通过并确认额度', '2026-03-20 02:55:03', 'admin', '2026-03-20 02:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order_status_history` (`history_id`, `sale_order_id`, `from_status`, `to_status`, `operation_type`, `audit_role`, `audit_by`, `audit_comment`, `operate_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (35, 5, 'manager_approved', 'audited', 'finance_approve', 'finance', 'admin', '财务审核通过并确认额度', '2026-03-21 02:55:03', 'admin', '2026-03-21 02:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order_status_history` (`history_id`, `sale_order_id`, `from_status`, `to_status`, `operation_type`, `audit_role`, `audit_by`, `audit_comment`, `operate_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (36, 6, 'manager_approved', 'audited', 'finance_approve', 'finance', 'admin', '财务审核通过并确认额度', '2026-03-22 02:55:03', 'admin', '2026-03-22 02:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order_status_history` (`history_id`, `sale_order_id`, `from_status`, `to_status`, `operation_type`, `audit_role`, `audit_by`, `audit_comment`, `operate_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (37, 7, 'manager_approved', 'audited', 'finance_approve', 'finance', 'admin', '财务审核通过并确认额度', '2026-03-23 02:55:03', 'admin', '2026-03-23 02:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order_status_history` (`history_id`, `sale_order_id`, `from_status`, `to_status`, `operation_type`, `audit_role`, `audit_by`, `audit_comment`, `operate_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (38, 8, 'manager_approved', 'audited', 'finance_approve', 'finance', 'admin', '财务审核通过并确认额度', '2026-03-24 02:55:03', 'admin', '2026-03-24 02:55:03', '', NULL, NULL);
INSERT INTO `wms_sale_order_status_history` (`history_id`, `sale_order_id`, `from_status`, `to_status`, `operation_type`, `audit_role`, `audit_by`, `audit_comment`, `operate_time`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (46, 10, NULL, 'draft', 'create', 'system', 'admin', NULL, '2026-11-22 17:45:43', 'admin', '2026-11-22 17:45:41', '', NULL, NULL);

-- ----------------------------
-- Table structure for wms_sale_return
-- ----------------------------
DROP TABLE IF EXISTS `wms_sale_return`;
CREATE TABLE `wms_sale_return` (
  `sale_return_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'éå®éè´§ä¸»é®',
  `return_no` varchar(32) NOT NULL COMMENT 'éè´§åå·',
  `return_type` varchar(20) DEFAULT NULL COMMENT 'éè´§ç±»å',
  `customer_id` bigint NOT NULL COMMENT 'å®¢æ·ç¼å·',
  `sale_order_id` bigint DEFAULT NULL COMMENT '关联销售订单ID',
  `warehouse_id` bigint NOT NULL COMMENT 'ä»åºç¼å·',
  `total_qty` decimal(18,2) DEFAULT '0.00' COMMENT 'æ»æ°é',
  `total_amount` decimal(18,2) DEFAULT '0.00' COMMENT 'æ»éé¢',
  `status` varchar(20) DEFAULT 'draft' COMMENT 'ç¶æ',
  `audit_by` varchar(64) DEFAULT NULL COMMENT 'å®¡æ ¸äºº',
  `audit_time` datetime DEFAULT NULL COMMENT 'å®¡æ ¸æ¶é´',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '审核驳回原因',
  `create_by` varchar(64) DEFAULT '' COMMENT 'åå»ºè',
  `create_time` datetime DEFAULT NULL COMMENT 'åå»ºæ¶é´',
  `update_by` varchar(64) DEFAULT '' COMMENT 'æ´æ°è',
  `update_time` datetime DEFAULT NULL COMMENT 'æ´æ°æ¶é´',
  `remark` varchar(255) DEFAULT NULL COMMENT 'å¤æ³¨',
  PRIMARY KEY (`sale_return_id`),
  UNIQUE KEY `uk_wms_sale_return_no` (`return_no`),
  KEY `idx_wms_sale_return_customer_id` (`customer_id`),
  KEY `idx_wms_sale_return_warehouse_id` (`warehouse_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='éå®éè´§ä¸»è¡¨';

-- ----------------------------
-- Records of wms_sale_return
-- ----------------------------
INSERT INTO `wms_sale_return` (`sale_return_id`, `return_no`, `return_type`, `customer_id`, `sale_order_id`, `warehouse_id`, `total_qty`, `total_amount`, `status`, `audit_by`, `audit_time`, `reject_reason`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES (1001, 'SR202605010001', 'sale_return', 990018, NULL, 990001, 1.00, 7999.00, 'draft', NULL, NULL, NULL, 'admin', '2026-11-22 10:50:53', '', NULL, '客户外包装破损申请换退（草稿待审）');

-- ----------------------------
-- Table structure for wms_sale_return_item
-- ----------------------------
DROP TABLE IF EXISTS `wms_sale_return_item`;
CREATE TABLE `wms_sale_return_item` (
  `sale_return_item_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'éå®éè´§æç»ä¸»é®',
  `sale_return_id` bigint NOT NULL COMMENT 'éå®éè´§ç¼å·',
  `product_id` bigint NOT NULL COMMENT 'ååç¼å·',
  `location_id` bigint DEFAULT NULL COMMENT '退入库位ID',
  `batch_no` varchar(64) DEFAULT NULL COMMENT '退回批次号',
  `quantity` decimal(18,2) DEFAULT '0.00' COMMENT 'æ°é',
  `price` decimal(18,2) DEFAULT '0.00' COMMENT 'åä»·',
  `cost_price_snapshot` decimal(18,4) DEFAULT NULL COMMENT '退货审核瞬时单位成本快照',
  `amount` decimal(18,2) DEFAULT '0.00' COMMENT 'éé¢',
  PRIMARY KEY (`sale_return_item_id`),
  KEY `idx_wms_sale_return_item_return_id` (`sale_return_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='éå®éè´§æç»è¡¨';

-- ----------------------------
-- Records of wms_sale_return_item
-- ----------------------------
INSERT INTO `wms_sale_return_item` (`sale_return_item_id`, `sale_return_id`, `product_id`, `location_id`, `batch_no`, `quantity`, `price`, `cost_price_snapshot`, `amount`) VALUES (1001, 1001, 990011, NULL, NULL, 1.00, 7999.00, 6800.0000, 7999.00);

-- ----------------------------
-- Table structure for wms_stock
-- ----------------------------
DROP TABLE IF EXISTS `wms_stock`;
CREATE TABLE `wms_stock` (
  `stock_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Stock primary key',
  `warehouse_id` bigint NOT NULL COMMENT 'Warehouse id',
  `product_id` bigint NOT NULL COMMENT 'Product id',
  `location_id` bigint DEFAULT NULL COMMENT 'Location id',
  `batch_no` varchar(64) DEFAULT NULL COMMENT 'Batch no',
  `expire_date` date DEFAULT NULL COMMENT '批次效期/到期日',
  `quantity` decimal(18,2) DEFAULT '0.00' COMMENT 'Quantity',
  `locked_quantity` decimal(18,2) DEFAULT '0.00' COMMENT 'Locked quantity',
  `frozen_quantity` decimal(18,2) DEFAULT '0.00' COMMENT 'Frozen quantity',
  `warning_min_qty` decimal(18,2) DEFAULT NULL COMMENT 'Minimum warning quantity',
  `warning_max_qty` decimal(18,2) DEFAULT NULL COMMENT 'Maximum warning quantity',
  `version` int DEFAULT '0' COMMENT 'Version',
  `create_by` varchar(64) DEFAULT '' COMMENT 'Created by',
  `create_time` datetime DEFAULT NULL COMMENT 'Created time',
  `update_by` varchar(64) DEFAULT '' COMMENT 'Updated by',
  `update_time` datetime DEFAULT NULL COMMENT 'Updated time',
  PRIMARY KEY (`stock_id`),
  UNIQUE KEY `uk_wms_stock_key` (`warehouse_id`,`product_id`,`location_id`,`batch_no`),
  KEY `idx_wms_stock_product_id` (`product_id`),
  KEY `idx_wms_stock_warehouse_product_location_batch` (`warehouse_id`,`product_id`,`location_id`,`batch_no`),
  KEY `idx_wms_stock_location_id` (`location_id`),
  KEY `idx_wms_stock_warehouse_id` (`warehouse_id`)
) ENGINE=InnoDB AUTO_INCREMENT=990021 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Stock ledger';

-- ----------------------------
-- Records of wms_stock
-- ----------------------------
INSERT INTO `wms_stock` (`stock_id`, `warehouse_id`, `product_id`, `location_id`, `batch_no`, `expire_date`, `quantity`, `locked_quantity`, `frozen_quantity`, `warning_min_qty`, `warning_max_qty`, `version`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (990001, 990001, 990001, 990001, 'batch-in-001', '2026-12-04', 85.00, 3.00, 0.00, 10.00, 500.00, 3, 'admin', '2026-03-23 23:04:08', 'admin', '2026-11-22 21:50:06');
INSERT INTO `wms_stock` (`stock_id`, `warehouse_id`, `product_id`, `location_id`, `batch_no`, `expire_date`, `quantity`, `locked_quantity`, `frozen_quantity`, `warning_min_qty`, `warning_max_qty`, `version`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (990002, 990002, 990002, 990002, 'batch-in-002', '2027-02-05', 67.00, 2.00, 0.00, 8.00, 400.00, 1, 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08');
INSERT INTO `wms_stock` (`stock_id`, `warehouse_id`, `product_id`, `location_id`, `batch_no`, `expire_date`, `quantity`, `locked_quantity`, `frozen_quantity`, `warning_min_qty`, `warning_max_qty`, `version`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (990003, 990003, 990003, 990003, 'batch-in-003', NULL, 48.00, 1.00, 0.00, 6.00, 300.00, 1, 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08');
INSERT INTO `wms_stock` (`stock_id`, `warehouse_id`, `product_id`, `location_id`, `batch_no`, `expire_date`, `quantity`, `locked_quantity`, `frozen_quantity`, `warning_min_qty`, `warning_max_qty`, `version`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (990004, 990004, 990004, 990004, 'batch-in-004', '2027-05-21', 36.00, 1.00, 0.00, 4.00, 200.00, 1, 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08');
INSERT INTO `wms_stock` (`stock_id`, `warehouse_id`, `product_id`, `location_id`, `batch_no`, `expire_date`, `quantity`, `locked_quantity`, `frozen_quantity`, `warning_min_qty`, `warning_max_qty`, `version`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (990005, 990005, 990005, 990005, 'batch-in-005', '2026-11-27', 121.00, 5.00, 0.00, 12.00, 600.00, 1, 'admin', '2026-03-23 23:04:08', 'admin', '2026-03-23 23:04:08');
INSERT INTO `wms_stock` (`stock_id`, `warehouse_id`, `product_id`, `location_id`, `batch_no`, `expire_date`, `quantity`, `locked_quantity`, `frozen_quantity`, `warning_min_qty`, `warning_max_qty`, `version`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (990016, 990001, 990011, 990001, 'B20231001', NULL, 50.00, 0.00, 0.00, 20.00, 200.00, 0, 'admin', '2026-03-24 00:55:03', '', NULL);
INSERT INTO `wms_stock` (`stock_id`, `warehouse_id`, `product_id`, `location_id`, `batch_no`, `expire_date`, `quantity`, `locked_quantity`, `frozen_quantity`, `warning_min_qty`, `warning_max_qty`, `version`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (990017, 990001, 990012, 990001, 'B20231001', NULL, 15.00, 0.00, 0.00, 30.00, 200.00, 0, 'admin', '2026-03-24 00:55:03', '', NULL);
INSERT INTO `wms_stock` (`stock_id`, `warehouse_id`, `product_id`, `location_id`, `batch_no`, `expire_date`, `quantity`, `locked_quantity`, `frozen_quantity`, `warning_min_qty`, `warning_max_qty`, `version`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (990018, 990002, 990013, 990002, 'B20231002', NULL, 8.00, 0.00, 0.00, 10.00, 100.00, 0, 'admin', '2026-03-24 00:55:03', '', NULL);
INSERT INTO `wms_stock` (`stock_id`, `warehouse_id`, `product_id`, `location_id`, `batch_no`, `expire_date`, `quantity`, `locked_quantity`, `frozen_quantity`, `warning_min_qty`, `warning_max_qty`, `version`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (990019, 990002, 990014, 990002, 'B20231002', NULL, 120.00, 0.00, 0.00, 10.00, 100.00, 0, 'admin', '2026-03-24 00:55:03', '', NULL);
INSERT INTO `wms_stock` (`stock_id`, `warehouse_id`, `product_id`, `location_id`, `batch_no`, `expire_date`, `quantity`, `locked_quantity`, `frozen_quantity`, `warning_min_qty`, `warning_max_qty`, `version`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (990020, 990003, 990015, 990003, 'B20231003', NULL, 300.00, 0.00, 0.00, 50.00, 500.00, 0, 'admin', '2026-03-24 00:55:03', '', NULL);

-- ----------------------------
-- Table structure for wms_stock_log
-- ----------------------------
DROP TABLE IF EXISTS `wms_stock_log`;
CREATE TABLE `wms_stock_log` (
  `stock_log_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Stock log primary key',
  `warehouse_id` bigint NOT NULL COMMENT 'Warehouse id',
  `product_id` bigint NOT NULL COMMENT 'Product id',
  `location_id` bigint DEFAULT NULL COMMENT 'Location id',
  `batch_no` varchar(64) DEFAULT NULL COMMENT 'Batch no',
  `bill_type` varchar(30) DEFAULT NULL COMMENT 'Bill type',
  `bill_id` bigint DEFAULT NULL COMMENT 'Bill id',
  `in_out` varchar(10) NOT NULL COMMENT 'In or out',
  `quantity` decimal(18,2) DEFAULT '0.00' COMMENT 'Quantity',
  `price` decimal(18,2) DEFAULT '0.00' COMMENT 'Price',
  `amount` decimal(18,2) DEFAULT '0.00' COMMENT 'Amount',
  `before_qty` decimal(18,2) DEFAULT '0.00' COMMENT 'Before quantity',
  `after_qty` decimal(18,2) DEFAULT '0.00' COMMENT 'After quantity',
  `create_by` varchar(64) DEFAULT '' COMMENT 'Created by',
  `create_time` datetime DEFAULT NULL COMMENT 'Created time',
  PRIMARY KEY (`stock_log_id`),
  KEY `idx_wms_stock_log_stock_key` (`warehouse_id`,`product_id`,`location_id`,`batch_no`),
  KEY `idx_wms_stock_log_bill` (`bill_type`,`bill_id`),
  KEY `idx_wms_stock_log_location_id` (`location_id`),
  KEY `idx_wms_stock_log_product_id` (`product_id`),
  KEY `idx_wms_stock_log_warehouse_id` (`warehouse_id`)
) ENGINE=InnoDB AUTO_INCREMENT=990013 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Stock change log';

-- ----------------------------
-- Records of wms_stock_log
-- ----------------------------
INSERT INTO `wms_stock_log` (`stock_log_id`, `warehouse_id`, `product_id`, `location_id`, `batch_no`, `bill_type`, `bill_id`, `in_out`, `quantity`, `price`, `amount`, `before_qty`, `after_qty`, `create_by`, `create_time`) VALUES (990001, 990001, 990001, 990001, 'batch-in-001', 'inbound', 990001, 'in', 120.00, 2.50, 300.00, 0.00, 120.00, 'admin', '2026-03-23 23:04:08');
INSERT INTO `wms_stock_log` (`stock_log_id`, `warehouse_id`, `product_id`, `location_id`, `batch_no`, `bill_type`, `bill_id`, `in_out`, `quantity`, `price`, `amount`, `before_qty`, `after_qty`, `create_by`, `create_time`) VALUES (990002, 990001, 990001, 990001, 'batch-in-001', 'outbound', 990001, 'out', 30.00, 4.00, 120.00, 120.00, 90.00, 'admin', '2026-03-23 23:04:08');
INSERT INTO `wms_stock_log` (`stock_log_id`, `warehouse_id`, `product_id`, `location_id`, `batch_no`, `bill_type`, `bill_id`, `in_out`, `quantity`, `price`, `amount`, `before_qty`, `after_qty`, `create_by`, `create_time`) VALUES (990003, 990001, 990001, 990001, 'batch-in-001', 'purchase_return', 990001, 'out', 5.00, 2.50, 12.50, 90.00, 85.00, 'admin', '2026-03-23 23:04:08');
INSERT INTO `wms_stock_log` (`stock_log_id`, `warehouse_id`, `product_id`, `location_id`, `batch_no`, `bill_type`, `bill_id`, `in_out`, `quantity`, `price`, `amount`, `before_qty`, `after_qty`, `create_by`, `create_time`) VALUES (990004, 990002, 990002, 990002, 'batch-in-002', 'inbound', 990002, 'in', 90.00, 3.20, 288.00, 0.00, 90.00, 'admin', '2026-03-23 23:04:08');
INSERT INTO `wms_stock_log` (`stock_log_id`, `warehouse_id`, `product_id`, `location_id`, `batch_no`, `bill_type`, `bill_id`, `in_out`, `quantity`, `price`, `amount`, `before_qty`, `after_qty`, `create_by`, `create_time`) VALUES (990005, 990002, 990002, 990002, 'batch-in-002', 'outbound', 990002, 'out', 20.00, 5.50, 110.00, 90.00, 70.00, 'admin', '2026-03-23 23:04:08');
INSERT INTO `wms_stock_log` (`stock_log_id`, `warehouse_id`, `product_id`, `location_id`, `batch_no`, `bill_type`, `bill_id`, `in_out`, `quantity`, `price`, `amount`, `before_qty`, `after_qty`, `create_by`, `create_time`) VALUES (990006, 990002, 990002, 990002, 'batch-in-002', 'purchase_return', 990002, 'out', 3.00, 3.20, 9.60, 70.00, 67.00, 'admin', '2026-03-23 23:04:08');
INSERT INTO `wms_stock_log` (`stock_log_id`, `warehouse_id`, `product_id`, `location_id`, `batch_no`, `bill_type`, `bill_id`, `in_out`, `quantity`, `price`, `amount`, `before_qty`, `after_qty`, `create_by`, `create_time`) VALUES (990007, 990003, 990003, 990003, 'batch-in-003', 'inbound', 990003, 'in', 60.00, 12.00, 720.00, 0.00, 60.00, 'admin', '2026-03-23 23:04:08');
INSERT INTO `wms_stock_log` (`stock_log_id`, `warehouse_id`, `product_id`, `location_id`, `batch_no`, `bill_type`, `bill_id`, `in_out`, `quantity`, `price`, `amount`, `before_qty`, `after_qty`, `create_by`, `create_time`) VALUES (990008, 990003, 990003, 990003, 'batch-in-003', 'outbound', 990003, 'out', 10.00, 18.00, 180.00, 60.00, 50.00, 'admin', '2026-03-23 23:04:08');
INSERT INTO `wms_stock_log` (`stock_log_id`, `warehouse_id`, `product_id`, `location_id`, `batch_no`, `bill_type`, `bill_id`, `in_out`, `quantity`, `price`, `amount`, `before_qty`, `after_qty`, `create_by`, `create_time`) VALUES (990009, 990004, 990004, 990004, 'batch-in-004', 'inbound', 990004, 'in', 45.00, 22.00, 990.00, 0.00, 45.00, 'admin', '2026-03-23 23:04:08');
INSERT INTO `wms_stock_log` (`stock_log_id`, `warehouse_id`, `product_id`, `location_id`, `batch_no`, `bill_type`, `bill_id`, `in_out`, `quantity`, `price`, `amount`, `before_qty`, `after_qty`, `create_by`, `create_time`) VALUES (990010, 990005, 990005, 990005, 'batch-in-005', 'inbound', 990005, 'in', 150.00, 6.50, 975.00, 0.00, 150.00, 'admin', '2026-03-23 23:04:08');

-- ----------------------------
-- Table structure for wms_transfer
-- ----------------------------
DROP TABLE IF EXISTS `wms_transfer`;
CREATE TABLE `wms_transfer` (
  `transfer_id` bigint NOT NULL AUTO_INCREMENT COMMENT '调拨单ID',
  `transfer_no` varchar(64) NOT NULL COMMENT '调拨单号',
  `out_warehouse_id` bigint NOT NULL COMMENT '调出仓库ID',
  `in_warehouse_id` bigint NOT NULL COMMENT '调入仓库ID',
  `transfer_date` datetime DEFAULT NULL COMMENT '调拨日期',
  `status` char(1) DEFAULT '0' COMMENT '状态（0待审核 1已审核 2已作废）',
  `receive_by` varchar(64) DEFAULT NULL COMMENT '调入签收人',
  `receive_time` datetime DEFAULT NULL COMMENT '调入签收时间',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '审核驳回原因',
  `total_quantity` decimal(10,2) DEFAULT '0.00' COMMENT '总数量',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`transfer_id`),
  KEY `idx_wms_transfer_out_warehouse_id` (`out_warehouse_id`),
  KEY `idx_wms_transfer_in_warehouse_id` (`in_warehouse_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存调拨表';

-- ----------------------------
-- Records of wms_transfer
-- ----------------------------
INSERT INTO `wms_transfer` (`transfer_id`, `transfer_no`, `out_warehouse_id`, `in_warehouse_id`, `transfer_date`, `status`, `receive_by`, `receive_time`, `reject_reason`, `total_quantity`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES (1001, 'TF202605010001', 990001, 990004, '2026-11-22 10:50:53', '0', NULL, NULL, NULL, 5.00, '向广州前置仓补充热销机型库存', 'admin', '2026-11-22 10:50:53', '', NULL);

-- ----------------------------
-- Table structure for wms_transfer_item
-- ----------------------------
DROP TABLE IF EXISTS `wms_transfer_item`;
CREATE TABLE `wms_transfer_item` (
  `transfer_item_id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `transfer_id` bigint NOT NULL COMMENT '调拨单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `out_location_id` bigint DEFAULT NULL COMMENT '调出库位ID',
  `in_location_id` bigint DEFAULT NULL COMMENT '调入库位ID',
  `batch_no` varchar(64) DEFAULT NULL COMMENT '批次号',
  `expire_date` date DEFAULT NULL COMMENT '效期到期日',
  `quantity` decimal(10,2) NOT NULL COMMENT '调拨数量',
  `unit_cost` decimal(18,2) DEFAULT '0.00' COMMENT '调拨单位成本',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`transfer_item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='库存调拨明细表';

-- ----------------------------
-- Records of wms_transfer_item
-- ----------------------------
INSERT INTO `wms_transfer_item` (`transfer_item_id`, `transfer_id`, `product_id`, `out_location_id`, `in_location_id`, `batch_no`, `expire_date`, `quantity`, `unit_cost`, `remark`) VALUES (1001, 1001, 990011, NULL, NULL, NULL, NULL, 5.00, 0.00, '调拨 iPhone 15 Pro 5台');

SET FOREIGN_KEY_CHECKS = 1;
