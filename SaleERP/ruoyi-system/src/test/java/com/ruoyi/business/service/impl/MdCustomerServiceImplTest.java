package com.ruoyi.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.business.domain.MdCustomer;
import com.ruoyi.business.mapper.MdCustomerMapper;
import com.ruoyi.common.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
class MdCustomerServiceImplTest
{
    @Mock
    private MdCustomerMapper mdCustomerMapper;

    @InjectMocks
    private MdCustomerServiceImpl mdCustomerService;

    @Test
    void customerProfileDetectsHighRiskOnOverdueReceivable()
    {
        MdCustomer customer = new MdCustomer();
        customer.setCustomerId(1L);
        customer.setCustomerName("上海宏达贸易有限公司");
        customer.setCreditLimit(new BigDecimal("50000"));
        when(mdCustomerMapper.selectMdCustomerById(1L)).thenReturn(customer);

        Map<String, Object> orderSummary = new HashMap<>();
        orderSummary.put("totalOrderCount", 4L);
        orderSummary.put("auditedOrderCount", 3L);
        orderSummary.put("totalSaleAmount", new BigDecimal("30000"));
        when(mdCustomerMapper.selectCustomerOrderSummary(1L)).thenReturn(orderSummary);

        Map<String, Object> recSummary = new HashMap<>();
        recSummary.put("totalReceivableAmount", new BigDecimal("30000"));
        recSummary.put("totalReceivedAmount", new BigDecimal("18000"));
        recSummary.put("remainReceivableAmount", new BigDecimal("12000"));
        recSummary.put("overdueAmount", new BigDecimal("5000"));
        recSummary.put("overdueCount", 1L);
        when(mdCustomerMapper.selectCustomerReceivableSummary(1L)).thenReturn(recSummary);
        when(mdCustomerMapper.selectCustomerRecentOrders(1L)).thenReturn(Collections.emptyList());
        when(mdCustomerMapper.selectCustomerRecentFollows(1L)).thenReturn(Collections.emptyList());
        when(mdCustomerMapper.selectCustomerTopProducts(1L)).thenReturn(Collections.emptyList());

        Map<String, Object> profile = mdCustomerService.selectCustomerProfile(1L);
        @SuppressWarnings("unchecked")
        Map<String, Object> summary = (Map<String, Object>) profile.get("summary");
        assertEquals("高风险", summary.get("riskLevel"));
        assertEquals(new BigDecimal("60.00"), summary.get("collectionRate"));
        assertEquals(new BigDecimal("24.00"), summary.get("creditUsageRate"));
    }

    @Test
    void customerProfileMarksNormalWhenFullyCollected()
    {
        MdCustomer customer = new MdCustomer();
        customer.setCustomerId(2L);
        customer.setCustomerName("杭州极速科技");
        customer.setCreditLimit(new BigDecimal("100000"));
        when(mdCustomerMapper.selectMdCustomerById(2L)).thenReturn(customer);
        when(mdCustomerMapper.selectCustomerOrderSummary(2L)).thenReturn(Collections.emptyMap());
        when(mdCustomerMapper.selectCustomerReceivableSummary(2L)).thenReturn(Collections.emptyMap());
        when(mdCustomerMapper.selectCustomerRecentOrders(2L)).thenReturn(Collections.emptyList());
        when(mdCustomerMapper.selectCustomerRecentFollows(2L)).thenReturn(Collections.emptyList());
        when(mdCustomerMapper.selectCustomerTopProducts(2L)).thenReturn(Collections.emptyList());

        Map<String, Object> profile = mdCustomerService.selectCustomerProfile(2L);
        @SuppressWarnings("unchecked")
        Map<String, Object> summary = (Map<String, Object>) profile.get("summary");
        assertEquals("正常", summary.get("riskLevel"));
        assertEquals(new BigDecimal("100.00"), summary.get("collectionRate"));
    }

    @Test
    void deleteReferencedCustomerThrowsException()
    {
        MdCustomer customer = new MdCustomer();
        customer.setCustomerId(1L);
        customer.setCustomerName("上海宏达贸易有限公司");
        when(mdCustomerMapper.selectMdCustomerById(1L)).thenReturn(customer);
        when(mdCustomerMapper.selectCustomerReferenceCount(1L)).thenReturn(3);

        assertThrows(ServiceException.class, () -> mdCustomerService.deleteMdCustomerById(1L));
    }
}
