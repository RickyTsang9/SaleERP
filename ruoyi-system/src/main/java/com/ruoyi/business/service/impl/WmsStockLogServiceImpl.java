package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.WmsStockLog;
import com.ruoyi.business.mapper.WmsStockLogMapper;
import com.ruoyi.business.service.IWmsStockLogService;

@Service
public class WmsStockLogServiceImpl implements IWmsStockLogService
{
    @Autowired
    private WmsStockLogMapper wmsStockLogMapper;

    @Override
    public WmsStockLog selectWmsStockLogById(Long stockLogId)
    {
        return wmsStockLogMapper.selectWmsStockLogById(stockLogId);
    }

    @Override
    public List<WmsStockLog> selectWmsStockLogList(WmsStockLog wmsStockLog)
    {
        return wmsStockLogMapper.selectWmsStockLogList(wmsStockLog);
    }

    @Override
    public int insertWmsStockLog(WmsStockLog wmsStockLog)
    {
        return wmsStockLogMapper.insertWmsStockLog(wmsStockLog);
    }

    @Override
    public int updateWmsStockLog(WmsStockLog wmsStockLog)
    {
        return wmsStockLogMapper.updateWmsStockLog(wmsStockLog);
    }

    @Override
    public int deleteWmsStockLogById(Long stockLogId)
    {
        return wmsStockLogMapper.deleteWmsStockLogById(stockLogId);
    }

    @Override
    public int deleteWmsStockLogByIds(Long[] stockLogIds)
    {
        return wmsStockLogMapper.deleteWmsStockLogByIds(stockLogIds);
    }
}
