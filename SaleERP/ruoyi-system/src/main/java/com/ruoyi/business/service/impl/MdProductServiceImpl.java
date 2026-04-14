package com.ruoyi.business.service.impl;

import java.util.List;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.MdProduct;
import com.ruoyi.business.mapper.MdProductMapper;
import com.ruoyi.business.service.IMdProductService;
import com.ruoyi.common.annotation.DataScope;

@Service
public class MdProductServiceImpl implements IMdProductService
{
    @Autowired
    private MdProductMapper mdProductMapper;

    @Override
    public MdProduct selectMdProductById(Long productId)
    {
        return mdProductMapper.selectMdProductById(productId);
    }

    @Override
    @DataScope(deptAlias = "su", userAlias = "su", permission = "business:product:list")
    public List<MdProduct> selectMdProductList(MdProduct mdProduct)
    {
        return mdProductMapper.selectMdProductList(mdProduct);
    }

    @Override
    public int insertMdProduct(MdProduct mdProduct)
    {
        return mdProductMapper.insertMdProduct(mdProduct);
    }

    @Override
    public int updateMdProduct(MdProduct mdProduct)
    {
        return mdProductMapper.updateMdProduct(mdProduct);
    }

    @Override
    public int deleteMdProductById(Long productId)
    {
        validateProductCanBeDeleted(productId, false);
        return mdProductMapper.deleteMdProductById(productId);
    }

    @Override
    public int deleteMdProductByIds(Long[] productIds)
    {
        for (Long productId : productIds)
        {
            validateProductCanBeDeleted(productId, true);
        }
        return mdProductMapper.deleteMdProductByIds(productIds);
    }

    @Override
    public String importMdProduct(List<MdProduct> mdProductList, Boolean isUpdateSupport, String operName)
    {
        if (StringUtils.isNull(mdProductList) || mdProductList.size() == 0)
        {
            throw new ServiceException("导入商品数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (MdProduct product : mdProductList)
        {
            try
            {
                // 验证是否存在这个商品编码
                MdProduct query = new MdProduct();
                query.setProductCode(product.getProductCode());
                List<MdProduct> list = mdProductMapper.selectMdProductList(query);
                boolean exists = list.size() > 0;
                
                if (!exists)
                {
                    product.setCreateBy(operName);
                    product.setStatus(StringUtils.isNotEmpty(product.getStatus()) ? product.getStatus() : "0");
                    this.insertMdProduct(product);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、商品编码 " + product.getProductCode() + " 导入成功");
                }
                else if (isUpdateSupport)
                {
                    MdProduct existingProduct = list.get(0);
                    product.setProductId(existingProduct.getProductId());
                    product.setUpdateBy(operName);
                    this.updateMdProduct(product);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、商品编码 " + product.getProductCode() + " 更新成功");
                }
                else
                {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、商品编码 " + product.getProductCode() + " 已存在");
                }
            }
            catch (Exception e)
            {
                failureNum++;
                String msg = "<br/>" + failureNum + "、商品编码 " + product.getProductCode() + " 导入失败：";
                failureMsg.append(msg + e.getMessage());
            }
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        }
        else
        {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }

    /**
     * 校验商品是否允许删除
     *
     * @param productId 商品编号
     * @param batchDelete 是否批量删除
     */
    private void validateProductCanBeDeleted(Long productId, boolean batchDelete)
    {
        if (productId == null)
        {
            return;
        }
        MdProduct databaseProduct = mdProductMapper.selectMdProductById(productId);
        if (databaseProduct == null)
        {
            return;
        }
        Integer referenceCount = mdProductMapper.selectProductReferenceCount(productId);
        if (referenceCount != null && referenceCount.intValue() > 0)
        {
            if (batchDelete)
            {
                throw new ServiceException("商品已被业务单据或库存引用，不能删除，商品名称：" + databaseProduct.getProductName());
            }
            throw new ServiceException("商品已被业务单据或库存引用，不能删除");
        }
    }
}
