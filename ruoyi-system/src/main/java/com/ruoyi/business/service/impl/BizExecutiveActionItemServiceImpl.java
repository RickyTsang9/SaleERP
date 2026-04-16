package com.ruoyi.business.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.BizExecutiveActionItem;
import com.ruoyi.business.domain.BizExecutiveBriefRecord;
import com.ruoyi.business.mapper.BizExecutiveActionItemMapper;
import com.ruoyi.business.service.IBizExecutiveActionItemService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 管理层经营决议事项Service实现
 * 
 * @author ruoyi
 * @date 2026-04-12
 */
@Service
public class BizExecutiveActionItemServiceImpl implements IBizExecutiveActionItemService
{
    /** 管理驾驶舱执行事项初始化脚本 */
    private static final String MANAGEMENT_ACTION_SQL_FILE_NAME = "add_management_phase2_action_20260412.sql";

    /** 经营决议事项表名称 */
    private static final String BIZ_EXECUTIVE_ACTION_ITEM_TABLE_NAME = "biz_executive_action_item";

    /** 已确认存在经营决议事项表 */
    private volatile boolean executiveActionItemTableReady;

    /** 待跟进状态 */
    private static final String ACTION_STATUS_TODO = "todo";

    /** 执行中状态 */
    private static final String ACTION_STATUS_IN_PROGRESS = "in_progress";

    /** 已完成状态 */
    private static final String ACTION_STATUS_COMPLETED = "completed";

    /** 默认优先级 */
    private static final String PRIORITY_LEVEL_MEDIUM = "medium";

    @Autowired
    private BizExecutiveActionItemMapper bizExecutiveActionItemMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查询经营决议事项
     * 
     * @param actionItemId 事项ID
     * @return 经营决议事项
     */
    @Override
    public BizExecutiveActionItem selectBizExecutiveActionItemByActionItemId(Long actionItemId)
    {
        if (!hasExecutiveActionItemTable())
        {
            return null;
        }
        return bizExecutiveActionItemMapper.selectBizExecutiveActionItemByActionItemId(actionItemId);
    }

    /**
     * 查询经营决议事项列表
     * 
     * @param bizExecutiveActionItem 经营决议事项
     * @return 经营决议事项集合
     */
    @Override
    public List<BizExecutiveActionItem> selectBizExecutiveActionItemList(BizExecutiveActionItem bizExecutiveActionItem)
    {
        if (!hasExecutiveActionItemTable())
        {
            return new ArrayList<BizExecutiveActionItem>();
        }
        return bizExecutiveActionItemMapper.selectBizExecutiveActionItemList(bizExecutiveActionItem);
    }

    /**
     * 新增经营决议事项
     * 
     * @param bizExecutiveActionItem 经营决议事项
     * @param operatorName 操作人
     * @return 结果
     */
    @Override
    public int insertBizExecutiveActionItem(BizExecutiveActionItem bizExecutiveActionItem, String operatorName)
    {
        validateExecutiveActionItemTableReady();
        BizExecutiveActionItem normalizedExecutiveActionItem =
            normalizeExecutiveActionItemForSave(bizExecutiveActionItem, operatorName, false);
        return bizExecutiveActionItemMapper.insertBizExecutiveActionItem(normalizedExecutiveActionItem);
    }

    /**
     * 修改经营决议事项
     * 
     * @param bizExecutiveActionItem 经营决议事项
     * @param operatorName 操作人
     * @return 结果
     */
    @Override
    public int updateBizExecutiveActionItem(BizExecutiveActionItem bizExecutiveActionItem, String operatorName)
    {
        validateExecutiveActionItemTableReady();
        BizExecutiveActionItem normalizedExecutiveActionItem =
            normalizeExecutiveActionItemForSave(bizExecutiveActionItem, operatorName, true);
        return bizExecutiveActionItemMapper.updateBizExecutiveActionItem(normalizedExecutiveActionItem);
    }

    /**
     * 删除经营决议事项
     * 
     * @param actionItemIds 需要删除的事项ID
     * @return 结果
     */
    @Override
    public int deleteBizExecutiveActionItemByActionItemIds(Long[] actionItemIds)
    {
        validateExecutiveActionItemTableReady();
        return bizExecutiveActionItemMapper.deleteBizExecutiveActionItemByActionItemIds(actionItemIds);
    }

    /**
     * 根据归档简报自动生成经营决议事项
     * 
     * @param bizExecutiveBriefRecord 归档简报
     * @param operatorName 操作人
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createBizExecutiveActionItemsFromBriefRecord(BizExecutiveBriefRecord bizExecutiveBriefRecord, String operatorName)
    {
        if (!hasExecutiveActionItemTable() || bizExecutiveBriefRecord == null)
        {
            return;
        }
        List<String> actionTextList = bizExecutiveBriefRecord.getActionTextList();
        if (actionTextList == null || actionTextList.isEmpty())
        {
            return;
        }
        for (String actionText : actionTextList)
        {
            if (StringUtils.isEmpty(actionText))
            {
                continue;
            }
            BizExecutiveActionItem bizExecutiveActionItem = new BizExecutiveActionItem();
            bizExecutiveActionItem.setBriefId(bizExecutiveBriefRecord.getBriefId());
            bizExecutiveActionItem.setBriefTitleSnapshot(bizExecutiveBriefRecord.getBriefTitle());
            bizExecutiveActionItem.setActionTitle(actionText);
            bizExecutiveActionItem.setDueDate(DateUtils.addDays(getBaseDueDate(bizExecutiveBriefRecord), 7));
            bizExecutiveActionItem.setActionStatus(ACTION_STATUS_TODO);
            bizExecutiveActionItem.setPriorityLevel(PRIORITY_LEVEL_MEDIUM);
            bizExecutiveActionItem.setRemark("由经营简报自动生成");
            BizExecutiveActionItem normalizedExecutiveActionItem =
                normalizeExecutiveActionItemForSave(bizExecutiveActionItem, operatorName, false);
            bizExecutiveActionItemMapper.insertBizExecutiveActionItem(normalizedExecutiveActionItem);
        }
    }

    /**
     * 获取默认到期基准日期
     * 
     * @param bizExecutiveBriefRecord 归档简报
     * @return 基准日期
     */
    private Date getBaseDueDate(BizExecutiveBriefRecord bizExecutiveBriefRecord)
    {
        if (bizExecutiveBriefRecord.getBriefDate() != null)
        {
            return bizExecutiveBriefRecord.getBriefDate();
        }
        return DateUtils.getNowDate();
    }

    /**
     * 校验经营决议事项表是否已经初始化
     */
    private void validateExecutiveActionItemTableReady()
    {
        if (!hasExecutiveActionItemTable())
        {
            throw new ServiceException("经营决议事项数据表未初始化，请先执行 " + MANAGEMENT_ACTION_SQL_FILE_NAME);
        }
    }

    /**
     * 判断经营决议事项表是否存在
     * 
     * @return 是否存在
     */
    private boolean hasExecutiveActionItemTable()
    {
        if (executiveActionItemTableReady)
        {
            return true;
        }
        return refreshExecutiveActionItemTableReady();
    }

    /**
     * 刷新经营决议事项表存在状态
     * 
     * @return 是否存在
     */
    private boolean refreshExecutiveActionItemTableReady()
    {
        String countSql =
            "select count(1) from information_schema.tables where table_schema = (select database()) and table_name = ?";
        Integer tableCount = jdbcTemplate.queryForObject(countSql, Integer.class, BIZ_EXECUTIVE_ACTION_ITEM_TABLE_NAME);
        boolean tableExists = tableCount != null && tableCount.intValue() > 0;
        if (tableExists)
        {
            executiveActionItemTableReady = true;
        }
        return tableExists;
    }

    /**
     * 规范经营决议事项保存数据
     * 
     * @param bizExecutiveActionItem 经营决议事项
     * @param operatorName 操作人
     * @param isUpdateOperation 是否更新操作
     * @return 规范后的经营决议事项
     */
    private BizExecutiveActionItem normalizeExecutiveActionItemForSave(BizExecutiveActionItem bizExecutiveActionItem,
        String operatorName, boolean isUpdateOperation)
    {
        if (StringUtils.isEmpty(bizExecutiveActionItem.getActionTitle()))
        {
            throw new ServiceException("经营决议事项内容不能为空");
        }
        if (StringUtils.isEmpty(bizExecutiveActionItem.getActionStatus()))
        {
            bizExecutiveActionItem.setActionStatus(ACTION_STATUS_TODO);
        }
        if (StringUtils.isEmpty(bizExecutiveActionItem.getPriorityLevel()))
        {
            bizExecutiveActionItem.setPriorityLevel(PRIORITY_LEVEL_MEDIUM);
        }
        if (ACTION_STATUS_COMPLETED.equals(bizExecutiveActionItem.getActionStatus()) && bizExecutiveActionItem.getCompletedTime() == null)
        {
            bizExecutiveActionItem.setCompletedTime(DateUtils.getNowDate());
        }
        if (!ACTION_STATUS_COMPLETED.equals(bizExecutiveActionItem.getActionStatus()))
        {
            bizExecutiveActionItem.setCompletedTime(null);
        }
        if (!isUpdateOperation)
        {
            bizExecutiveActionItem.setCreateBy(operatorName);
        }
        bizExecutiveActionItem.setUpdateBy(operatorName);
        if (ACTION_STATUS_TODO.equals(bizExecutiveActionItem.getActionStatus())
            && StringUtils.isEmpty(bizExecutiveActionItem.getOwnerName())
            && StringUtils.isNotEmpty(operatorName))
        {
            // 自动生成的事项先默认挂到归档操作者，便于后续分派和跟踪。
            bizExecutiveActionItem.setOwnerName(operatorName);
        }
        if (ACTION_STATUS_IN_PROGRESS.equals(bizExecutiveActionItem.getActionStatus())
            && StringUtils.isEmpty(bizExecutiveActionItem.getProgressRemark()))
        {
            bizExecutiveActionItem.setProgressRemark("已进入执行中，请补充最新进展。");
        }
        return bizExecutiveActionItem;
    }
}
