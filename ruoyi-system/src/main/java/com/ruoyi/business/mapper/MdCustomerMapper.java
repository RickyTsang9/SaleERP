package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.MdCustomer;

public interface MdCustomerMapper
{
    /**
     * 查询客户
     *
     * @param customerId 客户主键
     * @return 客户
     */
    public MdCustomer selectMdCustomerById(Long customerId);

    /**
     * 查询客户列表
     *
     * @param mdCustomer 客户
     * @return 客户集合
     */
    public List<MdCustomer> selectMdCustomerList(MdCustomer mdCustomer);

    /**
     * 根据客户编码查询客户
     *
     * @param customerCode 客户编码
     * @return 客户
     */
    public MdCustomer selectMdCustomerByCustomerCode(String customerCode);

    /**
     * 根据客户编码集合查询客户列表
     *
     * @param customerCodeList 客户编码集合
     * @return 客户集合
     */
    public List<MdCustomer> selectMdCustomerListByCustomerCodes(List<String> customerCodeList);

    /**
     * 新增客户
     *
     * @param mdCustomer 客户
     * @return 结果
     */
    public int insertMdCustomer(MdCustomer mdCustomer);

    /**
     * 修改客户
     *
     * @param mdCustomer 客户
     * @return 结果
     */
    public int updateMdCustomer(MdCustomer mdCustomer);

    /**
     * 查询客户引用数量
     *
     * @param customerId 客户主键
     * @return 引用数量
     */
    public int selectCustomerReferenceCount(Long customerId);

    /**
     * 删除客户
     *
     * @param customerId 客户主键
     * @return 结果
     */
    public int deleteMdCustomerById(Long customerId);

    /**
     * 批量删除客户
     *
     * @param customerIds 客户主键集合
     * @return 结果
     */
    public int deleteMdCustomerByIds(Long[] customerIds);
}
