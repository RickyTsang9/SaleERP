package com.ruoyi.business.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class WmsPurchaseReturn extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long purchaseReturnId;

    @Excel(name = "退货单号")
    private String returnNo;

    @Excel(name = "退货类型")
    private String returnType;

    @Excel(name = "供应商编号")
    private Long supplierId;

    /** 关联采购订单ID */
    private Long purchaseOrderId;

    /** 关联采购单号 */
    private String purchaseOrderNo;

    /** 供应商名称 */
    private String supplierName;

    @Excel(name = "仓库编号")
    private Long warehouseId;

    /** 仓库名称 */
    private String warehouseName;

    @Excel(name = "总数量")
    private BigDecimal totalQty;

    @Excel(name = "总金额")
    private BigDecimal totalAmount;

    @Excel(name = "状态")
    private String status;

    @Excel(name = "审核人")
    private String auditBy;

    private Date auditTime;

    /** 审核驳回原因 */
    private String rejectReason;

    /** 一体化建单明细列表（非表字段） */
    private List<WmsPurchaseReturnItem> items;

    public List<WmsPurchaseReturnItem> getItems()
    {
        return items;
    }

    public void setItems(List<WmsPurchaseReturnItem> items)
    {
        this.items = items;
    }

    public String getRejectReason()
    {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason)
    {
        this.rejectReason = rejectReason;
    }

    public Long getPurchaseReturnId()
    {
        return purchaseReturnId;
    }

    public String getSupplierName()
    {
        return supplierName;
    }

    public void setSupplierName(String supplierName)
    {
        this.supplierName = supplierName;
    }

    public String getWarehouseName()
    {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName)
    {
        this.warehouseName = warehouseName;
    }

    public void setPurchaseReturnId(Long purchaseReturnId)
    {
        this.purchaseReturnId = purchaseReturnId;
    }

    public String getReturnNo()
    {
        return returnNo;
    }

    public void setReturnNo(String returnNo)
    {
        this.returnNo = returnNo;
    }

    public String getReturnType()
    {
        return returnType;
    }

    public void setReturnType(String returnType)
    {
        this.returnType = returnType;
    }

    public Long getSupplierId()
    {
        return supplierId;
    }

    public void setSupplierId(Long supplierId)
    {
        this.supplierId = supplierId;
    }

    public Long getPurchaseOrderId()
    {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(Long purchaseOrderId)
    {
        this.purchaseOrderId = purchaseOrderId;
    }

    public String getPurchaseOrderNo()
    {
        return purchaseOrderNo;
    }

    public void setPurchaseOrderNo(String purchaseOrderNo)
    {
        this.purchaseOrderNo = purchaseOrderNo;
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

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("purchaseReturnId", getPurchaseReturnId())
            .append("returnNo", getReturnNo())
            .append("returnType", getReturnType())
            .append("supplierId", getSupplierId())
            .append("warehouseId", getWarehouseId())
            .append("totalQty", getTotalQty())
            .append("totalAmount", getTotalAmount())
            .append("status", getStatus())
            .append("auditBy", getAuditBy())
            .append("auditTime", getAuditTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
