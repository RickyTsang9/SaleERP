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
import com.ruoyi.business.domain.WmsSaleOrderItem;
import com.ruoyi.business.service.IWmsSaleOrderItemService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/business/saleOrderItem")
public class WmsSaleOrderItemController extends BaseController
{
    @Autowired
    private IWmsSaleOrderItemService wmsSaleOrderItemService;

    @PreAuthorize("@ss.hasPermi('business:saleOrder:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmsSaleOrderItem wmsSaleOrderItem)
    {
        startPage();
        List<WmsSaleOrderItem> wmsSaleOrderItemList = wmsSaleOrderItemService.selectWmsSaleOrderItemList(wmsSaleOrderItem);
        return getDataTable(wmsSaleOrderItemList);
    }

    @PreAuthorize("@ss.hasPermi('business:saleOrder:export')")
    @Log(title = "销售订单明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(javax.servlet.http.HttpServletResponse response, WmsSaleOrderItem wmsSaleOrderItem)
    {
        List<WmsSaleOrderItem> wmsSaleOrderItemList = wmsSaleOrderItemService.selectWmsSaleOrderItemList(wmsSaleOrderItem);
        ExcelUtil<WmsSaleOrderItem> util = new ExcelUtil<WmsSaleOrderItem>(WmsSaleOrderItem.class);
        util.exportExcel(response, wmsSaleOrderItemList, "销售订单明细数据");
    }

    @PreAuthorize("@ss.hasPermi('business:saleOrder:query')")
    @GetMapping("/{saleOrderItemId}")
    public AjaxResult getInfo(@PathVariable Long saleOrderItemId)
    {
        return success(wmsSaleOrderItemService.selectWmsSaleOrderItemById(saleOrderItemId));
    }

    @PreAuthorize("@ss.hasPermi('business:saleOrder:add')")
    @Log(title = "销售订单明细", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public AjaxResult add(@RequestBody WmsSaleOrderItem wmsSaleOrderItem)
    {
        return toAjax(wmsSaleOrderItemService.insertWmsSaleOrderItem(wmsSaleOrderItem));
    }

    @PreAuthorize("@ss.hasPermi('business:saleOrder:edit')")
    @Log(title = "销售订单明细", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public AjaxResult edit(@RequestBody WmsSaleOrderItem wmsSaleOrderItem)
    {
        return toAjax(wmsSaleOrderItemService.updateWmsSaleOrderItem(wmsSaleOrderItem));
    }

    @PreAuthorize("@ss.hasPermi('business:saleOrder:remove')")
    @Log(title = "销售订单明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/{saleOrderItemIds}")
    public AjaxResult remove(@PathVariable Long[] saleOrderItemIds)
    {
        return toAjax(wmsSaleOrderItemService.deleteWmsSaleOrderItemByIds(saleOrderItemIds));
    }
}
