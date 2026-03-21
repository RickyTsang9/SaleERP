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
import com.ruoyi.business.domain.WmsOutbound;
import com.ruoyi.business.service.IWmsOutboundService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/business/outbound")
public class WmsOutboundController extends BaseController
{
    @Autowired
    private IWmsOutboundService wmsOutboundService;

    @PreAuthorize("@ss.hasPermi('business:outbound:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmsOutbound wmsOutbound)
    {
        startPage();
        List<WmsOutbound> wmsOutboundList = wmsOutboundService.selectWmsOutboundList(wmsOutbound);
        return getDataTable(wmsOutboundList);
    }

    @PreAuthorize("@ss.hasPermi('business:outbound:export')")
    @Log(title = "销售出库单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(javax.servlet.http.HttpServletResponse response, WmsOutbound wmsOutbound)
    {
        List<WmsOutbound> wmsOutboundList = wmsOutboundService.selectWmsOutboundList(wmsOutbound);
        ExcelUtil<WmsOutbound> util = new ExcelUtil<WmsOutbound>(WmsOutbound.class);
        util.exportExcel(response, wmsOutboundList, "销售出库单数据");
    }

    @PreAuthorize("@ss.hasPermi('business:outbound:query')")
    @GetMapping("/{outboundId}")
    public AjaxResult getInfo(@PathVariable Long outboundId)
    {
        return success(wmsOutboundService.selectWmsOutboundById(outboundId));
    }

    @PreAuthorize("@ss.hasPermi('business:outbound:add')")
    @Log(title = "销售出库单", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public AjaxResult add(@RequestBody WmsOutbound wmsOutbound)
    {
        wmsOutbound.setCreateBy(getUsername());
        return toAjax(wmsOutboundService.insertWmsOutbound(wmsOutbound));
    }

    @PreAuthorize("@ss.hasPermi('business:outbound:edit')")
    @Log(title = "销售出库单", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public AjaxResult edit(@RequestBody WmsOutbound wmsOutbound)
    {
        wmsOutbound.setUpdateBy(getUsername());
        return toAjax(wmsOutboundService.updateWmsOutbound(wmsOutbound));
    }

    @PreAuthorize("@ss.hasPermi('business:outbound:remove')")
    @Log(title = "销售出库单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{outboundIds}")
    public AjaxResult remove(@PathVariable Long[] outboundIds)
    {
        return toAjax(wmsOutboundService.deleteWmsOutboundByIds(outboundIds));
    }
}
