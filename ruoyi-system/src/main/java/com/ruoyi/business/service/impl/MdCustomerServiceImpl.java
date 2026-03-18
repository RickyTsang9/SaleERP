package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.MdCustomer;
import com.ruoyi.business.mapper.MdCustomerMapper;
import com.ruoyi.business.service.IMdCustomerService;
import com.ruoyi.common.annotation.DataScope;

@Service
public class MdCustomerServiceImpl implements IMdCustomerService
{
    @Autowired
    private MdCustomerMapper mdCustomerMapper;

    @Override
    public MdCustomer selectMdCustomerById(Long customerId)
    {
        return mdCustomerMapper.selectMdCustomerById(customerId);
    }

    @Override
    @DataScope(deptAlias = "su", userAlias = "su", permission = "business:customer:list")
    public List<MdCustomer> selectMdCustomerList(MdCustomer mdCustomer)
    {
        return mdCustomerMapper.selectMdCustomerList(mdCustomer);
    }

    @Override
    public int insertMdCustomer(MdCustomer mdCustomer)
    {
        return mdCustomerMapper.insertMdCustomer(mdCustomer);
    }

    @Override
    public int updateMdCustomer(MdCustomer mdCustomer)
    {
        return mdCustomerMapper.updateMdCustomer(mdCustomer);
    }

    @Override
    public int deleteMdCustomerById(Long customerId)
    {
        return mdCustomerMapper.deleteMdCustomerById(customerId);
    }

    @Override
    public int deleteMdCustomerByIds(Long[] customerIds)
    {
        return mdCustomerMapper.deleteMdCustomerByIds(customerIds);
    }
}
