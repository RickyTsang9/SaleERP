package com.ruoyi.web.controller.business;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.business.domain.WmsSaleOrder;
import com.ruoyi.business.service.IWmsSaleOrderService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

@RestController
@RequestMapping("/business/saleOrder")
public class WmsSaleOrderController extends BaseController
{
    @Autowired
    private IWmsSaleOrderService wmsSaleOrderService;

    @PreAuthorize("@ss.hasPermi('business:saleOrder:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmsSaleOrder wmsSaleOrder)
    {
        startPage();
        List<WmsSaleOrder> wmsSaleOrderList = wmsSaleOrderService.selectWmsSaleOrderList(wmsSaleOrder);
        return getDataTable(wmsSaleOrderList);
    }

    @PreAuthorize("@ss.hasPermi('business:saleOrder:export')")
    @Log(title = "销售订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmsSaleOrder wmsSaleOrder)
    {
        List<WmsSaleOrder> wmsSaleOrderList = wmsSaleOrderService.selectWmsSaleOrderList(wmsSaleOrder);
        ExcelUtil<WmsSaleOrder> util = new ExcelUtil<WmsSaleOrder>(WmsSaleOrder.class);
        util.exportExcel(response, wmsSaleOrderList, "销售订单数据");
    }

    /**
     * 导入销售订单数据
     */
    @Log(title = "销售订单", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('business:saleOrder:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<WmsSaleOrder> excelUtil = new ExcelUtil<WmsSaleOrder>(WmsSaleOrder.class);
        List<WmsSaleOrder> saleOrderList = excelUtil.importExcel(file.getInputStream());
        String operationMessage = wmsSaleOrderService.importWmsSaleOrder(saleOrderList, updateSupport, getUsername());
        return success(operationMessage);
    }

    /**
     * 下载导入模板
     */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<WmsSaleOrder> excelUtil = new ExcelUtil<WmsSaleOrder>(WmsSaleOrder.class);
        excelUtil.importTemplateExcel(response, "销售订单模板");
    }

    @PreAuthorize("@ss.hasPermi('business:saleOrder:query')")
    @GetMapping("/{saleOrderId}")
    public AjaxResult getInfo(@PathVariable Long saleOrderId)
    {
        return success(wmsSaleOrderService.selectWmsSaleOrderById(saleOrderId));
    }

    /**
     * 获取打印模板数据
     */
    @PreAuthorize("@ss.hasPermi('business:saleOrder:print')")
    @GetMapping("/printTemplate/{saleOrderId}")
    public AjaxResult printTemplate(@PathVariable Long saleOrderId)
    {
        return success(wmsSaleOrderService.getPrintTemplateData(saleOrderId));
    }

    @PreAuthorize("@ss.hasPermi('business:saleOrder:add')")
    @Log(title = "销售订单", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public AjaxResult add(@RequestBody WmsSaleOrder wmsSaleOrder)
    {
        wmsSaleOrder.setCreateBy(getUsername());
        return toAjax(wmsSaleOrderService.insertWmsSaleOrder(wmsSaleOrder));
    }

    @PreAuthorize("@ss.hasPermi('business:saleOrder:edit')")
    @Log(title = "销售订单", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public AjaxResult edit(@RequestBody WmsSaleOrder wmsSaleOrder)
    {
        wmsSaleOrder.setUpdateBy(getUsername());
        return toAjax(wmsSaleOrderService.updateWmsSaleOrder(wmsSaleOrder));
    }

    @PreAuthorize("@ss.hasPermi('business:saleOrder:submit')")
    @Log(title = "销售订单", businessType = BusinessType.UPDATE)
    @PostMapping("/submit/{saleOrderId}")
    public AjaxResult submit(@PathVariable Long saleOrderId)
    {
        return toAjax(wmsSaleOrderService.submitWmsSaleOrder(saleOrderId));
    }

    /**
     * 经理审核销售单
     */
    @PreAuthorize("@ss.hasPermi('business:saleOrder:managerAudit')")
    @Log(title = "销售订单", businessType = BusinessType.UPDATE)
    @PostMapping("/managerAudit/{saleOrderId}")
    public AjaxResult managerAudit(@PathVariable Long saleOrderId, @RequestBody(required = false) WmsSaleOrder wmsSaleOrder)
    {
        String auditComment = wmsSaleOrder == null ? null : wmsSaleOrder.getManagerAuditComment();
        return toAjax(wmsSaleOrderService.managerAuditWmsSaleOrder(saleOrderId, auditComment));
    }

    /**
     * 经理回退销售单
     */
    @PreAuthorize("@ss.hasPermi('business:saleOrder:managerAudit')")
    @Log(title = "销售订单", businessType = BusinessType.UPDATE)
    @PostMapping("/managerRollback/{saleOrderId}")
    public AjaxResult managerRollback(@PathVariable Long saleOrderId, @RequestBody(required = false) WmsSaleOrder wmsSaleOrder)
    {
        String auditComment = wmsSaleOrder == null ? null : wmsSaleOrder.getManagerAuditComment();
        return toAjax(wmsSaleOrderService.managerRollbackWmsSaleOrder(saleOrderId, auditComment));
    }

    /**
     * 财务审核销售单
     */
    @PreAuthorize("@ss.hasPermi('business:saleOrder:financeAudit')")
    @Log(title = "销售订单", businessType = BusinessType.UPDATE)
    @PostMapping("/financeAudit/{saleOrderId}")
    public AjaxResult financeAudit(@PathVariable Long saleOrderId, @RequestBody(required = false) WmsSaleOrder wmsSaleOrder)
    {
        String auditComment = wmsSaleOrder == null ? null : wmsSaleOrder.getFinanceAuditComment();
        return toAjax(wmsSaleOrderService.financeAuditWmsSaleOrder(saleOrderId, auditComment));
    }

    /**
     * 财务回退销售单
     */
    @PreAuthorize("@ss.hasPermi('business:saleOrder:financeAudit')")
    @Log(title = "销售订单", businessType = BusinessType.UPDATE)
    @PostMapping("/financeRollback/{saleOrderId}")
    public AjaxResult financeRollback(@PathVariable Long saleOrderId, @RequestBody(required = false) WmsSaleOrder wmsSaleOrder)
    {
        String auditComment = wmsSaleOrder == null ? null : wmsSaleOrder.getFinanceAuditComment();
        return toAjax(wmsSaleOrderService.financeRollbackWmsSaleOrder(saleOrderId, auditComment));
    }

    /**
     * 查询销售单状态历史
     */
    @PreAuthorize("@ss.hasPermi('business:saleOrder:query')")
    @GetMapping("/statusHistory/{saleOrderId}")
    public AjaxResult statusHistory(@PathVariable Long saleOrderId)
    {
        return success(wmsSaleOrderService.selectWmsSaleOrderStatusHistoryList(saleOrderId));
    }

    @PreAuthorize("@ss.hasPermi('business:saleOrder:edit')")
    @Log(title = "销售订单", businessType = BusinessType.UPDATE)
    @PostMapping("/cancel/{saleOrderId}")
    public AjaxResult cancel(@PathVariable Long saleOrderId)
    {
        return toAjax(wmsSaleOrderService.cancelWmsSaleOrder(saleOrderId));
    }

    @PreAuthorize("@ss.hasPermi('business:saleOrder:remove')")
    @Log(title = "销售订单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{saleOrderIds}")
    public AjaxResult remove(@PathVariable Long[] saleOrderIds)
    {
        return toAjax(wmsSaleOrderService.deleteWmsSaleOrderByIds(saleOrderIds));
    }
}
