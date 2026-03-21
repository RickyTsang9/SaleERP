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
import com.ruoyi.business.domain.WmsSaleReturnItem;
import com.ruoyi.business.service.IWmsSaleReturnItemService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/business/saleReturnItem")
public class WmsSaleReturnItemController extends BaseController
{
    @Autowired
    private IWmsSaleReturnItemService wmsSaleReturnItemService;

    @PreAuthorize("@ss.hasPermi('business:saleReturn:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmsSaleReturnItem wmsSaleReturnItem)
    {
        startPage();
        List<WmsSaleReturnItem> wmsSaleReturnItemList = wmsSaleReturnItemService.selectWmsSaleReturnItemList(wmsSaleReturnItem);
        return getDataTable(wmsSaleReturnItemList);
    }

    @PreAuthorize("@ss.hasPermi('business:saleReturn:export')")
    @Log(title = "销售退货明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(javax.servlet.http.HttpServletResponse response, WmsSaleReturnItem wmsSaleReturnItem)
    {
        List<WmsSaleReturnItem> wmsSaleReturnItemList = wmsSaleReturnItemService.selectWmsSaleReturnItemList(wmsSaleReturnItem);
        ExcelUtil<WmsSaleReturnItem> util = new ExcelUtil<WmsSaleReturnItem>(WmsSaleReturnItem.class);
        util.exportExcel(response, wmsSaleReturnItemList, "销售退货明细数据");
    }

    @PreAuthorize("@ss.hasPermi('business:saleReturn:query')")
    @GetMapping("/{saleReturnItemId}")
    public AjaxResult getInfo(@PathVariable Long saleReturnItemId)
    {
        return success(wmsSaleReturnItemService.selectWmsSaleReturnItemById(saleReturnItemId));
    }

    @PreAuthorize("@ss.hasPermi('business:saleReturn:add')")
    @Log(title = "销售退货明细", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public AjaxResult add(@RequestBody WmsSaleReturnItem wmsSaleReturnItem)
    {
        return toAjax(wmsSaleReturnItemService.insertWmsSaleReturnItem(wmsSaleReturnItem));
    }

    @PreAuthorize("@ss.hasPermi('business:saleReturn:edit')")
    @Log(title = "销售退货明细", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public AjaxResult edit(@RequestBody WmsSaleReturnItem wmsSaleReturnItem)
    {
        return toAjax(wmsSaleReturnItemService.updateWmsSaleReturnItem(wmsSaleReturnItem));
    }

    @PreAuthorize("@ss.hasPermi('business:saleReturn:remove')")
    @Log(title = "销售退货明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/{saleReturnItemIds}")
    public AjaxResult remove(@PathVariable Long[] saleReturnItemIds)
    {
        return toAjax(wmsSaleReturnItemService.deleteWmsSaleReturnItemByIds(saleReturnItemIds));
    }
}
