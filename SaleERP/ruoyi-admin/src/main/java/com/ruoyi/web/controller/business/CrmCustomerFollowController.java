package com.ruoyi.web.controller.business;

import java.util.List;
import java.util.Map;
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
import com.ruoyi.business.domain.CrmCustomerFollow;
import com.ruoyi.business.service.ICrmCustomerFollowService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 客户跟进记录Controller
 * 
 * @author ruoyi
 * @date 2026-03-23
 */
@RestController
@RequestMapping("/business/customerFollow")
public class CrmCustomerFollowController extends BaseController
{
    @Autowired
    private ICrmCustomerFollowService crmCustomerFollowService;

    /**
     * 查询客户跟进记录列表
     */
    @PreAuthorize("@ss.hasPermi('business:customerFollow:list')")
    @GetMapping("/list")
    public TableDataInfo list(CrmCustomerFollow crmCustomerFollow)
    {
        startPage();
        List<CrmCustomerFollow> list = crmCustomerFollowService.selectCrmCustomerFollowList(crmCustomerFollow);
        return getDataTable(list);
    }

    /**
     * 查询客户跟进待办摘要
     */
    @PreAuthorize("@ss.hasPermi('business:customerFollow:list')")
    @GetMapping("/todoSummary")
    public AjaxResult todoSummary(CrmCustomerFollow crmCustomerFollow)
    {
        Map<String, Object> todoSummaryMap = crmCustomerFollowService.selectCrmCustomerFollowTodoSummary(crmCustomerFollow);
        return AjaxResult.success(todoSummaryMap);
    }

    /**
     * 导出客户跟进记录列表
     */
    @PreAuthorize("@ss.hasPermi('business:customerFollow:export')")
    @Log(title = "客户跟进记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CrmCustomerFollow crmCustomerFollow)
    {
        List<CrmCustomerFollow> list = crmCustomerFollowService.selectCrmCustomerFollowList(crmCustomerFollow);
        ExcelUtil<CrmCustomerFollow> util = new ExcelUtil<CrmCustomerFollow>(CrmCustomerFollow.class);
        util.exportExcel(response, list, "客户跟进记录数据");
    }

    /**
     * 获取客户跟进记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('business:customerFollow:query')")
    @GetMapping(value = "/{followId:\\d+}")
    public AjaxResult getInfo(@PathVariable("followId") Long followId)
    {
        return success(crmCustomerFollowService.selectCrmCustomerFollowByFollowId(followId));
    }

    /**
     * 新增客户跟进记录
     */
    @PreAuthorize("@ss.hasPermi('business:customerFollow:add')")
    @Log(title = "客户跟进记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CrmCustomerFollow crmCustomerFollow)
    {
        return toAjax(crmCustomerFollowService.insertCrmCustomerFollow(crmCustomerFollow));
    }

    /**
     * 修改客户跟进记录
     */
    @PreAuthorize("@ss.hasPermi('business:customerFollow:edit')")
    @Log(title = "客户跟进记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CrmCustomerFollow crmCustomerFollow)
    {
        return toAjax(crmCustomerFollowService.updateCrmCustomerFollow(crmCustomerFollow));
    }

    /**
     * 删除客户跟进记录
     */
    @PreAuthorize("@ss.hasPermi('business:customerFollow:remove')")
    @Log(title = "客户跟进记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{followIds}")
    public AjaxResult remove(@PathVariable Long[] followIds)
    {
        return toAjax(crmCustomerFollowService.deleteCrmCustomerFollowByFollowIds(followIds));
    }
}
