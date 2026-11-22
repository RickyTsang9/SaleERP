package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 付款记录对象 fin_payment
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
public class FinPayment extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 付款记录ID */
    private Long paymentId;

    /** 应付账款ID */
    @Excel(name = "应付账款ID")
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

    /** 供应商名称 */
    private String supplierName;

    /** 付款金额 */
    @Excel(name = "付款金额")
    private BigDecimal amount;

    /** 付款时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "付款时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date paymentTime;

    /** 付款方式 */
    @Excel(name = "付款方式")
    private String paymentMethod;

    private Long accountId;

    @Excel(name = "结算账户")
    private String accountName;

    @Excel(name = "状态", readConverterExp = "normal=正常,voided=已冲红")
    private String status;

    @Excel(name = "冲红原因")
    private String voidReason;

    private String voidBy;

    private Date voidTime;

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getVoidReason() { return voidReason; }
    public void setVoidReason(String voidReason) { this.voidReason = voidReason; }
    public String getVoidBy() { return voidBy; }
    public void setVoidBy(String voidBy) { this.voidBy = voidBy; }
    public Date getVoidTime() { return voidTime; }
    public void setVoidTime(Date voidTime) { this.voidTime = voidTime; }

    public void setPaymentId(Long paymentId) 
    {
        this.paymentId = paymentId;
    }

    public Long getPaymentId() 
    {
        return paymentId;
    }

    public String getSupplierName()
    {
        return supplierName;
    }

    public void setSupplierName(String supplierName)
    {
        this.supplierName = supplierName;
    }

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

    public void setAmount(BigDecimal amount) 
    {
        this.amount = amount;
    }

    public BigDecimal getAmount() 
    {
        return amount;
    }

    public void setPaymentTime(Date paymentTime) 
    {
        this.paymentTime = paymentTime;
    }

    public Date getPaymentTime() 
    {
        return paymentTime;
    }

    public void setPaymentMethod(String paymentMethod) 
    {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentMethod() 
    {
        return paymentMethod;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("paymentId", getPaymentId())
            .append("payableId", getPayableId())
            .append("purchaseOrderId", getPurchaseOrderId())
            .append("purchaseOrderNo", getPurchaseOrderNo())
            .append("supplierId", getSupplierId())
            .append("amount", getAmount())
            .append("paymentTime", getPaymentTime())
            .append("paymentMethod", getPaymentMethod())
            .append("remark", getRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
