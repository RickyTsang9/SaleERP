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
import com.ruoyi.business.domain.WmsPurchaseOrder;
import com.ruoyi.business.service.IWmsPurchaseOrderService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 采购订单Controller
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
@RestController
@RequestMapping("/business/purchaseOrder")
public class WmsPurchaseOrderController extends BaseController
{
    @Autowired
    private IWmsPurchaseOrderService wmsPurchaseOrderService;

    /**
     * 查询采购订单列表
     */
    @PreAuthorize("@ss.hasPermi('business:purchaseOrder:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmsPurchaseOrder wmsPurchaseOrder)
    {
        startPage();
        List<WmsPurchaseOrder> list = wmsPurchaseOrderService.selectWmsPurchaseOrderList(wmsPurchaseOrder);
        return getDataTable(list);
    }

    /**
     * 导出采购订单列表
     */
    @PreAuthorize("@ss.hasPermi('business:purchaseOrder:export')")
    @Log(title = "采购订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmsPurchaseOrder wmsPurchaseOrder)
    {
        List<WmsPurchaseOrder> list = wmsPurchaseOrderService.selectWmsPurchaseOrderList(wmsPurchaseOrder);
        ExcelUtil<WmsPurchaseOrder> util = new ExcelUtil<WmsPurchaseOrder>(WmsPurchaseOrder.class);
        util.exportExcel(response, list, "采购订单数据");
    }

    /**
     * 获取采购订单详细信息
     */
    @PreAuthorize("@ss.hasPermi('business:purchaseOrder:query')")
    @GetMapping(value = "/{purchaseOrderId}")
    public AjaxResult getInfo(@PathVariable("purchaseOrderId") Long purchaseOrderId)
    {
        return success(wmsPurchaseOrderService.selectWmsPurchaseOrderByPurchaseOrderId(purchaseOrderId));
    }

    /**
     * 新增采购订单
     */
    @PreAuthorize("@ss.hasPermi('business:purchaseOrder:add')")
    @Log(title = "采购订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody WmsPurchaseOrder wmsPurchaseOrder)
    {
        return toAjax(wmsPurchaseOrderService.insertWmsPurchaseOrder(wmsPurchaseOrder));
    }

    /**
     * 修改采购订单
     */
    @PreAuthorize("@ss.hasPermi('business:purchaseOrder:edit')")
    @Log(title = "采购订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody WmsPurchaseOrder wmsPurchaseOrder)
    {
        return toAjax(wmsPurchaseOrderService.updateWmsPurchaseOrder(wmsPurchaseOrder));
    }

    /**
     * 提交采购订单
     */
    @PreAuthorize("@ss.hasPermi('business:purchaseOrder:edit')")
    @Log(title = "采购订单", businessType = BusinessType.UPDATE)
    @PostMapping("/submit/{purchaseOrderId}")
    public AjaxResult submit(@PathVariable Long purchaseOrderId)
    {
        return toAjax(wmsPurchaseOrderService.submitWmsPurchaseOrder(purchaseOrderId));
    }

    /**
     * 审核采购订单
     */
    @PreAuthorize("@ss.hasPermi('business:purchaseOrder:audit')")
    @Log(title = "采购订单", businessType = BusinessType.UPDATE)
    @PostMapping("/audit/{purchaseOrderId}")
    public AjaxResult audit(@PathVariable Long purchaseOrderId)
    {
        return toAjax(wmsPurchaseOrderService.auditWmsPurchaseOrder(purchaseOrderId));
    }

    /**
     * 作废采购订单
     */
    @PreAuthorize("@ss.hasPermi('business:purchaseOrder:edit')")
    @Log(title = "采购订单", businessType = BusinessType.UPDATE)
    @PostMapping("/cancel/{purchaseOrderId}")
    public AjaxResult cancel(@PathVariable Long purchaseOrderId)
    {
        return toAjax(wmsPurchaseOrderService.cancelWmsPurchaseOrder(purchaseOrderId));
    }

    /**
     * 删除采购订单
     */
    @PreAuthorize("@ss.hasPermi('business:purchaseOrder:remove')")
    @Log(title = "采购订单", businessType = BusinessType.DELETE)
	@DeleteMapping("/{purchaseOrderIds}")
    public AjaxResult remove(@PathVariable Long[] purchaseOrderIds)
    {
        return toAjax(wmsPurchaseOrderService.deleteWmsPurchaseOrderByPurchaseOrderIds(purchaseOrderIds));
    }
}
