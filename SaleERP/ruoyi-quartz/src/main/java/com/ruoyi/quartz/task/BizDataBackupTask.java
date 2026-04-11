package com.ruoyi.quartz.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.business.service.IBizDataBackupService;

@Component("bizDataBackupTask")
public class BizDataBackupTask
{
    @Autowired
    private IBizDataBackupService bizDataBackupService;

    public void autoBackup()
    {
        bizDataBackupService.createAutoBackup();
    }
}
