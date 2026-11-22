package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.WmsOutbound;

public interface IWmsOutboundService
{
    public WmsOutbound selectWmsOutboundById(Long outboundId);

    public List<WmsOutbound> selectWmsOutboundList(WmsOutbound wmsOutbound);

    public int insertWmsOutbound(WmsOutbound wmsOutbound);

    public int updateWmsOutbound(WmsOutbound wmsOutbound);

    public int submitWmsOutbound(Long outboundId);

    public int auditWmsOutbound(Long outboundId);

    public int rejectWmsOutbound(Long outboundId, String rejectReason);

    /**
     * 更新已审核出库单配送物流状态（pending -> shipping -> delivered）
     */
    public int updateWmsOutboundDelivery(Long outboundId, String deliveryStatus, String carrier, String waybillNo, java.math.BigDecimal freightCost);

    public int cancelWmsOutbound(Long outboundId);

    public int deleteWmsOutboundById(Long outboundId);

    public int deleteWmsOutboundByIds(Long[] outboundIds);
}
