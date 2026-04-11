package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.FinReceipt;

public interface IFinReceiptService
{
    public FinReceipt selectFinReceiptById(Long receiptId);

    public List<FinReceipt> selectFinReceiptList(FinReceipt finReceipt);

    public int insertFinReceipt(FinReceipt finReceipt);

    public int updateFinReceipt(FinReceipt finReceipt);

    public int deleteFinReceiptById(Long receiptId);

    public int deleteFinReceiptByIds(Long[] receiptIds);
}
