package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.MdCustomer;

public interface IMdCustomerService
{
    public MdCustomer selectMdCustomerById(Long customerId);

    public List<MdCustomer> selectMdCustomerList(MdCustomer mdCustomer);

    public int insertMdCustomer(MdCustomer mdCustomer);

    public int updateMdCustomer(MdCustomer mdCustomer);

    public int deleteMdCustomerById(Long customerId);

    public int deleteMdCustomerByIds(Long[] customerIds);
}
