package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.WmsPurchaseReturn;

public interface WmsPurchaseReturnMapper
{
    public WmsPurchaseReturn selectWmsPurchaseReturnById(Long purchaseReturnId);

    public List<WmsPurchaseReturn> selectWmsPurchaseReturnList(WmsPurchaseReturn wmsPurchaseReturn);

    public int insertWmsPurchaseReturn(WmsPurchaseReturn wmsPurchaseReturn);

    public int updateWmsPurchaseReturn(WmsPurchaseReturn wmsPurchaseReturn);

    public int deleteWmsPurchaseReturnById(Long purchaseReturnId);

    public int deleteWmsPurchaseReturnByIds(Long[] purchaseReturnIds);

    public String selectMaxReturnNoByPrefix(String noPrefix);

    public int refreshTotalByPurchaseReturnId(Long purchaseReturnId);
}
