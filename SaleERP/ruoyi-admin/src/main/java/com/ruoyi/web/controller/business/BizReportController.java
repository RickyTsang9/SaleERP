package com.ruoyi.web.controller.business;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.service.IBizReportService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;

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

    @PreAuthorize("@ss.hasPermi('business:report:sale')")
    @GetMapping("/saleList")
    public TableDataInfo saleList(@RequestParam Map<String, Object> params)
    {
        startPage();
        List<Map<String, Object>> list = bizReportService.selectSaleReportList(params);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('business:report:view')")
    @GetMapping("/reconciliationList")
    public TableDataInfo reconciliationList(@RequestParam Map<String, Object> params)
    {
        startPage();
        List<Map<String, Object>> list = bizReportService.selectReconciliationList(params);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('business:report:view')")
    @GetMapping("/invoiceTaxList")
    public TableDataInfo invoiceTaxList(@RequestParam Map<String, Object> params)
    {
        startPage();
        List<Map<String, Object>> list = bizReportService.selectInvoiceTaxList(params);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('business:report:view')")
    @GetMapping("/cashFlowForecast")
    public AjaxResult cashFlowForecast(@RequestParam(required = false) Integer forecastDays)
    {
        return AjaxResult.success(bizReportService.getCashFlowForecast(forecastDays));
    }
}
