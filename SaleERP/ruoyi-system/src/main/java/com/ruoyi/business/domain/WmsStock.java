package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
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

    /** 仓库名称 */
    private String warehouseName;

    @Excel(name = "商品编号")
    private Long productId;

    /** 商品编码 */
    private String productCode;

    /** 商品名称 */
    private String productName;

    @Excel(name = "库位编号")
    private Long locationId;

    /** 库位名称 */
    private String locationName;

    @Excel(name = "批次号")
    private String batchNo;

    /** 批次效期/到期日 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "效期到期日", width = 20, dateFormat = "yyyy-MM-dd")
    private Date expireDate;

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

    /** 期初导入单位成本（仅用于期初库存导入） */
    @Excel(name = "期初单位成本")
    private BigDecimal openingCostPrice;

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

    public String getWarehouseName()
    {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName)
    {
        this.warehouseName = warehouseName;
    }

    public String getProductCode()
    {
        return productCode;
    }

    public void setProductCode(String productCode)
    {
        this.productCode = productCode;
    }

    public String getProductName()
    {
        return productName;
    }

    public void setProductName(String productName)
    {
        this.productName = productName;
    }

    public String getLocationName()
    {
        return locationName;
    }

    public void setLocationName(String locationName)
    {
        this.locationName = locationName;
    }

    public Date getExpireDate()
    {
        return expireDate;
    }

    public void setExpireDate(Date expireDate)
    {
        this.expireDate = expireDate;
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

    public BigDecimal getOpeningCostPrice()
    {
        return openingCostPrice;
    }

    public void setOpeningCostPrice(BigDecimal openingCostPrice)
    {
        this.openingCostPrice = openingCostPrice;
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
