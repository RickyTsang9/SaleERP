package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.WmsOutbound;

public interface IWmsOutboundService
{
    public WmsOutbound selectWmsOutboundById(Long outboundId);

    public List<WmsOutbound> selectWmsOutboundList(WmsOutbound wmsOutbound);

    public int insertWmsOutbound(WmsOutbound wmsOutbound);

    public int updateWmsOutbound(WmsOutbound wmsOutbound);

    public int deleteWmsOutboundById(Long outboundId);

    public int deleteWmsOutboundByIds(Long[] outboundIds);
}
