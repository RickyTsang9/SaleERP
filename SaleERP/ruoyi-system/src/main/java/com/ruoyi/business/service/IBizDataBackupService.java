package com.ruoyi.business.service;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import com.ruoyi.business.domain.BizDataBackup;

public interface IBizDataBackupService
{
    public BizDataBackup selectBizDataBackupById(Long backupId);

    public List<BizDataBackup> selectBizDataBackupList(BizDataBackup bizDataBackup);

    public int createManualBackup(String operatorName);

    public int createAutoBackup();

    public int restoreByBackupId(Long backupId, String operatorName);

    public int restoreFromUploadedFile(InputStream inputStream, String fileName, String operatorName);

    public File resolveBackupDownloadFile(Long backupId);

    public int deleteBizDataBackupByIds(Long[] backupIds);
}
