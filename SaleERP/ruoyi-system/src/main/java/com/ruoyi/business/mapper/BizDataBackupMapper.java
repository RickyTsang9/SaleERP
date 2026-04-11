package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.BizDataBackup;

public interface BizDataBackupMapper
{
    public BizDataBackup selectBizDataBackupById(Long backupId);

    public List<BizDataBackup> selectBizDataBackupList(BizDataBackup bizDataBackup);

    public int insertBizDataBackup(BizDataBackup bizDataBackup);

    public int updateBizDataBackup(BizDataBackup bizDataBackup);

    public int deleteBizDataBackupByIds(Long[] backupIds);
}
