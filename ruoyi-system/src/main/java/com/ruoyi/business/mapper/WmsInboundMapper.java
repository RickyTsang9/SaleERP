package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.WmsInbound;

public interface WmsInboundMapper
{
    public WmsInbound selectWmsInboundById(Long inboundId);

    public List<WmsInbound> selectWmsInboundList(WmsInbound wmsInbound);

    public int insertWmsInbound(WmsInbound wmsInbound);

    public int updateWmsInbound(WmsInbound wmsInbound);

    public int deleteWmsInboundById(Long inboundId);

    public int deleteWmsInboundByIds(Long[] inboundIds);

    public String selectMaxInboundNoByPrefix(String noPrefix);
}
