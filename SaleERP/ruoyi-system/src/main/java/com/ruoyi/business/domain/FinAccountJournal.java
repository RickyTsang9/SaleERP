package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资金结算账户收支流水明细对象 fin_account_journal
 */
public class FinAccountJournal extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long journalId;

    @Excel(name = "资金流水号")
    private String journalNo;

    private Long accountId;

    @Excel(name = "资金账户")
    private String accountName;

    @Excel(name = "收支方向", readConverterExp = "in=收入,out=支出")
    private String direction;

    @Excel(name = "业务类型")
    private String bizType;

    private Long bizId;

    @Excel(name = "关联单号")
    private String bizNo;

    @Excel(name = "往来单位/对手方")
    private String targetName;

    @Excel(name = "发生金额")
    private BigDecimal amount;

    @Excel(name = "变动前余额")
    private BigDecimal beforeBalance;

    @Excel(name = "变动后余额")
    private BigDecimal afterBalance;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "发生时间", width = 22, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date occurTime;

    public Long getJournalId()
    {
        return journalId;
    }

    public void setJournalId(Long journalId)
    {
        this.journalId = journalId;
    }

    public String getJournalNo()
    {
        return journalNo;
    }

    public void setJournalNo(String journalNo)
    {
        this.journalNo = journalNo;
    }

    public Long getAccountId()
    {
        return accountId;
    }

    public void setAccountId(Long accountId)
    {
        this.accountId = accountId;
    }

    public String getAccountName()
    {
        return accountName;
    }

    public void setAccountName(String accountName)
    {
        this.accountName = accountName;
    }

    public String getDirection()
    {
        return direction;
    }

    public void setDirection(String direction)
    {
        this.direction = direction;
    }

    public String getBizType()
    {
        return bizType;
    }

    public void setBizType(String bizType)
    {
        this.bizType = bizType;
    }

    public Long getBizId()
    {
        return bizId;
    }

    public void setBizId(Long bizId)
    {
        this.bizId = bizId;
    }

    public String getBizNo()
    {
        return bizNo;
    }

    public void setBizNo(String bizNo)
    {
        this.bizNo = bizNo;
    }

    public String getTargetName()
    {
        return targetName;
    }

    public void setTargetName(String targetName)
    {
        this.targetName = targetName;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public BigDecimal getBeforeBalance()
    {
        return beforeBalance;
    }

    public void setBeforeBalance(BigDecimal beforeBalance)
    {
        this.beforeBalance = beforeBalance;
    }

    public BigDecimal getAfterBalance()
    {
        return afterBalance;
    }

    public void setAfterBalance(BigDecimal afterBalance)
    {
        this.afterBalance = afterBalance;
    }

    public Date getOccurTime()
    {
        return occurTime;
    }

    public void setOccurTime(Date occurTime)
    {
        this.occurTime = occurTime;
    }
}
