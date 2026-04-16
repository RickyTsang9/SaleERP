package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.WmsPurchaseReturn;

public interface IWmsPurchaseReturnService
{
    public WmsPurchaseReturn selectWmsPurchaseReturnById(Long purchaseReturnId);

    public List<WmsPurchaseReturn> selectWmsPurchaseReturnList(WmsPurchaseReturn wmsPurchaseReturn);

    public int insertWmsPurchaseReturn(WmsPurchaseReturn wmsPurchaseReturn);

    public int updateWmsPurchaseReturn(WmsPurchaseReturn wmsPurchaseReturn);

    public int submitWmsPurchaseReturn(Long purchaseReturnId);

    public int auditWmsPurchaseReturn(Long purchaseReturnId);

    public int cancelWmsPurchaseReturn(Long purchaseReturnId);

    public int deleteWmsPurchaseReturnById(Long purchaseReturnId);

    public int deleteWmsPurchaseReturnByIds(Long[] purchaseReturnIds);
}
