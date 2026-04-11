package com.ruoyi.business.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class WmsStockLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long stockLogId;

    @Excel(name = "仓库编号")
    private Long warehouseId;

    @Excel(name = "商品编号")
    private Long productId;

    @Excel(name = "库位编号")
    private Long locationId;

    @Excel(name = "批次号")
    private String batchNo;

    @Excel(name = "单据类型")
    private String billType;

    @Excel(name = "单据编号")
    private Long billId;

    @Excel(name = "单据号")
    private String billNo;

    @Excel(name = "出入库方向")
    private String inOut;

    @Excel(name = "变动数量")
    private BigDecimal quantity;

    @Excel(name = "单价")
    private BigDecimal price;

    @Excel(name = "金额")
    private BigDecimal amount;

    @Excel(name = "变动前数量")
    private BigDecimal beforeQty;

    @Excel(name = "变动后数量")
    private BigDecimal afterQty;

    public Long getStockLogId()
    {
        return stockLogId;
    }

    public void setStockLogId(Long stockLogId)
    {
        this.stockLogId = stockLogId;
    }

    public Long getWarehouseId()
    {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId)
    {
        this.warehouseId = warehouseId;
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

    public String getBillType()
    {
        return billType;
    }

    public void setBillType(String billType)
    {
        this.billType = billType;
    }

    public Long getBillId()
    {
        return billId;
    }

    public void setBillId(Long billId)
    {
        this.billId = billId;
    }

    public String getBillNo()
    {
        return billNo;
    }

    public void setBillNo(String billNo)
    {
        this.billNo = billNo;
    }

    public String getInOut()
    {
        return inOut;
    }

    public void setInOut(String inOut)
    {
        this.inOut = inOut;
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

    public BigDecimal getBeforeQty()
    {
        return beforeQty;
    }

    public void setBeforeQty(BigDecimal beforeQty)
    {
        this.beforeQty = beforeQty;
    }

    public BigDecimal getAfterQty()
    {
        return afterQty;
    }

    public void setAfterQty(BigDecimal afterQty)
    {
        this.afterQty = afterQty;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("stockLogId", getStockLogId())
            .append("warehouseId", getWarehouseId())
            .append("productId", getProductId())
            .append("locationId", getLocationId())
            .append("batchNo", getBatchNo())
            .append("billType", getBillType())
            .append("billId", getBillId())
            .append("billNo", getBillNo())
            .append("inOut", getInOut())
            .append("quantity", getQuantity())
            .append("price", getPrice())
            .append("amount", getAmount())
            .append("beforeQty", getBeforeQty())
            .append("afterQty", getAfterQty())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .toString();
    }
}
