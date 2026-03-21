package com.ruoyi.business.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.WmsInbound;
import com.ruoyi.business.mapper.WmsInboundMapper;
import com.ruoyi.business.mapper.WmsInboundItemMapper;
import com.ruoyi.business.service.IWmsInboundService;

@Service
public class WmsInboundServiceImpl implements IWmsInboundService
{
    @Autowired
    private WmsInboundMapper wmsInboundMapper;

    @Autowired
    private WmsInboundItemMapper wmsInboundItemMapper;

    @Override
    public WmsInbound selectWmsInboundById(Long inboundId)
    {
        return wmsInboundMapper.selectWmsInboundById(inboundId);
    }

    @Override
    public List<WmsInbound> selectWmsInboundList(WmsInbound wmsInbound)
    {
        return wmsInboundMapper.selectWmsInboundList(wmsInbound);
    }

    @Override
    public int insertWmsInbound(WmsInbound wmsInbound)
    {
        if (wmsInbound.getInboundNo() == null || wmsInbound.getInboundNo().isEmpty())
        {
            wmsInbound.setInboundNo(generateInboundNo());
        }
        return wmsInboundMapper.insertWmsInbound(wmsInbound);
    }

    @Override
    public int updateWmsInbound(WmsInbound wmsInbound)
    {
        return wmsInboundMapper.updateWmsInbound(wmsInbound);
    }

    @Override
    public int deleteWmsInboundById(Long inboundId)
    {
        wmsInboundItemMapper.deleteWmsInboundItemByInboundIds(new Long[] { inboundId });
        return wmsInboundMapper.deleteWmsInboundById(inboundId);
    }

    @Override
    public int deleteWmsInboundByIds(Long[] inboundIds)
    {
        wmsInboundItemMapper.deleteWmsInboundItemByInboundIds(inboundIds);
        return wmsInboundMapper.deleteWmsInboundByIds(inboundIds);
    }

    private String generateInboundNo()
    {
        String dateValue = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String noPrefix = "RK" + dateValue;
        String maxInboundNo = wmsInboundMapper.selectMaxInboundNoByPrefix(noPrefix);
        if (maxInboundNo == null || maxInboundNo.isEmpty())
        {
            return noPrefix + "0001";
        }
        String sequenceValue = maxInboundNo.substring(noPrefix.length());
        int nextSequenceValue = Integer.parseInt(sequenceValue) + 1;
        return noPrefix + String.format("%04d", nextSequenceValue);
    }
}
