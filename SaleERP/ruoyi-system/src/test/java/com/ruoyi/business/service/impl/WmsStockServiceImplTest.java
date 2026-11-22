package com.ruoyi.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.business.domain.WmsStock;
import com.ruoyi.business.mapper.WmsStockMapper;
import com.ruoyi.business.service.IBizMessageService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.service.ISysNoticeService;

@ExtendWith(MockitoExtension.class)
class WmsStockServiceImplTest
{
    @Mock
    private WmsStockMapper wmsStockMapper;

    @Mock
    private ISysNoticeService sysNoticeService;

    @Mock
    private IBizMessageService bizMessageService;

    @InjectMocks
    private WmsStockServiceImpl wmsStockService;

    @Test
    void manualMutationsRemainBlocked()
    {
        WmsStock stock = new WmsStock();
        assertThrows(ServiceException.class, () -> wmsStockService.insertWmsStock(stock));
        assertThrows(ServiceException.class, () -> wmsStockService.updateWmsStock(stock));
        assertThrows(ServiceException.class, () -> wmsStockService.deleteWmsStockById(1L));
        assertThrows(ServiceException.class, () -> wmsStockService.deleteWmsStockByIds(new Long[] { 1L }));
    }

    @Test
    void updateWarningThresholdRejectsInvalidRange()
    {
        WmsStock existing = new WmsStock();
        existing.setStockId(10L);
        when(wmsStockMapper.selectWmsStockById(10L)).thenReturn(existing);

        WmsStock invalidMin = new WmsStock();
        invalidMin.setStockId(10L);
        invalidMin.setWarningMinQty(new BigDecimal("-1"));
        assertThrows(ServiceException.class, () -> wmsStockService.updateWmsStockWarningThreshold(invalidMin));

        WmsStock invertedRange = new WmsStock();
        invertedRange.setStockId(10L);
        invertedRange.setWarningMinQty(new BigDecimal("50"));
        invertedRange.setWarningMaxQty(new BigDecimal("20"));
        assertThrows(ServiceException.class, () -> wmsStockService.updateWmsStockWarningThreshold(invertedRange));
    }

    @Test
    void updateWarningThresholdOnlyUpdatesThresholds()
    {
        WmsStock existing = new WmsStock();
        existing.setStockId(10L);
        existing.setQuantity(new BigDecimal("100"));
        when(wmsStockMapper.selectWmsStockById(10L)).thenReturn(existing);
        when(wmsStockMapper.updateWmsStockWarningThreshold(any(WmsStock.class))).thenReturn(1);

        WmsStock payload = new WmsStock();
        payload.setStockId(10L);
        payload.setQuantity(new BigDecimal("9999"));
        payload.setWarningMinQty(new BigDecimal("15"));
        payload.setWarningMaxQty(new BigDecimal("200"));
        payload.setUpdateBy("admin");

        int rows = wmsStockService.updateWmsStockWarningThreshold(payload);
        assertEquals(1, rows);

        ArgumentCaptor<WmsStock> captor = ArgumentCaptor.forClass(WmsStock.class);
        verify(wmsStockMapper).updateWmsStockWarningThreshold(captor.capture());
        WmsStock passed = captor.getValue();
        assertEquals(10L, passed.getStockId());
        assertEquals(new BigDecimal("15"), passed.getWarningMinQty());
        assertEquals(new BigDecimal("200"), passed.getWarningMaxQty());
        assertEquals(null, passed.getQuantity());
    }
}
