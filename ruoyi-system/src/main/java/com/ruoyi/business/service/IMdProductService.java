package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.MdProduct;

public interface IMdProductService
{
    public MdProduct selectMdProductById(Long productId);

    public List<MdProduct> selectMdProductList(MdProduct mdProduct);

    public int insertMdProduct(MdProduct mdProduct);

    public int updateMdProduct(MdProduct mdProduct);

    public int deleteMdProductById(Long productId);

    public int deleteMdProductByIds(Long[] productIds);
}
