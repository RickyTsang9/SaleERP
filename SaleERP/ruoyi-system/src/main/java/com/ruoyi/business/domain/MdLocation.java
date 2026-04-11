package com.ruoyi.business.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class MdLocation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long locationId;

    @Excel(name = "仓库编号")
    private Long warehouseId;

    @Excel(name = "库位编码")
    private String locationCode;

    @Excel(name = "库位名称")
    private String locationName;

    @Excel(name = "状态")
    private String status;

    private String delFlag;

    public Long getLocationId()
    {
        return locationId;
    }

    public void setLocationId(Long locationId)
    {
        this.locationId = locationId;
    }

    public Long getWarehouseId()
    {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId)
    {
        this.warehouseId = warehouseId;
    }

    public String getLocationCode()
    {
        return locationCode;
    }

    public void setLocationCode(String locationCode)
    {
        this.locationCode = locationCode;
    }

    public String getLocationName()
    {
        return locationName;
    }

    public void setLocationName(String locationName)
    {
        this.locationName = locationName;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("locationId", getLocationId())
            .append("warehouseId", getWarehouseId())
            .append("locationCode", getLocationCode())
            .append("locationName", getLocationName())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
