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
import com.ruoyi.business.domain.WmsInventoryCheck;
import com.ruoyi.business.service.IWmsInventoryCheckService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/business/inventoryCheck")
public class WmsInventoryCheckController extends BaseController
{
    @Autowired
    private IWmsInventoryCheckService wmsInventoryCheckService;

    @PreAuthorize("@ss.hasPermi('business:inventoryCheck:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmsInventoryCheck wmsInventoryCheck)
    {
        startPage();
        List<WmsInventoryCheck> wmsInventoryCheckList = wmsInventoryCheckService.selectWmsInventoryCheckList(wmsInventoryCheck);
        return getDataTable(wmsInventoryCheckList);
    }

    @PreAuthorize("@ss.hasPermi('business:inventoryCheck:export')")
    @Log(title = "库存盘点单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(jakarta.servlet.http.HttpServletResponse response, WmsInventoryCheck wmsInventoryCheck)
    {
        List<WmsInventoryCheck> wmsInventoryCheckList = wmsInventoryCheckService.selectWmsInventoryCheckList(wmsInventoryCheck);
        ExcelUtil<WmsInventoryCheck> util = new ExcelUtil<WmsInventoryCheck>(WmsInventoryCheck.class);
        util.exportExcel(response, wmsInventoryCheckList, "库存盘点单数据");
    }

    @PreAuthorize("@ss.hasPermi('business:inventoryCheck:query')")
    @GetMapping("/{checkId}")
    public AjaxResult getInfo(@PathVariable Long checkId)
    {
        return success(wmsInventoryCheckService.selectWmsInventoryCheckById(checkId));
    }

    @PreAuthorize("@ss.hasPermi('business:inventoryCheck:add')")
    @Log(title = "库存盘点单", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public AjaxResult add(@RequestBody WmsInventoryCheck wmsInventoryCheck)
    {
        wmsInventoryCheck.setCreateBy(getUsername());
        return toAjax(wmsInventoryCheckService.insertWmsInventoryCheck(wmsInventoryCheck));
    }

    @PreAuthorize("@ss.hasPermi('business:inventoryCheck:edit')")
    @Log(title = "库存盘点单", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public AjaxResult edit(@RequestBody WmsInventoryCheck wmsInventoryCheck)
    {
        wmsInventoryCheck.setUpdateBy(getUsername());
        return toAjax(wmsInventoryCheckService.updateWmsInventoryCheck(wmsInventoryCheck));
    }

    @PreAuthorize("@ss.hasPermi('business:inventoryCheck:edit')")
    @Log(title = "库存盘点单", businessType = BusinessType.UPDATE)
    @PostMapping("/submit/{checkId}")
    public AjaxResult submit(@PathVariable Long checkId)
    {
        return toAjax(wmsInventoryCheckService.submitWmsInventoryCheck(checkId));
    }

    @PreAuthorize("@ss.hasPermi('business:inventoryCheck:audit')")
    @Log(title = "库存盘点单", businessType = BusinessType.UPDATE)
    @PostMapping("/audit/{checkId}")
    public AjaxResult audit(@PathVariable Long checkId)
    {
        return toAjax(wmsInventoryCheckService.auditWmsInventoryCheck(checkId));
    }

    @PreAuthorize("@ss.hasPermi('business:inventoryCheck:edit')")
    @Log(title = "库存盘点单", businessType = BusinessType.UPDATE)
    @PostMapping("/cancel/{checkId}")
    public AjaxResult cancel(@PathVariable Long checkId)
    {
        return toAjax(wmsInventoryCheckService.cancelWmsInventoryCheck(checkId));
    }

    @PreAuthorize("@ss.hasPermi('business:inventoryCheck:remove')")
    @Log(title = "库存盘点单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{checkIds}")
    public AjaxResult remove(@PathVariable Long[] checkIds)
    {
        return toAjax(wmsInventoryCheckService.deleteWmsInventoryCheckByIds(checkIds));
    }
}
