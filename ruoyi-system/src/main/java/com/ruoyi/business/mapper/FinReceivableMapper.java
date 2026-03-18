package com.ruoyi.business.mapper;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.business.domain.FinReceivable;

public interface FinReceivableMapper
{
    public FinReceivable selectFinReceivableById(Long receivableId);

    public FinReceivable selectFinReceivableBySaleOrderId(Long saleOrderId);

    public List<FinReceivable> selectFinReceivableList(FinReceivable finReceivable);

    public List<FinReceivable> selectFinReceivableDueReminderList(Integer remindDays);

    public List<FinReceivable> selectFinReceivableOverdueList();

    public BigDecimal selectCustomerOutstandingAmount(Long customerId);

    public int insertFinReceivable(FinReceivable finReceivable);

    public int updateFinReceivable(FinReceivable finReceivable);

    public int updateFinReceivablePaidAmount(Long receivableId, BigDecimal amountPaid, String status);

    public int updateFinReceivableAmounts(Long receivableId, BigDecimal amountDue, BigDecimal amountPaid, String status);

    public int deleteFinReceivableById(Long receivableId);

    public int deleteFinReceivableByIds(Long[] receivableIds);
}
