package com.ruoyi.business.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class WmsSaleOrderItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long saleOrderItemId;

    @Excel(name = "销售单编号")
    private Long saleOrderId;

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

    /** 已出库数量 */
    @Excel(name = "已出库数量")
    private BigDecimal outboundQuantity;

    /** 待出库数量 */
    @Excel(name = "待出库数量")
    private BigDecimal remainQuantity;

    public Long getSaleOrderItemId()
    {
        return saleOrderItemId;
    }

    public void setSaleOrderItemId(Long saleOrderItemId)
    {
        this.saleOrderItemId = saleOrderItemId;
    }

    public Long getSaleOrderId()
    {
        return saleOrderId;
    }

    public void setSaleOrderId(Long saleOrderId)
    {
        this.saleOrderId = saleOrderId;
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

    /**
     * 获取已出库数量
     *
     * @return 已出库数量
     */
    public BigDecimal getOutboundQuantity()
    {
        return outboundQuantity;
    }

    /**
     * 设置已出库数量
     *
     * @param outboundQuantity 已出库数量
     */
    public void setOutboundQuantity(BigDecimal outboundQuantity)
    {
        this.outboundQuantity = outboundQuantity;
    }

    /**
     * 获取待出库数量
     *
     * @return 待出库数量
     */
    public BigDecimal getRemainQuantity()
    {
        return remainQuantity;
    }

    /**
     * 设置待出库数量
     *
     * @param remainQuantity 待出库数量
     */
    public void setRemainQuantity(BigDecimal remainQuantity)
    {
        this.remainQuantity = remainQuantity;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("saleOrderItemId", getSaleOrderItemId())
            .append("saleOrderId", getSaleOrderId())
            .append("productId", getProductId())
            .append("locationId", getLocationId())
            .append("batchNo", getBatchNo())
            .append("quantity", getQuantity())
            .append("price", getPrice())
            .append("amount", getAmount())
            .append("outboundQuantity", getOutboundQuantity())
            .append("remainQuantity", getRemainQuantity())
            .toString();
    }
}
