package com.ruoyi.business.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.WmsOutbound;
import com.ruoyi.business.mapper.WmsOutboundMapper;
import com.ruoyi.business.service.IWmsOutboundService;

@Service
public class WmsOutboundServiceImpl implements IWmsOutboundService
{
    @Autowired
    private WmsOutboundMapper wmsOutboundMapper;

    @Override
    public WmsOutbound selectWmsOutboundById(Long outboundId)
    {
        return wmsOutboundMapper.selectWmsOutboundById(outboundId);
    }

    @Override
    public List<WmsOutbound> selectWmsOutboundList(WmsOutbound wmsOutbound)
    {
        return wmsOutboundMapper.selectWmsOutboundList(wmsOutbound);
    }

    @Override
    public int insertWmsOutbound(WmsOutbound wmsOutbound)
    {
        if (wmsOutbound.getOutboundNo() == null || wmsOutbound.getOutboundNo().isEmpty())
        {
            wmsOutbound.setOutboundNo(generateOutboundNo());
        }
        return wmsOutboundMapper.insertWmsOutbound(wmsOutbound);
    }

    @Override
    public int updateWmsOutbound(WmsOutbound wmsOutbound)
    {
        return wmsOutboundMapper.updateWmsOutbound(wmsOutbound);
    }

    @Override
    public int deleteWmsOutboundById(Long outboundId)
    {
        return wmsOutboundMapper.deleteWmsOutboundById(outboundId);
    }

    @Override
    public int deleteWmsOutboundByIds(Long[] outboundIds)
    {
        return wmsOutboundMapper.deleteWmsOutboundByIds(outboundIds);
    }

    private String generateOutboundNo()
    {
        String dateValue = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String noPrefix = "CK" + dateValue;
        String maxOutboundNo = wmsOutboundMapper.selectMaxOutboundNoByPrefix(noPrefix);
        if (maxOutboundNo == null || maxOutboundNo.isEmpty())
        {
            return noPrefix + "0001";
        }
        String sequenceValue = maxOutboundNo.substring(noPrefix.length());
        int nextSequenceValue = Integer.parseInt(sequenceValue) + 1;
        return noPrefix + String.format("%04d", nextSequenceValue);
    }
}
