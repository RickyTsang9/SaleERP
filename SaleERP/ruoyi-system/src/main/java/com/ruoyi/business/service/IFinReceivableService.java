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

    /**
     * 同一客户正向待收单与红字负额待退单内部对冲核销（不产生银行现金流水）
     */
    public java.math.BigDecimal offsetReceivable(Long positiveReceivableId, Long negativeReceivableId, String remark, String operator);
}
