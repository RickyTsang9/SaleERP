package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.BizExecutiveBriefRecord;

/**
 * 管理层经营简报归档Service接口
 * 
 * @author ruoyi
 * @date 2026-04-11
 */
public interface IBizExecutiveBriefRecordService
{
    /**
     * 查询经营简报归档
     * 
     * @param briefId 简报ID
     * @return 经营简报归档
     */
    public BizExecutiveBriefRecord selectBizExecutiveBriefRecordByBriefId(Long briefId);

    /**
     * 查询经营简报归档列表
     * 
     * @param bizExecutiveBriefRecord 经营简报归档
     * @return 经营简报归档集合
     */
    public List<BizExecutiveBriefRecord> selectBizExecutiveBriefRecordList(BizExecutiveBriefRecord bizExecutiveBriefRecord);

    /**
     * 新增经营简报归档
     * 
     * @param bizExecutiveBriefRecord 经营简报归档
     * @param operatorName 操作人
     * @return 结果
     */
    public int insertBizExecutiveBriefRecord(BizExecutiveBriefRecord bizExecutiveBriefRecord, String operatorName);
}
