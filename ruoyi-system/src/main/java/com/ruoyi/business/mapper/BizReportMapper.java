package com.ruoyi.business.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface BizReportMapper
{
    /**
     * 查询已审核销售总额
     * @return 已审核销售总额
     */
    public BigDecimal selectApprovedSaleAmount();

    /**
     * 查询应收总额
     * @return 应收总额
     */
    public BigDecimal selectReceivableAmount();

    /**
     * 查询已收总额
     * @return 已收总额
     */
    public BigDecimal selectReceivedAmount();

    /**
     * 查询逾期应收笔数
     * @return 逾期应收笔数
     */
    public Long selectOverdueReceivableCount();

    /**
     * 查询库存预警数量
     * @return 库存预警数量
     */
    public Long selectStockWarningCount();

    /**
     * 查询销售趋势
     * @return 销售趋势
     */
    public List<Map<String, Object>> selectSaleTrendList();

    /**
     * 查询大客户排行
     * @return 大客户排行
     */
    public List<Map<String, Object>> selectTopCustomerList();

    /**
     * 查询待审核销售单数量
     * @return 待审核销售单数量
     */
    public Long selectPendingSaleOrderCount();

    /**
     * 查询待审核入库单数量
     * @return 待审核入库单数量
     */
    public Long selectPendingInboundCount();

    /**
     * 查询待审核出库单数量
     * @return 待审核出库单数量
     */
    public Long selectPendingOutboundCount();

    /**
     * 查询最新公告列表
     * @return 最新公告列表
     */
    public List<Map<String, Object>> selectLatestNoticeList();

    /**
     * 查询最新库存预警消息
     * @return 最新库存预警消息
     */
    public List<Map<String, Object>> selectLatestStockWarningMessageList();

    /**
     * 查询最新应收到期消息
     * @return 最新应收到期消息
     */
    public List<Map<String, Object>> selectLatestDueReceivableList();

    /**
     * 查询最新销售审核消息
     * @return 最新销售审核消息
     */
    public List<Map<String, Object>> selectLatestSaleOrderAuditMessageList();

    /**
     * 查询商品销售分类占比
     * @return 分类销售额列表
     */
    public List<Map<String, Object>> selectProductCategorySales();

    /**
     * 多维度销售明细报表
     * @param params 查询参数
     * @return 销售明细列表
     */
    public List<Map<String, Object>> selectSaleReportList(Map<String, Object> params);

    /**
     * 查询对账中心列表
     * @param params 查询参数
     * @return 对账中心列表
     */
    public List<Map<String, Object>> selectReconciliationList(Map<String, Object> params);

    /**
     * 查询发票税务列表
     * @param params 查询参数
     * @return 发票税务列表
     */
    public List<Map<String, Object>> selectInvoiceTaxList(Map<String, Object> params);

    /**
     * 查询资金预测数据
     * @param params 查询参数
     * @return 资金预测数据
     */
    public Map<String, Object> selectCashFlowForecast(Map<String, Object> params);

    /**
     * 查询管理驾驶舱期间汇总
     * @param params 查询参数
     * @return 期间汇总
     */
    public Map<String, Object> selectManagementPeriodSummary(Map<String, Object> params);

    /**
     * 查询管理驾驶舱风险概览
     * @param params 查询参数
     * @return 风险概览
     */
    public Map<String, Object> selectManagementRiskOverview(Map<String, Object> params);

    /**
     * 查询应收账龄分布
     * @return 应收账龄分布
     */
    public List<Map<String, Object>> selectReceivableAgingList();

    /**
     * 查询应付账龄分布
     * @return 应付账龄分布
     */
    public List<Map<String, Object>> selectPayableAgingList();

    /**
     * 查询高风险客户列表
     * @return 高风险客户列表
     */
    public List<Map<String, Object>> selectTopOverdueCustomerList();

    /**
     * 查询重点付款供应商列表
     * @param params 查询参数
     * @return 重点付款供应商列表
     */
    public List<Map<String, Object>> selectTopDueSupplierList(Map<String, Object> params);
}
