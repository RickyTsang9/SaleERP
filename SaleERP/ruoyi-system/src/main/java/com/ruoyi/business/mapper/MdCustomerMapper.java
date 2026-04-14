package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.MdCustomer;

public interface MdCustomerMapper
{
    public MdCustomer selectMdCustomerById(Long customerId);

    public List<MdCustomer> selectMdCustomerList(MdCustomer mdCustomer);

    public int insertMdCustomer(MdCustomer mdCustomer);

    public int updateMdCustomer(MdCustomer mdCustomer);

    public int selectCustomerReferenceCount(Long customerId);

    public int deleteMdCustomerById(Long customerId);

    public int deleteMdCustomerByIds(Long[] customerIds);
}
