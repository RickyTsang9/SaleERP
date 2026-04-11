package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.BizMessage;
import com.ruoyi.business.domain.FinReceivable;
import com.ruoyi.business.domain.MdCustomer;
import com.ruoyi.business.domain.WmsOutbound;
import com.ruoyi.business.domain.WmsSaleOrder;
import com.ruoyi.business.domain.WmsSaleOrderItem;
import com.ruoyi.business.domain.WmsSaleOrderStatusHistory;
import com.ruoyi.business.domain.WmsStock;
import com.ruoyi.business.domain.WmsStockLog;
import com.ruoyi.business.mapper.FinReceivableMapper;
import com.ruoyi.business.mapper.MdCustomerMapper;
import com.ruoyi.business.mapper.WmsOutboundMapper;
import com.ruoyi.business.mapper.WmsSaleOrderItemMapper;
import com.ruoyi.business.mapper.WmsSaleOrderMapper;
import com.ruoyi.business.mapper.WmsSaleOrderStatusHistoryMapper;
import com.ruoyi.business.mapper.WmsStockLogMapper;
import com.ruoyi.business.mapper.WmsStockMapper;
import com.ruoyi.business.service.IBizMessageService;
import com.ruoyi.business.service.IWmsSaleOrderService;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

@Service
public class WmsSaleOrderServiceImpl implements IWmsSaleOrderService
{
    private static final String STATUS_DRAFT = "draft";

    private static final String STATUS_SUBMITTED = "submitted";

    private static final String STATUS_MANAGER_APPROVED = "manager_approved";

    private static final String STATUS_AUDITED = "audited";

    private static final String STATUS_CANCELLED = "cancelled";

    private static final String MESSAGE_TYPE_SALE_ORDER_AUDIT = "sale_order_audit";

    private static final String MESSAGE_LEVEL_WARNING = "warning";

    private static final String MESSAGE_LEVEL_NORMAL = "normal";

    private static final String MESSAGE_STATUS_ACTIVE = "0";

    @Autowired
    private WmsSaleOrderMapper wmsSaleOrderMapper;

    @Autowired
    private WmsSaleOrderItemMapper wmsSaleOrderItemMapper;

    @Autowired
    private WmsStockMapper wmsStockMapper;

    @Autowired
    private WmsStockLogMapper wmsStockLogMapper;

    @Autowired
    private FinReceivableMapper finReceivableMapper;

    @Autowired
    private MdCustomerMapper mdCustomerMapper;

    @Autowired
    private WmsOutboundMapper wmsOutboundMapper;

    @Autowired
    private WmsSaleOrderStatusHistoryMapper wmsSaleOrderStatusHistoryMapper;

    @Autowired
    private IBizMessageService bizMessageService;

    @Override
    public WmsSaleOrder selectWmsSaleOrderById(Long saleOrderId)
    {
        return wmsSaleOrderMapper.selectWmsSaleOrderById(saleOrderId);
    }

    @Override
    @DataScope(deptAlias = "su", userAlias = "su", permission = "business:saleOrder:list")
    public List<WmsSaleOrder> selectWmsSaleOrderList(WmsSaleOrder wmsSaleOrder)
    {
        return wmsSaleOrderMapper.selectWmsSaleOrderList(wmsSaleOrder);
    }

    @Override
    public int insertWmsSaleOrder(WmsSaleOrder wmsSaleOrder)
    {
        if (StringUtils.isEmpty(wmsSaleOrder.getOrderNo()))
        {
            wmsSaleOrder.setOrderNo(generateOrderNo());
        }
        wmsSaleOrder.setStatus(STATUS_DRAFT);
        wmsSaleOrder.setPaymentStatus("unpaid");
        int insertCount = wmsSaleOrderMapper.insertWmsSaleOrder(wmsSaleOrder);
        saveStatusHistory(wmsSaleOrder.getSaleOrderId(), null, wmsSaleOrder.getStatus(), "create", "system", null, wmsSaleOrder.getCreateBy());
        return insertCount;
    }

    @Override
    public int updateWmsSaleOrder(WmsSaleOrder wmsSaleOrder)
    {
        WmsSaleOrder databaseSaleOrder = wmsSaleOrderMapper.selectWmsSaleOrderById(wmsSaleOrder.getSaleOrderId());
        if (databaseSaleOrder == null)
        {
            throw new ServiceException("销售单不存在");
        }
        if (!STATUS_DRAFT.equals(databaseSaleOrder.getStatus()))
        {
            throw new ServiceException("仅草稿状态销售单允许修改");
        }
        return wmsSaleOrderMapper.updateWmsSaleOrder(wmsSaleOrder);
    }

    @Override
    public int submitWmsSaleOrder(Long saleOrderId)
    {
        WmsSaleOrder databaseSaleOrder = wmsSaleOrderMapper.selectWmsSaleOrderById(saleOrderId);
        if (databaseSaleOrder == null)
        {
            throw new ServiceException("销售单不存在");
        }
        if (!STATUS_DRAFT.equals(databaseSaleOrder.getStatus()))
        {
            throw new ServiceException("仅草稿状态销售单允许提交");
        }
        MdCustomer databaseCustomer = mdCustomerMapper.selectMdCustomerById(databaseSaleOrder.getCustomerId());
        if (databaseCustomer == null)
        {
            throw new ServiceException("销售单客户不存在");
        }
        if ("credit".equals(databaseCustomer.getPaymentMethod()))
        {
            BigDecimal customerCreditLimit =
                databaseCustomer.getCreditLimit() == null ? BigDecimal.ZERO : databaseCustomer.getCreditLimit();
            if (customerCreditLimit.compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new ServiceException("客户未设置可用信用额度，不允许提交赊销销售单");
            }
            BigDecimal customerOutstandingAmount =
                finReceivableMapper.selectCustomerOutstandingAmount(databaseSaleOrder.getCustomerId());
            if (customerOutstandingAmount == null)
            {
                customerOutstandingAmount = BigDecimal.ZERO;
            }
            BigDecimal customerPendingApprovalAmount =
                wmsSaleOrderMapper.selectCustomerPendingApprovalAmount(databaseSaleOrder.getCustomerId());
            if (customerPendingApprovalAmount == null)
            {
                customerPendingApprovalAmount = BigDecimal.ZERO;
            }
            BigDecimal currentSaleOrderAmount =
                databaseSaleOrder.getTotalAmount() == null ? BigDecimal.ZERO : databaseSaleOrder.getTotalAmount();
            BigDecimal projectedCreditAmount =
                customerOutstandingAmount.add(customerPendingApprovalAmount).add(currentSaleOrderAmount);
            if (projectedCreditAmount.compareTo(customerCreditLimit) > 0)
            {
                throw new ServiceException(
                    "客户信用额度不足，当前占用:" + projectedCreditAmount + "，信用额度:" + customerCreditLimit);
            }
        }
        String operationUsername = SecurityUtils.getUsername();
        int updateCount = wmsSaleOrderMapper.updateWmsSaleOrderStatus(
            saleOrderId, STATUS_DRAFT, STATUS_SUBMITTED, null, null, operationUsername);
        if (updateCount == 0)
        {
            throw new ServiceException("销售单状态已变更，请刷新后重试");
        }
        saveStatusHistory(saleOrderId, databaseSaleOrder.getStatus(), STATUS_SUBMITTED, "submit", "sales", null, operationUsername);
        pushSaleOrderAuditMessage(
            databaseSaleOrder, "销售单待经理审核-" + databaseSaleOrder.getOrderNo(), "销售单已提交，等待经理审核。", MESSAGE_LEVEL_WARNING, operationUsername);
        return updateCount;
    }

    /**
     * 经理审核销售单
     */
    @Override
    public int managerAuditWmsSaleOrder(Long saleOrderId, String auditComment)
    {
        WmsSaleOrder databaseSaleOrder = wmsSaleOrderMapper.selectWmsSaleOrderById(saleOrderId);
        if (databaseSaleOrder == null)
        {
            throw new ServiceException("销售单不存在");
        }
        if (!STATUS_SUBMITTED.equals(databaseSaleOrder.getStatus()))
        {
            throw new ServiceException("仅已提交销售单允许经理审核");
        }
        String managerAuditUsername = SecurityUtils.getUsername();
        int updateCount = wmsSaleOrderMapper.updateWmsSaleOrderManagerAudit(
            saleOrderId, STATUS_SUBMITTED, STATUS_MANAGER_APPROVED, managerAuditUsername, auditComment, managerAuditUsername);
        if (updateCount == 0)
        {
            throw new ServiceException("销售单状态已变更，请刷新后重试");
        }
        saveStatusHistory(
            saleOrderId, databaseSaleOrder.getStatus(), STATUS_MANAGER_APPROVED, "manager_audit", "manager", auditComment, managerAuditUsername);
        pushSaleOrderAuditMessage(
            databaseSaleOrder, "销售单待财务审核-" + databaseSaleOrder.getOrderNo(), "销售单经理审核通过，等待财务审核。", MESSAGE_LEVEL_WARNING, managerAuditUsername);
        return updateCount;
    }

    /**
     * 经理回退销售单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int managerRollbackWmsSaleOrder(Long saleOrderId, String auditComment)
    {
        WmsSaleOrder databaseSaleOrder = wmsSaleOrderMapper.selectWmsSaleOrderById(saleOrderId);
        if (databaseSaleOrder == null)
        {
            throw new ServiceException("销售单不存在");
        }
        if (!STATUS_MANAGER_APPROVED.equals(databaseSaleOrder.getStatus()))
        {
            throw new ServiceException("仅经理已审核销售单允许经理回退");
        }
        String managerAuditUsername = SecurityUtils.getUsername();
        int updateCount = wmsSaleOrderMapper.rollbackWmsSaleOrderToSubmitted(
            saleOrderId, STATUS_MANAGER_APPROVED, managerAuditUsername);
        if (updateCount == 0)
        {
            throw new ServiceException("销售单状态已变更，请刷新后重试");
        }
        saveStatusHistory(
            saleOrderId, databaseSaleOrder.getStatus(), STATUS_SUBMITTED, "manager_rollback", "manager", auditComment, managerAuditUsername);
        pushSaleOrderAuditMessage(
            databaseSaleOrder, "销售单经理回退-" + databaseSaleOrder.getOrderNo(), "销售单已被经理回退至提交状态。", MESSAGE_LEVEL_WARNING, managerAuditUsername);
        return updateCount;
    }

    /**
     * 财务审核销售单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int financeAuditWmsSaleOrder(Long saleOrderId, String auditComment)
    {
        WmsSaleOrder databaseSaleOrder = wmsSaleOrderMapper.selectWmsSaleOrderById(saleOrderId);
        if (databaseSaleOrder == null)
        {
            throw new ServiceException("销售单不存在");
        }
        if (!STATUS_MANAGER_APPROVED.equals(databaseSaleOrder.getStatus()))
        {
            throw new ServiceException("仅经理已审核销售单允许财务审核");
        }
        List<WmsSaleOrderItem> itemList = wmsSaleOrderItemMapper.selectWmsSaleOrderItemsByOrderId(saleOrderId);
        if (itemList == null || itemList.isEmpty())
        {
            throw new ServiceException("销售明细不能为空");
        }
        for (WmsSaleOrderItem wmsSaleOrderItem : itemList)
        {
            WmsStock stockQuery = new WmsStock();
            stockQuery.setWarehouseId(databaseSaleOrder.getWarehouseId());
            stockQuery.setProductId(wmsSaleOrderItem.getProductId());
            stockQuery.setLocationId(wmsSaleOrderItem.getLocationId());
            stockQuery.setBatchNo(wmsSaleOrderItem.getBatchNo());
            WmsStock databaseStock = wmsStockMapper.selectWmsStockByKey(stockQuery);
            if (databaseStock == null || databaseStock.getQuantity() == null)
            {
                throw new ServiceException("库存不存在，商品编号：" + wmsSaleOrderItem.getProductId());
            }
            if (databaseStock.getQuantity().compareTo(wmsSaleOrderItem.getQuantity()) < 0)
            {
                throw new ServiceException("库存不足，商品编号：" + wmsSaleOrderItem.getProductId());
            }
            BigDecimal beforeQuantity = databaseStock.getQuantity();
            BigDecimal afterQuantity = beforeQuantity.subtract(wmsSaleOrderItem.getQuantity());
            databaseStock.setQuantity(afterQuantity);
            databaseStock.setUpdateBy(SecurityUtils.getUsername());
            wmsStockMapper.updateWmsStock(databaseStock);
            WmsStockLog stockLog = new WmsStockLog();
            stockLog.setWarehouseId(databaseSaleOrder.getWarehouseId());
            stockLog.setProductId(wmsSaleOrderItem.getProductId());
            stockLog.setLocationId(wmsSaleOrderItem.getLocationId());
            stockLog.setBatchNo(wmsSaleOrderItem.getBatchNo());
            stockLog.setBillType("sale_order");
            stockLog.setBillId(saleOrderId);
            stockLog.setInOut("out");
            stockLog.setQuantity(wmsSaleOrderItem.getQuantity());
            stockLog.setPrice(wmsSaleOrderItem.getPrice());
            stockLog.setAmount(wmsSaleOrderItem.getAmount());
            stockLog.setBeforeQty(beforeQuantity);
            stockLog.setAfterQty(afterQuantity);
            wmsStockLogMapper.insertWmsStockLog(stockLog);
        }
        String auditUsername = SecurityUtils.getUsername();
        int updateCount = wmsSaleOrderMapper.updateWmsSaleOrderStatus(
            saleOrderId, STATUS_MANAGER_APPROVED, STATUS_AUDITED, auditUsername, auditComment, auditUsername);
        if (updateCount == 0)
        {
            throw new ServiceException("销售单状态已变更，请刷新后重试");
        }
        FinReceivable databaseReceivable = finReceivableMapper.selectFinReceivableBySaleOrderId(saleOrderId);
        if (databaseReceivable == null)
        {
            FinReceivable finReceivable = new FinReceivable();
            finReceivable.setSaleOrderId(databaseSaleOrder.getSaleOrderId());
            finReceivable.setCustomerId(databaseSaleOrder.getCustomerId());
            finReceivable.setAmountDue(databaseSaleOrder.getTotalAmount());
            finReceivable.setAmountPaid(BigDecimal.ZERO);
            finReceivable.setStatus("unpaid");
            Calendar dueCalendar = Calendar.getInstance();
            dueCalendar.add(Calendar.DAY_OF_MONTH, 30);
            finReceivable.setDueDate(dueCalendar.getTime());
            finReceivableMapper.insertFinReceivable(finReceivable);
        }
        saveStatusHistory(
            saleOrderId, databaseSaleOrder.getStatus(), STATUS_AUDITED, "finance_audit", "finance", auditComment, auditUsername);
        pushSaleOrderAuditMessage(
            databaseSaleOrder, "销售单财务审核通过-" + databaseSaleOrder.getOrderNo(), "销售单已完成财务审核。", MESSAGE_LEVEL_NORMAL, auditUsername);
        return 1;
    }

    /**
     * 财务回退销售单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int financeRollbackWmsSaleOrder(Long saleOrderId, String auditComment)
    {
        WmsSaleOrder databaseSaleOrder = wmsSaleOrderMapper.selectWmsSaleOrderById(saleOrderId);
        if (databaseSaleOrder == null)
        {
            throw new ServiceException("销售单不存在");
        }
        if (!STATUS_AUDITED.equals(databaseSaleOrder.getStatus()))
        {
            throw new ServiceException("仅财务已审核销售单允许财务回退");
        }
        FinReceivable databaseReceivable = finReceivableMapper.selectFinReceivableBySaleOrderId(saleOrderId);
        if (databaseReceivable != null)
        {
            BigDecimal paidAmount = databaseReceivable.getAmountPaid() == null ? BigDecimal.ZERO : databaseReceivable.getAmountPaid();
            if (paidAmount.compareTo(BigDecimal.ZERO) > 0)
            {
                throw new ServiceException("销售单存在回款记录，不允许财务回退");
            }
            finReceivableMapper.deleteFinReceivableById(databaseReceivable.getReceivableId());
        }
        validateNoLinkedOutboundForFinanceRollback(databaseSaleOrder);
        List<WmsSaleOrderItem> itemList = wmsSaleOrderItemMapper.selectWmsSaleOrderItemsByOrderId(saleOrderId);
        if (itemList == null || itemList.isEmpty())
        {
            throw new ServiceException("销售明细不能为空");
        }
        String financeUsername = SecurityUtils.getUsername();
        for (WmsSaleOrderItem wmsSaleOrderItem : itemList)
        {
            WmsStock stockQuery = new WmsStock();
            stockQuery.setWarehouseId(databaseSaleOrder.getWarehouseId());
            stockQuery.setProductId(wmsSaleOrderItem.getProductId());
            stockQuery.setLocationId(wmsSaleOrderItem.getLocationId());
            stockQuery.setBatchNo(wmsSaleOrderItem.getBatchNo());
            WmsStock databaseStock = wmsStockMapper.selectWmsStockByKey(stockQuery);
            if (databaseStock == null)
            {
                throw new ServiceException("库存不存在，商品编号：" + wmsSaleOrderItem.getProductId());
            }
            BigDecimal beforeQuantity = databaseStock.getQuantity() == null ? BigDecimal.ZERO : databaseStock.getQuantity();
            BigDecimal afterQuantity = beforeQuantity.add(wmsSaleOrderItem.getQuantity());
            databaseStock.setQuantity(afterQuantity);
            databaseStock.setUpdateBy(financeUsername);
            wmsStockMapper.updateWmsStock(databaseStock);
            WmsStockLog stockLog = new WmsStockLog();
            stockLog.setWarehouseId(databaseSaleOrder.getWarehouseId());
            stockLog.setProductId(wmsSaleOrderItem.getProductId());
            stockLog.setLocationId(wmsSaleOrderItem.getLocationId());
            stockLog.setBatchNo(wmsSaleOrderItem.getBatchNo());
            stockLog.setBillType("sale_order_rollback");
            stockLog.setBillId(saleOrderId);
            stockLog.setInOut("in");
            stockLog.setQuantity(wmsSaleOrderItem.getQuantity());
            stockLog.setPrice(wmsSaleOrderItem.getPrice());
            stockLog.setAmount(wmsSaleOrderItem.getAmount());
            stockLog.setBeforeQty(beforeQuantity);
            stockLog.setAfterQty(afterQuantity);
            wmsStockLogMapper.insertWmsStockLog(stockLog);
        }
        int updateCount = wmsSaleOrderMapper.rollbackWmsSaleOrderToManagerApproved(
            saleOrderId, STATUS_AUDITED, financeUsername);
        if (updateCount == 0)
        {
            throw new ServiceException("销售单状态已变更，请刷新后重试");
        }
        saveStatusHistory(
            saleOrderId, databaseSaleOrder.getStatus(), STATUS_MANAGER_APPROVED, "finance_rollback", "finance", auditComment, financeUsername);
        pushSaleOrderAuditMessage(
            databaseSaleOrder, "销售单财务回退-" + databaseSaleOrder.getOrderNo(), "销售单已被财务回退至经理已审核状态。", MESSAGE_LEVEL_WARNING, financeUsername);
        return updateCount;
    }

    @Override
    public int cancelWmsSaleOrder(Long saleOrderId)
    {
        WmsSaleOrder databaseSaleOrder = wmsSaleOrderMapper.selectWmsSaleOrderById(saleOrderId);
        if (databaseSaleOrder == null)
        {
            throw new ServiceException("销售单不存在");
        }
        if (STATUS_AUDITED.equals(databaseSaleOrder.getStatus()))
        {
            throw new ServiceException("已审核销售单不允许作废");
        }
        String operationUsername = SecurityUtils.getUsername();
        int updateCount = wmsSaleOrderMapper.updateWmsSaleOrderStatus(
            saleOrderId, databaseSaleOrder.getStatus(), STATUS_CANCELLED, null, null, operationUsername);
        if (updateCount == 0)
        {
            throw new ServiceException("销售单状态已变更，请刷新后重试");
        }
        saveStatusHistory(saleOrderId, databaseSaleOrder.getStatus(), STATUS_CANCELLED, "cancel", "sales", null, operationUsername);
        return updateCount;
    }

    @Override
    public int deleteWmsSaleOrderById(Long saleOrderId)
    {
        wmsSaleOrderItemMapper.deleteWmsSaleOrderItemBySaleOrderIds(new Long[] { saleOrderId });
        return wmsSaleOrderMapper.deleteWmsSaleOrderById(saleOrderId);
    }

    @Override
    public int deleteWmsSaleOrderByIds(Long[] saleOrderIds)
    {
        wmsSaleOrderItemMapper.deleteWmsSaleOrderItemBySaleOrderIds(saleOrderIds);
        return wmsSaleOrderMapper.deleteWmsSaleOrderByIds(saleOrderIds);
    }

    /**
     * 导入销售订单数据
     */
    @Override
    public String importWmsSaleOrder(List<WmsSaleOrder> saleOrderList, boolean updateSupport, String operatorName)
    {
        if (saleOrderList == null || saleOrderList.isEmpty())
        {
            throw new ServiceException("导入销售订单数据不能为空");
        }
        int successCount = 0;
        int failureCount = 0;
        StringBuilder successMessageBuilder = new StringBuilder();
        StringBuilder failureMessageBuilder = new StringBuilder();
        for (WmsSaleOrder saleOrder : saleOrderList)
        {
            try
            {
                validateImportData(saleOrder);
                WmsSaleOrder databaseSaleOrder = wmsSaleOrderMapper.selectWmsSaleOrderByOrderNo(saleOrder.getOrderNo());
                if (databaseSaleOrder == null)
                {
                    saleOrder.setCreateBy(operatorName);
                    insertWmsSaleOrder(saleOrder);
                    successCount++;
                    successMessageBuilder.append("<br/>").append(successCount).append("、销售单号 ").append(saleOrder.getOrderNo()).append(" 导入成功");
                    continue;
                }
                if (!updateSupport)
                {
                    failureCount++;
                    failureMessageBuilder.append("<br/>").append(failureCount).append("、销售单号 ").append(saleOrder.getOrderNo()).append(" 已存在");
                    continue;
                }
                if (!STATUS_DRAFT.equals(databaseSaleOrder.getStatus()))
                {
                    failureCount++;
                    failureMessageBuilder.append("<br/>").append(failureCount).append("、销售单号 ").append(saleOrder.getOrderNo()).append(" 非草稿状态，禁止覆盖");
                    continue;
                }
                saleOrder.setSaleOrderId(databaseSaleOrder.getSaleOrderId());
                saleOrder.setUpdateBy(operatorName);
                updateWmsSaleOrder(saleOrder);
                successCount++;
                successMessageBuilder.append("<br/>").append(successCount).append("、销售单号 ").append(saleOrder.getOrderNo()).append(" 更新成功");
            }
            catch (Exception exception)
            {
                failureCount++;
                failureMessageBuilder.append("<br/>").append(failureCount).append("、销售单号 ").append(saleOrder.getOrderNo()).append(" 导入失败：").append(exception.getMessage());
            }
        }
        if (failureCount > 0)
        {
            failureMessageBuilder.insert(0, "导入失败，共 " + failureCount + " 条数据格式或状态不符合要求，详情如下：");
            throw new ServiceException(failureMessageBuilder.toString());
        }
        successMessageBuilder.insert(0, "导入成功，共 " + successCount + " 条数据，详情如下：");
        return successMessageBuilder.toString();
    }

    /**
     * 获取销售订单打印模板数据
     */
    @Override
    public Map<String, Object> getPrintTemplateData(Long saleOrderId)
    {
        WmsSaleOrder databaseSaleOrder = selectWmsSaleOrderById(saleOrderId);
        if (databaseSaleOrder == null)
        {
            throw new ServiceException("销售单不存在");
        }
        Map<String, Object> printTemplateDataMap = new HashMap<>();
        printTemplateDataMap.put("saleOrder", databaseSaleOrder);
        List<WmsSaleOrderItem> saleOrderItemList = wmsSaleOrderItemMapper.selectWmsSaleOrderItemsByOrderId(saleOrderId);
        if (saleOrderItemList == null)
        {
            saleOrderItemList = new ArrayList<>();
        }
        printTemplateDataMap.put("itemList", saleOrderItemList);
        printTemplateDataMap.put("printTime", new Date());
        return printTemplateDataMap;
    }

    /**
     * 查询销售单状态历史
     */
    @Override
    public List<WmsSaleOrderStatusHistory> selectWmsSaleOrderStatusHistoryList(Long saleOrderId)
    {
        return wmsSaleOrderStatusHistoryMapper.selectWmsSaleOrderStatusHistoryList(saleOrderId);
    }

    /**
     * 校验导入销售订单数据
     */
    private void validateImportData(WmsSaleOrder saleOrder)
    {
        if (saleOrder == null)
        {
            throw new ServiceException("销售订单数据不能为空");
        }
        if (StringUtils.isEmpty(saleOrder.getOrderNo()))
        {
            throw new ServiceException("销售单号不能为空");
        }
        if (saleOrder.getCustomerId() == null)
        {
            throw new ServiceException("客户编号不能为空");
        }
        if (saleOrder.getWarehouseId() == null)
        {
            throw new ServiceException("仓库编号不能为空");
        }
        if (saleOrder.getTotalQty() == null)
        {
            throw new ServiceException("总数量不能为空");
        }
        if (saleOrder.getTotalAmount() == null)
        {
            throw new ServiceException("总金额不能为空");
        }
        if (StringUtils.isEmpty(saleOrder.getStatus()))
        {
            saleOrder.setStatus(STATUS_DRAFT);
        }
        if (StringUtils.isEmpty(saleOrder.getPaymentStatus()))
        {
            saleOrder.setPaymentStatus("unpaid");
        }
    }

    private String generateOrderNo()
    {
        String dateText = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String noPrefix = "xs" + dateText;
        String maxOrderNo = wmsSaleOrderMapper.selectMaxOrderNoByPrefix(noPrefix);
        if (StringUtils.isEmpty(maxOrderNo))
        {
            return noPrefix + "0001";
        }
        String sequenceText = maxOrderNo.substring(maxOrderNo.length() - 4);
        Integer nextSequence = Integer.parseInt(sequenceText) + 1;
        return noPrefix + String.format("%04d", nextSequence);
    }

    /**
     * 保存销售单状态历史
     */
    private void saveStatusHistory(Long saleOrderId, String fromStatus, String toStatus, String operationType, String auditRole, String auditComment, String operationUsername)
    {
        WmsSaleOrderStatusHistory statusHistory = new WmsSaleOrderStatusHistory();
        statusHistory.setSaleOrderId(saleOrderId);
        statusHistory.setFromStatus(fromStatus);
        statusHistory.setToStatus(toStatus);
        statusHistory.setOperationType(operationType);
        statusHistory.setAuditRole(auditRole);
        statusHistory.setAuditBy(operationUsername);
        statusHistory.setAuditComment(auditComment);
        statusHistory.setOperateTime(new Date());
        statusHistory.setCreateBy(operationUsername);
        wmsSaleOrderStatusHistoryMapper.insertWmsSaleOrderStatusHistory(statusHistory);
    }

    private void pushSaleOrderAuditMessage(
        WmsSaleOrder saleOrder, String messageTitle, String messageContent, String messageLevel, String operationUsername)
    {
        BizMessage bizMessage = new BizMessage();
        bizMessage.setMessageType(MESSAGE_TYPE_SALE_ORDER_AUDIT);
        bizMessage.setMessageTitle(messageTitle);
        bizMessage.setMessageContent(messageContent);
        bizMessage.setMessageLevel(messageLevel);
        bizMessage.setBusinessType("sale_order");
        bizMessage.setBusinessId(saleOrder.getSaleOrderId());
        bizMessage.setStatus(MESSAGE_STATUS_ACTIVE);
        bizMessage.setCreateBy(operationUsername);
        bizMessageService.insertBizMessage(bizMessage);
    }

    /**
     * 校验财务回退前是否已生成销售出库单
     *
     * @param databaseSaleOrder 销售单
     */
    private void validateNoLinkedOutboundForFinanceRollback(WmsSaleOrder databaseSaleOrder)
    {
        WmsOutbound linkedOutboundQuery = new WmsOutbound();
        linkedOutboundQuery.setSaleOrderId(databaseSaleOrder.getSaleOrderId());
        List<WmsOutbound> linkedOutboundList = wmsOutboundMapper.selectWmsOutboundList(linkedOutboundQuery);
        if (hasActiveOutbound(linkedOutboundList))
        {
            throw new ServiceException("销售单已生成销售出库单，请先处理相关出库单后再执行财务回退");
        }
        WmsOutbound historicalOutboundQuery = new WmsOutbound();
        historicalOutboundQuery.setSourceOrderNo(databaseSaleOrder.getOrderNo());
        List<WmsOutbound> historicalOutboundList = wmsOutboundMapper.selectWmsOutboundList(historicalOutboundQuery);
        if (hasActiveOutbound(historicalOutboundList))
        {
            throw new ServiceException("销售单已生成销售出库单，请先处理相关出库单后再执行财务回退");
        }
    }

    /**
     * 判断是否存在未作废的销售出库单
     *
     * @param outboundList 销售出库单列表
     * @return 是否存在未作废的销售出库单
     */
    private boolean hasActiveOutbound(List<WmsOutbound> outboundList)
    {
        if (outboundList == null || outboundList.isEmpty())
        {
            return false;
        }
        for (WmsOutbound databaseOutbound : outboundList)
        {
            if (!STATUS_CANCELLED.equals(databaseOutbound.getStatus()))
            {
                return true;
            }
        }
        return false;
    }
}
