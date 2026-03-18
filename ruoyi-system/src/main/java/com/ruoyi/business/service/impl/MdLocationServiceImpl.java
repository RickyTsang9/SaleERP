package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.MdLocation;
import com.ruoyi.business.mapper.MdLocationMapper;
import com.ruoyi.business.service.IMdLocationService;

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
        return mdLocationMapper.deleteMdLocationById(locationId);
    }

    @Override
    public int deleteMdLocationByIds(Long[] locationIds)
    {
        return mdLocationMapper.deleteMdLocationByIds(locationIds);
    }
}
