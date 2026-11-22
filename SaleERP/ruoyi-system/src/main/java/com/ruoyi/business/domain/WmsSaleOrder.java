package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class WmsSaleOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long saleOrderId;

    @Excel(name = "销售单号")
    private String orderNo;

    @Excel(name = "客户编号")
    private Long customerId;

    /** 客户名称 */
    private String customerName;

    @Excel(name = "仓库编号")
    private Long warehouseId;

    /** 仓库名称 */
    private String warehouseName;

    @Excel(name = "总数量")
    private BigDecimal totalQty;

    @Excel(name = "总金额")
    private BigDecimal totalAmount;

    /** 应收金额 */
    @Excel(name = "应收金额")
    private BigDecimal receivableAmount;

    /** 已收金额 */
    @Excel(name = "已收金额")
    private BigDecimal receivedAmount;

    /** 未收金额 */
    @Excel(name = "未收金额")
    private BigDecimal remainReceivableAmount;

    /** 已出库数量 */
    @Excel(name = "已出库数量")
    private BigDecimal outboundQuantity;

    /** 待出库数量 */
    @Excel(name = "待出库数量")
    private BigDecimal remainQuantity;

    @Excel(name = "单据状态")
    private String status;

    @Excel(name = "付款状态")
    private String paymentStatus;

    /** 已开票金额 */
    @Excel(name = "已开票金额")
    private BigDecimal invoicedAmount;

    /** 开票状态（uninvoiced/partial/invoiced） */
    @Excel(name = "开票状态")
    private String invoiceStatus;

    private String stockDeductMode;

    @Excel(name = "审核人")
    private String auditBy;

    private Date auditTime;

    @Excel(name = "经理审核人")
    private String managerAuditBy;

    private Date managerAuditTime;

    @Excel(name = "经理审核意见")
    private String managerAuditComment;

    @Excel(name = "财务审核意见")
    private String financeAuditComment;

    /** 一体化建单明细列表（非表字段） */
    private List<WmsSaleOrderItem> items;

    public List<WmsSaleOrderItem> getItems()
    {
        return items;
    }

    public void setItems(List<WmsSaleOrderItem> items)
    {
        this.items = items;
    }

    public Long getSaleOrderId()
    {
        return saleOrderId;
    }

    public void setSaleOrderId(Long saleOrderId)
    {
        this.saleOrderId = saleOrderId;
    }

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    public Long getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(Long customerId)
    {
        this.customerId = customerId;
    }

    public String getCustomerName()
    {
        return customerName;
    }

    public void setCustomerName(String customerName)
    {
        this.customerName = customerName;
    }

    public String getWarehouseName()
    {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName)
    {
        this.warehouseName = warehouseName;
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

    /**
     * 获取应收金额
     *
     * @return 应收金额
     */
    public BigDecimal getReceivableAmount()
    {
        return receivableAmount;
    }

    /**
     * 设置应收金额
     *
     * @param receivableAmount 应收金额
     */
    public void setReceivableAmount(BigDecimal receivableAmount)
    {
        this.receivableAmount = receivableAmount;
    }

    /**
     * 获取已收金额
     *
     * @return 已收金额
     */
    public BigDecimal getReceivedAmount()
    {
        return receivedAmount;
    }

    /**
     * 设置已收金额
     *
     * @param receivedAmount 已收金额
     */
    public void setReceivedAmount(BigDecimal receivedAmount)
    {
        this.receivedAmount = receivedAmount;
    }

    /**
     * 获取未收金额
     *
     * @return 未收金额
     */
    public BigDecimal getRemainReceivableAmount()
    {
        return remainReceivableAmount;
    }

    /**
     * 设置未收金额
     *
     * @param remainReceivableAmount 未收金额
     */
    public void setRemainReceivableAmount(BigDecimal remainReceivableAmount)
    {
        this.remainReceivableAmount = remainReceivableAmount;
    }

    /**
     * 获取已出库数量
     *
     * @return 已出库数量
     */
    public BigDecimal getOutboundQuantity()
    {
        return outboundQuantity;
    }

    /**
     * 设置已出库数量
     *
     * @param outboundQuantity 已出库数量
     */
    public void setOutboundQuantity(BigDecimal outboundQuantity)
    {
        this.outboundQuantity = outboundQuantity;
    }

    /**
     * 获取待出库数量
     *
     * @return 待出库数量
     */
    public BigDecimal getRemainQuantity()
    {
        return remainQuantity;
    }

    /**
     * 设置待出库数量
     *
     * @param remainQuantity 待出库数量
     */
    public void setRemainQuantity(BigDecimal remainQuantity)
    {
        this.remainQuantity = remainQuantity;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getPaymentStatus()
    {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus)
    {
        this.paymentStatus = paymentStatus;
    }

    public BigDecimal getInvoicedAmount()
    {
        return invoicedAmount;
    }

    public void setInvoicedAmount(BigDecimal invoicedAmount)
    {
        this.invoicedAmount = invoicedAmount;
    }

    public String getInvoiceStatus()
    {
        return invoiceStatus;
    }

    public void setInvoiceStatus(String invoiceStatus)
    {
        this.invoiceStatus = invoiceStatus;
    }

    public String getStockDeductMode()
    {
        return stockDeductMode;
    }

    public void setStockDeductMode(String stockDeductMode)
    {
        this.stockDeductMode = stockDeductMode;
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

    public String getManagerAuditBy()
    {
        return managerAuditBy;
    }

    public void setManagerAuditBy(String managerAuditBy)
    {
        this.managerAuditBy = managerAuditBy;
    }

    public Date getManagerAuditTime()
    {
        return managerAuditTime;
    }

    public void setManagerAuditTime(Date managerAuditTime)
    {
        this.managerAuditTime = managerAuditTime;
    }

    public String getManagerAuditComment()
    {
        return managerAuditComment;
    }

    public void setManagerAuditComment(String managerAuditComment)
    {
        this.managerAuditComment = managerAuditComment;
    }

    public String getFinanceAuditComment()
    {
        return financeAuditComment;
    }

    public void setFinanceAuditComment(String financeAuditComment)
    {
        this.financeAuditComment = financeAuditComment;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("saleOrderId", getSaleOrderId())
            .append("orderNo", getOrderNo())
            .append("customerId", getCustomerId())
            .append("warehouseId", getWarehouseId())
            .append("totalQty", getTotalQty())
            .append("totalAmount", getTotalAmount())
            .append("receivableAmount", getReceivableAmount())
            .append("receivedAmount", getReceivedAmount())
            .append("remainReceivableAmount", getRemainReceivableAmount())
            .append("outboundQuantity", getOutboundQuantity())
            .append("remainQuantity", getRemainQuantity())
            .append("status", getStatus())
            .append("paymentStatus", getPaymentStatus())
            .append("auditBy", getAuditBy())
            .append("auditTime", getAuditTime())
            .append("managerAuditBy", getManagerAuditBy())
            .append("managerAuditTime", getManagerAuditTime())
            .append("managerAuditComment", getManagerAuditComment())
            .append("financeAuditComment", getFinanceAuditComment())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
