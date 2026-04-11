package com.ruoyi.business.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 管理层经营简报归档对象 biz_executive_brief_record
 * 
 * @author ruoyi
 * @date 2026-04-11
 */
public class BizExecutiveBriefRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 简报ID */
    private Long briefId;

    /** 简报标题 */
    private String briefTitle;

    /** 简报日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date briefDate;

    /** 简报状态 */
    private String briefStatus;

    /** 生成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date generatedTime;

    /** 经营结论 */
    private String summaryContent;

    /** 亮点内容原始JSON */
    @JsonIgnore
    private String highlightContent;

    /** 风险内容原始JSON */
    @JsonIgnore
    private String riskContent;

    /** 建议决策原始JSON */
    @JsonIgnore
    private String actionContent;

    /** 简报纯文本 */
    private String plainTextContent;

    /** 生成来源 */
    private String sourceMode;

    /** 亮点列表 */
    private List<String> highlightTextList;

    /** 风险列表 */
    private List<String> riskTextList;

    /** 建议决策列表 */
    private List<String> actionTextList;

    public Long getBriefId()
    {
        return briefId;
    }

    public void setBriefId(Long briefId)
    {
        this.briefId = briefId;
    }

    public String getBriefTitle()
    {
        return briefTitle;
    }

    public void setBriefTitle(String briefTitle)
    {
        this.briefTitle = briefTitle;
    }

    public Date getBriefDate()
    {
        return briefDate;
    }

    public void setBriefDate(Date briefDate)
    {
        this.briefDate = briefDate;
    }

    public String getBriefStatus()
    {
        return briefStatus;
    }

    public void setBriefStatus(String briefStatus)
    {
        this.briefStatus = briefStatus;
    }

    public Date getGeneratedTime()
    {
        return generatedTime;
    }

    public void setGeneratedTime(Date generatedTime)
    {
        this.generatedTime = generatedTime;
    }

    public String getSummaryContent()
    {
        return summaryContent;
    }

    public void setSummaryContent(String summaryContent)
    {
        this.summaryContent = summaryContent;
    }

    public String getHighlightContent()
    {
        return highlightContent;
    }

    public void setHighlightContent(String highlightContent)
    {
        this.highlightContent = highlightContent;
    }

    public String getRiskContent()
    {
        return riskContent;
    }

    public void setRiskContent(String riskContent)
    {
        this.riskContent = riskContent;
    }

    public String getActionContent()
    {
        return actionContent;
    }

    public void setActionContent(String actionContent)
    {
        this.actionContent = actionContent;
    }

    public String getPlainTextContent()
    {
        return plainTextContent;
    }

    public void setPlainTextContent(String plainTextContent)
    {
        this.plainTextContent = plainTextContent;
    }

    public String getSourceMode()
    {
        return sourceMode;
    }

    public void setSourceMode(String sourceMode)
    {
        this.sourceMode = sourceMode;
    }

    public List<String> getHighlightTextList()
    {
        return highlightTextList;
    }

    public void setHighlightTextList(List<String> highlightTextList)
    {
        this.highlightTextList = highlightTextList;
    }

    public List<String> getRiskTextList()
    {
        return riskTextList;
    }

    public void setRiskTextList(List<String> riskTextList)
    {
        this.riskTextList = riskTextList;
    }

    public List<String> getActionTextList()
    {
        return actionTextList;
    }

    public void setActionTextList(List<String> actionTextList)
    {
        this.actionTextList = actionTextList;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("briefId", getBriefId())
            .append("briefTitle", getBriefTitle())
            .append("briefDate", getBriefDate())
            .append("briefStatus", getBriefStatus())
            .append("generatedTime", getGeneratedTime())
            .append("summaryContent", getSummaryContent())
            .append("highlightTextList", getHighlightTextList())
            .append("riskTextList", getRiskTextList())
            .append("actionTextList", getActionTextList())
            .append("plainTextContent", getPlainTextContent())
            .append("sourceMode", getSourceMode())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
