package com.ruoyi.quartz.task;

import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.business.domain.FinReceivable;
import com.ruoyi.business.service.IFinReceivableService;
import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.service.ISysNoticeService;

@Component("finReceivableRemindTask")
public class FinReceivableRemindTask
{
    @Autowired
    private IFinReceivableService finReceivableService;

    @Autowired
    private ISysNoticeService sysNoticeService;

    public void pushDueReminder(String remindDaysParameter)
    {
        Integer remindDays = 7;
        if (remindDaysParameter != null && remindDaysParameter.trim().length() > 0)
        {
            remindDays = Integer.parseInt(remindDaysParameter.trim());
        }
        List<FinReceivable> dueReminderReceivableList = finReceivableService.selectFinReceivableDueReminderList(remindDays);
        List<FinReceivable> overdueReceivableList = finReceivableService.selectFinReceivableOverdueList();
        if (dueReminderReceivableList.isEmpty() && overdueReceivableList.isEmpty())
        {
            return;
        }
        String noticeTitle = "应收到期提醒-" + LocalDate.now();
        SysNotice queryNotice = new SysNotice();
        queryNotice.setNoticeTitle(noticeTitle);
        List<SysNotice> existingNoticeList = sysNoticeService.selectNoticeList(queryNotice);
        for (SysNotice existingNotice : existingNoticeList)
        {
            if (noticeTitle.equals(existingNotice.getNoticeTitle()))
            {
                return;
            }
        }
        SysNotice notice = new SysNotice();
        notice.setNoticeTitle(noticeTitle);
        notice.setNoticeType("1");
        notice.setStatus("0");
        notice.setCreateBy("system");
        notice.setNoticeContent("未来" + remindDays + "天到期应收" + dueReminderReceivableList.size() + "条，已逾期应收" + overdueReceivableList.size() + "条。");
        sysNoticeService.insertNotice(notice);
    }
}
