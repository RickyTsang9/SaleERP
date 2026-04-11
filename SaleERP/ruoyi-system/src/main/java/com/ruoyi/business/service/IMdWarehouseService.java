package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.MdWarehouse;

public interface IMdWarehouseService
{
    public MdWarehouse selectMdWarehouseById(Long warehouseId);

    public List<MdWarehouse> selectMdWarehouseList(MdWarehouse mdWarehouse);

    public int insertMdWarehouse(MdWarehouse mdWarehouse);

    public int updateMdWarehouse(MdWarehouse mdWarehouse);

    public int deleteMdWarehouseById(Long warehouseId);

    public int deleteMdWarehouseByIds(Long[] warehouseIds);
}
