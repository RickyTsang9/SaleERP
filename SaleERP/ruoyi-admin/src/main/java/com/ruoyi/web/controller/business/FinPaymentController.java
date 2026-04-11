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
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.business.domain.FinPayment;
import com.ruoyi.business.service.IFinPaymentService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 付款记录Controller
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
@RestController
@RequestMapping("/business/payment")
public class FinPaymentController extends BaseController
{
    @Autowired
    private IFinPaymentService finPaymentService;

    /**
     * 查询付款记录列表
     */
    @PreAuthorize("@ss.hasPermi('business:payment:list')")
    @GetMapping("/list")
    public TableDataInfo list(FinPayment finPayment)
    {
        startPage();
        List<FinPayment> list = finPaymentService.selectFinPaymentList(finPayment);
        return getDataTable(list);
    }

    /**
     * 导出付款记录列表
     */
    @PreAuthorize("@ss.hasPermi('business:payment:export')")
    @Log(title = "付款记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, FinPayment finPayment)
    {
        List<FinPayment> list = finPaymentService.selectFinPaymentList(finPayment);
        ExcelUtil<FinPayment> util = new ExcelUtil<FinPayment>(FinPayment.class);
        util.exportExcel(response, list, "付款记录数据");
    }

    /**
     * 获取付款记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('business:payment:query')")
    @GetMapping(value = "/{paymentId}")
    public AjaxResult getInfo(@PathVariable("paymentId") Long paymentId)
    {
        return success(finPaymentService.selectFinPaymentByPaymentId(paymentId));
    }

    /**
     * 新增付款记录
     */
    @PreAuthorize("@ss.hasPermi('business:payment:add')")
    @Log(title = "付款记录", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public AjaxResult add(@RequestBody FinPayment finPayment)
    {
        return toAjax(finPaymentService.insertFinPayment(finPayment));
    }

    /**
     * 修改付款记录
     */
    @PreAuthorize("@ss.hasPermi('business:payment:edit')")
    @Log(title = "付款记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FinPayment finPayment)
    {
        return toAjax(finPaymentService.updateFinPayment(finPayment));
    }

    /**
     * 删除付款记录
     */
    @PreAuthorize("@ss.hasPermi('business:payment:remove')")
    @Log(title = "付款记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{paymentIds}")
    public AjaxResult remove(@PathVariable Long[] paymentIds)
    {
        return toAjax(finPaymentService.deleteFinPaymentByPaymentIds(paymentIds));
    }
}
