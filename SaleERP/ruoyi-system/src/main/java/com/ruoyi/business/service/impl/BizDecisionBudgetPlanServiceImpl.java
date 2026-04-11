package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.BizDecisionBudgetPlan;
import com.ruoyi.business.mapper.BizDecisionBudgetPlanMapper;
import com.ruoyi.business.service.IBizDecisionBudgetPlanService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysConfig;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 管理层年度预算计划Service实现
 * 
 * @author ruoyi
 * @date 2026-04-11
 */
@Service
public class BizDecisionBudgetPlanServiceImpl implements IBizDecisionBudgetPlanService
{
    /** 管理驾驶舱二期初始化脚本 */
    private static final String MANAGEMENT_PHASE_TWO_SQL_FILE_NAME = "add_management_phase2_20260411.sql";

    /** 年度预算表名称 */
    private static final String BIZ_DECISION_BUDGET_PLAN_TABLE_NAME = "biz_decision_budget_plan";

    /** 草稿状态 */
    private static final String PLAN_STATUS_DRAFT = "draft";

    /** 待审批状态 */
    private static final String PLAN_STATUS_SUBMITTED = "submitted";

    /** 审批通过状态 */
    private static final String PLAN_STATUS_APPROVED = "approved";

    /** 驳回状态 */
    private static final String PLAN_STATUS_REJECTED = "rejected";

    /** 月份数量 */
    private static final int MONTH_COUNT = 12;

    /** 预算键列表 */
    private static final String[] DECISION_BUDGET_KEY_ARRAY = {
        "saleBudgetAmount",
        "grossProfitBudgetAmount",
        "collectionBudgetAmount",
        "purchaseBudgetAmount",
        "netCashBudgetAmount"
    };

    /** 配置键列表 */
    private static final String[] DECISION_BUDGET_CONFIG_KEY_ARRAY = {
        "biz.report.decision.saleBudgetAmount",
        "biz.report.decision.grossProfitBudgetAmount",
        "biz.report.decision.collectionBudgetAmount",
        "biz.report.decision.purchaseBudgetAmount",
        "biz.report.decision.netCashBudgetAmount"
    };

    /** 配置名称列表 */
    private static final String[] DECISION_BUDGET_CONFIG_NAME_ARRAY = {
        "管理驾驶舱销售预算",
        "管理驾驶舱毛利预算",
        "管理驾驶舱回款预算",
        "管理驾驶舱采购预算",
        "管理驾驶舱净现金流预算"
    };

    /** 配置备注列表 */
    private static final String[] DECISION_BUDGET_CONFIG_REMARK_ARRAY = {
        "审批通过后按当前月份同步销售预算",
        "审批通过后按当前月份同步毛利预算",
        "审批通过后按当前月份同步回款预算",
        "审批通过后按当前月份同步采购预算",
        "审批通过后按当前月份同步净现金流预算"
    };

    @Autowired
    private BizDecisionBudgetPlanMapper bizDecisionBudgetPlanMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ISysConfigService sysConfigService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查询指定年度预算计划
     * 
     * @param budgetYear 预算年度
     * @return 年度预算计划
     */
    @Override
    public BizDecisionBudgetPlan selectCurrentBizDecisionBudgetPlan(Integer budgetYear)
    {
        int validBudgetYear = budgetYear == null ? LocalDate.now().getYear() : budgetYear.intValue();
        // 二期预算表未初始化时直接回退到默认空计划，避免经营看板整页报错。
        if (!hasDecisionBudgetPlanTable())
        {
            return buildEmptyBizDecisionBudgetPlan(validBudgetYear);
        }
        BizDecisionBudgetPlan bizDecisionBudgetPlan = bizDecisionBudgetPlanMapper.selectBizDecisionBudgetPlanByBudgetYear(validBudgetYear);
        if (bizDecisionBudgetPlan == null)
        {
            return buildEmptyBizDecisionBudgetPlan(validBudgetYear);
        }
        return convertBizDecisionBudgetPlanForDisplay(bizDecisionBudgetPlan);
    }

    /**
     * 新增年度预算计划
     * 
     * @param bizDecisionBudgetPlan 年度预算计划
     * @param operatorName 操作人
     * @return 结果
     */
    @Override
    public int insertBizDecisionBudgetPlan(BizDecisionBudgetPlan bizDecisionBudgetPlan, String operatorName)
    {
        validateDecisionBudgetPlanTableReady();
        validateBudgetYearUnique(bizDecisionBudgetPlan.getBudgetYear(), null);
        BizDecisionBudgetPlan normalizedDecisionBudgetPlan = normalizeBizDecisionBudgetPlanForSave(bizDecisionBudgetPlan);
        normalizedDecisionBudgetPlan.setPlanStatus(PLAN_STATUS_DRAFT);
        normalizedDecisionBudgetPlan.setCreateBy(operatorName);
        normalizedDecisionBudgetPlan.setUpdateBy(operatorName);
        return bizDecisionBudgetPlanMapper.insertBizDecisionBudgetPlan(normalizedDecisionBudgetPlan);
    }

    /**
     * 修改年度预算计划
     * 
     * @param bizDecisionBudgetPlan 年度预算计划
     * @param operatorName 操作人
     * @return 结果
     */
    @Override
    public int updateBizDecisionBudgetPlan(BizDecisionBudgetPlan bizDecisionBudgetPlan, String operatorName)
    {
        validateDecisionBudgetPlanTableReady();
        if (bizDecisionBudgetPlan.getPlanId() == null)
        {
            throw new ServiceException("预算计划ID不能为空");
        }
        BizDecisionBudgetPlan databaseDecisionBudgetPlan =
            bizDecisionBudgetPlanMapper.selectBizDecisionBudgetPlanByPlanId(bizDecisionBudgetPlan.getPlanId());
        if (databaseDecisionBudgetPlan == null)
        {
            throw new ServiceException("预算计划不存在");
        }
        validatePlanEditable(databaseDecisionBudgetPlan);
        validateBudgetYearUnique(bizDecisionBudgetPlan.getBudgetYear(), bizDecisionBudgetPlan.getPlanId());
        BizDecisionBudgetPlan normalizedDecisionBudgetPlan = normalizeBizDecisionBudgetPlanForSave(bizDecisionBudgetPlan);
        normalizedDecisionBudgetPlan.setPlanStatus(PLAN_STATUS_DRAFT);
        normalizedDecisionBudgetPlan.setSubmitBy(null);
        normalizedDecisionBudgetPlan.setSubmitTime(null);
        normalizedDecisionBudgetPlan.setApproveBy(null);
        normalizedDecisionBudgetPlan.setApproveTime(null);
        normalizedDecisionBudgetPlan.setApproveRemark(null);
        normalizedDecisionBudgetPlan.setUpdateBy(operatorName);
        return bizDecisionBudgetPlanMapper.updateBizDecisionBudgetPlan(normalizedDecisionBudgetPlan);
    }

    /**
     * 提交年度预算计划审批
     * 
     * @param planId 预算计划ID
     * @param operatorName 操作人
     * @return 结果
     */
    @Override
    public int submitBizDecisionBudgetPlan(Long planId, String operatorName)
    {
        validateDecisionBudgetPlanTableReady();
        BizDecisionBudgetPlan databaseDecisionBudgetPlan = getRequiredBizDecisionBudgetPlan(planId);
        validatePlanEditable(databaseDecisionBudgetPlan);
        databaseDecisionBudgetPlan.setPlanStatus(PLAN_STATUS_SUBMITTED);
        databaseDecisionBudgetPlan.setSubmitBy(operatorName);
        databaseDecisionBudgetPlan.setSubmitTime(new Date());
        databaseDecisionBudgetPlan.setUpdateBy(operatorName);
        return bizDecisionBudgetPlanMapper.updateBizDecisionBudgetPlan(databaseDecisionBudgetPlan);
    }

    /**
     * 审批通过年度预算计划
     * 
     * @param planId 预算计划ID
     * @param operatorName 操作人
     * @param approveRemark 审批意见
     * @return 结果
     */
    @Override
    public int approveBizDecisionBudgetPlan(Long planId, String operatorName, String approveRemark)
    {
        validateDecisionBudgetPlanTableReady();
        BizDecisionBudgetPlan databaseDecisionBudgetPlan = getRequiredBizDecisionBudgetPlan(planId);
        validatePlanSubmitted(databaseDecisionBudgetPlan);
        databaseDecisionBudgetPlan.setPlanStatus(PLAN_STATUS_APPROVED);
        databaseDecisionBudgetPlan.setApproveBy(operatorName);
        databaseDecisionBudgetPlan.setApproveTime(new Date());
        databaseDecisionBudgetPlan.setApproveRemark(approveRemark);
        databaseDecisionBudgetPlan.setUpdateBy(operatorName);
        int updateRowCount = bizDecisionBudgetPlanMapper.updateBizDecisionBudgetPlan(databaseDecisionBudgetPlan);
        syncCurrentMonthDecisionBudgetConfig(convertBizDecisionBudgetPlanForDisplay(databaseDecisionBudgetPlan), operatorName);
        return updateRowCount;
    }

    /**
     * 驳回年度预算计划
     * 
     * @param planId 预算计划ID
     * @param operatorName 操作人
     * @param approveRemark 审批意见
     * @return 结果
     */
    @Override
    public int rejectBizDecisionBudgetPlan(Long planId, String operatorName, String approveRemark)
    {
        validateDecisionBudgetPlanTableReady();
        BizDecisionBudgetPlan databaseDecisionBudgetPlan = getRequiredBizDecisionBudgetPlan(planId);
        validatePlanSubmitted(databaseDecisionBudgetPlan);
        databaseDecisionBudgetPlan.setPlanStatus(PLAN_STATUS_REJECTED);
        databaseDecisionBudgetPlan.setApproveBy(operatorName);
        databaseDecisionBudgetPlan.setApproveTime(new Date());
        databaseDecisionBudgetPlan.setApproveRemark(approveRemark);
        databaseDecisionBudgetPlan.setUpdateBy(operatorName);
        return bizDecisionBudgetPlanMapper.updateBizDecisionBudgetPlan(databaseDecisionBudgetPlan);
    }

    /**
     * 查询必需存在的预算计划
     * 
     * @param planId 预算计划ID
     * @return 预算计划
     */
    private BizDecisionBudgetPlan getRequiredBizDecisionBudgetPlan(Long planId)
    {
        BizDecisionBudgetPlan databaseDecisionBudgetPlan = bizDecisionBudgetPlanMapper.selectBizDecisionBudgetPlanByPlanId(planId);
        if (databaseDecisionBudgetPlan == null)
        {
            throw new ServiceException("预算计划不存在");
        }
        return databaseDecisionBudgetPlan;
    }

    /**
     * 校验年度预算表是否已经初始化
     */
    private void validateDecisionBudgetPlanTableReady()
    {
        if (!hasDecisionBudgetPlanTable())
        {
            throw new ServiceException("管理驾驶舱二期数据表未初始化，请先执行 " + MANAGEMENT_PHASE_TWO_SQL_FILE_NAME);
        }
    }

    /**
     * 判断年度预算表是否存在
     * @return 是否存在
     */
    private boolean hasDecisionBudgetPlanTable()
    {
        String countSql =
            "select count(1) from information_schema.tables where table_schema = (select database()) and table_name = ?";
        Integer tableCount = jdbcTemplate.queryForObject(countSql, Integer.class, BIZ_DECISION_BUDGET_PLAN_TABLE_NAME);
        return tableCount != null && tableCount.intValue() > 0;
    }

    /**
     * 校验预算年度唯一性
     * 
     * @param budgetYear 预算年度
     * @param currentPlanId 当前计划ID
     */
    private void validateBudgetYearUnique(Integer budgetYear, Long currentPlanId)
    {
        if (budgetYear == null)
        {
            throw new ServiceException("预算年度不能为空");
        }
        BizDecisionBudgetPlan existingDecisionBudgetPlan = bizDecisionBudgetPlanMapper.selectBizDecisionBudgetPlanByBudgetYear(budgetYear);
        if (existingDecisionBudgetPlan == null)
        {
            return;
        }
        if (currentPlanId != null && currentPlanId.longValue() == existingDecisionBudgetPlan.getPlanId().longValue())
        {
            return;
        }
        throw new ServiceException("该年度预算计划已存在，请直接编辑当前计划");
    }

    /**
     * 校验预算计划是否允许编辑
     * 
     * @param bizDecisionBudgetPlan 预算计划
     */
    private void validatePlanEditable(BizDecisionBudgetPlan bizDecisionBudgetPlan)
    {
        String planStatus = StringUtils.defaultString(bizDecisionBudgetPlan.getPlanStatus(), PLAN_STATUS_DRAFT);
        if (PLAN_STATUS_SUBMITTED.equals(planStatus))
        {
            throw new ServiceException("预算计划已提交审批，暂不允许修改");
        }
        if (PLAN_STATUS_APPROVED.equals(planStatus))
        {
            throw new ServiceException("预算计划已审批通过，如需调整请重新规划新年度预算");
        }
    }

    /**
     * 校验预算计划是否处于待审批状态
     * 
     * @param bizDecisionBudgetPlan 预算计划
     */
    private void validatePlanSubmitted(BizDecisionBudgetPlan bizDecisionBudgetPlan)
    {
        String planStatus = StringUtils.defaultString(bizDecisionBudgetPlan.getPlanStatus(), PLAN_STATUS_DRAFT);
        if (!PLAN_STATUS_SUBMITTED.equals(planStatus))
        {
            throw new ServiceException("只有待审批状态的预算计划才能执行审批动作");
        }
    }

    /**
     * 构造默认空预算计划
     * 
     * @param budgetYear 预算年度
     * @return 默认空预算计划
     */
    private BizDecisionBudgetPlan buildEmptyBizDecisionBudgetPlan(int budgetYear)
    {
        BizDecisionBudgetPlan bizDecisionBudgetPlan = new BizDecisionBudgetPlan();
        bizDecisionBudgetPlan.setBudgetYear(budgetYear);
        bizDecisionBudgetPlan.setPlanName(budgetYear + "年度经营预算");
        bizDecisionBudgetPlan.setPlanStatus(PLAN_STATUS_DRAFT);
        bizDecisionBudgetPlan.setSaleBudgetAmount(BigDecimal.ZERO);
        bizDecisionBudgetPlan.setGrossProfitBudgetAmount(BigDecimal.ZERO);
        bizDecisionBudgetPlan.setCollectionBudgetAmount(BigDecimal.ZERO);
        bizDecisionBudgetPlan.setPurchaseBudgetAmount(BigDecimal.ZERO);
        bizDecisionBudgetPlan.setNetCashBudgetAmount(BigDecimal.ZERO);
        bizDecisionBudgetPlan.setSaleMonthlyPlanList(buildZeroMonthlyBudgetList());
        bizDecisionBudgetPlan.setGrossProfitMonthlyPlanList(buildZeroMonthlyBudgetList());
        bizDecisionBudgetPlan.setCollectionMonthlyPlanList(buildZeroMonthlyBudgetList());
        bizDecisionBudgetPlan.setPurchaseMonthlyPlanList(buildZeroMonthlyBudgetList());
        bizDecisionBudgetPlan.setNetCashMonthlyPlanList(buildZeroMonthlyBudgetList());
        return bizDecisionBudgetPlan;
    }

    /**
     * 规范预算计划保存数据
     * 
     * @param bizDecisionBudgetPlan 预算计划
     * @return 规范后的预算计划
     */
    private BizDecisionBudgetPlan normalizeBizDecisionBudgetPlanForSave(BizDecisionBudgetPlan bizDecisionBudgetPlan)
    {
        if (bizDecisionBudgetPlan.getBudgetYear() == null)
        {
            throw new ServiceException("预算年度不能为空");
        }
        bizDecisionBudgetPlan.setPlanName(StringUtils.isEmpty(bizDecisionBudgetPlan.getPlanName())
            ? bizDecisionBudgetPlan.getBudgetYear() + "年度经营预算"
            : bizDecisionBudgetPlan.getPlanName());

        List<BigDecimal> saleMonthlyPlanList =
            normalizeMonthlyBudgetList(bizDecisionBudgetPlan.getSaleMonthlyPlanList(), bizDecisionBudgetPlan.getSaleBudgetAmount());
        List<BigDecimal> grossProfitMonthlyPlanList =
            normalizeMonthlyBudgetList(bizDecisionBudgetPlan.getGrossProfitMonthlyPlanList(), bizDecisionBudgetPlan.getGrossProfitBudgetAmount());
        List<BigDecimal> collectionMonthlyPlanList =
            normalizeMonthlyBudgetList(bizDecisionBudgetPlan.getCollectionMonthlyPlanList(), bizDecisionBudgetPlan.getCollectionBudgetAmount());
        List<BigDecimal> purchaseMonthlyPlanList =
            normalizeMonthlyBudgetList(bizDecisionBudgetPlan.getPurchaseMonthlyPlanList(), bizDecisionBudgetPlan.getPurchaseBudgetAmount());
        List<BigDecimal> netCashMonthlyPlanList =
            normalizeMonthlyBudgetList(bizDecisionBudgetPlan.getNetCashMonthlyPlanList(), bizDecisionBudgetPlan.getNetCashBudgetAmount());

        bizDecisionBudgetPlan.setSaleMonthlyPlanList(saleMonthlyPlanList);
        bizDecisionBudgetPlan.setGrossProfitMonthlyPlanList(grossProfitMonthlyPlanList);
        bizDecisionBudgetPlan.setCollectionMonthlyPlanList(collectionMonthlyPlanList);
        bizDecisionBudgetPlan.setPurchaseMonthlyPlanList(purchaseMonthlyPlanList);
        bizDecisionBudgetPlan.setNetCashMonthlyPlanList(netCashMonthlyPlanList);
        bizDecisionBudgetPlan.setSaleBudgetAmount(sumMonthlyBudgetList(saleMonthlyPlanList));
        bizDecisionBudgetPlan.setGrossProfitBudgetAmount(sumMonthlyBudgetList(grossProfitMonthlyPlanList));
        bizDecisionBudgetPlan.setCollectionBudgetAmount(sumMonthlyBudgetList(collectionMonthlyPlanList));
        bizDecisionBudgetPlan.setPurchaseBudgetAmount(sumMonthlyBudgetList(purchaseMonthlyPlanList));
        bizDecisionBudgetPlan.setNetCashBudgetAmount(sumMonthlyBudgetList(netCashMonthlyPlanList));
        bizDecisionBudgetPlan.setSaleMonthlyPlanText(writeMonthlyBudgetListAsText(saleMonthlyPlanList));
        bizDecisionBudgetPlan.setGrossProfitMonthlyPlanText(writeMonthlyBudgetListAsText(grossProfitMonthlyPlanList));
        bizDecisionBudgetPlan.setCollectionMonthlyPlanText(writeMonthlyBudgetListAsText(collectionMonthlyPlanList));
        bizDecisionBudgetPlan.setPurchaseMonthlyPlanText(writeMonthlyBudgetListAsText(purchaseMonthlyPlanList));
        bizDecisionBudgetPlan.setNetCashMonthlyPlanText(writeMonthlyBudgetListAsText(netCashMonthlyPlanList));
        return bizDecisionBudgetPlan;
    }

    /**
     * 转换预算计划展示数据
     * 
     * @param bizDecisionBudgetPlan 预算计划
     * @return 展示用预算计划
     */
    private BizDecisionBudgetPlan convertBizDecisionBudgetPlanForDisplay(BizDecisionBudgetPlan bizDecisionBudgetPlan)
    {
        bizDecisionBudgetPlan.setSaleMonthlyPlanList(readMonthlyBudgetListFromText(
            bizDecisionBudgetPlan.getSaleMonthlyPlanText(), bizDecisionBudgetPlan.getSaleBudgetAmount()));
        bizDecisionBudgetPlan.setGrossProfitMonthlyPlanList(readMonthlyBudgetListFromText(
            bizDecisionBudgetPlan.getGrossProfitMonthlyPlanText(), bizDecisionBudgetPlan.getGrossProfitBudgetAmount()));
        bizDecisionBudgetPlan.setCollectionMonthlyPlanList(readMonthlyBudgetListFromText(
            bizDecisionBudgetPlan.getCollectionMonthlyPlanText(), bizDecisionBudgetPlan.getCollectionBudgetAmount()));
        bizDecisionBudgetPlan.setPurchaseMonthlyPlanList(readMonthlyBudgetListFromText(
            bizDecisionBudgetPlan.getPurchaseMonthlyPlanText(), bizDecisionBudgetPlan.getPurchaseBudgetAmount()));
        bizDecisionBudgetPlan.setNetCashMonthlyPlanList(readMonthlyBudgetListFromText(
            bizDecisionBudgetPlan.getNetCashMonthlyPlanText(), bizDecisionBudgetPlan.getNetCashBudgetAmount()));
        return bizDecisionBudgetPlan;
    }

    /**
     * 规范月度预算列表
     * 
     * @param sourceMonthlyBudgetList 原始月度预算列表
     * @param annualBudgetAmount 年度预算金额
     * @return 规范后的月度预算列表
     */
    private List<BigDecimal> normalizeMonthlyBudgetList(List<BigDecimal> sourceMonthlyBudgetList, BigDecimal annualBudgetAmount)
    {
        List<BigDecimal> normalizedMonthlyBudgetList = new ArrayList<BigDecimal>();
        boolean hasSourceValue = false;
        if (sourceMonthlyBudgetList != null)
        {
            for (BigDecimal sourceMonthlyBudgetAmount : sourceMonthlyBudgetList)
            {
                if (sourceMonthlyBudgetAmount != null && sourceMonthlyBudgetAmount.compareTo(BigDecimal.ZERO) != 0)
                {
                    hasSourceValue = true;
                    break;
                }
            }
        }
        if (hasSourceValue)
        {
            for (int monthIndexValue = 0; monthIndexValue < MONTH_COUNT; monthIndexValue++)
            {
                BigDecimal monthBudgetAmount = BigDecimal.ZERO;
                if (sourceMonthlyBudgetList.size() > monthIndexValue && sourceMonthlyBudgetList.get(monthIndexValue) != null)
                {
                    monthBudgetAmount = sourceMonthlyBudgetList.get(monthIndexValue).setScale(2, RoundingMode.HALF_UP);
                }
                normalizedMonthlyBudgetList.add(monthBudgetAmount);
            }
            return normalizedMonthlyBudgetList;
        }
        BigDecimal normalizedAnnualBudgetAmount = annualBudgetAmount == null ? BigDecimal.ZERO : annualBudgetAmount.setScale(2, RoundingMode.HALF_UP);
        if (normalizedAnnualBudgetAmount.compareTo(BigDecimal.ZERO) == 0)
        {
            return buildZeroMonthlyBudgetList();
        }
        BigDecimal averageMonthAmount = normalizedAnnualBudgetAmount.divide(new BigDecimal(MONTH_COUNT), 2, RoundingMode.DOWN);
        BigDecimal distributedAmount = BigDecimal.ZERO;
        for (int monthIndexValue = 0; monthIndexValue < MONTH_COUNT; monthIndexValue++)
        {
            if (monthIndexValue == MONTH_COUNT - 1)
            {
                normalizedMonthlyBudgetList.add(normalizedAnnualBudgetAmount.subtract(distributedAmount).setScale(2, RoundingMode.HALF_UP));
                continue;
            }
            normalizedMonthlyBudgetList.add(averageMonthAmount);
            distributedAmount = distributedAmount.add(averageMonthAmount);
        }
        return normalizedMonthlyBudgetList;
    }

    /**
     * 读取月度预算列表
     * 
     * @param monthlyPlanText 月度预算原始JSON
     * @param annualBudgetAmount 年度预算金额
     * @return 月度预算列表
     */
    private List<BigDecimal> readMonthlyBudgetListFromText(String monthlyPlanText, BigDecimal annualBudgetAmount)
    {
        if (StringUtils.isEmpty(monthlyPlanText))
        {
            return normalizeMonthlyBudgetList(null, annualBudgetAmount);
        }
        try
        {
            List<BigDecimal> monthlyBudgetList = objectMapper.readValue(monthlyPlanText, new TypeReference<List<BigDecimal>>()
            {
            });
            return normalizeMonthlyBudgetList(monthlyBudgetList, annualBudgetAmount);
        }
        catch (Exception exception)
        {
            throw new ServiceException("解析月度预算数据失败：" + exception.getMessage());
        }
    }

    /**
     * 序列化月度预算列表
     * 
     * @param monthlyBudgetList 月度预算列表
     * @return 月度预算原始JSON
     */
    private String writeMonthlyBudgetListAsText(List<BigDecimal> monthlyBudgetList)
    {
        try
        {
            return objectMapper.writeValueAsString(monthlyBudgetList);
        }
        catch (Exception exception)
        {
            throw new ServiceException("保存月度预算数据失败：" + exception.getMessage());
        }
    }

    /**
     * 汇总月度预算金额
     * 
     * @param monthlyBudgetList 月度预算列表
     * @return 汇总金额
     */
    private BigDecimal sumMonthlyBudgetList(List<BigDecimal> monthlyBudgetList)
    {
        BigDecimal totalBudgetAmount = BigDecimal.ZERO;
        for (BigDecimal monthBudgetAmount : monthlyBudgetList)
        {
            totalBudgetAmount = totalBudgetAmount.add(monthBudgetAmount == null ? BigDecimal.ZERO : monthBudgetAmount);
        }
        return totalBudgetAmount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 生成全零月度预算列表
     * 
     * @return 全零月度预算列表
     */
    private List<BigDecimal> buildZeroMonthlyBudgetList()
    {
        List<BigDecimal> zeroMonthlyBudgetList = new ArrayList<BigDecimal>();
        for (int monthIndexValue = 0; monthIndexValue < MONTH_COUNT; monthIndexValue++)
        {
            zeroMonthlyBudgetList.add(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
        return zeroMonthlyBudgetList;
    }

    /**
     * 审批通过后同步当前月份预算到管理驾驶舱参数
     * 
     * @param bizDecisionBudgetPlan 年度预算计划
     * @param operatorName 操作人
     */
    private void syncCurrentMonthDecisionBudgetConfig(BizDecisionBudgetPlan bizDecisionBudgetPlan, String operatorName)
    {
        int currentMonthIndexValue = LocalDate.now().getMonthValue() - 1;
        BigDecimal[] currentMonthBudgetAmountArray = {
            getMonthBudgetAmount(bizDecisionBudgetPlan.getSaleMonthlyPlanList(), currentMonthIndexValue),
            getMonthBudgetAmount(bizDecisionBudgetPlan.getGrossProfitMonthlyPlanList(), currentMonthIndexValue),
            getMonthBudgetAmount(bizDecisionBudgetPlan.getCollectionMonthlyPlanList(), currentMonthIndexValue),
            getMonthBudgetAmount(bizDecisionBudgetPlan.getPurchaseMonthlyPlanList(), currentMonthIndexValue),
            getMonthBudgetAmount(bizDecisionBudgetPlan.getNetCashMonthlyPlanList(), currentMonthIndexValue)
        };
        for (int configIndexValue = 0; configIndexValue < DECISION_BUDGET_KEY_ARRAY.length; configIndexValue++)
        {
            upsertDecisionBudgetConfig(
                DECISION_BUDGET_CONFIG_KEY_ARRAY[configIndexValue],
                DECISION_BUDGET_CONFIG_NAME_ARRAY[configIndexValue],
                currentMonthBudgetAmountArray[configIndexValue],
                DECISION_BUDGET_CONFIG_REMARK_ARRAY[configIndexValue],
                operatorName
            );
        }
    }

    /**
     * 获取指定月份预算金额
     * 
     * @param monthlyBudgetList 月度预算列表
     * @param monthIndexValue 月份索引
     * @return 月份预算金额
     */
    private BigDecimal getMonthBudgetAmount(List<BigDecimal> monthlyBudgetList, int monthIndexValue)
    {
        if (monthlyBudgetList == null || monthlyBudgetList.size() <= monthIndexValue)
        {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal monthBudgetAmount = monthlyBudgetList.get(monthIndexValue);
        return monthBudgetAmount == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : monthBudgetAmount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 新增或修改预算配置
     * 
     * @param configKey 配置键
     * @param configName 配置名称
     * @param configValue 配置值
     * @param remark 备注
     * @param operatorName 操作人
     */
    private void upsertDecisionBudgetConfig(String configKey, String configName, BigDecimal configValue, String remark, String operatorName)
    {
        SysConfig queryConfig = new SysConfig();
        queryConfig.setConfigKey(configKey);
        List<SysConfig> configList = sysConfigService.selectConfigList(queryConfig);
        String formattedConfigValue = (configValue == null ? BigDecimal.ZERO : configValue).setScale(2, RoundingMode.HALF_UP).toPlainString();
        if (configList == null || configList.isEmpty())
        {
            SysConfig insertConfig = new SysConfig();
            insertConfig.setConfigName(configName);
            insertConfig.setConfigKey(configKey);
            insertConfig.setConfigValue(formattedConfigValue);
            insertConfig.setConfigType("N");
            insertConfig.setRemark(remark);
            insertConfig.setCreateBy(operatorName);
            insertConfig.setUpdateBy(operatorName);
            sysConfigService.insertConfig(insertConfig);
            return;
        }
        SysConfig updateConfig = configList.get(0);
        updateConfig.setConfigName(configName);
        updateConfig.setConfigValue(formattedConfigValue);
        updateConfig.setConfigType("N");
        updateConfig.setRemark(remark);
        updateConfig.setUpdateBy(operatorName);
        sysConfigService.updateConfig(updateConfig);
    }
}
