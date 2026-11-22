package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.MdLocation;
import com.ruoyi.business.domain.MdProduct;
import com.ruoyi.business.domain.WmsStock;
import com.ruoyi.business.domain.WmsStockLog;
import com.ruoyi.business.domain.WmsTransfer;
import com.ruoyi.business.domain.WmsTransferItem;
import com.ruoyi.business.mapper.MdLocationMapper;
import com.ruoyi.business.mapper.MdProductMapper;
import com.ruoyi.business.mapper.WmsStockLogMapper;
import com.ruoyi.business.mapper.WmsStockMapper;
import com.ruoyi.business.mapper.WmsTransferMapper;
import com.ruoyi.business.service.IWmsStockService;
import com.ruoyi.business.service.IWmsTransferService;
import com.ruoyi.business.service.support.BizComplianceHelper;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

@Service
public class WmsTransferServiceImpl implements IWmsTransferService
{
    private static final String STATUS_DRAFT = "draft";

    private static final String STATUS_PENDING_AUDIT = "0";

    private static final String STATUS_IN_TRANSIT = "in_transit";

    private static final String STATUS_AUDITED = "1";

    private static final String STATUS_CANCELLED = "2";

    @Autowired
    private WmsTransferMapper wmsTransferMapper;

    @Autowired
    private WmsStockMapper wmsStockMapper;

    @Autowired
    private WmsStockLogMapper wmsStockLogMapper;

    @Autowired(required = false)
    private IWmsStockService wmsStockService;

    @Autowired(required = false)
    private MdProductMapper mdProductMapper;

    @Autowired(required = false)
    private MdLocationMapper mdLocationMapper;

    @Autowired(required = false)
    private BizComplianceHelper complianceHelper;

    @Override
    public WmsTransfer selectWmsTransferByTransferId(Long transferId)
    {
        return wmsTransferMapper.selectWmsTransferByTransferId(transferId);
    }

    @Override
    @DataScope(deptAlias = "su", userAlias = "su", permission = "business:transfer:list")
    public List<WmsTransfer> selectWmsTransferList(WmsTransfer wmsTransfer)
    {
        return wmsTransferMapper.selectWmsTransferList(wmsTransfer);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int insertWmsTransfer(WmsTransfer wmsTransfer)
    {
        validateTransferBeforeSave(wmsTransfer);
        if (StringUtils.isEmpty(wmsTransfer.getTransferNo()))
        {
            wmsTransfer.setTransferNo("DB" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + String.format("%04d", (int) (System.currentTimeMillis() % 10000)));
        }
        wmsTransfer.setStatus(STATUS_DRAFT);
        wmsTransfer.setCreateTime(DateUtils.getNowDate());
        try
        {
            wmsTransfer.setCreateBy(SecurityUtils.getUsername());
        }
        catch (Exception ignored) {}
        int rows = wmsTransferMapper.insertWmsTransfer(wmsTransfer);
        insertWmsTransferItem(wmsTransfer);
        return rows;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateWmsTransfer(WmsTransfer wmsTransfer)
    {
        WmsTransfer databaseTransfer = wmsTransferMapper.selectWmsTransferByTransferId(wmsTransfer.getTransferId());
        if (databaseTransfer == null)
        {
            throw new ServiceException("库存调拨不存在");
        }
        if (!STATUS_DRAFT.equals(databaseTransfer.getStatus()))
        {
            throw new ServiceException("仅草稿状态库存调拨允许修改，待审核单据请先驳回");
        }
        validateTransferBeforeSave(wmsTransfer);
        wmsTransfer.setStatus(databaseTransfer.getStatus());
        wmsTransfer.setUpdateTime(DateUtils.getNowDate());
        try
        {
            wmsTransfer.setUpdateBy(SecurityUtils.getUsername());
        }
        catch (Exception ignored) {}
        wmsTransferMapper.deleteWmsTransferItemByTransferId(wmsTransfer.getTransferId());
        insertWmsTransferItem(wmsTransfer);
        return wmsTransferMapper.updateWmsTransfer(wmsTransfer);
    }

    @Override
    public int submitWmsTransfer(Long transferId)
    {
        WmsTransfer databaseTransfer = wmsTransferMapper.selectWmsTransferByTransferId(transferId);
        if (databaseTransfer == null)
        {
            throw new ServiceException("库存调拨不存在");
        }
        if (!STATUS_DRAFT.equals(databaseTransfer.getStatus()))
        {
            throw new ServiceException("仅草稿状态库存调拨允许提交");
        }
        validateTransferBeforeSave(databaseTransfer);
        int rows = wmsTransferMapper.updateWmsTransferStatus(transferId, STATUS_DRAFT, STATUS_PENDING_AUDIT, SecurityUtils.getUsername());
        if (rows == 0)
        {
            throw new ServiceException("调拨单状态已变更，请刷新后重试");
        }
        return rows;
    }

    /**
     * 第一步：审核调出（扣减调出仓可用库存并进入在途 in_transit 状态，固化批次效期与单位成本）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditWmsTransfer(Long transferId)
    {
        WmsTransfer databaseTransfer = wmsTransferMapper.selectWmsTransferByTransferId(transferId);
        if (databaseTransfer == null)
        {
            throw new ServiceException("库存调拨不存在");
        }
        if (!STATUS_PENDING_AUDIT.equals(databaseTransfer.getStatus()))
        {
            throw new ServiceException("仅待审核状态库存调拨允许审核调出");
        }
        if (complianceHelper != null)
        {
            complianceHelper.assertPeriodOpen(new Date(), "审核库存调拨");
            complianceHelper.assertNotSelfAudit(databaseTransfer.getCreateBy(), "库存调拨审核");
            complianceHelper.assertEntityEnabled("md_warehouse", "warehouse_id", databaseTransfer.getOutWarehouseId(), "调出仓库");
            complianceHelper.assertEntityEnabled("md_warehouse", "warehouse_id", databaseTransfer.getInWarehouseId(), "调入仓库");
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

        String operator = SecurityUtils.getUsername();
        List<WmsTransferItem> splitBatchItems = new ArrayList<>();

        for (WmsTransferItem item : transferItemList)
        {
            if (item.getProductId() == null || item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new ServiceException("调拨明细信息不完整");
            }
            BigDecimal unitCost = resolveProductCost(item.getProductId());
            BigDecimal remainTransferQty = item.getQuantity();

            // 按 FEFO 顺序优先调出即将到期但未过期的批次
            List<WmsStock> candidateStocks = wmsStockMapper.selectFefoCandidateBatches(
                databaseTransfer.getOutWarehouseId(), item.getProductId()
            );
            if (candidateStocks == null || candidateStocks.isEmpty())
            {
                WmsStock fallbackQuery = new WmsStock();
                fallbackQuery.setWarehouseId(databaseTransfer.getOutWarehouseId());
                fallbackQuery.setProductId(item.getProductId());
                candidateStocks = wmsStockMapper.selectWmsStockList(fallbackQuery);
            }
            if (candidateStocks == null || candidateStocks.isEmpty())
            {
                throw new ServiceException("调出仓库库存不存在，商品编号：" + item.getProductId());
            }

            for (WmsStock outStock : candidateStocks)
            {
                if (remainTransferQty.compareTo(BigDecimal.ZERO) <= 0)
                {
                    break;
                }
                BigDecimal avail = safeAvailable(outStock);
                if (avail.compareTo(BigDecimal.ZERO) <= 0)
                {
                    continue;
                }
                BigDecimal stepQty = remainTransferQty.min(avail);
                BigDecimal[] qtyPair = wmsStockService != null
                    ? wmsStockService.deductAvailableStockByKeyAtomically(
                        databaseTransfer.getOutWarehouseId(), item.getProductId(),
                        outStock.getLocationId(), outStock.getBatchNo(), stepQty, operator, true)
                    : deductDirectly(outStock, stepQty);

                WmsStockLog outboundLog = new WmsStockLog();
                outboundLog.setWarehouseId(databaseTransfer.getOutWarehouseId());
                outboundLog.setProductId(item.getProductId());
                outboundLog.setLocationId(outStock.getLocationId());
                outboundLog.setBatchNo(outStock.getBatchNo());
                outboundLog.setBillType("transfer");
                outboundLog.setBillId(transferId);
                outboundLog.setBillNo(databaseTransfer.getTransferNo());
                outboundLog.setInOut("out");
                outboundLog.setQuantity(stepQty);
                outboundLog.setPrice(unitCost);
                outboundLog.setAmount(stepQty.multiply(unitCost));
                outboundLog.setBeforeQty(qtyPair[0]);
                outboundLog.setAfterQty(qtyPair[1]);
                wmsStockLogMapper.insertWmsStockLog(outboundLog);

                WmsTransferItem snapItem = new WmsTransferItem();
                snapItem.setTransferId(transferId);
                snapItem.setProductId(item.getProductId());
                snapItem.setOutLocationId(outStock.getLocationId());
                snapItem.setInLocationId(item.getInLocationId());
                snapItem.setBatchNo(outStock.getBatchNo());
                snapItem.setExpireDate(outStock.getExpireDate());
                snapItem.setQuantity(stepQty);
                snapItem.setUnitCost(unitCost);
                snapItem.setRemark(item.getRemark());
                splitBatchItems.add(snapItem);

                remainTransferQty = remainTransferQty.subtract(stepQty);
            }
            if (remainTransferQty.compareTo(BigDecimal.ZERO) > 0)
            {
                throw new ServiceException("调出仓库可用库存不足，商品编号：" + item.getProductId());
            }
        }

        wmsTransferMapper.deleteWmsTransferItemByTransferId(transferId);
        wmsTransferMapper.batchWmsTransferItem(splitBatchItems);

        int updateCount = wmsTransferMapper.updateWmsTransferStatus(
            transferId, STATUS_PENDING_AUDIT, STATUS_IN_TRANSIT, operator);
        if (updateCount == 0)
        {
            throw new ServiceException("库存调拨状态已变更，请刷新后重试");
        }
        return updateCount;
    }

    /**
     * 第二步：调入仓验货签收（将途中的批次、效期与成本完整计入调入仓库）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int confirmReceiveWmsTransfer(Long transferId, Long inLocationId)
    {
        WmsTransfer databaseTransfer = wmsTransferMapper.selectWmsTransferByTransferId(transferId);
        if (databaseTransfer == null)
        {
            throw new ServiceException("库存调拨不存在");
        }
        if (!STATUS_IN_TRANSIT.equals(databaseTransfer.getStatus()))
        {
            throw new ServiceException("仅在途待签收状态的调拨单允许执行调入签收");
        }
        if (complianceHelper != null)
        {
            complianceHelper.assertPeriodOpen(new Date(), "签收库存调拨");
        }
        List<WmsTransferItem> items = databaseTransfer.getWmsTransferItemList();
        if (items == null || items.isEmpty())
        {
            throw new ServiceException("调拨在途明细为空，无法签收");
        }
        Long resolvedDefaultLocationId = inLocationId != null ? inLocationId : resolveDefaultLocationForWarehouse(databaseTransfer.getInWarehouseId());
        String receiver = SecurityUtils.getUsername();

        for (WmsTransferItem item : items)
        {
            Long targetLocationId = inLocationId != null
                ? inLocationId
                : (item.getInLocationId() != null ? item.getInLocationId() : resolvedDefaultLocationId);
            String batchNo = StringUtils.isEmpty(item.getBatchNo()) ? "TR-" + transferId : item.getBatchNo();
            BigDecimal unitCost = item.getUnitCost() == null ? resolveProductCost(item.getProductId()) : item.getUnitCost();

            BigDecimal[] qtyPair;
            if (wmsStockService != null)
            {
                qtyPair = wmsStockService.increaseStockByKeyAtomically(
                    databaseTransfer.getInWarehouseId(), item.getProductId(),
                    targetLocationId, batchNo, item.getExpireDate(), item.getQuantity(), receiver
                );
            }
            else
            {
                qtyPair = new BigDecimal[] { BigDecimal.ZERO, item.getQuantity() };
            }

            WmsStockLog inboundLog = new WmsStockLog();
            inboundLog.setWarehouseId(databaseTransfer.getInWarehouseId());
            inboundLog.setProductId(item.getProductId());
            inboundLog.setLocationId(targetLocationId);
            inboundLog.setBatchNo(batchNo);
            inboundLog.setBillType("transfer");
            inboundLog.setBillId(transferId);
            inboundLog.setBillNo(databaseTransfer.getTransferNo());
            inboundLog.setInOut("in");
            inboundLog.setQuantity(item.getQuantity());
            inboundLog.setPrice(unitCost);
            inboundLog.setAmount(item.getQuantity().multiply(unitCost));
            inboundLog.setBeforeQty(qtyPair[0]);
            inboundLog.setAfterQty(qtyPair[1]);
            wmsStockLogMapper.insertWmsStockLog(inboundLog);
        }

        int rows = wmsTransferMapper.confirmReceiveWmsTransferStatus(transferId, receiver);
        if (rows == 0)
        {
            throw new ServiceException("调拨单状态已变更，请刷新后重试");
        }
        return rows;
    }

    @Override
    public int rejectWmsTransfer(Long transferId, String rejectReason)
    {
        WmsTransfer databaseTransfer = wmsTransferMapper.selectWmsTransferByTransferId(transferId);
        if (databaseTransfer == null)
        {
            throw new ServiceException("库存调拨不存在");
        }
        if (!STATUS_PENDING_AUDIT.equals(databaseTransfer.getStatus()))
        {
            throw new ServiceException("仅待审核状态调拨单允许驳回");
        }
        String reason = StringUtils.isEmpty(rejectReason) ? "审核驳回重改" : rejectReason.trim();
        int rows = wmsTransferMapper.rejectWmsTransferStatus(transferId, reason, SecurityUtils.getUsername());
        if (rows == 0)
        {
            throw new ServiceException("调拨单状态已变更，请刷新后重试");
        }
        return rows;
    }

    /**
     * 在途调拨撤回/拒收退回原仓（释放滞留在途的库存并恢复原调出仓批次效期与成本）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int recallWmsTransfer(Long transferId, String reason)
    {
        WmsTransfer databaseTransfer = wmsTransferMapper.selectWmsTransferByTransferId(transferId);
        if (databaseTransfer == null)
        {
            throw new ServiceException("库存调拨不存在");
        }
        if (!STATUS_IN_TRANSIT.equals(databaseTransfer.getStatus()))
        {
            throw new ServiceException("仅在途待签收状态的调拨单允许执行在途撤回");
        }
        if (complianceHelper != null)
        {
            complianceHelper.assertPeriodOpen(new Date(), "撤回在途调拨");
        }
        List<WmsTransferItem> items = databaseTransfer.getWmsTransferItemList();
        if (items == null || items.isEmpty())
        {
            throw new ServiceException("调拨在途明细为空，无法撤回");
        }
        String operator = SecurityUtils.getUsername();
        String recallReason = StringUtils.isEmpty(reason) ? "在途撤回退回原仓" : reason.trim();
        int rows = wmsTransferMapper.recallInTransitWmsTransferStatus(transferId, recallReason, operator);
        if (rows == 0)
        {
            throw new ServiceException("调拨单状态已变更，请刷新后重试");
        }
        for (WmsTransferItem item : items)
        {
            BigDecimal unitCost = item.getUnitCost() == null ? resolveProductCost(item.getProductId()) : item.getUnitCost();
            BigDecimal[] qtyPair = wmsStockService != null
                ? wmsStockService.increaseStockByKeyAtomically(
                    databaseTransfer.getOutWarehouseId(), item.getProductId(),
                    item.getOutLocationId(), item.getBatchNo(), item.getExpireDate(), item.getQuantity(), operator)
                : new BigDecimal[] { BigDecimal.ZERO, item.getQuantity() };
            WmsStockLog recallLog = new WmsStockLog();
            recallLog.setWarehouseId(databaseTransfer.getOutWarehouseId());
            recallLog.setProductId(item.getProductId());
            recallLog.setLocationId(item.getOutLocationId());
            recallLog.setBatchNo(item.getBatchNo());
            recallLog.setBillType("transfer_recall");
            recallLog.setBillId(transferId);
            recallLog.setBillNo(databaseTransfer.getTransferNo());
            recallLog.setInOut("in");
            recallLog.setQuantity(item.getQuantity());
            recallLog.setPrice(unitCost);
            recallLog.setAmount(item.getQuantity().multiply(unitCost));
            recallLog.setBeforeQty(qtyPair[0]);
            recallLog.setAfterQty(qtyPair[1]);
            wmsStockLogMapper.insertWmsStockLog(recallLog);
        }
        return rows;
    }

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
        if (STATUS_AUDITED.equals(databaseTransfer.getStatus()) || STATUS_IN_TRANSIT.equals(databaseTransfer.getStatus()))
        {
            throw new ServiceException("在途或已签收完成的调拨单不允许直接作废");
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

    @Transactional(rollbackFor = Exception.class)
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
            if (!STATUS_DRAFT.equals(databaseTransfer.getStatus()))
            {
                throw new ServiceException("仅草稿状态库存调拨允许删除，单号：" + databaseTransfer.getTransferNo());
            }
        }
        wmsTransferMapper.deleteWmsTransferItemByTransferIds(transferIds);
        return wmsTransferMapper.deleteWmsTransferByTransferIds(transferIds);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deleteWmsTransferByTransferId(Long transferId)
    {
        WmsTransfer databaseTransfer = wmsTransferMapper.selectWmsTransferByTransferId(transferId);
        if (databaseTransfer == null)
        {
            return 0;
        }
        if (!STATUS_DRAFT.equals(databaseTransfer.getStatus()))
        {
            throw new ServiceException("仅草稿状态库存调拨允许删除");
        }
        wmsTransferMapper.deleteWmsTransferItemByTransferId(transferId);
        return wmsTransferMapper.deleteWmsTransferByTransferId(transferId);
    }

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
            if (!list.isEmpty())
            {
                wmsTransferMapper.batchWmsTransferItem(list);
            }
        }
    }

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

    private void validateTransferItemList(List<WmsTransferItem> wmsTransferItemList)
    {
        if (wmsTransferItemList == null || wmsTransferItemList.isEmpty())
        {
            throw new ServiceException("调拨明细不能为空");
        }
        for (int i = 0; i < wmsTransferItemList.size(); i++)
        {
            WmsTransferItem item = wmsTransferItemList.get(i);
            if (item.getProductId() == null)
            {
                throw new ServiceException("第" + (i + 1) + "行调拨明细商品不能为空");
            }
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new ServiceException("第" + (i + 1) + "行调拨数量必须大于0");
            }
        }
    }

    private BigDecimal calculateTransferTotalQuantity(List<WmsTransferItem> wmsTransferItemList)
    {
        BigDecimal total = BigDecimal.ZERO;
        if (wmsTransferItemList == null)
        {
            return total;
        }
        for (WmsTransferItem item : wmsTransferItemList)
        {
            if (item.getQuantity() != null)
            {
                total = total.add(item.getQuantity());
            }
        }
        return total;
    }

    private BigDecimal resolveProductCost(Long productId)
    {
        if (mdProductMapper == null || productId == null)
        {
            return BigDecimal.ZERO;
        }
        MdProduct product = mdProductMapper.selectMdProductById(productId);
        return product == null || product.getCostPrice() == null ? BigDecimal.ZERO : product.getCostPrice();
    }

    private Long resolveDefaultLocationForWarehouse(Long warehouseId)
    {
        if (mdLocationMapper == null || warehouseId == null)
        {
            return null;
        }
        MdLocation q = new MdLocation();
        q.setWarehouseId(warehouseId);
        List<MdLocation> locations = mdLocationMapper.selectMdLocationList(q);
        return locations != null && !locations.isEmpty() ? locations.get(0).getLocationId() : null;
    }

    private BigDecimal safeAvailable(WmsStock stock)
    {
        BigDecimal q = stock.getQuantity() == null ? BigDecimal.ZERO : stock.getQuantity();
        BigDecimal l = stock.getLockedQuantity() == null ? BigDecimal.ZERO : stock.getLockedQuantity();
        BigDecimal f = stock.getFrozenQuantity() == null ? BigDecimal.ZERO : stock.getFrozenQuantity();
        return q.subtract(l).subtract(f);
    }

    private BigDecimal[] deductDirectly(WmsStock outStock, BigDecimal stepQty)
    {
        BigDecimal before = outStock.getQuantity() == null ? BigDecimal.ZERO : outStock.getQuantity();
        BigDecimal after = before.subtract(stepQty);
        outStock.setQuantity(after);
        wmsStockMapper.updateWmsStock(outStock);
        return new BigDecimal[] { before, after };
    }
}
