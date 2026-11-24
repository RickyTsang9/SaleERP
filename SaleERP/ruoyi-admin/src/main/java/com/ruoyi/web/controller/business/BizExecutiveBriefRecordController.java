package com.ruoyi.web.controller.business;

import java.util.ArrayList;
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
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.PermissionService;

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

    @Autowired
    private PermissionService permissionService;

    private boolean canViewProfit()
    {
        try
        {
            return SecurityUtils.isAdmin(getUserId()) || permissionService.hasPermi("business:report:profit");
        }
        catch (Exception ignored)
        {
            return false;
        }
    }

    private String redactProfitText(String text)
    {
        if (StringUtils.isEmpty(text))
        {
            return text;
        }
        return text.replaceAll("((?:本月|累计|预估|当月|年度)?(?:毛利额|毛利率|毛利目标|毛利预算|毛利)[：:\\s]*(?:[¥￥]?[-]?[0-9,]+(?:\\.[0-9]+)?%?))", "毛利数据[权限脱敏]");
    }

    private List<String> redactProfitList(List<String> list)
    {
        if (list == null)
        {
            return null;
        }
        List<String> masked = new ArrayList<>(list.size());
        for (String line : list)
        {
            masked.add(redactProfitText(line));
        }
        return masked;
    }

    private void maskBriefIfUnauthorized(BizExecutiveBriefRecord record)
    {
        if (record == null || canViewProfit())
        {
            return;
        }
        record.setSummaryContent(redactProfitText(record.getSummaryContent()));
        record.setPlainTextContent(redactProfitText(record.getPlainTextContent()));
        record.setHighlightTextList(redactProfitList(record.getHighlightTextList()));
        record.setRiskTextList(redactProfitList(record.getRiskTextList()));
        record.setActionTextList(redactProfitList(record.getActionTextList()));
    }

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
        if (!canViewProfit() && executiveBriefRecordList != null)
        {
            for (BizExecutiveBriefRecord record : executiveBriefRecordList)
            {
                maskBriefIfUnauthorized(record);
            }
        }
        return getDataTable(executiveBriefRecordList);
    }

    /**
     * 查询经营简报归档详情
     */
    @PreAuthorize("@ss.hasPermi('business:report:view')")
    @GetMapping("/{briefId}")
    public AjaxResult getInfo(@PathVariable Long briefId)
    {
        BizExecutiveBriefRecord record = bizExecutiveBriefRecordService.selectBizExecutiveBriefRecordByBriefId(briefId);
        maskBriefIfUnauthorized(record);
        return success(record);
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
