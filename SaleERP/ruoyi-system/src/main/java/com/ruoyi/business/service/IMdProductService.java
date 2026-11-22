package com.ruoyi.business.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.business.domain.MdProduct;

public interface IMdProductService
{
    public MdProduct selectMdProductById(Long productId);

    public List<MdProduct> selectMdProductList(MdProduct mdProduct);

    public int insertMdProduct(MdProduct mdProduct);

    public int updateMdProduct(MdProduct mdProduct);

    public int deleteMdProductById(Long productId);

    public int deleteMdProductByIds(Long[] productIds);
    /**
     * 导入商品数据
     *
     * @param mdProductList 商品数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName 操作用户
     * @return 结果
     */
    public String importMdProduct(List<MdProduct> mdProductList, Boolean isUpdateSupport, String operName);

    /**
     * 查询商品经营画像
     *
     * @param productId 商品编号
     * @return 商品经营画像数据
     */
    public Map<String, Object> selectProductProfile(Long productId);

    /**
     * 按条码或商品编码扫码匹配商品
     *
     * @param scanCode 条码或商品编码
     * @return 匹配到的商品
     */
    public MdProduct selectMdProductByScanCode(String scanCode);
}
