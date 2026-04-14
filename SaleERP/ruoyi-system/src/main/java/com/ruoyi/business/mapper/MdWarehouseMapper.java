package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.MdWarehouse;

public interface MdWarehouseMapper
{
    public MdWarehouse selectMdWarehouseById(Long warehouseId);

    public List<MdWarehouse> selectMdWarehouseList(MdWarehouse mdWarehouse);

    public int insertMdWarehouse(MdWarehouse mdWarehouse);

    public int updateMdWarehouse(MdWarehouse mdWarehouse);

    public int selectWarehouseReferenceCount(Long warehouseId);

    public int deleteMdWarehouseById(Long warehouseId);

    public int deleteMdWarehouseByIds(Long[] warehouseIds);
}
