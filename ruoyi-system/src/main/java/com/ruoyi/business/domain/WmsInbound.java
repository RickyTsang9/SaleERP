package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class WmsInbound extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long inboundId;

    @Excel(name = "入库单号")
    private String inboundNo;

    @Excel(name = "入库类型")
    private String inboundType;

    @Excel(name = "供应商编号")
    private Long supplierId;

    @Excel(name = "仓库编号")
    private Long warehouseId;

    @Excel(name = "总数量")
    private BigDecimal totalQty;

    @Excel(name = "总金额")
    private BigDecimal totalAmount;

    @Excel(name = "状态")
    private String status;

    @Excel(name = "审核人")
    private String auditBy;

    private Date auditTime;

    public Long getInboundId()
    {
        return inboundId;
    }

    public void setInboundId(Long inboundId)
    {
        this.inboundId = inboundId;
    }

    public String getInboundNo()
    {
        return inboundNo;
    }

    public void setInboundNo(String inboundNo)
    {
        this.inboundNo = inboundNo;
    }

    public String getInboundType()
    {
        return inboundType;
    }

    public void setInboundType(String inboundType)
    {
        this.inboundType = inboundType;
    }

    public Long getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(Long supplierId)
    {
        this.supplierId = supplierId;
    }

    public Long getWarehouseId()
    {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId)
    {
        this.warehouseId = warehouseId;
    }

    public BigDecimal getTotalQty()
    {
        return totalQty;
    }

    public void setTotalQty(BigDecimal totalQty)
    {
        this.totalQty = totalQty;
    }

    public BigDecimal getTotalAmount()
    {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount)
    {
        this.totalAmount = totalAmount;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
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
            .append("inboundId", getInboundId())
            .append("inboundNo", getInboundNo())
            .append("inboundType", getInboundType())
            .append("supplierId", getSupplierId())
            .append("warehouseId", getWarehouseId())
            .append("totalQty", getTotalQty())
            .append("totalAmount", getTotalAmount())
            .append("status", getStatus())
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
