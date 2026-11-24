package com.ruoyi.business.service.impl;

import java.util.List;
import java.util.Map;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.jdbc.core.JdbcTemplate;
import com.ruoyi.business.domain.FinInvoice;
import com.ruoyi.business.domain.FinPayable;
import com.ruoyi.business.domain.FinPayment;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import com.ruoyi.business.domain.WmsInbound;
import com.ruoyi.business.domain.WmsPurchaseOrderItem;
import com.ruoyi.business.domain.WmsPurchaseReturn;
import com.ruoyi.business.mapper.FinInvoiceMapper;
import com.ruoyi.business.mapper.FinPayableMapper;
import com.ruoyi.business.mapper.FinPaymentMapper;
import com.ruoyi.business.mapper.WmsInboundMapper;
import com.ruoyi.business.mapper.WmsPurchaseOrderMapper;
import com.ruoyi.business.mapper.WmsPurchaseReturnMapper;
import com.ruoyi.business.domain.WmsPurchaseOrder;
import com.ruoyi.business.service.IWmsPurchaseOrderService;
import com.ruoyi.business.service.support.BizComplianceHelper;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;

@Service
public class WmsPurchaseOrderServiceImpl implements IWmsPurchaseOrderService 
{
    private static final String STATUS_DRAFT = "draft";

    private static final String STATUS_PENDING_AUDIT = "0";

    private static final String STATUS_AUDITED = "1";

    private static final String STATUS_PARTIALLY_INBOUND = "2";

    private static final String STATUS_COMPLETED = "3";

    private static final String STATUS_CANCELLED = "4";

    @Autowired
    private WmsPurchaseOrderMapper wmsPurchaseOrderMapper;

    @Autowired
    private FinPayableMapper finPayableMapper;

    @Autowired
    private FinPaymentMapper finPaymentMapper;

    @Autowired
    private WmsInboundMapper wmsInboundMapper;

    @Autowired(required = false)
    private BizComplianceHelper complianceHelper;

    @Autowired(required = false)
    private WmsPurchaseReturnMapper wmsPurchaseReturnMapper;

    @Autowired(required = false)
    private FinInvoiceMapper finInvoiceMapper;

    @Autowired(required = false)
    private FinInvoiceService finInvoiceService;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    /**
     * 查询采购订单
     * 
     * @param purchaseOrderId 采购订单主键
     * @return 采购订单
     */
    @Override
    public WmsPurchaseOrder selectWmsPurchaseOrderByPurchaseOrderId(Long purchaseOrderId)
    {
        return wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(purchaseOrderId);
    }

    /**
     * 查询采购订单全链路追踪
     *
     * @param purchaseOrderId 采购订单主键
     * @return 采购订单链路数据
     */
    @Override
    public Map<String, Object> selectWmsPurchaseOrderChain(Long purchaseOrderId)
    {
        WmsPurchaseOrder purchaseOrder = wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(purchaseOrderId);
        if (purchaseOrder == null)
        {
            throw new ServiceException("采购订单不存在");
        }
        Map<String, Object> purchaseOrderChainMap = new HashMap<>();
        purchaseOrderChainMap.put("purchaseOrder", purchaseOrder);
        purchaseOrderChainMap.put("inboundList", selectPurchaseOrderChainInboundList(purchaseOrderId));
        purchaseOrderChainMap.put("payableList", selectPurchaseOrderChainPayableList(purchaseOrderId));
        purchaseOrderChainMap.put("paymentList", selectPurchaseOrderChainPaymentList(purchaseOrderId));
        purchaseOrderChainMap.put("purchaseReturnList", selectPurchaseOrderChainReturnList(purchaseOrderId));
        purchaseOrderChainMap.put("invoiceList", selectPurchaseOrderChainInvoiceList(purchaseOrderId));
        purchaseOrderChainMap.put("chainNodeList", buildPurchaseOrderChainNodeList(purchaseOrder, purchaseOrderChainMap));
        return purchaseOrderChainMap;
    }

    /**
     * 查询采购订单列表
     * 
     * @param wmsPurchaseOrder 采购订单
     * @return 采购订单
     */
    @Override
    @DataScope(deptAlias = "su", userAlias = "su", permission = "business:purchaseOrder:list")
    public List<WmsPurchaseOrder> selectWmsPurchaseOrderList(WmsPurchaseOrder wmsPurchaseOrder)
    {
        return wmsPurchaseOrderMapper.selectWmsPurchaseOrderList(wmsPurchaseOrder);
    }

    @Transactional
    @Override
    public int insertWmsPurchaseOrder(WmsPurchaseOrder wmsPurchaseOrder)
    {
        validatePurchaseOrderBeforeSave(wmsPurchaseOrder);
        if (StringUtils.isEmpty(wmsPurchaseOrder.getOrderNo()))
        {
            wmsPurchaseOrder.setOrderNo("PO" + DateUtils.dateTimeNow("yyyyMMddHHmmss"));
        }
        wmsPurchaseOrder.setStatus(STATUS_DRAFT);
        wmsPurchaseOrder.setCreateTime(DateUtils.getNowDate());
        try
        {
            wmsPurchaseOrder.setCreateBy(SecurityUtils.getUsername());
        }
        catch (Exception exception)
        {
            // ignore
        }
        int rows = wmsPurchaseOrderMapper.insertWmsPurchaseOrder(wmsPurchaseOrder);
        insertWmsPurchaseOrderItem(wmsPurchaseOrder);
        return rows;
    }

    @Transactional
    @Override
    public int updateWmsPurchaseOrder(WmsPurchaseOrder wmsPurchaseOrder)
    {
        WmsPurchaseOrder databasePurchaseOrder = wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(wmsPurchaseOrder.getPurchaseOrderId());
        if (databasePurchaseOrder == null)
        {
            throw new ServiceException("采购订单不存在");
        }
        if (!STATUS_DRAFT.equals(databasePurchaseOrder.getStatus()))
        {
            throw new ServiceException("仅草稿状态采购订单允许修改，待审核单据请先驳回");
        }
        validatePurchaseOrderBeforeSave(wmsPurchaseOrder);
        wmsPurchaseOrder.setStatus(databasePurchaseOrder.getStatus());
        wmsPurchaseOrder.setUpdateTime(DateUtils.getNowDate());
        try
        {
            wmsPurchaseOrder.setUpdateBy(SecurityUtils.getUsername());
        }
        catch (Exception exception)
        {
            // ignore
        }
        wmsPurchaseOrderMapper.deleteWmsPurchaseOrderItemByPurchaseOrderId(wmsPurchaseOrder.getPurchaseOrderId());
        insertWmsPurchaseOrderItem(wmsPurchaseOrder);
        return wmsPurchaseOrderMapper.updateWmsPurchaseOrder(wmsPurchaseOrder);
    }

    @Override
    public int submitWmsPurchaseOrder(Long purchaseOrderId)
    {
        WmsPurchaseOrder databasePurchaseOrder = wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(purchaseOrderId);
        if (databasePurchaseOrder == null)
        {
            throw new ServiceException("采购订单不存在");
        }
        if (!STATUS_DRAFT.equals(databasePurchaseOrder.getStatus()))
        {
            throw new ServiceException("仅草稿状态采购订单允许提交");
        }
        validatePurchaseOrderBeforeSave(databasePurchaseOrder);
        int rows = wmsPurchaseOrderMapper.updateWmsPurchaseOrderStatus(
            purchaseOrderId, STATUS_DRAFT, STATUS_PENDING_AUDIT, SecurityUtils.getUsername()
        );
        if (rows == 0)
        {
            throw new ServiceException("采购订单状态已变更，请刷新后重试");
        }
        return rows;
    }

    @Override
    @Transactional
    public int auditWmsPurchaseOrder(Long purchaseOrderId)
    {
        WmsPurchaseOrder databasePurchaseOrder = wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(purchaseOrderId);
        if (databasePurchaseOrder == null)
        {
            throw new ServiceException("采购订单不存在");
        }
        if (!STATUS_PENDING_AUDIT.equals(databasePurchaseOrder.getStatus()))
        {
            throw new ServiceException("仅待审核状态采购订单允许审核");
        }
        if (complianceHelper != null)
        {
            complianceHelper.assertPeriodOpen(databasePurchaseOrder.getPurchaseDate(), "审核采购订单");
            complianceHelper.assertPeriodOpen(new java.util.Date(), "审核采购订单");
            complianceHelper.assertNotSelfAudit(databasePurchaseOrder.getCreateBy(), "采购订单审核");
            complianceHelper.assertEntityEnabled("md_supplier", "supplier_id", databasePurchaseOrder.getSupplierId(), "供应商");
        }
        if (databasePurchaseOrder.getWmsPurchaseOrderItemList() == null || databasePurchaseOrder.getWmsPurchaseOrderItemList().isEmpty())
        {
            throw new ServiceException("采购明细不能为空");
        }
        if (complianceHelper != null)
        {
            for (WmsPurchaseOrderItem item : databasePurchaseOrder.getWmsPurchaseOrderItemList())
            {
                complianceHelper.assertEntityEnabled("md_product", "product_id", item.getProductId(), "商品");
            }
        }
        String updateUsername = SecurityUtils.getUsername();
        int updateCount = wmsPurchaseOrderMapper.updateWmsPurchaseOrderStatus(
            purchaseOrderId, STATUS_PENDING_AUDIT, STATUS_AUDITED, updateUsername);
        if (updateCount == 0)
        {
            throw new ServiceException("采购订单状态已变更，请刷新后重试");
        }
        ensurePayableCreated(databasePurchaseOrder, updateUsername);
        return updateCount;
    }

    @Override
    public int rejectWmsPurchaseOrder(Long purchaseOrderId, String rejectReason)
    {
        WmsPurchaseOrder databasePurchaseOrder = wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(purchaseOrderId);
        if (databasePurchaseOrder == null)
        {
            throw new ServiceException("采购订单不存在");
        }
        if (!STATUS_PENDING_AUDIT.equals(databasePurchaseOrder.getStatus()))
        {
            throw new ServiceException("仅待审核状态采购订单允许驳回");
        }
        String reason = StringUtils.isEmpty(rejectReason) ? "审核驳回重改" : rejectReason.trim();
        int rows = wmsPurchaseOrderMapper.rejectWmsPurchaseOrderStatus(purchaseOrderId, reason, SecurityUtils.getUsername());
        if (rows == 0)
        {
            throw new ServiceException("采购订单状态已变更，请刷新后重试");
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int reverseAuditWmsPurchaseOrder(Long purchaseOrderId)
    {
        WmsPurchaseOrder po = wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(purchaseOrderId);
        if (po == null)
        {
            throw new ServiceException("采购订单不存在");
        }
        if (!STATUS_AUDITED.equals(po.getStatus()))
        {
            throw new ServiceException("仅已审核且尚未入库的采购订单允许反审核");
        }
        if (complianceHelper != null)
        {
            complianceHelper.assertPeriodOpen(po.getPurchaseDate(), "反审核采购订单");
            complianceHelper.assertPeriodOpen(new java.util.Date(), "反审核采购订单");
        }
        assertNoActiveInvoiceOrReturnForPurchaseOrder(purchaseOrderId, "反审核");
        WmsInbound inboundQuery = new WmsInbound();
        inboundQuery.setPurchaseOrderId(purchaseOrderId);
        List<WmsInbound> inbounds = wmsInboundMapper.selectWmsInboundList(inboundQuery);
        if (inbounds != null)
        {
            for (WmsInbound in : inbounds)
            {
                if (!"cancelled".equals(in.getStatus()))
                {
                    throw new ServiceException("该采购订单已关联入库单（" + in.getInboundNo() + "），禁止反审核");
                }
            }
        }
        String operator = SecurityUtils.getUsername();
        clearUnpaidPayablesSafely(purchaseOrderId, operator, "采购订单反审核归零");
        int rows = wmsPurchaseOrderMapper.updateWmsPurchaseOrderStatus(
            purchaseOrderId, STATUS_AUDITED, STATUS_DRAFT, operator);
        if (rows == 0)
        {
            throw new ServiceException("采购订单状态已变更，请刷新后重试");
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int forceCloseWmsPurchaseOrder(Long purchaseOrderId)
    {
        WmsPurchaseOrder po = wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(purchaseOrderId);
        if (po == null)
        {
            throw new ServiceException("采购订单不存在");
        }
        if (!STATUS_PARTIALLY_INBOUND.equals(po.getStatus()))
        {
            throw new ServiceException("仅部分入库状态的采购订单支持短装结案");
        }
        if (complianceHelper != null)
        {
            complianceHelper.assertPeriodOpen(po.getPurchaseDate(), "短装结案采购订单");
            complianceHelper.assertPeriodOpen(new java.util.Date(), "短装结案采购订单");
        }
        WmsInbound inboundQuery = new WmsInbound();
        inboundQuery.setPurchaseOrderId(purchaseOrderId);
        List<WmsInbound> inbounds = wmsInboundMapper.selectWmsInboundList(inboundQuery);
        BigDecimal auditedInboundAmount = BigDecimal.ZERO;
        if (inbounds != null)
        {
            for (WmsInbound in : inbounds)
            {
                if ("draft".equals(in.getStatus()) || "submitted".equals(in.getStatus()))
                {
                    throw new ServiceException("存在未审核入库单（" + in.getInboundNo() + "），请先审核或作废后再短装结案");
                }
                if ("audited".equals(in.getStatus()) && in.getTotalAmount() != null)
                {
                    auditedInboundAmount = auditedInboundAmount.add(in.getTotalAmount());
                }
            }
        }
        if (jdbcTemplate != null)
        {
            BigDecimal activeInvoiceTotal = jdbcTemplate.queryForObject(
                "SELECT IFNULL(SUM(total_amount), 0) FROM fin_invoice WHERE invoice_direction = 'purchase' AND source_order_id = ? AND status IN ('draft', 'issued')",
                BigDecimal.class, purchaseOrderId
            );
            if (activeInvoiceTotal != null && activeInvoiceTotal.compareTo(auditedInboundAmount) > 0)
            {
                throw new ServiceException("该采购单已登记进项发票金额（¥" + activeInvoiceTotal + "）大于实际入库金额（¥" + auditedInboundAmount + "），请先红冲超额发票后再短装结案");
            }
        }
        String operator = SecurityUtils.getUsername();
        int rows = wmsPurchaseOrderMapper.updateWmsPurchaseOrderStatus(
            purchaseOrderId, STATUS_PARTIALLY_INBOUND, STATUS_COMPLETED, operator);
        if (rows == 0)
        {
            throw new ServiceException("采购订单状态已变更，请刷新后重试");
        }
        // 按实收入库金额核减正向应付台账，若已多付则生成红字待供应商退款应付
        FinPayable payableQuery = new FinPayable();
        payableQuery.setPurchaseOrderId(purchaseOrderId);
        List<FinPayable> payables = finPayableMapper.selectFinPayableList(payableQuery);
        if (payables != null)
        {
            for (FinPayable p : payables)
            {
                BigDecimal due = p.getAmountDue() == null ? BigDecimal.ZERO : p.getAmountDue();
                if (due.compareTo(BigDecimal.ZERO) <= 0)
                {
                    continue;
                }
                BigDecimal paid = p.getAmountPaid() == null ? BigDecimal.ZERO : p.getAmountPaid();
                BigDecimal targetDue = auditedInboundAmount.min(due);
                if (targetDue.compareTo(paid) >= 0)
                {
                    p.setAmountDue(targetDue);
                    BigDecimal remain = targetDue.subtract(paid);
                    p.setRemainAmount(remain);
                    p.setStatus(remain.compareTo(BigDecimal.ZERO) <= 0 ? "2" : (paid.compareTo(BigDecimal.ZERO) > 0 ? "1" : "0"));
                    p.setRemark((StringUtils.isEmpty(p.getRemark()) ? "" : p.getRemark() + "；") + "采购单短装结案按实收调减应付至¥" + targetDue);
                    p.setUpdateBy(operator);
                    finPayableMapper.updateFinPayable(p);
                }
                else
                {
                    BigDecimal overpaid = paid.subtract(targetDue);
                    p.setAmountDue(paid);
                    p.setRemainAmount(BigDecimal.ZERO);
                    p.setStatus("2");
                    p.setRemark((StringUtils.isEmpty(p.getRemark()) ? "" : p.getRemark() + "；") + "采购单短装结案，多付¥" + overpaid + "转红字待退款");
                    p.setUpdateBy(operator);
                    finPayableMapper.updateFinPayable(p);

                    FinPayable refundPayable = new FinPayable();
                    refundPayable.setPurchaseOrderId(purchaseOrderId);
                    refundPayable.setSupplierId(po.getSupplierId());
                    refundPayable.setAmountDue(overpaid.negate());
                    refundPayable.setAmountPaid(BigDecimal.ZERO);
                    refundPayable.setRemainAmount(overpaid.negate());
                    refundPayable.setStatus("0");
                    refundPayable.setRemark("采购单[" + po.getOrderNo() + "]短装结案多付款待供应商退款");
                    refundPayable.setCreateBy(operator);
                    refundPayable.setCreateTime(DateUtils.getNowDate());
                    finPayableMapper.insertFinPayable(refundPayable);
                }
                break;
            }
        }
        if (finInvoiceService != null)
        {
            finInvoiceService.syncOrderInvoiceStatus("purchase", purchaseOrderId);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelWmsPurchaseOrder(Long purchaseOrderId)
    {
        WmsPurchaseOrder databasePurchaseOrder = wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(purchaseOrderId);
        if (databasePurchaseOrder == null)
        {
            throw new ServiceException("采购订单不存在");
        }
        if (STATUS_CANCELLED.equals(databasePurchaseOrder.getStatus()))
        {
            throw new ServiceException("采购订单已作废，无需重复作废");
        }
        if (STATUS_PARTIALLY_INBOUND.equals(databasePurchaseOrder.getStatus()))
        {
            throw new ServiceException("部分入库采购单不允许直接作废，请使用「短装结案」关闭剩余待入库量");
        }
        if (!STATUS_DRAFT.equals(databasePurchaseOrder.getStatus())
            && !STATUS_PENDING_AUDIT.equals(databasePurchaseOrder.getStatus())
            && !STATUS_AUDITED.equals(databasePurchaseOrder.getStatus()))
        {
            throw new ServiceException("仅草稿、待审核或已审核未入库状态的采购订单允许作废");
        }
        if (complianceHelper != null)
        {
            complianceHelper.assertPeriodOpen(databasePurchaseOrder.getPurchaseDate(), "作废采购订单");
            complianceHelper.assertPeriodOpen(new java.util.Date(), "作废采购订单");
        }
        String updateUsername = SecurityUtils.getUsername();
        if (STATUS_AUDITED.equals(databasePurchaseOrder.getStatus()))
        {
            assertNoActiveInvoiceOrReturnForPurchaseOrder(purchaseOrderId, "作废");
            WmsInbound inboundQuery = new WmsInbound();
            inboundQuery.setPurchaseOrderId(purchaseOrderId);
            List<WmsInbound> inbounds = wmsInboundMapper.selectWmsInboundList(inboundQuery);
            if (inbounds != null)
            {
                for (WmsInbound in : inbounds)
                {
                    if (!"cancelled".equals(in.getStatus()))
                    {
                        throw new ServiceException("该采购单已有关联入库单（" + in.getInboundNo() + "），无法直接作废");
                    }
                }
            }
            clearUnpaidPayablesSafely(purchaseOrderId, updateUsername, "采购订单作废归零");
        }
        int updateCount = wmsPurchaseOrderMapper.updateWmsPurchaseOrderStatus(
            purchaseOrderId, databasePurchaseOrder.getStatus(), STATUS_CANCELLED, updateUsername);
        if (updateCount == 0)
        {
            throw new ServiceException("采购订单状态已变更，请刷新后重试");
        }
        return updateCount;
    }

    private void assertNoActiveInvoiceOrReturnForPurchaseOrder(Long purchaseOrderId, String actionLabel)
    {
        if (jdbcTemplate == null || purchaseOrderId == null)
        {
            return;
        }
        Long activeInvoiceCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM fin_invoice WHERE invoice_direction = 'purchase' AND source_order_id = ? AND status IN ('draft', 'issued')",
            Long.class, purchaseOrderId
        );
        if (activeInvoiceCount != null && activeInvoiceCount > 0L)
        {
            throw new ServiceException("该采购订单已关联有效进项发票，请先红冲作废或删除发票草稿后再" + actionLabel);
        }
        Long activeReturnCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM wms_purchase_return WHERE purchase_order_id = ? AND status <> 'cancelled'",
            Long.class, purchaseOrderId
        );
        if (activeReturnCount != null && activeReturnCount > 0L)
        {
            throw new ServiceException("该采购订单已关联采购退货单，禁止" + actionLabel);
        }
    }

    private void clearUnpaidPayablesSafely(Long purchaseOrderId, String operator, String zeroRemark)
    {
        FinPayable payableQuery = new FinPayable();
        payableQuery.setPurchaseOrderId(purchaseOrderId);
        List<FinPayable> payables = finPayableMapper.selectFinPayableList(payableQuery);
        if (payables == null || payables.isEmpty())
        {
            return;
        }
        for (FinPayable p : payables)
        {
            BigDecimal paid = p.getAmountPaid() == null ? BigDecimal.ZERO : p.getAmountPaid();
            if (paid.compareTo(BigDecimal.ZERO) > 0)
            {
                throw new ServiceException("该采购订单关联应付账款已发生付款（¥" + paid + "），请先冲红付款记录");
            }
        }
        for (FinPayable p : payables)
        {
            Long paymentHistoryCount = jdbcTemplate == null ? 0L : jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM fin_payment WHERE payable_id = ?",
                Long.class, p.getPayableId()
            );
            if (paymentHistoryCount != null && paymentHistoryCount > 0L)
            {
                p.setAmountDue(BigDecimal.ZERO);
                p.setAmountPaid(BigDecimal.ZERO);
                p.setRemainAmount(BigDecimal.ZERO);
                p.setStatus("2");
                p.setRemark((StringUtils.isEmpty(p.getRemark()) ? "" : p.getRemark() + "；") + zeroRemark);
                p.setUpdateBy(operator);
                finPayableMapper.updateFinPayable(p);
            }
            else
            {
                finPayableMapper.deleteFinPayableByPayableId(p.getPayableId());
            }
        }
    }

    /**
     * 校验采购入库关联的采购订单是否合法
     *
     * @param purchaseOrderId 采购订单主键
     * @param supplierId 供应商主键
     * @return 采购订单
     */
    @Override
    public WmsPurchaseOrder validatePurchaseOrderForInbound(Long purchaseOrderId, Long supplierId)
    {
        if (purchaseOrderId == null)
        {
            return null;
        }
        WmsPurchaseOrder databasePurchaseOrder = wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(purchaseOrderId);
        if (databasePurchaseOrder == null)
        {
            throw new ServiceException("来源采购订单不存在");
        }
        if (STATUS_DRAFT.equals(databasePurchaseOrder.getStatus()) || STATUS_PENDING_AUDIT.equals(databasePurchaseOrder.getStatus()))
        {
            throw new ServiceException("未审核采购订单（草稿/待审核）不允许新增采购入库");
        }
        if (STATUS_CANCELLED.equals(databasePurchaseOrder.getStatus()))
        {
            throw new ServiceException("已作废采购订单不允许新增采购入库");
        }
        if (STATUS_COMPLETED.equals(databasePurchaseOrder.getStatus()))
        {
            throw new ServiceException("已完成采购订单不允许继续新增采购入库");
        }
        if (supplierId != null && !supplierId.equals(databasePurchaseOrder.getSupplierId()))
        {
            throw new ServiceException("入库单供应商必须与来源采购订单保持一致");
        }
        return databasePurchaseOrder;
    }

    /**
     * 根据采购入库进度刷新采购订单状态
     *
     * @param purchaseOrderId 采购订单主键
     */
    @Override
    public void refreshPurchaseOrderInboundProgress(Long purchaseOrderId)
    {
        if (purchaseOrderId == null)
        {
            return;
        }
        WmsPurchaseOrder databasePurchaseOrder = wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(purchaseOrderId);
        if (databasePurchaseOrder == null || STATUS_CANCELLED.equals(databasePurchaseOrder.getStatus()))
        {
            return;
        }
        String targetStatus = buildInboundProgressStatus(databasePurchaseOrder);
        if (targetStatus.equals(databasePurchaseOrder.getStatus()))
        {
            return;
        }
        String updateUsername = SecurityUtils.getUsername();
        wmsPurchaseOrderMapper.updateWmsPurchaseOrderInboundStatus(purchaseOrderId, targetStatus, updateUsername);
    }

    /**
     * 批量删除采购订单
     * 
     * @param purchaseOrderIds 需要删除的采购订单主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteWmsPurchaseOrderByPurchaseOrderIds(Long[] purchaseOrderIds)
    {
        for (Long purchaseOrderId : purchaseOrderIds)
        {
            WmsPurchaseOrder databasePurchaseOrder = wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(purchaseOrderId);
            if (databasePurchaseOrder == null)
            {
                continue;
            }
            if (!STATUS_DRAFT.equals(databasePurchaseOrder.getStatus()))
            {
                throw new ServiceException("仅草稿状态采购订单允许删除，待审核单据请先驳回，采购单号：" + databasePurchaseOrder.getOrderNo());
            }
        }
        wmsPurchaseOrderMapper.deleteWmsPurchaseOrderItemByPurchaseOrderIds(purchaseOrderIds);
        return wmsPurchaseOrderMapper.deleteWmsPurchaseOrderByPurchaseOrderIds(purchaseOrderIds);
    }

    @Transactional
    @Override
    public int deleteWmsPurchaseOrderByPurchaseOrderId(Long purchaseOrderId)
    {
        WmsPurchaseOrder databasePurchaseOrder = wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(purchaseOrderId);
        if (databasePurchaseOrder == null)
        {
            return 0;
        }
        if (!STATUS_DRAFT.equals(databasePurchaseOrder.getStatus()))
        {
            throw new ServiceException("仅草稿状态采购订单允许删除，待审核单据请先驳回");
        }
        wmsPurchaseOrderMapper.deleteWmsPurchaseOrderItemByPurchaseOrderId(purchaseOrderId);
        return wmsPurchaseOrderMapper.deleteWmsPurchaseOrderByPurchaseOrderId(purchaseOrderId);
    }

    /**
     * 新增采购订单明细信息
     * 
     * @param wmsPurchaseOrder 采购订单对象
     */
    public void insertWmsPurchaseOrderItem(WmsPurchaseOrder wmsPurchaseOrder)
    {
        List<WmsPurchaseOrderItem> wmsPurchaseOrderItemList = wmsPurchaseOrder.getWmsPurchaseOrderItemList();
        Long purchaseOrderId = wmsPurchaseOrder.getPurchaseOrderId();
        if (StringUtils.isNotNull(wmsPurchaseOrderItemList))
        {
            List<WmsPurchaseOrderItem> list = new ArrayList<WmsPurchaseOrderItem>();
            for (WmsPurchaseOrderItem wmsPurchaseOrderItem : wmsPurchaseOrderItemList)
            {
                wmsPurchaseOrderItem.setPurchaseOrderId(purchaseOrderId);
                list.add(wmsPurchaseOrderItem);
            }
            if (list.size() > 0)
            {
                wmsPurchaseOrderMapper.batchWmsPurchaseOrderItem(list);
            }
        }
    }

    /**
     * 保存采购订单前校验主单和明细数据，并重算总数量与总金额
     * 
     * @param wmsPurchaseOrder 采购订单
     */
    private void validatePurchaseOrderBeforeSave(WmsPurchaseOrder wmsPurchaseOrder)
    {
        if (wmsPurchaseOrder == null)
        {
            throw new ServiceException("采购订单不存在");
        }
        if (wmsPurchaseOrder.getSupplierId() == null)
        {
            throw new ServiceException("供应商不能为空");
        }
        if (wmsPurchaseOrder.getPurchaseDate() == null)
        {
            throw new ServiceException("采购日期不能为空");
        }
        validatePurchaseOrderItemList(wmsPurchaseOrder.getWmsPurchaseOrderItemList());
        wmsPurchaseOrder.setTotalQuantity(calculatePurchaseOrderTotalQuantity(wmsPurchaseOrder.getWmsPurchaseOrderItemList()));
        wmsPurchaseOrder.setTotalAmount(calculatePurchaseOrderTotalAmount(wmsPurchaseOrder.getWmsPurchaseOrderItemList()));
    }

    /**
     * 校验采购订单明细是否完整
     * 
     * @param wmsPurchaseOrderItemList 采购订单明细列表
     */
    private void validatePurchaseOrderItemList(List<WmsPurchaseOrderItem> wmsPurchaseOrderItemList)
    {
        if (wmsPurchaseOrderItemList == null || wmsPurchaseOrderItemList.isEmpty())
        {
            throw new ServiceException("采购明细不能为空");
        }
        for (int purchaseOrderItemIndex = 0; purchaseOrderItemIndex < wmsPurchaseOrderItemList.size(); purchaseOrderItemIndex++)
        {
            WmsPurchaseOrderItem wmsPurchaseOrderItem = wmsPurchaseOrderItemList.get(purchaseOrderItemIndex);
            if (wmsPurchaseOrderItem.getProductId() == null)
            {
                throw new ServiceException("第" + (purchaseOrderItemIndex + 1) + "行采购明细商品不能为空");
            }
            if (wmsPurchaseOrderItem.getQuantity() == null || wmsPurchaseOrderItem.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new ServiceException("第" + (purchaseOrderItemIndex + 1) + "行采购数量必须大于0");
            }
            if (wmsPurchaseOrderItem.getPrice() == null || wmsPurchaseOrderItem.getPrice().compareTo(BigDecimal.ZERO) < 0)
            {
                throw new ServiceException("第" + (purchaseOrderItemIndex + 1) + "行采购单价不能小于0");
            }
            wmsPurchaseOrderItem.setAmount(calculatePurchaseOrderItemAmount(wmsPurchaseOrderItem));
        }
    }

    /**
     * 根据采购订单明细重新计算总数量
     * 
     * @param wmsPurchaseOrderItemList 采购订单明细列表
     * @return 总数量
     */
    private BigDecimal calculatePurchaseOrderTotalQuantity(List<WmsPurchaseOrderItem> wmsPurchaseOrderItemList)
    {
        BigDecimal totalQuantityValue = BigDecimal.ZERO;
        if (wmsPurchaseOrderItemList == null || wmsPurchaseOrderItemList.isEmpty())
        {
            return totalQuantityValue;
        }
        for (WmsPurchaseOrderItem wmsPurchaseOrderItem : wmsPurchaseOrderItemList)
        {
            if (wmsPurchaseOrderItem.getQuantity() != null)
            {
                totalQuantityValue = totalQuantityValue.add(wmsPurchaseOrderItem.getQuantity());
            }
        }
        return totalQuantityValue;
    }

    /**
     * 根据采购订单明细重新计算总金额
     * 
     * @param wmsPurchaseOrderItemList 采购订单明细列表
     * @return 总金额
     */
    private BigDecimal calculatePurchaseOrderTotalAmount(List<WmsPurchaseOrderItem> wmsPurchaseOrderItemList)
    {
        BigDecimal totalAmountValue = BigDecimal.ZERO;
        if (wmsPurchaseOrderItemList == null || wmsPurchaseOrderItemList.isEmpty())
        {
            return totalAmountValue;
        }
        for (WmsPurchaseOrderItem wmsPurchaseOrderItem : wmsPurchaseOrderItemList)
        {
            totalAmountValue = totalAmountValue.add(calculatePurchaseOrderItemAmount(wmsPurchaseOrderItem));
        }
        return totalAmountValue;
    }

    /**
     * 计算采购明细金额
     * 
     * @param wmsPurchaseOrderItem 采购订单明细
     * @return 金额
     */
    private BigDecimal calculatePurchaseOrderItemAmount(WmsPurchaseOrderItem wmsPurchaseOrderItem)
    {
        BigDecimal quantityValue = wmsPurchaseOrderItem.getQuantity() == null ? BigDecimal.ZERO : wmsPurchaseOrderItem.getQuantity();
        BigDecimal priceValue = wmsPurchaseOrderItem.getPrice() == null ? BigDecimal.ZERO : wmsPurchaseOrderItem.getPrice();
        return quantityValue.multiply(priceValue).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 确保采购订单已生成对应应付账款
     * 
     * @param databasePurchaseOrder 数据库中的采购订单
     * @param createUsername 创建人
     */
    private void ensurePayableCreated(WmsPurchaseOrder databasePurchaseOrder, String createUsername)
    {
        FinPayable existingFinPayable =
            finPayableMapper.selectFinPayableByPurchaseOrderId(databasePurchaseOrder.getPurchaseOrderId());
        BigDecimal amountDue = databasePurchaseOrder.getTotalAmount() == null ? BigDecimal.ZERO : databasePurchaseOrder.getTotalAmount();
        if (existingFinPayable != null)
        {
            if ((existingFinPayable.getAmountDue() == null || existingFinPayable.getAmountDue().compareTo(BigDecimal.ZERO) == 0)
                && (existingFinPayable.getAmountPaid() == null || existingFinPayable.getAmountPaid().compareTo(BigDecimal.ZERO) == 0))
            {
                existingFinPayable.setAmountDue(amountDue);
                existingFinPayable.setAmountPaid(BigDecimal.ZERO);
                existingFinPayable.setRemainAmount(amountDue);
                existingFinPayable.setStatus("0");
                existingFinPayable.setUpdateBy(createUsername);
                finPayableMapper.updateFinPayable(existingFinPayable);
            }
            return;
        }
        FinPayable finPayable = new FinPayable();
        finPayable.setPurchaseOrderId(databasePurchaseOrder.getPurchaseOrderId());
        finPayable.setSupplierId(databasePurchaseOrder.getSupplierId());
        finPayable.setAmountDue(amountDue);
        finPayable.setAmountPaid(BigDecimal.ZERO);
        finPayable.setRemainAmount(amountDue);
        finPayable.setStatus("0");
        finPayable.setDueDate(databasePurchaseOrder.getPurchaseDate());
        finPayable.setCreateBy(createUsername);
        finPayable.setCreateTime(DateUtils.getNowDate());
        finPayableMapper.insertFinPayable(finPayable);
    }

    /**
     * 根据采购订单数量与已入库数量计算目标状态
     *
     * @param databasePurchaseOrder 采购订单
     * @return 目标状态
     */
    private String buildInboundProgressStatus(WmsPurchaseOrder databasePurchaseOrder)
    {
        BigDecimal totalQuantity = databasePurchaseOrder.getTotalQuantity() == null ? BigDecimal.ZERO : databasePurchaseOrder.getTotalQuantity();
        BigDecimal inboundQuantity = databasePurchaseOrder.getInboundQuantity() == null ? BigDecimal.ZERO : databasePurchaseOrder.getInboundQuantity();
        if (totalQuantity.compareTo(BigDecimal.ZERO) <= 0 || inboundQuantity.compareTo(BigDecimal.ZERO) <= 0)
        {
            return STATUS_AUDITED;
        }
        if (inboundQuantity.compareTo(totalQuantity) >= 0)
        {
            return STATUS_COMPLETED;
        }
        return STATUS_PARTIALLY_INBOUND;
    }

    /**
     * 查询采购订单关联入库单集合
     *
     * @param purchaseOrderId 采购订单主键
     * @return 入库单集合
     */
    private List<WmsInbound> selectPurchaseOrderChainInboundList(Long purchaseOrderId)
    {
        WmsInbound inboundQuery = new WmsInbound();
        inboundQuery.setPurchaseOrderId(purchaseOrderId);
        return wmsInboundMapper.selectWmsInboundList(inboundQuery);
    }

    /**
     * 查询采购订单关联应付台账集合
     *
     * @param purchaseOrderId 采购订单主键
     * @return 应付台账集合
     */
    private List<FinPayable> selectPurchaseOrderChainPayableList(Long purchaseOrderId)
    {
        FinPayable payableQuery = new FinPayable();
        payableQuery.setPurchaseOrderId(purchaseOrderId);
        return finPayableMapper.selectFinPayableList(payableQuery);
    }

    /**
     * 查询采购订单关联付款流水集合
     *
     * @param purchaseOrderId 采购订单主键
     * @return 付款流水集合
     */
    private List<FinPayment> selectPurchaseOrderChainPaymentList(Long purchaseOrderId)
    {
        FinPayment paymentQuery = new FinPayment();
        paymentQuery.setPurchaseOrderId(purchaseOrderId);
        return finPaymentMapper.selectFinPaymentList(paymentQuery);
    }

    private List<WmsPurchaseReturn> selectPurchaseOrderChainReturnList(Long purchaseOrderId)
    {
        if (wmsPurchaseReturnMapper == null || purchaseOrderId == null)
        {
            return new ArrayList<>();
        }
        WmsPurchaseReturn query = new WmsPurchaseReturn();
        query.setPurchaseOrderId(purchaseOrderId);
        List<WmsPurchaseReturn> list = wmsPurchaseReturnMapper.selectWmsPurchaseReturnList(query);
        return list == null ? new ArrayList<>() : list;
    }

    private List<FinInvoice> selectPurchaseOrderChainInvoiceList(Long purchaseOrderId)
    {
        if (finInvoiceMapper == null || purchaseOrderId == null)
        {
            return new ArrayList<>();
        }
        FinInvoice query = new FinInvoice();
        query.setInvoiceDirection("purchase");
        query.setSourceOrderId(purchaseOrderId);
        List<FinInvoice> list = finInvoiceMapper.selectFinInvoiceList(query);
        return list == null ? new ArrayList<>() : list;
    }

    /**
     * 构建采购订单链路节点
     *
     * @param purchaseOrder 采购订单
     * @param purchaseOrderChainMap 采购订单链路数据
     * @return 链路节点集合
     */
    private List<Map<String, Object>> buildPurchaseOrderChainNodeList(WmsPurchaseOrder purchaseOrder, Map<String, Object> purchaseOrderChainMap)
    {
        List<Map<String, Object>> chainNodeList = new ArrayList<>();
        chainNodeList.add(buildChainNode("purchaseOrder", "采购订单", "已创建", purchaseOrder.getOrderNo()));
        chainNodeList.add(buildChainNode("audit", "采购审核", buildPurchaseOrderAuditStatus(purchaseOrder.getStatus()), null));
        chainNodeList.add(buildChainNode("inbound", "采购入库", buildChainNodeStatus(purchaseOrderChainMap.get("inboundList")), null));
        chainNodeList.add(buildChainNode("payable", "应付台账", buildChainNodeStatus(purchaseOrderChainMap.get("payableList")), null));
        chainNodeList.add(buildChainNode("payment", "付款流水", buildChainNodeStatus(purchaseOrderChainMap.get("paymentList")), null));
        chainNodeList.add(buildChainNode("purchaseReturn", "采购退货", buildChainNodeStatus(purchaseOrderChainMap.get("purchaseReturnList")), null));
        chainNodeList.add(buildChainNode("invoice", "进项发票", buildChainNodeStatus(purchaseOrderChainMap.get("invoiceList")), null));
        return chainNodeList;
    }

    /**
     * 构建采购订单审核节点状态
     *
     * @param statusValue 采购订单状态
     * @return 审核节点状态
     */
    private String buildPurchaseOrderAuditStatus(String statusValue)
    {
        if (STATUS_PENDING_AUDIT.equals(statusValue))
        {
            return "待审核";
        }
        if (STATUS_CANCELLED.equals(statusValue))
        {
            return "已作废";
        }
        return "已审核";
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

    @Override
    public Map<String, Object> suggestPurchasePrice(Long supplierId, Long productId)
    {
        Map<String, Object> result = new HashMap<>();
        if (productId == null)
        {
            return result;
        }
        BigDecimal lastSupplierPrice = null;
        BigDecimal lastInboundPrice = null;
        BigDecimal lowestPoPrice = null;
        BigDecimal currentCostPrice = null;
        if (jdbcTemplate != null)
        {
            if (supplierId != null)
            {
                List<BigDecimal> supPrices = jdbcTemplate.queryForList(
                    "SELECT poi.price FROM wms_purchase_order_item poi "
                    + "JOIN wms_purchase_order po ON po.purchase_order_id = poi.purchase_order_id "
                    + "WHERE po.supplier_id = ? AND poi.product_id = ? AND po.status IN ('1','2','3') AND poi.price > 0 "
                    + "ORDER BY po.purchase_order_id DESC LIMIT 1",
                    BigDecimal.class, supplierId, productId
                );
                if (!supPrices.isEmpty())
                {
                    lastSupplierPrice = supPrices.get(0);
                }
            }
            List<BigDecimal> inPrices = jdbcTemplate.queryForList(
                "SELECT ii.price FROM wms_inbound_item ii "
                + "JOIN wms_inbound i ON i.inbound_id = ii.inbound_id "
                + "WHERE ii.product_id = ? AND i.status = 'audited' AND ii.price > 0 "
                + "ORDER BY i.inbound_id DESC LIMIT 1",
                BigDecimal.class, productId
            );
            if (!inPrices.isEmpty())
            {
                lastInboundPrice = inPrices.get(0);
            }
            List<BigDecimal> minPrices = jdbcTemplate.queryForList(
                "SELECT MIN(poi.price) FROM wms_purchase_order_item poi "
                + "JOIN wms_purchase_order po ON po.purchase_order_id = poi.purchase_order_id "
                + "WHERE poi.product_id = ? AND po.status IN ('1','2','3') AND poi.price > 0",
                BigDecimal.class, productId
            );
            if (!minPrices.isEmpty())
            {
                lowestPoPrice = minPrices.get(0);
            }
            List<BigDecimal> costPrices = jdbcTemplate.queryForList(
                "SELECT cost_price FROM md_product WHERE product_id = ?",
                BigDecimal.class, productId
            );
            if (!costPrices.isEmpty() && costPrices.get(0) != null)
            {
                currentCostPrice = costPrices.get(0);
            }
        }
        BigDecimal suggestedPrice = BigDecimal.ZERO;
        String priceSource = "暂无历史进价";
        if (lastSupplierPrice != null && lastSupplierPrice.compareTo(BigDecimal.ZERO) > 0)
        {
            suggestedPrice = lastSupplierPrice;
            priceSource = "该供应商最近成交价";
        }
        else if (lastInboundPrice != null && lastInboundPrice.compareTo(BigDecimal.ZERO) > 0)
        {
            suggestedPrice = lastInboundPrice;
            priceSource = "最近审核入库价";
        }
        else if (currentCostPrice != null && currentCostPrice.compareTo(BigDecimal.ZERO) > 0)
        {
            suggestedPrice = currentCostPrice.setScale(2, RoundingMode.HALF_UP);
            priceSource = "当前移动加权成本价";
        }
        result.put("suggestedPrice", suggestedPrice);
        result.put("priceSource", priceSource);
        result.put("lastSupplierPrice", lastSupplierPrice);
        result.put("lastInboundPrice", lastInboundPrice);
        result.put("lowestPoPrice", lowestPoPrice);
        result.put("currentCostPrice", currentCostPrice);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createReplenishPurchaseOrders(Long defaultSupplierId, List<Map<String, Object>> items, String operator)
    {
        if (items == null || items.isEmpty())
        {
            throw new ServiceException("请至少选择一条需要补货的商品");
        }
        Long fallbackSupplierId = defaultSupplierId;
        if (fallbackSupplierId == null && jdbcTemplate != null)
        {
            List<Long> activeSups = jdbcTemplate.queryForList(
                "SELECT supplier_id FROM md_supplier WHERE status = '0' AND del_flag = '0' ORDER BY supplier_id ASC LIMIT 1",
                Long.class
            );
            if (!activeSups.isEmpty())
            {
                fallbackSupplierId = activeSups.get(0);
            }
        }
        Map<Long, List<WmsPurchaseOrderItem>> groupedBySupplier = new java.util.LinkedHashMap<>();
        for (Map<String, Object> row : items)
        {
            if (row == null || row.get("productId") == null)
            {
                continue;
            }
            Long productId = Long.valueOf(String.valueOf(row.get("productId")));
            Long targetSupplierId = row.get("supplierId") != null
                ? Long.valueOf(String.valueOf(row.get("supplierId")))
                : defaultSupplierId;
            if (targetSupplierId == null && jdbcTemplate != null)
            {
                List<Long> lastSups = jdbcTemplate.queryForList(
                    "SELECT po.supplier_id FROM wms_purchase_order_item poi "
                    + "JOIN wms_purchase_order po ON po.purchase_order_id = poi.purchase_order_id "
                    + "WHERE poi.product_id = ? AND po.status IN ('1','2','3') "
                    + "ORDER BY po.purchase_order_id DESC LIMIT 1",
                    Long.class, productId
                );
                if (!lastSups.isEmpty())
                {
                    targetSupplierId = lastSups.get(0);
                }
            }
            if (targetSupplierId == null)
            {
                targetSupplierId = fallbackSupplierId;
            }
            if (targetSupplierId == null)
            {
                throw new ServiceException("未找到可用启用供应商，请先指定补货供应商");
            }
            BigDecimal reqQty = null;
            if (row.get("quantity") != null && StringUtils.isNotEmpty(String.valueOf(row.get("quantity"))))
            {
                reqQty = new BigDecimal(String.valueOf(row.get("quantity")));
            }
            if (reqQty == null || reqQty.compareTo(BigDecimal.ZERO) <= 0)
            {
                reqQty = new BigDecimal("10");
                if (jdbcTemplate != null)
                {
                    Map<String, Object> stockStat = jdbcTemplate.queryForMap(
                        "SELECT IFNULL(MAX(warning_min_qty), 0) AS min_qty, "
                        + "IFNULL(SUM(quantity - IFNULL(locked_quantity, 0) - IFNULL(frozen_quantity, 0)), 0) AS net_avail "
                        + "FROM wms_stock WHERE product_id = ?",
                        productId
                    );
                    BigDecimal minQty = new BigDecimal(String.valueOf(stockStat.get("min_qty")));
                    BigDecimal netAvail = new BigDecimal(String.valueOf(stockStat.get("net_avail")));
                    BigDecimal inTransit = jdbcTemplate.queryForObject(
                        "SELECT IFNULL(SUM(GREATEST(0, poi.quantity - IFNULL(ib.rec_qty, 0))), 0) "
                        + "FROM wms_purchase_order_item poi JOIN wms_purchase_order po ON po.purchase_order_id = poi.purchase_order_id "
                        + "LEFT JOIN (SELECT ii.purchase_order_item_id, SUM(ii.quantity) AS rec_qty FROM wms_inbound_item ii "
                        + "JOIN wms_inbound i ON i.inbound_id = ii.inbound_id WHERE i.status = 'audited' GROUP BY ii.purchase_order_item_id) ib "
                        + "ON ib.purchase_order_item_id = poi.purchase_order_item_id "
                        + "WHERE poi.product_id = ? AND po.status IN ('0','1','2')",
                        BigDecimal.class, productId
                    );
                    BigDecimal shortage = minQty.subtract(netAvail).subtract(inTransit == null ? BigDecimal.ZERO : inTransit);
                    if (shortage.compareTo(BigDecimal.ZERO) > 0)
                    {
                        reqQty = shortage;
                    }
                }
            }
            Map<String, Object> priceInfo = suggestPurchasePrice(targetSupplierId, productId);
            BigDecimal unitPrice = row.get("price") != null && StringUtils.isNotEmpty(String.valueOf(row.get("price")))
                ? new BigDecimal(String.valueOf(row.get("price")))
                : (BigDecimal) priceInfo.getOrDefault("suggestedPrice", BigDecimal.ZERO);

            WmsPurchaseOrderItem poItem = new WmsPurchaseOrderItem();
            poItem.setProductId(productId);
            poItem.setQuantity(reqQty);
            poItem.setPrice(unitPrice == null ? BigDecimal.ZERO : unitPrice);
            poItem.setAmount(poItem.getQuantity().multiply(poItem.getPrice()).setScale(2, RoundingMode.HALF_UP));
            poItem.setRemark("缺货预警自动补货");
            groupedBySupplier.computeIfAbsent(targetSupplierId, k -> new java.util.ArrayList<>()).add(poItem);
        }
        List<Long> createdIds = new java.util.ArrayList<>();
        List<String> createdNos = new java.util.ArrayList<>();
        int seq = 1;
        for (Map.Entry<Long, List<WmsPurchaseOrderItem>> entry : groupedBySupplier.entrySet())
        {
            WmsPurchaseOrder po = new WmsPurchaseOrder();
            po.setOrderNo("PO" + DateUtils.dateTimeNow() + String.format("%02d", seq++));
            po.setSupplierId(entry.getKey());
            po.setPurchaseDate(DateUtils.getNowDate());
            po.setStatus(STATUS_DRAFT);
            po.setCreateBy(operator);
            po.setRemark("由库存缺货预警一键生成的补货草稿单");
            po.setWmsPurchaseOrderItemList(entry.getValue());
            insertWmsPurchaseOrder(po);
            createdIds.add(po.getPurchaseOrderId());
            createdNos.add(po.getOrderNo());
        }
        Map<String, Object> summary = new HashMap<>();
        summary.put("createdCount", createdIds.size());
        summary.put("purchaseOrderIds", createdIds);
        summary.put("orderNos", createdNos);
        return summary;
    }
}
