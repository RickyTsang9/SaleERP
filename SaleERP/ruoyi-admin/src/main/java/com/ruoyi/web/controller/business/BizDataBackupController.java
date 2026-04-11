package com.ruoyi.web.controller.business;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.BizDataBackup;
import com.ruoyi.business.service.IBizDataBackupService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

@RestController
@RequestMapping("/business/dataBackup")
public class BizDataBackupController extends BaseController
{
    @Autowired
    private IBizDataBackupService bizDataBackupService;

    @PreAuthorize("@ss.hasPermi('business:dataBackup:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizDataBackup bizDataBackup)
    {
        startPage();
        List<BizDataBackup> backupRecordList = bizDataBackupService.selectBizDataBackupList(bizDataBackup);
        return getDataTable(backupRecordList);
    }

    @PreAuthorize("@ss.hasPermi('business:dataBackup:query')")
    @GetMapping("/{backupId}")
    public AjaxResult getInfo(@PathVariable Long backupId)
    {
        return success(bizDataBackupService.selectBizDataBackupById(backupId));
    }

    @PreAuthorize("@ss.hasPermi('business:dataBackup:add')")
    @Log(title = "数据备份", businessType = BusinessType.INSERT)
    @PostMapping("/manual")
    public AjaxResult manualBackup()
    {
        return toAjax(bizDataBackupService.createManualBackup(getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('business:dataBackup:restore')")
    @Log(title = "数据恢复", businessType = BusinessType.UPDATE)
    @PostMapping("/restore/{backupId}")
    public AjaxResult restore(@PathVariable Long backupId)
    {
        return toAjax(bizDataBackupService.restoreByBackupId(backupId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('business:dataBackup:remove')")
    @Log(title = "备份记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{backupIds}")
    public AjaxResult remove(@PathVariable Long[] backupIds)
    {
        return toAjax(bizDataBackupService.deleteBizDataBackupByIds(backupIds));
    }
}
