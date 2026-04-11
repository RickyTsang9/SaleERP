package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class WmsInventoryCheck extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long checkId;

    @Excel(name = "盘点单号")
    private String checkNo;

    @Excel(name = "仓库编号")
    private Long warehouseId;

    @Excel(name = "状态")
    private String status;

    @Excel(name = "差异数量")
    private BigDecimal totalDiffQty;

    @Excel(name = "差异金额")
    private BigDecimal totalDiffAmount;

    @Excel(name = "审核人")
    private String auditBy;

    private Date auditTime;

    public Long getCheckId()
    {
        return checkId;
    }

    public void setCheckId(Long checkId)
    {
        this.checkId = checkId;
    }

    public String getCheckNo()
    {
        return checkNo;
    }

    public void setCheckNo(String checkNo)
    {
        this.checkNo = checkNo;
    }

    public Long getWarehouseId()
    {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId)
    {
        this.warehouseId = warehouseId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public BigDecimal getTotalDiffQty()
    {
        return totalDiffQty;
    }

    public void setTotalDiffQty(BigDecimal totalDiffQty)
    {
        this.totalDiffQty = totalDiffQty;
    }

    public BigDecimal getTotalDiffAmount()
    {
        return totalDiffAmount;
    }

    public void setTotalDiffAmount(BigDecimal totalDiffAmount)
    {
        this.totalDiffAmount = totalDiffAmount;
    }

    public String getAuditBy()
    {
        return auditBy;
    }

    public void setAuditBy(String auditBy)
    {
        this.auditBy = auditBy;
    }

    public Date getAuditTime()
    {
        return auditTime;
    }

    public void setAuditTime(Date auditTime)
    {
        this.auditTime = auditTime;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("checkId", getCheckId())
            .append("checkNo", getCheckNo())
            .append("warehouseId", getWarehouseId())
            .append("status", getStatus())
            .append("totalDiffQty", getTotalDiffQty())
            .append("totalDiffAmount", getTotalDiffAmount())
            .append("auditBy", getAuditBy())
            .append("auditTime", getAuditTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
