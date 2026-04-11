package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.FinPayable;
import com.ruoyi.business.domain.WmsPurchaseReturn;
import com.ruoyi.business.domain.WmsPurchaseReturnItem;
import com.ruoyi.business.domain.WmsStock;
import com.ruoyi.business.domain.WmsStockLog;
import com.ruoyi.business.mapper.FinPayableMapper;
import com.ruoyi.business.mapper.WmsPurchaseReturnItemMapper;
import com.ruoyi.business.mapper.WmsPurchaseReturnMapper;
import com.ruoyi.business.mapper.WmsStockLogMapper;
import com.ruoyi.business.mapper.WmsStockMapper;
import com.ruoyi.business.service.IWmsPurchaseReturnService;
import com.ruoyi.common.exception.ServiceException;
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

    @Override
    public WmsPurchaseReturn selectWmsPurchaseReturnById(Long purchaseReturnId)
    {
        return wmsPurchaseReturnMapper.selectWmsPurchaseReturnById(purchaseReturnId);
    }

    @Override
    public List<WmsPurchaseReturn> selectWmsPurchaseReturnList(WmsPurchaseReturn wmsPurchaseReturn)
    {
        return wmsPurchaseReturnMapper.selectWmsPurchaseReturnList(wmsPurchaseReturn);
    }

    @Override
    public int insertWmsPurchaseReturn(WmsPurchaseReturn wmsPurchaseReturn)
    {
        if (wmsPurchaseReturn.getReturnNo() == null || wmsPurchaseReturn.getReturnNo().isEmpty())
        {
            wmsPurchaseReturn.setReturnNo(generatePurchaseReturnNo());
        }
        if (StringUtils.isEmpty(wmsPurchaseReturn.getStatus()))
        {
            wmsPurchaseReturn.setStatus(STATUS_DRAFT);
        }
        return wmsPurchaseReturnMapper.insertWmsPurchaseReturn(wmsPurchaseReturn);
    }

    @Override
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
        wmsPurchaseReturn.setStatus(databasePurchaseReturn.getStatus());
        return wmsPurchaseReturnMapper.updateWmsPurchaseReturn(wmsPurchaseReturn);
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
        WmsPurchaseReturnItem purchaseReturnItemQuery = new WmsPurchaseReturnItem();
        purchaseReturnItemQuery.setPurchaseReturnId(purchaseReturnId);
        List<WmsPurchaseReturnItem> purchaseReturnItemList = wmsPurchaseReturnItemMapper.selectWmsPurchaseReturnItemList(purchaseReturnItemQuery);
        if (purchaseReturnItemList == null || purchaseReturnItemList.isEmpty())
        {
            throw new ServiceException("采购退货明细不能为空");
        }
        for (WmsPurchaseReturnItem wmsPurchaseReturnItem : purchaseReturnItemList)
        {
            if (wmsPurchaseReturnItem.getProductId() == null || wmsPurchaseReturnItem.getQuantity() == null
                || wmsPurchaseReturnItem.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new ServiceException("采购退货明细信息不完整");
            }
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
                BigDecimal beforeQuantity = stock.getQuantity() == null ? BigDecimal.ZERO : stock.getQuantity();
                if (beforeQuantity.compareTo(BigDecimal.ZERO) <= 0)
                {
                    continue;
                }
                BigDecimal currentReturnQuantity = remainReturnQuantity.min(beforeQuantity);
                BigDecimal afterQuantity = beforeQuantity.subtract(currentReturnQuantity);
                stock.setQuantity(afterQuantity);
                wmsStockMapper.updateWmsStock(stock);
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
                stockLog.setAmount(wmsPurchaseReturnItem.getAmount());
                stockLog.setBeforeQty(beforeQuantity);
                stockLog.setAfterQty(afterQuantity);
                wmsStockLogMapper.insertWmsStockLog(stockLog);
                remainReturnQuantity = remainReturnQuantity.subtract(currentReturnQuantity);
            }
            if (remainReturnQuantity.compareTo(BigDecimal.ZERO) > 0)
            {
                throw new ServiceException("库存不足，商品编号：" + wmsPurchaseReturnItem.getProductId());
            }
        }
        processPayableOffset(databasePurchaseReturn);
        String auditUsername = SecurityUtils.getUsername();
        int updateCount = wmsPurchaseReturnMapper.updateWmsPurchaseReturnStatus(
            purchaseReturnId, STATUS_SUBMITTED, STATUS_AUDITED, auditUsername, auditUsername);
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

    @Override
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

    @Override
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

    private void processPayableOffset(WmsPurchaseReturn databasePurchaseReturn)
    {
        if (databasePurchaseReturn.getSupplierId() == null || databasePurchaseReturn.getTotalAmount() == null
            || databasePurchaseReturn.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            return;
        }
        FinPayable payableQuery = new FinPayable();
        payableQuery.setSupplierId(databasePurchaseReturn.getSupplierId());
        List<FinPayable> payableList = finPayableMapper.selectFinPayableList(payableQuery);
        if (payableList == null || payableList.isEmpty())
        {
            return;
        }
        BigDecimal remainOffsetAmount = databasePurchaseReturn.getTotalAmount();
        for (FinPayable finPayable : payableList)
        {
            if (remainOffsetAmount.compareTo(BigDecimal.ZERO) <= 0)
            {
                break;
            }
            if (!"0".equals(finPayable.getStatus()) && !"1".equals(finPayable.getStatus()))
            {
                continue;
            }
            BigDecimal amountDue = finPayable.getAmountDue() == null ? BigDecimal.ZERO : finPayable.getAmountDue();
            BigDecimal amountPaid = finPayable.getAmountPaid() == null ? BigDecimal.ZERO : finPayable.getAmountPaid();
            BigDecimal remainAmount = amountDue.subtract(amountPaid);
            if (remainAmount.compareTo(BigDecimal.ZERO) <= 0)
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
    }
}
