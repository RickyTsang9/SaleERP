-- =========================================================
-- SaleERP 第三批 (P2-07) 当月经营闭环演示数据增量脚本 (2026-11)
-- 幂等执行：补充 2026-11 当月购销、出库、应收回款、应付付款、进销项发票与资金流水
-- 使经营看板与首页展示真实非零当月营收、采购、毛利、回款率与资金流水闭环
-- =========================================================

SET NAMES utf8mb4;

-- 1. 当月已审核采购订单 PO20261115001 (8台 iPhone 15 Pro @ 6800 = 54400.00)
INSERT IGNORE INTO `wms_purchase_order` (
    `purchase_order_id`, `order_no`, `supplier_id`, `purchase_date`,
    `total_amount`, `invoiced_amount`, `invoice_status`, `status`,
    `remark`, `create_by`, `create_time`, `update_by`, `update_time`
) VALUES (
    104, 'PO20261115001', 1, '2026-11-15 09:30:00',
    54400.00, 54400.00, 'invoiced', '3',
    '2026年11月主力机型补货采购', 'admin', '2026-11-15 09:30:00', 'admin', '2026-11-16 11:00:00'
);

INSERT IGNORE INTO `wms_purchase_order_item` (
    `purchase_order_item_id`, `purchase_order_id`, `product_id`, `quantity`, `price`, `amount`, `remark`
) VALUES (
    1003, 104, 990011, 8.00, 6800.00, 54400.00, '2026年11月采购补货'
);

INSERT IGNORE INTO `fin_payable` (
    `payable_id`, `purchase_order_id`, `supplier_id`, `amount_due`, `amount_paid`, `remain_amount`,
    `status`, `due_date`, `overdue_days`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`,
    `amount`, `paid_amount`
) VALUES (
    1003, 104, 1, 54400.00, 30000.00, 24400.00,
    '1', '2026-12-15', 0, 'PO20261115001应付账款', 'admin', '2026-11-15 10:00:00', 'admin', '2026-11-21 14:00:00',
    54400.00, 30000.00
);

INSERT IGNORE INTO `fin_payment` (
    `payment_id`, `payable_id`, `purchase_order_id`, `supplier_id`, `amount`,
    `payment_time`, `payment_method`, `account_id`, `status`, `remark`,
    `create_by`, `create_time`, `update_by`, `update_time`, `payment_date`
) VALUES (
    1002, 1003, 104, 1, 30000.00,
    '2026-11-21 14:00:00', 'bank_transfer', 1, 'normal', 'PO20261115001首期货款',
    'admin', '2026-11-21 14:00:00', 'admin', '2026-11-21 14:00:00', '2026-11-21 14:00:00'
);

INSERT IGNORE INTO `fin_invoice` (
    `invoice_id`, `invoice_no`, `invoice_code`, `invoice_direction`, `invoice_type`,
    `source_order_id`, `source_order_no`, `target_id`, `target_name`,
    `tax_rate`, `total_amount`, `amount_without_tax`, `tax_amount`, `invoice_date`,
    `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`
) VALUES (
    5, '26442000000111500001', '', 'purchase', 'special',
    104, 'PO20261115001', 1, '深圳华强数码供应链有限公司',
    13.00, 54400.00, 48141.59, 6258.41, '2026-11-16',
    'issued', '2026年11月采购进项专票', 'admin', '2026-11-16 14:00:00', 'admin', '2026-11-16 14:00:00'
);

-- 2. 当月已审核销售订单 SO20261118001 (10台 iPhone 15 Pro @ 7999 = 79990.00)
INSERT IGNORE INTO `wms_sale_order` (
    `sale_order_id`, `order_no`, `customer_id`, `warehouse_id`, `total_qty`, `total_amount`,
    `status`, `payment_status`, `invoiced_amount`, `invoice_status`, `stock_deduct_mode`,
    `manager_audit_by`, `manager_audit_time`, `manager_audit_comment`,
    `audit_by`, `audit_time`, `finance_audit_comment`,
    `create_by`, `create_time`, `update_by`, `update_time`, `remark`
) VALUES (
    10, 'SO20261118001', 990018, 990001, 10.00, 79990.00,
    'audited', 'partial', 79990.00, 'invoiced', 'lock_on_audit',
    'admin', '2026-11-18 11:00:00', '老客户授信正常，同意发货',
    'admin', '2026-11-18 14:30:00', '财务审核通过',
    'admin', '2026-11-18 09:30:00', 'admin', '2026-11-20 15:00:00', '2026年11月重点客户集采订单'
);

INSERT IGNORE INTO `wms_sale_order_item` (
    `sale_order_item_id`, `sale_order_id`, `product_id`, `location_id`, `batch_no`,
    `quantity`, `price`, `amount`
) VALUES (
    10, 10, 990011, 990001, 'B20261115',
    10.00, 7999.00, 79990.00
);

-- 3. 对应已审并签收销售出库单 CK20261119001（带真实成本快照 6800.00 => 当月毛利 11990.00）
INSERT IGNORE INTO `wms_outbound` (
    `outbound_id`, `outbound_no`, `outbound_type`, `customer_id`, `sale_order_id`, `sale_order_no`,
    `warehouse_id`, `total_qty`, `total_amount`, `status`, `audit_by`, `audit_time`,
    `source_order_no`, `carrier`, `waybill_no`, `freight_cost`, `delivery_status`,
    `create_by`, `create_time`, `update_by`, `update_time`, `remark`
) VALUES (
    990009, 'CK20261119001', 'sale', 990018, 10, 'SO20261118001',
    990001, 10.00, 79990.00, 'audited', 'admin', '2026-11-19 10:15:00',
    'SO20261118001', '顺丰速运', 'SF202611190088', 65.00, 'delivered',
    'admin', '2026-11-19 09:00:00', 'admin', '2026-11-20 09:30:00', '2026年11月顺丰签收出库'
);

INSERT IGNORE INTO `wms_outbound_item` (
    `outbound_item_id`, `outbound_id`, `sale_order_item_id`, `product_id`, `location_id`,
    `batch_no`, `quantity`, `price`, `cost_price_snapshot`, `amount`
) VALUES (
    990008, 990009, 10, 990011, 990001,
    'B20261115', 10.00, 7999.00, 6800.00, 79990.00
);

-- 4. 对应应收台账、当月回款与销项数电票
INSERT IGNORE INTO `fin_receivable` (
    `receivable_id`, `sale_order_id`, `customer_id`, `amount_due`, `amount_paid`, `status`,
    `due_date`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`, `amount`, `received_amount`
) VALUES (
    4, 10, 990018, 79990.00, 50000.00, 'partial',
    '2026-12-18', 'admin', '2026-11-18 14:30:00', 'admin', '2026-11-20 15:00:00', 'SO20261118001应收台账', 79990.00, 50000.00
);

INSERT IGNORE INTO `fin_receipt` (
    `receipt_id`, `receivable_id`, `sale_order_id`, `customer_id`, `amount`,
    `payment_time`, `payment_method`, `account_id`, `status`, `receipt_date`, `remark`,
    `create_by`, `create_time`, `update_by`, `update_time`
) VALUES (
    3, 4, 10, 990018, 50000.00,
    '2026-11-20 15:00:00', 'bank_transfer', 1, 'normal', '2026-11-20 15:00:00', 'SO20261118001首期回款',
    'admin', '2026-11-20 15:00:00', 'admin', '2026-11-20 15:00:00'
);

INSERT IGNORE INTO `fin_invoice` (
    `invoice_id`, `invoice_no`, `invoice_code`, `invoice_direction`, `invoice_type`,
    `source_order_id`, `source_order_no`, `target_id`, `target_name`,
    `tax_rate`, `total_amount`, `amount_without_tax`, `tax_amount`, `invoice_date`,
    `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`
) VALUES (
    6, '26112000000111900002', '', 'sale', 'electronic',
    10, 'SO20261118001', 990018, '北京华数科技有限公司',
    13.00, 79990.00, 70787.61, 9202.39, '2026-11-19',
    'issued', '2026年11月销项全电数电票', 'admin', '2026-11-19 16:00:00', 'admin', '2026-11-19 16:00:00'
);

-- 5. 同步资金收支流水与账户余额（539990 + 50000 - 30000 = 559990）
INSERT IGNORE INTO `fin_account_journal` (
    `journal_id`, `journal_no`, `account_id`, `account_name`, `direction`, `biz_type`,
    `biz_id`, `biz_no`, `target_name`, `amount`, `before_balance`, `after_balance`,
    `occur_time`, `remark`, `create_by`, `create_time`
) VALUES
(
    4, 'ZJ-RC-20261120-001', 1, '招商银行基本存款户', 'in', 'receipt',
    3, 'SO20261118001', '北京华数科技有限公司', 50000.00, 539990.00, 589990.00,
    '2026-11-20 15:00:00', 'SO20261118001首期回款', 'admin', '2026-11-20 15:00:00'
),
(
    5, 'ZJ-PM-20261121-001', 1, '招商银行基本存款户', 'out', 'payment',
    1002, 'PO20261115001', '深圳华强数码供应链有限公司', 30000.00, 589990.00, 559990.00,
    '2026-11-21 14:00:00', 'PO20261115001首期货款', 'admin', '2026-11-21 14:00:00'
);

UPDATE `fin_account`
SET `current_balance` = 559990.00
WHERE `account_id` = 1
  AND EXISTS (SELECT 1 FROM `fin_account_journal` WHERE `journal_no` = 'ZJ-PM-20261121-001');
