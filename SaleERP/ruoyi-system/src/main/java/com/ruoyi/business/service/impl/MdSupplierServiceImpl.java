package com.ruoyi.business.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.mapper.MdSupplierMapper;
import com.ruoyi.business.domain.MdSupplier;
import com.ruoyi.business.service.IMdSupplierService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

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

    @Autowired(required = false)
    private com.ruoyi.business.service.support.BizComplianceHelper complianceHelper;

    /**
     * 修改供应商
     * 
     * @param mdSupplier 供应商
     * @return 结果
     */
    @Override
    public int updateMdSupplier(MdSupplier mdSupplier)
    {
        if (mdSupplier != null && "1".equals(mdSupplier.getStatus()) && mdSupplier.getSupplierId() != null && complianceHelper != null)
        {
            Long unpaidCount = complianceHelper.queryLong(
                "SELECT COUNT(1) FROM fin_payable WHERE supplier_id = ? AND status IN ('0', '1')",
                mdSupplier.getSupplierId()
            );
            if (unpaidCount > 0)
            {
                throw new ServiceException("该供应商仍有未结清应付账款（" + unpaidCount + " 笔），禁止停用");
            }
            Long openPoCount = complianceHelper.queryLong(
                "SELECT COUNT(1) FROM wms_purchase_order WHERE supplier_id = ? AND status IN ('draft', '0', '1', '2')",
                mdSupplier.getSupplierId()
            );
            if (openPoCount > 0)
            {
                throw new ServiceException("该供应商仍有未完结采购订单（" + openPoCount + " 笔），禁止停用");
            }
        }
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
        for (Long supplierId : supplierIds)
        {
            validateSupplierCanBeDeleted(supplierId, true);
        }
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
        validateSupplierCanBeDeleted(supplierId, false);
        return mdSupplierMapper.deleteMdSupplierBySupplierId(supplierId);
    }

    @Override
    public String importMdSupplier(List<MdSupplier> supplierList, boolean updateSupport, String operName)
    {
        if (supplierList == null || supplierList.isEmpty())
        {
            throw new ServiceException("导入供应商数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (MdSupplier item : supplierList)
        {
            try
            {
                if (StringUtils.isEmpty(item.getSupplierName()))
                {
                    failureNum++;
                    failureMsg.append("<br/>供应商名称为空，已跳过");
                    continue;
                }
                if (StringUtils.isEmpty(item.getStatus()))
                {
                    item.setStatus("0");
                }
                MdSupplier query = new MdSupplier();
                if (StringUtils.isNotEmpty(item.getSupplierCode()))
                {
                    query.setSupplierCode(item.getSupplierCode().trim());
                }
                else
                {
                    query.setSupplierName(item.getSupplierName().trim());
                }
                List<MdSupplier> existList = mdSupplierMapper.selectMdSupplierList(query);
                MdSupplier exist = (existList != null && !existList.isEmpty()) ? existList.get(0) : null;
                if (exist == null)
                {
                    item.setCreateBy(operName);
                    item.setCreateTime(DateUtils.getNowDate());
                    mdSupplierMapper.insertMdSupplier(item);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、供应商 " + item.getSupplierName() + " 导入成功");
                }
                else if (updateSupport)
                {
                    item.setSupplierId(exist.getSupplierId());
                    item.setUpdateBy(operName);
                    item.setUpdateTime(DateUtils.getNowDate());
                    this.updateMdSupplier(item);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、供应商 " + item.getSupplierName() + " 更新成功");
                }
                else
                {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、供应商 " + item.getSupplierName() + " 已存在");
                }
            }
            catch (Exception e)
            {
                failureNum++;
                failureMsg.append("<br/>" + failureNum + "、供应商 " + item.getSupplierName() + " 导入失败：" + e.getMessage());
            }
        }
        if (failureNum > 0 && successNum == 0)
        {
            throw new ServiceException("很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确：" + failureMsg);
        }
        return "恭喜您，成功导入 " + successNum + " 条供应商记录" + (failureNum > 0 ? "（跳过/失败 " + failureNum + " 条）" : "");
    }

    /**
     * 校验供应商是否允许删除
     * 
     * @param supplierId 供应商主键
     * @param batchDelete 是否批量删除
     */
    private void validateSupplierCanBeDeleted(Long supplierId, boolean batchDelete)
    {
        if (supplierId == null)
        {
            return;
        }
        MdSupplier databaseSupplier = mdSupplierMapper.selectMdSupplierBySupplierId(supplierId);
        if (databaseSupplier == null)
        {
            return;
        }
        Integer referenceCount = mdSupplierMapper.selectSupplierReferenceCount(supplierId);
        if (referenceCount != null && referenceCount.intValue() > 0)
        {
            if (batchDelete)
            {
                throw new ServiceException("供应商已被业务单据引用，不能删除，供应商名称：" + databaseSupplier.getSupplierName());
            }
            throw new ServiceException("供应商已被业务单据引用，不能删除");
        }
    }
}
