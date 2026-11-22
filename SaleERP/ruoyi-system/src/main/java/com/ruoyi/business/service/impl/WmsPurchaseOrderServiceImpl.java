package com.ruoyi.business.service.impl;

import java.util.List;
import java.util.Map;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.math.BigDecimal;
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
            complianceHelper.assertPeriodOpen(new java.util.Date(), "反审核采购订单");
        }
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
        FinPayable payableQuery = new FinPayable();
        payableQuery.setPurchaseOrderId(purchaseOrderId);
        List<FinPayable> payables = finPayableMapper.selectFinPayableList(payableQuery);
        if (payables != null)
        {
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
                finPayableMapper.deleteFinPayableByPayableId(p.getPayableId());
            }
        }
        int rows = wmsPurchaseOrderMapper.updateWmsPurchaseOrderStatus(
            purchaseOrderId, STATUS_AUDITED, STATUS_DRAFT, SecurityUtils.getUsername());
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
                    p.setStatus(remain.compareTo(BigDecimal.ZERO) <= 0 ? "2" : (paid.compareTo(BigDecimal.ZERO) > 0 ? "1" : "0"));
                    p.setRemark((StringUtils.isEmpty(p.getRemark()) ? "" : p.getRemark() + "；") + "采购单短装结案按实收调减应付至¥" + targetDue);
                    p.setUpdateBy(operator);
                    finPayableMapper.updateFinPayable(p);
                }
                else
                {
                    BigDecimal overpaid = paid.subtract(targetDue);
                    p.setAmountDue(paid);
                    p.setStatus("2");
                    p.setRemark((StringUtils.isEmpty(p.getRemark()) ? "" : p.getRemark() + "；") + "采购单短装结案，多付¥" + overpaid + "转红字待退款");
                    p.setUpdateBy(operator);
                    finPayableMapper.updateFinPayable(p);

                    FinPayable refundPayable = new FinPayable();
                    refundPayable.setPurchaseOrderId(purchaseOrderId);
                    refundPayable.setSupplierId(po.getSupplierId());
                    refundPayable.setAmountDue(overpaid.negate());
                    refundPayable.setAmountPaid(BigDecimal.ZERO);
                    refundPayable.setStatus("0");
                    refundPayable.setRemark("采购单[" + po.getOrderNo() + "]短装结案多付款待供应商退款");
                    refundPayable.setCreateBy(operator);
                    refundPayable.setCreateTime(DateUtils.getNowDate());
                    finPayableMapper.insertFinPayable(refundPayable);
                }
                break;
            }
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
            complianceHelper.assertPeriodOpen(new java.util.Date(), "作废采购订单");
        }
        if (STATUS_AUDITED.equals(databasePurchaseOrder.getStatus()))
        {
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
            FinPayable payableQuery = new FinPayable();
            payableQuery.setPurchaseOrderId(purchaseOrderId);
            List<FinPayable> payables = finPayableMapper.selectFinPayableList(payableQuery);
            if (payables != null)
            {
                for (FinPayable p : payables)
                {
                    BigDecimal paid = p.getAmountPaid() == null ? BigDecimal.ZERO : p.getAmountPaid();
                    if (paid.compareTo(BigDecimal.ZERO) > 0)
                    {
                        throw new ServiceException("该采购单已发生付款，请先冲红付款后再作废");
                    }
                }
                for (FinPayable p : payables)
                {
                    finPayableMapper.deleteFinPayableByPayableId(p.getPayableId());
                }
            }
        }
        String updateUsername = SecurityUtils.getUsername();
        int updateCount = wmsPurchaseOrderMapper.updateWmsPurchaseOrderStatus(
            purchaseOrderId, databasePurchaseOrder.getStatus(), STATUS_CANCELLED, updateUsername);
        if (updateCount == 0)
        {
            throw new ServiceException("采购订单状态已变更，请刷新后重试");
        }
        return updateCount;
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
        if (!STATUS_DRAFT.equals(databasePurchaseOrder.getStatus()) && !STATUS_PENDING_AUDIT.equals(databasePurchaseOrder.getStatus()))
        {
            throw new ServiceException("仅草稿或待审核状态采购订单允许删除");
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
        return quantityValue.multiply(priceValue);
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
        if (existingFinPayable != null)
        {
            return;
        }
        BigDecimal amountDue = databasePurchaseOrder.getTotalAmount() == null ? BigDecimal.ZERO : databasePurchaseOrder.getTotalAmount();
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
}
