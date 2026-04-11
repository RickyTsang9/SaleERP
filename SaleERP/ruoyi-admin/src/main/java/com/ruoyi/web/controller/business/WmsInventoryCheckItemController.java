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
import com.ruoyi.business.domain.WmsInventoryCheckItem;
import com.ruoyi.business.service.IWmsInventoryCheckItemService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/business/inventoryCheckItem")
public class WmsInventoryCheckItemController extends BaseController
{
    @Autowired
    private IWmsInventoryCheckItemService wmsInventoryCheckItemService;

    @PreAuthorize("@ss.hasPermi('business:inventoryCheck:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmsInventoryCheckItem wmsInventoryCheckItem)
    {
        startPage();
        List<WmsInventoryCheckItem> wmsInventoryCheckItemList = wmsInventoryCheckItemService.selectWmsInventoryCheckItemList(wmsInventoryCheckItem);
        return getDataTable(wmsInventoryCheckItemList);
    }

    @PreAuthorize("@ss.hasPermi('business:inventoryCheck:export')")
    @Log(title = "库存盘点明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(javax.servlet.http.HttpServletResponse response, WmsInventoryCheckItem wmsInventoryCheckItem)
    {
        List<WmsInventoryCheckItem> wmsInventoryCheckItemList = wmsInventoryCheckItemService.selectWmsInventoryCheckItemList(wmsInventoryCheckItem);
        ExcelUtil<WmsInventoryCheckItem> util = new ExcelUtil<WmsInventoryCheckItem>(WmsInventoryCheckItem.class);
        util.exportExcel(response, wmsInventoryCheckItemList, "库存盘点明细数据");
    }

    @PreAuthorize("@ss.hasPermi('business:inventoryCheck:query')")
    @GetMapping("/{checkItemId}")
    public AjaxResult getInfo(@PathVariable Long checkItemId)
    {
        return success(wmsInventoryCheckItemService.selectWmsInventoryCheckItemById(checkItemId));
    }

    @PreAuthorize("@ss.hasPermi('business:inventoryCheck:add')")
    @Log(title = "库存盘点明细", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public AjaxResult add(@RequestBody WmsInventoryCheckItem wmsInventoryCheckItem)
    {
        return toAjax(wmsInventoryCheckItemService.insertWmsInventoryCheckItem(wmsInventoryCheckItem));
    }

    @PreAuthorize("@ss.hasPermi('business:inventoryCheck:edit')")
    @Log(title = "库存盘点明细", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public AjaxResult edit(@RequestBody WmsInventoryCheckItem wmsInventoryCheckItem)
    {
        return toAjax(wmsInventoryCheckItemService.updateWmsInventoryCheckItem(wmsInventoryCheckItem));
    }

    @PreAuthorize("@ss.hasPermi('business:inventoryCheck:remove')")
    @Log(title = "库存盘点明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/{checkItemIds}")
    public AjaxResult remove(@PathVariable Long[] checkItemIds)
    {
        return toAjax(wmsInventoryCheckItemService.deleteWmsInventoryCheckItemByIds(checkItemIds));
    }
}
