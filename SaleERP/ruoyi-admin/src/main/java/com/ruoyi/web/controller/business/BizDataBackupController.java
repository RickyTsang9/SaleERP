package com.ruoyi.web.controller.business;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
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

    @PreAuthorize("@ss.hasAnyPermi('business:dataBackup:export,business:dataBackup:list')")
    @Log(title = "备份包下载", businessType = BusinessType.EXPORT)
    @GetMapping("/{backupId}/download")
    public void download(@PathVariable Long backupId, HttpServletResponse response) throws Exception
    {
        File backupFile = bizDataBackupService.resolveBackupDownloadFile(backupId);
        String encodedName = URLEncoder.encode(backupFile.getName(), StandardCharsets.UTF_8.name()).replace("+", "%20");
        response.setContentType("application/gzip");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedName + "\"");
        response.setContentLengthLong(backupFile.length());
        try (InputStream in = new FileInputStream(backupFile);
             OutputStream out = response.getOutputStream())
        {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1)
            {
                out.write(buffer, 0, len);
            }
            out.flush();
        }
    }

    @PreAuthorize("@ss.hasPermi('business:dataBackup:restore')")
    @Log(title = "数据恢复", businessType = BusinessType.UPDATE)
    @PostMapping("/restore/{backupId}")
    public AjaxResult restore(@PathVariable Long backupId)
    {
        return toAjax(bizDataBackupService.restoreByBackupId(backupId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('business:dataBackup:restore')")
    @Log(title = "上传离线包恢复", businessType = BusinessType.UPDATE)
    @PostMapping("/uploadRestore")
    public AjaxResult uploadRestore(@RequestParam("file") MultipartFile file) throws Exception
    {
        if (file == null || file.isEmpty())
        {
            return error("请上传非空的 .json.gz 或 .json 离线备份包");
        }
        try (InputStream in = file.getInputStream())
        {
            return toAjax(bizDataBackupService.restoreFromUploadedFile(in, file.getOriginalFilename(), getUsername()));
        }
    }

    @PreAuthorize("@ss.hasPermi('business:dataBackup:remove')")
    @Log(title = "备份记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{backupIds}")
    public AjaxResult remove(@PathVariable Long[] backupIds)
    {
        return toAjax(bizDataBackupService.deleteBizDataBackupByIds(backupIds));
    }
}
