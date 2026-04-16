package com.ruoyi.business.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class WmsOutboundItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long outboundItemId;

    @Excel(name = "出库单编号")
    private Long outboundId;

    /** 来源销售明细编号 */
    @Excel(name = "来源销售明细编号")
    private Long saleOrderItemId;

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

    /** 销售数量 */
    @Excel(name = "销售数量")
    private BigDecimal saleQuantity;

    /** 已出库数量 */
    @Excel(name = "已出库数量")
    private BigDecimal shippedQuantity;

    /** 待出库数量 */
    @Excel(name = "待出库数量")
    private BigDecimal remainQuantity;

    public Long getOutboundItemId()
    {
        return outboundItemId;
    }

    public void setOutboundItemId(Long outboundItemId)
    {
        this.outboundItemId = outboundItemId;
    }

    public Long getOutboundId()
    {
        return outboundId;
    }

    public void setOutboundId(Long outboundId)
    {
        this.outboundId = outboundId;
    }

    /**
     * 获取来源销售明细编号
     *
     * @return 来源销售明细编号
     */
    public Long getSaleOrderItemId()
    {
        return saleOrderItemId;
    }

    /**
     * 设置来源销售明细编号
     *
     * @param saleOrderItemId 来源销售明细编号
     */
    public void setSaleOrderItemId(Long saleOrderItemId)
    {
        this.saleOrderItemId = saleOrderItemId;
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
     * 获取销售数量
     *
     * @return 销售数量
     */
    public BigDecimal getSaleQuantity()
    {
        return saleQuantity;
    }

    /**
     * 设置销售数量
     *
     * @param saleQuantity 销售数量
     */
    public void setSaleQuantity(BigDecimal saleQuantity)
    {
        this.saleQuantity = saleQuantity;
    }

    /**
     * 获取已出库数量
     *
     * @return 已出库数量
     */
    public BigDecimal getShippedQuantity()
    {
        return shippedQuantity;
    }

    /**
     * 设置已出库数量
     *
     * @param shippedQuantity 已出库数量
     */
    public void setShippedQuantity(BigDecimal shippedQuantity)
    {
        this.shippedQuantity = shippedQuantity;
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
            .append("outboundItemId", getOutboundItemId())
            .append("outboundId", getOutboundId())
            .append("saleOrderItemId", getSaleOrderItemId())
            .append("productId", getProductId())
            .append("locationId", getLocationId())
            .append("batchNo", getBatchNo())
            .append("quantity", getQuantity())
            .append("price", getPrice())
            .append("amount", getAmount())
            .append("saleQuantity", getSaleQuantity())
            .append("shippedQuantity", getShippedQuantity())
            .append("remainQuantity", getRemainQuantity())
            .toString();
    }
}
