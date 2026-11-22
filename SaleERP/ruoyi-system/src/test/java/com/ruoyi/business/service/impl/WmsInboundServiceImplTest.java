package com.ruoyi.business.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.business.domain.WmsInbound;
import com.ruoyi.business.mapper.WmsInboundItemMapper;
import com.ruoyi.business.mapper.WmsInboundMapper;
import com.ruoyi.common.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
class WmsInboundServiceImplTest
{
    @Mock
    private WmsInboundMapper wmsInboundMapper;

    @Mock
    private WmsInboundItemMapper wmsInboundItemMapper;

    @InjectMocks
    private WmsInboundServiceImpl wmsInboundService;

    @Test
    void deleteAuditedInboundIsRejected()
    {
        WmsInbound audited = new WmsInbound();
        audited.setInboundId(100L);
        audited.setStatus("audited");
        when(wmsInboundMapper.selectWmsInboundById(100L)).thenReturn(audited);

        assertThrows(ServiceException.class, () -> wmsInboundService.deleteWmsInboundById(100L));
    }

    @Test
    void deleteDraftInboundCleansItemsAndHeader()
    {
        WmsInbound draft = new WmsInbound();
        draft.setInboundId(101L);
        draft.setStatus("draft");
        when(wmsInboundMapper.selectWmsInboundById(101L)).thenReturn(draft);
        when(wmsInboundMapper.deleteWmsInboundById(101L)).thenReturn(1);

        int rows = wmsInboundService.deleteWmsInboundById(101L);
        assertEquals(1, rows);
        verify(wmsInboundItemMapper).deleteWmsInboundItemByInboundIds(new Long[] { 101L });
        verify(wmsInboundMapper).deleteWmsInboundById(101L);
    }
}
