package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.WmsOutbound;

public interface WmsOutboundMapper
{
    /**
     * 查询出库单
     *
     * @param outboundId 出库单主键
     * @return 出库单
     */
    public WmsOutbound selectWmsOutboundById(Long outboundId);

    /**
     * 查询出库单列表
     *
     * @param wmsOutbound 出库单
     * @return 出库单集合
     */
    public List<WmsOutbound> selectWmsOutboundList(WmsOutbound wmsOutbound);

    /**
     * 根据销售订单统计待处理关联出库单数量
     *
     * @param saleOrderId 销售订单主键
     * @param currentOutboundId 当前出库单主键
     * @return 待处理关联出库单数量
     */
    public Long selectPendingLinkedOutboundCountBySaleOrderId(Long saleOrderId, Long currentOutboundId);

    /**
     * 根据来源单号统计待处理关联出库单数量
     *
     * @param sourceOrderNo 来源单号
     * @param currentOutboundId 当前出库单主键
     * @return 待处理关联出库单数量
     */
    public Long selectPendingLinkedOutboundCountBySourceOrderNo(String sourceOrderNo, Long currentOutboundId);

    /**
     * 根据销售订单统计有效关联出库单数量
     *
     * @param saleOrderId 销售订单主键
     * @return 有效关联出库单数量
     */
    public Long selectActiveOutboundCountBySaleOrderId(Long saleOrderId);

    /**
     * 根据来源单号统计有效关联出库单数量
     *
     * @param sourceOrderNo 来源单号
     * @return 有效关联出库单数量
     */
    public Long selectActiveOutboundCountBySourceOrderNo(String sourceOrderNo);

    /**
     * 新增出库单
     *
     * @param wmsOutbound 出库单
     * @return 结果
     */
    public int insertWmsOutbound(WmsOutbound wmsOutbound);

    /**
     * 修改出库单
     *
     * @param wmsOutbound 出库单
     * @return 结果
     */
    public int updateWmsOutbound(WmsOutbound wmsOutbound);

    /**
     * 删除出库单
     *
     * @param outboundId 出库单主键
     * @return 结果
     */
    public int deleteWmsOutboundById(Long outboundId);

    /**
     * 批量删除出库单
     *
     * @param outboundIds 出库单主键集合
     * @return 结果
     */
    public int deleteWmsOutboundByIds(Long[] outboundIds);

    /**
     * 根据前缀查询最大出库单号
     *
     * @param noPrefix 单号前缀
     * @return 最大出库单号
     */
    public String selectMaxOutboundNoByPrefix(String noPrefix);

    /**
     * 刷新出库单汇总
     *
     * @param outboundId 出库单主键
     * @return 结果
     */
    public int refreshTotalByOutboundId(Long outboundId);

    /**
     * 更新出库单状态
     *
     * @param outboundId 出库单主键
     * @param oldStatus 原状态
     * @param newStatus 新状态
     * @param auditBy 审核人
     * @param updateBy 更新人
     * @return 结果
     */
    public int updateWmsOutboundStatus(Long outboundId, String oldStatus, String newStatus, String auditBy, String updateBy);
}
