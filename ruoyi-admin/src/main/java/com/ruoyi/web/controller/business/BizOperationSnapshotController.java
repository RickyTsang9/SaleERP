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
        return getDataTable(operationSnapshotList);
    }

    /**
     * 查询经营快照详情
     */
    @PreAuthorize("@ss.hasPermi('business:report:view')")
    @GetMapping("/{snapshotId}")
    public AjaxResult getInfo(@PathVariable Long snapshotId)
    {
        return success(bizOperationSnapshotService.selectBizOperationSnapshotBySnapshotId(snapshotId));
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
