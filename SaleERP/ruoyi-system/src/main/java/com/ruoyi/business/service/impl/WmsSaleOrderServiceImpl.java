package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.BizMessage;
import com.ruoyi.business.domain.FinInvoice;
import com.ruoyi.business.domain.FinReceivable;
import com.ruoyi.business.domain.FinReceipt;
import com.ruoyi.business.domain.MdCustomer;
import com.ruoyi.business.domain.MdProduct;
import com.ruoyi.business.domain.WmsOutbound;
import com.ruoyi.business.domain.WmsSaleOrder;
import com.ruoyi.business.domain.WmsSaleOrderItem;
import com.ruoyi.business.domain.WmsSaleOrderStatusHistory;
import com.ruoyi.business.domain.WmsSaleReturn;
import com.ruoyi.business.domain.WmsStock;
import com.ruoyi.business.domain.WmsStockLog;
import com.ruoyi.business.mapper.FinInvoiceMapper;
import com.ruoyi.business.mapper.FinReceivableMapper;
import com.ruoyi.business.mapper.MdCustomerMapper;
import com.ruoyi.business.mapper.MdProductMapper;
import com.ruoyi.business.mapper.WmsOutboundMapper;
import com.ruoyi.business.mapper.WmsSaleOrderItemMapper;
import com.ruoyi.business.mapper.WmsSaleOrderMapper;
import com.ruoyi.business.mapper.WmsSaleOrderStatusHistoryMapper;
import com.ruoyi.business.mapper.WmsSaleReturnMapper;
import com.ruoyi.business.mapper.WmsStockLogMapper;
import com.ruoyi.business.mapper.WmsStockMapper;
import com.ruoyi.business.service.IBizMessageService;
import com.ruoyi.business.service.IFinReceiptService;
import com.ruoyi.business.service.IWmsSaleOrderService;
import com.ruoyi.business.service.IWmsStockService;
import com.ruoyi.business.service.support.BizComplianceHelper;
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

    private static final String STATUS_CLOSED = "closed";

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
    private IFinReceiptService finReceiptService;

    @Autowired
    private MdCustomerMapper mdCustomerMapper;

    @Autowired
    private WmsOutboundMapper wmsOutboundMapper;

    @Autowired
    private WmsSaleOrderStatusHistoryMapper wmsSaleOrderStatusHistoryMapper;

    @Autowired
    private WmsSaleReturnMapper wmsSaleReturnMapper;

    @Autowired
    private IBizMessageService bizMessageService;

    @Autowired
    private IWmsStockService wmsStockService;

    @Autowired(required = false)
    private MdProductMapper mdProductMapper;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private BizComplianceHelper complianceHelper;

    @Autowired(required = false)
    private FinInvoiceMapper finInvoiceMapper;

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

    /**
     * 新增销售单
     *
     * @param wmsSaleOrder 销售单
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int insertWmsSaleOrder(WmsSaleOrder wmsSaleOrder)
    {
        if (complianceHelper != null)
        {
            complianceHelper.assertEntityEnabled("md_customer", "customer_id", wmsSaleOrder.getCustomerId(), "客户");
            complianceHelper.assertEntityEnabled("md_warehouse", "warehouse_id", wmsSaleOrder.getWarehouseId(), "仓库");
        }
        if (StringUtils.isEmpty(wmsSaleOrder.getOrderNo()))
        {
            wmsSaleOrder.setOrderNo(generateOrderNo());
        }
        wmsSaleOrder.setStatus(STATUS_DRAFT);
        wmsSaleOrder.setPaymentStatus("unpaid");
        if (wmsSaleOrder.getTotalQty() == null)
        {
            wmsSaleOrder.setTotalQty(BigDecimal.ZERO);
        }
        if (wmsSaleOrder.getTotalAmount() == null)
        {
            wmsSaleOrder.setTotalAmount(BigDecimal.ZERO);
        }
        int insertCount = wmsSaleOrderMapper.insertWmsSaleOrder(wmsSaleOrder);
        if (wmsSaleOrder.getItems() != null && !wmsSaleOrder.getItems().isEmpty())
        {
            BigDecimal sumQty = BigDecimal.ZERO;
            BigDecimal sumAmount = BigDecimal.ZERO;
            for (WmsSaleOrderItem item : wmsSaleOrder.getItems())
            {
                if (item == null || item.getProductId() == null)
                {
                    continue;
                }
                if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
                {
                    throw new ServiceException("销售明细数量必须大于0");
                }
                if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) < 0)
                {
                    throw new ServiceException("销售明细单价不能小于0");
                }
                if (complianceHelper != null)
                {
                    complianceHelper.assertEntityEnabled("md_product", "product_id", item.getProductId(), "商品");
                }
                BigDecimal lineAmount = item.getQuantity().multiply(item.getPrice()).setScale(2, RoundingMode.HALF_UP);
                item.setSaleOrderId(wmsSaleOrder.getSaleOrderId());
                item.setAmount(lineAmount);
                wmsSaleOrderItemMapper.insertWmsSaleOrderItem(item);
                sumQty = sumQty.add(item.getQuantity());
                sumAmount = sumAmount.add(lineAmount);
            }
            if (sumQty.compareTo(BigDecimal.ZERO) > 0)
            {
                wmsSaleOrderMapper.refreshTotalBySaleOrderId(wmsSaleOrder.getSaleOrderId());
                wmsSaleOrder.setTotalQty(sumQty);
                wmsSaleOrder.setTotalAmount(sumAmount);
            }
        }
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
        if (wmsSaleOrder.getWarehouseId() != null && !Objects.equals(databaseSaleOrder.getWarehouseId(), wmsSaleOrder.getWarehouseId()))
        {
            List<WmsSaleOrderItem> existingItems = wmsSaleOrderItemMapper.selectWmsSaleOrderItemsByOrderId(wmsSaleOrder.getSaleOrderId());
            if (existingItems != null && !existingItems.isEmpty())
            {
                throw new ServiceException("销售单已添加明细，不允许切换出库仓库，请先清空明细后再改仓");
            }
        }
        if (complianceHelper != null)
        {
            if (wmsSaleOrder.getCustomerId() != null)
            {
                complianceHelper.assertEntityEnabled("md_customer", "customer_id", wmsSaleOrder.getCustomerId(), "客户");
            }
            if (wmsSaleOrder.getWarehouseId() != null)
            {
                complianceHelper.assertEntityEnabled("md_warehouse", "warehouse_id", wmsSaleOrder.getWarehouseId(), "仓库");
            }
        }
        return wmsSaleOrderMapper.updateWmsSaleOrder(wmsSaleOrder);
    }

    /**
     * 提交销售单
     *
     * @param saleOrderId 销售单编号
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
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
        List<WmsSaleOrderItem> itemList = wmsSaleOrderItemMapper.selectWmsSaleOrderItemsByOrderId(saleOrderId);
        if (itemList == null || itemList.isEmpty())
        {
            throw new ServiceException("销售明细不能为空，请先添加商品明细后再提交");
        }
        MdCustomer databaseCustomer = mdCustomerMapper.selectMdCustomerById(databaseSaleOrder.getCustomerId());
        if (databaseCustomer == null)
        {
            throw new ServiceException("销售单客户不存在");
        }
        if (complianceHelper != null)
        {
            complianceHelper.assertEntityEnabled("md_customer", "customer_id", databaseSaleOrder.getCustomerId(), "客户");
            complianceHelper.assertEntityEnabled("md_warehouse", "warehouse_id", databaseSaleOrder.getWarehouseId(), "仓库");
            for (WmsSaleOrderItem item : itemList)
            {
                complianceHelper.assertEntityEnabled("md_product", "product_id", item.getProductId(), "商品");
            }
        }
        validateCustomerCreditLimit(databaseCustomer, databaseSaleOrder, false);
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
     * 校验赊销/月结客户信用额度（包含已欠款 + 待审占用 + 当前单据）
     */
    private void validateCustomerCreditLimit(MdCustomer customer, WmsSaleOrder saleOrder, boolean alreadyInPendingStatus)
    {
        if (customer == null)
        {
            return;
        }
        String paymentMethod = customer.getPaymentMethod();
        if (!"credit".equals(paymentMethod) && !"month".equals(paymentMethod))
        {
            return;
        }
        BigDecimal customerCreditLimit = customer.getCreditLimit() == null ? BigDecimal.ZERO : customer.getCreditLimit();
        if (customerCreditLimit.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("客户未设置可用信用额度，不允许提交或审核赊销/月结销售单");
        }
        BigDecimal customerOutstandingAmount = finReceivableMapper.selectCustomerOutstandingAmount(saleOrder.getCustomerId());
        if (customerOutstandingAmount == null)
        {
            customerOutstandingAmount = BigDecimal.ZERO;
        }
        BigDecimal customerPendingApprovalAmount = wmsSaleOrderMapper.selectCustomerPendingApprovalAmount(saleOrder.getCustomerId());
        if (customerPendingApprovalAmount == null)
        {
            customerPendingApprovalAmount = BigDecimal.ZERO;
        }
        BigDecimal currentSaleOrderAmount = saleOrder.getTotalAmount() == null ? BigDecimal.ZERO : saleOrder.getTotalAmount();
        BigDecimal projectedCreditAmount = alreadyInPendingStatus
            ? customerOutstandingAmount.add(customerPendingApprovalAmount)
            : customerOutstandingAmount.add(customerPendingApprovalAmount).add(currentSaleOrderAmount);
        if (projectedCreditAmount.compareTo(customerCreditLimit) > 0)
        {
            throw new ServiceException(
                "客户信用额度不足，预计占用:" + projectedCreditAmount + "，可用信用额度:" + customerCreditLimit);
        }
    }

    /**
     * 经理审核销售单
     *
     * @param saleOrderId 销售单编号
     * @param auditComment 审核意见
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
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
        if (complianceHelper != null)
        {
            complianceHelper.assertPeriodOpen(new Date(), "经理审核销售单");
            complianceHelper.assertNotSelfAudit(databaseSaleOrder.getCreateBy(), "销售单经理审核");
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
        if (complianceHelper != null)
        {
            complianceHelper.assertPeriodOpen(new Date(), "财务审核销售单");
            complianceHelper.assertNotSelfAudit(databaseSaleOrder.getCreateBy(), "销售单财务审核");
        }
        List<WmsSaleOrderItem> itemList = wmsSaleOrderItemMapper.selectWmsSaleOrderItemsByOrderId(saleOrderId);
        if (itemList == null || itemList.isEmpty())
        {
            throw new ServiceException("销售明细不能为空");
        }
        MdCustomer databaseCustomer = mdCustomerMapper.selectMdCustomerById(databaseSaleOrder.getCustomerId());
        if (databaseCustomer == null)
        {
            throw new ServiceException("销售单客户不存在");
        }
        if (complianceHelper != null)
        {
            complianceHelper.assertEntityEnabled("md_customer", "customer_id", databaseSaleOrder.getCustomerId(), "客户");
            complianceHelper.assertEntityEnabled("md_warehouse", "warehouse_id", databaseSaleOrder.getWarehouseId(), "仓库");
            for (WmsSaleOrderItem item : itemList)
            {
                complianceHelper.assertEntityEnabled("md_product", "product_id", item.getProductId(), "商品");
            }
        }
        validateCustomerCreditLimit(databaseCustomer, databaseSaleOrder, true);
        String auditUsername = SecurityUtils.getUsername();
        // 标准 ERP 锁库机制：销售单财务审核仅原子锁定可用库存 (locked_quantity)，仓库审核出库单时才扣减实物库存
        for (WmsSaleOrderItem wmsSaleOrderItem : itemList)
        {
            wmsStockService.lockStockByKeyAtomically(
                databaseSaleOrder.getWarehouseId(),
                wmsSaleOrderItem.getProductId(),
                wmsSaleOrderItem.getLocationId(),
                wmsSaleOrderItem.getBatchNo(),
                wmsSaleOrderItem.getQuantity(),
                auditUsername
            );
        }
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
            databaseSaleOrder, "销售单财务审核通过-" + databaseSaleOrder.getOrderNo(), "销售单已完成财务审核并锁定库存。", MESSAGE_LEVEL_NORMAL, auditUsername);
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
        if (complianceHelper != null)
        {
            complianceHelper.assertPeriodOpen(databaseSaleOrder.getAuditTime(), "财务回退销售单");
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
        boolean isLegacyDeducted = "legacy_deducted".equals(databaseSaleOrder.getStockDeductMode());
        for (WmsSaleOrderItem wmsSaleOrderItem : itemList)
        {
            if (isLegacyDeducted)
            {
                BigDecimal[] qtyPair = wmsStockService.increaseStockByKeyAtomically(
                    databaseSaleOrder.getWarehouseId(),
                    wmsSaleOrderItem.getProductId(),
                    wmsSaleOrderItem.getLocationId(),
                    wmsSaleOrderItem.getBatchNo(),
                    null,
                    wmsSaleOrderItem.getQuantity(),
                    financeUsername
                );
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
                stockLog.setBeforeQty(qtyPair[0]);
                stockLog.setAfterQty(qtyPair[1]);
                wmsStockLogMapper.insertWmsStockLog(stockLog);
            }
            else
            {
                wmsStockService.releaseLockByKeyAtomically(
                    databaseSaleOrder.getWarehouseId(),
                    wmsSaleOrderItem.getProductId(),
                    wmsSaleOrderItem.getLocationId(),
                    wmsSaleOrderItem.getBatchNo(),
                    wmsSaleOrderItem.getQuantity(),
                    financeUsername
                );
            }
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

    /**
     * 作废销售单
     *
     * @param saleOrderId 销售单编号
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int cancelWmsSaleOrder(Long saleOrderId)
    {
        WmsSaleOrder databaseSaleOrder = wmsSaleOrderMapper.selectWmsSaleOrderById(saleOrderId);
        if (databaseSaleOrder == null)
        {
            throw new ServiceException("销售单不存在");
        }
        if (STATUS_CANCELLED.equals(databaseSaleOrder.getStatus()))
        {
            throw new ServiceException("销售单已作废，无需重复作废");
        }
        if (STATUS_AUDITED.equals(databaseSaleOrder.getStatus()) || STATUS_CLOSED.equals(databaseSaleOrder.getStatus()))
        {
            throw new ServiceException("已审核或已结案销售单不允许作废");
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

    /**
     * 销售单短装强制结案：释放未出库锁定库存并按实发金额重算应收（超收则生成红字退款台账）
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int forceCloseWmsSaleOrder(Long saleOrderId, String reason)
    {
        WmsSaleOrder databaseSaleOrder = wmsSaleOrderMapper.selectWmsSaleOrderById(saleOrderId);
        if (databaseSaleOrder == null)
        {
            throw new ServiceException("销售单不存在");
        }
        if (!STATUS_AUDITED.equals(databaseSaleOrder.getStatus()))
        {
            throw new ServiceException("仅财务已审核状态的销售单允许执行短装结案");
        }
        if (complianceHelper != null)
        {
            complianceHelper.assertPeriodOpen(new Date(), "销售单短装结案");
        }
        Long pendingByOrderId = wmsOutboundMapper.selectPendingLinkedOutboundCountBySaleOrderId(saleOrderId, null);
        Long pendingByOrderNo = wmsOutboundMapper.selectPendingLinkedOutboundCountBySourceOrderNo(databaseSaleOrder.getOrderNo(), null);
        if ((pendingByOrderId != null && pendingByOrderId > 0L) || (pendingByOrderNo != null && pendingByOrderNo > 0L))
        {
            throw new ServiceException("销售单存在未审核完成的关联出库单，请先审核或作废后再执行短装结案");
        }
        BigDecimal totalOutboundQty = databaseSaleOrder.getOutboundQuantity() == null ? BigDecimal.ZERO : databaseSaleOrder.getOutboundQuantity();
        if (totalOutboundQty.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("销售单尚未发生出库，请直接执行财务回退而非短装结案");
        }
        List<WmsSaleOrderItem> itemList = wmsSaleOrderItemMapper.selectWmsSaleOrderItemsByOrderId(saleOrderId);
        if (itemList == null || itemList.isEmpty())
        {
            throw new ServiceException("销售明细不能为空");
        }
        String operatorName = SecurityUtils.getUsername();
        boolean isLegacyDeducted = "legacy_deducted".equals(databaseSaleOrder.getStockDeductMode());
        BigDecimal actualShippedAmount = BigDecimal.ZERO;
        for (WmsSaleOrderItem item : itemList)
        {
            BigDecimal shippedQty = item.getOutboundQuantity() == null ? BigDecimal.ZERO : item.getOutboundQuantity();
            BigDecimal remainQty = item.getRemainQuantity() == null ? BigDecimal.ZERO : item.getRemainQuantity();
            BigDecimal price = item.getPrice() == null ? BigDecimal.ZERO : item.getPrice();
            actualShippedAmount = actualShippedAmount.add(shippedQty.multiply(price).setScale(2, RoundingMode.HALF_UP));
            if (!isLegacyDeducted && remainQty.compareTo(BigDecimal.ZERO) > 0)
            {
                wmsStockService.releaseLockByKeyAtomically(
                    databaseSaleOrder.getWarehouseId(),
                    item.getProductId(),
                    item.getLocationId(),
                    item.getBatchNo(),
                    remainQty,
                    operatorName
                );
            }
        }
        FinReceivable receivable = finReceivableMapper.selectFinReceivableBySaleOrderId(saleOrderId);
        if (receivable != null)
        {
            BigDecimal amountPaid = receivable.getAmountPaid() == null ? BigDecimal.ZERO : receivable.getAmountPaid();
            if (actualShippedAmount.compareTo(amountPaid) >= 0)
            {
                String newPayStatus = actualShippedAmount.compareTo(amountPaid) == 0
                    ? "paid"
                    : (amountPaid.compareTo(BigDecimal.ZERO) > 0 ? "partial" : "unpaid");
                finReceivableMapper.updateFinReceivableAmounts(
                    receivable.getReceivableId(), actualShippedAmount, amountPaid, newPayStatus);
                wmsSaleOrderMapper.updateWmsSaleOrderPaymentStatus(saleOrderId, newPayStatus);
            }
            else
            {
                BigDecimal overpaidAmount = amountPaid.subtract(actualShippedAmount);
                finReceivableMapper.updateFinReceivableAmounts(
                    receivable.getReceivableId(), amountPaid, amountPaid, "paid");
                FinReceivable refundReceivable = new FinReceivable();
                refundReceivable.setSaleOrderId(saleOrderId);
                refundReceivable.setCustomerId(databaseSaleOrder.getCustomerId());
                refundReceivable.setAmountDue(overpaidAmount.negate());
                refundReceivable.setAmountPaid(BigDecimal.ZERO);
                refundReceivable.setStatus("unpaid");
                refundReceivable.setDueDate(new Date());
                refundReceivable.setCreateBy(operatorName);
                refundReceivable.setRemark("销售单短装结案超收退款（" + databaseSaleOrder.getOrderNo() + "）");
                finReceivableMapper.insertFinReceivable(refundReceivable);
                wmsSaleOrderMapper.updateWmsSaleOrderPaymentStatus(saleOrderId, "paid");
            }
        }
        String closeComment = StringUtils.isEmpty(reason) ? "短装强制结案，已释放剩余锁库并重算应收" : reason.trim();
        int updateCount = wmsSaleOrderMapper.updateWmsSaleOrderStatus(
            saleOrderId, STATUS_AUDITED, STATUS_CLOSED, databaseSaleOrder.getAuditBy(), closeComment, operatorName);
        if (updateCount == 0)
        {
            throw new ServiceException("销售单状态已变更，请刷新后重试");
        }
        saveStatusHistory(saleOrderId, STATUS_AUDITED, STATUS_CLOSED, "force_close", "finance", closeComment, operatorName);
        pushSaleOrderAuditMessage(
            databaseSaleOrder, "销售单短装结案-" + databaseSaleOrder.getOrderNo(), closeComment, MESSAGE_LEVEL_NORMAL, operatorName);
        return updateCount;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deleteWmsSaleOrderById(Long saleOrderId)
    {
        WmsSaleOrder databaseSaleOrder = wmsSaleOrderMapper.selectWmsSaleOrderById(saleOrderId);
        if (databaseSaleOrder == null)
        {
            return 0;
        }
        validateSaleOrderCanBeDeleted(databaseSaleOrder, false);
        wmsSaleOrderItemMapper.deleteWmsSaleOrderItemBySaleOrderIds(new Long[] { saleOrderId });
        return wmsSaleOrderMapper.deleteWmsSaleOrderById(saleOrderId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deleteWmsSaleOrderByIds(Long[] saleOrderIds)
    {
        for (Long saleOrderId : saleOrderIds)
        {
            WmsSaleOrder databaseSaleOrder = wmsSaleOrderMapper.selectWmsSaleOrderById(saleOrderId);
            if (databaseSaleOrder == null)
            {
                continue;
            }
            validateSaleOrderCanBeDeleted(databaseSaleOrder, true);
        }
        wmsSaleOrderItemMapper.deleteWmsSaleOrderItemBySaleOrderIds(saleOrderIds);
        return wmsSaleOrderMapper.deleteWmsSaleOrderByIds(saleOrderIds);
    }

    /**
     * 校验销售单是否允许删除
     *
     * @param databaseSaleOrder 数据库中的销售单
     * @param batchDelete 是否批量删除
     */
    private void validateSaleOrderCanBeDeleted(WmsSaleOrder databaseSaleOrder, boolean batchDelete)
    {
        if (!STATUS_DRAFT.equals(databaseSaleOrder.getStatus()))
        {
            if (batchDelete)
            {
                throw new ServiceException("仅草稿状态销售单允许删除，销售单号：" + databaseSaleOrder.getOrderNo());
            }
            throw new ServiceException("仅草稿状态销售单允许删除");
        }
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
        Map<String, WmsSaleOrder> importExistingSaleOrderMap = buildImportExistingSaleOrderMap(saleOrderList);
        int successCount = 0;
        int failureCount = 0;
        StringBuilder successMessageBuilder = new StringBuilder();
        StringBuilder failureMessageBuilder = new StringBuilder();
        for (WmsSaleOrder saleOrder : saleOrderList)
        {
            try
            {
                validateImportData(saleOrder);
                WmsSaleOrder databaseSaleOrder = importExistingSaleOrderMap.get(saleOrder.getOrderNo());
                if (databaseSaleOrder == null)
                {
                    saleOrder.setCreateBy(operatorName);
                    insertWmsSaleOrder(saleOrder);
                    importExistingSaleOrderMap.put(saleOrder.getOrderNo(), saleOrder);
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
                importExistingSaleOrderMap.put(saleOrder.getOrderNo(), databaseSaleOrder);
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
     * 批量构建导入销售订单已存在数据映射
     *
     * @param saleOrderList 导入销售订单集合
     * @return 销售单号与销售订单映射
     */
    private Map<String, WmsSaleOrder> buildImportExistingSaleOrderMap(List<WmsSaleOrder> saleOrderList)
    {
        Map<String, WmsSaleOrder> existingSaleOrderMap = new HashMap<>();
        List<String> orderNoList = new ArrayList<>();
        for (WmsSaleOrder saleOrder : saleOrderList)
        {
            if (saleOrder != null && StringUtils.isNotEmpty(saleOrder.getOrderNo()))
            {
                orderNoList.add(saleOrder.getOrderNo());
            }
        }
        if (orderNoList.isEmpty())
        {
            return existingSaleOrderMap;
        }
        List<WmsSaleOrder> existingSaleOrderList = wmsSaleOrderMapper.selectWmsSaleOrderImportListByOrderNos(orderNoList);
        if (existingSaleOrderList == null || existingSaleOrderList.isEmpty())
        {
            return existingSaleOrderMap;
        }
        for (WmsSaleOrder existingSaleOrder : existingSaleOrderList)
        {
            existingSaleOrderMap.put(existingSaleOrder.getOrderNo(), existingSaleOrder);
        }
        return existingSaleOrderMap;
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
     * 查询销售订单全链路追踪
     *
     * @param saleOrderId 销售订单主键
     * @return 销售订单链路数据
     */
    @Override
    public Map<String, Object> selectWmsSaleOrderChain(Long saleOrderId)
    {
        WmsSaleOrder saleOrder = wmsSaleOrderMapper.selectWmsSaleOrderById(saleOrderId);
        if (saleOrder == null)
        {
            throw new ServiceException("销售单不存在");
        }
        Map<String, Object> saleOrderChainMap = new HashMap<>();
        saleOrderChainMap.put("saleOrder", saleOrder);
        saleOrderChainMap.put("statusHistoryList", wmsSaleOrderStatusHistoryMapper.selectWmsSaleOrderStatusHistoryList(saleOrderId));
        saleOrderChainMap.put("outboundList", selectSaleOrderChainOutboundList(saleOrder));
        saleOrderChainMap.put("receivableList", selectSaleOrderChainReceivableList(saleOrderId));
        saleOrderChainMap.put("receiptList", selectSaleOrderChainReceiptList(saleOrderId));
        saleOrderChainMap.put("saleReturnList", selectSaleOrderChainSaleReturnList(saleOrder));
        saleOrderChainMap.put("invoiceList", selectSaleOrderChainInvoiceList(saleOrderId));
        saleOrderChainMap.put("chainNodeList", buildSaleOrderChainNodeList(saleOrder, saleOrderChainMap));
        return saleOrderChainMap;
    }

    /**
     * 查询销售订单关联出库单并兼容历史来源单号
     *
     * @param saleOrder 销售订单
     * @return 出库单集合
     */
    private List<WmsOutbound> selectSaleOrderChainOutboundList(WmsSaleOrder saleOrder)
    {
        Map<Long, WmsOutbound> outboundMap = new HashMap<>();
        WmsOutbound outboundQuery = new WmsOutbound();
        outboundQuery.setSaleOrderId(saleOrder.getSaleOrderId());
        appendSaleOrderChainOutboundList(outboundMap, wmsOutboundMapper.selectWmsOutboundList(outboundQuery));
        if (StringUtils.isNotEmpty(saleOrder.getOrderNo()))
        {
            WmsOutbound sourceOrderNoOutboundQuery = new WmsOutbound();
            sourceOrderNoOutboundQuery.setSourceOrderNo(saleOrder.getOrderNo());
            appendSaleOrderChainOutboundList(outboundMap, wmsOutboundMapper.selectWmsOutboundList(sourceOrderNoOutboundQuery));
        }
        return new ArrayList<>(outboundMap.values());
    }

    /**
     * 合并销售订单链路中的出库单，避免双字段命中时重复展示
     *
     * @param outboundMap 出库单映射
     * @param outboundList 出库单集合
     */
    private void appendSaleOrderChainOutboundList(Map<Long, WmsOutbound> outboundMap, List<WmsOutbound> outboundList)
    {
        if (outboundList == null || outboundList.isEmpty())
        {
            return;
        }
        for (WmsOutbound outbound : outboundList)
        {
            if (outbound != null && outbound.getOutboundId() != null)
            {
                outboundMap.put(outbound.getOutboundId(), outbound);
            }
        }
    }

    /**
     * 查询销售订单关联应收台账集合
     *
     * @param saleOrderId 销售订单主键
     * @return 应收台账集合
     */
    private List<FinReceivable> selectSaleOrderChainReceivableList(Long saleOrderId)
    {
        FinReceivable receivableQuery = new FinReceivable();
        receivableQuery.setSaleOrderId(saleOrderId);
        return finReceivableMapper.selectFinReceivableList(receivableQuery);
    }

    /**
     * 查询销售订单关联回款流水集合
     *
     * @param saleOrderId 销售订单主键
     * @return 回款流水集合
     */
    private List<FinReceipt> selectSaleOrderChainReceiptList(Long saleOrderId)
    {
        FinReceipt receiptQuery = new FinReceipt();
        receiptQuery.setSaleOrderId(saleOrderId);
        return finReceiptService.selectFinReceiptList(receiptQuery);
    }

    /**
     * 查询销售订单关联销售退货单集合
     *
     * @param saleOrder 销售订单
     * @return 销售退货集合
     */
    private List<WmsSaleReturn> selectSaleOrderChainSaleReturnList(WmsSaleOrder saleOrder)
    {
        if (saleOrder == null || saleOrder.getSaleOrderId() == null)
        {
            return new ArrayList<>();
        }
        WmsSaleReturn saleReturnQuery = new WmsSaleReturn();
        saleReturnQuery.setSaleOrderId(saleOrder.getSaleOrderId());
        return wmsSaleReturnMapper.selectWmsSaleReturnList(saleReturnQuery);
    }

    private List<FinInvoice> selectSaleOrderChainInvoiceList(Long saleOrderId)
    {
        if (finInvoiceMapper == null || saleOrderId == null)
        {
            return new ArrayList<>();
        }
        FinInvoice query = new FinInvoice();
        query.setInvoiceDirection("sale");
        query.setSourceOrderId(saleOrderId);
        List<FinInvoice> list = finInvoiceMapper.selectFinInvoiceList(query);
        return list == null ? new ArrayList<>() : list;
    }

    /**
     * 构建销售订单链路节点
     *
     * @param saleOrder 销售订单
     * @param saleOrderChainMap 销售订单链路数据
     * @return 链路节点集合
     */
    private List<Map<String, Object>> buildSaleOrderChainNodeList(WmsSaleOrder saleOrder, Map<String, Object> saleOrderChainMap)
    {
        List<Map<String, Object>> chainNodeList = new ArrayList<>();
        chainNodeList.add(buildChainNode("saleOrder", "销售订单", "已创建", saleOrder.getOrderNo()));
        chainNodeList.add(buildChainNode("audit", "审批流转", buildSaleOrderAuditSummary(saleOrder), buildSaleOrderAuditSummary(saleOrder)));
        chainNodeList.add(buildChainNode("outbound", "销售出库", buildChainNodeStatus(saleOrderChainMap.get("outboundList")), null));
        chainNodeList.add(buildChainNode("receivable", "应收台账", buildChainNodeStatus(saleOrderChainMap.get("receivableList")), null));
        chainNodeList.add(buildChainNode("receipt", "回款流水", buildChainNodeStatus(saleOrderChainMap.get("receiptList")), null));
        chainNodeList.add(buildChainNode("saleReturn", "销售退货", buildChainNodeStatus(saleOrderChainMap.get("saleReturnList")), null));
        chainNodeList.add(buildChainNode("invoice", "销项发票", buildChainNodeStatus(saleOrderChainMap.get("invoiceList")), null));
        return chainNodeList;
    }

    /**
     * 构建销售订单审核摘要
     *
     * @param saleOrder 销售订单
     * @return 审核摘要
     */
    private String buildSaleOrderAuditSummary(WmsSaleOrder saleOrder)
    {
        if (STATUS_AUDITED.equals(saleOrder.getStatus()))
        {
            return "财务已审核";
        }
        if (STATUS_CLOSED.equals(saleOrder.getStatus()))
        {
            return "已短装结案";
        }
        if (STATUS_MANAGER_APPROVED.equals(saleOrder.getStatus()))
        {
            return "经理已审核";
        }
        if (STATUS_SUBMITTED.equals(saleOrder.getStatus()))
        {
            return "待经理审核";
        }
        if (STATUS_DRAFT.equals(saleOrder.getStatus()))
        {
            return "草稿待提交";
        }
        if (STATUS_CANCELLED.equals(saleOrder.getStatus()))
        {
            return "已作废";
        }
        return saleOrder.getStatus();
    }

    /**
     * 构建通用链路节点
     *
     * @param nodeKey 节点标识
     * @param nodeName 节点名称
     * @param nodeStatus 节点状态
     * @param nodeSummary 节点摘要
     * @return 链路节点
     */
    private Map<String, Object> buildChainNode(String nodeKey, String nodeName, String nodeStatus, String nodeSummary)
    {
        Map<String, Object> chainNodeMap = new HashMap<>();
        chainNodeMap.put("nodeKey", nodeKey);
        chainNodeMap.put("nodeName", nodeName);
        chainNodeMap.put("nodeStatus", nodeStatus);
        chainNodeMap.put("nodeSummary", nodeSummary);
        return chainNodeMap;
    }

    /**
     * 根据链路数据集合构建节点状态
     *
     * @param chainData 链路数据
     * @return 节点状态
     */
    private String buildChainNodeStatus(Object chainData)
    {
        if (chainData instanceof List && !((List<?>) chainData).isEmpty())
        {
            return "已生成";
        }
        return "未生成";
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
        Long linkedOutboundCount =
            wmsOutboundMapper.selectActiveOutboundCountBySaleOrderId(databaseSaleOrder.getSaleOrderId());
        if (linkedOutboundCount != null && linkedOutboundCount.longValue() > 0L)
        {
            throw new ServiceException("销售单已生成销售出库单，请先处理相关出库单后再执行财务回退");
        }
        Long historicalOutboundCount =
            wmsOutboundMapper.selectActiveOutboundCountBySourceOrderNo(databaseSaleOrder.getOrderNo());
        if (historicalOutboundCount != null && historicalOutboundCount.longValue() > 0L)
        {
            throw new ServiceException("销售单已生成销售出库单，请先处理相关出库单后再执行财务回退");
        }
    }

    @Override
    public Map<String, Object> suggestCustomerProductPrice(Long customerId, Long productId)
    {
        Map<String, Object> result = new LinkedHashMap<>();
        if (productId == null || mdProductMapper == null)
        {
            return result;
        }
        MdProduct product = mdProductMapper.selectMdProductById(productId);
        if (product == null)
        {
            return result;
        }
        BigDecimal standardSalePrice = product.getSalePrice() == null ? BigDecimal.ZERO : product.getSalePrice();
        BigDecimal costPrice = product.getCostPrice() == null ? BigDecimal.ZERO : product.getCostPrice();
        BigDecimal suggestedPrice = standardSalePrice;
        String priceSource = "标准销售价";

        if (customerId != null && jdbcTemplate != null)
        {
            try
            {
                // 1. 优先匹配客户专属协议价
                List<BigDecimal> agreedPrices = jdbcTemplate.queryForList(
                    "SELECT agreed_price FROM md_customer_price WHERE customer_id = ? AND product_id = ? AND status = '0' LIMIT 1",
                    BigDecimal.class, customerId, productId
                );
                if (agreedPrices != null && !agreedPrices.isEmpty() && agreedPrices.get(0) != null)
                {
                    suggestedPrice = agreedPrices.get(0);
                    priceSource = "客户专属协议价";
                }
                else
                {
                    // 2. 匹配最近一次已审核历史成交价
                    List<BigDecimal> historyPrices = jdbcTemplate.queryForList(
                        "SELECT soi.price FROM wms_sale_order_item soi " +
                        "JOIN wms_sale_order so ON so.sale_order_id = soi.sale_order_id " +
                        "WHERE so.customer_id = ? AND soi.product_id = ? AND so.status = 'audited' " +
                        "ORDER BY so.sale_order_id DESC LIMIT 1",
                        BigDecimal.class, customerId, productId
                    );
                    if (historyPrices != null && !historyPrices.isEmpty() && historyPrices.get(0) != null)
                    {
                        suggestedPrice = historyPrices.get(0);
                        priceSource = "历史最近成交价";
                    }
                    else
                    {
                        // 3. 按客户等级给予默认折扣
                        MdCustomer customer = mdCustomerMapper.selectMdCustomerById(customerId);
                        if (customer != null && StringUtils.isNotEmpty(customer.getCustomerLevel()))
                        {
                            String level = customer.getCustomerLevel().trim().toLowerCase();
                            if ("a".equals(level) || "vip".equals(level) || "战略客户".equals(customer.getCustomerLevel()))
                            {
                                suggestedPrice = standardSalePrice.multiply(new BigDecimal("0.95")).setScale(2, RoundingMode.HALF_UP);
                                priceSource = "VIP等级折扣价(95折)";
                            }
                            else if ("b".equals(level) || "重点客户".equals(customer.getCustomerLevel()))
                            {
                                suggestedPrice = standardSalePrice.multiply(new BigDecimal("0.98")).setScale(2, RoundingMode.HALF_UP);
                                priceSource = "重点客户折扣价(98折)";
                            }
                        }
                    }
                }
            }
            catch (Exception ignored)
            {
                // 回退标准售价
            }
        }
        result.put("suggestedPrice", suggestedPrice);
        result.put("priceSource", priceSource);
        result.put("standardSalePrice", standardSalePrice);
        result.put("costPrice", costPrice);
        return result;
    }
}
