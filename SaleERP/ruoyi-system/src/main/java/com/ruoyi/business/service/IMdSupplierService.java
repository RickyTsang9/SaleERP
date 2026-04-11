package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.MdSupplier;

/**
 * 供应商Service接口
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
public interface IMdSupplierService 
{
    /**
     * 查询供应商
     * 
     * @param supplierId 供应商主键
     * @return 供应商
     */
    public MdSupplier selectMdSupplierBySupplierId(Long supplierId);

    /**
     * 查询供应商列表
     * 
     * @param mdSupplier 供应商
     * @return 供应商集合
     */
    public List<MdSupplier> selectMdSupplierList(MdSupplier mdSupplier);

    /**
     * 新增供应商
     * 
     * @param mdSupplier 供应商
     * @return 结果
     */
    public int insertMdSupplier(MdSupplier mdSupplier);

    /**
     * 修改供应商
     * 
     * @param mdSupplier 供应商
     * @return 结果
     */
    public int updateMdSupplier(MdSupplier mdSupplier);

    /**
     * 批量删除供应商
     * 
     * @param supplierIds 需要删除的供应商主键集合
     * @return 结果
     */
    public int deleteMdSupplierBySupplierIds(Long[] supplierIds);

    /**
     * 删除供应商信息
     * 
     * @param supplierId 供应商主键
     * @return 结果
     */
    public int deleteMdSupplierBySupplierId(Long supplierId);
}