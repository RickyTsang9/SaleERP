package com.ruoyi.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import com.ruoyi.business.domain.MdProduct;
import com.ruoyi.business.mapper.MdProductMapper;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
class MdProductServiceImplTest
{
    @Mock
    private MdProductMapper mdProductMapper;

    @InjectMocks
    private MdProductServiceImpl mdProductService;

    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void productProfileCalculatesMarginReturnRateAndWarningStatus()
    {
        SysUser adminUser = new SysUser();
        adminUser.setUserId(1L);
        adminUser.setUserName("admin");
        LoginUser loginUser = new LoginUser(1L, 100L, adminUser, Collections.singleton("*:*:*"));
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList())
        );

        MdProduct product = new MdProduct();
        product.setProductId(5L);
        product.setProductName("工业扫码枪");
        product.setCostPrice(new BigDecimal("60.00"));
        product.setSalePrice(new BigDecimal("100.00"));
        when(mdProductMapper.selectMdProductById(5L)).thenReturn(product);

        Map<String, Object> saleSummary = new HashMap<>();
        saleSummary.put("saleOrderCount", 2L);
        saleSummary.put("totalSaleQty", new BigDecimal("20"));
        saleSummary.put("totalSaleAmount", new BigDecimal("2000.00"));
        when(mdProductMapper.selectProductSaleSummary(5L)).thenReturn(saleSummary);

        Map<String, Object> returnSummary = new HashMap<>();
        returnSummary.put("returnCount", 1L);
        returnSummary.put("returnQty", new BigDecimal("2"));
        returnSummary.put("returnAmount", new BigDecimal("200.00"));
        when(mdProductMapper.selectProductReturnSummary(5L)).thenReturn(returnSummary);

        when(mdProductMapper.selectProductPurchaseSummary(5L)).thenReturn(Collections.emptyMap());

        Map<String, Object> stockRow = new HashMap<>();
        stockRow.put("quantity", new BigDecimal("5"));
        stockRow.put("lockedQuantity", new BigDecimal("2"));
        stockRow.put("frozenQuantity", new BigDecimal("0"));
        stockRow.put("warningMinQty", new BigDecimal("10"));
        stockRow.put("warningMaxQty", new BigDecimal("100"));
        when(mdProductMapper.selectProductWarehouseStockList(5L)).thenReturn(Collections.singletonList(stockRow));

        Map<String, Object> profile = mdProductService.selectProductProfile(5L);
        @SuppressWarnings("unchecked")
        Map<String, Object> summary = (Map<String, Object>) profile.get("summary");
        // Net sale qty = 20 - 2 = 18 => cost = 18 * 60 = 1080.00, net sale = 1800.00 => profit = 720.00 (40.00%)
        assertEquals(new BigDecimal("1080.00"), summary.get("estimatedCostAmount"));
        assertEquals(new BigDecimal("720.00"), summary.get("grossProfitAmount"));
        assertEquals(new BigDecimal("40.00"), summary.get("grossMarginRate"));
        assertEquals(new BigDecimal("10.00"), summary.get("returnRate"));
        assertEquals(new BigDecimal("3"), summary.get("totalAvailableQty"));
        assertEquals("低于下限", summary.get("warningStatus"));

        // Without finance/profit permission, cost and profit must be masked
        SecurityContextHolder.clearContext();
        Map<String, Object> maskedProfile = mdProductService.selectProductProfile(5L);
        @SuppressWarnings("unchecked")
        Map<String, Object> maskedSummary = (Map<String, Object>) maskedProfile.get("summary");
        assertNull(maskedSummary.get("estimatedCostAmount"));
        assertNull(maskedSummary.get("grossProfitAmount"));
        assertNull(maskedSummary.get("grossMarginRate"));
    }

    @Test
    void scanByCodeMatchesActiveProductAndRejectsDisabledOrMissing()
    {
        MdProduct activeProduct = new MdProduct();
        activeProduct.setProductId(990011L);
        activeProduct.setProductName("iPhone 15 Pro");
        activeProduct.setBarCode("6901234567890");
        activeProduct.setStatus("0");
        when(mdProductMapper.selectMdProductByScanCode("6901234567890")).thenReturn(activeProduct);

        MdProduct matched = mdProductService.selectMdProductByScanCode(" 6901234567890 ");
        assertEquals(990011L, matched.getProductId());

        MdProduct disabledProduct = new MdProduct();
        disabledProduct.setProductName("停用商品");
        disabledProduct.setStatus("1");
        when(mdProductMapper.selectMdProductByScanCode("DISABLED")).thenReturn(disabledProduct);
        assertThrows(ServiceException.class, () -> mdProductService.selectMdProductByScanCode("DISABLED"));

        when(mdProductMapper.selectMdProductByScanCode("NOT_FOUND")).thenReturn(null);
        assertThrows(ServiceException.class, () -> mdProductService.selectMdProductByScanCode("NOT_FOUND"));
    }
}
