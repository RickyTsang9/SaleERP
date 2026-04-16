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

    /** 来源采购订单明细编号 */
    @Excel(name = "来源采购明细编号")
    private Long purchaseOrderItemId;

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

    /** 来源采购数量 */
    @Excel(name = "来源采购数量")
    private BigDecimal purchaseQuantity;

    /** 已入库数量 */
    @Excel(name = "已入库数量")
    private BigDecimal receivedQuantity;

    /** 待入库数量 */
    @Excel(name = "待入库数量")
    private BigDecimal remainQuantity;

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

    /**
     * 获取来源采购订单明细编号
     *
     * @return 来源采购订单明细编号
     */
    public Long getPurchaseOrderItemId()
    {
        return purchaseOrderItemId;
    }

    /**
     * 设置来源采购订单明细编号
     *
     * @param purchaseOrderItemId 来源采购订单明细编号
     */
    public void setPurchaseOrderItemId(Long purchaseOrderItemId)
    {
        this.purchaseOrderItemId = purchaseOrderItemId;
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
     * 获取来源采购数量
     *
     * @return 来源采购数量
     */
    public BigDecimal getPurchaseQuantity()
    {
        return purchaseQuantity;
    }

    /**
     * 设置来源采购数量
     *
     * @param purchaseQuantity 来源采购数量
     */
    public void setPurchaseQuantity(BigDecimal purchaseQuantity)
    {
        this.purchaseQuantity = purchaseQuantity;
    }

    /**
     * 获取已入库数量
     *
     * @return 已入库数量
     */
    public BigDecimal getReceivedQuantity()
    {
        return receivedQuantity;
    }

    /**
     * 设置已入库数量
     *
     * @param receivedQuantity 已入库数量
     */
    public void setReceivedQuantity(BigDecimal receivedQuantity)
    {
        this.receivedQuantity = receivedQuantity;
    }

    /**
     * 获取待入库数量
     *
     * @return 待入库数量
     */
    public BigDecimal getRemainQuantity()
    {
        return remainQuantity;
    }

    /**
     * 设置待入库数量
     *
     * @param remainQuantity 待入库数量
     */
    public void setRemainQuantity(BigDecimal remainQuantity)
    {
        this.remainQuantity = remainQuantity;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("inboundItemId", getInboundItemId())
            .append("inboundId", getInboundId())
            .append("purchaseOrderItemId", getPurchaseOrderItemId())
            .append("productId", getProductId())
            .append("locationId", getLocationId())
            .append("batchNo", getBatchNo())
            .append("quantity", getQuantity())
            .append("price", getPrice())
            .append("amount", getAmount())
            .append("purchaseQuantity", getPurchaseQuantity())
            .append("receivedQuantity", getReceivedQuantity())
            .append("remainQuantity", getRemainQuantity())
            .toString();
    }
}
