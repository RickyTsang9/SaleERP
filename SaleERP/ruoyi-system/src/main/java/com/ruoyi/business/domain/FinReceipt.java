package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class FinReceipt extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long receiptId;

    @Excel(name = "应收编号")
    private Long receivableId;

    @Excel(name = "销售单编号")
    private Long saleOrderId;

    @Excel(name = "客户编号")
    private Long customerId;

    @Excel(name = "回款金额")
    private BigDecimal amount;

    private Date paymentTime;

    @Excel(name = "回款方式")
    private String paymentMethod;

    public Long getReceiptId()
    {
        return receiptId;
    }

    public void setReceiptId(Long receiptId)
    {
        this.receiptId = receiptId;
    }

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

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public Date getPaymentTime()
    {
        return paymentTime;
    }

    public void setPaymentTime(Date paymentTime)
    {
        this.paymentTime = paymentTime;
    }

    public String getPaymentMethod()
    {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod)
    {
        this.paymentMethod = paymentMethod;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("receiptId", getReceiptId())
            .append("receivableId", getReceivableId())
            .append("saleOrderId", getSaleOrderId())
            .append("customerId", getCustomerId())
            .append("amount", getAmount())
            .append("paymentTime", getPaymentTime())
            .append("paymentMethod", getPaymentMethod())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
