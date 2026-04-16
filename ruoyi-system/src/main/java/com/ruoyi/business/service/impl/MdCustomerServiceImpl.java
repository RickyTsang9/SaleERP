package com.ruoyi.business.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
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

    /**
     * 查询客户
     *
     * @param customerId 客户编号
     * @return 客户信息
     */
    @Override
    public MdCustomer selectMdCustomerById(Long customerId)
    {
        return mdCustomerMapper.selectMdCustomerById(customerId);
    }

    /**
     * 查询客户列表
     *
     * @param mdCustomer 客户查询条件
     * @return 客户列表
     */
    @Override
    @DataScope(deptAlias = "su", userAlias = "su", permission = "business:customer:list")
    public List<MdCustomer> selectMdCustomerList(MdCustomer mdCustomer)
    {
        return mdCustomerMapper.selectMdCustomerList(mdCustomer);
    }

    /**
     * 新增客户
     *
     * @param mdCustomer 客户信息
     * @return 结果
     */
    @Override
    public int insertMdCustomer(MdCustomer mdCustomer)
    {
        return mdCustomerMapper.insertMdCustomer(mdCustomer);
    }

    /**
     * 修改客户
     *
     * @param mdCustomer 客户信息
     * @return 结果
     */
    @Override
    public int updateMdCustomer(MdCustomer mdCustomer)
    {
        return mdCustomerMapper.updateMdCustomer(mdCustomer);
    }

    /**
     * 删除客户
     *
     * @param customerId 客户编号
     * @return 结果
     */
    @Override
    public int deleteMdCustomerById(Long customerId)
    {
        validateCustomerCanBeDeleted(customerId, false);
        return mdCustomerMapper.deleteMdCustomerById(customerId);
    }

    /**
     * 批量删除客户
     *
     * @param customerIds 客户编号数组
     * @return 结果
     */
    @Override
    public int deleteMdCustomerByIds(Long[] customerIds)
    {
        for (Long customerId : customerIds)
        {
            validateCustomerCanBeDeleted(customerId, true);
        }
        return mdCustomerMapper.deleteMdCustomerByIds(customerIds);
    }

    /**
     * 导入客户数据
     *
     * @param mdCustomerList 客户导入集合
     * @param isUpdateSupport 是否支持更新
     * @param operName 操作人
     * @return 导入结果
     */
    @Override
    public String importMdCustomer(List<MdCustomer> mdCustomerList, Boolean isUpdateSupport, String operName)
    {
        if (StringUtils.isNull(mdCustomerList) || mdCustomerList.size() == 0)
        {
            throw new ServiceException("导入客户数据不能为空！");
        }
        Map<String, MdCustomer> existingCustomerMap = buildExistingCustomerMap(mdCustomerList);
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (MdCustomer customer : mdCustomerList)
        {
            try
            {
                // 导入前批量加载已存在客户，循环内直接命中映射，减少逐条查库。
                MdCustomer existingCustomer = existingCustomerMap.get(customer.getCustomerCode());
                boolean exists = existingCustomer != null;

                if (!exists)
                {
                    customer.setCreateBy(operName);
                    customer.setStatus(StringUtils.isNotEmpty(customer.getStatus()) ? customer.getStatus() : "0");
                    this.insertMdCustomer(customer);
                    existingCustomerMap.put(customer.getCustomerCode(), customer);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、客户编码 " + customer.getCustomerCode() + " 导入成功");
                }
                else if (isUpdateSupport)
                {
                    customer.setCustomerId(existingCustomer.getCustomerId());
                    customer.setUpdateBy(operName);
                    this.updateMdCustomer(customer);
                    existingCustomerMap.put(customer.getCustomerCode(), customer);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、客户编码 " + customer.getCustomerCode() + " 更新成功");
                }
                else
                {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、客户编码 " + customer.getCustomerCode() + " 已存在");
                }
            }
            catch (Exception e)
            {
                failureNum++;
                String msg = "<br/>" + failureNum + "、客户编码 " + customer.getCustomerCode() + " 导入失败：";
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
     * 批量构建已存在客户映射
     *
     * @param mdCustomerList 客户导入集合
     * @return 客户编码映射
     */
    private Map<String, MdCustomer> buildExistingCustomerMap(List<MdCustomer> mdCustomerList)
    {
        Map<String, MdCustomer> existingCustomerMap = new HashMap<String, MdCustomer>();
        List<String> customerCodeList = new ArrayList<String>();
        for (MdCustomer mdCustomer : mdCustomerList)
        {
            if (mdCustomer != null && StringUtils.isNotEmpty(mdCustomer.getCustomerCode()))
            {
                customerCodeList.add(mdCustomer.getCustomerCode());
            }
        }
        if (customerCodeList.isEmpty())
        {
            return existingCustomerMap;
        }
        List<MdCustomer> existingCustomerList = mdCustomerMapper.selectMdCustomerListByCustomerCodes(customerCodeList);
        if (existingCustomerList == null || existingCustomerList.isEmpty())
        {
            return existingCustomerMap;
        }
        for (MdCustomer existingCustomer : existingCustomerList)
        {
            existingCustomerMap.put(existingCustomer.getCustomerCode(), existingCustomer);
        }
        return existingCustomerMap;
    }

    /**
     * 校验客户是否允许删除
     *
     * @param customerId 客户编号
     * @param batchDelete 是否批量删除
     */
    private void validateCustomerCanBeDeleted(Long customerId, boolean batchDelete)
    {
        if (customerId == null)
        {
            return;
        }
        MdCustomer databaseCustomer = mdCustomerMapper.selectMdCustomerById(customerId);
        if (databaseCustomer == null)
        {
            return;
        }
        Integer referenceCount = mdCustomerMapper.selectCustomerReferenceCount(customerId);
        if (referenceCount != null && referenceCount.intValue() > 0)
        {
            if (batchDelete)
            {
                throw new ServiceException("客户已被业务单据引用，不能删除，客户名称：" + databaseCustomer.getCustomerName());
            }
            throw new ServiceException("客户已被业务单据引用，不能删除");
        }
    }
}
