package com.ruoyi.business.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.ruoyi.business.domain.FinReceipt;

public interface IFinReceiptService
{
    public FinReceipt selectFinReceiptById(Long receiptId);

    public List<FinReceipt> selectFinReceiptList(FinReceipt finReceipt);

    public int insertFinReceipt(FinReceipt finReceipt);

    /**
     * 多单合并回款核销（按所选应收台账顺序依次核销）
     */
    public int batchInsertFinReceipt(List<Long> receivableIds, BigDecimal totalAmount, String paymentMethod,
                                     Long accountId, Date paymentTime, String remark);

    public int voidFinReceipt(Long receiptId, String voidReason);

    public int updateFinReceipt(FinReceipt finReceipt);

    public int deleteFinReceiptById(Long receiptId);

    public int deleteFinReceiptByIds(Long[] receiptIds);
}
