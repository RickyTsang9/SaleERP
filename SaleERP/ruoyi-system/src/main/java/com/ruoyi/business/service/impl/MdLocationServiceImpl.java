package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.MdLocation;
import com.ruoyi.business.mapper.MdLocationMapper;
import com.ruoyi.business.service.IMdLocationService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class MdLocationServiceImpl implements IMdLocationService
{
    @Autowired
    private MdLocationMapper mdLocationMapper;

    @Autowired(required = false)
    private com.ruoyi.business.service.support.BizComplianceHelper complianceHelper;

    @Override
    public MdLocation selectMdLocationById(Long locationId)
    {
        return mdLocationMapper.selectMdLocationById(locationId);
    }

    @Override
    public List<MdLocation> selectMdLocationList(MdLocation mdLocation)
    {
        return mdLocationMapper.selectMdLocationList(mdLocation);
    }

    @Override
    public int insertMdLocation(MdLocation mdLocation)
    {
        return mdLocationMapper.insertMdLocation(mdLocation);
    }

    @Override
    public int updateMdLocation(MdLocation mdLocation)
    {
        if (mdLocation != null && "1".equals(mdLocation.getStatus()) && mdLocation.getLocationId() != null && complianceHelper != null)
        {
            java.math.BigDecimal remainStock = complianceHelper.queryBigDecimal(
                "SELECT IFNULL(SUM(IFNULL(quantity, 0)), 0) FROM wms_stock WHERE location_id = ?",
                mdLocation.getLocationId()
            );
            if (remainStock.compareTo(java.math.BigDecimal.ZERO) > 0)
            {
                throw new ServiceException("该库位当前仍有在库库存（" + remainStock + "），禁止停用");
            }
        }
        return mdLocationMapper.updateMdLocation(mdLocation);
    }

    @Override
    public int deleteMdLocationById(Long locationId)
    {
        validateLocationCanBeDeleted(locationId, false);
        return mdLocationMapper.deleteMdLocationById(locationId);
    }

    @Override
    public int deleteMdLocationByIds(Long[] locationIds)
    {
        for (Long locationId : locationIds)
        {
            validateLocationCanBeDeleted(locationId, true);
        }
        return mdLocationMapper.deleteMdLocationByIds(locationIds);
    }

    @Override
    public String importMdLocation(List<MdLocation> locationList, boolean updateSupport, String operName)
    {
        if (locationList == null || locationList.isEmpty())
        {
            throw new ServiceException("导入库位数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder failureMsg = new StringBuilder();
        for (MdLocation item : locationList)
        {
            try
            {
                if (item.getWarehouseId() == null || StringUtils.isEmpty(item.getLocationCode()))
                {
                    failureNum++;
                    failureMsg.append("<br/>仓库编号或库位编码为空，已跳过");
                    continue;
                }
                if (StringUtils.isEmpty(item.getLocationName()))
                {
                    item.setLocationName(item.getLocationCode());
                }
                if (StringUtils.isEmpty(item.getStatus()))
                {
                    item.setStatus("0");
                }
                MdLocation query = new MdLocation();
                query.setWarehouseId(item.getWarehouseId());
                query.setLocationCode(item.getLocationCode().trim());
                List<MdLocation> existList = mdLocationMapper.selectMdLocationList(query);
                MdLocation exist = (existList != null && !existList.isEmpty()) ? existList.get(0) : null;
                if (exist == null)
                {
                    item.setCreateBy(operName);
                    mdLocationMapper.insertMdLocation(item);
                    successNum++;
                }
                else if (updateSupport)
                {
                    item.setLocationId(exist.getLocationId());
                    item.setUpdateBy(operName);
                    mdLocationMapper.updateMdLocation(item);
                    successNum++;
                }
                else
                {
                    failureNum++;
                    failureMsg.append("<br/>库位 " + item.getLocationCode() + " 已存在");
                }
            }
            catch (Exception e)
            {
                failureNum++;
                failureMsg.append("<br/>库位 " + item.getLocationCode() + " 导入失败：" + e.getMessage());
            }
        }
        if (failureNum > 0 && successNum == 0)
        {
            throw new ServiceException("导入失败！共 " + failureNum + " 条异常：" + failureMsg);
        }
        return "成功导入 " + successNum + " 条库位资料" + (failureNum > 0 ? "（跳过/失败 " + failureNum + " 条）" : "");
    }

    /**
     * 校验库位是否允许删除
     *
     * @param locationId 库位编号
     * @param batchDelete 是否批量删除
     */
    private void validateLocationCanBeDeleted(Long locationId, boolean batchDelete)
    {
        if (locationId == null)
        {
            return;
        }
        MdLocation databaseLocation = mdLocationMapper.selectMdLocationById(locationId);
        if (databaseLocation == null)
        {
            return;
        }
        Integer referenceCount = mdLocationMapper.selectLocationReferenceCount(locationId);
        if (referenceCount != null && referenceCount.intValue() > 0)
        {
            if (batchDelete)
            {
                throw new ServiceException("库位已被业务单据或库存引用，不能删除，库位名称：" + databaseLocation.getLocationName());
            }
            throw new ServiceException("库位已被业务单据或库存引用，不能删除");
        }
    }
}
