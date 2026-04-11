package com.ruoyi.web.controller.business;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.WmsStock;
import com.ruoyi.business.service.IWmsStockService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/business/stock")
public class WmsStockController extends BaseController
{
    @Autowired
    private IWmsStockService wmsStockService;

    @PreAuthorize("@ss.hasPermi('business:stock:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmsStock wmsStock)
    {
        startPage();
        List<WmsStock> wmsStockList = wmsStockService.selectWmsStockList(wmsStock);
        return getDataTable(wmsStockList);
    }

    @PreAuthorize("@ss.hasPermi('business:stock:list')")
    @GetMapping("/warning/list")
    public TableDataInfo warningList(WmsStock wmsStock)
    {
        startPage();
        List<WmsStock> wmsStockList = wmsStockService.selectWmsStockWarningList(wmsStock);
        return getDataTable(wmsStockList);
    }

    @PreAuthorize("@ss.hasPermi('business:stock:list')")
    @PostMapping("/warning/remind")
    public AjaxResult warningRemind()
    {
        int warningCount = wmsStockService.pushWarningReminder(getUsername());
        AjaxResult ajaxResult = AjaxResult.success();
        ajaxResult.put("warningCount", warningCount);
        return ajaxResult;
    }

    @PreAuthorize("@ss.hasPermi('business:stock:export')")
    @Log(title = "库存台账", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(javax.servlet.http.HttpServletResponse response, WmsStock wmsStock)
    {
        List<WmsStock> wmsStockList = wmsStockService.selectWmsStockList(wmsStock);
        ExcelUtil<WmsStock> util = new ExcelUtil<WmsStock>(WmsStock.class);
        util.exportExcel(response, wmsStockList, "库存台账数据");
    }

    @PreAuthorize("@ss.hasPermi('business:stock:query')")
    @GetMapping("/{stockId}")
    public AjaxResult getInfo(@PathVariable Long stockId)
    {
        return success(wmsStockService.selectWmsStockById(stockId));
    }

    @PreAuthorize("@ss.hasPermi('business:stock:add')")
    @Log(title = "库存台账", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public AjaxResult add(@RequestBody WmsStock wmsStock)
    {
        wmsStock.setCreateBy(getUsername());
        return toAjax(wmsStockService.insertWmsStock(wmsStock));
    }

    @PreAuthorize("@ss.hasPermi('business:stock:edit')")
    @Log(title = "库存台账", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public AjaxResult edit(@RequestBody WmsStock wmsStock)
    {
        wmsStock.setUpdateBy(getUsername());
        return toAjax(wmsStockService.updateWmsStock(wmsStock));
    }

    @PreAuthorize("@ss.hasPermi('business:stock:remove')")
    @Log(title = "库存台账", businessType = BusinessType.DELETE)
    @DeleteMapping("/{stockIds}")
    public AjaxResult remove(@PathVariable Long[] stockIds)
    {
        return toAjax(wmsStockService.deleteWmsStockByIds(stockIds));
    }
}
