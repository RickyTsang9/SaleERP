package com.ruoyi.business.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class WmsSaleReturnItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long saleReturnItemId;

    @Excel(name = "退货单编号")
    private Long saleReturnId;

    @Excel(name = "商品编号")
    private Long productId;

    @Excel(name = "数量")
    private BigDecimal quantity;

    @Excel(name = "单价")
    private BigDecimal price;

    @Excel(name = "金额")
    private BigDecimal amount;

    public Long getSaleReturnItemId()
    {
        return saleReturnItemId;
    }

    public void setSaleReturnItemId(Long saleReturnItemId)
    {
        this.saleReturnItemId = saleReturnItemId;
    }

    public Long getSaleReturnId()
    {
        return saleReturnId;
    }

    public void setSaleReturnId(Long saleReturnId)
    {
        this.saleReturnId = saleReturnId;
    }

    public Long getProductId()
    {
        return productId;
    }

    public void setProductId(Long productId)
    {
        this.productId = productId;
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
            .append("saleReturnItemId", getSaleReturnItemId())
            .append("saleReturnId", getSaleReturnId())
            .append("productId", getProductId())
            .append("quantity", getQuantity())
            .append("price", getPrice())
            .append("amount", getAmount())
            .toString();
    }
}
