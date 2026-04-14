package com.ruoyi.business.service.impl;

import java.util.List;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.DateUtils;
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
        validateCustomerCanBeDeleted(customerId, false);
        return mdCustomerMapper.deleteMdCustomerById(customerId);
    }

    @Override
    public int deleteMdCustomerByIds(Long[] customerIds)
    {
        for (Long customerId : customerIds)
        {
            validateCustomerCanBeDeleted(customerId, true);
        }
        return mdCustomerMapper.deleteMdCustomerByIds(customerIds);
    }

    @Override
    public String importMdCustomer(List<MdCustomer> mdCustomerList, Boolean isUpdateSupport, String operName)
    {
        if (StringUtils.isNull(mdCustomerList) || mdCustomerList.size() == 0)
        {
            throw new ServiceException("导入客户数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (MdCustomer customer : mdCustomerList)
        {
            try
            {
                // 验证是否存在这个客户编码
                MdCustomer query = new MdCustomer();
                query.setCustomerCode(customer.getCustomerCode());
                List<MdCustomer> list = mdCustomerMapper.selectMdCustomerList(query);
                boolean exists = list.size() > 0;

                if (!exists)
                {
                    customer.setCreateBy(operName);
                    customer.setStatus(StringUtils.isNotEmpty(customer.getStatus()) ? customer.getStatus() : "0");
                    this.insertMdCustomer(customer);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、客户编码 " + customer.getCustomerCode() + " 导入成功");
                }
                else if (isUpdateSupport)
                {
                    MdCustomer existingCustomer = list.get(0);
                    customer.setCustomerId(existingCustomer.getCustomerId());
                    customer.setUpdateBy(operName);
                    this.updateMdCustomer(customer);
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
