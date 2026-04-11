package com.ruoyi.business.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

public class BizDataBackup extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long backupId;

    private String backupName;

    private String backupType;

    private String backupStatus;

    private Integer tableCount;

    private Long recordCount;

    private String backupContent;

    private String restoreStatus;

    private String restoreBy;

    private Date restoreTime;

    public Long getBackupId()
    {
        return backupId;
    }

    public void setBackupId(Long backupId)
    {
        this.backupId = backupId;
    }

    public String getBackupName()
    {
        return backupName;
    }

    public void setBackupName(String backupName)
    {
        this.backupName = backupName;
    }

    public String getBackupType()
    {
        return backupType;
    }

    public void setBackupType(String backupType)
    {
        this.backupType = backupType;
    }

    public String getBackupStatus()
    {
        return backupStatus;
    }

    public void setBackupStatus(String backupStatus)
    {
        this.backupStatus = backupStatus;
    }

    public Integer getTableCount()
    {
        return tableCount;
    }

    public void setTableCount(Integer tableCount)
    {
        this.tableCount = tableCount;
    }

    public Long getRecordCount()
    {
        return recordCount;
    }

    public void setRecordCount(Long recordCount)
    {
        this.recordCount = recordCount;
    }

    public String getBackupContent()
    {
        return backupContent;
    }

    public void setBackupContent(String backupContent)
    {
        this.backupContent = backupContent;
    }

    public String getRestoreStatus()
    {
        return restoreStatus;
    }

    public void setRestoreStatus(String restoreStatus)
    {
        this.restoreStatus = restoreStatus;
    }

    public String getRestoreBy()
    {
        return restoreBy;
    }

    public void setRestoreBy(String restoreBy)
    {
        this.restoreBy = restoreBy;
    }

    public Date getRestoreTime()
    {
        return restoreTime;
    }

    public void setRestoreTime(Date restoreTime)
    {
        this.restoreTime = restoreTime;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("backupId", getBackupId())
            .append("backupName", getBackupName())
            .append("backupType", getBackupType())
            .append("backupStatus", getBackupStatus())
            .append("tableCount", getTableCount())
            .append("recordCount", getRecordCount())
            .append("backupContent", getBackupContent())
            .append("restoreStatus", getRestoreStatus())
            .append("restoreBy", getRestoreBy())
            .append("restoreTime", getRestoreTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
