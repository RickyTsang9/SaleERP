package com.ruoyi.business.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

public class WmsSaleOrderStatusHistory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long historyId;

    private Long saleOrderId;

    @Excel(name = "原状态")
    private String fromStatus;

    @Excel(name = "新状态")
    private String toStatus;

    @Excel(name = "操作类型")
    private String operationType;

    @Excel(name = "审核角色")
    private String auditRole;

    @Excel(name = "操作人")
    private String auditBy;

    @Excel(name = "审核意见")
    private String auditComment;

    private Date operateTime;

    public Long getHistoryId()
    {
        return historyId;
    }

    public void setHistoryId(Long historyId)
    {
        this.historyId = historyId;
    }

    public Long getSaleOrderId()
    {
        return saleOrderId;
    }

    public void setSaleOrderId(Long saleOrderId)
    {
        this.saleOrderId = saleOrderId;
    }

    public String getFromStatus()
    {
        return fromStatus;
    }

    public void setFromStatus(String fromStatus)
    {
        this.fromStatus = fromStatus;
    }

    public String getToStatus()
    {
        return toStatus;
    }

    public void setToStatus(String toStatus)
    {
        this.toStatus = toStatus;
    }

    public String getOperationType()
    {
        return operationType;
    }

    public void setOperationType(String operationType)
    {
        this.operationType = operationType;
    }

    public String getAuditRole()
    {
        return auditRole;
    }

    public void setAuditRole(String auditRole)
    {
        this.auditRole = auditRole;
    }

    public String getAuditBy()
    {
        return auditBy;
    }

    public void setAuditBy(String auditBy)
    {
        this.auditBy = auditBy;
    }

    public String getAuditComment()
    {
        return auditComment;
    }

    public void setAuditComment(String auditComment)
    {
        this.auditComment = auditComment;
    }

    public Date getOperateTime()
    {
        return operateTime;
    }

    public void setOperateTime(Date operateTime)
    {
        this.operateTime = operateTime;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("historyId", getHistoryId())
            .append("saleOrderId", getSaleOrderId())
            .append("fromStatus", getFromStatus())
            .append("toStatus", getToStatus())
            .append("operationType", getOperationType())
            .append("auditRole", getAuditRole())
            .append("auditBy", getAuditBy())
            .append("auditComment", getAuditComment())
            .append("operateTime", getOperateTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
