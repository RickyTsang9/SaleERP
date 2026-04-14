package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 管理层经营快照对象 biz_operation_snapshot
 * 
 * @author ruoyi
 * @date 2026-04-12
 */
public class BizOperationSnapshot extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 快照ID */
    private Long snapshotId;

    /** 快照标题 */
    private String snapshotTitle;

    /** 快照日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date snapshotDate;

    /** 快照类型 */
    private String snapshotType;

    /** 销售额 */
    private BigDecimal saleAmount;

    /** 毛利额 */
    private BigDecimal grossProfitAmount;

    /** 回款率 */
    private BigDecimal collectionRate;

    /** 逾期笔数 */
    private Integer overdueCount;

    /** 库存预警数 */
    private Integer stockWarningCount;

    /** 待办事项数 */
    private Integer pendingActionCount;

    /** 经营结论 */
    private String summaryContent;

    /** 快照纯文本 */
    private String plainTextContent;

    /** 生成来源 */
    private String sourceMode;

    public Long getSnapshotId()
    {
        return snapshotId;
    }

    public void setSnapshotId(Long snapshotId)
    {
        this.snapshotId = snapshotId;
    }

    public String getSnapshotTitle()
    {
        return snapshotTitle;
    }

    public void setSnapshotTitle(String snapshotTitle)
    {
        this.snapshotTitle = snapshotTitle;
    }

    public Date getSnapshotDate()
    {
        return snapshotDate;
    }

    public void setSnapshotDate(Date snapshotDate)
    {
        this.snapshotDate = snapshotDate;
    }

    public String getSnapshotType()
    {
        return snapshotType;
    }

    public void setSnapshotType(String snapshotType)
    {
        this.snapshotType = snapshotType;
    }

    public BigDecimal getSaleAmount()
    {
        return saleAmount;
    }

    public void setSaleAmount(BigDecimal saleAmount)
    {
        this.saleAmount = saleAmount;
    }

    public BigDecimal getGrossProfitAmount()
    {
        return grossProfitAmount;
    }

    public void setGrossProfitAmount(BigDecimal grossProfitAmount)
    {
        this.grossProfitAmount = grossProfitAmount;
    }

    public BigDecimal getCollectionRate()
    {
        return collectionRate;
    }

    public void setCollectionRate(BigDecimal collectionRate)
    {
        this.collectionRate = collectionRate;
    }

    public Integer getOverdueCount()
    {
        return overdueCount;
    }

    public void setOverdueCount(Integer overdueCount)
    {
        this.overdueCount = overdueCount;
    }

    public Integer getStockWarningCount()
    {
        return stockWarningCount;
    }

    public void setStockWarningCount(Integer stockWarningCount)
    {
        this.stockWarningCount = stockWarningCount;
    }

    public Integer getPendingActionCount()
    {
        return pendingActionCount;
    }

    public void setPendingActionCount(Integer pendingActionCount)
    {
        this.pendingActionCount = pendingActionCount;
    }

    public String getSummaryContent()
    {
        return summaryContent;
    }

    public void setSummaryContent(String summaryContent)
    {
        this.summaryContent = summaryContent;
    }

    public String getPlainTextContent()
    {
        return plainTextContent;
    }

    public void setPlainTextContent(String plainTextContent)
    {
        this.plainTextContent = plainTextContent;
    }

    public String getSourceMode()
    {
        return sourceMode;
    }

    public void setSourceMode(String sourceMode)
    {
        this.sourceMode = sourceMode;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("snapshotId", getSnapshotId())
            .append("snapshotTitle", getSnapshotTitle())
            .append("snapshotDate", getSnapshotDate())
            .append("snapshotType", getSnapshotType())
            .append("saleAmount", getSaleAmount())
            .append("grossProfitAmount", getGrossProfitAmount())
            .append("collectionRate", getCollectionRate())
            .append("overdueCount", getOverdueCount())
            .append("stockWarningCount", getStockWarningCount())
            .append("pendingActionCount", getPendingActionCount())
            .append("summaryContent", getSummaryContent())
            .append("plainTextContent", getPlainTextContent())
            .append("sourceMode", getSourceMode())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
