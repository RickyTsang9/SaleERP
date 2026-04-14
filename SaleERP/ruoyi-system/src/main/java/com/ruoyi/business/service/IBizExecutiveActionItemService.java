package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.BizExecutiveActionItem;
import com.ruoyi.business.domain.BizExecutiveBriefRecord;

/**
 * 管理层经营决议事项Service接口
 * 
 * @author ruoyi
 * @date 2026-04-12
 */
public interface IBizExecutiveActionItemService
{
    /**
     * 查询经营决议事项
     * 
     * @param actionItemId 事项ID
     * @return 经营决议事项
     */
    public BizExecutiveActionItem selectBizExecutiveActionItemByActionItemId(Long actionItemId);

    /**
     * 查询经营决议事项列表
     * 
     * @param bizExecutiveActionItem 经营决议事项
     * @return 经营决议事项集合
     */
    public List<BizExecutiveActionItem> selectBizExecutiveActionItemList(BizExecutiveActionItem bizExecutiveActionItem);

    /**
     * 新增经营决议事项
     * 
     * @param bizExecutiveActionItem 经营决议事项
     * @param operatorName 操作人
     * @return 结果
     */
    public int insertBizExecutiveActionItem(BizExecutiveActionItem bizExecutiveActionItem, String operatorName);

    /**
     * 修改经营决议事项
     * 
     * @param bizExecutiveActionItem 经营决议事项
     * @param operatorName 操作人
     * @return 结果
     */
    public int updateBizExecutiveActionItem(BizExecutiveActionItem bizExecutiveActionItem, String operatorName);

    /**
     * 删除经营决议事项
     * 
     * @param actionItemIds 需要删除的事项ID
     * @return 结果
     */
    public int deleteBizExecutiveActionItemByActionItemIds(Long[] actionItemIds);

    /**
     * 根据归档简报自动生成经营决议事项
     * 
     * @param bizExecutiveBriefRecord 归档简报
     * @param operatorName 操作人
     */
    public void createBizExecutiveActionItemsFromBriefRecord(BizExecutiveBriefRecord bizExecutiveBriefRecord, String operatorName);
}
