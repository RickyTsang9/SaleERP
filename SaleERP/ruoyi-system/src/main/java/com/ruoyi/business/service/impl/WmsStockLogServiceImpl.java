package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.WmsStockLog;
import com.ruoyi.business.mapper.WmsStockLogMapper;
import com.ruoyi.business.service.IWmsStockLogService;
import com.ruoyi.common.exception.ServiceException;

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

    /**
     * 库存流水由系统自动生成，禁止手工新增。
     *
     * @param wmsStockLog 库存流水
     * @return 处理结果
     */
    @Override
    public int insertWmsStockLog(WmsStockLog wmsStockLog)
    {
        throw new ServiceException("库存流水由业务单据自动生成，不支持手工新增");
    }

    /**
     * 库存流水由系统自动生成，禁止手工修改。
     *
     * @param wmsStockLog 库存流水
     * @return 处理结果
     */
    @Override
    public int updateWmsStockLog(WmsStockLog wmsStockLog)
    {
        throw new ServiceException("库存流水由业务单据自动生成，不支持手工修改");
    }

    /**
     * 库存流水由系统自动生成，禁止手工删除。
     *
     * @param stockLogId 库存流水编号
     * @return 处理结果
     */
    @Override
    public int deleteWmsStockLogById(Long stockLogId)
    {
        throw new ServiceException("库存流水由业务单据自动生成，不支持手工删除");
    }

    /**
     * 库存流水由系统自动生成，禁止手工批量删除。
     *
     * @param stockLogIds 库存流水编号集合
     * @return 处理结果
     */
    @Override
    public int deleteWmsStockLogByIds(Long[] stockLogIds)
    {
        throw new ServiceException("库存流水由业务单据自动生成，不支持手工删除");
    }
}
