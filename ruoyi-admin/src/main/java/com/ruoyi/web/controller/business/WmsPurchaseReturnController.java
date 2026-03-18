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
import com.ruoyi.business.domain.WmsPurchaseReturn;
import com.ruoyi.business.service.IWmsPurchaseReturnService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/business/purchaseReturn")
public class WmsPurchaseReturnController extends BaseController
{
    @Autowired
    private IWmsPurchaseReturnService wmsPurchaseReturnService;

    @PreAuthorize("@ss.hasPermi('business:purchaseReturn:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmsPurchaseReturn wmsPurchaseReturn)
    {
        startPage();
        List<WmsPurchaseReturn> wmsPurchaseReturnList = wmsPurchaseReturnService.selectWmsPurchaseReturnList(wmsPurchaseReturn);
        return getDataTable(wmsPurchaseReturnList);
    }

    @PreAuthorize("@ss.hasPermi('business:purchaseReturn:export')")
    @Log(title = "采购退货单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(jakarta.servlet.http.HttpServletResponse response, WmsPurchaseReturn wmsPurchaseReturn)
    {
        List<WmsPurchaseReturn> wmsPurchaseReturnList = wmsPurchaseReturnService.selectWmsPurchaseReturnList(wmsPurchaseReturn);
        ExcelUtil<WmsPurchaseReturn> util = new ExcelUtil<WmsPurchaseReturn>(WmsPurchaseReturn.class);
        util.exportExcel(response, wmsPurchaseReturnList, "采购退货单数据");
    }

    @PreAuthorize("@ss.hasPermi('business:purchaseReturn:query')")
    @GetMapping("/{purchaseReturnId}")
    public AjaxResult getInfo(@PathVariable Long purchaseReturnId)
    {
        return success(wmsPurchaseReturnService.selectWmsPurchaseReturnById(purchaseReturnId));
    }

    @PreAuthorize("@ss.hasPermi('business:purchaseReturn:add')")
    @Log(title = "采购退货单", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public AjaxResult add(@RequestBody WmsPurchaseReturn wmsPurchaseReturn)
    {
        wmsPurchaseReturn.setCreateBy(getUsername());
        return toAjax(wmsPurchaseReturnService.insertWmsPurchaseReturn(wmsPurchaseReturn));
    }

    @PreAuthorize("@ss.hasPermi('business:purchaseReturn:edit')")
    @Log(title = "采购退货单", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public AjaxResult edit(@RequestBody WmsPurchaseReturn wmsPurchaseReturn)
    {
        wmsPurchaseReturn.setUpdateBy(getUsername());
        return toAjax(wmsPurchaseReturnService.updateWmsPurchaseReturn(wmsPurchaseReturn));
    }

    @PreAuthorize("@ss.hasPermi('business:purchaseReturn:remove')")
    @Log(title = "采购退货单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{purchaseReturnIds}")
    public AjaxResult remove(@PathVariable Long[] purchaseReturnIds)
    {
        return toAjax(wmsPurchaseReturnService.deleteWmsPurchaseReturnByIds(purchaseReturnIds));
    }
}
