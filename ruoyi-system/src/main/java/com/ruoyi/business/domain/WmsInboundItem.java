package com.ruoyi.business.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class WmsInboundItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long inboundItemId;

    @Excel(name = "入库单编号")
    private Long inboundId;

    @Excel(name = "商品编号")
    private Long productId;

    @Excel(name = "库位编号")
    private Long locationId;

    @Excel(name = "批次号")
    private String batchNo;

    @Excel(name = "数量")
    private BigDecimal quantity;

    @Excel(name = "单价")
    private BigDecimal price;

    @Excel(name = "金额")
    private BigDecimal amount;

    public Long getInboundItemId()
    {
        return inboundItemId;
    }

    public void setInboundItemId(Long inboundItemId)
    {
        this.inboundItemId = inboundItemId;
    }

    public Long getInboundId()
    {
        return inboundId;
    }

    public void setInboundId(Long inboundId)
    {
        this.inboundId = inboundId;
    }

    public Long getProductId()
    {
        return productId;
    }

    public void setProductId(Long productId)
    {
        this.productId = productId;
    }

    public Long getLocationId()
    {
        return locationId;
    }

    public void setLocationId(Long locationId)
    {
        this.locationId = locationId;
    }

    public String getBatchNo()
    {
        return batchNo;
    }

    public void setBatchNo(String batchNo)
    {
        this.batchNo = batchNo;
    }

    public BigDecimal getQuantity()
    {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity)
    {
        this.quantity = quantity;
    }

    public BigDecimal getPrice()
    {
        return price;
    }

    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("inboundItemId", getInboundItemId())
            .append("inboundId", getInboundId())
            .append("productId", getProductId())
            .append("locationId", getLocationId())
            .append("batchNo", getBatchNo())
            .append("quantity", getQuantity())
            .append("price", getPrice())
            .append("amount", getAmount())
            .toString();
    }
}
