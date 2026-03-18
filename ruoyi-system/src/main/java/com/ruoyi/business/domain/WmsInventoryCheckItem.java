package com.ruoyi.business.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class WmsInventoryCheckItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long checkItemId;

    @Excel(name = "盘点单编号")
    private Long checkId;

    @Excel(name = "商品编号")
    private Long productId;

    @Excel(name = "库位编号")
    private Long locationId;

    @Excel(name = "批次号")
    private String batchNo;

    @Excel(name = "账面数量")
    private BigDecimal stockQty;

    @Excel(name = "实盘数量")
    private BigDecimal actualQty;

    @Excel(name = "差异数量")
    private BigDecimal diffQty;

    @Excel(name = "单价")
    private BigDecimal price;

    @Excel(name = "差异金额")
    private BigDecimal diffAmount;

    public Long getCheckItemId()
    {
        return checkItemId;
    }

    public void setCheckItemId(Long checkItemId)
    {
        this.checkItemId = checkItemId;
    }

    public Long getCheckId()
    {
        return checkId;
    }

    public void setCheckId(Long checkId)
    {
        this.checkId = checkId;
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

    public BigDecimal getStockQty()
    {
        return stockQty;
    }

    public void setStockQty(BigDecimal stockQty)
    {
        this.stockQty = stockQty;
    }

    public BigDecimal getActualQty()
    {
        return actualQty;
    }

    public void setActualQty(BigDecimal actualQty)
    {
        this.actualQty = actualQty;
    }

    public BigDecimal getDiffQty()
    {
        return diffQty;
    }

    public void setDiffQty(BigDecimal diffQty)
    {
        this.diffQty = diffQty;
    }

    public BigDecimal getPrice()
    {
        return price;
    }

    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    public BigDecimal getDiffAmount()
    {
        return diffAmount;
    }

    public void setDiffAmount(BigDecimal diffAmount)
    {
        this.diffAmount = diffAmount;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("checkItemId", getCheckItemId())
            .append("checkId", getCheckId())
            .append("productId", getProductId())
            .append("locationId", getLocationId())
            .append("batchNo", getBatchNo())
            .append("stockQty", getStockQty())
            .append("actualQty", getActualQty())
            .append("diffQty", getDiffQty())
            .append("price", getPrice())
            .append("diffAmount", getDiffAmount())
            .toString();
    }
}
