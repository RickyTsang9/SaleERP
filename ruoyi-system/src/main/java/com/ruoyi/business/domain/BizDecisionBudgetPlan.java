package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 管理层年度预算计划对象 biz_decision_budget_plan
 * 
 * @author ruoyi
 * @date 2026-04-11
 */
public class BizDecisionBudgetPlan extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 预算计划ID */
    private Long planId;

    /** 预算年度 */
    private Integer budgetYear;

    /** 预算计划名称 */
    private String planName;

    /** 版本号 */
    private Integer versionNo;

    /** 版本标签 */
    private String versionLabel;

    /** 基线计划ID */
    private Long basePlanId;

    /** 是否为当前生效版本（y是 n否） */
    private String effectiveFlag;

    /** 调整原因 */
    private String adjustmentReason;

    /** 计划状态（draft草稿 submitted待审批 approved已通过 rejected已驳回） */
    private String planStatus;

    /** 年度销售预算 */
    private BigDecimal saleBudgetAmount;

    /** 年度毛利预算 */
    private BigDecimal grossProfitBudgetAmount;

    /** 年度回款预算 */
    private BigDecimal collectionBudgetAmount;

    /** 年度采购预算 */
    private BigDecimal purchaseBudgetAmount;

    /** 年度净现金流预算 */
    private BigDecimal netCashBudgetAmount;

    /** 销售月度预算原始JSON */
    @JsonIgnore
    private String saleMonthlyPlanText;

    /** 毛利月度预算原始JSON */
    @JsonIgnore
    private String grossProfitMonthlyPlanText;

    /** 回款月度预算原始JSON */
    @JsonIgnore
    private String collectionMonthlyPlanText;

    /** 采购月度预算原始JSON */
    @JsonIgnore
    private String purchaseMonthlyPlanText;

    /** 净现金流月度预算原始JSON */
    @JsonIgnore
    private String netCashMonthlyPlanText;

    /** 销售月度预算列表 */
    private List<BigDecimal> saleMonthlyPlanList;

    /** 毛利月度预算列表 */
    private List<BigDecimal> grossProfitMonthlyPlanList;

    /** 回款月度预算列表 */
    private List<BigDecimal> collectionMonthlyPlanList;

    /** 采购月度预算列表 */
    private List<BigDecimal> purchaseMonthlyPlanList;

    /** 净现金流月度预算列表 */
    private List<BigDecimal> netCashMonthlyPlanList;

    /** 提交人 */
    private String submitBy;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    /** 审批人 */
    private String approveBy;

    /** 审批时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date approveTime;

    /** 审批意见 */
    private String approveRemark;

    public Long getPlanId()
    {
        return planId;
    }

    public void setPlanId(Long planId)
    {
        this.planId = planId;
    }

    public Integer getBudgetYear()
    {
        return budgetYear;
    }

    public void setBudgetYear(Integer budgetYear)
    {
        this.budgetYear = budgetYear;
    }

    public String getPlanName()
    {
        return planName;
    }

    public void setPlanName(String planName)
    {
        this.planName = planName;
    }

    public Integer getVersionNo()
    {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo)
    {
        this.versionNo = versionNo;
    }

    public String getVersionLabel()
    {
        return versionLabel;
    }

    public void setVersionLabel(String versionLabel)
    {
        this.versionLabel = versionLabel;
    }

    public Long getBasePlanId()
    {
        return basePlanId;
    }

    public void setBasePlanId(Long basePlanId)
    {
        this.basePlanId = basePlanId;
    }

    public String getEffectiveFlag()
    {
        return effectiveFlag;
    }

    public void setEffectiveFlag(String effectiveFlag)
    {
        this.effectiveFlag = effectiveFlag;
    }

    public String getAdjustmentReason()
    {
        return adjustmentReason;
    }

    public void setAdjustmentReason(String adjustmentReason)
    {
        this.adjustmentReason = adjustmentReason;
    }

    public String getPlanStatus()
    {
        return planStatus;
    }

    public void setPlanStatus(String planStatus)
    {
        this.planStatus = planStatus;
    }

    public BigDecimal getSaleBudgetAmount()
    {
        return saleBudgetAmount;
    }

    public void setSaleBudgetAmount(BigDecimal saleBudgetAmount)
    {
        this.saleBudgetAmount = saleBudgetAmount;
    }

    public BigDecimal getGrossProfitBudgetAmount()
    {
        return grossProfitBudgetAmount;
    }

    public void setGrossProfitBudgetAmount(BigDecimal grossProfitBudgetAmount)
    {
        this.grossProfitBudgetAmount = grossProfitBudgetAmount;
    }

    public BigDecimal getCollectionBudgetAmount()
    {
        return collectionBudgetAmount;
    }

    public void setCollectionBudgetAmount(BigDecimal collectionBudgetAmount)
    {
        this.collectionBudgetAmount = collectionBudgetAmount;
    }

    public BigDecimal getPurchaseBudgetAmount()
    {
        return purchaseBudgetAmount;
    }

    public void setPurchaseBudgetAmount(BigDecimal purchaseBudgetAmount)
    {
        this.purchaseBudgetAmount = purchaseBudgetAmount;
    }

    public BigDecimal getNetCashBudgetAmount()
    {
        return netCashBudgetAmount;
    }

    public void setNetCashBudgetAmount(BigDecimal netCashBudgetAmount)
    {
        this.netCashBudgetAmount = netCashBudgetAmount;
    }

    public String getSaleMonthlyPlanText()
    {
        return saleMonthlyPlanText;
    }

    public void setSaleMonthlyPlanText(String saleMonthlyPlanText)
    {
        this.saleMonthlyPlanText = saleMonthlyPlanText;
    }

    public String getGrossProfitMonthlyPlanText()
    {
        return grossProfitMonthlyPlanText;
    }

    public void setGrossProfitMonthlyPlanText(String grossProfitMonthlyPlanText)
    {
        this.grossProfitMonthlyPlanText = grossProfitMonthlyPlanText;
    }

    public String getCollectionMonthlyPlanText()
    {
        return collectionMonthlyPlanText;
    }

    public void setCollectionMonthlyPlanText(String collectionMonthlyPlanText)
    {
        this.collectionMonthlyPlanText = collectionMonthlyPlanText;
    }

    public String getPurchaseMonthlyPlanText()
    {
        return purchaseMonthlyPlanText;
    }

    public void setPurchaseMonthlyPlanText(String purchaseMonthlyPlanText)
    {
        this.purchaseMonthlyPlanText = purchaseMonthlyPlanText;
    }

    public String getNetCashMonthlyPlanText()
    {
        return netCashMonthlyPlanText;
    }

    public void setNetCashMonthlyPlanText(String netCashMonthlyPlanText)
    {
        this.netCashMonthlyPlanText = netCashMonthlyPlanText;
    }

    public List<BigDecimal> getSaleMonthlyPlanList()
    {
        return saleMonthlyPlanList;
    }

    public void setSaleMonthlyPlanList(List<BigDecimal> saleMonthlyPlanList)
    {
        this.saleMonthlyPlanList = saleMonthlyPlanList;
    }

    public List<BigDecimal> getGrossProfitMonthlyPlanList()
    {
        return grossProfitMonthlyPlanList;
    }

    public void setGrossProfitMonthlyPlanList(List<BigDecimal> grossProfitMonthlyPlanList)
    {
        this.grossProfitMonthlyPlanList = grossProfitMonthlyPlanList;
    }

    public List<BigDecimal> getCollectionMonthlyPlanList()
    {
        return collectionMonthlyPlanList;
    }

    public void setCollectionMonthlyPlanList(List<BigDecimal> collectionMonthlyPlanList)
    {
        this.collectionMonthlyPlanList = collectionMonthlyPlanList;
    }

    public List<BigDecimal> getPurchaseMonthlyPlanList()
    {
        return purchaseMonthlyPlanList;
    }

    public void setPurchaseMonthlyPlanList(List<BigDecimal> purchaseMonthlyPlanList)
    {
        this.purchaseMonthlyPlanList = purchaseMonthlyPlanList;
    }

    public List<BigDecimal> getNetCashMonthlyPlanList()
    {
        return netCashMonthlyPlanList;
    }

    public void setNetCashMonthlyPlanList(List<BigDecimal> netCashMonthlyPlanList)
    {
        this.netCashMonthlyPlanList = netCashMonthlyPlanList;
    }

    public String getSubmitBy()
    {
        return submitBy;
    }

    public void setSubmitBy(String submitBy)
    {
        this.submitBy = submitBy;
    }

    public Date getSubmitTime()
    {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime)
    {
        this.submitTime = submitTime;
    }

    public String getApproveBy()
    {
        return approveBy;
    }

    public void setApproveBy(String approveBy)
    {
        this.approveBy = approveBy;
    }

    public Date getApproveTime()
    {
        return approveTime;
    }

    public void setApproveTime(Date approveTime)
    {
        this.approveTime = approveTime;
    }

    public String getApproveRemark()
    {
        return approveRemark;
    }

    public void setApproveRemark(String approveRemark)
    {
        this.approveRemark = approveRemark;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("planId", getPlanId())
            .append("budgetYear", getBudgetYear())
            .append("planName", getPlanName())
            .append("versionNo", getVersionNo())
            .append("versionLabel", getVersionLabel())
            .append("basePlanId", getBasePlanId())
            .append("effectiveFlag", getEffectiveFlag())
            .append("adjustmentReason", getAdjustmentReason())
            .append("planStatus", getPlanStatus())
            .append("saleBudgetAmount", getSaleBudgetAmount())
            .append("grossProfitBudgetAmount", getGrossProfitBudgetAmount())
            .append("collectionBudgetAmount", getCollectionBudgetAmount())
            .append("purchaseBudgetAmount", getPurchaseBudgetAmount())
            .append("netCashBudgetAmount", getNetCashBudgetAmount())
            .append("saleMonthlyPlanList", getSaleMonthlyPlanList())
            .append("grossProfitMonthlyPlanList", getGrossProfitMonthlyPlanList())
            .append("collectionMonthlyPlanList", getCollectionMonthlyPlanList())
            .append("purchaseMonthlyPlanList", getPurchaseMonthlyPlanList())
            .append("netCashMonthlyPlanList", getNetCashMonthlyPlanList())
            .append("submitBy", getSubmitBy())
            .append("submitTime", getSubmitTime())
            .append("approveBy", getApproveBy())
            .append("approveTime", getApproveTime())
            .append("approveRemark", getApproveRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
