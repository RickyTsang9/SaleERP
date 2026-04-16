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
    /**
     * 导入客户数据
     *
     * @param mdCustomerList 客户数据列表
     * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
     * @param operName 操作用户
     * @return 结果
     */
    public String importMdCustomer(List<MdCustomer> mdCustomerList, Boolean isUpdateSupport, String operName);
}
