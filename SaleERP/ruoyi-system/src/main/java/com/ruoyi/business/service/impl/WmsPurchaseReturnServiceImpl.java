package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
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
import com.ruoyi.business.domain.FinPayable;
import com.ruoyi.business.domain.WmsPurchaseOrder;
import com.ruoyi.business.domain.WmsPurchaseReturn;
import com.ruoyi.business.domain.WmsPurchaseReturnItem;
import com.ruoyi.business.domain.WmsStock;
import com.ruoyi.business.domain.WmsStockLog;
import com.ruoyi.business.mapper.FinPayableMapper;
import com.ruoyi.business.mapper.WmsPurchaseOrderMapper;
import com.ruoyi.business.mapper.WmsPurchaseReturnItemMapper;
import com.ruoyi.business.mapper.WmsPurchaseReturnMapper;
import com.ruoyi.business.mapper.WmsStockLogMapper;
import com.ruoyi.business.mapper.WmsStockMapper;
import com.ruoyi.business.service.IWmsPurchaseReturnService;
import com.ruoyi.business.service.IWmsStockService;
import com.ruoyi.business.service.support.BizComplianceHelper;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

@Service
public class WmsPurchaseReturnServiceImpl implements IWmsPurchaseReturnService
{
    private static final String STATUS_DRAFT = "draft";

    private static final String STATUS_SUBMITTED = "submitted";

    private static final String STATUS_AUDITED = "audited";

    private static final String STATUS_CANCELLED = "cancelled";

    @Autowired
    private WmsPurchaseReturnMapper wmsPurchaseReturnMapper;

    @Autowired
    private WmsPurchaseReturnItemMapper wmsPurchaseReturnItemMapper;

    @Autowired
    private WmsStockMapper wmsStockMapper;

    @Autowired
    private WmsStockLogMapper wmsStockLogMapper;

    @Autowired
    private FinPayableMapper finPayableMapper;

    @Autowired(required = false)
    private FinInvoiceService finInvoiceService;

    @Autowired
    private IWmsStockService wmsStockService;

    @Autowired(required = false)
    private WmsPurchaseOrderMapper wmsPurchaseOrderMapper;

    @Autowired(required = false)
    private com.ruoyi.business.mapper.MdProductMapper mdProductMapper;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private BizComplianceHelper bizComplianceHelper;

    @Override
    public WmsPurchaseReturn selectWmsPurchaseReturnById(Long purchaseReturnId)
    {
        return wmsPurchaseReturnMapper.selectWmsPurchaseReturnById(purchaseReturnId);
    }

    @Override
    @DataScope(deptAlias = "su", userAlias = "su", permission = "business:purchaseReturn:list")
    public List<WmsPurchaseReturn> selectWmsPurchaseReturnList(WmsPurchaseReturn wmsPurchaseReturn)
    {
        return wmsPurchaseReturnMapper.selectWmsPurchaseReturnList(wmsPurchaseReturn);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertWmsPurchaseReturn(WmsPurchaseReturn wmsPurchaseReturn)
    {
        validatePurchaseReturnBeforeSave(wmsPurchaseReturn);
        if (wmsPurchaseReturn.getReturnNo() == null || wmsPurchaseReturn.getReturnNo().isEmpty())
        {
            wmsPurchaseReturn.setReturnNo(generatePurchaseReturnNo());
        }
        wmsPurchaseReturn.setStatus(STATUS_DRAFT);
        if (wmsPurchaseReturn.getTotalQty() == null)
        {
            wmsPurchaseReturn.setTotalQty(BigDecimal.ZERO);
        }
        if (wmsPurchaseReturn.getTotalAmount() == null)
        {
            wmsPurchaseReturn.setTotalAmount(BigDecimal.ZERO);
        }
        int rows = wmsPurchaseReturnMapper.insertWmsPurchaseReturn(wmsPurchaseReturn);
        if (wmsPurchaseReturn.getItems() != null && !wmsPurchaseReturn.getItems().isEmpty())
        {
            for (WmsPurchaseReturnItem item : wmsPurchaseReturn.getItems())
            {
                if (item == null || item.getProductId() == null)
                {
                    continue;
                }
                if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
                {
                    throw new ServiceException("采购退货明细数量必须大于0");
                }
                if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) < 0)
                {
                    throw new ServiceException("采购退货明细单价不能小于0");
                }
                item.setPurchaseReturnId(wmsPurchaseReturn.getPurchaseReturnId());
                item.setAmount(item.getQuantity().multiply(item.getPrice()).setScale(2, java.math.RoundingMode.HALF_UP));
                wmsPurchaseReturnItemMapper.insertWmsPurchaseReturnItem(item);
            }
            wmsPurchaseReturnMapper.refreshTotalByPurchaseReturnId(wmsPurchaseReturn.getPurchaseReturnId());
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateWmsPurchaseReturn(WmsPurchaseReturn wmsPurchaseReturn)
    {
        WmsPurchaseReturn databasePurchaseReturn = wmsPurchaseReturnMapper.selectWmsPurchaseReturnById(wmsPurchaseReturn.getPurchaseReturnId());
        if (databasePurchaseReturn == null)
        {
            throw new ServiceException("采购退货单不存在");
        }
        if (!STATUS_DRAFT.equals(databasePurchaseReturn.getStatus()))
        {
            throw new ServiceException("仅草稿状态采购退货单允许修改");
        }
        validatePurchaseReturnBeforeSave(wmsPurchaseReturn);
        wmsPurchaseReturn.setStatus(databasePurchaseReturn.getStatus());
        int rows = wmsPurchaseReturnMapper.updateWmsPurchaseReturn(wmsPurchaseReturn);
        if (wmsPurchaseReturn.getItems() != null)
        {
            wmsPurchaseReturnItemMapper.deleteWmsPurchaseReturnItemByPurchaseReturnIds(new Long[] { wmsPurchaseReturn.getPurchaseReturnId() });
            for (WmsPurchaseReturnItem item : wmsPurchaseReturn.getItems())
            {
                if (item == null || item.getProductId() == null)
                {
                    continue;
                }
                if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
                {
                    throw new ServiceException("采购退货明细数量必须大于0");
                }
                if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) < 0)
                {
                    throw new ServiceException("采购退货明细单价不能小于0");
                }
                item.setPurchaseReturnId(wmsPurchaseReturn.getPurchaseReturnId());
                item.setAmount(item.getQuantity().multiply(item.getPrice()).setScale(2, java.math.RoundingMode.HALF_UP));
                wmsPurchaseReturnItemMapper.insertWmsPurchaseReturnItem(item);
            }
            wmsPurchaseReturnMapper.refreshTotalByPurchaseReturnId(wmsPurchaseReturn.getPurchaseReturnId());
        }
        return rows;
    }

    @Override
    public int submitWmsPurchaseReturn(Long purchaseReturnId)
    {
        WmsPurchaseReturn databasePurchaseReturn = wmsPurchaseReturnMapper.selectWmsPurchaseReturnById(purchaseReturnId);
        if (databasePurchaseReturn == null)
        {
            throw new ServiceException("采购退货单不存在");
        }
        if (!STATUS_DRAFT.equals(databasePurchaseReturn.getStatus()))
        {
            throw new ServiceException("仅草稿状态采购退货单允许提交");
        }
        WmsPurchaseReturnItem purchaseReturnItemQuery = new WmsPurchaseReturnItem();
        purchaseReturnItemQuery.setPurchaseReturnId(purchaseReturnId);
        List<WmsPurchaseReturnItem> purchaseReturnItemList = wmsPurchaseReturnItemMapper.selectWmsPurchaseReturnItemList(purchaseReturnItemQuery);
        if (purchaseReturnItemList == null || purchaseReturnItemList.isEmpty())
        {
            throw new ServiceException("采购退货明细不能为空");
        }
        validateNotOverReturning(databasePurchaseReturn, purchaseReturnItemList);
        int updateCount = wmsPurchaseReturnMapper.updateWmsPurchaseReturnStatus(
            purchaseReturnId, STATUS_DRAFT, STATUS_SUBMITTED, null, SecurityUtils.getUsername());
        if (updateCount == 0)
        {
            throw new ServiceException("采购退货单状态已变更，请刷新后重试");
        }
        return updateCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditWmsPurchaseReturn(Long purchaseReturnId)
    {
        WmsPurchaseReturn databasePurchaseReturn = wmsPurchaseReturnMapper.selectWmsPurchaseReturnById(purchaseReturnId);
        if (databasePurchaseReturn == null)
        {
            throw new ServiceException("采购退货单不存在");
        }
        if (!STATUS_SUBMITTED.equals(databasePurchaseReturn.getStatus()))
        {
            throw new ServiceException("仅已提交采购退货单允许审核");
        }
        if (bizComplianceHelper != null)
        {
            bizComplianceHelper.assertPeriodOpen(new Date(), "审核采购退货单");
            bizComplianceHelper.assertNotSelfAudit(databasePurchaseReturn.getCreateBy(), "审核采购退货单");
            bizComplianceHelper.assertEntityEnabled("md_supplier", "supplier_id", databasePurchaseReturn.getSupplierId(), "供应商");
            bizComplianceHelper.assertEntityEnabled("md_warehouse", "warehouse_id", databasePurchaseReturn.getWarehouseId(), "仓库");
        }
        WmsPurchaseReturnItem purchaseReturnItemQuery = new WmsPurchaseReturnItem();
        purchaseReturnItemQuery.setPurchaseReturnId(purchaseReturnId);
        List<WmsPurchaseReturnItem> purchaseReturnItemList = wmsPurchaseReturnItemMapper.selectWmsPurchaseReturnItemList(purchaseReturnItemQuery);
        if (purchaseReturnItemList == null || purchaseReturnItemList.isEmpty())
        {
            throw new ServiceException("采购退货明细不能为空");
        }
        validateNotOverReturning(databasePurchaseReturn, purchaseReturnItemList);
        String auditUsername = SecurityUtils.getUsername();
        for (WmsPurchaseReturnItem wmsPurchaseReturnItem : purchaseReturnItemList)
        {
            validatePurchaseReturnItem(wmsPurchaseReturnItem);
            if (bizComplianceHelper != null)
            {
                bizComplianceHelper.assertEntityEnabled("md_product", "product_id", wmsPurchaseReturnItem.getProductId(), "商品");
                if (wmsPurchaseReturnItem.getLocationId() != null)
                {
                    bizComplianceHelper.assertEntityEnabled("md_location", "location_id", wmsPurchaseReturnItem.getLocationId(), "库位");
                }
            }
            // OPT-12: 采购退货出库前按退货单价回调移动加权平均成本，防止错价入库再退货导致成本永久失真
            if (mdProductMapper != null && wmsStockMapper != null
                && wmsPurchaseReturnItem.getPrice() != null && wmsPurchaseReturnItem.getQuantity() != null)
            {
                com.ruoyi.business.domain.MdProduct product = mdProductMapper.selectMdProductById(wmsPurchaseReturnItem.getProductId());
                BigDecimal oldTotalStock = wmsStockMapper.selectTotalProductStockQuantity(wmsPurchaseReturnItem.getProductId());
                if (product != null && product.getCostPrice() != null && product.getCostPrice().compareTo(BigDecimal.ZERO) > 0
                    && oldTotalStock != null && oldTotalStock.compareTo(wmsPurchaseReturnItem.getQuantity()) > 0)
                {
                    BigDecimal remainQty = oldTotalStock.subtract(wmsPurchaseReturnItem.getQuantity());
                    BigDecimal remainValue = oldTotalStock.multiply(product.getCostPrice())
                        .subtract(wmsPurchaseReturnItem.getQuantity().multiply(wmsPurchaseReturnItem.getPrice()));
                    if (remainValue.compareTo(BigDecimal.ZERO) > 0)
                    {
                        BigDecimal newCost = remainValue.divide(remainQty, 4, java.math.RoundingMode.HALF_UP).stripTrailingZeros();
                        if (newCost.scale() < 2)
                        {
                            newCost = newCost.setScale(2, java.math.RoundingMode.HALF_UP);
                        }
                        com.ruoyi.business.domain.MdProduct patch = new com.ruoyi.business.domain.MdProduct();
                        patch.setProductId(product.getProductId());
                        patch.setCostPrice(newCost);
                        patch.setUpdateBy(auditUsername);
                        mdProductMapper.updateMdProduct(patch);
                    }
                }
            }
            if (wmsPurchaseReturnItem.getLocationId() != null || StringUtils.isNotEmpty(wmsPurchaseReturnItem.getBatchNo()))
            {
                BigDecimal[] qtys = wmsStockService.deductAvailableStockByKeyAtomically(
                    databasePurchaseReturn.getWarehouseId(),
                    wmsPurchaseReturnItem.getProductId(),
                    wmsPurchaseReturnItem.getLocationId(),
                    wmsPurchaseReturnItem.getBatchNo(),
                    wmsPurchaseReturnItem.getQuantity(),
                    auditUsername,
                    false
                );
                WmsStockLog stockLog = new WmsStockLog();
                stockLog.setWarehouseId(databasePurchaseReturn.getWarehouseId());
                stockLog.setProductId(wmsPurchaseReturnItem.getProductId());
                stockLog.setLocationId(wmsPurchaseReturnItem.getLocationId());
                stockLog.setBatchNo(wmsPurchaseReturnItem.getBatchNo());
                stockLog.setBillType("purchase_return");
                stockLog.setBillId(purchaseReturnId);
                stockLog.setBillNo(databasePurchaseReturn.getReturnNo());
                stockLog.setInOut("out");
                stockLog.setQuantity(wmsPurchaseReturnItem.getQuantity());
                stockLog.setPrice(wmsPurchaseReturnItem.getPrice());
                stockLog.setAmount(wmsPurchaseReturnItem.getAmount());
                stockLog.setBeforeQty(qtys[0]);
                stockLog.setAfterQty(qtys[1]);
                wmsStockLogMapper.insertWmsStockLog(stockLog);
            }
            else
            {
                BigDecimal remainReturnQuantity = wmsPurchaseReturnItem.getQuantity();
                WmsStock stockQuery = new WmsStock();
                stockQuery.setWarehouseId(databasePurchaseReturn.getWarehouseId());
                stockQuery.setProductId(wmsPurchaseReturnItem.getProductId());
                List<WmsStock> stockList = wmsStockMapper.selectWmsStockList(stockQuery);
                if (stockList == null || stockList.isEmpty())
                {
                    throw new ServiceException("库存不存在，商品编号：" + wmsPurchaseReturnItem.getProductId());
                }
                for (WmsStock stock : stockList)
                {
                    if (remainReturnQuantity.compareTo(BigDecimal.ZERO) <= 0)
                    {
                        break;
                    }
                    BigDecimal qty = stock.getQuantity() == null ? BigDecimal.ZERO : stock.getQuantity();
                    BigDecimal locked = stock.getLockedQuantity() == null ? BigDecimal.ZERO : stock.getLockedQuantity();
                    BigDecimal frozen = stock.getFrozenQuantity() == null ? BigDecimal.ZERO : stock.getFrozenQuantity();
                    BigDecimal available = qty.subtract(locked).subtract(frozen);
                    if (available.compareTo(BigDecimal.ZERO) <= 0)
                    {
                        continue;
                    }
                    BigDecimal currentReturnQuantity = remainReturnQuantity.min(available);
                    BigDecimal[] qtys = wmsStockService.deductAvailableStockByKeyAtomically(
                        databasePurchaseReturn.getWarehouseId(),
                        wmsPurchaseReturnItem.getProductId(),
                        stock.getLocationId(),
                        stock.getBatchNo(),
                        currentReturnQuantity,
                        auditUsername,
                        false
                    );
                    WmsStockLog stockLog = new WmsStockLog();
                    stockLog.setWarehouseId(databasePurchaseReturn.getWarehouseId());
                    stockLog.setProductId(wmsPurchaseReturnItem.getProductId());
                    stockLog.setLocationId(stock.getLocationId());
                    stockLog.setBatchNo(stock.getBatchNo());
                    stockLog.setBillType("purchase_return");
                    stockLog.setBillId(purchaseReturnId);
                    stockLog.setBillNo(databasePurchaseReturn.getReturnNo());
                    stockLog.setInOut("out");
                    stockLog.setQuantity(currentReturnQuantity);
                    stockLog.setPrice(wmsPurchaseReturnItem.getPrice());
                    stockLog.setAmount(currentReturnQuantity.multiply(wmsPurchaseReturnItem.getPrice()));
                    stockLog.setBeforeQty(qtys[0]);
                    stockLog.setAfterQty(qtys[1]);
                    wmsStockLogMapper.insertWmsStockLog(stockLog);
                    remainReturnQuantity = remainReturnQuantity.subtract(currentReturnQuantity);
                }
                if (remainReturnQuantity.compareTo(BigDecimal.ZERO) > 0)
                {
                    throw new ServiceException("可用库存不足（含锁定保护），商品编号：" + wmsPurchaseReturnItem.getProductId());
                }
            }
        }
        processPayableOffset(databasePurchaseReturn, auditUsername);
        int updateCount = wmsPurchaseReturnMapper.updateWmsPurchaseReturnStatus(
            purchaseReturnId, STATUS_SUBMITTED, STATUS_AUDITED, auditUsername, auditUsername);
        if (updateCount == 0)
        {
            throw new ServiceException("采购退货单状态已变更，请刷新后重试");
        }
        if (finInvoiceService != null && databasePurchaseReturn.getPurchaseOrderId() != null)
        {
            finInvoiceService.syncOrderInvoiceStatus("purchase", databasePurchaseReturn.getPurchaseOrderId());
        }
        return updateCount;
    }

    @Override
    public int rejectWmsPurchaseReturn(Long purchaseReturnId, String rejectReason)
    {
        WmsPurchaseReturn databasePurchaseReturn = wmsPurchaseReturnMapper.selectWmsPurchaseReturnById(purchaseReturnId);
        if (databasePurchaseReturn == null)
        {
            throw new ServiceException("采购退货单不存在");
        }
        if (!STATUS_SUBMITTED.equals(databasePurchaseReturn.getStatus()))
        {
            throw new ServiceException("仅已提交待审状态的采购退货单允许驳回");
        }
        String reason = StringUtils.isEmpty(rejectReason) ? "审核驳回重改" : rejectReason.trim();
        int updateCount = wmsPurchaseReturnMapper.rejectWmsPurchaseReturnStatus(purchaseReturnId, reason, SecurityUtils.getUsername());
        if (updateCount == 0)
        {
            throw new ServiceException("采购退货单状态已变更，请刷新后重试");
        }
        return updateCount;
    }

    @Override
    public int cancelWmsPurchaseReturn(Long purchaseReturnId)
    {
        WmsPurchaseReturn databasePurchaseReturn = wmsPurchaseReturnMapper.selectWmsPurchaseReturnById(purchaseReturnId);
        if (databasePurchaseReturn == null)
        {
            throw new ServiceException("采购退货单不存在");
        }
        if (STATUS_CANCELLED.equals(databasePurchaseReturn.getStatus()))
        {
            throw new ServiceException("采购退货单已作废，无需重复作废");
        }
        if (STATUS_AUDITED.equals(databasePurchaseReturn.getStatus()))
        {
            throw new ServiceException("已审核采购退货单不允许作废");
        }
        int updateCount = wmsPurchaseReturnMapper.updateWmsPurchaseReturnStatus(
            purchaseReturnId, databasePurchaseReturn.getStatus(), STATUS_CANCELLED, null, SecurityUtils.getUsername());
        if (updateCount == 0)
        {
            throw new ServiceException("采购退货单状态已变更，请刷新后重试");
        }
        return updateCount;
    }

    /**
     * 删除采购退货单
     *
     * @param purchaseReturnId 采购退货单编号
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWmsPurchaseReturnById(Long purchaseReturnId)
    {
        WmsPurchaseReturn databasePurchaseReturn = wmsPurchaseReturnMapper.selectWmsPurchaseReturnById(purchaseReturnId);
        if (databasePurchaseReturn == null)
        {
            return 0;
        }
        if (!STATUS_DRAFT.equals(databasePurchaseReturn.getStatus()))
        {
            throw new ServiceException("仅草稿状态采购退货单允许删除");
        }
        wmsPurchaseReturnItemMapper.deleteWmsPurchaseReturnItemByPurchaseReturnIds(new Long[] { purchaseReturnId });
        return wmsPurchaseReturnMapper.deleteWmsPurchaseReturnById(purchaseReturnId);
    }

    /**
     * 批量删除采购退货单
     *
     * @param purchaseReturnIds 采购退货单编号集合
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWmsPurchaseReturnByIds(Long[] purchaseReturnIds)
    {
        for (Long purchaseReturnId : purchaseReturnIds)
        {
            WmsPurchaseReturn databasePurchaseReturn = wmsPurchaseReturnMapper.selectWmsPurchaseReturnById(purchaseReturnId);
            if (databasePurchaseReturn == null)
            {
                continue;
            }
            if (!STATUS_DRAFT.equals(databasePurchaseReturn.getStatus()))
            {
                throw new ServiceException("仅草稿状态采购退货单允许删除，退货单号：" + databasePurchaseReturn.getReturnNo());
            }
        }
        wmsPurchaseReturnItemMapper.deleteWmsPurchaseReturnItemByPurchaseReturnIds(purchaseReturnIds);
        return wmsPurchaseReturnMapper.deleteWmsPurchaseReturnByIds(purchaseReturnIds);
    }

    private String generatePurchaseReturnNo()
    {
        String dateValue = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String noPrefix = "CGTH" + dateValue;
        String maxReturnNo = wmsPurchaseReturnMapper.selectMaxReturnNoByPrefix(noPrefix);
        if (maxReturnNo == null || maxReturnNo.isEmpty())
        {
            return noPrefix + "0001";
        }
        String sequenceValue = maxReturnNo.substring(noPrefix.length());
        int nextSequenceValue = Integer.parseInt(sequenceValue) + 1;
        return noPrefix + String.format("%04d", nextSequenceValue);
    }

    /**
     * 保存采购退货单前校验主单必填信息
     *
     * @param wmsPurchaseReturn 采购退货单
     */
    private void validatePurchaseReturnBeforeSave(WmsPurchaseReturn wmsPurchaseReturn)
    {
        if (wmsPurchaseReturn == null)
        {
            throw new ServiceException("采购退货单不存在");
        }
        if (wmsPurchaseReturn.getSupplierId() == null)
        {
            throw new ServiceException("供应商不能为空");
        }
        if (wmsPurchaseReturn.getWarehouseId() == null)
        {
            throw new ServiceException("仓库不能为空");
        }
        if (bizComplianceHelper != null)
        {
            bizComplianceHelper.assertEntityEnabled("md_supplier", "supplier_id", wmsPurchaseReturn.getSupplierId(), "供应商");
            bizComplianceHelper.assertEntityEnabled("md_warehouse", "warehouse_id", wmsPurchaseReturn.getWarehouseId(), "仓库");
        }
        if (wmsPurchaseReturn.getPurchaseOrderId() != null && wmsPurchaseOrderMapper != null)
        {
            WmsPurchaseOrder po = wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(wmsPurchaseReturn.getPurchaseOrderId());
            if (po == null)
            {
                throw new ServiceException("关联来源采购订单不存在");
            }
            if (!"1".equals(po.getStatus()) && !"2".equals(po.getStatus()) && !"3".equals(po.getStatus()))
            {
                throw new ServiceException("仅已审核/已入库的采购订单允许关联采购退货");
            }
            if (!Objects.equals(po.getSupplierId(), wmsPurchaseReturn.getSupplierId()))
            {
                throw new ServiceException("采购退货单供应商必须与来源采购订单一致");
            }
        }
    }

    /**
     * 校验关联采购单退货数量不得超过该订单已审核入库数量减去历史已退数量
     */
    private void validateNotOverReturning(WmsPurchaseReturn purchaseReturn, List<WmsPurchaseReturnItem> itemList)
    {
        if (purchaseReturn == null || purchaseReturn.getPurchaseOrderId() == null || jdbcTemplate == null || itemList == null)
        {
            return;
        }
        Map<Long, BigDecimal> productReturnQtyMap = new LinkedHashMap<>();
        for (WmsPurchaseReturnItem item : itemList)
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
            BigDecimal receivedQty = jdbcTemplate.queryForObject(
                "SELECT IFNULL(SUM(ii.quantity), 0) FROM wms_inbound_item ii " +
                "JOIN wms_inbound i ON i.inbound_id = ii.inbound_id " +
                "WHERE i.status = 'audited' AND i.purchase_order_id = ? AND ii.product_id = ?",
                BigDecimal.class, purchaseReturn.getPurchaseOrderId(), productId
            );
            BigDecimal returnedQty = jdbcTemplate.queryForObject(
                "SELECT IFNULL(SUM(ri.quantity), 0) FROM wms_purchase_return_item ri " +
                "JOIN wms_purchase_return r ON r.purchase_return_id = ri.purchase_return_id " +
                "WHERE r.status = 'audited' AND r.purchase_order_id = ? AND ri.product_id = ? AND r.purchase_return_id <> ?",
                BigDecimal.class, purchaseReturn.getPurchaseOrderId(), productId,
                purchaseReturn.getPurchaseReturnId() == null ? -1L : purchaseReturn.getPurchaseReturnId()
            );
            BigDecimal maxReturnable = (receivedQty == null ? BigDecimal.ZERO : receivedQty)
                .subtract(returnedQty == null ? BigDecimal.ZERO : returnedQty);
            if (currentQty.compareTo(maxReturnable) > 0)
            {
                throw new ServiceException("商品[" + productId + "]采购退货数量(" + currentQty + ")超过来源采购单可退余量(" + maxReturnable.max(BigDecimal.ZERO) + ")");
            }
        }
    }

    /**
     * 审核前校验采购退货明细字段
     *
     * @param wmsPurchaseReturnItem 采购退货明细
     */
    private void validatePurchaseReturnItem(WmsPurchaseReturnItem wmsPurchaseReturnItem)
    {
        if (wmsPurchaseReturnItem.getProductId() == null)
        {
            throw new ServiceException("采购退货明细商品不能为空");
        }
        if (wmsPurchaseReturnItem.getQuantity() == null || wmsPurchaseReturnItem.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("采购退货数量必须大于0");
        }
        if (wmsPurchaseReturnItem.getPrice() == null || wmsPurchaseReturnItem.getPrice().compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ServiceException("采购退货单价不能小于0");
        }
    }

    /**
     * 使用采购退货金额冲减供应商未结清应付账款；若存在已结清溢付差额则生成红字待供应商退款应付台账
     *
     * @param databasePurchaseReturn 采购退货单
     */
    private void processPayableOffset(WmsPurchaseReturn databasePurchaseReturn, String operatorName)
    {
        if (databasePurchaseReturn.getSupplierId() == null || databasePurchaseReturn.getTotalAmount() == null
            || databasePurchaseReturn.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            return;
        }
        List<FinPayable> orderedPayables = new ArrayList<>();
        if (databasePurchaseReturn.getPurchaseOrderId() != null)
        {
            FinPayable linkedPayable = finPayableMapper.selectFinPayableByPurchaseOrderId(databasePurchaseReturn.getPurchaseOrderId());
            if (linkedPayable != null)
            {
                orderedPayables.add(linkedPayable);
            }
        }
        List<FinPayable> payableList =
            finPayableMapper.selectPendingFinPayableListBySupplierId(databasePurchaseReturn.getSupplierId());
        if (payableList != null)
        {
            for (FinPayable p : payableList)
            {
                boolean alreadyAdded = orderedPayables.stream()
                    .anyMatch(existing -> Objects.equals(existing.getPayableId(), p.getPayableId()));
                if (!alreadyAdded)
                {
                    orderedPayables.add(p);
                }
            }
        }
        BigDecimal remainOffsetAmount = databasePurchaseReturn.getTotalAmount();
        for (FinPayable finPayable : orderedPayables)
        {
            if (remainOffsetAmount.compareTo(BigDecimal.ZERO) <= 0)
            {
                break;
            }
            BigDecimal amountDue = finPayable.getAmountDue() == null ? BigDecimal.ZERO : finPayable.getAmountDue();
            BigDecimal amountPaid = finPayable.getAmountPaid() == null ? BigDecimal.ZERO : finPayable.getAmountPaid();
            BigDecimal remainAmount = amountDue.subtract(amountPaid);
            if (amountDue.compareTo(BigDecimal.ZERO) <= 0 || remainAmount.compareTo(BigDecimal.ZERO) <= 0)
            {
                continue;
            }
            BigDecimal currentOffsetAmount = remainOffsetAmount.min(remainAmount);
            BigDecimal newAmountDue = amountDue.subtract(currentOffsetAmount);
            if (newAmountDue.compareTo(BigDecimal.ZERO) < 0)
            {
                newAmountDue = BigDecimal.ZERO;
            }
            BigDecimal newAmountPaid = amountPaid;
            if (newAmountPaid.compareTo(newAmountDue) > 0)
            {
                newAmountPaid = newAmountDue;
            }
            BigDecimal newRemainAmount = newAmountDue.subtract(newAmountPaid);
            String newStatus;
            if (newRemainAmount.compareTo(BigDecimal.ZERO) <= 0)
            {
                newStatus = "2";
            }
            else if (newAmountPaid.compareTo(BigDecimal.ZERO) > 0)
            {
                newStatus = "1";
            }
            else
            {
                newStatus = "0";
            }
            finPayable.setAmountDue(newAmountDue);
            finPayable.setAmountPaid(newAmountPaid);
            finPayable.setRemainAmount(newRemainAmount);
            finPayable.setStatus(newStatus);
            finPayableMapper.updateFinPayable(finPayable);
            remainOffsetAmount = remainOffsetAmount.subtract(currentOffsetAmount);
        }
        if (remainOffsetAmount.compareTo(BigDecimal.ZERO) > 0)
        {
            FinPayable refundPayable = new FinPayable();
            refundPayable.setPurchaseOrderId(databasePurchaseReturn.getPurchaseOrderId());
            refundPayable.setSupplierId(databasePurchaseReturn.getSupplierId());
            refundPayable.setAmountDue(remainOffsetAmount.negate());
            refundPayable.setAmountPaid(BigDecimal.ZERO);
            refundPayable.setRemainAmount(remainOffsetAmount.negate());
            refundPayable.setStatus("0");
            refundPayable.setCreateBy(operatorName);
            refundPayable.setCreateTime(DateUtils.getNowDate());
            refundPayable.setRemark("采购退货溢付冲红待供应商退款（来源退货单：" + databasePurchaseReturn.getReturnNo() + "）");
            finPayableMapper.insertFinPayable(refundPayable);
        }
    }
}
