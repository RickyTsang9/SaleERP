package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.WmsTransfer;
import com.ruoyi.business.domain.WmsTransferItem;

/**
 * 库存调拨Mapper接口
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
public interface WmsTransferMapper 
{
    /**
     * 查询库存调拨
     * 
     * @param transferId 库存调拨主键
     * @return 库存调拨
     */
    public WmsTransfer selectWmsTransferByTransferId(Long transferId);

    /**
     * 查询库存调拨列表
     * 
     * @param wmsTransfer 库存调拨
     * @return 库存调拨集合
     */
    public List<WmsTransfer> selectWmsTransferList(WmsTransfer wmsTransfer);

    /**
     * 新增库存调拨
     * 
     * @param wmsTransfer 库存调拨
     * @return 结果
     */
    public int insertWmsTransfer(WmsTransfer wmsTransfer);

    /**
     * 修改库存调拨
     * 
     * @param wmsTransfer 库存调拨
     * @return 结果
     */
    public int updateWmsTransfer(WmsTransfer wmsTransfer);

    /**
     * 删除库存调拨
     * 
     * @param transferId 库存调拨主键
     * @return 结果
     */
    public int deleteWmsTransferByTransferId(Long transferId);

    /**
     * 批量删除库存调拨
     * 
     * @param transferIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteWmsTransferByTransferIds(Long[] transferIds);

    /**
     * 批量删除库存调拨明细
     * 
     * @param transferIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteWmsTransferItemByTransferIds(Long[] transferIds);
    
    /**
     * 批量新增库存调拨明细
     * 
     * @param wmsTransferItemList 库存调拨明细列表
     * @return 结果
     */
    public int batchWmsTransferItem(List<WmsTransferItem> wmsTransferItemList);
    

    /**
     * 通过库存调拨主键删除库存调拨明细信息
     * 
     * @param transferId 库存调拨ID
     * @return 结果
     */
    public int deleteWmsTransferItemByTransferId(Long transferId);

    /**
     * 更新库存调拨状态
     * 
     * @param transferId 调拨单主键
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     * @param updateBy 更新人
     * @return 结果
     */
    public int updateWmsTransferStatus(Long transferId, String oldStatus, String newStatus, String updateBy);
}
