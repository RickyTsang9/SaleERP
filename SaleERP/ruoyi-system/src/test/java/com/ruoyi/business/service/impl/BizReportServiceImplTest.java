package com.ruoyi.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.business.domain.MdCustomer;
import com.ruoyi.business.mapper.BizReportMapper;
import com.ruoyi.business.mapper.MdCustomerMapper;
import com.ruoyi.business.mapper.MdSupplierMapper;
import com.ruoyi.common.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
class BizReportServiceImplTest
{
    @Mock
    private BizReportMapper bizReportMapper;

    @Mock
    private MdCustomerMapper mdCustomerMapper;

    @Mock
    private MdSupplierMapper mdSupplierMapper;

    @InjectMocks
    private BizReportServiceImpl bizReportService;

    @Test
    void reconciliationStatementRejectsInvalidParams()
    {
        Map<String, Object> badType = new HashMap<>();
        badType.put("reconcileType", "invalid");
        badType.put("targetId", "1");
        assertThrows(ServiceException.class, () -> bizReportService.getReconciliationStatement(badType));

        Map<String, Object> badId = new HashMap<>();
        badId.put("reconcileType", "receivable");
        badId.put("targetId", "abc");
        assertThrows(ServiceException.class, () -> bizReportService.getReconciliationStatement(badId));
    }

    @Test
    void reconciliationStatementAggregatesReceivableAndReceiptTotals()
    {
        MdCustomer customer = new MdCustomer();
        customer.setCustomerId(1L);
        customer.setCustomerCode("CUST-001");
        customer.setCustomerName("上海宏达贸易有限公司");
        when(mdCustomerMapper.selectMdCustomerById(1L)).thenReturn(customer);

        Map<String, Object> bill = new HashMap<>();
        bill.put("amountDue", new BigDecimal("10000"));
        bill.put("amountPaid", new BigDecimal("4000"));
        bill.put("remainAmount", new BigDecimal("6000"));
        bill.put("overdueDays", 12L);
        bill.put("status", "partial");
        when(bizReportMapper.selectCustomerReconciliationBills(anyMap())).thenReturn(Collections.singletonList(bill));

        Map<String, Object> receipt = new HashMap<>();
        receipt.put("amount", new BigDecimal("4000"));
        receipt.put("paymentMethod", "bank_transfer");
        when(bizReportMapper.selectCustomerReconciliationReceipts(anyMap())).thenReturn(Collections.singletonList(receipt));

        Map<String, Object> params = new HashMap<>();
        params.put("reconcileType", "receivable");
        params.put("targetId", "1");

        Map<String, Object> statement = bizReportService.getReconciliationStatement(params);
        assertEquals("客户应收对账单", statement.get("reconcileTypeLabel"));
        assertEquals("上海宏达贸易有限公司", statement.get("targetName"));
        assertEquals(new BigDecimal("10000"), statement.get("totalDueAmount"));
        assertEquals(new BigDecimal("4000"), statement.get("totalPaidAmount"));
        assertEquals(new BigDecimal("6000"), statement.get("totalRemainAmount"));
        assertEquals(new BigDecimal("6000"), statement.get("overdueAmount"));
        assertEquals("部分收款", bill.get("statusLabel"));
        assertEquals("银行转账", receipt.get("paymentMethodLabel"));
    }

    @Test
    void cashFlowForecastComputesNetCashFlowWithDefaultWindow()
    {
        Map<String, Object> dbMap = new HashMap<>();
        dbMap.put("receivableAmount", new BigDecimal("25000"));
        dbMap.put("payableAmount", new BigDecimal("10000"));
        when(bizReportMapper.selectCashFlowForecast(anyMap())).thenReturn(dbMap);

        Map<String, Object> forecast = bizReportService.getCashFlowForecast(null);
        assertEquals(30, forecast.get("forecastDays"));
        assertEquals(new BigDecimal("15000"), forecast.get("netCashFlow"));
    }

    @Test
    void productRankingComputesMarginReturnRateTurnoverAndSorts()
    {
        Map<String, Object> rowA = new HashMap<>();
        rowA.put("productId", 1L);
        rowA.put("productName", "商品A");
        rowA.put("costPrice", new BigDecimal("50"));
        rowA.put("saleQty", new BigDecimal("10"));
        rowA.put("saleAmount", new BigDecimal("1000"));
        rowA.put("returnQty", new BigDecimal("1"));
        rowA.put("returnAmount", new BigDecimal("100"));
        rowA.put("stockQty", new BigDecimal("5"));

        Map<String, Object> rowB = new HashMap<>();
        rowB.put("productId", 2L);
        rowB.put("productName", "商品B");
        rowB.put("costPrice", new BigDecimal("10"));
        rowB.put("saleQty", new BigDecimal("20"));
        rowB.put("saleAmount", new BigDecimal("2000"));
        rowB.put("returnQty", new BigDecimal("0"));
        rowB.put("returnAmount", BigDecimal.ZERO);
        rowB.put("stockQty", new BigDecimal("100"));

        when(bizReportMapper.selectProductRankingList(anyMap())).thenReturn(Arrays.asList(rowA, rowB));

        Map<String, Object> sortByTurnover = new HashMap<>();
        sortByTurnover.put("sortBy", "turnoverRate");
        List<Map<String, Object>> ranked = bizReportService.selectProductRankingList(sortByTurnover);

        assertEquals(2, ranked.size());
        assertEquals(1L, ranked.get(0).get("productId"));
        // Net sale qty = 10 - 1 = 9, stock = 5 => turnoverRate = 9 / 5 = 1.80
        assertEquals(new BigDecimal("1.80"), ranked.get(0).get("turnoverRate"));
        assertEquals(new BigDecimal("50.00"), ranked.get(0).get("grossMarginRate"));
        assertEquals(new BigDecimal("10.00"), ranked.get(0).get("returnRate"));
        assertEquals(new BigDecimal("1800.00"), ranked.get(1).get("grossProfitAmount"));
    }
}
