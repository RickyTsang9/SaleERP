package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.WmsOutbound;

public interface WmsOutboundMapper
{
    public WmsOutbound selectWmsOutboundById(Long outboundId);

    public List<WmsOutbound> selectWmsOutboundList(WmsOutbound wmsOutbound);

    public int insertWmsOutbound(WmsOutbound wmsOutbound);

    public int updateWmsOutbound(WmsOutbound wmsOutbound);

    public int deleteWmsOutboundById(Long outboundId);

    public int deleteWmsOutboundByIds(Long[] outboundIds);

    public String selectMaxOutboundNoByPrefix(String noPrefix);

    public int refreshTotalByOutboundId(Long outboundId);

    public int updateWmsOutboundStatus(Long outboundId, String oldStatus, String newStatus, String auditBy, String updateBy);
}
