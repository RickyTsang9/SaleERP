package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.WmsInbound;
import com.ruoyi.business.domain.WmsInboundItem;
import com.ruoyi.business.domain.WmsPurchaseOrder;
import com.ruoyi.business.domain.WmsPurchaseOrderItem;
import com.ruoyi.business.domain.WmsStock;
import com.ruoyi.business.domain.WmsStockLog;
import com.ruoyi.business.mapper.WmsInboundMapper;
import com.ruoyi.business.mapper.WmsInboundItemMapper;
import com.ruoyi.business.mapper.WmsStockLogMapper;
import com.ruoyi.business.mapper.WmsStockMapper;
import com.ruoyi.business.service.IWmsPurchaseOrderService;
import com.ruoyi.business.service.IWmsInboundService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

@Service
public class WmsInboundServiceImpl implements IWmsInboundService
{
    private static final String STATUS_DRAFT = "draft";

    private static final String STATUS_SUBMITTED = "submitted";

    private static final String STATUS_AUDITED = "audited";

    private static final String STATUS_CANCELLED = "cancelled";

    @Autowired
    private WmsInboundMapper wmsInboundMapper;

    @Autowired
    private WmsInboundItemMapper wmsInboundItemMapper;

    @Autowired
    private WmsStockMapper wmsStockMapper;

    @Autowired
    private WmsStockLogMapper wmsStockLogMapper;

    @Autowired
    private IWmsPurchaseOrderService wmsPurchaseOrderService;

    @Override
    public WmsInbound selectWmsInboundById(Long inboundId)
    {
        return wmsInboundMapper.selectWmsInboundById(inboundId);
    }

    @Override
    public List<WmsInbound> selectWmsInboundList(WmsInbound wmsInbound)
    {
        return wmsInboundMapper.selectWmsInboundList(wmsInbound);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertWmsInbound(WmsInbound wmsInbound)
    {
        fillPurchaseOrderInfo(wmsInbound);
        if (wmsInbound.getInboundNo() == null || wmsInbound.getInboundNo().isEmpty())
        {
            wmsInbound.setInboundNo(generateInboundNo());
        }
        wmsInbound.setStatus(STATUS_DRAFT);
        int insertRows = wmsInboundMapper.insertWmsInbound(wmsInbound);
        initializeLinkedPurchaseOrderItems(wmsInbound);
        return insertRows;
    }

    @Override
    public int updateWmsInbound(WmsInbound wmsInbound)
    {
        WmsInbound databaseInbound = wmsInboundMapper.selectWmsInboundById(wmsInbound.getInboundId());
        if (databaseInbound == null)
        {
            throw new ServiceException("入库单不存在");
        }
        if (!STATUS_DRAFT.equals(databaseInbound.getStatus()))
        {
            throw new ServiceException("仅草稿状态入库单允许修改");
        }
        if (!Objects.equals(databaseInbound.getPurchaseOrderId(), wmsInbound.getPurchaseOrderId()))
        {
            throw new ServiceException("来源采购单创建后不允许修改");
        }
        fillPurchaseOrderInfo(wmsInbound);
        wmsInbound.setStatus(databaseInbound.getStatus());
        return wmsInboundMapper.updateWmsInbound(wmsInbound);
    }

    @Override
    public int submitWmsInbound(Long inboundId)
    {
        WmsInbound databaseInbound = wmsInboundMapper.selectWmsInboundById(inboundId);
        if (databaseInbound == null)
        {
            throw new ServiceException("入库单不存在");
        }
        if (!STATUS_DRAFT.equals(databaseInbound.getStatus()))
        {
            throw new ServiceException("仅草稿状态入库单允许提交");
        }
        List<WmsInboundItem> inboundItemList = wmsInboundItemMapper.selectWmsInboundItemList(buildInboundItemQuery(inboundId));
        if (inboundItemList == null || inboundItemList.isEmpty())
        {
            throw new ServiceException("入库明细不能为空");
        }
        validateLinkedPurchaseOrderItems(databaseInbound, inboundItemList);
        int updateCount = wmsInboundMapper.updateWmsInboundStatus(
            inboundId, STATUS_DRAFT, STATUS_SUBMITTED, null, SecurityUtils.getUsername());
        if (updateCount == 0)
        {
            throw new ServiceException("入库单状态已变更，请刷新后重试");
        }
        return updateCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditWmsInbound(Long inboundId)
    {
        WmsInbound databaseInbound = wmsInboundMapper.selectWmsInboundById(inboundId);
        if (databaseInbound == null)
        {
            throw new ServiceException("入库单不存在");
        }
        if (!STATUS_SUBMITTED.equals(databaseInbound.getStatus()))
        {
            throw new ServiceException("仅已提交入库单允许审核");
        }
        List<WmsInboundItem> inboundItemList = wmsInboundItemMapper.selectWmsInboundItemList(buildInboundItemQuery(inboundId));
        if (inboundItemList == null || inboundItemList.isEmpty())
        {
            throw new ServiceException("入库明细不能为空");
        }
        validateLinkedPurchaseOrderItems(databaseInbound, inboundItemList);
        for (WmsInboundItem wmsInboundItem : inboundItemList)
        {
            if (wmsInboundItem.getProductId() == null || wmsInboundItem.getLocationId() == null || StringUtils.isEmpty(wmsInboundItem.getBatchNo())
                || wmsInboundItem.getQuantity() == null)
            {
                throw new ServiceException("入库明细信息不完整");
            }
            WmsStock stockQuery = new WmsStock();
            stockQuery.setWarehouseId(databaseInbound.getWarehouseId());
            stockQuery.setProductId(wmsInboundItem.getProductId());
            stockQuery.setLocationId(wmsInboundItem.getLocationId());
            stockQuery.setBatchNo(wmsInboundItem.getBatchNo());
            WmsStock databaseStock = wmsStockMapper.selectWmsStockByKey(stockQuery);
            BigDecimal beforeQuantity = BigDecimal.ZERO;
            if (databaseStock == null)
            {
                databaseStock = new WmsStock();
                databaseStock.setWarehouseId(databaseInbound.getWarehouseId());
                databaseStock.setProductId(wmsInboundItem.getProductId());
                databaseStock.setLocationId(wmsInboundItem.getLocationId());
                databaseStock.setBatchNo(wmsInboundItem.getBatchNo());
                databaseStock.setQuantity(BigDecimal.ZERO);
                databaseStock.setLockedQuantity(BigDecimal.ZERO);
                databaseStock.setFrozenQuantity(BigDecimal.ZERO);
                databaseStock.setVersion(0);
                wmsStockMapper.insertWmsStock(databaseStock);
            }
            else
            {
                beforeQuantity = databaseStock.getQuantity() == null ? BigDecimal.ZERO : databaseStock.getQuantity();
            }
            BigDecimal afterQuantity = beforeQuantity.add(wmsInboundItem.getQuantity());
            databaseStock.setQuantity(afterQuantity);
            wmsStockMapper.updateWmsStock(databaseStock);
            WmsStockLog wmsStockLog = new WmsStockLog();
            wmsStockLog.setWarehouseId(databaseInbound.getWarehouseId());
            wmsStockLog.setProductId(wmsInboundItem.getProductId());
            wmsStockLog.setLocationId(wmsInboundItem.getLocationId());
            wmsStockLog.setBatchNo(wmsInboundItem.getBatchNo());
            wmsStockLog.setBillType("inbound");
            wmsStockLog.setBillId(inboundId);
            wmsStockLog.setBillNo(databaseInbound.getInboundNo());
            wmsStockLog.setInOut("in");
            wmsStockLog.setQuantity(wmsInboundItem.getQuantity());
            wmsStockLog.setPrice(wmsInboundItem.getPrice());
            wmsStockLog.setAmount(wmsInboundItem.getAmount());
            wmsStockLog.setBeforeQty(beforeQuantity);
            wmsStockLog.setAfterQty(afterQuantity);
            wmsStockLogMapper.insertWmsStockLog(wmsStockLog);
        }
        String auditUsername = SecurityUtils.getUsername();
        int updateCount = wmsInboundMapper.updateWmsInboundStatus(
            inboundId, STATUS_SUBMITTED, STATUS_AUDITED, auditUsername, auditUsername);
        if (updateCount == 0)
        {
            throw new ServiceException("入库单状态已变更，请刷新后重试");
        }
        if (databaseInbound.getPurchaseOrderId() != null)
        {
            wmsPurchaseOrderService.refreshPurchaseOrderInboundProgress(databaseInbound.getPurchaseOrderId());
        }
        return updateCount;
    }

    @Override
    public int cancelWmsInbound(Long inboundId)
    {
        WmsInbound databaseInbound = wmsInboundMapper.selectWmsInboundById(inboundId);
        if (databaseInbound == null)
        {
            throw new ServiceException("入库单不存在");
        }
        if (STATUS_CANCELLED.equals(databaseInbound.getStatus()))
        {
            throw new ServiceException("入库单已作废，无需重复作废");
        }
        if (STATUS_AUDITED.equals(databaseInbound.getStatus()))
        {
            throw new ServiceException("已审核入库单不允许作废");
        }
        int updateCount = wmsInboundMapper.updateWmsInboundStatus(
            inboundId, databaseInbound.getStatus(), STATUS_CANCELLED, null, SecurityUtils.getUsername());
        if (updateCount == 0)
        {
            throw new ServiceException("入库单状态已变更，请刷新后重试");
        }
        return updateCount;
    }

    @Override
    public int deleteWmsInboundById(Long inboundId)
    {
        WmsInbound databaseInbound = wmsInboundMapper.selectWmsInboundById(inboundId);
        if (databaseInbound == null)
        {
            return 0;
        }
        if (!STATUS_DRAFT.equals(databaseInbound.getStatus()))
        {
            throw new ServiceException("仅草稿状态入库单允许删除");
        }
        wmsInboundItemMapper.deleteWmsInboundItemByInboundIds(new Long[] { inboundId });
        return wmsInboundMapper.deleteWmsInboundById(inboundId);
    }

    @Override
    public int deleteWmsInboundByIds(Long[] inboundIds)
    {
        for (Long inboundId : inboundIds)
        {
            WmsInbound databaseInbound = wmsInboundMapper.selectWmsInboundById(inboundId);
            if (databaseInbound == null)
            {
                continue;
            }
            if (!STATUS_DRAFT.equals(databaseInbound.getStatus()))
            {
                throw new ServiceException("仅草稿状态入库单允许删除，入库单号：" + databaseInbound.getInboundNo());
            }
        }
        wmsInboundItemMapper.deleteWmsInboundItemByInboundIds(inboundIds);
        return wmsInboundMapper.deleteWmsInboundByIds(inboundIds);
    }

    private WmsInboundItem buildInboundItemQuery(Long inboundId)
    {
        WmsInboundItem wmsInboundItem = new WmsInboundItem();
        wmsInboundItem.setInboundId(inboundId);
        return wmsInboundItem;
    }

    private String generateInboundNo()
    {
        String dateValue = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String noPrefix = "RK" + dateValue;
        String maxInboundNo = wmsInboundMapper.selectMaxInboundNoByPrefix(noPrefix);
        if (maxInboundNo == null || maxInboundNo.isEmpty())
        {
            return noPrefix + "0001";
        }
        String sequenceValue = maxInboundNo.substring(noPrefix.length());
        int nextSequenceValue = Integer.parseInt(sequenceValue) + 1;
        return noPrefix + String.format("%04d", nextSequenceValue);
    }

    /**
     * 初始化来源采购单自动带入的入库明细
     *
     * @param wmsInbound 入库单
     */
    private void initializeLinkedPurchaseOrderItems(WmsInbound wmsInbound)
    {
        if (wmsInbound.getInboundId() == null || wmsInbound.getPurchaseOrderId() == null)
        {
            return;
        }
        WmsPurchaseOrder databasePurchaseOrder = wmsPurchaseOrderService.selectWmsPurchaseOrderByPurchaseOrderId(
            wmsInbound.getPurchaseOrderId());
        if (databasePurchaseOrder == null || databasePurchaseOrder.getWmsPurchaseOrderItemList() == null
            || databasePurchaseOrder.getWmsPurchaseOrderItemList().isEmpty())
        {
            throw new ServiceException("来源采购订单不存在可入库明细");
        }
        boolean hasGeneratedInboundItem = false;
        for (WmsPurchaseOrderItem purchaseOrderItem : databasePurchaseOrder.getWmsPurchaseOrderItemList())
        {
            if (!hasPositiveQuantity(purchaseOrderItem.getRemainQuantity()))
            {
                continue;
            }
            WmsInboundItem generatedInboundItem = new WmsInboundItem();
            generatedInboundItem.setInboundId(wmsInbound.getInboundId());
            generatedInboundItem.setPurchaseOrderItemId(purchaseOrderItem.getPurchaseOrderItemId());
            generatedInboundItem.setProductId(purchaseOrderItem.getProductId());
            generatedInboundItem.setQuantity(purchaseOrderItem.getRemainQuantity());
            generatedInboundItem.setPrice(purchaseOrderItem.getPrice());
            generatedInboundItem.setAmount(calculateInboundAmount(purchaseOrderItem.getRemainQuantity(), purchaseOrderItem.getPrice()));
            wmsInboundItemMapper.insertWmsInboundItem(generatedInboundItem);
            hasGeneratedInboundItem = true;
        }
        if (!hasGeneratedInboundItem)
        {
            throw new ServiceException("来源采购订单没有可入库数量");
        }
        wmsInboundMapper.refreshTotalByInboundId(wmsInbound.getInboundId());
    }

    /**
     * 校验来源采购单带出的入库明细是否仍然有效
     *
     * @param databaseInbound 数据库中的入库单
     * @param inboundItemList 入库明细列表
     */
    private void validateLinkedPurchaseOrderItems(WmsInbound databaseInbound, List<WmsInboundItem> inboundItemList)
    {
        if (databaseInbound.getPurchaseOrderId() == null)
        {
            return;
        }
        WmsPurchaseOrder databasePurchaseOrder = wmsPurchaseOrderService.selectWmsPurchaseOrderByPurchaseOrderId(
            databaseInbound.getPurchaseOrderId());
        if (databasePurchaseOrder == null || databasePurchaseOrder.getWmsPurchaseOrderItemList() == null
            || databasePurchaseOrder.getWmsPurchaseOrderItemList().isEmpty())
        {
            throw new ServiceException("来源采购订单不存在可入库明细");
        }
        for (int inboundItemIndex = 0; inboundItemIndex < inboundItemList.size(); inboundItemIndex++)
        {
            WmsInboundItem wmsInboundItem = inboundItemList.get(inboundItemIndex);
            if (wmsInboundItem.getPurchaseOrderItemId() == null)
            {
                throw new ServiceException("第" + (inboundItemIndex + 1) + "行入库明细缺少来源采购明细");
            }
            WmsPurchaseOrderItem purchaseOrderItem = findPurchaseOrderItem(
                databasePurchaseOrder.getWmsPurchaseOrderItemList(), wmsInboundItem.getPurchaseOrderItemId());
            if (purchaseOrderItem == null)
            {
                throw new ServiceException("第" + (inboundItemIndex + 1) + "行入库明细的来源采购明细不存在");
            }
            if (!Objects.equals(wmsInboundItem.getProductId(), purchaseOrderItem.getProductId()))
            {
                throw new ServiceException("第" + (inboundItemIndex + 1) + "行入库明细商品与来源采购明细不一致");
            }
            if (!hasPositiveQuantity(wmsInboundItem.getQuantity()))
            {
                throw new ServiceException("第" + (inboundItemIndex + 1) + "行入库数量必须大于0");
            }
            BigDecimal remainQuantity = purchaseOrderItem.getRemainQuantity() == null ? BigDecimal.ZERO : purchaseOrderItem.getRemainQuantity();
            if (wmsInboundItem.getQuantity().compareTo(remainQuantity) > 0)
            {
                throw new ServiceException("第" + (inboundItemIndex + 1) + "行入库数量不能超过待入库数量");
            }
        }
    }

    /**
     * 根据来源采购订单补齐和校验入库单的采购关联信息
     *
     * @param wmsInbound 入库单
     */
    private void fillPurchaseOrderInfo(WmsInbound wmsInbound)
    {
        if (wmsInbound.getPurchaseOrderId() == null)
        {
            wmsInbound.setPurchaseOrderNo(null);
            return;
        }
        WmsPurchaseOrder databasePurchaseOrder = wmsPurchaseOrderService.validatePurchaseOrderForInbound(
            wmsInbound.getPurchaseOrderId(), wmsInbound.getSupplierId());
        wmsInbound.setPurchaseOrderNo(databasePurchaseOrder.getOrderNo());
        wmsInbound.setSupplierId(databasePurchaseOrder.getSupplierId());
        if (StringUtils.isEmpty(wmsInbound.getInboundType()))
        {
            wmsInbound.setInboundType("purchase");
        }
    }

    /**
     * 根据采购订单明细编号查找对应的采购明细
     *
     * @param purchaseOrderItemList 采购订单明细列表
     * @param purchaseOrderItemId 采购订单明细编号
     * @return 采购订单明细
     */
    private WmsPurchaseOrderItem findPurchaseOrderItem(List<WmsPurchaseOrderItem> purchaseOrderItemList, Long purchaseOrderItemId)
    {
        for (WmsPurchaseOrderItem purchaseOrderItem : purchaseOrderItemList)
        {
            if (Objects.equals(purchaseOrderItem.getPurchaseOrderItemId(), purchaseOrderItemId))
            {
                return purchaseOrderItem;
            }
        }
        return null;
    }

    /**
     * 判断数量是否大于0
     *
     * @param quantityValue 数量
     * @return 是否为正数
     */
    private boolean hasPositiveQuantity(BigDecimal quantityValue)
    {
        return quantityValue != null && quantityValue.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 计算入库明细金额
     *
     * @param quantityValue 数量
     * @param priceValue 单价
     * @return 金额
     */
    private BigDecimal calculateInboundAmount(BigDecimal quantityValue, BigDecimal priceValue)
    {
        BigDecimal safeQuantityValue = quantityValue == null ? BigDecimal.ZERO : quantityValue;
        BigDecimal safePriceValue = priceValue == null ? BigDecimal.ZERO : priceValue;
        return safeQuantityValue.multiply(safePriceValue);
    }
}
