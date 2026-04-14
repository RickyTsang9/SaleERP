package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizOperationSnapshot;

/**
 * 管理层经营快照Mapper接口
 * 
 * @author ruoyi
 * @date 2026-04-12
 */
public interface BizOperationSnapshotMapper
{
    /**
     * 查询经营快照
     * 
     * @param snapshotId 快照ID
     * @return 经营快照
     */
    public BizOperationSnapshot selectBizOperationSnapshotBySnapshotId(Long snapshotId);

    /**
     * 查询经营快照列表
     * 
     * @param bizOperationSnapshot 经营快照
     * @return 经营快照集合
     */
    public List<BizOperationSnapshot> selectBizOperationSnapshotList(BizOperationSnapshot bizOperationSnapshot);

    /**
     * 新增经营快照
     * 
     * @param bizOperationSnapshot 经营快照
     * @return 结果
     */
    public int insertBizOperationSnapshot(BizOperationSnapshot bizOperationSnapshot);
}
