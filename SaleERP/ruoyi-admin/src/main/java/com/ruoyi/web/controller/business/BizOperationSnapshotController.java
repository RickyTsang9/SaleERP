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
import com.ruoyi.business.domain.BizOperationSnapshot;
import com.ruoyi.business.service.IBizOperationSnapshotService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.PermissionService;

/**
 * 管理层经营快照Controller
 * 
 * @author ruoyi
 * @date 2026-04-12
 */
@RestController
@RequestMapping("/business/operationSnapshot")
public class BizOperationSnapshotController extends BaseController
{
    @Autowired
    private IBizOperationSnapshotService bizOperationSnapshotService;

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

    private void maskSnapshotIfUnauthorized(BizOperationSnapshot snapshot)
    {
        if (snapshot == null || canViewProfit())
        {
            return;
        }
        snapshot.setGrossProfitAmount(null);
        snapshot.setSummaryContent(redactProfitText(snapshot.getSummaryContent()));
        snapshot.setPlainTextContent(redactProfitText(snapshot.getPlainTextContent()));
    }

    /**
     * 查询经营快照列表
     */
    @PreAuthorize("@ss.hasPermi('business:report:view')")
    @GetMapping("/list")
    public TableDataInfo list(BizOperationSnapshot bizOperationSnapshot)
    {
        startPage();
        List<BizOperationSnapshot> operationSnapshotList =
            bizOperationSnapshotService.selectBizOperationSnapshotList(bizOperationSnapshot);
        if (!canViewProfit() && operationSnapshotList != null)
        {
            for (BizOperationSnapshot item : operationSnapshotList)
            {
                maskSnapshotIfUnauthorized(item);
            }
        }
        return getDataTable(operationSnapshotList);
    }

    /**
     * 查询经营快照详情
     */
    @PreAuthorize("@ss.hasPermi('business:report:view')")
    @GetMapping("/{snapshotId}")
    public AjaxResult getInfo(@PathVariable Long snapshotId)
    {
        BizOperationSnapshot snapshot = bizOperationSnapshotService.selectBizOperationSnapshotBySnapshotId(snapshotId);
        maskSnapshotIfUnauthorized(snapshot);
        return success(snapshot);
    }

    /**
     * 新增经营快照
     */
    @PreAuthorize("@ss.hasPermi('business:report:brief')")
    @Log(title = "经营快照归档", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizOperationSnapshot bizOperationSnapshot)
    {
        return toAjax(bizOperationSnapshotService.insertBizOperationSnapshot(bizOperationSnapshot, getUsername()));
    }
}
