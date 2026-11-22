package com.ruoyi.business.service.support;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 业务内控合规公共校验器（月结关账锁定期、制审分离、过期批次拦截）
 */
@Component
public class BizComplianceHelper
{
    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private ISysConfigService sysConfigService;

    /**
     * 校验业务日期所在月份是否已月结关账
     */
    public void assertPeriodOpen(Date businessDate, String actionLabel)
    {
        if (jdbcTemplate == null)
        {
            return;
        }
        Date checkDate = businessDate == null ? new Date() : businessDate;
        String periodMonth = new SimpleDateFormat("yyyy-MM").format(checkDate);
        try
        {
            Integer closedCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM fin_period_close WHERE period_month = ? AND close_status = 'closed'",
                Integer.class,
                periodMonth
            );
            if (closedCount != null && closedCount > 0)
            {
                throw new ServiceException("会计期间 [" + periodMonth + "] 已完成财务月结关账，禁止" + actionLabel + "单据");
            }
        }
        catch (ServiceException se)
        {
            throw se;
        }
        catch (Exception ex)
        {
            throw new ServiceException("校验会计期间状态失败，请联系管理员检查月结配置：" + ex.getMessage());
        }
    }

    /**
     * 校验基础资料是否处于启用状态（status = '0'），拦截已停用资料参与新业务流转
     */
    public void assertEntityEnabled(String tableName, String idColumn, Long id, String entityLabel)
    {
        if (jdbcTemplate == null || id == null)
        {
            return;
        }
        String status = null;
        try
        {
            status = jdbcTemplate.queryForObject(
                "SELECT status FROM " + tableName + " WHERE " + idColumn + " = ?",
                String.class,
                id
            );
        }
        catch (org.springframework.dao.EmptyResultDataAccessException emptyEx)
        {
            throw new ServiceException(entityLabel + "（编号：" + id + "）不存在");
        }
        if ("1".equals(status))
        {
            throw new ServiceException(entityLabel + "（编号：" + id + "）已停用，禁止参与业务操作");
        }
    }

    /**
     * 校验商品在全库总在库库存是否为0（停用前防线）
     */
    public BigDecimal queryBigDecimal(String sql, Object... args)
    {
        if (jdbcTemplate == null)
        {
            return BigDecimal.ZERO;
        }
        BigDecimal val = jdbcTemplate.queryForObject(sql, BigDecimal.class, args);
        return val == null ? BigDecimal.ZERO : val;
    }

    public Long queryLong(String sql, Object... args)
    {
        if (jdbcTemplate == null)
        {
            return 0L;
        }
        Long val = jdbcTemplate.queryForObject(sql, Long.class, args);
        return val == null ? 0L : val;
    }

    /**
     * 校验制单人与审核人分离（受系统参数 biz.audit.preventSelfApprove 控制）
     */
    public void assertNotSelfAudit(String creatorUsername, String actionLabel)
    {
        if (sysConfigService == null || StringUtils.isEmpty(creatorUsername))
        {
            return;
        }
        String preventSelf = sysConfigService.selectConfigByKey("biz.audit.preventSelfApprove");
        if (!"true".equalsIgnoreCase(StringUtils.trim(preventSelf)))
        {
            return;
        }
        try
        {
            String currentUsername = SecurityUtils.getUsername();
            if (StringUtils.isNotEmpty(currentUsername) && currentUsername.equalsIgnoreCase(creatorUsername.trim()))
            {
                throw new ServiceException("内控合规要求：制单人与审核人不能为同一人（" + actionLabel + "）");
            }
        }
        catch (ServiceException se)
        {
            throw se;
        }
        catch (Exception ignored)
        {
            // 非 Web 线程忽略
        }
    }

    /**
     * 校验出库/销售批次是否已过期
     */
    public void assertBatchNotExpired(Date expireDate, String batchNo)
    {
        if (expireDate == null)
        {
            return;
        }
        LocalDate expLocalDate = new java.sql.Date(expireDate.getTime()).toLocalDate();
        if (expLocalDate.isBefore(LocalDate.now()))
        {
            throw new ServiceException("批次 [" + (StringUtils.isEmpty(batchNo) ? "-" : batchNo)
                + "] 已于 " + expLocalDate + " 过期，禁止销售出库，请走盘点报损或隔离处理");
        }
    }
}
