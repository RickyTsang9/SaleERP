package com.ruoyi.business.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class MdCustomer extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long customerId;

    @Excel(name = "客户编码")
    private String customerCode;

    @Excel(name = "客户名称")
    private String customerName;

    @Excel(name = "联系人")
    private String contactPerson;

    @Excel(name = "联系电话")
    private String contactPhone;

    @Excel(name = "邮箱")
    private String contactEmail;

    @Excel(name = "地址")
    private String address;

    @Excel(name = "信用额度")
    private BigDecimal creditLimit;

    @Excel(name = "付款方式")
    private String paymentMethod;

    @Excel(name = "客户等级")
    private String customerLevel;

    @Excel(name = "状态")
    private String status;

    private String delFlag;

    public Long getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Long customerId)
    {
        this.customerId = customerId;
    }

    public String getCustomerCode()
    {
        return customerCode;
    }

    public void setCustomerCode(String customerCode)
    {
        this.customerCode = customerCode;
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public void setCustomerName(String customerName)
    {
        this.customerName = customerName;
    }

    public String getContactPerson()
    {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson)
    {
        this.contactPerson = contactPerson;
    }

    public String getContactPhone()
    {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone)
    {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail()
    {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail)
    {
        this.contactEmail = contactEmail;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public BigDecimal getCreditLimit()
    {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit)
    {
        this.creditLimit = creditLimit;
    }

    public String getPaymentMethod()
    {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod)
    {
        this.paymentMethod = paymentMethod;
    }

    public String getCustomerLevel()
    {
        return customerLevel;
    }

    public void setCustomerLevel(String customerLevel)
    {
        this.customerLevel = customerLevel;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("customerId", getCustomerId())
            .append("customerCode", getCustomerCode())
            .append("customerName", getCustomerName())
            .append("contactPerson", getContactPerson())
            .append("contactPhone", getContactPhone())
            .append("contactEmail", getContactEmail())
            .append("address", getAddress())
            .append("creditLimit", getCreditLimit())
            .append("paymentMethod", getPaymentMethod())
            .append("customerLevel", getCustomerLevel())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
