package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.MdSupplier;

/**
 * 供应商Mapper接口
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
public interface MdSupplierMapper 
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
     * 查询供应商被业务单据引用数量
     *
     * @param supplierId 供应商主键
     * @return 引用数量
     */
    public int selectSupplierReferenceCount(Long supplierId);

    /**
     * 删除供应商
     * 
     * @param supplierId 供应商主键
     * @return 结果
     */
    public int deleteMdSupplierBySupplierId(Long supplierId);

    /**
     * 批量删除供应商
     * 
     * @param supplierIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMdSupplierBySupplierIds(Long[] supplierIds);
}
