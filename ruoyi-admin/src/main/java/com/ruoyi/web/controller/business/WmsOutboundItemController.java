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
import com.ruoyi.business.domain.WmsOutboundItem;
import com.ruoyi.business.service.IWmsOutboundItemService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/business/outboundItem")
public class WmsOutboundItemController extends BaseController
{
    @Autowired
    private IWmsOutboundItemService wmsOutboundItemService;

    @PreAuthorize("@ss.hasPermi('business:outbound:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmsOutboundItem wmsOutboundItem)
    {
        startPage();
        List<WmsOutboundItem> wmsOutboundItemList = wmsOutboundItemService.selectWmsOutboundItemList(wmsOutboundItem);
        return getDataTable(wmsOutboundItemList);
    }

    @PreAuthorize("@ss.hasPermi('business:outbound:export')")
    @Log(title = "出库明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(javax.servlet.http.HttpServletResponse response, WmsOutboundItem wmsOutboundItem)
    {
        List<WmsOutboundItem> wmsOutboundItemList = wmsOutboundItemService.selectWmsOutboundItemList(wmsOutboundItem);
        ExcelUtil<WmsOutboundItem> util = new ExcelUtil<WmsOutboundItem>(WmsOutboundItem.class);
        util.exportExcel(response, wmsOutboundItemList, "出库明细数据");
    }

    @PreAuthorize("@ss.hasPermi('business:outbound:query')")
    @GetMapping("/{outboundItemId}")
    public AjaxResult getInfo(@PathVariable Long outboundItemId)
    {
        return success(wmsOutboundItemService.selectWmsOutboundItemById(outboundItemId));
    }

    @PreAuthorize("@ss.hasPermi('business:outbound:add')")
    @Log(title = "出库明细", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public AjaxResult add(@RequestBody WmsOutboundItem wmsOutboundItem)
    {
        return toAjax(wmsOutboundItemService.insertWmsOutboundItem(wmsOutboundItem));
    }

    @PreAuthorize("@ss.hasPermi('business:outbound:edit')")
    @Log(title = "出库明细", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public AjaxResult edit(@RequestBody WmsOutboundItem wmsOutboundItem)
    {
        return toAjax(wmsOutboundItemService.updateWmsOutboundItem(wmsOutboundItem));
    }

    @PreAuthorize("@ss.hasPermi('business:outbound:remove')")
    @Log(title = "出库明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/{outboundItemIds}")
    public AjaxResult remove(@PathVariable Long[] outboundItemIds)
    {
        return toAjax(wmsOutboundItemService.deleteWmsOutboundItemByIds(outboundItemIds));
    }
}
