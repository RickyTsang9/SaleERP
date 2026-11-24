package com.ruoyi.web.controller.business;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.FinAccount;
import com.ruoyi.business.domain.FinAccountJournal;
import com.ruoyi.business.domain.FinPeriodClose;
import com.ruoyi.business.service.impl.FinAccountPeriodService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.DateUtils;

@RestController
@RequestMapping("/business/account")
public class FinAccountController extends BaseController
{
    @Autowired
    private FinAccountPeriodService finAccountPeriodService;

    @PreAuthorize("@ss.hasAnyPermi('business:account:list,business:receipt:list,business:receipt:add,business:payment:list,business:payment:add')")
    @GetMapping("/list")
    public TableDataInfo list(FinAccount finAccount)
    {
        startPage();
        List<FinAccount> list = finAccountPeriodService.selectFinAccountList(finAccount);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('business:account:list')")
    @GetMapping("/summary")
    public AjaxResult summary()
    {
        return success(finAccountPeriodService.selectFinAccountSummary());
    }

    @PreAuthorize("@ss.hasPermi('business:account:list')")
    @GetMapping("/{accountId}")
    public AjaxResult getInfo(@PathVariable Long accountId)
    {
        return success(finAccountPeriodService.selectFinAccountById(accountId));
    }

    @PreAuthorize("@ss.hasPermi('business:account:edit')")
    @Log(title = "资金结算账户", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping
    public AjaxResult add(@RequestBody FinAccount finAccount)
    {
        finAccount.setCreateBy(getUsername());
        return toAjax(finAccountPeriodService.insertFinAccount(finAccount));
    }

    @PreAuthorize("@ss.hasPermi('business:account:edit')")
    @Log(title = "资金结算账户", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PutMapping
    public AjaxResult edit(@RequestBody FinAccount finAccount)
    {
        finAccount.setUpdateBy(getUsername());
        return toAjax(finAccountPeriodService.updateFinAccount(finAccount));
    }

    @PreAuthorize("@ss.hasPermi('business:account:list')")
    @Log(title = "资金结算账户", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(javax.servlet.http.HttpServletResponse response, FinAccount finAccount)
    {
        List<FinAccount> list = finAccountPeriodService.selectFinAccountList(finAccount);
        com.ruoyi.common.utils.poi.ExcelUtil<FinAccount> util = new com.ruoyi.common.utils.poi.ExcelUtil<FinAccount>(FinAccount.class);
        util.exportExcel(response, list, "资金结算账户数据");
    }

    @PreAuthorize("@ss.hasPermi('business:account:list')")
    @GetMapping("/journal/list")
    public TableDataInfo journalList(FinAccountJournal query)
    {
        startPage();
        List<FinAccountJournal> list = finAccountPeriodService.selectFinAccountJournalList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('business:account:list')")
    @Log(title = "资金收支流水", businessType = BusinessType.EXPORT)
    @PostMapping("/journal/export")
    public void exportJournal(javax.servlet.http.HttpServletResponse response, FinAccountJournal query)
    {
        List<FinAccountJournal> list = finAccountPeriodService.selectFinAccountJournalList(query);
        com.ruoyi.common.utils.poi.ExcelUtil<FinAccountJournal> util = new com.ruoyi.common.utils.poi.ExcelUtil<FinAccountJournal>(FinAccountJournal.class);
        util.exportExcel(response, list, "资金收支流水数据");
    }

    @PreAuthorize("@ss.hasPermi('business:account:edit')")
    @Log(title = "资金账户互转", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/transfer")
    public AjaxResult transfer(@RequestBody Map<String, Object> body)
    {
        Long fromAccountId = body == null || body.get("fromAccountId") == null ? null : Long.valueOf(String.valueOf(body.get("fromAccountId")));
        Long toAccountId = body == null || body.get("toAccountId") == null ? null : Long.valueOf(String.valueOf(body.get("toAccountId")));
        BigDecimal amount = body == null || body.get("amount") == null ? null : new BigDecimal(String.valueOf(body.get("amount")));
        Date occurTime = body == null || body.get("occurTime") == null ? new Date() : DateUtils.parseDate(body.get("occurTime"));
        String remark = body == null || body.get("remark") == null ? null : String.valueOf(body.get("remark"));
        return toAjax(finAccountPeriodService.transferBetweenAccounts(fromAccountId, toAccountId, amount, occurTime, remark, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('business:account:edit')")
    @Log(title = "日常资金收支", businessType = BusinessType.INSERT)
    @RepeatSubmit
    @PostMapping("/otherFund")
    public AjaxResult otherFund(@RequestBody Map<String, Object> body)
    {
        Long accountId = body == null || body.get("accountId") == null ? null : Long.valueOf(String.valueOf(body.get("accountId")));
        String direction = body == null || body.get("direction") == null ? null : String.valueOf(body.get("direction"));
        BigDecimal amount = body == null || body.get("amount") == null ? null : new BigDecimal(String.valueOf(body.get("amount")));
        String targetName = body == null || body.get("targetName") == null ? null : String.valueOf(body.get("targetName"));
        Date occurTime = body == null || body.get("occurTime") == null ? new Date() : DateUtils.parseDate(body.get("occurTime"));
        String remark = body == null || body.get("remark") == null ? null : String.valueOf(body.get("remark"));
        return toAjax(finAccountPeriodService.recordOtherFund(accountId, direction, amount, targetName, occurTime, remark, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('business:account:list')")
    @GetMapping("/period/list")
    public TableDataInfo periodList(FinPeriodClose query)
    {
        startPage();
        List<FinPeriodClose> list = finAccountPeriodService.selectFinPeriodCloseList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('business:periodClose:manage')")
    @Log(title = "财务月结关账", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/period/close")
    public AjaxResult closePeriod(@RequestBody Map<String, Object> body)
    {
        String periodMonth = body == null || body.get("periodMonth") == null ? null : String.valueOf(body.get("periodMonth"));
        String remark = body == null || body.get("remark") == null ? null : String.valueOf(body.get("remark"));
        boolean forceClose = body != null && Boolean.parseBoolean(String.valueOf(body.get("forceClose")));
        return toAjax(finAccountPeriodService.closePeriod(periodMonth, remark, forceClose, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('business:periodClose:manage')")
    @Log(title = "财务月结反结账", businessType = BusinessType.UPDATE)
    @RepeatSubmit
    @PostMapping("/period/reopen")
    public AjaxResult reopenPeriod(@RequestBody Map<String, Object> body)
    {
        Long periodId = body == null || body.get("periodId") == null ? null : Long.valueOf(String.valueOf(body.get("periodId")));
        String reopenReason = body == null || body.get("reopenReason") == null ? null : String.valueOf(body.get("reopenReason"));
        return toAjax(finAccountPeriodService.reopenPeriod(periodId, reopenReason, getUsername()));
    }
}
