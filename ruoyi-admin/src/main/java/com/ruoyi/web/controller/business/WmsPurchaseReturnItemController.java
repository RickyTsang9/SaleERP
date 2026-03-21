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
import com.ruoyi.business.domain.WmsPurchaseReturnItem;
import com.ruoyi.business.service.IWmsPurchaseReturnItemService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/business/purchaseReturnItem")
public class WmsPurchaseReturnItemController extends BaseController
{
    @Autowired
    private IWmsPurchaseReturnItemService wmsPurchaseReturnItemService;

    @PreAuthorize("@ss.hasPermi('business:purchaseReturn:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmsPurchaseReturnItem wmsPurchaseReturnItem)
    {
        startPage();
        List<WmsPurchaseReturnItem> wmsPurchaseReturnItemList = wmsPurchaseReturnItemService.selectWmsPurchaseReturnItemList(wmsPurchaseReturnItem);
        return getDataTable(wmsPurchaseReturnItemList);
    }

    @PreAuthorize("@ss.hasPermi('business:purchaseReturn:export')")
    @Log(title = "采购退货明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(javax.servlet.http.HttpServletResponse response, WmsPurchaseReturnItem wmsPurchaseReturnItem)
    {
        List<WmsPurchaseReturnItem> wmsPurchaseReturnItemList = wmsPurchaseReturnItemService.selectWmsPurchaseReturnItemList(wmsPurchaseReturnItem);
        ExcelUtil<WmsPurchaseReturnItem> util = new ExcelUtil<WmsPurchaseReturnItem>(WmsPurchaseReturnItem.class);
        util.exportExcel(response, wmsPurchaseReturnItemList, "采购退货明细数据");
    }

    @PreAuthorize("@ss.hasPermi('business:purchaseReturn:query')")
    @GetMapping("/{purchaseReturnItemId}")
    public AjaxResult getInfo(@PathVariable Long purchaseReturnItemId)
    {
        return success(wmsPurchaseReturnItemService.selectWmsPurchaseReturnItemById(purchaseReturnItemId));
    }

    @PreAuthorize("@ss.hasPermi('business:purchaseReturn:add')")
    @Log(title = "采购退货明细", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public AjaxResult add(@RequestBody WmsPurchaseReturnItem wmsPurchaseReturnItem)
    {
        return toAjax(wmsPurchaseReturnItemService.insertWmsPurchaseReturnItem(wmsPurchaseReturnItem));
    }

    @PreAuthorize("@ss.hasPermi('business:purchaseReturn:edit')")
    @Log(title = "采购退货明细", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public AjaxResult edit(@RequestBody WmsPurchaseReturnItem wmsPurchaseReturnItem)
    {
        return toAjax(wmsPurchaseReturnItemService.updateWmsPurchaseReturnItem(wmsPurchaseReturnItem));
    }

    @PreAuthorize("@ss.hasPermi('business:purchaseReturn:remove')")
    @Log(title = "采购退货明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/{purchaseReturnItemIds}")
    public AjaxResult remove(@PathVariable Long[] purchaseReturnItemIds)
    {
        return toAjax(wmsPurchaseReturnItemService.deleteWmsPurchaseReturnItemByIds(purchaseReturnItemIds));
    }
}
