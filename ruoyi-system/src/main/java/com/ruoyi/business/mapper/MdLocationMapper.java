package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.MdLocation;

public interface MdLocationMapper
{
    public MdLocation selectMdLocationById(Long locationId);

    public List<MdLocation> selectMdLocationList(MdLocation mdLocation);

    public int insertMdLocation(MdLocation mdLocation);

    public int updateMdLocation(MdLocation mdLocation);

    public int selectLocationReferenceCount(Long locationId);

    public int deleteMdLocationById(Long locationId);

    public int deleteMdLocationByIds(Long[] locationIds);
}
