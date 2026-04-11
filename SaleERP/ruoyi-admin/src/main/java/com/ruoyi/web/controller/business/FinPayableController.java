package com.ruoyi.web.controller.business;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.business.domain.FinPayable;
import com.ruoyi.business.service.IFinPayableService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 应付账款Controller
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
@RestController
@RequestMapping("/business/payable")
public class FinPayableController extends BaseController
{
    @Autowired
    private IFinPayableService finPayableService;

    /**
     * 查询应付账款列表
     */
    @PreAuthorize("@ss.hasPermi('business:payable:list')")
    @GetMapping("/list")
    public TableDataInfo list(FinPayable finPayable)
    {
        startPage();
        List<FinPayable> list = finPayableService.selectFinPayableList(finPayable);
        return getDataTable(list);
    }

    /**
     * 导出应付账款列表
     */
    @PreAuthorize("@ss.hasPermi('business:payable:export')")
    @Log(title = "应付账款", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, FinPayable finPayable)
    {
        List<FinPayable> list = finPayableService.selectFinPayableList(finPayable);
        ExcelUtil<FinPayable> util = new ExcelUtil<FinPayable>(FinPayable.class);
        util.exportExcel(response, list, "应付账款数据");
    }

    /**
     * 获取应付账款详细信息
     */
    @PreAuthorize("@ss.hasPermi('business:payable:query')")
    @GetMapping(value = "/{payableId}")
    public AjaxResult getInfo(@PathVariable("payableId") Long payableId)
    {
        return success(finPayableService.selectFinPayableByPayableId(payableId));
    }

    /**
     * 新增应付账款
     */
    @PreAuthorize("@ss.hasPermi('business:payable:add')")
    @Log(title = "应付账款", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FinPayable finPayable)
    {
        return toAjax(finPayableService.insertFinPayable(finPayable));
    }

    /**
     * 修改应付账款
     */
    @PreAuthorize("@ss.hasPermi('business:payable:edit')")
    @Log(title = "应付账款", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FinPayable finPayable)
    {
        return toAjax(finPayableService.updateFinPayable(finPayable));
    }

    /**
     * 删除应付账款
     */
    @PreAuthorize("@ss.hasPermi('business:payable:remove')")
    @Log(title = "应付账款", businessType = BusinessType.DELETE)
	@DeleteMapping("/{payableIds}")
    public AjaxResult remove(@PathVariable Long[] payableIds)
    {
        return toAjax(finPayableService.deleteFinPayableByPayableIds(payableIds));
    }
}