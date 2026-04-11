package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class WmsOutbound extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long outboundId;

    @Excel(name = "出库单号")
    private String outboundNo;

    @Excel(name = "出库类型")
    private String outboundType;

    @Excel(name = "客户编号")
    private Long customerId;

    /** 来源销售单编号 */
    @Excel(name = "来源销售单编号")
    private Long saleOrderId;

    /** 来源销售单号 */
    @Excel(name = "来源销售单号")
    private String saleOrderNo;

    @Excel(name = "仓库编号")
    private Long warehouseId;

    @Excel(name = "总数量")
    private BigDecimal totalQty;

    @Excel(name = "总金额")
    private BigDecimal totalAmount;

    @Excel(name = "状态")
    private String status;

    @Excel(name = "审核人")
    private String auditBy;

    private Date auditTime;

    @Excel(name = "平台类型")
    private String platformType;

    @Excel(name = "店铺编号")
    private Long storeId;

    @Excel(name = "来源单号")
    private String sourceOrderNo;

    @Excel(name = "承运商")
    private String carrier;

    @Excel(name = "运单号")
    private String waybillNo;

    @Excel(name = "运费")
    private BigDecimal freightCost;

    @Excel(name = "配送状态")
    private String deliveryStatus;

    public Long getOutboundId()
    {
        return outboundId;
    }

    public void setOutboundId(Long outboundId)
    {
        this.outboundId = outboundId;
    }

    public String getOutboundNo()
    {
        return outboundNo;
    }

    public void setOutboundNo(String outboundNo)
    {
        this.outboundNo = outboundNo;
    }

    public String getOutboundType()
    {
        return outboundType;
    }

    public void setOutboundType(String outboundType)
    {
        this.outboundType = outboundType;
    }

    public Long getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Long customerId)
    {
        this.customerId = customerId;
    }

    /**
     * 获取来源销售单编号
     *
     * @return 来源销售单编号
     */
    public Long getSaleOrderId()
    {
        return saleOrderId;
    }

    /**
     * 设置来源销售单编号
     *
     * @param saleOrderId 来源销售单编号
     */
    public void setSaleOrderId(Long saleOrderId)
    {
        this.saleOrderId = saleOrderId;
    }

    /**
     * 获取来源销售单号
     *
     * @return 来源销售单号
     */
    public String getSaleOrderNo()
    {
        return saleOrderNo;
    }

    /**
     * 设置来源销售单号
     *
     * @param saleOrderNo 来源销售单号
     */
    public void setSaleOrderNo(String saleOrderNo)
    {
        this.saleOrderNo = saleOrderNo;
    }

    public Long getWarehouseId()
    {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId)
    {
        this.warehouseId = warehouseId;
    }

    public BigDecimal getTotalQty()
    {
        return totalQty;
    }

    public void setTotalQty(BigDecimal totalQty)
    {
        this.totalQty = totalQty;
    }

    public BigDecimal getTotalAmount()
    {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount)
    {
        this.totalAmount = totalAmount;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getAuditBy()
    {
        return auditBy;
    }

    public void setAuditBy(String auditBy)
    {
        this.auditBy = auditBy;
    }

    public Date getAuditTime()
    {
        return auditTime;
    }

    public void setAuditTime(Date auditTime)
    {
        this.auditTime = auditTime;
    }

    public String getPlatformType()
    {
        return platformType;
    }

    public void setPlatformType(String platformType)
    {
        this.platformType = platformType;
    }

    public Long getStoreId()
    {
        return storeId;
    }

    public void setStoreId(Long storeId)
    {
        this.storeId = storeId;
    }

    public String getSourceOrderNo()
    {
        return sourceOrderNo;
    }

    public void setSourceOrderNo(String sourceOrderNo)
    {
        this.sourceOrderNo = sourceOrderNo;
    }

    public String getCarrier()
    {
        return carrier;
    }

    public void setCarrier(String carrier)
    {
        this.carrier = carrier;
    }

    public String getWaybillNo()
    {
        return waybillNo;
    }

    public void setWaybillNo(String waybillNo)
    {
        this.waybillNo = waybillNo;
    }

    public BigDecimal getFreightCost()
    {
        return freightCost;
    }

    public void setFreightCost(BigDecimal freightCost)
    {
        this.freightCost = freightCost;
    }

    public String getDeliveryStatus()
    {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus)
    {
        this.deliveryStatus = deliveryStatus;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("outboundId", getOutboundId())
            .append("outboundNo", getOutboundNo())
            .append("outboundType", getOutboundType())
            .append("customerId", getCustomerId())
            .append("saleOrderId", getSaleOrderId())
            .append("saleOrderNo", getSaleOrderNo())
            .append("warehouseId", getWarehouseId())
            .append("totalQty", getTotalQty())
            .append("totalAmount", getTotalAmount())
            .append("status", getStatus())
            .append("auditBy", getAuditBy())
            .append("auditTime", getAuditTime())
            .append("platformType", getPlatformType())
            .append("storeId", getStoreId())
            .append("sourceOrderNo", getSourceOrderNo())
            .append("carrier", getCarrier())
            .append("waybillNo", getWaybillNo())
            .append("freightCost", getFreightCost())
            .append("deliveryStatus", getDeliveryStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
