package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.WmsInventoryCheck;

public interface WmsInventoryCheckMapper
{
    public WmsInventoryCheck selectWmsInventoryCheckById(Long checkId);

    public List<WmsInventoryCheck> selectWmsInventoryCheckList(WmsInventoryCheck wmsInventoryCheck);

    public int insertWmsInventoryCheck(WmsInventoryCheck wmsInventoryCheck);

    public int updateWmsInventoryCheck(WmsInventoryCheck wmsInventoryCheck);

    public int updateWmsInventoryCheckStatus(Long checkId, String status, String auditBy);

    public int deleteWmsInventoryCheckById(Long checkId);

    public int deleteWmsInventoryCheckByIds(Long[] checkIds);

    public String selectMaxCheckNoByPrefix(String noPrefix);
}
