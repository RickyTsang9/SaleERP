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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.FinReceivable;
import com.ruoyi.business.service.IFinReceivableService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/business/receivable")
public class FinReceivableController extends BaseController
{
    @Autowired
    private IFinReceivableService finReceivableService;

    @PreAuthorize("@ss.hasPermi('business:receivable:list')")
    @GetMapping("/list")
    public TableDataInfo list(FinReceivable finReceivable)
    {
        startPage();
        List<FinReceivable> finReceivableList = finReceivableService.selectFinReceivableList(finReceivable);
        return getDataTable(finReceivableList);
    }

    @PreAuthorize("@ss.hasPermi('business:receivable:due')")
    @GetMapping("/due/list")
    public TableDataInfo dueList(@RequestParam(value = "remindDays", required = false) Integer remindDays)
    {
        startPage();
        List<FinReceivable> finReceivableList = finReceivableService.selectFinReceivableDueReminderList(remindDays);
        return getDataTable(finReceivableList);
    }

    @PreAuthorize("@ss.hasPermi('business:receivable:overdue')")
    @GetMapping("/overdue/list")
    public TableDataInfo overdueList()
    {
        startPage();
        List<FinReceivable> finReceivableList = finReceivableService.selectFinReceivableOverdueList();
        return getDataTable(finReceivableList);
    }

    @PreAuthorize("@ss.hasPermi('business:receivable:export')")
    @Log(title = "应收台账", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(javax.servlet.http.HttpServletResponse response, FinReceivable finReceivable)
    {
        List<FinReceivable> finReceivableList = finReceivableService.selectFinReceivableList(finReceivable);
        ExcelUtil<FinReceivable> util = new ExcelUtil<FinReceivable>(FinReceivable.class);
        util.exportExcel(response, finReceivableList, "应收台账数据");
    }

    @PreAuthorize("@ss.hasPermi('business:receivable:query')")
    @GetMapping("/{receivableId}")
    public AjaxResult getInfo(@PathVariable Long receivableId)
    {
        return success(finReceivableService.selectFinReceivableById(receivableId));
    }

    @PreAuthorize("@ss.hasPermi('business:receivable:add')")
    @Log(title = "应收台账", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public AjaxResult add(@RequestBody FinReceivable finReceivable)
    {
        finReceivable.setCreateBy(getUsername());
        return toAjax(finReceivableService.insertFinReceivable(finReceivable));
    }

    @PreAuthorize("@ss.hasPermi('business:receivable:edit')")
    @Log(title = "应收台账", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public AjaxResult edit(@RequestBody FinReceivable finReceivable)
    {
        finReceivable.setUpdateBy(getUsername());
        return toAjax(finReceivableService.updateFinReceivable(finReceivable));
    }

    @PreAuthorize("@ss.hasPermi('business:receivable:remove')")
    @Log(title = "应收台账", businessType = BusinessType.DELETE)
    @DeleteMapping("/{receivableIds}")
    public AjaxResult remove(@PathVariable Long[] receivableIds)
    {
        return toAjax(finReceivableService.deleteFinReceivableByIds(receivableIds));
    }
}
