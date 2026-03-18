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
import com.ruoyi.business.domain.MdLocation;
import com.ruoyi.business.service.IMdLocationService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/business/location")
public class MdLocationController extends BaseController
{
    @Autowired
    private IMdLocationService mdLocationService;

    @PreAuthorize("@ss.hasPermi('business:location:list')")
    @GetMapping("/list")
    public TableDataInfo list(MdLocation mdLocation)
    {
        startPage();
        List<MdLocation> mdLocationList = mdLocationService.selectMdLocationList(mdLocation);
        return getDataTable(mdLocationList);
    }

    @PreAuthorize("@ss.hasPermi('business:location:export')")
    @Log(title = "库位资料", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(jakarta.servlet.http.HttpServletResponse response, MdLocation mdLocation)
    {
        List<MdLocation> mdLocationList = mdLocationService.selectMdLocationList(mdLocation);
        ExcelUtil<MdLocation> util = new ExcelUtil<MdLocation>(MdLocation.class);
        util.exportExcel(response, mdLocationList, "库位资料数据");
    }

    @PreAuthorize("@ss.hasPermi('business:location:query')")
    @GetMapping("/{locationId}")
    public AjaxResult getInfo(@PathVariable Long locationId)
    {
        return success(mdLocationService.selectMdLocationById(locationId));
    }

    @PreAuthorize("@ss.hasPermi('business:location:add')")
    @Log(title = "库位资料", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public AjaxResult add(@RequestBody MdLocation mdLocation)
    {
        mdLocation.setCreateBy(getUsername());
        return toAjax(mdLocationService.insertMdLocation(mdLocation));
    }

    @PreAuthorize("@ss.hasPermi('business:location:edit')")
    @Log(title = "库位资料", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public AjaxResult edit(@RequestBody MdLocation mdLocation)
    {
        mdLocation.setUpdateBy(getUsername());
        return toAjax(mdLocationService.updateMdLocation(mdLocation));
    }

    @PreAuthorize("@ss.hasPermi('business:location:remove')")
    @Log(title = "库位资料", businessType = BusinessType.DELETE)
    @DeleteMapping("/{locationIds}")
    public AjaxResult remove(@PathVariable Long[] locationIds)
    {
        return toAjax(mdLocationService.deleteMdLocationByIds(locationIds));
    }
}
