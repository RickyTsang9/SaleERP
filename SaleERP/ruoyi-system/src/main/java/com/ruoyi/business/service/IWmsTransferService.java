package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.WmsTransfer;

/**
 * 库存调拨Service接口
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
public interface IWmsTransferService 
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
     * 提交库存调拨
     * 
     * @param transferId 库存调拨主键
     * @return 结果
     */
    public int submitWmsTransfer(Long transferId);

    /**
     * 审核库存调拨
     * 
     * @param transferId 库存调拨主键
     * @return 结果
     */
    public int auditWmsTransfer(Long transferId);

    /**
     * 作废库存调拨
     * 
     * @param transferId 库存调拨主键
     * @return 结果
     */
    public int cancelWmsTransfer(Long transferId);

    /**
     * 批量删除库存调拨
     * 
     * @param transferIds 需要删除的库存调拨主键集合
     * @return 结果
     */
    public int deleteWmsTransferByTransferIds(Long[] transferIds);

    /**
     * 删除库存调拨信息
     * 
     * @param transferId 库存调拨主键
     * @return 结果
     */
    public int deleteWmsTransferByTransferId(Long transferId);
}
