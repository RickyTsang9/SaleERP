package com.ruoyi.business.service.impl;

import java.util.List;
import java.util.Map;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.mapper.CrmCustomerFollowMapper;
import com.ruoyi.business.domain.CrmCustomerFollow;
import com.ruoyi.business.service.ICrmCustomerFollowService;
import com.ruoyi.business.service.support.BizComplianceHelper;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 客户跟进记录Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
@Service
public class CrmCustomerFollowServiceImpl implements ICrmCustomerFollowService 
{
    @Autowired
    private CrmCustomerFollowMapper crmCustomerFollowMapper;

    @Autowired(required = false)
    private BizComplianceHelper complianceHelper;

    @Autowired(required = false)
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private com.ruoyi.business.service.IBizMessageService bizMessageService;

    /**
     * 查询客户跟进记录
     * 
     * @param followId 客户跟进记录主键
     * @return 客户跟进记录
     */
    @Override
    public CrmCustomerFollow selectCrmCustomerFollowByFollowId(Long followId)
    {
        return crmCustomerFollowMapper.selectCrmCustomerFollowByFollowId(followId);
    }

    /**
     * 查询客户跟进记录列表
     * 
     * @param crmCustomerFollow 客户跟进记录
     * @return 客户跟进记录
     */
    @Override
    @DataScope(deptAlias = "su", userAlias = "su", permission = "business:customerFollow:list")
    public List<CrmCustomerFollow> selectCrmCustomerFollowList(CrmCustomerFollow crmCustomerFollow)
    {
        return crmCustomerFollowMapper.selectCrmCustomerFollowList(crmCustomerFollow);
    }

    /**
     * 查询客户跟进待办摘要
     * 
     * @param crmCustomerFollow 客户跟进记录筛选条件
     * @return 客户跟进待办摘要
     */
    @Override
    @DataScope(deptAlias = "su", userAlias = "su", permission = "business:customerFollow:list")
    public Map<String, Object> selectCrmCustomerFollowTodoSummary(CrmCustomerFollow crmCustomerFollow)
    {
        return crmCustomerFollowMapper.selectCrmCustomerFollowTodoSummary(crmCustomerFollow);
    }

    /**
     * 新增客户跟进记录
     * 
     * @param crmCustomerFollow 客户跟进记录
     * @return 结果
     */
    @Override
    public int insertCrmCustomerFollow(CrmCustomerFollow crmCustomerFollow)
    {
        if (complianceHelper != null && crmCustomerFollow != null && crmCustomerFollow.getCustomerId() != null)
        {
            complianceHelper.assertEntityEnabled("md_customer", "customer_id", crmCustomerFollow.getCustomerId(), "客户");
        }
        crmCustomerFollow.setCreateTime(DateUtils.getNowDate());
        try {
            crmCustomerFollow.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
            // ignore
        }
        // OPT-15: 新增同客户跟进记录时，自动将该客户此前处于待跟进(status='0')的历史记录完结为已跟进('1')，避免过期旧待办堆积
        if (jdbcTemplate != null && crmCustomerFollow != null && crmCustomerFollow.getCustomerId() != null)
        {
            jdbcTemplate.update(
                "UPDATE crm_customer_follow SET status = '1', update_time = NOW(), update_by = ? WHERE customer_id = ? AND status = '0'",
                crmCustomerFollow.getCreateBy(), crmCustomerFollow.getCustomerId()
            );
        }
        return crmCustomerFollowMapper.insertCrmCustomerFollow(crmCustomerFollow);
    }

    /**
     * 修改客户跟进记录
     * 
     * @param crmCustomerFollow 客户跟进记录
     * @return 结果
     */
    @Override
    public int updateCrmCustomerFollow(CrmCustomerFollow crmCustomerFollow)
    {
        if (complianceHelper != null && crmCustomerFollow != null && crmCustomerFollow.getCustomerId() != null)
        {
            complianceHelper.assertEntityEnabled("md_customer", "customer_id", crmCustomerFollow.getCustomerId(), "客户");
        }
        crmCustomerFollow.setUpdateTime(DateUtils.getNowDate());
        try {
            crmCustomerFollow.setUpdateBy(SecurityUtils.getUsername());
        } catch (Exception e) {
            // ignore
        }
        return crmCustomerFollowMapper.updateCrmCustomerFollow(crmCustomerFollow);
    }

    /**
     * 批量删除客户跟进记录
     * 
     * @param followIds 需要删除的客户跟进记录主键
     * @return 结果
     */
    @Override
    public int deleteCrmCustomerFollowByFollowIds(Long[] followIds)
    {
        return crmCustomerFollowMapper.deleteCrmCustomerFollowByFollowIds(followIds);
    }

    /**
     * 删除客户跟进记录信息
     * 
     * @param followId 客户跟进记录主键
     * @return 结果
     */
    @Override
    public int deleteCrmCustomerFollowByFollowId(Long followId)
    {
        return crmCustomerFollowMapper.deleteCrmCustomerFollowByFollowId(followId);
    }

    @Override
    public int pushDueFollowReminder(String operator)
    {
        if (jdbcTemplate == null || bizMessageService == null)
        {
            return 0;
        }
        List<Map<String, Object>> dueRows = jdbcTemplate.queryForList(
            "SELECT cf.follow_id, cf.customer_id, IFNULL(mc.customer_name, CONCAT('客户#', cf.customer_id)) AS customer_name, "
            + "DATE_FORMAT(cf.next_follow_time, '%Y-%m-%d %H:%i') AS due_str, cf.create_by "
            + "FROM crm_customer_follow cf LEFT JOIN md_customer mc ON mc.customer_id = cf.customer_id "
            + "WHERE cf.status = '0' AND cf.next_follow_time IS NOT NULL AND cf.next_follow_time < DATE_ADD(CURDATE(), INTERVAL 1 DAY) "
            + "ORDER BY cf.next_follow_time ASC LIMIT 50"
        );
        if (dueRows == null || dueRows.isEmpty())
        {
            return 0;
        }
        String todayKey = DateUtils.getDate();
        int pushed = 0;
        for (Map<String, Object> row : dueRows)
        {
            Long followId = ((Number) row.get("follow_id")).longValue();
            String customerName = String.valueOf(row.get("customer_name"));
            String title = "客户回访提醒[" + todayKey + "]：" + customerName + " (#" + followId + ")";
            if (bizMessageService.existsBizMessageByTypeAndTitle("customer_follow_remind", title))
            {
                continue;
            }
            com.ruoyi.business.domain.BizMessage msg = new com.ruoyi.business.domain.BizMessage();
            msg.setMessageType("customer_follow_remind");
            msg.setMessageTitle(title);
            msg.setMessageContent("客户【" + customerName + "】计划回访时间（" + row.get("due_str") + "）已到期，请及时跟进。");
            msg.setMessageLevel("warning");
            msg.setBusinessType("customer_follow");
            msg.setBusinessId(followId);
            msg.setStatus("0");
            msg.setCreateBy(operator == null ? "system" : operator);
            bizMessageService.insertBizMessage(msg);
            pushed++;
        }
        return pushed;
    }
}
