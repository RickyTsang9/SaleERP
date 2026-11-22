package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class WmsTransferItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long transferItemId;

    @Excel(name = "调拨单ID")
    private Long transferId;

    @Excel(name = "商品ID")
    private Long productId;

    private Long outLocationId;

    private Long inLocationId;

    private String batchNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expireDate;

    @Excel(name = "调拨数量")
    private BigDecimal quantity;

    private BigDecimal unitCost;

    public void setTransferItemId(Long transferItemId)
    {
        this.transferItemId = transferItemId;
    }

    public Long getTransferItemId()
    {
        return transferItemId;
    }

    public void setTransferId(Long transferId)
    {
        this.transferId = transferId;
    }

    public Long getTransferId()
    {
        return transferId;
    }

    public void setProductId(Long productId)
    {
        this.productId = productId;
    }

    public Long getProductId()
    {
        return productId;
    }

    public Long getOutLocationId()
    {
        return outLocationId;
    }

    public void setOutLocationId(Long outLocationId)
    {
        this.outLocationId = outLocationId;
    }

    public Long getInLocationId()
    {
        return inLocationId;
    }

    public void setInLocationId(Long inLocationId)
    {
        this.inLocationId = inLocationId;
    }

    public String getBatchNo()
    {
        return batchNo;
    }

    public void setBatchNo(String batchNo)
    {
        this.batchNo = batchNo;
    }

    public Date getExpireDate()
    {
        return expireDate;
    }

    public void setExpireDate(Date expireDate)
    {
        this.expireDate = expireDate;
    }

    public void setQuantity(BigDecimal quantity)
    {
        this.quantity = quantity;
    }

    public BigDecimal getQuantity()
    {
        return quantity;
    }

    public BigDecimal getUnitCost()
    {
        return unitCost;
    }

    public void setUnitCost(BigDecimal unitCost)
    {
        this.unitCost = unitCost;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("transferItemId", getTransferItemId())
            .append("transferId", getTransferId())
            .append("productId", getProductId())
            .append("batchNo", getBatchNo())
            .append("quantity", getQuantity())
            .append("unitCost", getUnitCost())
            .append("remark", getRemark())
            .toString();
    }
}
