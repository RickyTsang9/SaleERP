package com.ruoyi.business.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.mapper.MdSupplierMapper;
import com.ruoyi.business.domain.MdSupplier;
import com.ruoyi.business.service.IMdSupplierService;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 供应商Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
@Service
public class MdSupplierServiceImpl implements IMdSupplierService 
{
    @Autowired
    private MdSupplierMapper mdSupplierMapper;

    /**
     * 查询供应商
     * 
     * @param supplierId 供应商主键
     * @return 供应商
     */
    @Override
    public MdSupplier selectMdSupplierBySupplierId(Long supplierId)
    {
        return mdSupplierMapper.selectMdSupplierBySupplierId(supplierId);
    }

    /**
     * 查询供应商列表
     * 
     * @param mdSupplier 供应商
     * @return 供应商
     */
    @Override
    public List<MdSupplier> selectMdSupplierList(MdSupplier mdSupplier)
    {
        return mdSupplierMapper.selectMdSupplierList(mdSupplier);
    }

    /**
     * 新增供应商
     * 
     * @param mdSupplier 供应商
     * @return 结果
     */
    @Override
    public int insertMdSupplier(MdSupplier mdSupplier)
    {
        mdSupplier.setCreateTime(DateUtils.getNowDate());
        try {
            mdSupplier.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
            // ignore
        }
        return mdSupplierMapper.insertMdSupplier(mdSupplier);
    }

    /**
     * 修改供应商
     * 
     * @param mdSupplier 供应商
     * @return 结果
     */
    @Override
    public int updateMdSupplier(MdSupplier mdSupplier)
    {
        mdSupplier.setUpdateTime(DateUtils.getNowDate());
        try {
            mdSupplier.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
            // ignore
        }
        return mdSupplierMapper.updateMdSupplier(mdSupplier);
    }

    /**
     * 批量删除供应商
     * 
     * @param supplierIds 需要删除的供应商主键
     * @return 结果
     */
    @Override
    public int deleteMdSupplierBySupplierIds(Long[] supplierIds)
    {
        return mdSupplierMapper.deleteMdSupplierBySupplierIds(supplierIds);
    }

    /**
     * 删除供应商信息
     * 
     * @param supplierId 供应商主键
     * @return 结果
     */
    @Override
    public int deleteMdSupplierBySupplierId(Long supplierId)
    {
        return mdSupplierMapper.deleteMdSupplierBySupplierId(supplierId);
    }
}