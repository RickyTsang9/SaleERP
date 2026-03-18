package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.WmsInbound;

public interface IWmsInboundService
{
    public WmsInbound selectWmsInboundById(Long inboundId);

    public List<WmsInbound> selectWmsInboundList(WmsInbound wmsInbound);

    public int insertWmsInbound(WmsInbound wmsInbound);

    public int updateWmsInbound(WmsInbound wmsInbound);

    public int deleteWmsInboundById(Long inboundId);

    public int deleteWmsInboundByIds(Long[] inboundIds);
}
