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

    /**
     * 手工新增应收台账
     */
    public int insertFinReceivable(FinReceivable finReceivable);

    /**
     * 调整应收台账
     */
    public int updateFinReceivable(FinReceivable finReceivable);

    /**
     * 删除单条应收台账
     */
    public int deleteFinReceivableById(Long receivableId);

    /**
     * 批量删除应收台账
     */
    public int deleteFinReceivableByIds(Long[] receivableIds);
}
