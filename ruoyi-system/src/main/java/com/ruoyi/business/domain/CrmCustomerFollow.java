package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 客户跟进记录对象 crm_customer_follow
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
public class CrmCustomerFollow extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 跟进记录ID */
    private Long followId;

    /** 客户ID */
    @Excel(name = "客户ID")
    private Long customerId;

    /** 跟进方式（如电话、上门、微信等） */
    @Excel(name = "跟进方式", readConverterExp = "如=电话、上门、微信等")
    private String followType;

    /** 跟进时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "跟进时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date followTime;

    /** 跟进内容 */
    @Excel(name = "跟进内容")
    private String followContent;

    /** 下次跟进时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "下次跟进时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date nextFollowTime;

    /** 跟进状态（0进行中 1已转化 2已流失） */
    @Excel(name = "跟进状态", readConverterExp = "0=进行中,1=已转化,2=已流失")
    private String status;

    /** 待办筛选类型 */
    private String todoType;

    public void setFollowId(Long followId) 
    {
        this.followId = followId;
    }

    public Long getFollowId() 
    {
        return followId;
    }

    public void setCustomerId(Long customerId) 
    {
        this.customerId = customerId;
    }

    public Long getCustomerId() 
    {
        return customerId;
    }

    public void setFollowType(String followType) 
    {
        this.followType = followType;
    }

    public String getFollowType() 
    {
        return followType;
    }

    public void setFollowTime(Date followTime) 
    {
        this.followTime = followTime;
    }

    public Date getFollowTime() 
    {
        return followTime;
    }

    public void setFollowContent(String followContent) 
    {
        this.followContent = followContent;
    }

    public String getFollowContent() 
    {
        return followContent;
    }

    public void setNextFollowTime(Date nextFollowTime) 
    {
        this.nextFollowTime = nextFollowTime;
    }

    public Date getNextFollowTime() 
    {
        return nextFollowTime;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setTodoType(String todoType)
    {
        this.todoType = todoType;
    }

    public String getTodoType()
    {
        return todoType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("followId", getFollowId())
            .append("customerId", getCustomerId())
            .append("followType", getFollowType())
            .append("followTime", getFollowTime())
            .append("followContent", getFollowContent())
            .append("nextFollowTime", getNextFollowTime())
            .append("status", getStatus())
            .append("todoType", getTodoType())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
