package com.ruoyi.business.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class WmsStock extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long stockId;

    @Excel(name = "仓库编号")
    private Long warehouseId;

    @Excel(name = "商品编号")
    private Long productId;

    @Excel(name = "库位编号")
    private Long locationId;

    @Excel(name = "批次号")
    private String batchNo;

    @Excel(name = "库存数量")
    private BigDecimal quantity;

    @Excel(name = "锁定数量")
    private BigDecimal lockedQuantity;

    @Excel(name = "冻结数量")
    private BigDecimal frozenQuantity;

    @Excel(name = "最小预警值")
    private BigDecimal warningMinQty;

    @Excel(name = "最大预警值")
    private BigDecimal warningMaxQty;

    private Integer version;

    public Long getStockId()
    {
        return stockId;
    }

    public void setStockId(Long stockId)
    {
        this.stockId = stockId;
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

    public BigDecimal getQuantity()
    {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity)
    {
        this.quantity = quantity;
    }

    public BigDecimal getLockedQuantity()
    {
        return lockedQuantity;
    }

    public void setLockedQuantity(BigDecimal lockedQuantity)
    {
        this.lockedQuantity = lockedQuantity;
    }

    public BigDecimal getFrozenQuantity()
    {
        return frozenQuantity;
    }

    public void setFrozenQuantity(BigDecimal frozenQuantity)
    {
        this.frozenQuantity = frozenQuantity;
    }

    public BigDecimal getWarningMinQty()
    {
        return warningMinQty;
    }

    public void setWarningMinQty(BigDecimal warningMinQty)
    {
        this.warningMinQty = warningMinQty;
    }

    public BigDecimal getWarningMaxQty()
    {
        return warningMaxQty;
    }

    public void setWarningMaxQty(BigDecimal warningMaxQty)
    {
        this.warningMaxQty = warningMaxQty;
    }

    public Integer getVersion()
    {
        return version;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("stockId", getStockId())
            .append("warehouseId", getWarehouseId())
            .append("productId", getProductId())
            .append("locationId", getLocationId())
            .append("batchNo", getBatchNo())
            .append("quantity", getQuantity())
            .append("lockedQuantity", getLockedQuantity())
            .append("frozenQuantity", getFrozenQuantity())
            .append("warningMinQty", getWarningMinQty())
            .append("warningMaxQty", getWarningMaxQty())
            .append("version", getVersion())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
