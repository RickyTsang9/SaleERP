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
import com.ruoyi.business.domain.WmsSaleReturn;
import com.ruoyi.business.service.IWmsSaleReturnService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/business/saleReturn")
public class WmsSaleReturnController extends BaseController
{
    @Autowired
    private IWmsSaleReturnService wmsSaleReturnService;

    @PreAuthorize("@ss.hasPermi('business:saleReturn:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmsSaleReturn wmsSaleReturn)
    {
        startPage();
        List<WmsSaleReturn> wmsSaleReturnList = wmsSaleReturnService.selectWmsSaleReturnList(wmsSaleReturn);
        return getDataTable(wmsSaleReturnList);
    }

    @PreAuthorize("@ss.hasPermi('business:saleReturn:export')")
    @Log(title = "销售退货单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(javax.servlet.http.HttpServletResponse response, WmsSaleReturn wmsSaleReturn)
    {
        List<WmsSaleReturn> wmsSaleReturnList = wmsSaleReturnService.selectWmsSaleReturnList(wmsSaleReturn);
        ExcelUtil<WmsSaleReturn> util = new ExcelUtil<WmsSaleReturn>(WmsSaleReturn.class);
        util.exportExcel(response, wmsSaleReturnList, "销售退货单数据");
    }

    @PreAuthorize("@ss.hasPermi('business:saleReturn:query')")
    @GetMapping("/{saleReturnId}")
    public AjaxResult getInfo(@PathVariable Long saleReturnId)
    {
        return success(wmsSaleReturnService.selectWmsSaleReturnById(saleReturnId));
    }

    @PreAuthorize("@ss.hasPermi('business:saleReturn:add')")
    @Log(title = "销售退货单", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public AjaxResult add(@RequestBody WmsSaleReturn wmsSaleReturn)
    {
        wmsSaleReturn.setCreateBy(getUsername());
        return toAjax(wmsSaleReturnService.insertWmsSaleReturn(wmsSaleReturn));
    }

    @PreAuthorize("@ss.hasPermi('business:saleReturn:edit')")
    @Log(title = "销售退货单", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public AjaxResult edit(@RequestBody WmsSaleReturn wmsSaleReturn)
    {
        wmsSaleReturn.setUpdateBy(getUsername());
        return toAjax(wmsSaleReturnService.updateWmsSaleReturn(wmsSaleReturn));
    }

    @PreAuthorize("@ss.hasPermi('business:saleReturn:edit')")
    @Log(title = "销售退货单", businessType = BusinessType.UPDATE)
    @PostMapping("/submit/{saleReturnId}")
    public AjaxResult submit(@PathVariable Long saleReturnId)
    {
        return toAjax(wmsSaleReturnService.submitWmsSaleReturn(saleReturnId));
    }

    @PreAuthorize("@ss.hasPermi('business:saleReturn:audit')")
    @Log(title = "销售退货单", businessType = BusinessType.UPDATE)
    @PostMapping("/audit/{saleReturnId}")
    public AjaxResult audit(@PathVariable Long saleReturnId)
    {
        return toAjax(wmsSaleReturnService.auditWmsSaleReturn(saleReturnId));
    }

    @PreAuthorize("@ss.hasPermi('business:saleReturn:edit')")
    @Log(title = "销售退货单", businessType = BusinessType.UPDATE)
    @PostMapping("/cancel/{saleReturnId}")
    public AjaxResult cancel(@PathVariable Long saleReturnId)
    {
        return toAjax(wmsSaleReturnService.cancelWmsSaleReturn(saleReturnId));
    }

    @PreAuthorize("@ss.hasPermi('business:saleReturn:remove')")
    @Log(title = "销售退货单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{saleReturnIds}")
    public AjaxResult remove(@PathVariable Long[] saleReturnIds)
    {
        return toAjax(wmsSaleReturnService.deleteWmsSaleReturnByIds(saleReturnIds));
    }
}
