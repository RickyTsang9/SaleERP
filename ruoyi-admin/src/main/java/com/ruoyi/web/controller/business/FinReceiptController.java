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
import com.ruoyi.business.domain.FinReceipt;
import com.ruoyi.business.service.IFinReceiptService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/business/receipt")
public class FinReceiptController extends BaseController
{
    @Autowired
    private IFinReceiptService finReceiptService;

    @PreAuthorize("@ss.hasPermi('business:receipt:list')")
    @GetMapping("/list")
    public TableDataInfo list(FinReceipt finReceipt)
    {
        startPage();
        List<FinReceipt> finReceiptList = finReceiptService.selectFinReceiptList(finReceipt);
        return getDataTable(finReceiptList);
    }

    @PreAuthorize("@ss.hasPermi('business:receipt:export')")
    @Log(title = "回款登记", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(jakarta.servlet.http.HttpServletResponse response, FinReceipt finReceipt)
    {
        List<FinReceipt> finReceiptList = finReceiptService.selectFinReceiptList(finReceipt);
        ExcelUtil<FinReceipt> util = new ExcelUtil<FinReceipt>(FinReceipt.class);
        util.exportExcel(response, finReceiptList, "回款登记数据");
    }

    @PreAuthorize("@ss.hasPermi('business:receipt:query')")
    @GetMapping("/{receiptId}")
    public AjaxResult getInfo(@PathVariable Long receiptId)
    {
        return success(finReceiptService.selectFinReceiptById(receiptId));
    }

    @PreAuthorize("@ss.hasPermi('business:receipt:add')")
    @Log(title = "回款登记", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public AjaxResult add(@RequestBody FinReceipt finReceipt)
    {
        finReceipt.setCreateBy(getUsername());
        return toAjax(finReceiptService.insertFinReceipt(finReceipt));
    }

    @PreAuthorize("@ss.hasPermi('business:receipt:edit')")
    @Log(title = "回款登记", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public AjaxResult edit(@RequestBody FinReceipt finReceipt)
    {
        finReceipt.setUpdateBy(getUsername());
        return toAjax(finReceiptService.updateFinReceipt(finReceipt));
    }

    @PreAuthorize("@ss.hasPermi('business:receipt:remove')")
    @Log(title = "回款登记", businessType = BusinessType.DELETE)
    @DeleteMapping("/{receiptIds}")
    public AjaxResult remove(@PathVariable Long[] receiptIds)
    {
        return toAjax(finReceiptService.deleteFinReceiptByIds(receiptIds));
    }
}
