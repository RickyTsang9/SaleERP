package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.BizDataBackup;

public interface IBizDataBackupService
{
    public BizDataBackup selectBizDataBackupById(Long backupId);

    public List<BizDataBackup> selectBizDataBackupList(BizDataBackup bizDataBackup);

    public int createManualBackup(String operatorName);

    public int createAutoBackup();

    public int restoreByBackupId(Long backupId, String operatorName);

    public int deleteBizDataBackupByIds(Long[] backupIds);
}
