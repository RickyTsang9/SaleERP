package com.ruoyi.web.controller.business;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.WmsStockLog;
import com.ruoyi.business.service.IWmsStockLogService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/business/stockLog")
public class WmsStockLogController extends BaseController
{
    @Autowired
    private IWmsStockLogService wmsStockLogService;

    @PreAuthorize("@ss.hasPermi('business:stockLog:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmsStockLog wmsStockLog)
    {
        startPage();
        List<WmsStockLog> wmsStockLogList = wmsStockLogService.selectWmsStockLogList(wmsStockLog);
        return getDataTable(wmsStockLogList);
    }

    @PreAuthorize("@ss.hasPermi('business:stockLog:export')")
    @Log(title = "库存流水", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(javax.servlet.http.HttpServletResponse response, WmsStockLog wmsStockLog)
    {
        List<WmsStockLog> wmsStockLogList = wmsStockLogService.selectWmsStockLogList(wmsStockLog);
        ExcelUtil<WmsStockLog> util = new ExcelUtil<WmsStockLog>(WmsStockLog.class);
        util.exportExcel(response, wmsStockLogList, "库存流水数据");
    }

    @PreAuthorize("@ss.hasPermi('business:stockLog:query')")
    @GetMapping("/{stockLogId}")
    public AjaxResult getInfo(@PathVariable Long stockLogId)
    {
        return success(wmsStockLogService.selectWmsStockLogById(stockLogId));
    }
}
