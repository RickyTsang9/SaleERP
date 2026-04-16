package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.WmsTransferItem;
import com.ruoyi.business.domain.WmsStock;
import com.ruoyi.business.domain.WmsStockLog;
import com.ruoyi.business.mapper.WmsStockLogMapper;
import com.ruoyi.business.mapper.WmsStockMapper;
import com.ruoyi.business.mapper.WmsTransferMapper;
import com.ruoyi.business.domain.WmsTransfer;
import com.ruoyi.business.service.IWmsTransferService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 库存调拨Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
@Service
public class WmsTransferServiceImpl implements IWmsTransferService 
{
    private static final String STATUS_PENDING_AUDIT = "0";

    private static final String STATUS_AUDITED = "1";

    private static final String STATUS_CANCELLED = "2";

    @Autowired
    private WmsTransferMapper wmsTransferMapper;

    @Autowired
    private WmsStockMapper wmsStockMapper;

    @Autowired
    private WmsStockLogMapper wmsStockLogMapper;

    /**
     * 查询库存调拨
     * 
     * @param transferId 库存调拨主键
     * @return 库存调拨
     */
    @Override
    public WmsTransfer selectWmsTransferByTransferId(Long transferId)
    {
        return wmsTransferMapper.selectWmsTransferByTransferId(transferId);
    }

    /**
     * 查询库存调拨列表
     * 
     * @param wmsTransfer 库存调拨
     * @return 库存调拨
     */
    @Override
    public List<WmsTransfer> selectWmsTransferList(WmsTransfer wmsTransfer)
    {
        return wmsTransferMapper.selectWmsTransferList(wmsTransfer);
    }

    /**
     * 新增库存调拨
     * 
     * @param wmsTransfer 库存调拨
     * @return 结果
     */
    @Transactional
    @Override
    public int insertWmsTransfer(WmsTransfer wmsTransfer)
    {
        validateTransferBeforeSave(wmsTransfer);
        wmsTransfer.setStatus(STATUS_PENDING_AUDIT);
        wmsTransfer.setCreateTime(DateUtils.getNowDate());
        try
        {
            wmsTransfer.setCreateBy(SecurityUtils.getUsername());
        }
        catch (Exception exception)
        {
            // ignore
        }
        int rows = wmsTransferMapper.insertWmsTransfer(wmsTransfer);
        insertWmsTransferItem(wmsTransfer);
        return rows;
    }

    /**
     * 修改库存调拨
     * 
     * @param wmsTransfer 库存调拨
     * @return 结果
     */
    @Transactional
    @Override
    public int updateWmsTransfer(WmsTransfer wmsTransfer)
    {
        WmsTransfer databaseTransfer = wmsTransferMapper.selectWmsTransferByTransferId(wmsTransfer.getTransferId());
        if (databaseTransfer == null)
        {
            throw new ServiceException("库存调拨不存在");
        }
        if (!STATUS_PENDING_AUDIT.equals(databaseTransfer.getStatus()))
        {
            throw new ServiceException("仅待审核状态库存调拨允许修改");
        }
        validateTransferBeforeSave(wmsTransfer);
        wmsTransfer.setStatus(databaseTransfer.getStatus());
        wmsTransfer.setUpdateTime(DateUtils.getNowDate());
        try
        {
            wmsTransfer.setUpdateBy(SecurityUtils.getUsername());
        }
        catch (Exception exception)
        {
            // ignore
        }
        wmsTransferMapper.deleteWmsTransferItemByTransferId(wmsTransfer.getTransferId());
        insertWmsTransferItem(wmsTransfer);
        return wmsTransferMapper.updateWmsTransfer(wmsTransfer);
    }

    /**
     * 提交库存调拨
     * 
     * @param transferId 库存调拨主键
     * @return 结果
     */
    @Override
    public int submitWmsTransfer(Long transferId)
    {
        WmsTransfer databaseTransfer = wmsTransferMapper.selectWmsTransferByTransferId(transferId);
        if (databaseTransfer == null)
        {
            throw new ServiceException("库存调拨不存在");
        }
        if (!STATUS_PENDING_AUDIT.equals(databaseTransfer.getStatus()))
        {
            throw new ServiceException("仅待审核状态库存调拨允许提交");
        }
        validateTransferBeforeSave(databaseTransfer);
        throw new ServiceException("库存调拨保存后直接进入待审核，不需要重复提交");
    }

    /**
     * 审核库存调拨
     * 
     * @param transferId 库存调拨主键
     * @return 结果
     */
    @Override
    @Transactional
    public int auditWmsTransfer(Long transferId)
    {
        WmsTransfer databaseTransfer = wmsTransferMapper.selectWmsTransferByTransferId(transferId);
        if (databaseTransfer == null)
        {
            throw new ServiceException("库存调拨不存在");
        }
        if (!STATUS_PENDING_AUDIT.equals(databaseTransfer.getStatus()))
        {
            throw new ServiceException("仅待审核状态库存调拨允许审核");
        }
        if (databaseTransfer.getOutWarehouseId() == null || databaseTransfer.getInWarehouseId() == null)
        {
            throw new ServiceException("调入调出仓库不能为空");
        }
        if (databaseTransfer.getOutWarehouseId().equals(databaseTransfer.getInWarehouseId()))
        {
            throw new ServiceException("调入仓库不能与调出仓库相同");
        }
        List<WmsTransferItem> transferItemList = databaseTransfer.getWmsTransferItemList();
        if (transferItemList == null || transferItemList.isEmpty())
        {
            throw new ServiceException("调拨明细不能为空");
        }
        for (WmsTransferItem wmsTransferItem : transferItemList)
        {
            if (wmsTransferItem.getProductId() == null || wmsTransferItem.getQuantity() == null
                || wmsTransferItem.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new ServiceException("调拨明细信息不完整");
            }
            BigDecimal remainTransferQuantity = wmsTransferItem.getQuantity();
            WmsStock outStockQuery = new WmsStock();
            outStockQuery.setWarehouseId(databaseTransfer.getOutWarehouseId());
            outStockQuery.setProductId(wmsTransferItem.getProductId());
            List<WmsStock> outStockList = wmsStockMapper.selectWmsStockList(outStockQuery);
            if (outStockList == null || outStockList.isEmpty())
            {
                throw new ServiceException("调出仓库库存不存在，商品编号：" + wmsTransferItem.getProductId());
            }
            for (WmsStock outStock : outStockList)
            {
                if (remainTransferQuantity.compareTo(BigDecimal.ZERO) <= 0)
                {
                    break;
                }
                BigDecimal currentStockQuantity = outStock.getQuantity() == null ? BigDecimal.ZERO : outStock.getQuantity();
                if (currentStockQuantity.compareTo(BigDecimal.ZERO) <= 0)
                {
                    continue;
                }
                BigDecimal currentTransferQuantity = remainTransferQuantity.min(currentStockQuantity);
                BigDecimal afterOutQuantity = currentStockQuantity.subtract(currentTransferQuantity);
                outStock.setQuantity(afterOutQuantity);
                wmsStockMapper.updateWmsStock(outStock);
                WmsStockLog outboundStockLog = new WmsStockLog();
                outboundStockLog.setWarehouseId(databaseTransfer.getOutWarehouseId());
                outboundStockLog.setProductId(wmsTransferItem.getProductId());
                outboundStockLog.setLocationId(outStock.getLocationId());
                outboundStockLog.setBatchNo(outStock.getBatchNo());
                outboundStockLog.setBillType("transfer");
                outboundStockLog.setBillId(transferId);
                outboundStockLog.setBillNo(databaseTransfer.getTransferNo());
                outboundStockLog.setInOut("out");
                outboundStockLog.setQuantity(currentTransferQuantity);
                outboundStockLog.setPrice(BigDecimal.ZERO);
                outboundStockLog.setAmount(BigDecimal.ZERO);
                outboundStockLog.setBeforeQty(currentStockQuantity);
                outboundStockLog.setAfterQty(afterOutQuantity);
                wmsStockLogMapper.insertWmsStockLog(outboundStockLog);
                WmsStock inStock = getOrCreateInStock(databaseTransfer.getInWarehouseId(), wmsTransferItem.getProductId(), outStock);
                BigDecimal beforeInQuantity = inStock.getQuantity() == null ? BigDecimal.ZERO : inStock.getQuantity();
                BigDecimal afterInQuantity = beforeInQuantity.add(currentTransferQuantity);
                inStock.setQuantity(afterInQuantity);
                wmsStockMapper.updateWmsStock(inStock);
                WmsStockLog inboundStockLog = new WmsStockLog();
                inboundStockLog.setWarehouseId(databaseTransfer.getInWarehouseId());
                inboundStockLog.setProductId(wmsTransferItem.getProductId());
                inboundStockLog.setLocationId(inStock.getLocationId());
                inboundStockLog.setBatchNo(inStock.getBatchNo());
                inboundStockLog.setBillType("transfer");
                inboundStockLog.setBillId(transferId);
                inboundStockLog.setBillNo(databaseTransfer.getTransferNo());
                inboundStockLog.setInOut("in");
                inboundStockLog.setQuantity(currentTransferQuantity);
                inboundStockLog.setPrice(BigDecimal.ZERO);
                inboundStockLog.setAmount(BigDecimal.ZERO);
                inboundStockLog.setBeforeQty(beforeInQuantity);
                inboundStockLog.setAfterQty(afterInQuantity);
                wmsStockLogMapper.insertWmsStockLog(inboundStockLog);
                remainTransferQuantity = remainTransferQuantity.subtract(currentTransferQuantity);
            }
            if (remainTransferQuantity.compareTo(BigDecimal.ZERO) > 0)
            {
                throw new ServiceException("调出仓库库存不足，商品编号：" + wmsTransferItem.getProductId());
            }
        }
        String updateUsername = SecurityUtils.getUsername();
        int updateCount = wmsTransferMapper.updateWmsTransferStatus(
            transferId, STATUS_PENDING_AUDIT, STATUS_AUDITED, updateUsername);
        if (updateCount == 0)
        {
            throw new ServiceException("库存调拨状态已变更，请刷新后重试");
        }
        return updateCount;
    }

    /**
     * 作废库存调拨
     * 
     * @param transferId 库存调拨主键
     * @return 结果
     */
    @Override
    public int cancelWmsTransfer(Long transferId)
    {
        WmsTransfer databaseTransfer = wmsTransferMapper.selectWmsTransferByTransferId(transferId);
        if (databaseTransfer == null)
        {
            throw new ServiceException("库存调拨不存在");
        }
        if (STATUS_CANCELLED.equals(databaseTransfer.getStatus()))
        {
            throw new ServiceException("库存调拨已作废，无需重复作废");
        }
        if (STATUS_AUDITED.equals(databaseTransfer.getStatus()))
        {
            throw new ServiceException("已审核库存调拨不允许作废");
        }
        String updateUsername = SecurityUtils.getUsername();
        int updateCount = wmsTransferMapper.updateWmsTransferStatus(
            transferId, databaseTransfer.getStatus(), STATUS_CANCELLED, updateUsername);
        if (updateCount == 0)
        {
            throw new ServiceException("库存调拨状态已变更，请刷新后重试");
        }
        return updateCount;
    }

    /**
     * 批量删除库存调拨
     * 
     * @param transferIds 需要删除的库存调拨主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteWmsTransferByTransferIds(Long[] transferIds)
    {
        for (Long transferId : transferIds)
        {
            WmsTransfer databaseTransfer = wmsTransferMapper.selectWmsTransferByTransferId(transferId);
            if (databaseTransfer == null)
            {
                continue;
            }
            if (!STATUS_PENDING_AUDIT.equals(databaseTransfer.getStatus()))
            {
                throw new ServiceException("仅待审核状态库存调拨允许删除，调拨单号：" + databaseTransfer.getTransferNo());
            }
        }
        wmsTransferMapper.deleteWmsTransferItemByTransferIds(transferIds);
        return wmsTransferMapper.deleteWmsTransferByTransferIds(transferIds);
    }

    /**
     * 删除库存调拨信息
     * 
     * @param transferId 库存调拨主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteWmsTransferByTransferId(Long transferId)
    {
        WmsTransfer databaseTransfer = wmsTransferMapper.selectWmsTransferByTransferId(transferId);
        if (databaseTransfer == null)
        {
            return 0;
        }
        if (!STATUS_PENDING_AUDIT.equals(databaseTransfer.getStatus()))
        {
            throw new ServiceException("仅待审核状态库存调拨允许删除");
        }
        wmsTransferMapper.deleteWmsTransferItemByTransferId(transferId);
        return wmsTransferMapper.deleteWmsTransferByTransferId(transferId);
    }

    /**
     * 新增库存调拨明细信息
     * 
     * @param wmsTransfer 库存调拨对象
     */
    public void insertWmsTransferItem(WmsTransfer wmsTransfer)
    {
        List<WmsTransferItem> wmsTransferItemList = wmsTransfer.getWmsTransferItemList();
        Long transferId = wmsTransfer.getTransferId();
        if (StringUtils.isNotNull(wmsTransferItemList))
        {
            List<WmsTransferItem> list = new ArrayList<WmsTransferItem>();
            for (WmsTransferItem wmsTransferItem : wmsTransferItemList)
            {
                wmsTransferItem.setTransferId(transferId);
                list.add(wmsTransferItem);
            }
            if (list.size() > 0)
            {
                wmsTransferMapper.batchWmsTransferItem(list);
            }
        }
    }

    /**
     * 保存库存调拨前校验主单和明细数据，并重算总数量
     * 
     * @param wmsTransfer 库存调拨
     */
    private void validateTransferBeforeSave(WmsTransfer wmsTransfer)
    {
        if (wmsTransfer == null)
        {
            throw new ServiceException("库存调拨不存在");
        }
        if (wmsTransfer.getOutWarehouseId() == null || wmsTransfer.getInWarehouseId() == null)
        {
            throw new ServiceException("调入调出仓库不能为空");
        }
        if (wmsTransfer.getOutWarehouseId().equals(wmsTransfer.getInWarehouseId()))
        {
            throw new ServiceException("调入仓库不能与调出仓库相同");
        }
        if (wmsTransfer.getTransferDate() == null)
        {
            throw new ServiceException("调拨日期不能为空");
        }
        validateTransferItemList(wmsTransfer.getWmsTransferItemList());
        wmsTransfer.setTotalQuantity(calculateTransferTotalQuantity(wmsTransfer.getWmsTransferItemList()));
    }

    /**
     * 校验库存调拨明细是否完整
     * 
     * @param wmsTransferItemList 库存调拨明细列表
     */
    private void validateTransferItemList(List<WmsTransferItem> wmsTransferItemList)
    {
        if (wmsTransferItemList == null || wmsTransferItemList.isEmpty())
        {
            throw new ServiceException("调拨明细不能为空");
        }
        for (int transferItemIndex = 0; transferItemIndex < wmsTransferItemList.size(); transferItemIndex++)
        {
            WmsTransferItem wmsTransferItem = wmsTransferItemList.get(transferItemIndex);
            if (wmsTransferItem.getProductId() == null)
            {
                throw new ServiceException("第" + (transferItemIndex + 1) + "行调拨明细商品不能为空");
            }
            if (wmsTransferItem.getQuantity() == null || wmsTransferItem.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new ServiceException("第" + (transferItemIndex + 1) + "行调拨数量必须大于0");
            }
        }
    }

    /**
     * 根据调拨明细重新计算总数量
     * 
     * @param wmsTransferItemList 库存调拨明细列表
     * @return 总数量
     */
    private BigDecimal calculateTransferTotalQuantity(List<WmsTransferItem> wmsTransferItemList)
    {
        BigDecimal totalQuantityValue = BigDecimal.ZERO;
        if (wmsTransferItemList == null || wmsTransferItemList.isEmpty())
        {
            return totalQuantityValue;
        }
        for (WmsTransferItem wmsTransferItem : wmsTransferItemList)
        {
            if (wmsTransferItem.getQuantity() != null)
            {
                totalQuantityValue = totalQuantityValue.add(wmsTransferItem.getQuantity());
            }
        }
        return totalQuantityValue;
    }

    /**
     * 根据调出库存唯一键获取或创建调入库存
     *
     * @param inWarehouseId 调入仓库主键
     * @param productId 商品主键
     * @param sourceStock 调出库存
     * @return 调入库存
     */
    private WmsStock getOrCreateInStock(Long inWarehouseId, Long productId, WmsStock sourceStock)
    {
        WmsStock inStockQuery = new WmsStock();
        inStockQuery.setWarehouseId(inWarehouseId);
        inStockQuery.setProductId(productId);
        inStockQuery.setLocationId(sourceStock.getLocationId());
        inStockQuery.setBatchNo(sourceStock.getBatchNo());
        WmsStock existingInStock = wmsStockMapper.selectWmsStockByKey(inStockQuery);
        if (existingInStock != null)
        {
            return existingInStock;
        }
        WmsStock newStock = new WmsStock();
        newStock.setWarehouseId(inWarehouseId);
        newStock.setProductId(productId);
        newStock.setLocationId(sourceStock.getLocationId());
        newStock.setBatchNo(sourceStock.getBatchNo());
        newStock.setQuantity(BigDecimal.ZERO);
        newStock.setLockedQuantity(BigDecimal.ZERO);
        newStock.setFrozenQuantity(BigDecimal.ZERO);
        newStock.setVersion(0);
        wmsStockMapper.insertWmsStock(newStock);
        return newStock;
    }
}
