package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class FinReceivable extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long receivableId;

    @Excel(name = "销售单编号")
    private Long saleOrderId;

    @Excel(name = "客户编号")
    private Long customerId;

    @Excel(name = "应收金额")
    private BigDecimal amountDue;

    @Excel(name = "已收金额")
    private BigDecimal amountPaid;

    @Excel(name = "应收状态")
    private String status;

    private Date dueDate;

    @Excel(name = "未收金额")
    private BigDecimal remainAmount;

    @Excel(name = "逾期天数")
    private Long overdueDays;

    public Long getReceivableId()
    {
        return receivableId;
    }

    public void setReceivableId(Long receivableId)
    {
        this.receivableId = receivableId;
    }

    public Long getSaleOrderId()
    {
        return saleOrderId;
    }

    public void setSaleOrderId(Long saleOrderId)
    {
        this.saleOrderId = saleOrderId;
    }

    public Long getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Long customerId)
    {
        this.customerId = customerId;
    }

    public BigDecimal getAmountDue()
    {
        return amountDue;
    }

    public void setAmountDue(BigDecimal amountDue)
    {
        this.amountDue = amountDue;
    }

    public BigDecimal getAmountPaid()
    {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid)
    {
        this.amountPaid = amountPaid;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Date getDueDate()
    {
        return dueDate;
    }

    public void setDueDate(Date dueDate)
    {
        this.dueDate = dueDate;
    }

    public BigDecimal getRemainAmount()
    {
        return remainAmount;
    }

    public void setRemainAmount(BigDecimal remainAmount)
    {
        this.remainAmount = remainAmount;
    }

    public Long getOverdueDays()
    {
        return overdueDays;
    }

    public void setOverdueDays(Long overdueDays)
    {
        this.overdueDays = overdueDays;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("receivableId", getReceivableId())
            .append("saleOrderId", getSaleOrderId())
            .append("customerId", getCustomerId())
            .append("amountDue", getAmountDue())
            .append("amountPaid", getAmountPaid())
            .append("remainAmount", getRemainAmount())
            .append("status", getStatus())
            .append("dueDate", getDueDate())
            .append("overdueDays", getOverdueDays())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
