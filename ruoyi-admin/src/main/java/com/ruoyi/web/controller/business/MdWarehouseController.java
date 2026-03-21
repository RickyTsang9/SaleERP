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
import com.ruoyi.business.domain.MdWarehouse;
import com.ruoyi.business.service.IMdWarehouseService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/business/warehouse")
public class MdWarehouseController extends BaseController
{
    @Autowired
    private IMdWarehouseService mdWarehouseService;

    @PreAuthorize("@ss.hasPermi('business:warehouse:list')")
    @GetMapping("/list")
    public TableDataInfo list(MdWarehouse mdWarehouse)
    {
        startPage();
        List<MdWarehouse> mdWarehouseList = mdWarehouseService.selectMdWarehouseList(mdWarehouse);
        return getDataTable(mdWarehouseList);
    }

    @PreAuthorize("@ss.hasPermi('business:warehouse:export')")
    @Log(title = "仓库资料", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(javax.servlet.http.HttpServletResponse response, MdWarehouse mdWarehouse)
    {
        List<MdWarehouse> mdWarehouseList = mdWarehouseService.selectMdWarehouseList(mdWarehouse);
        ExcelUtil<MdWarehouse> util = new ExcelUtil<MdWarehouse>(MdWarehouse.class);
        util.exportExcel(response, mdWarehouseList, "仓库资料数据");
    }

    @PreAuthorize("@ss.hasPermi('business:warehouse:query')")
    @GetMapping("/{warehouseId}")
    public AjaxResult getInfo(@PathVariable Long warehouseId)
    {
        return success(mdWarehouseService.selectMdWarehouseById(warehouseId));
    }

    @PreAuthorize("@ss.hasPermi('business:warehouse:add')")
    @Log(title = "仓库资料", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public AjaxResult add(@RequestBody MdWarehouse mdWarehouse)
    {
        mdWarehouse.setCreateBy(getUsername());
        return toAjax(mdWarehouseService.insertMdWarehouse(mdWarehouse));
    }

    @PreAuthorize("@ss.hasPermi('business:warehouse:edit')")
    @Log(title = "仓库资料", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public AjaxResult edit(@RequestBody MdWarehouse mdWarehouse)
    {
        mdWarehouse.setUpdateBy(getUsername());
        return toAjax(mdWarehouseService.updateMdWarehouse(mdWarehouse));
    }

    @PreAuthorize("@ss.hasPermi('business:warehouse:remove')")
    @Log(title = "仓库资料", businessType = BusinessType.DELETE)
    @DeleteMapping("/{warehouseIds}")
    public AjaxResult remove(@PathVariable Long[] warehouseIds)
    {
        return toAjax(mdWarehouseService.deleteMdWarehouseByIds(warehouseIds));
    }
}
