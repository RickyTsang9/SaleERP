package com.ruoyi.business.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 采购订单明细对象 wms_purchase_order_item
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
public class WmsPurchaseOrderItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long purchaseOrderItemId;

    /** 采购订单ID */
    @Excel(name = "采购订单ID")
    private Long purchaseOrderId;

    /** 商品ID */
    @Excel(name = "商品ID")
    private Long productId;

    /** 采购数量 */
    @Excel(name = "采购数量")
    private BigDecimal quantity;

    /** 单价 */
    @Excel(name = "单价")
    private BigDecimal price;

    /** 金额 */
    @Excel(name = "金额")
    private BigDecimal amount;

    /** 已入库数量 */
    @Excel(name = "已入库数量")
    private BigDecimal inboundQuantity;

    /** 待入库数量 */
    @Excel(name = "待入库数量")
    private BigDecimal remainQuantity;

    public void setPurchaseOrderItemId(Long purchaseOrderItemId) 
    {
        this.purchaseOrderItemId = purchaseOrderItemId;
    }

    public Long getPurchaseOrderItemId() 
    {
        return purchaseOrderItemId;
    }

    public void setPurchaseOrderId(Long purchaseOrderId) 
    {
        this.purchaseOrderId = purchaseOrderId;
    }

    public Long getPurchaseOrderId() 
    {
        return purchaseOrderId;
    }

    public void setProductId(Long productId) 
    {
        this.productId = productId;
    }

    public Long getProductId() 
    {
        return productId;
    }

    public void setQuantity(BigDecimal quantity) 
    {
        this.quantity = quantity;
    }

    public BigDecimal getQuantity() 
    {
        return quantity;
    }

    public void setPrice(BigDecimal price) 
    {
        this.price = price;
    }

    public BigDecimal getPrice() 
    {
        return price;
    }

    public void setAmount(BigDecimal amount) 
    {
        this.amount = amount;
    }

    public BigDecimal getAmount() 
    {
        return amount;
    }

    /**
     * 设置已入库数量
     *
     * @param inboundQuantity 已入库数量
     */
    public void setInboundQuantity(BigDecimal inboundQuantity)
    {
        this.inboundQuantity = inboundQuantity;
    }

    /**
     * 获取已入库数量
     *
     * @return 已入库数量
     */
    public BigDecimal getInboundQuantity()
    {
        return inboundQuantity;
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

    /**
     * 获取待入库数量
     *
     * @return 待入库数量
     */
    public BigDecimal getRemainQuantity()
    {
        return remainQuantity;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("purchaseOrderItemId", getPurchaseOrderItemId())
            .append("purchaseOrderId", getPurchaseOrderId())
            .append("productId", getProductId())
            .append("quantity", getQuantity())
            .append("price", getPrice())
            .append("amount", getAmount())
            .append("inboundQuantity", getInboundQuantity())
            .append("remainQuantity", getRemainQuantity())
            .append("remark", getRemark())
            .toString();
    }
}
