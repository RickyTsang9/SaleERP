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
import com.ruoyi.business.domain.WmsInboundItem;
import com.ruoyi.business.service.IWmsInboundItemService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/business/inboundItem")
public class WmsInboundItemController extends BaseController
{
    @Autowired
    private IWmsInboundItemService wmsInboundItemService;

    @PreAuthorize("@ss.hasPermi('business:inboundItem:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmsInboundItem wmsInboundItem)
    {
        startPage();
        List<WmsInboundItem> wmsInboundItemList = wmsInboundItemService.selectWmsInboundItemList(wmsInboundItem);
        return getDataTable(wmsInboundItemList);
    }

    @PreAuthorize("@ss.hasPermi('business:inboundItem:export')")
    @Log(title = "入库明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(jakarta.servlet.http.HttpServletResponse response, WmsInboundItem wmsInboundItem)
    {
        List<WmsInboundItem> wmsInboundItemList = wmsInboundItemService.selectWmsInboundItemList(wmsInboundItem);
        ExcelUtil<WmsInboundItem> util = new ExcelUtil<WmsInboundItem>(WmsInboundItem.class);
        util.exportExcel(response, wmsInboundItemList, "入库明细数据");
    }

    @PreAuthorize("@ss.hasPermi('business:inboundItem:query')")
    @GetMapping("/{inboundItemId}")
    public AjaxResult getInfo(@PathVariable Long inboundItemId)
    {
        return success(wmsInboundItemService.selectWmsInboundItemById(inboundItemId));
    }

    @PreAuthorize("@ss.hasPermi('business:inboundItem:add')")
    @Log(title = "入库明细", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public AjaxResult add(@RequestBody WmsInboundItem wmsInboundItem)
    {
        return toAjax(wmsInboundItemService.insertWmsInboundItem(wmsInboundItem));
    }

    @PreAuthorize("@ss.hasPermi('business:inboundItem:edit')")
    @Log(title = "入库明细", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public AjaxResult edit(@RequestBody WmsInboundItem wmsInboundItem)
    {
        return toAjax(wmsInboundItemService.updateWmsInboundItem(wmsInboundItem));
    }

    @PreAuthorize("@ss.hasPermi('business:inboundItem:remove')")
    @Log(title = "入库明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/{inboundItemIds}")
    public AjaxResult remove(@PathVariable Long[] inboundItemIds)
    {
        return toAjax(wmsInboundItemService.deleteWmsInboundItemByIds(inboundItemIds));
    }
}
