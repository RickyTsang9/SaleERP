package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.FinReceivable;
import com.ruoyi.business.domain.WmsSaleReturn;
import com.ruoyi.business.domain.WmsSaleReturnItem;
import com.ruoyi.business.domain.WmsStock;
import com.ruoyi.business.domain.WmsStockLog;
import com.ruoyi.business.mapper.FinReceivableMapper;
import com.ruoyi.business.mapper.WmsSaleReturnItemMapper;
import com.ruoyi.business.mapper.WmsSaleReturnMapper;
import com.ruoyi.business.mapper.WmsStockLogMapper;
import com.ruoyi.business.mapper.WmsStockMapper;
import com.ruoyi.business.service.IWmsSaleReturnService;
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
    public int insertWmsSaleReturn(WmsSaleReturn wmsSaleReturn)
    {
        if (StringUtils.isEmpty(wmsSaleReturn.getReturnNo()))
        {
            wmsSaleReturn.setReturnNo(generateSaleReturnNo());
        }
        wmsSaleReturn.setStatus(STATUS_DRAFT);
        return wmsSaleReturnMapper.insertWmsSaleReturn(wmsSaleReturn);
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
        List<WmsSaleReturnItem> itemList = wmsSaleReturnItemMapper.selectWmsSaleReturnItemsByReturnId(saleReturnId);
        if (itemList == null || itemList.isEmpty())
        {
            throw new ServiceException("销售退货明细不能为空");
        }
        for (WmsSaleReturnItem wmsSaleReturnItem : itemList)
        {
            WmsStock databaseStock = wmsStockMapper.selectFirstWmsStockByWarehouseAndProduct(
                databaseSaleReturn.getWarehouseId(), wmsSaleReturnItem.getProductId());
            BigDecimal beforeQuantity = BigDecimal.ZERO;
            BigDecimal afterQuantity;
            if (databaseStock == null)
            {
                databaseStock = new WmsStock();
                databaseStock.setWarehouseId(databaseSaleReturn.getWarehouseId());
                databaseStock.setProductId(wmsSaleReturnItem.getProductId());
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
            afterQuantity = beforeQuantity.add(wmsSaleReturnItem.getQuantity());
            databaseStock.setQuantity(afterQuantity);
            wmsStockMapper.updateWmsStock(databaseStock);
            WmsStockLog stockLog = new WmsStockLog();
            stockLog.setWarehouseId(databaseSaleReturn.getWarehouseId());
            stockLog.setProductId(wmsSaleReturnItem.getProductId());
            stockLog.setLocationId(databaseStock.getLocationId());
            stockLog.setBatchNo(databaseStock.getBatchNo());
            stockLog.setBillType("sale_return");
            stockLog.setBillId(saleReturnId);
            stockLog.setInOut("in");
            stockLog.setQuantity(wmsSaleReturnItem.getQuantity());
            stockLog.setPrice(wmsSaleReturnItem.getPrice());
            stockLog.setAmount(wmsSaleReturnItem.getAmount());
            stockLog.setBeforeQty(beforeQuantity);
            stockLog.setAfterQty(afterQuantity);
            wmsStockLogMapper.insertWmsStockLog(stockLog);
        }
        processReceivableOffset(databaseSaleReturn);
        String auditUsername = SecurityUtils.getUsername();
        int updateCount = wmsSaleReturnMapper.updateWmsSaleReturnStatus(
            saleReturnId, STATUS_SUBMITTED, STATUS_AUDITED, auditUsername, auditUsername);
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
     * 使用销售退货金额冲减客户未结清应收台账
     *
     * @param databaseSaleReturn 销售退货单
     */
    private void processReceivableOffset(WmsSaleReturn databaseSaleReturn)
    {
        if (databaseSaleReturn.getCustomerId() == null || databaseSaleReturn.getTotalAmount() == null
            || databaseSaleReturn.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            return;
        }
        List<FinReceivable> pendingReceivableList =
            finReceivableMapper.selectPendingFinReceivableListByCustomerId(databaseSaleReturn.getCustomerId());
        if (pendingReceivableList == null || pendingReceivableList.isEmpty())
        {
            return;
        }
        BigDecimal remainOffsetAmount = databaseSaleReturn.getTotalAmount();
        for (FinReceivable pendingReceivable : pendingReceivableList)
        {
            if (remainOffsetAmount.compareTo(BigDecimal.ZERO) <= 0)
            {
                break;
            }
            BigDecimal amountDue = pendingReceivable.getAmountDue() == null ? BigDecimal.ZERO : pendingReceivable.getAmountDue();
            BigDecimal amountPaid = pendingReceivable.getAmountPaid() == null ? BigDecimal.ZERO : pendingReceivable.getAmountPaid();
            BigDecimal outstandingAmount = amountDue.subtract(amountPaid);
            if (outstandingAmount.compareTo(BigDecimal.ZERO) <= 0)
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
            String updatedStatus;
            if (updatedAmountDue.compareTo(BigDecimal.ZERO) == 0)
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
            remainOffsetAmount = remainOffsetAmount.subtract(currentOffsetAmount);
        }
    }
}
