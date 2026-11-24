package com.ruoyi.quartz.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.business.service.ICrmCustomerFollowService;

@Component("crmCustomerFollowRemindTask")
public class CrmCustomerFollowRemindTask
{
    @Autowired
    private ICrmCustomerFollowService crmCustomerFollowService;

    public void pushDueFollowReminder()
    {
        crmCustomerFollowService.pushDueFollowReminder("system");
    }
}
