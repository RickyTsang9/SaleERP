package com.ruoyi.business.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.FinInvoice;

public interface FinInvoiceMapper
{
    public FinInvoice selectFinInvoiceById(Long invoiceId);

    public FinInvoice selectFinInvoiceByNo(String invoiceNo);

    public List<FinInvoice> selectFinInvoiceList(FinInvoice finInvoice);

    public Map<String, Object> selectFinInvoiceSummary(FinInvoice finInvoice);

    public BigDecimal selectIssuedAmountBySourceOrder(@Param("invoiceDirection") String invoiceDirection,
                                                      @Param("sourceOrderId") Long sourceOrderId,
                                                      @Param("excludeInvoiceId") Long excludeInvoiceId);

    public BigDecimal selectActiveAmountBySourceOrder(@Param("invoiceDirection") String invoiceDirection,
                                                      @Param("sourceOrderId") Long sourceOrderId,
                                                      @Param("excludeInvoiceId") Long excludeInvoiceId);

    public int insertFinInvoice(FinInvoice finInvoice);

    public int updateFinInvoice(FinInvoice finInvoice);

    public int voidFinInvoice(@Param("invoiceId") Long invoiceId,
                              @Param("voidReason") String voidReason,
                              @Param("updateBy") String updateBy);
}
