package com.ruoyi.business.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.common.core.domain.AjaxResult;

public interface IBizReportService
{
    /**
     * 查询经营看板首页数据
     * @return 经营看板首页数据
     */
    public AjaxResult getDashboardData();

    /**
     * 查询销售趋势数据
     * @return 销售趋势数据
     */
    public Map<String, Object> getSaleTrendData();

    /**
     * 查询大客户排行数据
     * @return 大客户排行数据
     */
    public Map<String, Object> getTopCustomerData();

    /**
     * 获取商品销售分类占比数据
     * @return 商品销售分类占比数据
     */
    public List<Map<String, Object>> getProductCategorySalesData();

    /**
     * 多维度销售明细报表
     * @param params 查询参数
     * @return 销售明细列表
     */
    public List<Map<String, Object>> selectSaleReportList(Map<String, Object> params);

    /**
     * 对账中心汇总报表
     * @param params 查询参数
     * @return 对账列表
     */
    public List<Map<String, Object>> selectReconciliationList(Map<String, Object> params);

    /**
     * 发票税务汇总报表
     * @param params 查询参数
     * @return 发票税务列表
     */
    public List<Map<String, Object>> selectInvoiceTaxList(Map<String, Object> params);

    /**
     * 资金预测
     * @param forecastDays 预测天数
     * @return 资金预测
     */
    public Map<String, Object> getCashFlowForecast(Integer forecastDays);

    /**
     * 查询统一待办中心数据
     * @return 统一待办中心数据
     */
    public Map<String, Object> getTodoCenterData();

    /**
     * 查询标准往来对账单明细
     * @param params 查询参数（reconcileType, targetId, startDate, endDate）
     * @return 标准往来对账单数据
     */
    public Map<String, Object> getReconciliationStatement(Map<String, Object> params);

    /**
     * 查询商品经营排行分析
     * @param params 查询参数（sortBy, categoryName, keyword）
     * @return 商品经营排行列表
     */
    public List<Map<String, Object>> selectProductRankingList(Map<String, Object> params);
}
