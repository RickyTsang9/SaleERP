package com.ruoyi.business.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

public class BizMessage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long messageId;

    private String messageType;

    private String messageTitle;

    private String messageContent;

    private String messageLevel;

    private String businessType;

    private Long businessId;

    private String status;

    private String readStatus;

    public Long getMessageId()
    {
        return messageId;
    }

    public void setMessageId(Long messageId)
    {
        this.messageId = messageId;
    }

    public String getMessageType()
    {
        return messageType;
    }

    public void setMessageType(String messageType)
    {
        this.messageType = messageType;
    }

    public String getMessageTitle()
    {
        return messageTitle;
    }

    public void setMessageTitle(String messageTitle)
    {
        this.messageTitle = messageTitle;
    }

    public String getMessageContent()
    {
        return messageContent;
    }

    public void setMessageContent(String messageContent)
    {
        this.messageContent = messageContent;
    }

    public String getMessageLevel()
    {
        return messageLevel;
    }

    public void setMessageLevel(String messageLevel)
    {
        this.messageLevel = messageLevel;
    }

    public String getBusinessType()
    {
        return businessType;
    }

    public void setBusinessType(String businessType)
    {
        this.businessType = businessType;
    }

    public Long getBusinessId()
    {
        return businessId;
    }

    public void setBusinessId(Long businessId)
    {
        this.businessId = businessId;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getReadStatus()
    {
        return readStatus;
    }

    public void setReadStatus(String readStatus)
    {
        this.readStatus = readStatus;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("messageId", getMessageId())
            .append("messageType", getMessageType())
            .append("messageTitle", getMessageTitle())
            .append("messageContent", getMessageContent())
            .append("messageLevel", getMessageLevel())
            .append("businessType", getBusinessType())
            .append("businessId", getBusinessId())
            .append("status", getStatus())
            .append("readStatus", getReadStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
