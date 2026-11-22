package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 财务会计期间月结关账对象 fin_period_close
 */
public class FinPeriodClose extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long periodId;

    private String periodMonth;

    private String closeStatus;

    private BigDecimal saleAmountSnapshot;

    private BigDecimal purchaseAmountSnapshot;

    private String closedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date closedTime;

    private String reopenReason;

    public Long getPeriodId()
    {
        return periodId;
    }

    public void setPeriodId(Long periodId)
    {
        this.periodId = periodId;
    }

    public String getPeriodMonth()
    {
        return periodMonth;
    }

    public void setPeriodMonth(String periodMonth)
    {
        this.periodMonth = periodMonth;
    }

    public String getCloseStatus()
    {
        return closeStatus;
    }

    public void setCloseStatus(String closeStatus)
    {
        this.closeStatus = closeStatus;
    }

    public BigDecimal getSaleAmountSnapshot()
    {
        return saleAmountSnapshot;
    }

    public void setSaleAmountSnapshot(BigDecimal saleAmountSnapshot)
    {
        this.saleAmountSnapshot = saleAmountSnapshot;
    }

    public BigDecimal getPurchaseAmountSnapshot()
    {
        return purchaseAmountSnapshot;
    }

    public void setPurchaseAmountSnapshot(BigDecimal purchaseAmountSnapshot)
    {
        this.purchaseAmountSnapshot = purchaseAmountSnapshot;
    }

    public String getClosedBy()
    {
        return closedBy;
    }

    public void setClosedBy(String closedBy)
    {
        this.closedBy = closedBy;
    }

    public Date getClosedTime()
    {
        return closedTime;
    }

    public void setClosedTime(Date closedTime)
    {
        this.closedTime = closedTime;
    }

    public String getReopenReason()
    {
        return reopenReason;
    }

    public void setReopenReason(String reopenReason)
    {
        this.reopenReason = reopenReason;
    }
}
