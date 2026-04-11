package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.WmsSaleOrderStatusHistory;

public interface WmsSaleOrderStatusHistoryMapper
{
    public int insertWmsSaleOrderStatusHistory(WmsSaleOrderStatusHistory wmsSaleOrderStatusHistory);

    public List<WmsSaleOrderStatusHistory> selectWmsSaleOrderStatusHistoryList(Long saleOrderId);
}
