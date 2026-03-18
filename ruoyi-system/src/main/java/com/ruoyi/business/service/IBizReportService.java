package com.ruoyi.business.service;

import java.util.Map;
import com.ruoyi.common.core.domain.AjaxResult;

public interface IBizReportService
{
    public AjaxResult getDashboardData();

    public Map<String, Object> getSaleTrendData();

    public Map<String, Object> getTopCustomerData();
}
