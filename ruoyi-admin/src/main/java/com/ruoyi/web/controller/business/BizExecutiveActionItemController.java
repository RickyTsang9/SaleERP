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
import com.ruoyi.business.domain.BizExecutiveActionItem;
import com.ruoyi.business.service.IBizExecutiveActionItemService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * 管理层经营决议事项Controller
 * 
 * @author ruoyi
 * @date 2026-04-12
 */
@RestController
@RequestMapping("/business/executiveActionItem")
public class BizExecutiveActionItemController extends BaseController
{
    @Autowired
    private IBizExecutiveActionItemService bizExecutiveActionItemService;

    /**
     * 查询经营决议事项列表
     */
    @PreAuthorize("@ss.hasPermi('business:report:view')")
    @GetMapping("/list")
    public TableDataInfo list(BizExecutiveActionItem bizExecutiveActionItem)
    {
        startPage();
        List<BizExecutiveActionItem> executiveActionItemList =
            bizExecutiveActionItemService.selectBizExecutiveActionItemList(bizExecutiveActionItem);
        return getDataTable(executiveActionItemList);
    }

    /**
     * 查询经营决议事项详情
     */
    @PreAuthorize("@ss.hasPermi('business:report:view')")
    @GetMapping("/{actionItemId}")
    public AjaxResult getInfo(@PathVariable Long actionItemId)
    {
        return success(bizExecutiveActionItemService.selectBizExecutiveActionItemByActionItemId(actionItemId));
    }

    /**
     * 新增经营决议事项
     */
    @PreAuthorize("@ss.hasPermi('business:report:edit')")
    @Log(title = "经营决议事项", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizExecutiveActionItem bizExecutiveActionItem)
    {
        return toAjax(bizExecutiveActionItemService.insertBizExecutiveActionItem(bizExecutiveActionItem, getUsername()));
    }

    /**
     * 修改经营决议事项
     */
    @PreAuthorize("@ss.hasPermi('business:report:edit')")
    @Log(title = "经营决议事项", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BizExecutiveActionItem bizExecutiveActionItem)
    {
        return toAjax(bizExecutiveActionItemService.updateBizExecutiveActionItem(bizExecutiveActionItem, getUsername()));
    }

    /**
     * 删除经营决议事项
     */
    @PreAuthorize("@ss.hasPermi('business:report:edit')")
    @Log(title = "经营决议事项", businessType = BusinessType.DELETE)
    @DeleteMapping("/{actionItemIds}")
    public AjaxResult remove(@PathVariable Long[] actionItemIds)
    {
        return toAjax(bizExecutiveActionItemService.deleteBizExecutiveActionItemByActionItemIds(actionItemIds));
    }
}
