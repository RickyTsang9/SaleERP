package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.MdProduct;
import com.ruoyi.business.domain.WmsInbound;
import com.ruoyi.business.domain.WmsInboundItem;
import com.ruoyi.business.domain.WmsPurchaseOrder;
import com.ruoyi.business.domain.WmsPurchaseOrderItem;
import com.ruoyi.business.domain.WmsStock;
import com.ruoyi.business.domain.WmsStockLog;
import com.ruoyi.business.mapper.MdProductMapper;
import com.ruoyi.business.mapper.WmsInboundMapper;
import com.ruoyi.business.mapper.WmsInboundItemMapper;
import com.ruoyi.business.mapper.WmsStockLogMapper;
import com.ruoyi.business.mapper.WmsStockMapper;
import com.ruoyi.business.service.IWmsPurchaseOrderService;
import com.ruoyi.business.service.IWmsInboundService;
import com.ruoyi.business.service.IWmsStockService;
import com.ruoyi.business.service.support.BizComplianceHelper;
import com.ruoyi.common.annotation.DataScope;
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

    @Autowired(required = false)
    private IWmsStockService wmsStockService;

    @Autowired(required = false)
    private MdProductMapper mdProductMapper;

    @Autowired(required = false)
    private BizComplianceHelper complianceHelper;

    @Override
    public WmsInbound selectWmsInboundById(Long inboundId)
    {
        return wmsInboundMapper.selectWmsInboundById(inboundId);
    }

    @Override
    @DataScope(deptAlias = "su", userAlias = "su", permission = "business:inbound:list")
    public List<WmsInbound> selectWmsInboundList(WmsInbound wmsInbound)
    {
        return wmsInboundMapper.selectWmsInboundList(wmsInbound);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertWmsInbound(WmsInbound wmsInbound)
    {
        if (wmsInbound.getPurchaseOrderId() != null)
        {
            WmsInbound activeQuery = new WmsInbound();
            activeQuery.setPurchaseOrderId(wmsInbound.getPurchaseOrderId());
            List<WmsInbound> existingList = wmsInboundMapper.selectWmsInboundList(activeQuery);
            if (existingList != null)
            {
                for (WmsInbound ex : existingList)
                {
                    if (STATUS_DRAFT.equals(ex.getStatus()) || STATUS_SUBMITTED.equals(ex.getStatus()))
                    {
                        throw new ServiceException("该采购订单已存在未审核入库单（" + ex.getInboundNo() + "），请先完成审核或作废后再新建");
                    }
                }
            }
        }
        fillPurchaseOrderInfo(wmsInbound);
        if (complianceHelper != null)
        {
            if (wmsInbound.getSupplierId() != null)
            {
                complianceHelper.assertEntityEnabled("md_supplier", "supplier_id", wmsInbound.getSupplierId(), "供应商");
            }
            if (wmsInbound.getWarehouseId() != null)
            {
                complianceHelper.assertEntityEnabled("md_warehouse", "warehouse_id", wmsInbound.getWarehouseId(), "仓库");
            }
        }
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
        for (WmsInboundItem item : inboundItemList)
        {
            if (item.getProductId() == null || item.getLocationId() == null
                || StringUtils.isEmpty(item.getBatchNo())
                || item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new ServiceException("存在未填写上架库位、批次号或数量的入库明细，请完善后再提交审核");
            }
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
        if (complianceHelper != null)
        {
            complianceHelper.assertPeriodOpen(new Date(), "审核采购入库单");
            complianceHelper.assertNotSelfAudit(databaseInbound.getCreateBy(), "入库单审核");
            if (databaseInbound.getSupplierId() != null)
            {
                complianceHelper.assertEntityEnabled("md_supplier", "supplier_id", databaseInbound.getSupplierId(), "供应商");
            }
            if (databaseInbound.getWarehouseId() != null)
            {
                complianceHelper.assertEntityEnabled("md_warehouse", "warehouse_id", databaseInbound.getWarehouseId(), "仓库");
            }
        }
        List<WmsInboundItem> inboundItemList = wmsInboundItemMapper.selectWmsInboundItemList(buildInboundItemQuery(inboundId));
        if (inboundItemList == null || inboundItemList.isEmpty())
        {
            throw new ServiceException("入库明细不能为空");
        }
        validateLinkedPurchaseOrderItems(databaseInbound, inboundItemList);
        String auditUsername = "system";
        try
        {
            auditUsername = SecurityUtils.getUsername();
        }
        catch (Exception ignored) {}
        for (WmsInboundItem wmsInboundItem : inboundItemList)
        {
            if (wmsInboundItem.getProductId() == null || wmsInboundItem.getLocationId() == null || StringUtils.isEmpty(wmsInboundItem.getBatchNo())
                || wmsInboundItem.getQuantity() == null)
            {
                throw new ServiceException("入库明细信息不完整");
            }
            if (complianceHelper != null)
            {
                complianceHelper.assertEntityEnabled("md_product", "product_id", wmsInboundItem.getProductId(), "商品");
                complianceHelper.assertEntityEnabled("md_location", "location_id", wmsInboundItem.getLocationId(), "库位");
            }
            // 按移动加权平均法重算商品单位成本价（入库前先读取全仓旧库存）
            recalculateMovingWeightedAverageCost(wmsInboundItem, auditUsername);

            BigDecimal[] qtyPair;
            if (wmsStockService != null)
            {
                qtyPair = wmsStockService.increaseStockByKeyAtomically(
                    databaseInbound.getWarehouseId(),
                    wmsInboundItem.getProductId(),
                    wmsInboundItem.getLocationId(),
                    wmsInboundItem.getBatchNo(),
                    wmsInboundItem.getExpireDate(),
                    wmsInboundItem.getQuantity(),
                    auditUsername
                );
            }
            else
            {
                WmsStock stockQuery = new WmsStock();
                stockQuery.setWarehouseId(databaseInbound.getWarehouseId());
                stockQuery.setProductId(wmsInboundItem.getProductId());
                stockQuery.setLocationId(wmsInboundItem.getLocationId());
                stockQuery.setBatchNo(wmsInboundItem.getBatchNo());
                WmsStock databaseStock = wmsStockMapper.selectWmsStockByKey(stockQuery);
                BigDecimal beforeQuantity = databaseStock == null || databaseStock.getQuantity() == null ? BigDecimal.ZERO : databaseStock.getQuantity();
                BigDecimal afterQuantity = beforeQuantity.add(wmsInboundItem.getQuantity());
                if (databaseStock == null)
                {
                    databaseStock = new WmsStock();
                    databaseStock.setWarehouseId(databaseInbound.getWarehouseId());
                    databaseStock.setProductId(wmsInboundItem.getProductId());
                    databaseStock.setLocationId(wmsInboundItem.getLocationId());
                    databaseStock.setBatchNo(wmsInboundItem.getBatchNo());
                    databaseStock.setExpireDate(wmsInboundItem.getExpireDate());
                    databaseStock.setQuantity(afterQuantity);
                    databaseStock.setLockedQuantity(BigDecimal.ZERO);
                    databaseStock.setFrozenQuantity(BigDecimal.ZERO);
                    databaseStock.setVersion(1);
                    wmsStockMapper.insertWmsStock(databaseStock);
                }
                else
                {
                    if (wmsInboundItem.getExpireDate() != null)
                    {
                        databaseStock.setExpireDate(wmsInboundItem.getExpireDate());
                    }
                    databaseStock.setQuantity(afterQuantity);
                    wmsStockMapper.updateWmsStock(databaseStock);
                }
                qtyPair = new BigDecimal[] { beforeQuantity, afterQuantity };
            }
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
            wmsStockLog.setBeforeQty(qtyPair[0]);
            wmsStockLog.setAfterQty(qtyPair[1]);
            wmsStockLogMapper.insertWmsStockLog(wmsStockLog);
        }
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
    public int rejectWmsInbound(Long inboundId, String rejectReason)
    {
        WmsInbound databaseInbound = wmsInboundMapper.selectWmsInboundById(inboundId);
        if (databaseInbound == null)
        {
            throw new ServiceException("入库单不存在");
        }
        if (!STATUS_SUBMITTED.equals(databaseInbound.getStatus()))
        {
            throw new ServiceException("仅已提交待审状态的入库单允许驳回");
        }
        String reason = StringUtils.isEmpty(rejectReason) ? "审核驳回重改" : rejectReason.trim();
        int updateCount = wmsInboundMapper.rejectWmsInboundStatus(inboundId, reason, SecurityUtils.getUsername());
        if (updateCount == 0)
        {
            throw new ServiceException("入库单状态已变更，请刷新后重试");
        }
        return updateCount;
    }

    /**
     * 按移动加权平均法重算商品单位成本价：C_new = (max(Q_old,0) * C_old + Q_in * C_in) / (max(Q_old,0) + Q_in)
     */
    private void recalculateMovingWeightedAverageCost(WmsInboundItem inboundItem, String operatorName)
    {
        if (mdProductMapper == null || wmsStockMapper == null || inboundItem == null
            || inboundItem.getProductId() == null || inboundItem.getPrice() == null
            || inboundItem.getPrice().compareTo(BigDecimal.ZERO) <= 0
            || inboundItem.getQuantity() == null || inboundItem.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
        {
            return;
        }
        MdProduct product = mdProductMapper.selectMdProductById(inboundItem.getProductId());
        if (product == null)
        {
            return;
        }
        BigDecimal oldTotalStock = wmsStockMapper.selectTotalProductStockQuantity(inboundItem.getProductId());
        if (oldTotalStock == null || oldTotalStock.compareTo(BigDecimal.ZERO) < 0)
        {
            oldTotalStock = BigDecimal.ZERO;
        }
        BigDecimal oldCost = (product.getCostPrice() == null || product.getCostPrice().compareTo(BigDecimal.ZERO) <= 0)
            ? inboundItem.getPrice()
            : product.getCostPrice();
        BigDecimal inQty = inboundItem.getQuantity();
        BigDecimal inPrice = inboundItem.getPrice();
        BigDecimal denom = oldTotalStock.add(inQty);
        if (denom.compareTo(BigDecimal.ZERO) <= 0)
        {
            return;
        }
        BigDecimal newWeightedCost = oldTotalStock.multiply(oldCost)
            .add(inQty.multiply(inPrice))
            .divide(denom, 2, RoundingMode.HALF_UP);
        MdProduct updateProduct = new MdProduct();
        updateProduct.setProductId(product.getProductId());
        updateProduct.setCostPrice(newWeightedCost);
        updateProduct.setUpdateBy(operatorName);
        mdProductMapper.updateMdProduct(updateProduct);
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

    /**
     * 删除采购入库单
     *
     * @param inboundId 入库单编号
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * 批量删除采购入库单
     *
     * @param inboundIds 入库单编号集合
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
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
        Map<Long, BigDecimal> purchaseOrderItemQuantityMap = new HashMap<Long, BigDecimal>();
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
            BigDecimal accumulatedInboundQuantity = accumulateInboundQuantity(
                purchaseOrderItemQuantityMap, wmsInboundItem.getPurchaseOrderItemId(), wmsInboundItem.getQuantity());
            if (accumulatedInboundQuantity.compareTo(remainQuantity) > 0)
            {
                throw new ServiceException("第" + (inboundItemIndex + 1) + "行对应来源采购明细的累计入库数量不能超过待入库数量");
            }
        }
    }

    /**
     * 按来源采购明细累计本次入库数量
     *
     * @param purchaseOrderItemQuantityMap 来源采购明细累计入库数量映射
     * @param purchaseOrderItemId 来源采购明细编号
     * @param currentInboundQuantity 当前入库数量
     * @return 累计入库数量
     */
    private BigDecimal accumulateInboundQuantity(Map<Long, BigDecimal> purchaseOrderItemQuantityMap, Long purchaseOrderItemId,
        BigDecimal currentInboundQuantity)
    {
        BigDecimal historyInboundQuantity = purchaseOrderItemQuantityMap.get(purchaseOrderItemId);
        if (historyInboundQuantity == null)
        {
            historyInboundQuantity = BigDecimal.ZERO;
        }
        BigDecimal validCurrentInboundQuantity =
            currentInboundQuantity == null ? BigDecimal.ZERO : currentInboundQuantity;
        BigDecimal accumulatedInboundQuantity = historyInboundQuantity.add(validCurrentInboundQuantity);
        purchaseOrderItemQuantityMap.put(purchaseOrderItemId, accumulatedInboundQuantity);
        return accumulatedInboundQuantity;
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
