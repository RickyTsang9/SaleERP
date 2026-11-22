package com.ruoyi.business.domain;

import java.math.BigDecimal;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资金结算账户对象 fin_account
 */
public class FinAccount extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long accountId;

    @Excel(name = "账户编码")
    private String accountCode;

    @Excel(name = "账户名称")
    private String accountName;

    @Excel(name = "账户类型", readConverterExp = "bank=银行对公/储蓄户,wechat=微信商户,alipay=支付宝商户,cash=现金备用金")
    private String accountType;

    @Excel(name = "开户行/平台")
    private String bankName;

    @Excel(name = "账号/商户号")
    private String bankNo;

    @Excel(name = "期初余额")
    private BigDecimal openingBalance;

    @Excel(name = "当前实时余额")
    private BigDecimal currentBalance;

    @Excel(name = "默认账户", readConverterExp = "Y=是,N=否")
    private String isDefault;

    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public Long getAccountId()
    {
        return accountId;
    }

    public void setAccountId(Long accountId)
    {
        this.accountId = accountId;
    }

    public String getAccountCode()
    {
        return accountCode;
    }

    public void setAccountCode(String accountCode)
    {
        this.accountCode = accountCode;
    }

    public String getAccountName()
    {
        return accountName;
    }

    public void setAccountName(String accountName)
    {
        this.accountName = accountName;
    }

    public String getAccountType()
    {
        return accountType;
    }

    public void setAccountType(String accountType)
    {
        this.accountType = accountType;
    }

    public String getBankName()
    {
        return bankName;
    }

    public void setBankName(String bankName)
    {
        this.bankName = bankName;
    }

    public String getBankNo()
    {
        return bankNo;
    }

    public void setBankNo(String bankNo)
    {
        this.bankNo = bankNo;
    }

    public BigDecimal getOpeningBalance()
    {
        return openingBalance;
    }

    public void setOpeningBalance(BigDecimal openingBalance)
    {
        this.openingBalance = openingBalance;
    }

    public BigDecimal getCurrentBalance()
    {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance)
    {
        this.currentBalance = currentBalance;
    }

    public String getIsDefault()
    {
        return isDefault;
    }

    public void setIsDefault(String isDefault)
    {
        this.isDefault = isDefault;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
}
