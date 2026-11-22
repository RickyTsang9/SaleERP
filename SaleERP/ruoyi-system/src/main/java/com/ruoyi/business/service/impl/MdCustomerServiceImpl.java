package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.MdCustomer;
import com.ruoyi.business.mapper.MdCustomerMapper;
import com.ruoyi.business.service.IMdCustomerService;
import com.ruoyi.common.annotation.DataScope;

@Service
public class MdCustomerServiceImpl implements IMdCustomerService
{
    @Autowired
    private MdCustomerMapper mdCustomerMapper;

    @Autowired(required = false)
    private com.ruoyi.business.service.support.BizComplianceHelper complianceHelper;

    /**
     * 查询客户
     *
     * @param customerId 客户编号
     * @return 客户信息
     */
    @Override
    public MdCustomer selectMdCustomerById(Long customerId)
    {
        return mdCustomerMapper.selectMdCustomerById(customerId);
    }

    /**
     * 查询客户列表
     *
     * @param mdCustomer 客户查询条件
     * @return 客户列表
     */
    @Override
    @DataScope(deptAlias = "su", userAlias = "su", permission = "business:customer:list")
    public List<MdCustomer> selectMdCustomerList(MdCustomer mdCustomer)
    {
        return mdCustomerMapper.selectMdCustomerList(mdCustomer);
    }

    /**
     * 新增客户
     *
     * @param mdCustomer 客户信息
     * @return 结果
     */
    @Override
    public int insertMdCustomer(MdCustomer mdCustomer)
    {
        return mdCustomerMapper.insertMdCustomer(mdCustomer);
    }

    /**
     * 修改客户
     *
     * @param mdCustomer 客户信息
     * @return 结果
     */
    @Override
    public int updateMdCustomer(MdCustomer mdCustomer)
    {
        if (mdCustomer != null && "1".equals(mdCustomer.getStatus()) && mdCustomer.getCustomerId() != null && complianceHelper != null)
        {
            Long unpaidCount = complianceHelper.queryLong(
                "SELECT COUNT(1) FROM fin_receivable WHERE customer_id = ? AND status IN ('unpaid', 'partial')",
                mdCustomer.getCustomerId()
            );
            if (unpaidCount > 0)
            {
                throw new ServiceException("该客户仍有未结清应收账款（" + unpaidCount + " 笔），禁止停用");
            }
            Long openOrderCount = complianceHelper.queryLong(
                "SELECT COUNT(1) FROM wms_sale_order WHERE customer_id = ? AND status IN ('draft', 'submitted', 'manager_approved')",
                mdCustomer.getCustomerId()
            );
            if (openOrderCount > 0)
            {
                throw new ServiceException("该客户仍有在途未审销售订单（" + openOrderCount + " 笔），禁止停用");
            }
        }
        return mdCustomerMapper.updateMdCustomer(mdCustomer);
    }

    /**
     * 删除客户
     *
     * @param customerId 客户编号
     * @return 结果
     */
    @Override
    public int deleteMdCustomerById(Long customerId)
    {
        validateCustomerCanBeDeleted(customerId, false);
        return mdCustomerMapper.deleteMdCustomerById(customerId);
    }

    /**
     * 批量删除客户
     *
     * @param customerIds 客户编号数组
     * @return 结果
     */
    @Override
    public int deleteMdCustomerByIds(Long[] customerIds)
    {
        for (Long customerId : customerIds)
        {
            validateCustomerCanBeDeleted(customerId, true);
        }
        return mdCustomerMapper.deleteMdCustomerByIds(customerIds);
    }

    /**
     * 导入客户数据
     *
     * @param mdCustomerList 客户导入集合
     * @param isUpdateSupport 是否支持更新
     * @param operName 操作人
     * @return 导入结果
     */
    @Override
    public String importMdCustomer(List<MdCustomer> mdCustomerList, Boolean isUpdateSupport, String operName)
    {
        if (StringUtils.isNull(mdCustomerList) || mdCustomerList.size() == 0)
        {
            throw new ServiceException("导入客户数据不能为空！");
        }
        Map<String, MdCustomer> existingCustomerMap = buildExistingCustomerMap(mdCustomerList);
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (MdCustomer customer : mdCustomerList)
        {
            try
            {
                // 导入前批量加载已存在客户，循环内直接命中映射，减少逐条查库。
                MdCustomer existingCustomer = existingCustomerMap.get(customer.getCustomerCode());
                boolean exists = existingCustomer != null;

                if (!exists)
                {
                    customer.setCreateBy(operName);
                    customer.setStatus(StringUtils.isNotEmpty(customer.getStatus()) ? customer.getStatus() : "0");
                    this.insertMdCustomer(customer);
                    existingCustomerMap.put(customer.getCustomerCode(), customer);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、客户编码 " + customer.getCustomerCode() + " 导入成功");
                }
                else if (isUpdateSupport)
                {
                    customer.setCustomerId(existingCustomer.getCustomerId());
                    customer.setUpdateBy(operName);
                    this.updateMdCustomer(customer);
                    existingCustomerMap.put(customer.getCustomerCode(), customer);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、客户编码 " + customer.getCustomerCode() + " 更新成功");
                }
                else
                {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、客户编码 " + customer.getCustomerCode() + " 已存在");
                }
            }
            catch (Exception e)
            {
                failureNum++;
                String msg = "<br/>" + failureNum + "、客户编码 " + customer.getCustomerCode() + " 导入失败：";
                failureMsg.append(msg + e.getMessage());
            }
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        }
        else
        {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }

    /**
     * 批量构建已存在客户映射
     *
     * @param mdCustomerList 客户导入集合
     * @return 客户编码映射
     */
    private Map<String, MdCustomer> buildExistingCustomerMap(List<MdCustomer> mdCustomerList)
    {
        Map<String, MdCustomer> existingCustomerMap = new HashMap<String, MdCustomer>();
        List<String> customerCodeList = new ArrayList<String>();
        for (MdCustomer mdCustomer : mdCustomerList)
        {
            if (mdCustomer != null && StringUtils.isNotEmpty(mdCustomer.getCustomerCode()))
            {
                customerCodeList.add(mdCustomer.getCustomerCode());
            }
        }
        if (customerCodeList.isEmpty())
        {
            return existingCustomerMap;
        }
        List<MdCustomer> existingCustomerList = mdCustomerMapper.selectMdCustomerListByCustomerCodes(customerCodeList);
        if (existingCustomerList == null || existingCustomerList.isEmpty())
        {
            return existingCustomerMap;
        }
        for (MdCustomer existingCustomer : existingCustomerList)
        {
            existingCustomerMap.put(existingCustomer.getCustomerCode(), existingCustomer);
        }
        return existingCustomerMap;
    }

    /**
     * 校验客户是否允许删除
     *
     * @param customerId 客户编号
     * @param batchDelete 是否批量删除
     */
    private void validateCustomerCanBeDeleted(Long customerId, boolean batchDelete)
    {
        if (customerId == null)
        {
            return;
        }
        MdCustomer databaseCustomer = mdCustomerMapper.selectMdCustomerById(customerId);
        if (databaseCustomer == null)
        {
            return;
        }
        Integer referenceCount = mdCustomerMapper.selectCustomerReferenceCount(customerId);
        if (referenceCount != null && referenceCount.intValue() > 0)
        {
            if (batchDelete)
            {
                throw new ServiceException("客户已被业务单据引用，不能删除，客户名称：" + databaseCustomer.getCustomerName());
            }
            throw new ServiceException("客户已被业务单据引用，不能删除");
        }
    }

    /**
     * 查询客户360度经营画像
     *
     * @param customerId 客户编号
     * @return 客户经营画像数据
     */
    @Override
    public Map<String, Object> selectCustomerProfile(Long customerId)
    {
        if (customerId == null)
        {
            throw new ServiceException("客户编号不能为空");
        }
        MdCustomer customer = mdCustomerMapper.selectMdCustomerById(customerId);
        if (customer == null)
        {
            throw new ServiceException("客户不存在或已被删除");
        }
        Map<String, Object> orderSummary = mdCustomerMapper.selectCustomerOrderSummary(customerId);
        Map<String, Object> receivableSummary = mdCustomerMapper.selectCustomerReceivableSummary(customerId);
        List<Map<String, Object>> recentOrderList = mdCustomerMapper.selectCustomerRecentOrders(customerId);
        List<Map<String, Object>> recentFollowList = mdCustomerMapper.selectCustomerRecentFollows(customerId);
        List<Map<String, Object>> topProductList = mdCustomerMapper.selectCustomerTopProducts(customerId);

        long totalOrderCount = toLong(orderSummary == null ? null : orderSummary.get("totalOrderCount"));
        long auditedOrderCount = toLong(orderSummary == null ? null : orderSummary.get("auditedOrderCount"));
        BigDecimal totalSaleAmount = toBigDecimal(orderSummary == null ? null : orderSummary.get("totalSaleAmount"));
        BigDecimal totalReceivableAmount = toBigDecimal(receivableSummary == null ? null : receivableSummary.get("totalReceivableAmount"));
        BigDecimal totalReceivedAmount = toBigDecimal(receivableSummary == null ? null : receivableSummary.get("totalReceivedAmount"));
        BigDecimal remainReceivableAmount = toBigDecimal(receivableSummary == null ? null : receivableSummary.get("remainReceivableAmount"));
        BigDecimal overdueAmount = toBigDecimal(receivableSummary == null ? null : receivableSummary.get("overdueAmount"));
        long overdueCount = toLong(receivableSummary == null ? null : receivableSummary.get("overdueCount"));
        BigDecimal creditLimit = customer.getCreditLimit() == null ? BigDecimal.ZERO : customer.getCreditLimit();

        BigDecimal collectionRate = totalReceivableAmount.compareTo(BigDecimal.ZERO) > 0
            ? totalReceivedAmount.multiply(new BigDecimal("100")).divide(totalReceivableAmount, 2, RoundingMode.HALF_UP)
            : new BigDecimal("100.00");
        BigDecimal creditUsageRate = creditLimit.compareTo(BigDecimal.ZERO) > 0
            ? remainReceivableAmount.multiply(new BigDecimal("100")).divide(creditLimit, 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        String riskLevel = "正常";
        String riskReason = "回款及信用占用处于健康区间";
        if (overdueAmount.compareTo(BigDecimal.ZERO) > 0)
        {
            riskLevel = "高风险";
            riskReason = "存在 " + overdueCount + " 笔逾期应收，逾期金额 ¥" + overdueAmount.setScale(2, RoundingMode.HALF_UP);
        }
        else if (creditLimit.compareTo(BigDecimal.ZERO) > 0 && remainReceivableAmount.compareTo(creditLimit) > 0)
        {
            riskLevel = "高风险";
            riskReason = "当前未收应收已超过信用额度";
        }
        else if (remainReceivableAmount.compareTo(BigDecimal.ZERO) > 0
            && ((creditLimit.compareTo(BigDecimal.ZERO) > 0 && creditUsageRate.compareTo(new BigDecimal("80")) >= 0)
                || collectionRate.compareTo(new BigDecimal("60")) < 0))
        {
            riskLevel = "中风险";
            riskReason = "信用占用偏高或回款率低于60%，建议加强跟进催收";
        }

        Map<String, Object> summary = new HashMap<String, Object>();
        summary.put("totalOrderCount", totalOrderCount);
        summary.put("auditedOrderCount", auditedOrderCount);
        summary.put("totalSaleAmount", totalSaleAmount);
        summary.put("totalReceivableAmount", totalReceivableAmount);
        summary.put("totalReceivedAmount", totalReceivedAmount);
        summary.put("remainReceivableAmount", remainReceivableAmount);
        summary.put("overdueAmount", overdueAmount);
        summary.put("overdueCount", overdueCount);
        summary.put("collectionRate", collectionRate);
        summary.put("creditLimit", creditLimit);
        summary.put("creditUsageRate", creditUsageRate);
        summary.put("riskLevel", riskLevel);
        summary.put("riskReason", riskReason);
        summary.put("lastOrderTime", orderSummary == null ? null : orderSummary.get("lastOrderTime"));
        summary.put("lastFollowTime", (recentFollowList != null && !recentFollowList.isEmpty()) ? recentFollowList.get(0).get("followTime") : null);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("customer", customer);
        result.put("summary", summary);
        result.put("recentOrderList", recentOrderList == null ? new ArrayList<Map<String, Object>>() : recentOrderList);
        result.put("recentFollowList", recentFollowList == null ? new ArrayList<Map<String, Object>>() : recentFollowList);
        result.put("topProductList", topProductList == null ? new ArrayList<Map<String, Object>>() : topProductList);
        return result;
    }

    private BigDecimal toBigDecimal(Object value)
    {
        if (value == null)
        {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal)
        {
            return (BigDecimal) value;
        }
        return new BigDecimal(String.valueOf(value));
    }

    private long toLong(Object value)
    {
        if (value == null)
        {
            return 0L;
        }
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }
}
