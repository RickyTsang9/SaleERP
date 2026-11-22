package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.FinReceivable;
import com.ruoyi.business.domain.MdProduct;
import com.ruoyi.business.domain.WmsSaleOrder;
import com.ruoyi.business.domain.WmsSaleReturn;
import com.ruoyi.business.domain.WmsSaleReturnItem;
import com.ruoyi.business.domain.WmsStock;
import com.ruoyi.business.domain.WmsStockLog;
import com.ruoyi.business.mapper.FinReceivableMapper;
import com.ruoyi.business.mapper.MdProductMapper;
import com.ruoyi.business.mapper.WmsSaleOrderMapper;
import com.ruoyi.business.mapper.WmsSaleReturnItemMapper;
import com.ruoyi.business.mapper.WmsSaleReturnMapper;
import com.ruoyi.business.mapper.WmsStockLogMapper;
import com.ruoyi.business.mapper.WmsStockMapper;
import com.ruoyi.business.service.IWmsSaleReturnService;
import com.ruoyi.business.service.IWmsStockService;
import com.ruoyi.business.service.support.BizComplianceHelper;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

@Service
public class WmsSaleReturnServiceImpl implements IWmsSaleReturnService
{
    private static final String STATUS_DRAFT = "draft";

    private static final String STATUS_SUBMITTED = "submitted";

    private static final String STATUS_AUDITED = "audited";

    private static final String STATUS_CANCELLED = "cancelled";

    @Autowired
    private WmsSaleReturnMapper wmsSaleReturnMapper;

    @Autowired
    private WmsSaleReturnItemMapper wmsSaleReturnItemMapper;

    @Autowired
    private WmsStockMapper wmsStockMapper;

    @Autowired
    private WmsStockLogMapper wmsStockLogMapper;

    @Autowired
    private FinReceivableMapper finReceivableMapper;

    @Autowired
    private IWmsStockService wmsStockService;

    @Autowired(required = false)
    private WmsSaleOrderMapper wmsSaleOrderMapper;

    @Autowired(required = false)
    private MdProductMapper mdProductMapper;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private BizComplianceHelper bizComplianceHelper;

    @Override
    public WmsSaleReturn selectWmsSaleReturnById(Long saleReturnId)
    {
        return wmsSaleReturnMapper.selectWmsSaleReturnById(saleReturnId);
    }

    @Override
    @DataScope(deptAlias = "su", userAlias = "su", permission = "business:saleReturn:list")
    public List<WmsSaleReturn> selectWmsSaleReturnList(WmsSaleReturn wmsSaleReturn)
    {
        return wmsSaleReturnMapper.selectWmsSaleReturnList(wmsSaleReturn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertWmsSaleReturn(WmsSaleReturn wmsSaleReturn)
    {
        validateSaleReturnBeforeSave(wmsSaleReturn);
        if (StringUtils.isEmpty(wmsSaleReturn.getReturnNo()))
        {
            wmsSaleReturn.setReturnNo(generateSaleReturnNo());
        }
        wmsSaleReturn.setStatus(STATUS_DRAFT);
        if (wmsSaleReturn.getTotalQty() == null)
        {
            wmsSaleReturn.setTotalQty(BigDecimal.ZERO);
        }
        if (wmsSaleReturn.getTotalAmount() == null)
        {
            wmsSaleReturn.setTotalAmount(BigDecimal.ZERO);
        }
        int rows = wmsSaleReturnMapper.insertWmsSaleReturn(wmsSaleReturn);
        if (wmsSaleReturn.getItems() != null && !wmsSaleReturn.getItems().isEmpty())
        {
            for (WmsSaleReturnItem item : wmsSaleReturn.getItems())
            {
                if (item == null || item.getProductId() == null)
                {
                    continue;
                }
                if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
                {
                    throw new ServiceException("销售退货明细数量必须大于0");
                }
                if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) < 0)
                {
                    throw new ServiceException("销售退货明细单价不能小于0");
                }
                item.setSaleReturnId(wmsSaleReturn.getSaleReturnId());
                item.setAmount(item.getQuantity().multiply(item.getPrice()).setScale(2, RoundingMode.HALF_UP));
                wmsSaleReturnItemMapper.insertWmsSaleReturnItem(item);
            }
            wmsSaleReturnMapper.refreshTotalBySaleReturnId(wmsSaleReturn.getSaleReturnId());
        }
        return rows;
    }

    @Override
    public int updateWmsSaleReturn(WmsSaleReturn wmsSaleReturn)
    {
        WmsSaleReturn databaseSaleReturn = wmsSaleReturnMapper.selectWmsSaleReturnById(wmsSaleReturn.getSaleReturnId());
        if (databaseSaleReturn == null)
        {
            throw new ServiceException("销售退货单不存在");
        }
        if (!STATUS_DRAFT.equals(databaseSaleReturn.getStatus()))
        {
            throw new ServiceException("仅草稿状态销售退货单允许修改");
        }
        validateSaleReturnBeforeSave(wmsSaleReturn);
        wmsSaleReturn.setStatus(databaseSaleReturn.getStatus());
        return wmsSaleReturnMapper.updateWmsSaleReturn(wmsSaleReturn);
    }

    @Override
    public int submitWmsSaleReturn(Long saleReturnId)
    {
        WmsSaleReturn databaseSaleReturn = wmsSaleReturnMapper.selectWmsSaleReturnById(saleReturnId);
        if (databaseSaleReturn == null)
        {
            throw new ServiceException("销售退货单不存在");
        }
        if (!STATUS_DRAFT.equals(databaseSaleReturn.getStatus()))
        {
            throw new ServiceException("仅草稿状态销售退货单允许提交");
        }
        List<WmsSaleReturnItem> saleReturnItemList = wmsSaleReturnItemMapper.selectWmsSaleReturnItemsByReturnId(saleReturnId);
        if (saleReturnItemList == null || saleReturnItemList.isEmpty())
        {
            throw new ServiceException("销售退货明细不能为空");
        }
        validateNotOverReturning(databaseSaleReturn, saleReturnItemList);
        int updateCount = wmsSaleReturnMapper.updateWmsSaleReturnStatus(
            saleReturnId, STATUS_DRAFT, STATUS_SUBMITTED, null, SecurityUtils.getUsername());
        if (updateCount == 0)
        {
            throw new ServiceException("销售退货单状态已变更，请刷新后重试");
        }
        return updateCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditWmsSaleReturn(Long saleReturnId)
    {
        WmsSaleReturn databaseSaleReturn = wmsSaleReturnMapper.selectWmsSaleReturnById(saleReturnId);
        if (databaseSaleReturn == null)
        {
            throw new ServiceException("销售退货单不存在");
        }
        if (!STATUS_SUBMITTED.equals(databaseSaleReturn.getStatus()))
        {
            throw new ServiceException("仅已提交销售退货单允许审核");
        }
        if (bizComplianceHelper != null)
        {
            bizComplianceHelper.assertPeriodOpen(new Date(), "审核销售退货单");
            bizComplianceHelper.assertNotSelfAudit(databaseSaleReturn.getCreateBy(), "审核销售退货单");
            bizComplianceHelper.assertEntityEnabled("md_customer", "customer_id", databaseSaleReturn.getCustomerId(), "客户");
            bizComplianceHelper.assertEntityEnabled("md_warehouse", "warehouse_id", databaseSaleReturn.getWarehouseId(), "仓库");
        }
        List<WmsSaleReturnItem> itemList = wmsSaleReturnItemMapper.selectWmsSaleReturnItemsByReturnId(saleReturnId);
        if (itemList == null || itemList.isEmpty())
        {
            throw new ServiceException("销售退货明细不能为空");
        }
        validateNotOverReturning(databaseSaleReturn, itemList);
        String auditUsername = SecurityUtils.getUsername();
        for (WmsSaleReturnItem wmsSaleReturnItem : itemList)
        {
            validateSaleReturnItem(wmsSaleReturnItem);
            Long locationId = wmsSaleReturnItem.getLocationId();
            String batchNo = wmsSaleReturnItem.getBatchNo();
            Date existingExpireDate = null;
            if (locationId == null && StringUtils.isEmpty(batchNo))
            {
                WmsStock defaultStock = wmsStockMapper.selectFirstWmsStockByWarehouseAndProduct(
                    databaseSaleReturn.getWarehouseId(), wmsSaleReturnItem.getProductId());
                if (defaultStock != null)
                {
                    locationId = defaultStock.getLocationId();
                    batchNo = defaultStock.getBatchNo();
                    existingExpireDate = defaultStock.getExpireDate();
                }
            }
            else
            {
                WmsStock stockKeyQuery = new WmsStock();
                stockKeyQuery.setWarehouseId(databaseSaleReturn.getWarehouseId());
                stockKeyQuery.setProductId(wmsSaleReturnItem.getProductId());
                stockKeyQuery.setLocationId(locationId);
                stockKeyQuery.setBatchNo(batchNo);
                WmsStock matchedStock = wmsStockMapper.selectWmsStockByKey(stockKeyQuery);
                if (matchedStock != null)
                {
                    existingExpireDate = matchedStock.getExpireDate();
                }
            }
            BigDecimal[] qtys = wmsStockService.increaseStockByKeyAtomically(
                databaseSaleReturn.getWarehouseId(),
                wmsSaleReturnItem.getProductId(),
                locationId,
                batchNo,
                existingExpireDate,
                wmsSaleReturnItem.getQuantity(),
                auditUsername
            );
            BigDecimal costPriceSnapshot = resolveReturnUnitCost(databaseSaleReturn.getSaleOrderId(), wmsSaleReturnItem.getProductId());
            WmsSaleReturnItem costPatch = new WmsSaleReturnItem();
            costPatch.setSaleReturnItemId(wmsSaleReturnItem.getSaleReturnItemId());
            costPatch.setCostPriceSnapshot(costPriceSnapshot);
            wmsSaleReturnItemMapper.updateWmsSaleReturnItem(costPatch);

            BigDecimal costAmount = costPriceSnapshot.multiply(wmsSaleReturnItem.getQuantity()).setScale(2, RoundingMode.HALF_UP);
            WmsStockLog stockLog = new WmsStockLog();
            stockLog.setWarehouseId(databaseSaleReturn.getWarehouseId());
            stockLog.setProductId(wmsSaleReturnItem.getProductId());
            stockLog.setLocationId(locationId);
            stockLog.setBatchNo(batchNo);
            stockLog.setBillType("sale_return");
            stockLog.setBillId(saleReturnId);
            stockLog.setBillNo(databaseSaleReturn.getReturnNo());
            stockLog.setInOut("in");
            stockLog.setQuantity(wmsSaleReturnItem.getQuantity());
            stockLog.setPrice(costPriceSnapshot);
            stockLog.setAmount(costAmount);
            stockLog.setBeforeQty(qtys[0]);
            stockLog.setAfterQty(qtys[1]);
            wmsStockLogMapper.insertWmsStockLog(stockLog);
        }
        processReceivableOffset(databaseSaleReturn, auditUsername);
        int updateCount = wmsSaleReturnMapper.updateWmsSaleReturnStatus(
            saleReturnId, STATUS_SUBMITTED, STATUS_AUDITED, auditUsername, auditUsername);
        if (updateCount == 0)
        {
            throw new ServiceException("销售退货单状态已变更，请刷新后重试");
        }
        return updateCount;
    }

    @Override
    public int rejectWmsSaleReturn(Long saleReturnId, String rejectReason)
    {
        WmsSaleReturn databaseSaleReturn = wmsSaleReturnMapper.selectWmsSaleReturnById(saleReturnId);
        if (databaseSaleReturn == null)
        {
            throw new ServiceException("销售退货单不存在");
        }
        if (!STATUS_SUBMITTED.equals(databaseSaleReturn.getStatus()))
        {
            throw new ServiceException("仅已提交待审状态的销售退货单允许驳回");
        }
        String reason = StringUtils.isEmpty(rejectReason) ? "审核驳回重改" : rejectReason.trim();
        int updateCount = wmsSaleReturnMapper.rejectWmsSaleReturnStatus(saleReturnId, reason, SecurityUtils.getUsername());
        if (updateCount == 0)
        {
            throw new ServiceException("销售退货单状态已变更，请刷新后重试");
        }
        return updateCount;
    }

    @Override
    public int cancelWmsSaleReturn(Long saleReturnId)
    {
        WmsSaleReturn databaseSaleReturn = wmsSaleReturnMapper.selectWmsSaleReturnById(saleReturnId);
        if (databaseSaleReturn == null)
        {
            throw new ServiceException("销售退货单不存在");
        }
        if (STATUS_CANCELLED.equals(databaseSaleReturn.getStatus()))
        {
            throw new ServiceException("销售退货单已作废，无需重复作废");
        }
        if (STATUS_AUDITED.equals(databaseSaleReturn.getStatus()))
        {
            throw new ServiceException("已审核销售退货单不允许作废");
        }
        int updateCount = wmsSaleReturnMapper.updateWmsSaleReturnStatus(
            saleReturnId, databaseSaleReturn.getStatus(), STATUS_CANCELLED, null, SecurityUtils.getUsername());
        if (updateCount == 0)
        {
            throw new ServiceException("销售退货单状态已变更，请刷新后重试");
        }
        return updateCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWmsSaleReturnById(Long saleReturnId)
    {
        WmsSaleReturn databaseSaleReturn = wmsSaleReturnMapper.selectWmsSaleReturnById(saleReturnId);
        if (databaseSaleReturn == null)
        {
            return 0;
        }
        validateSaleReturnCanBeDeleted(databaseSaleReturn, false);
        wmsSaleReturnItemMapper.deleteWmsSaleReturnItemBySaleReturnIds(new Long[] { saleReturnId });
        return wmsSaleReturnMapper.deleteWmsSaleReturnById(saleReturnId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWmsSaleReturnByIds(Long[] saleReturnIds)
    {
        for (Long saleReturnId : saleReturnIds)
        {
            WmsSaleReturn databaseSaleReturn = wmsSaleReturnMapper.selectWmsSaleReturnById(saleReturnId);
            if (databaseSaleReturn == null)
            {
                continue;
            }
            validateSaleReturnCanBeDeleted(databaseSaleReturn, true);
        }
        wmsSaleReturnItemMapper.deleteWmsSaleReturnItemBySaleReturnIds(saleReturnIds);
        return wmsSaleReturnMapper.deleteWmsSaleReturnByIds(saleReturnIds);
    }

    /**
     * 校验销售退货单是否允许删除
     *
     * @param databaseSaleReturn 数据库中的销售退货单
     * @param batchDelete 是否批量删除
     */
    private void validateSaleReturnCanBeDeleted(WmsSaleReturn databaseSaleReturn, boolean batchDelete)
    {
        if (!STATUS_DRAFT.equals(databaseSaleReturn.getStatus()))
        {
            if (batchDelete)
            {
                throw new ServiceException("仅草稿状态销售退货单允许删除，退货单号：" + databaseSaleReturn.getReturnNo());
            }
            throw new ServiceException("仅草稿状态销售退货单允许删除");
        }
    }

    private String generateSaleReturnNo()
    {
        String dateValue = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String noPrefix = "xsth" + dateValue;
        String maxReturnNo = wmsSaleReturnMapper.selectMaxReturnNoByPrefix(noPrefix);
        if (StringUtils.isEmpty(maxReturnNo))
        {
            return noPrefix + "0001";
        }
        String sequenceValue = maxReturnNo.substring(noPrefix.length());
        int nextSequenceValue = Integer.parseInt(sequenceValue) + 1;
        return noPrefix + String.format("%04d", nextSequenceValue);
    }

    /**
     * 保存销售退货单前校验主单必填信息
     *
     * @param wmsSaleReturn 销售退货单
     */
    private void validateSaleReturnBeforeSave(WmsSaleReturn wmsSaleReturn)
    {
        if (wmsSaleReturn == null)
        {
            throw new ServiceException("销售退货单不存在");
        }
        if (wmsSaleReturn.getCustomerId() == null)
        {
            throw new ServiceException("客户不能为空");
        }
        if (wmsSaleReturn.getWarehouseId() == null)
        {
            throw new ServiceException("仓库不能为空");
        }
        if (bizComplianceHelper != null)
        {
            bizComplianceHelper.assertEntityEnabled("md_customer", "customer_id", wmsSaleReturn.getCustomerId(), "客户");
            bizComplianceHelper.assertEntityEnabled("md_warehouse", "warehouse_id", wmsSaleReturn.getWarehouseId(), "仓库");
        }
        if (wmsSaleReturn.getSaleOrderId() != null && wmsSaleOrderMapper != null)
        {
            WmsSaleOrder saleOrder = wmsSaleOrderMapper.selectWmsSaleOrderById(wmsSaleReturn.getSaleOrderId());
            if (saleOrder == null)
            {
                throw new ServiceException("关联来源销售订单不存在");
            }
            if (!"audited".equals(saleOrder.getStatus()) && !"closed".equals(saleOrder.getStatus()))
            {
                throw new ServiceException("仅已审核或已结案的销售订单允许关联销售退货");
            }
            if (!Objects.equals(saleOrder.getCustomerId(), wmsSaleReturn.getCustomerId()))
            {
                throw new ServiceException("退货单客户必须与来源销售订单客户一致");
            }
        }
    }

    /**
     * 校验关联销售单退货数量不得超过该订单已审核出库数量减去历史已退数量
     */
    private void validateNotOverReturning(WmsSaleReturn saleReturn, List<WmsSaleReturnItem> itemList)
    {
        if (saleReturn == null || saleReturn.getSaleOrderId() == null || jdbcTemplate == null || itemList == null)
        {
            return;
        }
        WmsSaleOrder saleOrder = wmsSaleOrderMapper == null ? null : wmsSaleOrderMapper.selectWmsSaleOrderById(saleReturn.getSaleOrderId());
        String orderNo = saleOrder == null ? "" : StringUtils.nvl(saleOrder.getOrderNo(), "");
        Map<Long, BigDecimal> productReturnQtyMap = new LinkedHashMap<>();
        for (WmsSaleReturnItem item : itemList)
        {
            if (item.getProductId() == null || item.getQuantity() == null)
            {
                continue;
            }
            productReturnQtyMap.merge(item.getProductId(), item.getQuantity(), BigDecimal::add);
        }
        for (Map.Entry<Long, BigDecimal> entry : productReturnQtyMap.entrySet())
        {
            Long productId = entry.getKey();
            BigDecimal currentQty = entry.getValue();
            BigDecimal shippedQty = jdbcTemplate.queryForObject(
                "SELECT IFNULL(SUM(oi.quantity), 0) FROM wms_outbound_item oi " +
                "JOIN wms_outbound o ON o.outbound_id = oi.outbound_id " +
                "WHERE o.status = 'audited' AND oi.product_id = ? " +
                "  AND (o.sale_order_id = ? OR (o.sale_order_id IS NULL AND o.source_order_no = ?))",
                BigDecimal.class, productId, saleReturn.getSaleOrderId(), orderNo
            );
            BigDecimal returnedQty = jdbcTemplate.queryForObject(
                "SELECT IFNULL(SUM(ri.quantity), 0) FROM wms_sale_return_item ri " +
                "JOIN wms_sale_return r ON r.sale_return_id = ri.sale_return_id " +
                "WHERE r.status = 'audited' AND r.sale_order_id = ? AND ri.product_id = ? AND r.sale_return_id <> ?",
                BigDecimal.class, saleReturn.getSaleOrderId(), productId,
                saleReturn.getSaleReturnId() == null ? -1L : saleReturn.getSaleReturnId()
            );
            BigDecimal maxReturnable = (shippedQty == null ? BigDecimal.ZERO : shippedQty)
                .subtract(returnedQty == null ? BigDecimal.ZERO : returnedQty);
            if (currentQty.compareTo(maxReturnable) > 0)
            {
                throw new ServiceException("商品[" + productId + "]退货数量(" + currentQty + ")超过来源销售单可退余量(" + maxReturnable.max(BigDecimal.ZERO) + ")");
            }
        }
    }

    /**
     * 解析退货单位成本快照（优先取来源销售单出库成本快照，兜底取商品档案移动加权成本）
     */
    private BigDecimal resolveReturnUnitCost(Long saleOrderId, Long productId)
    {
        if (saleOrderId != null && jdbcTemplate != null)
        {
            try
            {
                List<BigDecimal> list = jdbcTemplate.queryForList(
                    "SELECT oi.cost_price_snapshot FROM wms_outbound_item oi " +
                    "JOIN wms_outbound o ON o.outbound_id = oi.outbound_id " +
                    "WHERE o.status = 'audited' AND o.sale_order_id = ? AND oi.product_id = ? AND oi.cost_price_snapshot > 0 " +
                    "ORDER BY oi.outbound_item_id DESC LIMIT 1",
                    BigDecimal.class, saleOrderId, productId
                );
                if (list != null && !list.isEmpty() && list.get(0) != null)
                {
                    return list.get(0);
                }
            }
            catch (Exception ignored)
            {
                // fallback to product cost
            }
        }
        if (mdProductMapper != null && productId != null)
        {
            MdProduct product = mdProductMapper.selectMdProductById(productId);
            if (product != null && product.getCostPrice() != null)
            {
                return product.getCostPrice();
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * 审核前校验销售退货明细字段
     *
     * @param wmsSaleReturnItem 销售退货明细
     */
    private void validateSaleReturnItem(WmsSaleReturnItem wmsSaleReturnItem)
    {
        if (wmsSaleReturnItem.getProductId() == null)
        {
            throw new ServiceException("销售退货明细商品不能为空");
        }
        if (wmsSaleReturnItem.getQuantity() == null || wmsSaleReturnItem.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("销售退货数量必须大于0");
        }
        if (wmsSaleReturnItem.getPrice() == null || wmsSaleReturnItem.getPrice().compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ServiceException("销售退货单价不能小于0");
        }
    }

    /**
     * 使用销售退货金额冲减客户未结清应收台账；若存在已结清溢收差额则生成红字待退客户应收台账
     *
     * @param databaseSaleReturn 销售退货单
     */
    private void processReceivableOffset(WmsSaleReturn databaseSaleReturn, String operatorName)
    {
        if (databaseSaleReturn.getCustomerId() == null || databaseSaleReturn.getTotalAmount() == null
            || databaseSaleReturn.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            return;
        }
        List<FinReceivable> orderedReceivables = new ArrayList<>();
        if (databaseSaleReturn.getSaleOrderId() != null)
        {
            FinReceivable linkedReceivable = finReceivableMapper.selectFinReceivableBySaleOrderId(databaseSaleReturn.getSaleOrderId());
            if (linkedReceivable != null)
            {
                orderedReceivables.add(linkedReceivable);
            }
        }
        List<FinReceivable> pendingReceivableList =
            finReceivableMapper.selectPendingFinReceivableListByCustomerId(databaseSaleReturn.getCustomerId());
        if (pendingReceivableList != null)
        {
            for (FinReceivable r : pendingReceivableList)
            {
                boolean alreadyAdded = orderedReceivables.stream()
                    .anyMatch(existing -> Objects.equals(existing.getReceivableId(), r.getReceivableId()));
                if (!alreadyAdded)
                {
                    orderedReceivables.add(r);
                }
            }
        }
        BigDecimal remainOffsetAmount = databaseSaleReturn.getTotalAmount();
        for (FinReceivable pendingReceivable : orderedReceivables)
        {
            if (remainOffsetAmount.compareTo(BigDecimal.ZERO) <= 0)
            {
                break;
            }
            BigDecimal amountDue = pendingReceivable.getAmountDue() == null ? BigDecimal.ZERO : pendingReceivable.getAmountDue();
            BigDecimal amountPaid = pendingReceivable.getAmountPaid() == null ? BigDecimal.ZERO : pendingReceivable.getAmountPaid();
            BigDecimal outstandingAmount = amountDue.subtract(amountPaid);
            if (amountDue.compareTo(BigDecimal.ZERO) <= 0 || outstandingAmount.compareTo(BigDecimal.ZERO) <= 0)
            {
                continue;
            }
            BigDecimal currentOffsetAmount = remainOffsetAmount.min(outstandingAmount);
            BigDecimal updatedAmountDue = amountDue.subtract(currentOffsetAmount);
            if (updatedAmountDue.compareTo(BigDecimal.ZERO) < 0)
            {
                updatedAmountDue = BigDecimal.ZERO;
            }
            BigDecimal updatedAmountPaid = amountPaid;
            if (updatedAmountPaid.compareTo(updatedAmountDue) > 0)
            {
                updatedAmountPaid = updatedAmountDue;
            }
            BigDecimal updatedRemain = updatedAmountDue.subtract(updatedAmountPaid);
            String updatedStatus;
            if (updatedRemain.compareTo(BigDecimal.ZERO) <= 0)
            {
                updatedStatus = "paid";
            }
            else if (updatedAmountPaid.compareTo(BigDecimal.ZERO) > 0)
            {
                updatedStatus = "partial";
            }
            else
            {
                updatedStatus = "unpaid";
            }
            finReceivableMapper.updateFinReceivableAmounts(
                pendingReceivable.getReceivableId(), updatedAmountDue, updatedAmountPaid, updatedStatus);
            if (pendingReceivable.getSaleOrderId() != null && wmsSaleOrderMapper != null)
            {
                wmsSaleOrderMapper.updateWmsSaleOrderPaymentStatus(pendingReceivable.getSaleOrderId(), updatedStatus);
            }
            remainOffsetAmount = remainOffsetAmount.subtract(currentOffsetAmount);
        }
        if (remainOffsetAmount.compareTo(BigDecimal.ZERO) > 0)
        {
            FinReceivable refundReceivable = new FinReceivable();
            refundReceivable.setSaleOrderId(databaseSaleReturn.getSaleOrderId());
            refundReceivable.setCustomerId(databaseSaleReturn.getCustomerId());
            refundReceivable.setAmountDue(remainOffsetAmount.negate());
            refundReceivable.setAmountPaid(BigDecimal.ZERO);
            refundReceivable.setStatus("unpaid");
            refundReceivable.setDueDate(new Date());
            refundReceivable.setCreateBy(operatorName);
            refundReceivable.setRemark("销售退货溢收冲红待退客户款（来源退货单：" + databaseSaleReturn.getReturnNo() + "）");
            finReceivableMapper.insertFinReceivable(refundReceivable);
        }
    }
}
