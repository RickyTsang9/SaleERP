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
 * 库存调拨对象 wms_transfer
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
public class WmsTransfer extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 调拨单ID */
    private Long transferId;

    /** 调拨单号 */
    @Excel(name = "调拨单号")
    private String transferNo;

    /** 调出仓库ID */
    @Excel(name = "调出仓库ID")
    private Long outWarehouseId;

    /** 调入仓库ID */
    @Excel(name = "调入仓库ID")
    private Long inWarehouseId;

    /** 调拨日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "调拨日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date transferDate;

    /** 状态（0待审核 1已审核 2已作废） */
    @Excel(name = "状态", readConverterExp = "0=待审核,1=已审核,2=已作废")
    private String status;

    /** 总数量 */
    @Excel(name = "总数量")
    private BigDecimal totalQuantity;

    /** 库存调拨明细信息 */
    private List<WmsTransferItem> wmsTransferItemList;

    public void setTransferId(Long transferId) 
    {
        this.transferId = transferId;
    }

    public Long getTransferId() 
    {
        return transferId;
    }

    public void setTransferNo(String transferNo) 
    {
        this.transferNo = transferNo;
    }

    public String getTransferNo() 
    {
        return transferNo;
    }

    public void setOutWarehouseId(Long outWarehouseId) 
    {
        this.outWarehouseId = outWarehouseId;
    }

    public Long getOutWarehouseId() 
    {
        return outWarehouseId;
    }

    public void setInWarehouseId(Long inWarehouseId) 
    {
        this.inWarehouseId = inWarehouseId;
    }

    public Long getInWarehouseId() 
    {
        return inWarehouseId;
    }

    public void setTransferDate(Date transferDate) 
    {
        this.transferDate = transferDate;
    }

    public Date getTransferDate() 
    {
        return transferDate;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setTotalQuantity(BigDecimal totalQuantity) 
    {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getTotalQuantity() 
    {
        return totalQuantity;
    }

    public List<WmsTransferItem> getWmsTransferItemList()
    {
        return wmsTransferItemList;
    }

    public void setWmsTransferItemList(List<WmsTransferItem> wmsTransferItemList)
    {
        this.wmsTransferItemList = wmsTransferItemList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("transferId", getTransferId())
            .append("transferNo", getTransferNo())
            .append("outWarehouseId", getOutWarehouseId())
            .append("inWarehouseId", getInWarehouseId())
            .append("transferDate", getTransferDate())
            .append("status", getStatus())
            .append("totalQuantity", getTotalQuantity())
            .append("remark", getRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("wmsTransferItemList", getWmsTransferItemList())
            .toString();
    }
}