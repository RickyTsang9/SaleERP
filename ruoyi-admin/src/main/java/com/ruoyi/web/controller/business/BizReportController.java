package com.ruoyi.web.controller.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.service.IBizReportService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;

@RestController
@RequestMapping("/business/report")
public class BizReportController extends BaseController
{
    @Autowired
    private IBizReportService bizReportService;

    @PreAuthorize("@ss.hasPermi('business:report:view')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard()
    {
        return bizReportService.getDashboardData();
    }
}
