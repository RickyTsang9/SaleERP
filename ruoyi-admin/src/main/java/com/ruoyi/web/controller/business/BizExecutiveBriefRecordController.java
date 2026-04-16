package com.ruoyi.web.controller.business;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.BizExecutiveBriefRecord;
import com.ruoyi.business.service.IBizExecutiveBriefRecordService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * 管理层经营简报归档Controller
 * 
 * @author ruoyi
 * @date 2026-04-11
 */
@RestController
@RequestMapping("/business/executiveBriefRecord")
public class BizExecutiveBriefRecordController extends BaseController
{
    @Autowired
    private IBizExecutiveBriefRecordService bizExecutiveBriefRecordService;

    /**
     * 查询经营简报归档列表
     */
    @PreAuthorize("@ss.hasPermi('business:report:view')")
    @GetMapping("/list")
    public TableDataInfo list(BizExecutiveBriefRecord bizExecutiveBriefRecord)
    {
        startPage();
        List<BizExecutiveBriefRecord> executiveBriefRecordList =
            bizExecutiveBriefRecordService.selectBizExecutiveBriefRecordList(bizExecutiveBriefRecord);
        return getDataTable(executiveBriefRecordList);
    }

    /**
     * 查询经营简报归档详情
     */
    @PreAuthorize("@ss.hasPermi('business:report:view')")
    @GetMapping("/{briefId}")
    public AjaxResult getInfo(@PathVariable Long briefId)
    {
        return success(bizExecutiveBriefRecordService.selectBizExecutiveBriefRecordByBriefId(briefId));
    }

    /**
     * 新增经营简报归档
     */
    @PreAuthorize("@ss.hasPermi('business:report:brief')")
    @Log(title = "经营简报归档", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizExecutiveBriefRecord bizExecutiveBriefRecord)
    {
        return toAjax(bizExecutiveBriefRecordService.insertBizExecutiveBriefRecord(bizExecutiveBriefRecord, getUsername()));
    }
}
