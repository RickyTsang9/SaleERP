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
import com.ruoyi.business.domain.WmsTransfer;
import com.ruoyi.business.service.IWmsTransferService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 库存调拨Controller
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
@RestController
@RequestMapping("/business/transfer")
public class WmsTransferController extends BaseController
{
    @Autowired
    private IWmsTransferService wmsTransferService;

    /**
     * 查询库存调拨列表
     */
    @PreAuthorize("@ss.hasPermi('business:transfer:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmsTransfer wmsTransfer)
    {
        startPage();
        List<WmsTransfer> list = wmsTransferService.selectWmsTransferList(wmsTransfer);
        return getDataTable(list);
    }

    /**
     * 导出库存调拨列表
     */
    @PreAuthorize("@ss.hasPermi('business:transfer:export')")
    @Log(title = "库存调拨", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmsTransfer wmsTransfer)
    {
        List<WmsTransfer> list = wmsTransferService.selectWmsTransferList(wmsTransfer);
        ExcelUtil<WmsTransfer> util = new ExcelUtil<WmsTransfer>(WmsTransfer.class);
        util.exportExcel(response, list, "库存调拨数据");
    }

    /**
     * 获取库存调拨详细信息
     */
    @PreAuthorize("@ss.hasPermi('business:transfer:query')")
    @GetMapping(value = "/{transferId}")
    public AjaxResult getInfo(@PathVariable("transferId") Long transferId)
    {
        return success(wmsTransferService.selectWmsTransferByTransferId(transferId));
    }

    /**
     * 新增库存调拨
     */
    @PreAuthorize("@ss.hasPermi('business:transfer:add')")
    @Log(title = "库存调拨", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody WmsTransfer wmsTransfer)
    {
        return toAjax(wmsTransferService.insertWmsTransfer(wmsTransfer));
    }

    /**
     * 修改库存调拨
     */
    @PreAuthorize("@ss.hasPermi('business:transfer:edit')")
    @Log(title = "库存调拨", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody WmsTransfer wmsTransfer)
    {
        return toAjax(wmsTransferService.updateWmsTransfer(wmsTransfer));
    }

    /**
     * 提交库存调拨
     */
    @PreAuthorize("@ss.hasPermi('business:transfer:edit')")
    @Log(title = "库存调拨", businessType = BusinessType.UPDATE)
    @PostMapping("/submit/{transferId}")
    public AjaxResult submit(@PathVariable Long transferId)
    {
        return toAjax(wmsTransferService.submitWmsTransfer(transferId));
    }

    /**
     * 审核库存调拨
     */
    @PreAuthorize("@ss.hasPermi('business:transfer:audit')")
    @Log(title = "库存调拨", businessType = BusinessType.UPDATE)
    @PostMapping("/audit/{transferId}")
    public AjaxResult audit(@PathVariable Long transferId)
    {
        return toAjax(wmsTransferService.auditWmsTransfer(transferId));
    }

    /**
     * 作废库存调拨
     */
    @PreAuthorize("@ss.hasPermi('business:transfer:edit')")
    @Log(title = "库存调拨", businessType = BusinessType.UPDATE)
    @PostMapping("/cancel/{transferId}")
    public AjaxResult cancel(@PathVariable Long transferId)
    {
        return toAjax(wmsTransferService.cancelWmsTransfer(transferId));
    }

    /**
     * 删除库存调拨
     */
    @PreAuthorize("@ss.hasPermi('business:transfer:remove')")
    @Log(title = "库存调拨", businessType = BusinessType.DELETE)
	@DeleteMapping("/{transferIds}")
    public AjaxResult remove(@PathVariable Long[] transferIds)
    {
        return toAjax(wmsTransferService.deleteWmsTransferByTransferIds(transferIds));
    }
}
