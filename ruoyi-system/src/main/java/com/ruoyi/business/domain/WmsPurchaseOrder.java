package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 采购订单对象 wms_purchase_order
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
public class WmsPurchaseOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 采购订单ID */
    private Long purchaseOrderId;

    /** 采购单号 */
    @Excel(name = "采购单号")
    private String orderNo;

    /** 供应商ID */
    @Excel(name = "供应商ID")
    private Long supplierId;

    /** 采购日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "采购日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date purchaseDate;

    /** 总金额 */
    @Excel(name = "总金额")
    private BigDecimal totalAmount;

    /** 采购数量 */
    @Excel(name = "采购数量")
    private BigDecimal totalQuantity;

    /** 已审核入库单数 */
    @Excel(name = "已审核入库单数")
    private Integer inboundCount;

    /** 已入库数量 */
    @Excel(name = "已入库数量")
    private BigDecimal inboundQuantity;

    /** 待入库数量 */
    @Excel(name = "待入库数量")
    private BigDecimal remainQuantity;

    /** 状态（0待审核 1已审核 2部分入库 3已完成 4已作废） */
    @Excel(name = "状态", readConverterExp = "0=待审核,1=已审核,2=部分入库,3=已完成,4=已作废")
    private String status;

    /** 采购订单明细信息 */
    private List<WmsPurchaseOrderItem> wmsPurchaseOrderItemList;

    public void setPurchaseOrderId(Long purchaseOrderId) 
    {
        this.purchaseOrderId = purchaseOrderId;
    }

    public Long getPurchaseOrderId() 
    {
        return purchaseOrderId;
    }

    public void setOrderNo(String orderNo) 
    {
        this.orderNo = orderNo;
    }

    public String getOrderNo() 
    {
        return orderNo;
    }

    public void setSupplierId(Long supplierId) 
    {
        this.supplierId = supplierId;
    }

    public Long getSupplierId() 
    {
        return supplierId;
    }

    public void setPurchaseDate(Date purchaseDate) 
    {
        this.purchaseDate = purchaseDate;
    }

    public Date getPurchaseDate() 
    {
        return purchaseDate;
    }

    public void setTotalAmount(BigDecimal totalAmount) 
    {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getTotalAmount() 
    {
        return totalAmount;
    }

    /**
     * 设置采购数量
     *
     * @param totalQuantity 采购数量
     */
    public void setTotalQuantity(BigDecimal totalQuantity)
    {
        this.totalQuantity = totalQuantity;
    }

    /**
     * 获取采购数量
     *
     * @return 采购数量
     */
    public BigDecimal getTotalQuantity()
    {
        return totalQuantity;
    }

    /**
     * 设置已审核入库单数
     *
     * @param inboundCount 已审核入库单数
     */
    public void setInboundCount(Integer inboundCount)
    {
        this.inboundCount = inboundCount;
    }

    /**
     * 获取已审核入库单数
     *
     * @return 已审核入库单数
     */
    public Integer getInboundCount()
    {
        return inboundCount;
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

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public List<WmsPurchaseOrderItem> getWmsPurchaseOrderItemList()
    {
        return wmsPurchaseOrderItemList;
    }

    public void setWmsPurchaseOrderItemList(List<WmsPurchaseOrderItem> wmsPurchaseOrderItemList)
    {
        this.wmsPurchaseOrderItemList = wmsPurchaseOrderItemList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("purchaseOrderId", getPurchaseOrderId())
            .append("orderNo", getOrderNo())
            .append("supplierId", getSupplierId())
            .append("purchaseDate", getPurchaseDate())
            .append("totalAmount", getTotalAmount())
            .append("totalQuantity", getTotalQuantity())
            .append("inboundCount", getInboundCount())
            .append("inboundQuantity", getInboundQuantity())
            .append("remainQuantity", getRemainQuantity())
            .append("status", getStatus())
            .append("remark", getRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("wmsPurchaseOrderItemList", getWmsPurchaseOrderItemList())
            .toString();
    }
}
