package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.MdLocation;
import com.ruoyi.business.mapper.MdLocationMapper;
import com.ruoyi.business.service.IMdLocationService;
import com.ruoyi.common.exception.ServiceException;

@Service
public class MdLocationServiceImpl implements IMdLocationService
{
    @Autowired
    private MdLocationMapper mdLocationMapper;

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
