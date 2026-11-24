package com.ruoyi.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import com.ruoyi.business.domain.FinReceipt;
import com.ruoyi.business.domain.FinReceivable;
import com.ruoyi.business.domain.MdCustomer;
import com.ruoyi.business.domain.MdProduct;
import com.ruoyi.business.domain.WmsInbound;
import com.ruoyi.business.domain.WmsInboundItem;
import com.ruoyi.business.domain.WmsOutbound;
import com.ruoyi.business.domain.WmsOutboundItem;
import com.ruoyi.business.domain.WmsPurchaseOrder;
import com.ruoyi.business.domain.WmsSaleOrder;
import com.ruoyi.business.domain.WmsSaleOrderItem;
import com.ruoyi.business.domain.WmsStockLog;
import com.ruoyi.business.mapper.FinReceiptMapper;
import com.ruoyi.business.mapper.FinReceivableMapper;
import com.ruoyi.business.mapper.MdCustomerMapper;
import com.ruoyi.business.mapper.MdProductMapper;
import com.ruoyi.business.mapper.WmsInboundItemMapper;
import com.ruoyi.business.mapper.WmsInboundMapper;
import com.ruoyi.business.mapper.WmsOutboundItemMapper;
import com.ruoyi.business.mapper.WmsOutboundMapper;
import com.ruoyi.business.mapper.WmsPurchaseOrderMapper;
import com.ruoyi.business.mapper.WmsSaleOrderItemMapper;
import com.ruoyi.business.mapper.WmsSaleOrderMapper;
import com.ruoyi.business.mapper.WmsSaleOrderStatusHistoryMapper;
import com.ruoyi.business.mapper.WmsStockLogMapper;
import com.ruoyi.business.mapper.WmsStockMapper;
import com.ruoyi.business.service.IBizMessageService;
import com.ruoyi.business.service.IWmsStockService;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;

/**
 * SaleERP 优化路线图 (OPT-01 ~ OPT-23) 核心领域逻辑单元测试
 */
@ExtendWith(MockitoExtension.class)
class OptimizationRoadmapTest
{
    @Mock
    private WmsOutboundMapper wmsOutboundMapper;

    @Mock
    private WmsOutboundItemMapper wmsOutboundItemMapper;

    @Mock
    private WmsInboundMapper wmsInboundMapper;

    @Mock
    private WmsInboundItemMapper wmsInboundItemMapper;

    @Mock
    private WmsSaleOrderMapper wmsSaleOrderMapper;

    @Mock
    private WmsSaleOrderItemMapper wmsSaleOrderItemMapper;

    @Mock
    private WmsSaleOrderStatusHistoryMapper wmsSaleOrderStatusHistoryMapper;

    @Mock
    private WmsPurchaseOrderMapper wmsPurchaseOrderMapper;

    @Mock
    private WmsStockMapper wmsStockMapper;

    @Mock
    private WmsStockLogMapper wmsStockLogMapper;

    @Mock
    private IWmsStockService wmsStockService;

    @Mock
    private MdProductMapper mdProductMapper;

    @Mock
    private MdCustomerMapper mdCustomerMapper;

    @Mock
    private FinReceivableMapper finReceivableMapper;

    @Mock
    private FinReceiptMapper finReceiptMapper;

    @Mock
    private IBizMessageService bizMessageService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private WmsOutboundServiceImpl wmsOutboundService;

    @InjectMocks
    private WmsInboundServiceImpl wmsInboundService;

    @InjectMocks
    private WmsSaleOrderServiceImpl wmsSaleOrderService;

    @InjectMocks
    private WmsPurchaseOrderServiceImpl wmsPurchaseOrderService;

    @InjectMocks
    private FinReceivableServiceImpl finReceivableService;

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
    void outboundAuditWritesCostSnapshotAndBillNoToStockLogInsteadOfSellingPrice()
    {
        mockLoginUser("auditor01");

        WmsOutbound outbound = new WmsOutbound();
        outbound.setOutboundId(100L);
        outbound.setOutboundNo("OB20261124001");
        outbound.setWarehouseId(1L);
        outbound.setStatus("submitted");
        outbound.setCreateBy("warehouse01");

        WmsOutboundItem item = new WmsOutboundItem();
        item.setOutboundItemId(10L);
        item.setOutboundId(100L);
        item.setProductId(5L);
        item.setLocationId(2L);
        item.setBatchNo("BATCH-01");
        item.setQuantity(new BigDecimal("3"));
        item.setPrice(new BigDecimal("120.00"));
        item.setAmount(new BigDecimal("360.00"));

        MdProduct product = new MdProduct();
        product.setProductId(5L);
        product.setCostPrice(new BigDecimal("65.5000"));

        when(wmsOutboundMapper.selectWmsOutboundById(100L)).thenReturn(outbound);
        when(wmsOutboundItemMapper.selectWmsOutboundItemList(any())).thenReturn(Collections.singletonList(item));
        when(mdProductMapper.selectMdProductById(5L)).thenReturn(product);
        when(wmsStockService.deductAvailableStockByKeyAtomically(
            eq(1L), eq(5L), eq(2L), eq("BATCH-01"), eq(new BigDecimal("3")), eq("auditor01"), eq(true)))
            .thenReturn(new BigDecimal[] { new BigDecimal("10"), new BigDecimal("7") });
        when(wmsOutboundMapper.updateWmsOutboundStatus(100L, "submitted", "audited", "auditor01", "auditor01"))
            .thenReturn(1);

        int rows = wmsOutboundService.auditWmsOutbound(100L);
        assertEquals(1, rows);

        ArgumentCaptor<WmsStockLog> logCaptor = ArgumentCaptor.forClass(WmsStockLog.class);
        verify(wmsStockLogMapper).insertWmsStockLog(logCaptor.capture());
        WmsStockLog savedLog = logCaptor.getValue();
        assertEquals("OB20261124001", savedLog.getBillNo());
        assertEquals(0, new BigDecimal("65.50").compareTo(savedLog.getPrice()));
        assertEquals(0, new BigDecimal("196.50").compareTo(savedLog.getAmount()));
    }

    @Test
    void inboundAuditDilutesZeroPriceBonusAndRetainsFourDecimalPrecision()
    {
        mockLoginUser("auditor01");

        WmsInbound inbound = new WmsInbound();
        inbound.setInboundId(200L);
        inbound.setInboundNo("IB20261124001");
        inbound.setWarehouseId(1L);
        inbound.setStatus("submitted");

        // 明细1：旧库存 80 件 @ 100.00 + 零价赠品 20 件 @ 0.00 => 新加权成本应摊薄为 80.00
        WmsInboundItem bonusItem = new WmsInboundItem();
        bonusItem.setInboundItemId(21L);
        bonusItem.setProductId(6L);
        bonusItem.setLocationId(2L);
        bonusItem.setBatchNo("BONUS-01");
        bonusItem.setQuantity(new BigDecimal("20"));
        bonusItem.setPrice(BigDecimal.ZERO);
        bonusItem.setAmount(BigDecimal.ZERO);

        // 明细2：旧库存 2 件 @ 5.0000 + 零价赠品 1 件 @ 0.00 => 10 / 3 = 3.3333（保留4位小数）
        WmsInboundItem precisionItem = new WmsInboundItem();
        precisionItem.setInboundItemId(22L);
        precisionItem.setProductId(7L);
        precisionItem.setLocationId(2L);
        precisionItem.setBatchNo("PREC-01");
        precisionItem.setQuantity(new BigDecimal("1"));
        precisionItem.setPrice(BigDecimal.ZERO);
        precisionItem.setAmount(BigDecimal.ZERO);

        MdProduct prod6 = new MdProduct();
        prod6.setProductId(6L);
        prod6.setCostPrice(new BigDecimal("100.0000"));

        MdProduct prod7 = new MdProduct();
        prod7.setProductId(7L);
        prod7.setCostPrice(new BigDecimal("5.0000"));

        when(wmsInboundMapper.selectWmsInboundById(200L)).thenReturn(inbound);
        when(wmsInboundItemMapper.selectWmsInboundItemList(any())).thenReturn(Arrays.asList(bonusItem, precisionItem));
        when(mdProductMapper.selectMdProductById(6L)).thenReturn(prod6);
        when(mdProductMapper.selectMdProductById(7L)).thenReturn(prod7);
        when(wmsStockMapper.selectTotalProductStockQuantity(6L)).thenReturn(new BigDecimal("80"));
        when(wmsStockMapper.selectTotalProductStockQuantity(7L)).thenReturn(new BigDecimal("2"));
        when(wmsStockService.increaseStockByKeyAtomically(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(new BigDecimal[] { new BigDecimal("80"), new BigDecimal("100") });
        when(wmsInboundMapper.updateWmsInboundStatus(200L, "submitted", "audited", "auditor01", "auditor01"))
            .thenReturn(1);

        wmsInboundService.auditWmsInbound(200L);

        ArgumentCaptor<MdProduct> prodCaptor = ArgumentCaptor.forClass(MdProduct.class);
        verify(mdProductMapper, times(2)).updateMdProduct(prodCaptor.capture());
        List<MdProduct> updates = prodCaptor.getAllValues();

        assertEquals(new BigDecimal("80.00"), updates.get(0).getCostPrice());
        assertEquals(new BigDecimal("3.3333"), updates.get(1).getCostPrice());
    }

    @Test
    void financeAuditEnforcesPositiveCreditLimitBlocksOverdueUnlessOverriddenAndAppliesCreditDays()
    {
        mockLoginUser("finance01");

        WmsSaleOrder saleOrder = new WmsSaleOrder();
        saleOrder.setSaleOrderId(300L);
        saleOrder.setOrderNo("SO20261124001");
        saleOrder.setCustomerId(8L);
        saleOrder.setWarehouseId(1L);
        saleOrder.setStatus("manager_approved");
        saleOrder.setTotalAmount(new BigDecimal("500.00"));
        saleOrder.setCreateBy("sales01");

        WmsSaleOrderItem soItem = new WmsSaleOrderItem();
        soItem.setSaleOrderItemId(31L);
        soItem.setProductId(5L);
        soItem.setLocationId(2L);
        soItem.setBatchNo("B001");
        soItem.setQuantity(new BigDecimal("5"));

        // 即使结算方式为现结(cash)，只要配置了正信用额度也必须受控
        MdCustomer customer = new MdCustomer();
        customer.setCustomerId(8L);
        customer.setCustomerName("华东测试客户");
        customer.setPaymentMethod("cash");
        customer.setCreditLimit(new BigDecimal("10000.00"));
        customer.setCreditDays(45);

        when(wmsSaleOrderMapper.selectWmsSaleOrderById(300L)).thenReturn(saleOrder);
        when(wmsSaleOrderItemMapper.selectWmsSaleOrderItemsByOrderId(300L)).thenReturn(Collections.singletonList(soItem));
        when(mdCustomerMapper.selectMdCustomerById(8L)).thenReturn(customer);

        // 场景1：现结客户正额度超限被拦截
        when(finReceivableMapper.selectCustomerOutstandingAmount(8L)).thenReturn(new BigDecimal("9800.00"));
        when(wmsSaleOrderMapper.selectCustomerPendingApprovalAmount(8L)).thenReturn(new BigDecimal("500.00"));
        ServiceException creditEx = assertThrows(ServiceException.class,
            () -> wmsSaleOrderService.financeAuditWmsSaleOrder(300L, "常规审核"));
        assertTrue(creditEx.getMessage().contains("信用额度不足"));

        // 场景2：额度充足但存在逾期应收，常规审核被风控拦截
        when(finReceivableMapper.selectCustomerOutstandingAmount(8L)).thenReturn(new BigDecimal("1200.00"));
        Map<String, Object> overdueMap = new HashMap<>();
        overdueMap.put("cnt", 2L);
        overdueMap.put("amt", new BigDecimal("1200.00"));
        when(jdbcTemplate.queryForMap(anyString(), eq(8L))).thenReturn(overdueMap);

        ServiceException overdueEx = assertThrows(ServiceException.class,
            () -> wmsSaleOrderService.financeAuditWmsSaleOrder(300L, "常规审核"));
        assertTrue(overdueEx.getMessage().contains("逾期未结清应收款"));
        assertTrue(overdueEx.getMessage().contains("特批放行"));

        // 场景3：财务主管填写【特批放行】原因后放行，并按客户 creditDays=45 生成应收账期
        when(wmsSaleOrderMapper.updateWmsSaleOrderStatus(
            300L, "manager_approved", "audited", "finance01", "特批放行：月底对账结清", "finance01")).thenReturn(1);
        when(finReceivableMapper.selectFinReceivableBySaleOrderId(300L)).thenReturn(null);

        int rows = wmsSaleOrderService.financeAuditWmsSaleOrder(300L, "特批放行：月底对账结清");
        assertEquals(1, rows);

        ArgumentCaptor<FinReceivable> arCaptor = ArgumentCaptor.forClass(FinReceivable.class);
        verify(finReceivableMapper).insertFinReceivable(arCaptor.capture());
        FinReceivable createdAr = arCaptor.getValue();
        assertNotNull(createdAr.getDueDate());
        long diffDays = TimeUnit.MILLISECONDS.toDays(createdAr.getDueDate().getTime() - System.currentTimeMillis());
        assertTrue(diffDays >= 44 && diffDays <= 46, "应收账期应按 creditDays=45 计算，实际天数差: " + diffDays);
    }

    @Test
    void smartPurchaseSuggestPriceAndReplenishOrdersGroupBySupplier()
    {
        mockLoginUser("buyer01");

        when(jdbcTemplate.queryForList(anyString(), eq(BigDecimal.class), eq(11L), eq(5L)))
            .thenReturn(Collections.singletonList(new BigDecimal("48.50")));
        when(jdbcTemplate.queryForList(anyString(), eq(BigDecimal.class), eq(5L)))
            .thenReturn(Collections.singletonList(new BigDecimal("49.00")));

        Map<String, Object> priceInfo = wmsPurchaseOrderService.suggestPurchasePrice(11L, 5L);
        assertEquals(0, new BigDecimal("48.50").compareTo((BigDecimal) priceInfo.get("suggestedPrice")));
        assertEquals("该供应商最近成交价", priceInfo.get("priceSource"));

        when(jdbcTemplate.queryForList(anyString(), eq(Long.class)))
            .thenReturn(Collections.singletonList(99L));
        when(jdbcTemplate.queryForList(anyString(), eq(BigDecimal.class), eq(12L), eq(6L)))
            .thenReturn(Collections.emptyList());
        when(jdbcTemplate.queryForList(anyString(), eq(BigDecimal.class), eq(6L)))
            .thenReturn(Collections.singletonList(new BigDecimal("32.00")));
        when(wmsPurchaseOrderMapper.insertWmsPurchaseOrder(any(WmsPurchaseOrder.class))).thenAnswer(inv -> {
            WmsPurchaseOrder po = inv.getArgument(0);
            po.setPurchaseOrderId(po.getSupplierId() + 1000L);
            return 1;
        });

        Map<String, Object> row1 = new HashMap<>();
        row1.put("productId", 5L);
        row1.put("supplierId", 11L);
        row1.put("quantity", "20");

        Map<String, Object> row2 = new HashMap<>();
        row2.put("productId", 6L);
        row2.put("supplierId", 12L);
        row2.put("quantity", "15");

        Map<String, Object> summary = wmsPurchaseOrderService.createReplenishPurchaseOrders(
            null, Arrays.asList(row1, row2), "buyer01");
        assertEquals(2, summary.get("createdCount"));
        assertEquals(2, ((List<?>) summary.get("orderNos")).size());
        verify(wmsPurchaseOrderMapper, times(2)).insertWmsPurchaseOrder(any(WmsPurchaseOrder.class));
    }

    @Test
    void receivableRedInkOffsetLocksAscendingAndWritesPairedOffsetReceipts()
    {
        FinReceivable negAr = new FinReceivable();
        negAr.setReceivableId(40L);
        negAr.setSaleOrderId(301L);
        negAr.setCustomerId(8L);
        negAr.setAmountDue(new BigDecimal("-300.00"));
        negAr.setAmountPaid(BigDecimal.ZERO);
        negAr.setStatus("unpaid");

        FinReceivable posAr = new FinReceivable();
        posAr.setReceivableId(90L);
        posAr.setSaleOrderId(300L);
        posAr.setCustomerId(8L);
        posAr.setAmountDue(new BigDecimal("1000.00"));
        posAr.setAmountPaid(new BigDecimal("200.00"));
        posAr.setStatus("partial");

        when(finReceivableMapper.selectFinReceivableByIdForUpdate(40L)).thenReturn(negAr);
        when(finReceivableMapper.selectFinReceivableByIdForUpdate(90L)).thenReturn(posAr);

        // 传入 posId=90, negId=40，验证内部按主键升序 40 -> 90 加锁防死锁
        BigDecimal offsetAmt = finReceivableService.offsetReceivable(90L, 40L, "销售退货红冲对冲", "finance01");
        assertEquals(new BigDecimal("300.00"), offsetAmt);

        InOrder lockOrder = inOrder(finReceivableMapper);
        lockOrder.verify(finReceivableMapper).selectFinReceivableByIdForUpdate(40L);
        lockOrder.verify(finReceivableMapper).selectFinReceivableByIdForUpdate(90L);

        ArgumentCaptor<FinReceipt> recCaptor = ArgumentCaptor.forClass(FinReceipt.class);
        verify(finReceiptMapper, times(2)).insertFinReceipt(recCaptor.capture());
        List<FinReceipt> receipts = recCaptor.getAllValues();
        assertEquals("offset", receipts.get(0).getPaymentMethod());
        assertEquals(new BigDecimal("300.00"), receipts.get(0).getAmount());
        assertEquals("offset", receipts.get(1).getPaymentMethod());
        assertEquals(new BigDecimal("-300.00"), receipts.get(1).getAmount());

        verify(finReceivableMapper).updateFinReceivablePaidAmount(90L, new BigDecimal("500.00"), "partial");
        verify(finReceivableMapper).updateFinReceivablePaidAmount(40L, new BigDecimal("-300.00"), "paid");
        verify(wmsSaleOrderMapper).updateWmsSaleOrderPaymentStatus(300L, "partial");
    }
}
