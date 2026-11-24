package com.ruoyi.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import com.ruoyi.business.domain.FinInvoice;
import com.ruoyi.business.domain.FinPayable;
import com.ruoyi.business.domain.MdCustomer;
import com.ruoyi.business.domain.MdProduct;
import com.ruoyi.business.domain.WmsInbound;
import com.ruoyi.business.domain.WmsInboundItem;
import com.ruoyi.business.domain.WmsInventoryCheck;
import com.ruoyi.business.domain.WmsInventoryCheckItem;
import com.ruoyi.business.domain.WmsPurchaseOrder;
import com.ruoyi.business.domain.WmsSaleOrder;
import com.ruoyi.business.mapper.FinInvoiceMapper;
import com.ruoyi.business.mapper.FinPayableMapper;
import com.ruoyi.business.mapper.FinPaymentMapper;
import com.ruoyi.business.mapper.FinReceivableMapper;
import com.ruoyi.business.mapper.MdCustomerMapper;
import com.ruoyi.business.mapper.MdLocationMapper;
import com.ruoyi.business.mapper.MdProductMapper;
import com.ruoyi.business.mapper.WmsInboundItemMapper;
import com.ruoyi.business.mapper.WmsInboundMapper;
import com.ruoyi.business.mapper.WmsInventoryCheckItemMapper;
import com.ruoyi.business.mapper.WmsInventoryCheckMapper;
import com.ruoyi.business.mapper.WmsOutboundMapper;
import com.ruoyi.business.mapper.WmsPurchaseOrderMapper;
import com.ruoyi.business.mapper.WmsPurchaseReturnMapper;
import com.ruoyi.business.mapper.WmsSaleOrderMapper;
import com.ruoyi.business.mapper.WmsSaleReturnMapper;
import com.ruoyi.business.service.support.BizComplianceHelper;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
class DeepAuditFixesTest
{
    @Mock
    private FinInvoiceMapper finInvoiceMapper;

    @Mock
    private WmsSaleOrderMapper wmsSaleOrderMapper;

    @Mock
    private WmsPurchaseOrderMapper wmsPurchaseOrderMapper;

    @Mock
    private MdCustomerMapper mdCustomerMapper;

    @Mock
    private FinPayableMapper finPayableMapper;

    @Mock
    private FinPaymentMapper finPaymentMapper;

    @Mock
    private WmsInboundMapper wmsInboundMapper;

    @Mock
    private WmsInboundItemMapper wmsInboundItemMapper;

    @Mock
    private WmsOutboundMapper wmsOutboundMapper;

    @Mock
    private WmsSaleReturnMapper wmsSaleReturnMapper;

    @Mock
    private WmsPurchaseReturnMapper wmsPurchaseReturnMapper;

    @Mock
    private FinReceivableMapper finReceivableMapper;

    @Mock
    private WmsInventoryCheckMapper wmsInventoryCheckMapper;

    @Mock
    private WmsInventoryCheckItemMapper wmsInventoryCheckItemMapper;

    @Mock
    private MdProductMapper mdProductMapper;

    @Mock
    private MdLocationMapper mdLocationMapper;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private FinInvoiceService finInvoiceService;

    @InjectMocks
    private WmsPurchaseOrderServiceImpl wmsPurchaseOrderService;

    @InjectMocks
    private WmsSaleOrderServiceImpl wmsSaleOrderService;

    @InjectMocks
    private WmsInboundServiceImpl wmsInboundService;

    @InjectMocks
    private WmsInventoryCheckItemServiceImpl wmsInventoryCheckItemService;

    @InjectMocks
    private BizComplianceHelper bizComplianceHelper;

    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

    private void mockLoginUser(String username)
    {
        SysUser user = new SysUser();
        user.setUserId(1L);
        user.setUserName(username);
        LoginUser loginUser = new LoginUser(1L, 100L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
    }

    @Test
    void issuedInvoiceCannotBeModifiedAndRedVoidCannotBeInjected()
    {
        FinInvoice existing = new FinInvoice();
        existing.setInvoiceId(10L);
        existing.setStatus("issued");
        when(finInvoiceMapper.selectFinInvoiceById(10L)).thenReturn(existing);

        FinInvoice patch = new FinInvoice();
        patch.setInvoiceId(10L);
        patch.setTotalAmount(new BigDecimal("100.00"));
        ServiceException ex1 = assertThrows(ServiceException.class, () -> finInvoiceService.updateFinInvoice(patch));
        assertTrue(ex1.getMessage().contains("已开具") || ex1.getMessage().contains("红冲"));

        FinInvoice forged = new FinInvoice();
        forged.setInvoiceNo("INV-FORGE-01");
        forged.setInvoiceDirection("sale");
        forged.setSourceOrderId(1L);
        forged.setTaxRate(new BigDecimal("13.00"));
        forged.setTotalAmount(new BigDecimal("113.00"));
        forged.setStatus("red_void");
        ServiceException ex2 = assertThrows(ServiceException.class, () -> finInvoiceService.insertFinInvoice(forged));
        assertTrue(ex2.getMessage().contains("draft") || ex2.getMessage().contains("状态"));
    }

    @Test
    void shortClosedSaleOrderCapsInvoiceAtActualShippedAmount()
    {
        WmsSaleOrder closedOrder = new WmsSaleOrder();
        closedOrder.setSaleOrderId(20L);
        closedOrder.setOrderNo("SO-CLOSED-01");
        closedOrder.setCustomerId(1L);
        closedOrder.setStatus("closed");
        closedOrder.setTotalAmount(new BigDecimal("10000.00"));

        when(wmsSaleOrderMapper.selectWmsSaleOrderById(20L)).thenReturn(closedOrder);
        when(jdbcTemplate.queryForObject(
            eq("SELECT IFNULL(SUM(total_amount), 0) FROM wms_outbound WHERE sale_order_id = ? AND status = 'audited'"),
            eq(BigDecimal.class), eq(20L))).thenReturn(new BigDecimal("4000.00"));
        when(jdbcTemplate.queryForObject(
            eq("SELECT IFNULL(SUM(total_amount), 0) FROM wms_sale_return WHERE sale_order_id = ? AND status = 'audited'"),
            eq(BigDecimal.class), eq(20L))).thenReturn(BigDecimal.ZERO);
        when(finInvoiceMapper.selectActiveAmountBySourceOrder("sale", 20L, null)).thenReturn(BigDecimal.ZERO);

        FinInvoice tooBig = new FinInvoice();
        tooBig.setInvoiceNo("INV-CLOSED-01");
        tooBig.setInvoiceDirection("sale");
        tooBig.setSourceOrderId(20L);
        tooBig.setTaxRate(new BigDecimal("13.00"));
        tooBig.setTotalAmount(new BigDecimal("5000.00"));

        ServiceException ex = assertThrows(ServiceException.class, () -> finInvoiceService.insertFinInvoice(tooBig));
        assertTrue(ex.getMessage().contains("4000.00"));
    }

    @Test
    void purchaseOrderSingleDeleteRejectsNonDraftAndForceCloseSetsRemainAmount()
    {
        mockLoginUser("admin");

        WmsPurchaseOrder pendingPo = new WmsPurchaseOrder();
        pendingPo.setPurchaseOrderId(30L);
        pendingPo.setStatus("0");
        when(wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(30L)).thenReturn(pendingPo);

        ServiceException delEx = assertThrows(ServiceException.class,
            () -> wmsPurchaseOrderService.deleteWmsPurchaseOrderByPurchaseOrderId(30L));
        assertTrue(delEx.getMessage().contains("草稿"));

        // Short-close overpaid PO (audited inbound = 400, paid = 600)
        // => original payable due = 600, remain = 0, status = "2"; red-ink refund payable due = -200, remain = -200
        WmsPurchaseOrder auditedPo = new WmsPurchaseOrder();
        auditedPo.setPurchaseOrderId(31L);
        auditedPo.setOrderNo("PO-31");
        auditedPo.setSupplierId(5L);
        auditedPo.setStatus("2");
        auditedPo.setTotalAmount(new BigDecimal("1000.00"));
        auditedPo.setPurchaseDate(new Date());

        WmsInbound auditedInbound = new WmsInbound();
        auditedInbound.setInboundId(100L);
        auditedInbound.setStatus("audited");
        auditedInbound.setTotalAmount(new BigDecimal("400.00"));

        FinPayable payable = new FinPayable();
        payable.setPayableId(301L);
        payable.setPurchaseOrderId(31L);
        payable.setAmountDue(new BigDecimal("1000.00"));
        payable.setAmountPaid(new BigDecimal("600.00"));
        payable.setRemainAmount(new BigDecimal("400.00"));

        when(wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(31L)).thenReturn(auditedPo);
        when(wmsInboundMapper.selectWmsInboundList(any(WmsInbound.class))).thenReturn(Collections.singletonList(auditedInbound));
        when(jdbcTemplate.queryForObject(
            eq("SELECT IFNULL(SUM(total_amount), 0) FROM fin_invoice WHERE invoice_direction = 'purchase' AND source_order_id = ? AND status IN ('draft', 'issued')"),
            eq(BigDecimal.class), eq(31L))).thenReturn(BigDecimal.ZERO);
        when(wmsPurchaseOrderMapper.updateWmsPurchaseOrderStatus(eq(31L), eq("2"), eq("3"), eq("admin"))).thenReturn(1);
        when(finPayableMapper.selectFinPayableList(any(FinPayable.class))).thenReturn(Collections.singletonList(payable));

        wmsPurchaseOrderService.forceCloseWmsPurchaseOrder(31L);

        ArgumentCaptor<FinPayable> updateCaptor = ArgumentCaptor.forClass(FinPayable.class);
        verify(finPayableMapper).updateFinPayable(updateCaptor.capture());
        assertEquals(new BigDecimal("600.00"), updateCaptor.getValue().getAmountDue());
        assertEquals(BigDecimal.ZERO, updateCaptor.getValue().getRemainAmount());
        assertEquals("2", updateCaptor.getValue().getStatus());

        ArgumentCaptor<FinPayable> insertCaptor = ArgumentCaptor.forClass(FinPayable.class);
        verify(finPayableMapper).insertFinPayable(insertCaptor.capture());
        assertEquals(new BigDecimal("-200.00"), insertCaptor.getValue().getAmountDue());
        assertEquals(new BigDecimal("-200.00"), insertCaptor.getValue().getRemainAmount());
    }

    @Test
    void saleOrderFinanceRollbackBlocksWhenActiveInvoiceOrReturnExists()
    {
        WmsSaleOrder auditedSo = new WmsSaleOrder();
        auditedSo.setSaleOrderId(40L);
        auditedSo.setOrderNo("SO-40");
        auditedSo.setStatus("audited");
        when(wmsSaleOrderMapper.selectWmsSaleOrderById(40L)).thenReturn(auditedSo);
        when(jdbcTemplate.queryForObject(
            eq("SELECT COUNT(*) FROM fin_invoice WHERE invoice_direction = 'sale' AND source_order_id = ? AND status IN ('draft', 'issued')"),
            eq(Long.class), eq(40L))).thenReturn(1L);

        ServiceException invEx = assertThrows(ServiceException.class,
            () -> wmsSaleOrderService.financeRollbackWmsSaleOrder(40L, "测试回退"));
        assertTrue(invEx.getMessage().contains("发票"));

        when(jdbcTemplate.queryForObject(
            eq("SELECT COUNT(*) FROM fin_invoice WHERE invoice_direction = 'sale' AND source_order_id = ? AND status IN ('draft', 'issued')"),
            eq(Long.class), eq(40L))).thenReturn(0L);
        when(jdbcTemplate.queryForObject(
            eq("SELECT COUNT(*) FROM wms_sale_return WHERE sale_order_id = ? AND status <> 'cancelled'"),
            eq(Long.class), eq(40L))).thenReturn(1L);

        ServiceException retEx = assertThrows(ServiceException.class,
            () -> wmsSaleOrderService.financeRollbackWmsSaleOrder(40L, "测试回退"));
        assertTrue(retEx.getMessage().contains("退货"));
    }

    @Test
    void submitInboundBlocksIncompleteBinOrBatch()
    {
        WmsInbound draft = new WmsInbound();
        draft.setInboundId(50L);
        draft.setStatus("draft");

        WmsInboundItem missingBatch = new WmsInboundItem();
        missingBatch.setProductId(1L);
        missingBatch.setLocationId(1L);
        missingBatch.setBatchNo("");
        missingBatch.setQuantity(new BigDecimal("5"));

        when(wmsInboundMapper.selectWmsInboundById(50L)).thenReturn(draft);
        when(wmsInboundItemMapper.selectWmsInboundItemList(any(WmsInboundItem.class)))
            .thenReturn(Collections.singletonList(missingBatch));

        ServiceException ex = assertThrows(ServiceException.class, () -> wmsInboundService.submitWmsInbound(50L));
        assertTrue(ex.getMessage().contains("库位") || ex.getMessage().contains("批次号"));
    }

    @Test
    void inventoryCheckItemPreservesRealCostWhenMaskedClientSubmitsNullOrZero()
    {
        WmsInventoryCheck draftCheck = new WmsInventoryCheck();
        draftCheck.setCheckId(60L);
        draftCheck.setWarehouseId(1L);
        draftCheck.setStatus("draft");

        WmsInventoryCheckItem dbItem = new WmsInventoryCheckItem();
        dbItem.setCheckItemId(601L);
        dbItem.setCheckId(60L);
        dbItem.setProductId(10L);
        dbItem.setLocationId(1L);
        dbItem.setBatchNo("B01");
        dbItem.setStockQty(new BigDecimal("10"));
        dbItem.setPrice(new BigDecimal("88.50"));

        when(wmsInventoryCheckItemMapper.selectWmsInventoryCheckItemById(601L)).thenReturn(dbItem);
        when(wmsInventoryCheckMapper.selectWmsInventoryCheckById(60L)).thenReturn(draftCheck);
        when(wmsInventoryCheckItemMapper.updateWmsInventoryCheckItem(any(WmsInventoryCheckItem.class))).thenReturn(1);
        when(wmsInventoryCheckItemMapper.selectWmsInventoryCheckItemsByCheckId(60L)).thenReturn(Collections.emptyList());

        WmsInventoryCheckItem maskedPatch = new WmsInventoryCheckItem();
        maskedPatch.setCheckItemId(601L);
        maskedPatch.setProductId(10L);
        maskedPatch.setLocationId(1L);
        maskedPatch.setBatchNo("B01");
        maskedPatch.setStockQty(new BigDecimal("10"));
        maskedPatch.setActualQty(new BigDecimal("12"));
        maskedPatch.setPrice(null);

        wmsInventoryCheckItemService.updateWmsInventoryCheckItem(maskedPatch);
        assertEquals(new BigDecimal("88.50"), maskedPatch.getPrice());
        assertEquals(new BigDecimal("177.00"), maskedPatch.getDiffAmount());

        // New surplus item with masked price falls back to md_product.cost_price
        MdProduct prod = new MdProduct();
        prod.setProductId(10L);
        prod.setCostPrice(new BigDecimal("66.00"));
        when(mdProductMapper.selectMdProductById(10L)).thenReturn(prod);
        when(wmsInventoryCheckItemMapper.insertWmsInventoryCheckItem(any(WmsInventoryCheckItem.class))).thenReturn(1);

        WmsInventoryCheckItem surplus = new WmsInventoryCheckItem();
        surplus.setCheckId(60L);
        surplus.setProductId(10L);
        surplus.setLocationId(1L);
        surplus.setBatchNo("SURPLUS-20261124");
        surplus.setStockQty(BigDecimal.ZERO);
        surplus.setActualQty(BigDecimal.ONE);
        surplus.setPrice(null);

        wmsInventoryCheckItemService.insertWmsInventoryCheckItem(surplus);
        assertEquals(new BigDecimal("66.00"), surplus.getPrice());
        assertEquals(new BigDecimal("66.00"), surplus.getDiffAmount());
    }

    @Test
    void complianceHelperBlocksDisabledOrSoftDeletedMasterData()
    {
        Map<String, Object> disabledRow = new HashMap<>();
        disabledRow.put("status", "1");
        disabledRow.put("del_flag", "0");
        when(jdbcTemplate.queryForMap("SELECT status, del_flag FROM md_product WHERE product_id = ?", 99L))
            .thenReturn(disabledRow);
        ServiceException ex1 = assertThrows(ServiceException.class,
            () -> bizComplianceHelper.assertEntityEnabled("md_product", "product_id", 99L, "商品"));
        assertTrue(ex1.getMessage().contains("停用"));

        Map<String, Object> deletedRow = new HashMap<>();
        deletedRow.put("status", "0");
        deletedRow.put("del_flag", "2");
        when(jdbcTemplate.queryForMap("SELECT status, del_flag FROM md_customer WHERE customer_id = ?", 88L))
            .thenReturn(deletedRow);
        ServiceException ex2 = assertThrows(ServiceException.class,
            () -> bizComplianceHelper.assertEntityEnabled("md_customer", "customer_id", 88L, "客户"));
        assertTrue(ex2.getMessage().contains("删除"));
    }
}
