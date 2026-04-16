package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 应付账款对象 fin_payable
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
public class FinPayable extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 应付账款ID */
    private Long payableId;

    /** 采购订单ID */
    @Excel(name = "采购订单ID")
    private Long purchaseOrderId;

    /** 采购单号 */
    @Excel(name = "采购单号")
    private String purchaseOrderNo;

    /** 供应商ID */
    @Excel(name = "供应商ID")
    private Long supplierId;

    /** 应付金额 */
    @Excel(name = "应付金额")
    private BigDecimal amountDue;

    /** 已付金额 */
    @Excel(name = "已付金额")
    private BigDecimal amountPaid;

    /** 未付金额 */
    @Excel(name = "未付金额")
    private BigDecimal remainAmount;

    /** 状态（0未付款 1部分付款 2已付款） */
    @Excel(name = "状态", readConverterExp = "0=未付款,1=部分付款,2=已付款")
    private String status;

    /** 应付日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "应付日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date dueDate;

    /** 逾期天数 */
    @Excel(name = "逾期天数")
    private Long overdueDays;

    public void setPayableId(Long payableId) 
    {
        this.payableId = payableId;
    }

    public Long getPayableId() 
    {
        return payableId;
    }

    public void setPurchaseOrderId(Long purchaseOrderId) 
    {
        this.purchaseOrderId = purchaseOrderId;
    }

    public Long getPurchaseOrderId() 
    {
        return purchaseOrderId;
    }

    public void setPurchaseOrderNo(String purchaseOrderNo) 
    {
        this.purchaseOrderNo = purchaseOrderNo;
    }

    public String getPurchaseOrderNo() 
    {
        return purchaseOrderNo;
    }

    public void setSupplierId(Long supplierId) 
    {
        this.supplierId = supplierId;
    }

    public Long getSupplierId() 
    {
        return supplierId;
    }

    public void setAmountDue(BigDecimal amountDue) 
    {
        this.amountDue = amountDue;
    }

    public BigDecimal getAmountDue() 
    {
        return amountDue;
    }

    public void setAmountPaid(BigDecimal amountPaid) 
    {
        this.amountPaid = amountPaid;
    }

    public BigDecimal getAmountPaid() 
    {
        return amountPaid;
    }

    public void setRemainAmount(BigDecimal remainAmount) 
    {
        this.remainAmount = remainAmount;
    }

    public BigDecimal getRemainAmount() 
    {
        return remainAmount;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setDueDate(Date dueDate) 
    {
        this.dueDate = dueDate;
    }

    public Date getDueDate() 
    {
        return dueDate;
    }

    public void setOverdueDays(Long overdueDays) 
    {
        this.overdueDays = overdueDays;
    }

    public Long getOverdueDays() 
    {
        return overdueDays;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("payableId", getPayableId())
            .append("purchaseOrderId", getPurchaseOrderId())
            .append("purchaseOrderNo", getPurchaseOrderNo())
            .append("supplierId", getSupplierId())
            .append("amountDue", getAmountDue())
            .append("amountPaid", getAmountPaid())
            .append("remainAmount", getRemainAmount())
            .append("status", getStatus())
            .append("dueDate", getDueDate())
            .append("overdueDays", getOverdueDays())
            .append("remark", getRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
