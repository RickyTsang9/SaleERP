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

    public int auditWmsInventoryCheckStatusCas(@org.apache.ibatis.annotations.Param("checkId") Long checkId,
                                               @org.apache.ibatis.annotations.Param("auditBy") String auditBy);

    public int rejectWmsInventoryCheckStatus(@org.apache.ibatis.annotations.Param("checkId") Long checkId,
                                             @org.apache.ibatis.annotations.Param("rejectReason") String rejectReason,
                                             @org.apache.ibatis.annotations.Param("updateBy") String updateBy);

    public int deleteWmsInventoryCheckById(Long checkId);

    public int deleteWmsInventoryCheckByIds(Long[] checkIds);

    public String selectMaxCheckNoByPrefix(String noPrefix);
}
