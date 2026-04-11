package com.ruoyi.business.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 库存调拨明细对象 wms_transfer_item
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
public class WmsTransferItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long transferItemId;

    /** 调拨单ID */
    @Excel(name = "调拨单ID")
    private Long transferId;

    /** 商品ID */
    @Excel(name = "商品ID")
    private Long productId;

    /** 调拨数量 */
    @Excel(name = "调拨数量")
    private BigDecimal quantity;

    public void setTransferItemId(Long transferItemId) 
    {
        this.transferItemId = transferItemId;
    }

    public Long getTransferItemId() 
    {
        return transferItemId;
    }

    public void setTransferId(Long transferId) 
    {
        this.transferId = transferId;
    }

    public Long getTransferId() 
    {
        return transferId;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("transferItemId", getTransferItemId())
            .append("transferId", getTransferId())
            .append("productId", getProductId())
            .append("quantity", getQuantity())
            .append("remark", getRemark())
            .toString();
    }
}