package com.ruoyi.business.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 管理层经营决议事项对象 biz_executive_action_item
 * 
 * @author ruoyi
 * @date 2026-04-12
 */
public class BizExecutiveActionItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 事项ID */
    private Long actionItemId;

    /** 简报ID */
    private Long briefId;

    /** 简报标题快照 */
    private String briefTitleSnapshot;

    /** 决议事项 */
    private String actionTitle;

    /** 负责人 */
    private String ownerName;

    /** 到期日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dueDate;

    /** 状态（todo待跟进 in_progress执行中 completed已完成 canceled已取消） */
    private String actionStatus;

    /** 优先级（high高 medium中 low低） */
    private String priorityLevel;

    /** 进度备注 */
    private String progressRemark;

    /** 完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date completedTime;

    public Long getActionItemId()
    {
        return actionItemId;
    }

    public void setActionItemId(Long actionItemId)
    {
        this.actionItemId = actionItemId;
    }

    public Long getBriefId()
    {
        return briefId;
    }

    public void setBriefId(Long briefId)
    {
        this.briefId = briefId;
    }

    public String getBriefTitleSnapshot()
    {
        return briefTitleSnapshot;
    }

    public void setBriefTitleSnapshot(String briefTitleSnapshot)
    {
        this.briefTitleSnapshot = briefTitleSnapshot;
    }

    public String getActionTitle()
    {
        return actionTitle;
    }

    public void setActionTitle(String actionTitle)
    {
        this.actionTitle = actionTitle;
    }

    public String getOwnerName()
    {
        return ownerName;
    }

    public void setOwnerName(String ownerName)
    {
        this.ownerName = ownerName;
    }

    public Date getDueDate()
    {
        return dueDate;
    }

    public void setDueDate(Date dueDate)
    {
        this.dueDate = dueDate;
    }

    public String getActionStatus()
    {
        return actionStatus;
    }

    public void setActionStatus(String actionStatus)
    {
        this.actionStatus = actionStatus;
    }

    public String getPriorityLevel()
    {
        return priorityLevel;
    }

    public void setPriorityLevel(String priorityLevel)
    {
        this.priorityLevel = priorityLevel;
    }

    public String getProgressRemark()
    {
        return progressRemark;
    }

    public void setProgressRemark(String progressRemark)
    {
        this.progressRemark = progressRemark;
    }

    public Date getCompletedTime()
    {
        return completedTime;
    }

    public void setCompletedTime(Date completedTime)
    {
        this.completedTime = completedTime;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("actionItemId", getActionItemId())
            .append("briefId", getBriefId())
            .append("briefTitleSnapshot", getBriefTitleSnapshot())
            .append("actionTitle", getActionTitle())
            .append("ownerName", getOwnerName())
            .append("dueDate", getDueDate())
            .append("actionStatus", getActionStatus())
            .append("priorityLevel", getPriorityLevel())
            .append("progressRemark", getProgressRemark())
            .append("completedTime", getCompletedTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
