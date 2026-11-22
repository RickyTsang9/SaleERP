package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 财务销项与进项发票台账对象 fin_invoice
 */
public class FinInvoice extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long invoiceId;

    @Excel(name = "发票号码")
    private String invoiceNo;

    @Excel(name = "发票代码")
    private String invoiceCode;

    @Excel(name = "发票方向", readConverterExp = "sale=销项发票,purchase=进项发票")
    private String invoiceDirection;

    @Excel(name = "发票类型", readConverterExp = "special=增值税专票,normal=增值税普票,electronic=全电数电票")
    private String invoiceType;

    private Long sourceOrderId;

    @Excel(name = "关联业务单号")
    private String sourceOrderNo;

    private Long targetId;

    @Excel(name = "往来单位名称")
    private String targetName;

    @Excel(name = "税率(%)")
    private BigDecimal taxRate;

    @Excel(name = "价税合计")
    private BigDecimal totalAmount;

    @Excel(name = "不含税金额")
    private BigDecimal amountWithoutTax;

    @Excel(name = "税额")
    private BigDecimal taxAmount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "开票/收票日期", width = 18, dateFormat = "yyyy-MM-dd")
    private Date invoiceDate;

    @Excel(name = "状态", readConverterExp = "draft=草稿,issued=正常有效,red_void=已红冲作废")
    private String status;

    @Excel(name = "红冲原因")
    private String voidReason;

    public Long getInvoiceId()
    {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId)
    {
        this.invoiceId = invoiceId;
    }

    public String getInvoiceNo()
    {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo)
    {
        this.invoiceNo = invoiceNo;
    }

    public String getInvoiceCode()
    {
        return invoiceCode;
    }

    public void setInvoiceCode(String invoiceCode)
    {
        this.invoiceCode = invoiceCode;
    }

    public String getInvoiceDirection()
    {
        return invoiceDirection;
    }

    public void setInvoiceDirection(String invoiceDirection)
    {
        this.invoiceDirection = invoiceDirection;
    }

    public String getInvoiceType()
    {
        return invoiceType;
    }

    public void setInvoiceType(String invoiceType)
    {
        this.invoiceType = invoiceType;
    }

    public Long getSourceOrderId()
    {
        return sourceOrderId;
    }

    public void setSourceOrderId(Long sourceOrderId)
    {
        this.sourceOrderId = sourceOrderId;
    }

    public String getSourceOrderNo()
    {
        return sourceOrderNo;
    }

    public void setSourceOrderNo(String sourceOrderNo)
    {
        this.sourceOrderNo = sourceOrderNo;
    }

    public Long getTargetId()
    {
        return targetId;
    }

    public void setTargetId(Long targetId)
    {
        this.targetId = targetId;
    }

    public String getTargetName()
    {
        return targetName;
    }

    public void setTargetName(String targetName)
    {
        this.targetName = targetName;
    }

    public BigDecimal getTaxRate()
    {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate)
    {
        this.taxRate = taxRate;
    }

    public BigDecimal getTotalAmount()
    {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount)
    {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getAmountWithoutTax()
    {
        return amountWithoutTax;
    }

    public void setAmountWithoutTax(BigDecimal amountWithoutTax)
    {
        this.amountWithoutTax = amountWithoutTax;
    }

    public BigDecimal getTaxAmount()
    {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount)
    {
        this.taxAmount = taxAmount;
    }

    public Date getInvoiceDate()
    {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate)
    {
        this.invoiceDate = invoiceDate;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getVoidReason()
    {
        return voidReason;
    }

    public void setVoidReason(String voidReason)
    {
        this.voidReason = voidReason;
    }
}
