package com.ruoyi.business.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.BizOperationSnapshot;
import com.ruoyi.business.mapper.BizOperationSnapshotMapper;
import com.ruoyi.business.service.IBizOperationSnapshotService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * 管理层经营快照Service实现
 * 
 * @author ruoyi
 * @date 2026-04-12
 */
@Service
public class BizOperationSnapshotServiceImpl implements IBizOperationSnapshotService
{
    /** 管理驾驶舱三期初始化脚本 */
    private static final String MANAGEMENT_PHASE_THREE_SQL_FILE_NAME = "add_management_phase3_v3_20260412.sql";

    /** 经营快照表名称 */
    private static final String BIZ_OPERATION_SNAPSHOT_TABLE_NAME = "biz_operation_snapshot";

    /** 已确认存在经营快照表 */
    private volatile boolean operationSnapshotTableReady;

    @Autowired
    private BizOperationSnapshotMapper bizOperationSnapshotMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查询经营快照
     * 
     * @param snapshotId 快照ID
     * @return 经营快照
     */
    @Override
    public BizOperationSnapshot selectBizOperationSnapshotBySnapshotId(Long snapshotId)
    {
        if (!hasOperationSnapshotTable())
        {
            return null;
        }
        return bizOperationSnapshotMapper.selectBizOperationSnapshotBySnapshotId(snapshotId);
    }

    /**
     * 查询经营快照列表
     * 
     * @param bizOperationSnapshot 经营快照
     * @return 经营快照集合
     */
    @Override
    public List<BizOperationSnapshot> selectBizOperationSnapshotList(BizOperationSnapshot bizOperationSnapshot)
    {
        if (!hasOperationSnapshotTable())
        {
            return new ArrayList<BizOperationSnapshot>();
        }
        return bizOperationSnapshotMapper.selectBizOperationSnapshotList(bizOperationSnapshot);
    }

    /**
     * 新增经营快照
     * 
     * @param bizOperationSnapshot 经营快照
     * @param operatorName 操作人
     * @return 结果
     */
    @Override
    public int insertBizOperationSnapshot(BizOperationSnapshot bizOperationSnapshot, String operatorName)
    {
        validateOperationSnapshotTableReady();
        BizOperationSnapshot normalizedOperationSnapshot = normalizeBizOperationSnapshotForSave(bizOperationSnapshot);
        normalizedOperationSnapshot.setCreateBy(operatorName);
        normalizedOperationSnapshot.setUpdateBy(operatorName);
        return bizOperationSnapshotMapper.insertBizOperationSnapshot(normalizedOperationSnapshot);
    }

    /**
     * 校验经营快照表是否已经初始化
     */
    private void validateOperationSnapshotTableReady()
    {
        if (!hasOperationSnapshotTable())
        {
            throw new ServiceException("管理驾驶舱三期数据表未初始化，请先执行 " + MANAGEMENT_PHASE_THREE_SQL_FILE_NAME);
        }
    }

    /**
     * 判断经营快照表是否存在
     * 
     * @return 是否存在
     */
    private boolean hasOperationSnapshotTable()
    {
        if (operationSnapshotTableReady)
        {
            return true;
        }
        return refreshOperationSnapshotTableReady();
    }

    /**
     * 刷新经营快照表存在状态
     * 
     * @return 是否存在
     */
    private boolean refreshOperationSnapshotTableReady()
    {
        String countSql =
            "select count(1) from information_schema.tables where table_schema = (select database()) and table_name = ?";
        Integer tableCount = jdbcTemplate.queryForObject(countSql, Integer.class, BIZ_OPERATION_SNAPSHOT_TABLE_NAME);
        boolean tableExists = tableCount != null && tableCount.intValue() > 0;
        if (tableExists)
        {
            operationSnapshotTableReady = true;
        }
        return tableExists;
    }

    /**
     * 规范经营快照保存数据
     * 
     * @param bizOperationSnapshot 经营快照
     * @return 规范后的经营快照
     */
    private BizOperationSnapshot normalizeBizOperationSnapshotForSave(BizOperationSnapshot bizOperationSnapshot)
    {
        if (bizOperationSnapshot.getSnapshotDate() == null)
        {
            bizOperationSnapshot.setSnapshotDate(new Date());
        }
        if (StringUtils.isEmpty(bizOperationSnapshot.getSnapshotType()))
        {
            bizOperationSnapshot.setSnapshotType("weekly");
        }
        if (StringUtils.isEmpty(bizOperationSnapshot.getSnapshotTitle()))
        {
            bizOperationSnapshot.setSnapshotTitle("经营快照归档");
        }
        if (StringUtils.isEmpty(bizOperationSnapshot.getSourceMode()))
        {
            bizOperationSnapshot.setSourceMode("dashboard");
        }
        return bizOperationSnapshot;
    }
}
