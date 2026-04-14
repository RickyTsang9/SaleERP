package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.MdWarehouse;
import com.ruoyi.business.mapper.MdWarehouseMapper;
import com.ruoyi.business.service.IMdWarehouseService;
import com.ruoyi.common.exception.ServiceException;

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
        validateWarehouseCanBeDeleted(warehouseId, false);
        return mdWarehouseMapper.deleteMdWarehouseById(warehouseId);
    }

    @Override
    public int deleteMdWarehouseByIds(Long[] warehouseIds)
    {
        for (Long warehouseId : warehouseIds)
        {
            validateWarehouseCanBeDeleted(warehouseId, true);
        }
        return mdWarehouseMapper.deleteMdWarehouseByIds(warehouseIds);
    }

    /**
     * 校验仓库是否允许删除
     *
     * @param warehouseId 仓库编号
     * @param batchDelete 是否批量删除
     */
    private void validateWarehouseCanBeDeleted(Long warehouseId, boolean batchDelete)
    {
        if (warehouseId == null)
        {
            return;
        }
        MdWarehouse databaseWarehouse = mdWarehouseMapper.selectMdWarehouseById(warehouseId);
        if (databaseWarehouse == null)
        {
            return;
        }
        Integer referenceCount = mdWarehouseMapper.selectWarehouseReferenceCount(warehouseId);
        if (referenceCount != null && referenceCount.intValue() > 0)
        {
            if (batchDelete)
            {
                throw new ServiceException("仓库已被业务单据、库存或库位引用，不能删除，仓库名称：" + databaseWarehouse.getWarehouseName());
            }
            throw new ServiceException("仓库已被业务单据、库存或库位引用，不能删除");
        }
    }
}
