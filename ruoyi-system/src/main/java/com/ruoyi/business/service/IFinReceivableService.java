package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.FinReceivable;

public interface IFinReceivableService
{
    public FinReceivable selectFinReceivableById(Long receivableId);

    public FinReceivable selectFinReceivableBySaleOrderId(Long saleOrderId);

    public List<FinReceivable> selectFinReceivableList(FinReceivable finReceivable);

    public List<FinReceivable> selectFinReceivableDueReminderList(Integer remindDays);

    public List<FinReceivable> selectFinReceivableOverdueList();

    public int insertFinReceivable(FinReceivable finReceivable);

    public int updateFinReceivable(FinReceivable finReceivable);

    public int deleteFinReceivableById(Long receivableId);

    public int deleteFinReceivableByIds(Long[] receivableIds);
}
