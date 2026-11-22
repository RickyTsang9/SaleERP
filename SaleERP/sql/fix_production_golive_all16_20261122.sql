-- ============================================================
-- SaleERP 正式投产上线全量升级迁移脚本 (覆盖第 1~16 项全部数据库变更)
-- 日期: 2026-11-22
-- 特性: 100% 幂等，可重复安全执行
-- ============================================================

SET NAMES utf8mb4;

DROP PROCEDURE IF EXISTS sp_add_col_if_absent;
DELIMITER $$
CREATE PROCEDURE sp_add_col_if_absent(
    IN p_table VARCHAR(64),
    IN p_column VARCHAR(64),
    IN p_ddl TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table
          AND COLUMN_NAME = p_column
    ) THEN
        SET @sql_stmt = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN ', p_ddl);
        PREPARE stmt FROM @sql_stmt;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- 1. 数据备份表扩展（离线压缩包路径与文件大小）并清理历史 0 表空壳备份
CALL sp_add_col_if_absent('biz_data_backup', 'file_path', '`file_path` VARCHAR(255) NULL COMMENT ''磁盘备份压缩包相对路径'' AFTER `record_count`');
CALL sp_add_col_if_absent('biz_data_backup', 'file_size', '`file_size` BIGINT DEFAULT 0 COMMENT ''备份文件字节大小'' AFTER `file_path`');
DELETE FROM biz_data_backup WHERE IFNULL(table_count, 0) = 0 AND IFNULL(record_count, 0) = 0;

-- 2. 库存表乐观锁与锁定/冻结默认值兜底
UPDATE wms_stock SET version = 0 WHERE version IS NULL;
UPDATE wms_stock SET locked_quantity = 0 WHERE locked_quantity IS NULL;
UPDATE wms_stock SET frozen_quantity = 0 WHERE frozen_quantity IS NULL;

-- 3. 销售订单增加库存扣减模式字段，存量已审核老单标记为 legacy_deducted 防重复扣减
CALL sp_add_col_if_absent('wms_sale_order', 'stock_deduct_mode', '`stock_deduct_mode` VARCHAR(24) NOT NULL DEFAULT ''lock_on_audit'' COMMENT ''库存扣减模式(lock_on_audit锁库出库扣减 legacy_deducted历史审单已扣)'' AFTER `payment_status`');
UPDATE wms_sale_order SET stock_deduct_mode = 'legacy_deducted' WHERE status = 'audited' AND (stock_deduct_mode IS NULL OR stock_deduct_mode = 'lock_on_audit');

-- 4. 回款与付款冲红作废字段 + 资金结算账户关联字段
CALL sp_add_col_if_absent('fin_receipt', 'account_id', '`account_id` BIGINT NULL COMMENT ''资金结算账户ID'' AFTER `payment_method`');
CALL sp_add_col_if_absent('fin_receipt', 'status', '`status` VARCHAR(20) NOT NULL DEFAULT ''normal'' COMMENT ''状态(normal正常 voided已冲红)'' AFTER `account_id`');
CALL sp_add_col_if_absent('fin_receipt', 'void_reason', '`void_reason` VARCHAR(255) NULL COMMENT ''冲红作废原因'' AFTER `status`');
CALL sp_add_col_if_absent('fin_receipt', 'void_by', '`void_by` VARCHAR(64) NULL COMMENT ''冲红操作人'' AFTER `void_reason`');
CALL sp_add_col_if_absent('fin_receipt', 'void_time', '`void_time` DATETIME NULL COMMENT ''冲红时间'' AFTER `void_by`');

CALL sp_add_col_if_absent('fin_payment', 'account_id', '`account_id` BIGINT NULL COMMENT ''资金结算账户ID'' AFTER `payment_method`');
CALL sp_add_col_if_absent('fin_payment', 'status', '`status` VARCHAR(20) NOT NULL DEFAULT ''normal'' COMMENT ''状态(normal正常 voided已冲红)'' AFTER `account_id`');
CALL sp_add_col_if_absent('fin_payment', 'void_reason', '`void_reason` VARCHAR(255) NULL COMMENT ''冲红作废原因'' AFTER `status`');
CALL sp_add_col_if_absent('fin_payment', 'void_by', '`void_by` VARCHAR(64) NULL COMMENT ''冲红操作人'' AFTER `void_reason`');
CALL sp_add_col_if_absent('fin_payment', 'void_time', '`void_time` DATETIME NULL COMMENT ''冲红时间'' AFTER `void_by`');

-- 5. 库存调拨两步在途签收与批次效期成本字段
CALL sp_add_col_if_absent('wms_transfer', 'receive_by', '`receive_by` VARCHAR(64) NULL COMMENT ''调入签收人'' AFTER `status`');
CALL sp_add_col_if_absent('wms_transfer', 'receive_time', '`receive_time` DATETIME NULL COMMENT ''调入签收时间'' AFTER `receive_by`');
CALL sp_add_col_if_absent('wms_transfer', 'reject_reason', '`reject_reason` VARCHAR(255) NULL COMMENT ''审核驳回原因'' AFTER `receive_time`');

CALL sp_add_col_if_absent('wms_transfer_item', 'out_location_id', '`out_location_id` BIGINT NULL COMMENT ''调出库位ID'' AFTER `product_id`');
CALL sp_add_col_if_absent('wms_transfer_item', 'in_location_id', '`in_location_id` BIGINT NULL COMMENT ''调入库位ID'' AFTER `out_location_id`');
CALL sp_add_col_if_absent('wms_transfer_item', 'batch_no', '`batch_no` VARCHAR(64) NULL COMMENT ''批次号'' AFTER `in_location_id`');
CALL sp_add_col_if_absent('wms_transfer_item', 'expire_date', '`expire_date` DATE NULL COMMENT ''效期到期日'' AFTER `batch_no`');
CALL sp_add_col_if_absent('wms_transfer_item', 'unit_cost', '`unit_cost` DECIMAL(18,2) DEFAULT 0.00 COMMENT ''调拨单位成本'' AFTER `quantity`');

-- 6. 单据审核驳回原因字段（采购/入库/出库/盘点）与出库快照成本
CALL sp_add_col_if_absent('wms_purchase_order', 'reject_reason', '`reject_reason` VARCHAR(255) NULL COMMENT ''审核驳回原因'' AFTER `status`');
CALL sp_add_col_if_absent('wms_inbound', 'reject_reason', '`reject_reason` VARCHAR(255) NULL COMMENT ''审核驳回原因'' AFTER `audit_time`');
CALL sp_add_col_if_absent('wms_outbound', 'reject_reason', '`reject_reason` VARCHAR(255) NULL COMMENT ''审核驳回原因'' AFTER `audit_time`');
CALL sp_add_col_if_absent('wms_inventory_check', 'reject_reason', '`reject_reason` VARCHAR(255) NULL COMMENT ''审核驳回原因'' AFTER `audit_time`');

-- 7. 销售退货与采购退货来源单号、库位与批次字段
CALL sp_add_col_if_absent('wms_sale_return', 'sale_order_id', '`sale_order_id` BIGINT NULL COMMENT ''关联销售订单ID'' AFTER `customer_id`');
CALL sp_add_col_if_absent('wms_sale_return_item', 'location_id', '`location_id` BIGINT NULL COMMENT ''退入库位ID'' AFTER `product_id`');
CALL sp_add_col_if_absent('wms_sale_return_item', 'batch_no', '`batch_no` VARCHAR(64) NULL COMMENT ''退回批次号'' AFTER `location_id`');

CALL sp_add_col_if_absent('wms_purchase_return', 'purchase_order_id', '`purchase_order_id` BIGINT NULL COMMENT ''关联采购订单ID'' AFTER `supplier_id`');
CALL sp_add_col_if_absent('wms_purchase_return_item', 'location_id', '`location_id` BIGINT NULL COMMENT ''退货出库库位ID'' AFTER `product_id`');
CALL sp_add_col_if_absent('wms_purchase_return_item', 'batch_no', '`batch_no` VARCHAR(64) NULL COMMENT ''退货批次号'' AFTER `location_id`');

DROP PROCEDURE IF EXISTS sp_add_col_if_absent;

-- 8. 真实销项/进项发票管理台账表 fin_invoice
CREATE TABLE IF NOT EXISTS `fin_invoice` (
    `invoice_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '发票主键',
    `invoice_no` VARCHAR(64) NOT NULL COMMENT '发票号码',
    `invoice_code` VARCHAR(64) NULL COMMENT '发票代码',
    `invoice_direction` VARCHAR(20) NOT NULL COMMENT '发票方向(sale销项 purchase进项)',
    `invoice_type` VARCHAR(24) NOT NULL DEFAULT 'electronic' COMMENT '发票类型(special专票 normal普票 electronic数电票)',
    `source_order_id` BIGINT NULL COMMENT '关联销售单/采购单ID',
    `source_order_no` VARCHAR(64) NULL COMMENT '关联业务单号',
    `target_id` BIGINT NOT NULL COMMENT '客户ID或供应商ID',
    `target_name` VARCHAR(128) NULL COMMENT '往来单位名称快照',
    `tax_rate` DECIMAL(5,2) NOT NULL DEFAULT 13.00 COMMENT '税率(%)',
    `total_amount` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '价税合计金额',
    `amount_without_tax` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '不含税金额',
    `tax_amount` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '税额',
    `invoice_date` DATE NOT NULL COMMENT '开票/收票日期',
    `status` VARCHAR(20) NOT NULL DEFAULT 'issued' COMMENT '状态(draft草稿 issued已开具 red_void已红冲)',
    `void_reason` VARCHAR(255) NULL COMMENT '红冲作废原因',
    `remark` VARCHAR(500) NULL COMMENT '备注',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`invoice_id`),
    UNIQUE KEY `uk_fin_invoice_no` (`invoice_no`),
    KEY `idx_fin_invoice_dir_date` (`invoice_direction`, `invoice_date`),
    KEY `idx_fin_invoice_source` (`invoice_direction`, `source_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='财务销项与进项发票台账表';

INSERT INTO `fin_invoice` (`invoice_id`, `invoice_no`, `invoice_code`, `invoice_direction`, `invoice_type`, `source_order_id`, `source_order_no`, `target_id`, `target_name`, `tax_rate`, `total_amount`, `amount_without_tax`, `tax_amount`, `invoice_date`, `status`, `remark`, `create_by`, `create_time`)
VALUES
(1, '2631200000010001', '031002600111', 'sale', 'electronic', 1, 'SO20231026001', 1, '北京华数科技有限公司', 13.00, 79990.00, 70787.61, 9202.39, '2026-11-05', 'issued', '首批iPhone与配件销项数电专票', 'admin', NOW()),
(2, '2631200000010002', '031002600111', 'sale', 'special', 2, 'SO20231026002', 2, '上海天创商贸有限公司', 13.00, 34995.00, 30969.03, 4025.97, '2026-11-08', 'issued', '天创商贸首批办公设备专票', 'admin', NOW()),
(3, '2644200000020001', '044002600211', 'purchase', 'special', 1, 'PO202604160001', 1, '深圳华强数码供应链有限公司', 13.00, 124000.00, 109734.51, 14265.49, '2026-11-02', 'issued', '华强供应链进项抵扣专票', 'admin', NOW()),
(4, '2644200000020002', '044002600211', 'purchase', 'electronic', 2, 'PO202604160002', 2, '广州智汇信息设备有限公司', 9.00, 75000.00, 68807.34, 6192.66, '2026-11-10', 'issued', '智汇设备及运输服务混合进项票', 'admin', NOW())
ON DUPLICATE KEY UPDATE `target_name` = VALUES(`target_name`), `status` = VALUES(`status`);

-- 9. 资金结算账户表 fin_account 与财务月结关账表 fin_period_close
CREATE TABLE IF NOT EXISTS `fin_account` (
    `account_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '账户主键',
    `account_code` VARCHAR(32) NOT NULL COMMENT '账户编码',
    `account_name` VARCHAR(100) NOT NULL COMMENT '账户名称',
    `account_type` VARCHAR(20) NOT NULL DEFAULT 'bank' COMMENT '账户类型(bank银行户 wechat微信商户 alipay支付宝 cash现金)',
    `bank_name` VARCHAR(100) NULL COMMENT '开户行/平台名称',
    `bank_no` VARCHAR(64) NULL COMMENT '银行账号/商户号',
    `opening_balance` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '期初余额',
    `current_balance` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '当前实时余额',
    `is_default` CHAR(1) NOT NULL DEFAULT 'N' COMMENT '是否默认结算户(Y/N)',
    `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态(0正常 1停用)',
    `remark` VARCHAR(255) NULL COMMENT '备注',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`account_id`),
    UNIQUE KEY `uk_fin_account_code` (`account_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资金结算账户表';

INSERT INTO `fin_account` (`account_id`, `account_code`, `account_name`, `account_type`, `bank_name`, `bank_no`, `opening_balance`, `current_balance`, `is_default`, `status`, `remark`, `create_by`, `create_time`)
VALUES
(1, 'ACC-BANK-01', '招商银行基本存款户', 'bank', '招商银行深圳高新园支行', '755900188810901', 500000.00, 539990.00, 'Y', '0', '对公收付款主力基本户', 'admin', NOW()),
(2, 'ACC-WX-01', '企业微信商户收款户', 'wechat', '微信支付商户平台', 'MCH-160889901', 50000.00, 50000.00, 'N', '0', '终端门店与小额扫码收款户', 'admin', NOW()),
(3, 'ACC-CASH-01', '财务日常备用金现金户', 'cash', '总部财务保险柜', '-', 20000.00, 20000.00, 'N', '0', '零星采购与运费报销备用金', 'admin', NOW())
ON DUPLICATE KEY UPDATE `account_name` = VALUES(`account_name`);

UPDATE `fin_receipt` SET `account_id` = 1 WHERE `account_id` IS NULL;
UPDATE `fin_payment` SET `account_id` = 1 WHERE `account_id` IS NULL;

CREATE TABLE IF NOT EXISTS `fin_period_close` (
    `period_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '月结主键',
    `period_month` VARCHAR(7) NOT NULL COMMENT '会计期间(YYYY-MM)',
    `close_status` VARCHAR(20) NOT NULL DEFAULT 'closed' COMMENT '状态(closed已关账 reopened已反结账)',
    `sale_amount_snapshot` DECIMAL(18,2) DEFAULT 0.00 COMMENT '关账时月度销售额快照',
    `purchase_amount_snapshot` DECIMAL(18,2) DEFAULT 0.00 COMMENT '关账时月度采购额快照',
    `closed_by` VARCHAR(64) NULL COMMENT '关账操作人',
    `closed_time` DATETIME NULL COMMENT '关账时间',
    `reopen_reason` VARCHAR(255) NULL COMMENT '反结账原因',
    `remark` VARCHAR(255) NULL COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`period_id`),
    UNIQUE KEY `uk_fin_period_month` (`period_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='财务会计期间月结关账表';

INSERT INTO `fin_period_close` (`period_id`, `period_month`, `close_status`, `sale_amount_snapshot`, `purchase_amount_snapshot`, `closed_by`, `closed_time`, `remark`)
VALUES
(1, '2026-09', 'closed', 168000.00, 96000.00, 'admin', '2026-10-03 18:00:00', '2026年9月账实核对无误，已完成月结锁定')
ON DUPLICATE KEY UPDATE `remark` = VALUES(`remark`);

-- 10. 客户协议价表 md_customer_price
CREATE TABLE IF NOT EXISTS `md_customer_price` (
    `price_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '协议价主键',
    `customer_id` BIGINT NOT NULL COMMENT '客户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `agreed_price` DECIMAL(18,2) NOT NULL COMMENT '客户专属协议单价',
    `status` CHAR(1) NOT NULL DEFAULT '0' COMMENT '状态(0有效 1停用)',
    `remark` VARCHAR(255) NULL COMMENT '备注',
    `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_by` VARCHAR(64) DEFAULT '',
    `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`price_id`),
    UNIQUE KEY `uk_customer_product_price` (`customer_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户商品专属协议价表';

INSERT INTO `md_customer_price` (`price_id`, `customer_id`, `product_id`, `agreed_price`, `status`, `remark`, `create_by`)
VALUES
(1, 1, 1, 7850.00, '0', '战略大客户年度框架协议价', 'admin'),
(2, 2, 3, 8650.00, '0', '上海天创核心机型协议价', 'admin')
ON DUPLICATE KEY UPDATE `agreed_price` = VALUES(`agreed_price`);

-- 11. 系统内控参数：制单与审核分离参数开关（默认 false 便于单人联调，生产上线改为 true）
INSERT INTO `sys_config` (`config_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `remark`)
VALUES (20, '单据审批是否强制制审分离', 'biz.audit.preventSelfApprove', 'false', 'Y', 'admin', NOW(), '开启(true)后禁止制单人审核本人提交的业务单据；开发环境默认false')
ON DUPLICATE KEY UPDATE `config_name` = VALUES(`config_name`), `remark` = VALUES(`remark`);

-- 12. 新增菜单与细粒度权限（发票管理、资金与月结、移动仓配作业、毛利脱敏权限、备份下载权限）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
VALUES
(5310, '发票管理', 3028, 5, 'invoice', 'business/invoice/index', '', '', 1, 0, 'C', '0', '0', 'business:invoice:list', 'documentation', 'admin', NOW(), '销项与进项发票登记与红冲台账'),
(5311, '发票查询', 5310, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'business:invoice:query', '#', 'admin', NOW(), ''),
(5312, '发票新增', 5310, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'business:invoice:add', '#', 'admin', NOW(), ''),
(5313, '发票修改', 5310, 3, '', '', '', '', 1, 0, 'F', '0', '0', 'business:invoice:edit', '#', 'admin', NOW(), ''),
(5314, '发票红冲', 5310, 4, '', '', '', '', 1, 0, 'F', '0', '0', 'business:invoice:void', '#', 'admin', NOW(), ''),
(5315, '发票导出', 5310, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'business:invoice:export', '#', 'admin', NOW(), ''),
(5320, '资金与月结', 3028, 7, 'account', 'business/account/index', '', '', 1, 0, 'C', '0', '0', 'business:account:list', 'money', 'admin', NOW(), '资金结算账户余额与财务月结关账'),
(5321, '账户维护', 5320, 1, '', '', '', '', 1, 0, 'F', '0', '0', 'business:account:edit', '#', 'admin', NOW(), ''),
(5322, '财务月结关账', 5320, 2, '', '', '', '', 1, 0, 'F', '0', '0', 'business:periodClose:manage', '#', 'admin', NOW(), ''),
(5330, '移动仓配作业', 3027, 6, 'mobileWms', 'business/mobileWms/index', '', '', 1, 0, 'C', '0', '0', 'business:stock:list', 'phone', 'admin', NOW(), '手机/PDA现场收货、拣货与扫码盘点工作台'),
(5335, '成本与毛利查看', 3021, 10, '', '', '', '', 1, 0, 'F', '0', '0', 'business:report:profit', '#', 'admin', NOW(), '控制商品成本价与毛利指标可见性'),
(5336, '备份包下载', 3022, 5, '', '', '', '', 1, 0, 'F', '0', '0', 'business:dataBackup:export', '#', 'admin', NOW(), '下载离线压缩备份文件')
ON DUPLICATE KEY UPDATE `menu_name` = VALUES(`menu_name`), `component` = VALUES(`component`), `perms` = VALUES(`perms`), `icon` = VALUES(`icon`);

-- 角色授权：经理(4)与财务(5)授予发票、资金月结与毛利权限；普通/仓管授予移动仓配作业
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(4, 5310), (4, 5311), (4, 5320), (4, 5330), (4, 5335),
(5, 5310), (5, 5311), (5, 5312), (5, 5313), (5, 5314), (5, 5315), (5, 5320), (5, 5321), (5, 5322), (5, 5335), (5, 5336),
(2, 5330);
