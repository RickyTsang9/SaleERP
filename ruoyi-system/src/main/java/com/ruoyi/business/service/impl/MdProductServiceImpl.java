package com.ruoyi.business.service.impl;

import java.util.List;
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
        return mdProductMapper.deleteMdProductById(productId);
    }

    @Override
    public int deleteMdProductByIds(Long[] productIds)
    {
        return mdProductMapper.deleteMdProductByIds(productIds);
    }
}
