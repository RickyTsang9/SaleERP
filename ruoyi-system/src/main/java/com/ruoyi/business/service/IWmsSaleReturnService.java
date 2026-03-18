package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.WmsSaleReturn;

public interface IWmsSaleReturnService
{
    public WmsSaleReturn selectWmsSaleReturnById(Long saleReturnId);

    public List<WmsSaleReturn> selectWmsSaleReturnList(WmsSaleReturn wmsSaleReturn);

    public int insertWmsSaleReturn(WmsSaleReturn wmsSaleReturn);

    public int updateWmsSaleReturn(WmsSaleReturn wmsSaleReturn);

    public int submitWmsSaleReturn(Long saleReturnId);

    public int auditWmsSaleReturn(Long saleReturnId);

    public int cancelWmsSaleReturn(Long saleReturnId);

    public int deleteWmsSaleReturnById(Long saleReturnId);

    public int deleteWmsSaleReturnByIds(Long[] saleReturnIds);
}
