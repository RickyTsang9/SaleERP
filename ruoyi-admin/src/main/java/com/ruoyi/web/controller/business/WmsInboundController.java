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
import com.ruoyi.business.domain.WmsInbound;
import com.ruoyi.business.service.IWmsInboundService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/business/inbound")
public class WmsInboundController extends BaseController
{
    @Autowired
    private IWmsInboundService wmsInboundService;

    @PreAuthorize("@ss.hasPermi('business:inbound:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmsInbound wmsInbound)
    {
        startPage();
        List<WmsInbound> wmsInboundList = wmsInboundService.selectWmsInboundList(wmsInbound);
        return getDataTable(wmsInboundList);
    }

    @PreAuthorize("@ss.hasPermi('business:inbound:export')")
    @Log(title = "入库单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(jakarta.servlet.http.HttpServletResponse response, WmsInbound wmsInbound)
    {
        List<WmsInbound> wmsInboundList = wmsInboundService.selectWmsInboundList(wmsInbound);
        ExcelUtil<WmsInbound> util = new ExcelUtil<WmsInbound>(WmsInbound.class);
        util.exportExcel(response, wmsInboundList, "入库单数据");
    }

    @PreAuthorize("@ss.hasPermi('business:inbound:query')")
    @GetMapping("/{inboundId}")
    public AjaxResult getInfo(@PathVariable Long inboundId)
    {
        return success(wmsInboundService.selectWmsInboundById(inboundId));
    }

    @PreAuthorize("@ss.hasPermi('business:inbound:add')")
    @Log(title = "入库单", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public AjaxResult add(@RequestBody WmsInbound wmsInbound)
    {
        wmsInbound.setCreateBy(getUsername());
        return toAjax(wmsInboundService.insertWmsInbound(wmsInbound));
    }

    @PreAuthorize("@ss.hasPermi('business:inbound:edit')")
    @Log(title = "入库单", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public AjaxResult edit(@RequestBody WmsInbound wmsInbound)
    {
        wmsInbound.setUpdateBy(getUsername());
        return toAjax(wmsInboundService.updateWmsInbound(wmsInbound));
    }

    @PreAuthorize("@ss.hasPermi('business:inbound:remove')")
    @Log(title = "入库单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{inboundIds}")
    public AjaxResult remove(@PathVariable Long[] inboundIds)
    {
        return toAjax(wmsInboundService.deleteWmsInboundByIds(inboundIds));
    }
}
