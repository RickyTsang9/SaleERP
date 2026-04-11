package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.WmsSaleReturn;

public interface WmsSaleReturnMapper
{
    public WmsSaleReturn selectWmsSaleReturnById(Long saleReturnId);

    public List<WmsSaleReturn> selectWmsSaleReturnList(WmsSaleReturn wmsSaleReturn);

    public int insertWmsSaleReturn(WmsSaleReturn wmsSaleReturn);

    public int updateWmsSaleReturn(WmsSaleReturn wmsSaleReturn);

    public int updateWmsSaleReturnStatus(Long saleReturnId, String currentStatus, String status, String auditBy, String updateBy);

    public int deleteWmsSaleReturnById(Long saleReturnId);

    public int deleteWmsSaleReturnByIds(Long[] saleReturnIds);

    public String selectMaxReturnNoByPrefix(String noPrefix);

    public int refreshTotalBySaleReturnId(Long saleReturnId);
}
