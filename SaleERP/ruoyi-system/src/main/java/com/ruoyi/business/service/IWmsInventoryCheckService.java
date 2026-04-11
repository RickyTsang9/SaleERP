package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.WmsInventoryCheck;

public interface IWmsInventoryCheckService
{
    public WmsInventoryCheck selectWmsInventoryCheckById(Long checkId);

    public List<WmsInventoryCheck> selectWmsInventoryCheckList(WmsInventoryCheck wmsInventoryCheck);

    public int insertWmsInventoryCheck(WmsInventoryCheck wmsInventoryCheck);

    public int updateWmsInventoryCheck(WmsInventoryCheck wmsInventoryCheck);

    public int submitWmsInventoryCheck(Long checkId);

    public int auditWmsInventoryCheck(Long checkId);

    public int cancelWmsInventoryCheck(Long checkId);

    public int deleteWmsInventoryCheckById(Long checkId);

    public int deleteWmsInventoryCheckByIds(Long[] checkIds);
}
