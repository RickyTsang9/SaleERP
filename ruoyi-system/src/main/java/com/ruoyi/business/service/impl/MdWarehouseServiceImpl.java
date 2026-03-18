package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.MdWarehouse;
import com.ruoyi.business.mapper.MdWarehouseMapper;
import com.ruoyi.business.service.IMdWarehouseService;

@Service
public class MdWarehouseServiceImpl implements IMdWarehouseService
{
    @Autowired
    private MdWarehouseMapper mdWarehouseMapper;

    @Override
    public MdWarehouse selectMdWarehouseById(Long warehouseId)
    {
        return mdWarehouseMapper.selectMdWarehouseById(warehouseId);
    }

    @Override
    public List<MdWarehouse> selectMdWarehouseList(MdWarehouse mdWarehouse)
    {
        return mdWarehouseMapper.selectMdWarehouseList(mdWarehouse);
    }

    @Override
    public int insertMdWarehouse(MdWarehouse mdWarehouse)
    {
        return mdWarehouseMapper.insertMdWarehouse(mdWarehouse);
    }

    @Override
    public int updateMdWarehouse(MdWarehouse mdWarehouse)
    {
        return mdWarehouseMapper.updateMdWarehouse(mdWarehouse);
    }

    @Override
    public int deleteMdWarehouseById(Long warehouseId)
    {
        return mdWarehouseMapper.deleteMdWarehouseById(warehouseId);
    }

    @Override
    public int deleteMdWarehouseByIds(Long[] warehouseIds)
    {
        return mdWarehouseMapper.deleteMdWarehouseByIds(warehouseIds);
    }
}
