package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.MdProduct;

public interface MdProductMapper
{
    /**
     * 查询商品
     *
     * @param productId 商品主键
     * @return 商品
     */
    public MdProduct selectMdProductById(Long productId);

    /**
     * 查询商品列表
     *
     * @param mdProduct 商品
     * @return 商品集合
     */
    public List<MdProduct> selectMdProductList(MdProduct mdProduct);

    /**
     * 根据商品编码查询商品
     *
     * @param productCode 商品编码
     * @return 商品
     */
    public MdProduct selectMdProductByProductCode(String productCode);

    /**
     * 根据商品编码集合查询商品列表
     *
     * @param productCodeList 商品编码集合
     * @return 商品集合
     */
    public List<MdProduct> selectMdProductListByProductCodes(List<String> productCodeList);

    /**
     * 新增商品
     *
     * @param mdProduct 商品
     * @return 结果
     */
    public int insertMdProduct(MdProduct mdProduct);

    /**
     * 修改商品
     *
     * @param mdProduct 商品
     * @return 结果
     */
    public int updateMdProduct(MdProduct mdProduct);

    /**
     * 查询商品引用数量
     *
     * @param productId 商品主键
     * @return 引用数量
     */
    public int selectProductReferenceCount(Long productId);

    /**
     * 删除商品
     *
     * @param productId 商品主键
     * @return 结果
     */
    public int deleteMdProductById(Long productId);

    /**
     * 批量删除商品
     *
     * @param productIds 商品主键集合
     * @return 结果
     */
    public int deleteMdProductByIds(Long[] productIds);
}
