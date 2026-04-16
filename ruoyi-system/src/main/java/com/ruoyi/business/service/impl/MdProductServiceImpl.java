package com.ruoyi.business.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
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

    /**
     * 查询商品
     *
     * @param productId 商品编号
     * @return 商品信息
     */
    @Override
    public MdProduct selectMdProductById(Long productId)
    {
        return mdProductMapper.selectMdProductById(productId);
    }

    /**
     * 查询商品列表
     *
     * @param mdProduct 商品查询条件
     * @return 商品列表
     */
    @Override
    @DataScope(deptAlias = "su", userAlias = "su", permission = "business:product:list")
    public List<MdProduct> selectMdProductList(MdProduct mdProduct)
    {
        return mdProductMapper.selectMdProductList(mdProduct);
    }

    /**
     * 新增商品
     *
     * @param mdProduct 商品信息
     * @return 结果
     */
    @Override
    public int insertMdProduct(MdProduct mdProduct)
    {
        return mdProductMapper.insertMdProduct(mdProduct);
    }

    /**
     * 修改商品
     *
     * @param mdProduct 商品信息
     * @return 结果
     */
    @Override
    public int updateMdProduct(MdProduct mdProduct)
    {
        return mdProductMapper.updateMdProduct(mdProduct);
    }

    /**
     * 删除商品
     *
     * @param productId 商品编号
     * @return 结果
     */
    @Override
    public int deleteMdProductById(Long productId)
    {
        validateProductCanBeDeleted(productId, false);
        return mdProductMapper.deleteMdProductById(productId);
    }

    /**
     * 批量删除商品
     *
     * @param productIds 商品编号数组
     * @return 结果
     */
    @Override
    public int deleteMdProductByIds(Long[] productIds)
    {
        for (Long productId : productIds)
        {
            validateProductCanBeDeleted(productId, true);
        }
        return mdProductMapper.deleteMdProductByIds(productIds);
    }

    /**
     * 导入商品数据
     *
     * @param mdProductList 商品导入集合
     * @param isUpdateSupport 是否支持更新
     * @param operName 操作人
     * @return 导入结果
     */
    @Override
    public String importMdProduct(List<MdProduct> mdProductList, Boolean isUpdateSupport, String operName)
    {
        if (StringUtils.isNull(mdProductList) || mdProductList.size() == 0)
        {
            throw new ServiceException("导入商品数据不能为空！");
        }
        Map<String, MdProduct> existingProductMap = buildExistingProductMap(mdProductList);
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (MdProduct product : mdProductList)
        {
            try
            {
                // 导入前批量加载已存在商品，循环内直接命中映射，减少逐条查库。
                MdProduct existingProduct = existingProductMap.get(product.getProductCode());
                boolean exists = existingProduct != null;
                
                if (!exists)
                {
                    product.setCreateBy(operName);
                    product.setStatus(StringUtils.isNotEmpty(product.getStatus()) ? product.getStatus() : "0");
                    this.insertMdProduct(product);
                    existingProductMap.put(product.getProductCode(), product);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、商品编码 " + product.getProductCode() + " 导入成功");
                }
                else if (isUpdateSupport)
                {
                    product.setProductId(existingProduct.getProductId());
                    product.setUpdateBy(operName);
                    this.updateMdProduct(product);
                    existingProductMap.put(product.getProductCode(), product);
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
     * 批量构建已存在商品映射
     *
     * @param mdProductList 商品导入集合
     * @return 商品编码映射
     */
    private Map<String, MdProduct> buildExistingProductMap(List<MdProduct> mdProductList)
    {
        Map<String, MdProduct> existingProductMap = new HashMap<String, MdProduct>();
        List<String> productCodeList = new ArrayList<String>();
        for (MdProduct mdProduct : mdProductList)
        {
            if (mdProduct != null && StringUtils.isNotEmpty(mdProduct.getProductCode()))
            {
                productCodeList.add(mdProduct.getProductCode());
            }
        }
        if (productCodeList.isEmpty())
        {
            return existingProductMap;
        }
        List<MdProduct> existingProductList = mdProductMapper.selectMdProductListByProductCodes(productCodeList);
        if (existingProductList == null || existingProductList.isEmpty())
        {
            return existingProductMap;
        }
        for (MdProduct existingProduct : existingProductList)
        {
            existingProductMap.put(existingProduct.getProductCode(), existingProduct);
        }
        return existingProductMap;
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
