package com.ruoyi.business.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.WmsPurchaseReturn;
import com.ruoyi.business.mapper.WmsPurchaseReturnMapper;
import com.ruoyi.business.service.IWmsPurchaseReturnService;

@Service
public class WmsPurchaseReturnServiceImpl implements IWmsPurchaseReturnService
{
    @Autowired
    private WmsPurchaseReturnMapper wmsPurchaseReturnMapper;

    @Override
    public WmsPurchaseReturn selectWmsPurchaseReturnById(Long purchaseReturnId)
    {
        return wmsPurchaseReturnMapper.selectWmsPurchaseReturnById(purchaseReturnId);
    }

    @Override
    public List<WmsPurchaseReturn> selectWmsPurchaseReturnList(WmsPurchaseReturn wmsPurchaseReturn)
    {
        return wmsPurchaseReturnMapper.selectWmsPurchaseReturnList(wmsPurchaseReturn);
    }

    @Override
    public int insertWmsPurchaseReturn(WmsPurchaseReturn wmsPurchaseReturn)
    {
        if (wmsPurchaseReturn.getReturnNo() == null || wmsPurchaseReturn.getReturnNo().isEmpty())
        {
            wmsPurchaseReturn.setReturnNo(generatePurchaseReturnNo());
        }
        return wmsPurchaseReturnMapper.insertWmsPurchaseReturn(wmsPurchaseReturn);
    }

    @Override
    public int updateWmsPurchaseReturn(WmsPurchaseReturn wmsPurchaseReturn)
    {
        return wmsPurchaseReturnMapper.updateWmsPurchaseReturn(wmsPurchaseReturn);
    }

    @Override
    public int deleteWmsPurchaseReturnById(Long purchaseReturnId)
    {
        return wmsPurchaseReturnMapper.deleteWmsPurchaseReturnById(purchaseReturnId);
    }

    @Override
    public int deleteWmsPurchaseReturnByIds(Long[] purchaseReturnIds)
    {
        return wmsPurchaseReturnMapper.deleteWmsPurchaseReturnByIds(purchaseReturnIds);
    }

    private String generatePurchaseReturnNo()
    {
        String dateValue = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String noPrefix = "CGTH" + dateValue;
        String maxReturnNo = wmsPurchaseReturnMapper.selectMaxReturnNoByPrefix(noPrefix);
        if (maxReturnNo == null || maxReturnNo.isEmpty())
        {
            return noPrefix + "0001";
        }
        String sequenceValue = maxReturnNo.substring(noPrefix.length());
        int nextSequenceValue = Integer.parseInt(sequenceValue) + 1;
        return noPrefix + String.format("%04d", nextSequenceValue);
    }
}
