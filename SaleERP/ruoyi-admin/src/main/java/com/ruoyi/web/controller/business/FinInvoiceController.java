package com.ruoyi.web.controller.business;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.FinInvoice;
import com.ruoyi.business.service.impl.FinInvoiceService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/business/invoice")
public class FinInvoiceController extends BaseController
{
    @Autowired
    private FinInvoiceService finInvoiceService;

    @PreAuthorize("@ss.hasPermi('business:invoice:list')")
    @GetMapping("/list")
    public TableDataInfo list(FinInvoice finInvoice)
    {
        startPage();
        List<FinInvoice> list = finInvoiceService.selectFinInvoiceList(finInvoice);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('business:invoice:list')")
    @GetMapping("/summary")
    public AjaxResult summary(FinInvoice finInvoice)
    {
        return success(finInvoiceService.selectFinInvoiceSummary(finInvoice));
    }

    @PreAuthorize("@ss.hasAnyPermi('business:invoice:list,business:invoice:add,business:invoice:edit')")
    @GetMapping("/sourceOptions")
    public AjaxResult sourceOptions()
    {
        return success(finInvoiceService.getInvoiceSourceOptions());
    }

    @PreAuthorize("@ss.hasPermi('business:invoice:query')")
    @GetMapping("/{invoiceId}")
    public AjaxResult getInfo(@PathVariable Long invoiceId)
    {
        return success(finInvoiceService.selectFinInvoiceById(invoiceId));
    }

    @PreAuthorize("@ss.hasPermi('business:invoice:add')")
    @Log(title = "发票管理", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public AjaxResult add(@RequestBody FinInvoice finInvoice)
    {
        finInvoice.setCreateBy(getUsername());
        return toAjax(finInvoiceService.insertFinInvoice(finInvoice));
    }

    @PreAuthorize("@ss.hasPermi('business:invoice:edit')")
    @Log(title = "发票管理", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public AjaxResult edit(@RequestBody FinInvoice finInvoice)
    {
        finInvoice.setUpdateBy(getUsername());
        return toAjax(finInvoiceService.updateFinInvoice(finInvoice));
    }

    @PreAuthorize("@ss.hasPermi('business:invoice:void')")
    @Log(title = "发票红冲作废", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping("/{invoiceId}/void")
    public AjaxResult voidInvoice(@PathVariable Long invoiceId, @RequestBody(required = false) Map<String, String> body)
    {
        String voidReason = body == null ? null : body.get("voidReason");
        return toAjax(finInvoiceService.voidFinInvoice(invoiceId, voidReason, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('business:invoice:export')")
    @Log(title = "发票管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, FinInvoice finInvoice)
    {
        List<FinInvoice> list = finInvoiceService.selectFinInvoiceList(finInvoice);
        ExcelUtil<FinInvoice> util = new ExcelUtil<FinInvoice>(FinInvoice.class);
        util.exportExcel(response, list, "发票税务台账");
    }
}
