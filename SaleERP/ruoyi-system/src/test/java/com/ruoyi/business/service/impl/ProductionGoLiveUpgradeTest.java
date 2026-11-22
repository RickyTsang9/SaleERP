package com.ruoyi.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.business.domain.FinInvoice;
import com.ruoyi.business.domain.FinReceivable;
import com.ruoyi.business.domain.FinReceipt;
import com.ruoyi.business.domain.MdCustomer;
import com.ruoyi.business.domain.MdProduct;
import com.ruoyi.business.domain.WmsInbound;
import com.ruoyi.business.domain.WmsInboundItem;
import com.ruoyi.business.domain.WmsSaleOrder;
import com.ruoyi.business.domain.WmsStock;
import com.ruoyi.business.mapper.FinInvoiceMapper;
import com.ruoyi.business.mapper.FinReceivableMapper;
import com.ruoyi.business.mapper.FinReceiptMapper;
import com.ruoyi.business.mapper.MdCustomerMapper;
import com.ruoyi.business.mapper.MdProductMapper;
import com.ruoyi.business.mapper.WmsInboundItemMapper;
import com.ruoyi.business.mapper.WmsInboundMapper;
import com.ruoyi.business.mapper.WmsSaleOrderMapper;
import com.ruoyi.business.mapper.WmsStockLogMapper;
import com.ruoyi.business.mapper.WmsStockMapper;
import com.ruoyi.common.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
class ProductionGoLiveUpgradeTest
{
    @Mock
    private WmsStockMapper wmsStockMapper;

    @Mock
    private WmsStockLogMapper wmsStockLogMapper;

    @Mock
    private FinReceiptMapper finReceiptMapper;

    @Mock
    private FinReceivableMapper finReceivableMapper;

    @Mock
    private WmsSaleOrderMapper wmsSaleOrderMapper;

    @Mock
    private WmsInboundMapper wmsInboundMapper;

    @Mock
    private WmsInboundItemMapper wmsInboundItemMapper;

    @Mock
    private MdProductMapper mdProductMapper;

    @Mock
    private FinInvoiceMapper finInvoiceMapper;

    @Mock
    private MdCustomerMapper mdCustomerMapper;

    @InjectMocks
    private WmsStockServiceImpl wmsStockService;

    @InjectMocks
    private FinReceiptServiceImpl finReceiptService;

    @InjectMocks
    private WmsInboundServiceImpl wmsInboundService;

    @InjectMocks
    private FinInvoiceService finInvoiceService;

    @Test
    void optimisticLockRetriesOnVersionConflictAndProtectsLockedStock()
    {
        WmsStock stale = new WmsStock();
        stale.setStockId(88L);
        stale.setQuantity(new BigDecimal("20"));
        stale.setLockedQuantity(new BigDecimal("5"));
        stale.setFrozenQuantity(BigDecimal.ZERO);
        stale.setVersion(1);

        WmsStock fresh = new WmsStock();
        fresh.setStockId(88L);
        fresh.setQuantity(new BigDecimal("20"));
        fresh.setLockedQuantity(new BigDecimal("5"));
        fresh.setFrozenQuantity(BigDecimal.ZERO);
        fresh.setVersion(2);

        when(wmsStockMapper.selectWmsStockByKey(any(WmsStock.class))).thenReturn(stale, fresh);
        when(wmsStockMapper.lockStockAtomically(eq(88L), eq(1), eq(new BigDecimal("10")), any())).thenReturn(0);
        when(wmsStockMapper.lockStockAtomically(eq(88L), eq(2), eq(new BigDecimal("10")), any())).thenReturn(1);

        wmsStockService.lockStockByKeyAtomically(1L, 1L, 1L, "BATCH-01", new BigDecimal("10"), "admin");
        verify(wmsStockMapper, times(2)).selectWmsStockByKey(any(WmsStock.class));

        // Exceeding available stock (20 - 5 = 15 < 16) must throw
        when(wmsStockMapper.selectWmsStockByKey(any(WmsStock.class))).thenReturn(fresh);
        assertThrows(ServiceException.class, () ->
            wmsStockService.lockStockByKeyAtomically(1L, 1L, 1L, "BATCH-01", new BigDecimal("16"), "admin")
        );
    }

    @Test
    void voidFinReceiptRollsBackReceivableAndSaleOrderPaymentStatus()
    {
        FinReceipt receipt = new FinReceipt();
        receipt.setReceiptId(501L);
        receipt.setReceivableId(301L);
        receipt.setAmount(new BigDecimal("5000.00"));
        receipt.setStatus("normal");
        receipt.setPaymentTime(new Date());

        FinReceivable receivable = new FinReceivable();
        receivable.setReceivableId(301L);
        receivable.setSaleOrderId(901L);
        receivable.setAmountDue(new BigDecimal("5000.00"));
        receivable.setAmountPaid(new BigDecimal("5000.00"));
        receivable.setStatus("paid");

        when(finReceiptMapper.selectFinReceiptById(501L)).thenReturn(receipt);
        when(finReceiptMapper.voidFinReceiptStatus(eq(501L), eq("录入错户冲红"), any())).thenReturn(1);
        when(finReceivableMapper.selectFinReceivableById(301L)).thenReturn(receivable);
        when(finReceiptMapper.selectValidReceiptSumByReceivableId(301L)).thenReturn(BigDecimal.ZERO);

        int rows = finReceiptService.voidFinReceipt(501L, "录入错户冲红");
        assertEquals(1, rows);
        verify(finReceivableMapper).updateFinReceivablePaidAmount(301L, BigDecimal.ZERO, "unpaid");
        verify(wmsSaleOrderMapper).updateWmsSaleOrderPaymentStatus(901L, "unpaid");
    }

    @Test
    void auditInboundRecalculatesMovingWeightedAverageCost()
    {
        WmsInbound inbound = new WmsInbound();
        inbound.setInboundId(700L);
        inbound.setInboundNo("RK202611220001");
        inbound.setWarehouseId(1L);
        inbound.setStatus("submitted");

        WmsInboundItem item = new WmsInboundItem();
        item.setInboundId(700L);
        item.setProductId(10L);
        item.setLocationId(1L);
        item.setBatchNo("B202611");
        item.setQuantity(new BigDecimal("10"));
        item.setPrice(new BigDecimal("120.00"));
        item.setAmount(new BigDecimal("1200.00"));

        MdProduct product = new MdProduct();
        product.setProductId(10L);
        product.setCostPrice(new BigDecimal("100.00"));

        when(wmsInboundMapper.selectWmsInboundById(700L)).thenReturn(inbound);
        when(wmsInboundItemMapper.selectWmsInboundItemList(any(WmsInboundItem.class))).thenReturn(Collections.singletonList(item));
        when(mdProductMapper.selectMdProductById(10L)).thenReturn(product);
        // Old stock = 30 @ 100.00 = 3000; New inbound = 10 @ 120.00 = 1200 => (3000 + 1200)/40 = 105.00
        when(wmsStockMapper.selectTotalProductStockQuantity(10L)).thenReturn(new BigDecimal("30"));
        when(wmsInboundMapper.updateWmsInboundStatus(eq(700L), eq("submitted"), eq("audited"), any(), any())).thenReturn(1);

        int updated = wmsInboundService.auditWmsInbound(700L);
        assertEquals(1, updated);

        ArgumentCaptor<MdProduct> productCaptor = ArgumentCaptor.forClass(MdProduct.class);
        verify(mdProductMapper).updateMdProduct(productCaptor.capture());
        assertEquals(new BigDecimal("105.00"), productCaptor.getValue().getCostPrice());
    }

    @Test
    void finInvoiceCalculatesTaxAndBlocksOverInvoicing()
    {
        WmsSaleOrder saleOrder = new WmsSaleOrder();
        saleOrder.setSaleOrderId(1L);
        saleOrder.setOrderNo("SO-001");
        saleOrder.setCustomerId(1L);
        saleOrder.setStatus("audited");
        saleOrder.setTotalAmount(new BigDecimal("11300.00"));

        MdCustomer customer = new MdCustomer();
        customer.setCustomerId(1L);
        customer.setCustomerName("测试客户");

        when(wmsSaleOrderMapper.selectWmsSaleOrderById(1L)).thenReturn(saleOrder);
        when(mdCustomerMapper.selectMdCustomerById(1L)).thenReturn(customer);
        when(finInvoiceMapper.selectActiveAmountBySourceOrder("sale", 1L, null)).thenReturn(BigDecimal.ZERO);
        when(finInvoiceMapper.insertFinInvoice(any(FinInvoice.class))).thenReturn(1);

        FinInvoice invoice = new FinInvoice();
        invoice.setInvoiceNo("INV-20261122-01");
        invoice.setInvoiceDirection("sale");
        invoice.setSourceOrderId(1L);
        invoice.setTaxRate(new BigDecimal("13.00"));
        invoice.setTotalAmount(new BigDecimal("11300.00"));

        finInvoiceService.insertFinInvoice(invoice);
        assertEquals(new BigDecimal("10000.00"), invoice.getAmountWithoutTax());
        assertEquals(new BigDecimal("1300.00"), invoice.getTaxAmount());

        // Second invoice on fully invoiced order must be blocked
        when(finInvoiceMapper.selectActiveAmountBySourceOrder("sale", 1L, null)).thenReturn(new BigDecimal("11300.00"));
        FinInvoice overInvoice = new FinInvoice();
        overInvoice.setInvoiceNo("INV-20261122-02");
        overInvoice.setInvoiceDirection("sale");
        overInvoice.setSourceOrderId(1L);
        overInvoice.setTaxRate(new BigDecimal("13.00"));
        overInvoice.setTotalAmount(new BigDecimal("100.00"));
        assertThrows(ServiceException.class, () -> finInvoiceService.insertFinInvoice(overInvoice));
    }
}
