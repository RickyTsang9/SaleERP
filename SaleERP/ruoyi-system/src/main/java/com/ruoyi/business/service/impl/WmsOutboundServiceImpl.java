package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.WmsOutbound;
import com.ruoyi.business.domain.WmsOutboundItem;
import com.ruoyi.business.domain.WmsSaleOrder;
import com.ruoyi.business.domain.WmsSaleOrderItem;
import com.ruoyi.business.domain.WmsStock;
import com.ruoyi.business.domain.WmsStockLog;
import com.ruoyi.business.mapper.WmsOutboundItemMapper;
import com.ruoyi.business.mapper.WmsOutboundMapper;
import com.ruoyi.business.mapper.WmsSaleOrderItemMapper;
import com.ruoyi.business.mapper.WmsSaleOrderMapper;
import com.ruoyi.business.mapper.WmsStockLogMapper;
import com.ruoyi.business.mapper.WmsStockMapper;
import com.ruoyi.business.service.IWmsOutboundService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

@Service
public class WmsOutboundServiceImpl implements IWmsOutboundService
{
    private static final String STATUS_DRAFT = "draft";

    private static final String STATUS_SUBMITTED = "submitted";

    private static final String STATUS_AUDITED = "audited";

    private static final String STATUS_CANCELLED = "cancelled";

    @Autowired
    private WmsOutboundMapper wmsOutboundMapper;

    @Autowired
    private WmsOutboundItemMapper wmsOutboundItemMapper;

    @Autowired
    private WmsSaleOrderMapper wmsSaleOrderMapper;

    @Autowired
    private WmsSaleOrderItemMapper wmsSaleOrderItemMapper;

    @Autowired
    private WmsStockMapper wmsStockMapper;

    @Autowired
    private WmsStockLogMapper wmsStockLogMapper;

    /**
     * 查询销售出库单
     *
     * @param outboundId 销售出库单编号
     * @return 销售出库单
     */
    @Override
    public WmsOutbound selectWmsOutboundById(Long outboundId)
    {
        return wmsOutboundMapper.selectWmsOutboundById(outboundId);
    }

    /**
     * 查询销售出库单列表
     *
     * @param wmsOutbound 销售出库单
     * @return 销售出库单集合
     */
    @Override
    public List<WmsOutbound> selectWmsOutboundList(WmsOutbound wmsOutbound)
    {
        return wmsOutboundMapper.selectWmsOutboundList(wmsOutbound);
    }

    /**
     * 新增销售出库单
     *
     * @param wmsOutbound 销售出库单
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertWmsOutbound(WmsOutbound wmsOutbound)
    {
        fillSaleOrderInfo(wmsOutbound);
        if (wmsOutbound.getOutboundNo() == null || wmsOutbound.getOutboundNo().isEmpty())
        {
            wmsOutbound.setOutboundNo(generateOutboundNo());
        }
        wmsOutbound.setStatus(STATUS_DRAFT);
        int insertRows = wmsOutboundMapper.insertWmsOutbound(wmsOutbound);
        initializeLinkedSaleOrderItems(wmsOutbound);
        return insertRows;
    }

    /**
     * 修改销售出库单
     *
     * @param wmsOutbound 销售出库单
     * @return 结果
     */
    @Override
    public int updateWmsOutbound(WmsOutbound wmsOutbound)
    {
        WmsOutbound databaseOutbound = wmsOutboundMapper.selectWmsOutboundById(wmsOutbound.getOutboundId());
        if (databaseOutbound == null)
        {
            throw new ServiceException("出库单不存在");
        }
        if (!STATUS_DRAFT.equals(databaseOutbound.getStatus()))
        {
            throw new ServiceException("仅草稿状态出库单允许修改");
        }
        if (!Objects.equals(databaseOutbound.getSaleOrderId(), wmsOutbound.getSaleOrderId()))
        {
            throw new ServiceException("来源销售单创建后不允许修改");
        }
        fillSaleOrderInfo(wmsOutbound);
        wmsOutbound.setStatus(databaseOutbound.getStatus());
        return wmsOutboundMapper.updateWmsOutbound(wmsOutbound);
    }

    /**
     * 提交销售出库单
     *
     * @param outboundId 销售出库单编号
     * @return 结果
     */
    @Override
    public int submitWmsOutbound(Long outboundId)
    {
        WmsOutbound databaseOutbound = wmsOutboundMapper.selectWmsOutboundById(outboundId);
        if (databaseOutbound == null)
        {
            throw new ServiceException("出库单不存在");
        }
        if (!STATUS_DRAFT.equals(databaseOutbound.getStatus()))
        {
            throw new ServiceException("仅草稿状态出库单允许提交");
        }
        List<WmsOutboundItem> outboundItemList = wmsOutboundItemMapper.selectWmsOutboundItemList(buildOutboundItemQuery(outboundId));
        if (outboundItemList == null || outboundItemList.isEmpty())
        {
            throw new ServiceException("出库明细不能为空");
        }
        validateLinkedSaleOrderItems(databaseOutbound, outboundItemList);
        int updateCount = wmsOutboundMapper.updateWmsOutboundStatus(
            outboundId, STATUS_DRAFT, STATUS_SUBMITTED, null, SecurityUtils.getUsername());
        if (updateCount == 0)
        {
            throw new ServiceException("出库单状态已变更，请刷新后重试");
        }
        return updateCount;
    }

    /**
     * 审核销售出库单
     *
     * @param outboundId 销售出库单编号
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditWmsOutbound(Long outboundId)
    {
        WmsOutbound databaseOutbound = wmsOutboundMapper.selectWmsOutboundById(outboundId);
        if (databaseOutbound == null)
        {
            throw new ServiceException("出库单不存在");
        }
        if (!STATUS_SUBMITTED.equals(databaseOutbound.getStatus()))
        {
            throw new ServiceException("仅已提交出库单允许审核");
        }
        List<WmsOutboundItem> outboundItemList = wmsOutboundItemMapper.selectWmsOutboundItemList(buildOutboundItemQuery(outboundId));
        if (outboundItemList == null || outboundItemList.isEmpty())
        {
            throw new ServiceException("出库明细不能为空");
        }
        validateLinkedSaleOrderItems(databaseOutbound, outboundItemList);
        for (WmsOutboundItem wmsOutboundItem : outboundItemList)
        {
            if (wmsOutboundItem.getProductId() == null || wmsOutboundItem.getLocationId() == null || StringUtils.isEmpty(wmsOutboundItem.getBatchNo())
                || wmsOutboundItem.getQuantity() == null)
            {
                throw new ServiceException("出库明细信息不完整");
            }
            if (databaseOutbound.getSaleOrderId() != null)
            {
                continue;
            }
            WmsStock stockQuery = new WmsStock();
            stockQuery.setWarehouseId(databaseOutbound.getWarehouseId());
            stockQuery.setProductId(wmsOutboundItem.getProductId());
            stockQuery.setLocationId(wmsOutboundItem.getLocationId());
            stockQuery.setBatchNo(wmsOutboundItem.getBatchNo());
            WmsStock databaseStock = wmsStockMapper.selectWmsStockByKey(stockQuery);
            if (databaseStock == null || databaseStock.getQuantity() == null)
            {
                throw new ServiceException("库存不存在，商品编号：" + wmsOutboundItem.getProductId());
            }
            BigDecimal beforeQuantity = databaseStock.getQuantity();
            if (beforeQuantity.compareTo(wmsOutboundItem.getQuantity()) < 0)
            {
                throw new ServiceException("库存不足，商品编号：" + wmsOutboundItem.getProductId());
            }
            BigDecimal afterQuantity = beforeQuantity.subtract(wmsOutboundItem.getQuantity());
            databaseStock.setQuantity(afterQuantity);
            wmsStockMapper.updateWmsStock(databaseStock);
            WmsStockLog wmsStockLog = new WmsStockLog();
            wmsStockLog.setWarehouseId(databaseOutbound.getWarehouseId());
            wmsStockLog.setProductId(wmsOutboundItem.getProductId());
            wmsStockLog.setLocationId(wmsOutboundItem.getLocationId());
            wmsStockLog.setBatchNo(wmsOutboundItem.getBatchNo());
            wmsStockLog.setBillType("outbound");
            wmsStockLog.setBillId(outboundId);
            wmsStockLog.setBillNo(databaseOutbound.getOutboundNo());
            wmsStockLog.setInOut("out");
            wmsStockLog.setQuantity(wmsOutboundItem.getQuantity());
            wmsStockLog.setPrice(wmsOutboundItem.getPrice());
            wmsStockLog.setAmount(wmsOutboundItem.getAmount());
            wmsStockLog.setBeforeQty(beforeQuantity);
            wmsStockLog.setAfterQty(afterQuantity);
            wmsStockLogMapper.insertWmsStockLog(wmsStockLog);
        }
        String auditUsername = SecurityUtils.getUsername();
        int updateCount = wmsOutboundMapper.updateWmsOutboundStatus(
            outboundId, STATUS_SUBMITTED, STATUS_AUDITED, auditUsername, auditUsername);
        if (updateCount == 0)
        {
            throw new ServiceException("出库单状态已变更，请刷新后重试");
        }
        return updateCount;
    }

    /**
     * 作废销售出库单
     *
     * @param outboundId 销售出库单编号
     * @return 结果
     */
    @Override
    public int cancelWmsOutbound(Long outboundId)
    {
        WmsOutbound databaseOutbound = wmsOutboundMapper.selectWmsOutboundById(outboundId);
        if (databaseOutbound == null)
        {
            throw new ServiceException("出库单不存在");
        }
        if (STATUS_CANCELLED.equals(databaseOutbound.getStatus()))
        {
            throw new ServiceException("出库单已作废，无需重复作废");
        }
        if (STATUS_AUDITED.equals(databaseOutbound.getStatus()))
        {
            throw new ServiceException("已审核出库单不允许作废");
        }
        int updateCount = wmsOutboundMapper.updateWmsOutboundStatus(
            outboundId, databaseOutbound.getStatus(), STATUS_CANCELLED, null, SecurityUtils.getUsername());
        if (updateCount == 0)
        {
            throw new ServiceException("出库单状态已变更，请刷新后重试");
        }
        return updateCount;
    }

    /**
     * 删除销售出库单
     *
     * @param outboundId 销售出库单编号
     * @return 结果
     */
    @Override
    public int deleteWmsOutboundById(Long outboundId)
    {
        WmsOutbound databaseOutbound = wmsOutboundMapper.selectWmsOutboundById(outboundId);
        if (databaseOutbound == null)
        {
            return 0;
        }
        if (!STATUS_DRAFT.equals(databaseOutbound.getStatus()))
        {
            throw new ServiceException("仅草稿状态出库单允许删除");
        }
        wmsOutboundItemMapper.deleteWmsOutboundItemByOutboundIds(new Long[] { outboundId });
        return wmsOutboundMapper.deleteWmsOutboundById(outboundId);
    }

    /**
     * 批量删除销售出库单
     *
     * @param outboundIds 销售出库单编号集合
     * @return 结果
     */
    @Override
    public int deleteWmsOutboundByIds(Long[] outboundIds)
    {
        for (Long outboundId : outboundIds)
        {
            WmsOutbound databaseOutbound = wmsOutboundMapper.selectWmsOutboundById(outboundId);
            if (databaseOutbound == null)
            {
                continue;
            }
            if (!STATUS_DRAFT.equals(databaseOutbound.getStatus()))
            {
                throw new ServiceException("仅草稿状态出库单允许删除，出库单号：" + databaseOutbound.getOutboundNo());
            }
        }
        wmsOutboundItemMapper.deleteWmsOutboundItemByOutboundIds(outboundIds);
        return wmsOutboundMapper.deleteWmsOutboundByIds(outboundIds);
    }

    /**
     * 构造查询出库明细的条件对象
     *
     * @param outboundId 出库单编号
     * @return 出库明细查询条件
     */
    private WmsOutboundItem buildOutboundItemQuery(Long outboundId)
    {
        WmsOutboundItem wmsOutboundItem = new WmsOutboundItem();
        wmsOutboundItem.setOutboundId(outboundId);
        return wmsOutboundItem;
    }

    /**
     * 生成销售出库单号
     *
     * @return 销售出库单号
     */
    private String generateOutboundNo()
    {
        String dateValue = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String noPrefix = "CK" + dateValue;
        String maxOutboundNo = wmsOutboundMapper.selectMaxOutboundNoByPrefix(noPrefix);
        if (maxOutboundNo == null || maxOutboundNo.isEmpty())
        {
            return noPrefix + "0001";
        }
        String sequenceValue = maxOutboundNo.substring(noPrefix.length());
        int nextSequenceValue = Integer.parseInt(sequenceValue) + 1;
        return noPrefix + String.format("%04d", nextSequenceValue);
    }

    /**
     * 初始化来源销售单自动带入的出库明细
     *
     * @param wmsOutbound 出库单
     */
    private void initializeLinkedSaleOrderItems(WmsOutbound wmsOutbound)
    {
        if (wmsOutbound.getOutboundId() == null || wmsOutbound.getSaleOrderId() == null)
        {
            return;
        }
        List<WmsSaleOrderItem> saleOrderItemList = wmsSaleOrderItemMapper.selectWmsSaleOrderItemsByOrderId(wmsOutbound.getSaleOrderId());
        if (saleOrderItemList == null || saleOrderItemList.isEmpty())
        {
            throw new ServiceException("来源销售订单不存在可出库明细");
        }
        boolean hasGeneratedOutboundItem = false;
        for (WmsSaleOrderItem saleOrderItem : saleOrderItemList)
        {
            if (!hasPositiveQuantity(saleOrderItem.getRemainQuantity()))
            {
                continue;
            }
            WmsOutboundItem generatedOutboundItem = new WmsOutboundItem();
            generatedOutboundItem.setOutboundId(wmsOutbound.getOutboundId());
            generatedOutboundItem.setSaleOrderItemId(saleOrderItem.getSaleOrderItemId());
            generatedOutboundItem.setProductId(saleOrderItem.getProductId());
            generatedOutboundItem.setLocationId(saleOrderItem.getLocationId());
            generatedOutboundItem.setBatchNo(saleOrderItem.getBatchNo());
            generatedOutboundItem.setQuantity(saleOrderItem.getRemainQuantity());
            generatedOutboundItem.setPrice(saleOrderItem.getPrice());
            generatedOutboundItem.setAmount(calculateOutboundAmount(saleOrderItem.getRemainQuantity(), saleOrderItem.getPrice()));
            wmsOutboundItemMapper.insertWmsOutboundItem(generatedOutboundItem);
            hasGeneratedOutboundItem = true;
        }
        if (!hasGeneratedOutboundItem)
        {
            throw new ServiceException("来源销售订单没有可出库数量");
        }
        wmsOutboundMapper.refreshTotalByOutboundId(wmsOutbound.getOutboundId());
    }

    /**
     * 校验来源销售单带出的出库明细是否仍然有效
     *
     * @param databaseOutbound 数据库中的出库单
     * @param outboundItemList 出库明细列表
     */
    private void validateLinkedSaleOrderItems(WmsOutbound databaseOutbound, List<WmsOutboundItem> outboundItemList)
    {
        if (databaseOutbound.getSaleOrderId() == null)
        {
            return;
        }
        WmsSaleOrder databaseSaleOrder = validateSaleOrderForOutbound(
            databaseOutbound.getSaleOrderId(), databaseOutbound.getCustomerId(), databaseOutbound.getWarehouseId(), databaseOutbound.getOutboundId());
        List<WmsSaleOrderItem> saleOrderItemList = wmsSaleOrderItemMapper.selectWmsSaleOrderItemsByOrderId(databaseSaleOrder.getSaleOrderId());
        if (saleOrderItemList == null || saleOrderItemList.isEmpty())
        {
            throw new ServiceException("来源销售订单不存在可出库明细");
        }
        for (int outboundItemIndex = 0; outboundItemIndex < outboundItemList.size(); outboundItemIndex++)
        {
            WmsOutboundItem wmsOutboundItem = outboundItemList.get(outboundItemIndex);
            if (wmsOutboundItem.getSaleOrderItemId() == null)
            {
                throw new ServiceException("第" + (outboundItemIndex + 1) + "行出库明细缺少来源销售明细");
            }
            WmsSaleOrderItem saleOrderItem = findSaleOrderItem(saleOrderItemList, wmsOutboundItem.getSaleOrderItemId());
            if (saleOrderItem == null)
            {
                throw new ServiceException("第" + (outboundItemIndex + 1) + "行出库明细的来源销售明细不存在");
            }
            if (!Objects.equals(wmsOutboundItem.getProductId(), saleOrderItem.getProductId()))
            {
                throw new ServiceException("第" + (outboundItemIndex + 1) + "行出库明细商品与来源销售明细不一致");
            }
            if (!Objects.equals(wmsOutboundItem.getLocationId(), saleOrderItem.getLocationId()))
            {
                throw new ServiceException("第" + (outboundItemIndex + 1) + "行出库明细库位与来源销售明细不一致");
            }
            if (!Objects.equals(wmsOutboundItem.getBatchNo(), saleOrderItem.getBatchNo()))
            {
                throw new ServiceException("第" + (outboundItemIndex + 1) + "行出库明细批次与来源销售明细不一致");
            }
            if (!hasPositiveQuantity(wmsOutboundItem.getQuantity()))
            {
                throw new ServiceException("第" + (outboundItemIndex + 1) + "行出库数量必须大于0");
            }
            BigDecimal remainQuantity = saleOrderItem.getRemainQuantity() == null ? BigDecimal.ZERO : saleOrderItem.getRemainQuantity();
            if (wmsOutboundItem.getQuantity().compareTo(remainQuantity) > 0)
            {
                throw new ServiceException("第" + (outboundItemIndex + 1) + "行出库数量不能超过待出库数量");
            }
        }
    }

    /**
     * 根据来源销售单补齐和校验出库单的销售关联信息
     *
     * @param wmsOutbound 出库单
     */
    private void fillSaleOrderInfo(WmsOutbound wmsOutbound)
    {
        if (wmsOutbound.getSaleOrderId() == null)
        {
            wmsOutbound.setSaleOrderNo(null);
            if (StringUtils.isEmpty(wmsOutbound.getOutboundType()))
            {
                wmsOutbound.setOutboundType("sale");
            }
            return;
        }
        WmsSaleOrder databaseSaleOrder = validateSaleOrderForOutbound(
            wmsOutbound.getSaleOrderId(), wmsOutbound.getCustomerId(), wmsOutbound.getWarehouseId(), wmsOutbound.getOutboundId());
        wmsOutbound.setSaleOrderNo(databaseSaleOrder.getOrderNo());
        wmsOutbound.setSourceOrderNo(databaseSaleOrder.getOrderNo());
        wmsOutbound.setCustomerId(databaseSaleOrder.getCustomerId());
        wmsOutbound.setWarehouseId(databaseSaleOrder.getWarehouseId());
        if (StringUtils.isEmpty(wmsOutbound.getOutboundType()))
        {
            wmsOutbound.setOutboundType("sale");
        }
    }

    /**
     * 校验来源销售单是否允许新增销售出库
     *
     * @param saleOrderId 销售单编号
     * @param customerId 客户编号
     * @param warehouseId 仓库编号
     * @param currentOutboundId 当前出库单编号
     * @return 销售单
     */
    private WmsSaleOrder validateSaleOrderForOutbound(Long saleOrderId, Long customerId, Long warehouseId, Long currentOutboundId)
    {
        if (saleOrderId == null)
        {
            return null;
        }
        WmsSaleOrder databaseSaleOrder = wmsSaleOrderMapper.selectWmsSaleOrderById(saleOrderId);
        if (databaseSaleOrder == null)
        {
            throw new ServiceException("来源销售订单不存在");
        }
        if (!STATUS_AUDITED.equals(databaseSaleOrder.getStatus()))
        {
            throw new ServiceException("仅财务已审核销售订单允许新增销售出库");
        }
        if (customerId != null && !Objects.equals(customerId, databaseSaleOrder.getCustomerId()))
        {
            throw new ServiceException("出库单客户必须与来源销售订单保持一致");
        }
        if (warehouseId != null && !Objects.equals(warehouseId, databaseSaleOrder.getWarehouseId()))
        {
            throw new ServiceException("出库单仓库必须与来源销售订单保持一致");
        }
        validateNoPendingLinkedOutbound(databaseSaleOrder, currentOutboundId);
        return databaseSaleOrder;
    }

    /**
     * 校验来源销售单是否已存在未完成的销售出库单
     *
     * @param databaseSaleOrder 销售单
     * @param currentOutboundId 当前出库单编号
     */
    private void validateNoPendingLinkedOutbound(WmsSaleOrder databaseSaleOrder, Long currentOutboundId)
    {
        WmsOutbound linkedOutboundQuery = new WmsOutbound();
        linkedOutboundQuery.setSaleOrderId(databaseSaleOrder.getSaleOrderId());
        List<WmsOutbound> linkedOutboundList = wmsOutboundMapper.selectWmsOutboundList(linkedOutboundQuery);
        if (hasPendingLinkedOutbound(linkedOutboundList, currentOutboundId))
        {
            throw new ServiceException("来源销售订单已存在未完成的销售出库单，请先处理原单据");
        }
        WmsOutbound historicalOutboundQuery = new WmsOutbound();
        historicalOutboundQuery.setSourceOrderNo(databaseSaleOrder.getOrderNo());
        List<WmsOutbound> historicalOutboundList = wmsOutboundMapper.selectWmsOutboundList(historicalOutboundQuery);
        if (hasPendingLinkedOutbound(historicalOutboundList, currentOutboundId))
        {
            throw new ServiceException("来源销售订单已存在未完成的销售出库单，请先处理原单据");
        }
    }

    /**
     * 判断是否存在未完成的关联出库单
     *
     * @param outboundList 出库单列表
     * @param currentOutboundId 当前出库单编号
     * @return 是否存在未完成出库单
     */
    private boolean hasPendingLinkedOutbound(List<WmsOutbound> outboundList, Long currentOutboundId)
    {
        if (outboundList == null || outboundList.isEmpty())
        {
            return false;
        }
        for (WmsOutbound databaseOutbound : outboundList)
        {
            if (Objects.equals(databaseOutbound.getOutboundId(), currentOutboundId))
            {
                continue;
            }
            if (STATUS_DRAFT.equals(databaseOutbound.getStatus()) || STATUS_SUBMITTED.equals(databaseOutbound.getStatus()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据销售订单明细编号查找对应的销售明细
     *
     * @param saleOrderItemList 销售订单明细列表
     * @param saleOrderItemId 销售订单明细编号
     * @return 销售订单明细
     */
    private WmsSaleOrderItem findSaleOrderItem(List<WmsSaleOrderItem> saleOrderItemList, Long saleOrderItemId)
    {
        for (WmsSaleOrderItem saleOrderItem : saleOrderItemList)
        {
            if (Objects.equals(saleOrderItem.getSaleOrderItemId(), saleOrderItemId))
            {
                return saleOrderItem;
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
     * 计算出库明细金额
     *
     * @param quantityValue 数量
     * @param priceValue 单价
     * @return 金额
     */
    private BigDecimal calculateOutboundAmount(BigDecimal quantityValue, BigDecimal priceValue)
    {
        BigDecimal safeQuantityValue = quantityValue == null ? BigDecimal.ZERO : quantityValue;
        BigDecimal safePriceValue = priceValue == null ? BigDecimal.ZERO : priceValue;
        return safeQuantityValue.multiply(safePriceValue);
    }
}
