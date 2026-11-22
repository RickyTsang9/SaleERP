# 更新日志

## 2026-11-22

### 修复与增强

- 新增 `sql/fix_baseline_and_menus_20261122.sql` 与全量初始化基线 `sql/sale_erp_full_baseline_20261122.sql`，补齐供应商、采购单明细、销售状态历史、应付付款、客户跟进、退货、盘点、调拨闭环演示数据，修复全部主外键断链及菜单入口缺失。
- 扩展应收台账（`FinReceivable`）与回款流水（`FinReceipt`）关联返回业务销售单号 `saleOrderNo` 并支持按单号检索。
- 增强库存流水（`WmsStockLogMapper`）单据号解析，关联入库、出库、采购退货、盘点、调拨真实业务单号。
- 完善销售订单链路追踪（`WmsSaleOrderServiceImpl`），新增销售退货链路列表并统一审核节点中文状态；出库列表按 `saleOrderId` 查询时兼容历史 `source_order_no`。
- 统一经营看板高风险客户、往来对账与逾期应收待办客户名称回退关联口径，消除待办中心库存预警裸露商品 ID，更新 Swagger 接口文档简介。
- 新增 `sql/fix_phase3_cleanup_20261122.sql` 并刷新 `sql/sale_erp_full_baseline_20261122.sql`：清理系统配置注释乱码、修复全部 6 处历史乱码/孤立菜单并补挂库存预警阈值权限（`business:stock:edit`）、移除 3 条若依遗留演示定时任务、测试账号 `ry` 更名为 `tester`、补齐 2026 年度预算/经营简报/经营快照/决议事项完整基线演示数据。
- 外置 `application.yml` 与 `application-druid.yml` 中的数据库、Redis、JWT 密钥、Druid 控制台、上传路径及防盗链白名单配置为 `${ENV:default}` 环境变量，并在 Swagger 中新增“核心业务接口”与“系统管理接口”分组。
- 为 16 张核心业务表 Domain 与 MyBatis Mapper 补齐客户/供应商/仓库/商品/库位关联名称字段，从根源消除前端列表 N+1 回补请求；顺带修复 `WmsSaleOrderMapper.xml` 缺失 `sys_user su` 关联导致的非超管 `@DataScope` 潜在 SQL 报错，以及 MySQL 8 `ONLY_FULL_GROUP_BY` 下对账/逾期客户查询与待办跳转路径问题。
- 落地二期/三期高价值经营能力接口：客户 360° 经营画像（`GET /business/customer/{id}/profile`）、商品经营画像（`GET /business/product/{id}/profile`）、库存预警阈值独立维护（`PUT /business/stock/warningThreshold`）及标准往来对账单明细接口（`GET /business/report/reconciliationStatement`）。
- 引入 `spring-boot-starter-test` 与 `maven-surefire-plugin:2.22.2`，补齐 4 组共 10 项核心 Service JUnit 5 + Mockito 单元测试，`mvn clean install` 全量通过。
- 完成三至五期路线图全量收口：新增 `sql/fix_phase45_roadmap_20261122.sql` 并刷新全量基线 `sql/sale_erp_full_baseline_20261122.sql`，为 `wms_stock` / `wms_inbound_item` 增加批次效期字段 `expire_date` 并打通入库审核回写与 30 天临期预警筛选；新增条码扫码查询接口（`GET /business/product/scan/{scanCode}`）、商品经营多维排行分析接口（`GET /business/report/productRanking`）、应收/应付账龄区间过滤参数（`params.agingBucket`）；待办中心扩展纳入客户逾期跟进（`customer_follow_due`）与库存临期预警（`stock_expiring`）；新增 Quartz 定时任务 `BizOperationReportTask`（自动归档日/周/月经营快照，支持按日幂等防重）；扩展单元测试至 5 组共 14 项（14/14 PASS）。
- 完成全模块第三批（P2-01 ~ P2-08）管理闭环与容灾体验全面打磨（`sql/fix_closure_p2_20261122.sql` 并刷新 72 表全量基线 `sql/sale_erp_full_baseline_20261122.sql`）：
  - **统一待办中心全覆盖与防挤占**：`BizReportMapper.xml` 新增采退待审、销退待审、盘点待审、调拨待审、在途调拨待签收、红字应收/应付待退款、已审订单待开票 7 类待办（共覆盖 19 类业务待办），库存/效期预警子查询独立限流防挤占审批待办，并补齐全角色权限映射。
  - **购销全链路追踪补齐与一体化建单**：销售订单链路新增 `invoiceList`（销项发票节点），采购订单链路新增 `purchaseReturnList`（采购退货）与 `invoiceList`（进项发票）；`FinInvoiceMapper.xml` 补齐 `sourceOrderId` 精确筛选；销售订单、销售退货、采购退货新增表头+明细事务一体化建单支持（`items`）。
  - **数据备份容灾升级与销售报表导出**：`BizDataBackupController` / `BizDataBackupServiceImpl` 新增离线包上传恢复接口（`POST /business/dataBackup/uploadRestore`，支持 `.json.gz`/`.json` 自动识别），恢复前清空全部受管业务表防旧包残留脏数据，大表分页流式备份按主键 `ORDER BY` 确定性导出，修复 v2 磁盘包缺失回退解析误报；`BizReportController` 新增销售明细报表成本/毛利快照列（含权限脱敏）与 Excel 导出接口（`POST /business/report/saleExport`）；补充 `2026-11` 当月完整购销/出库/收付款/发票/资金流水闭环演示数据。
- 完成全模块第一批（P0-01 ~ P0-10）与第二批（P1-01 ~ P1-10）共 20 项业务闭环深度修复（`sql/fix_closure_p0p1_20261122.sql` 并刷新 72 表全量基线 `sql/sale_erp_full_baseline_20261122.sql`）：
  - **财务资金与红字退赔闭环**：新建 `fin_account_journal` 资金收支流水表与 `FinAccountJournal` 领域模型，以行锁（`FOR UPDATE`）重构 `changeAccountBalanceWithJournal` 消除吞错/超支并发漏洞，新增账户互转（`POST /business/account/transfer`）、日常收支（`POST /business/account/otherFund`）与流水查询（`GET /business/account/journal/list`），停用账户校验余额归零与非默认约束；`FinReceipt` / `FinPayment` 支持红字负应收/负应付真实退赔（含作废冲回）及多单合并核销/付款（`POST /business/receipt/batch`、`POST /business/payment/batch`）；修复退货冲减未足额即误标 `paid` 及 `selectByOrderId` 多记录异常，收紧月结关账未审单据检查至全部 8 类业务单据。
  - **进销存单据全链路闭环**：销售订单支持部分发货短装结案（`POST /business/saleOrder/forceClose/{id}` 释放锁库并按实发重算应收/红字退款），补齐 `credit`/`month` 双模式提交与财务终审信用额度校验，禁止含明细切仓及表头篡改总额；采购订单新增已审反审核（`POST /business/purchaseOrder/reverseAudit/{id}`）与部分到货短装结案（`POST /business/purchaseOrder/forceClose/{id}`），修复采购入库移动加权平均成本公式及数量单价负数漏洞；销售出库持久化 `cost_price_snapshot` 历史成本快照并新增配送物流流转接口（`POST /business/outbound/delivery/{id}`）；销售/采购退货补齐按原单过滤、超额退货拦截、审批驳回（`POST /reject/{id}`）及原批次效期保留；库存调拨新增在途撤回（`POST /business/transfer/recall/{id}`）并禁止超额调出；库存盘点严格按当前库存 `FOR UPDATE` 重算盈亏、拦截盘亏致负库存与超冻结数，并新增仓库盲盘初始化接口（`POST /business/inventoryCheck/initWarehouseBlind`）；库存台账新增冻结/解冻接口（`POST /business/stock/freeze`、`unfreeze`）及可用量扣减 `frozen_quantity`。
  - **税务发票、主数据保护、报表净额与权限闭环**：`FinInvoice` 新增 `GET /business/invoice/sourceOptions` 消除纯财务角色跨模块 403，双日期月结校验、退货额扣减防超开发票，并实时回写 `invoiced_amount`/`invoice_status` 至销售/采购主单；主数据（客户/供应商/仓库/库位/商品）补齐在用单据引用删除保护与唯一编码并发防重；经营看板/趋势/客户/品类/销售明细报表全面扣除已审退货并切换至 `cost_price_snapshot` 精确毛利核算，修复对账红字被 `GREATEST(...,0)` 截断问题与非授权角色驾驶舱毛利脱敏；修复待办中心销售审批权限标识与 `sys_menu` 补挂 `5340 销售明细报表`、`5341 库位导入`、`5342 库存冻结解冻`。
- 完成正式投产上线全模块 16 项工程升级（`sql/fix_production_golive_all16_20261122.sql` 并刷新 71 表全量基线 `sql/sale_erp_full_baseline_20261122.sql`）：
  - **数据备份与灾难恢复修复**：修复 MySQL 8 `UPPER(table_type) = 'BASE TABLE'` 空表 Bug，纳管全部 `md_*`/`crm_*`/`wms_*`/`fin_*`/`biz_*` 表，支持流式 GZIP `.json.gz` 磁盘落盘与离线下载（`GET /business/dataBackup/{id}/download`），恢复前自动生成 `pre_restore_` 保护快照并临时解除外键约束。
  - **库存乐观锁防超卖与锁库/实扣两段式闭环**：`WmsStock` 新增 5 组基于 `version` 乐观锁 + SQL 余额下限的原子库存操作与 3 次冲突重试；销售订单财务终审改为占用 `locked_quantity`（兼容历史 `legacy_deducted`），销售出库审核正式执行核销并扣减实物库存；新增 FEFO 近效期批次推荐（`GET /business/stock/recommendBatch`）、过期批次强拦截与期初库存导入（`POST /business/stock/importData`）。
  - **财务冲红、多账户结算、发票台账与月结关账**：回款与付款新增冲红作废接口（`PUT /business/receipt/{id}/void`、`PUT /business/payment/{id}/void`）自动回滚应收应付及结算户余额；新建 `fin_invoice` 真实销进项发票台账与 `FinInvoiceController`（含防超额开票校验并接管经营看板税务报表数据源）；新建 `fin_account` 多资金结算账户与 `fin_period_close` 会计期间月结关账/反结账体系（`BizComplianceHelper` 统一拦截已关账月份单据变更与制审分离校验）。
  - **采购/仓储/退货/主数据与权限内控闭环**：采购入库审核自动按移动加权平均法重算更新 `md_product.cost_price`；采购订单补齐草稿与提交态，采购/入库/出库/盘点/调拨统一新增审核驳回原因流转；库存盘点新增一键载入全仓在库批次快照接口（`POST /business/inventoryCheck/{id}/loadStockSnapshot`）；库存调拨升级为“调出扣减(`in_transit`) -> 调入签收(`1`)”两阶段在途闭环并修正跨仓目标库位映射；销售/采购退货支持原批次库位及已结清溢收/溢付红字台账生成；补齐 10 张业务表 `@DataScope` 数据隔离、非授权角色成本毛利字段脱敏、待办中心按岗位权限过滤，以及供应商/库位 Excel 导入；JUnit 5 单元测试扩展至 **18/18 全量通过**。

## 2026-04-16

### 本次发布概览

- 完成 SaleERP 管理系统二期能力建设，补齐管理层驾驶舱、经营分析与关键业务闭环能力。
- 完成采购、调拨、应收应付、付款回款、客户跟进等核心 ERP 场景的功能落地与联调修复。
- 持续优化导入、库存、出库、报表、恢复备份等高频链路，降低不必要的全表扫描与逐条写库开销。

### 新增功能

- 新增管理层预算计划模块，支持年度预算、版本管理、提交审批与审批结果同步。
- 新增管理简报、经营快照、行动项跟踪能力，方便公司管理层进行经营决策与执行追踪。
- 新增应付账款、付款管理、采购订单、调拨单、供应商管理、客户跟进等业务模块。
- 新增多类控制器、领域对象、service、mapper 与对应 xml 映射，形成完整后端业务链路。

### 优化与修复

- 修复回款字段兼容、可选业务表不存在时的容错处理，避免页面首次进入直接报错。
- 优化销售订单、客户、商品导入逻辑，改为批量预加载已存在数据，减少逐条查库。
- 优化出库、退货、库存预警、应收冲减、应付冲减等链路中的列表查询，替换为计数或精确查询。
- 优化经营分析 SQL，减少标量子查询与非必要的日期函数影响，提升管理报表查询效率。
- 优化数据恢复流程，恢复时按批次写入数据库，降低大批量恢复时的执行耗时。
- 补充多项数据库索引脚本，覆盖库存、订单、客户、商品、财务与报表相关高频字段。
- 补齐本轮涉及方法注释，统一后端 service、mapper 与系统现有代码风格。

### 验证结果

- 后端执行 `mvn -q -DskipTests compile` 编译通过。
- 关键变更均已完成 mapper 映射校验与 service 编译回归。
