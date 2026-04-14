package com.ruoyi.business.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.BizExecutiveBriefRecord;
import com.ruoyi.business.mapper.BizExecutiveBriefRecordMapper;
import com.ruoyi.business.service.IBizExecutiveActionItemService;
import com.ruoyi.business.service.IBizExecutiveBriefRecordService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * 管理层经营简报归档Service实现
 * 
 * @author ruoyi
 * @date 2026-04-11
 */
@Service
public class BizExecutiveBriefRecordServiceImpl implements IBizExecutiveBriefRecordService
{
    /** 管理驾驶舱二期初始化脚本 */
    private static final String MANAGEMENT_PHASE_TWO_SQL_FILE_NAME = "add_management_phase2_20260411.sql";

    /** 经营简报归档表名称 */
    private static final String BIZ_EXECUTIVE_BRIEF_RECORD_TABLE_NAME = "biz_executive_brief_record";

    /** 已归档状态 */
    private static final String BRIEF_STATUS_ARCHIVED = "archived";

    /** 看板生成来源 */
    private static final String SOURCE_MODE_DASHBOARD = "dashboard";

    @Autowired
    private BizExecutiveBriefRecordMapper bizExecutiveBriefRecordMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private IBizExecutiveActionItemService bizExecutiveActionItemService;

    /**
     * 查询经营简报归档
     * 
     * @param briefId 简报ID
     * @return 经营简报归档
     */
    @Override
    public BizExecutiveBriefRecord selectBizExecutiveBriefRecordByBriefId(Long briefId)
    {
        // 二期归档表未初始化时返回空，避免管理看板打开历史简报时报错。
        if (!hasExecutiveBriefRecordTable())
        {
            return null;
        }
        BizExecutiveBriefRecord bizExecutiveBriefRecord = bizExecutiveBriefRecordMapper.selectBizExecutiveBriefRecordByBriefId(briefId);
        if (bizExecutiveBriefRecord == null)
        {
            return null;
        }
        return convertBizExecutiveBriefRecordForDisplay(bizExecutiveBriefRecord);
    }

    /**
     * 查询经营简报归档列表
     * 
     * @param bizExecutiveBriefRecord 经营简报归档
     * @return 经营简报归档集合
     */
    @Override
    public List<BizExecutiveBriefRecord> selectBizExecutiveBriefRecordList(BizExecutiveBriefRecord bizExecutiveBriefRecord)
    {
        // 归档表未初始化时直接返回空列表，保持看板兼容模式可正常展示。
        if (!hasExecutiveBriefRecordTable())
        {
            return new ArrayList<BizExecutiveBriefRecord>();
        }
        List<BizExecutiveBriefRecord> executiveBriefRecordList =
            bizExecutiveBriefRecordMapper.selectBizExecutiveBriefRecordList(bizExecutiveBriefRecord);
        for (BizExecutiveBriefRecord currentExecutiveBriefRecord : executiveBriefRecordList)
        {
            convertBizExecutiveBriefRecordForDisplay(currentExecutiveBriefRecord);
        }
        return executiveBriefRecordList;
    }

    /**
     * 新增经营简报归档
     * 
     * @param bizExecutiveBriefRecord 经营简报归档
     * @param operatorName 操作人
     * @return 结果
     */
    @Override
    public int insertBizExecutiveBriefRecord(BizExecutiveBriefRecord bizExecutiveBriefRecord, String operatorName)
    {
        validateExecutiveBriefRecordTableReady();
        BizExecutiveBriefRecord normalizedExecutiveBriefRecord = normalizeBizExecutiveBriefRecordForSave(bizExecutiveBriefRecord);
        normalizedExecutiveBriefRecord.setBriefStatus(BRIEF_STATUS_ARCHIVED);
        normalizedExecutiveBriefRecord.setSourceMode(StringUtils.isEmpty(normalizedExecutiveBriefRecord.getSourceMode())
            ? SOURCE_MODE_DASHBOARD
            : normalizedExecutiveBriefRecord.getSourceMode());
        normalizedExecutiveBriefRecord.setCreateBy(operatorName);
        normalizedExecutiveBriefRecord.setUpdateBy(operatorName);
        int insertRowCount = bizExecutiveBriefRecordMapper.insertBizExecutiveBriefRecord(normalizedExecutiveBriefRecord);
        if (insertRowCount > 0)
        {
            // 简报归档后同步生成经营决议事项，帮助管理层把建议动作沉淀为可跟踪的执行台账。
            bizExecutiveActionItemService.createBizExecutiveActionItemsFromBriefRecord(normalizedExecutiveBriefRecord, operatorName);
        }
        return insertRowCount;
    }

    /**
     * 校验经营简报归档表是否已经初始化
     */
    private void validateExecutiveBriefRecordTableReady()
    {
        if (!hasExecutiveBriefRecordTable())
        {
            throw new ServiceException("管理驾驶舱二期数据表未初始化，请先执行 " + MANAGEMENT_PHASE_TWO_SQL_FILE_NAME);
        }
    }

    /**
     * 判断经营简报归档表是否存在
     * @return 是否存在
     */
    private boolean hasExecutiveBriefRecordTable()
    {
        String countSql =
            "select count(1) from information_schema.tables where table_schema = (select database()) and table_name = ?";
        Integer tableCount = jdbcTemplate.queryForObject(countSql, Integer.class, BIZ_EXECUTIVE_BRIEF_RECORD_TABLE_NAME);
        return tableCount != null && tableCount.intValue() > 0;
    }

    /**
     * 规范经营简报归档保存数据
     * 
     * @param bizExecutiveBriefRecord 经营简报归档
     * @return 规范后的经营简报归档
     */
    private BizExecutiveBriefRecord normalizeBizExecutiveBriefRecordForSave(BizExecutiveBriefRecord bizExecutiveBriefRecord)
    {
        if (bizExecutiveBriefRecord.getBriefDate() == null)
        {
            bizExecutiveBriefRecord.setBriefDate(new Date());
        }
        if (bizExecutiveBriefRecord.getGeneratedTime() == null)
        {
            bizExecutiveBriefRecord.setGeneratedTime(new Date());
        }
        if (StringUtils.isEmpty(bizExecutiveBriefRecord.getBriefTitle()))
        {
            bizExecutiveBriefRecord.setBriefTitle("经营简报归档");
        }
        bizExecutiveBriefRecord.setHighlightContent(writeTextListAsJson(bizExecutiveBriefRecord.getHighlightTextList()));
        bizExecutiveBriefRecord.setRiskContent(writeTextListAsJson(bizExecutiveBriefRecord.getRiskTextList()));
        bizExecutiveBriefRecord.setActionContent(writeTextListAsJson(bizExecutiveBriefRecord.getActionTextList()));
        return bizExecutiveBriefRecord;
    }

    /**
     * 转换经营简报归档展示数据
     * 
     * @param bizExecutiveBriefRecord 经营简报归档
     * @return 展示用经营简报归档
     */
    private BizExecutiveBriefRecord convertBizExecutiveBriefRecordForDisplay(BizExecutiveBriefRecord bizExecutiveBriefRecord)
    {
        bizExecutiveBriefRecord.setHighlightTextList(readTextListFromJson(bizExecutiveBriefRecord.getHighlightContent()));
        bizExecutiveBriefRecord.setRiskTextList(readTextListFromJson(bizExecutiveBriefRecord.getRiskContent()));
        bizExecutiveBriefRecord.setActionTextList(readTextListFromJson(bizExecutiveBriefRecord.getActionContent()));
        return bizExecutiveBriefRecord;
    }

    /**
     * 序列化文本列表
     * 
     * @param textList 文本列表
     * @return 文本JSON
     */
    private String writeTextListAsJson(List<String> textList)
    {
        try
        {
            List<String> normalizedTextList = textList == null ? new ArrayList<String>() : textList;
            return objectMapper.writeValueAsString(normalizedTextList);
        }
        catch (Exception exception)
        {
            throw new ServiceException("保存经营简报内容失败：" + exception.getMessage());
        }
    }

    /**
     * 解析文本列表
     * 
     * @param textJson 文本JSON
     * @return 文本列表
     */
    private List<String> readTextListFromJson(String textJson)
    {
        if (StringUtils.isEmpty(textJson))
        {
            return new ArrayList<String>();
        }
        try
        {
            return objectMapper.readValue(textJson, new TypeReference<List<String>>()
            {
            });
        }
        catch (Exception exception)
        {
            throw new ServiceException("解析经营简报内容失败：" + exception.getMessage());
        }
    }
}
