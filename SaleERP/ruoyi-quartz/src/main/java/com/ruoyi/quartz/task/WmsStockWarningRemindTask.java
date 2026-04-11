package com.ruoyi.quartz.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.business.service.IWmsStockService;

@Component("wmsStockWarningRemindTask")
public class WmsStockWarningRemindTask
{
    @Autowired
    private IWmsStockService wmsStockService;

    public void pushWarningReminder()
    {
        wmsStockService.pushWarningReminder("system");
    }
}
