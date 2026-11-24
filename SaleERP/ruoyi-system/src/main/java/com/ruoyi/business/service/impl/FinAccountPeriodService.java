package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.FinAccount;
import com.ruoyi.business.domain.FinAccountJournal;
import com.ruoyi.business.domain.FinPeriodClose;
import com.ruoyi.business.mapper.FinAccountPeriodMapper;
import com.ruoyi.business.service.support.BizComplianceHelper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class FinAccountPeriodService
{
    private static final Pattern PERIOD_PATTERN = Pattern.compile("^\\d{4}-(0[1-9]|1[0-2])$");

    @Autowired
    private FinAccountPeriodMapper finAccountPeriodMapper;

    @Autowired(required = false)
    private BizComplianceHelper complianceHelper;

    public FinAccount selectFinAccountById(Long accountId)
    {
        return finAccountPeriodMapper.selectFinAccountById(accountId);
    }

    public List<FinAccount> selectFinAccountList(FinAccount finAccount)
    {
        return finAccountPeriodMapper.selectFinAccountList(finAccount);
    }

    public Map<String, Object> selectFinAccountSummary()
    {
        Map<String, Object> raw = finAccountPeriodMapper.selectFinAccountSummary();
        Map<String, Object> result = new HashMap<>();
        result.put("totalBalance", toBigDecimal(raw == null ? null : raw.get("totalBalance")));
        result.put("bankBalance", toBigDecimal(raw == null ? null : raw.get("bankBalance")));
        result.put("onlineBalance", toBigDecimal(raw == null ? null : raw.get("onlineBalance")));
        result.put("cashBalance", toBigDecimal(raw == null ? null : raw.get("cashBalance")));
        result.put("activeCount", raw == null || raw.get("activeCount") == null ? 0L : ((Number) raw.get("activeCount")).longValue());
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertFinAccount(FinAccount finAccount)
    {
        if (finAccount == null || StringUtils.isEmpty(StringUtils.trim(finAccount.getAccountName())))
        {
            throw new ServiceException("账户名称不能为空");
        }
        if (complianceHelper != null)
        {
            complianceHelper.assertPeriodOpen(new Date(), "新增资金结算账户");
        }
        if (StringUtils.isEmpty(StringUtils.trim(finAccount.getAccountCode())))
        {
            finAccount.setAccountCode("ACC-" + (System.currentTimeMillis() % 1000000L));
        }
        else
        {
            finAccount.setAccountCode(finAccount.getAccountCode().trim());
        }
        if (finAccountPeriodMapper.selectFinAccountByCode(finAccount.getAccountCode()) != null)
        {
            throw new ServiceException("账户编码 [" + finAccount.getAccountCode() + "] 已存在");
        }
        if (StringUtils.isEmpty(finAccount.getAccountType()))
        {
            finAccount.setAccountType("bank");
        }
        BigDecimal opening = finAccount.getOpeningBalance() == null ? BigDecimal.ZERO : finAccount.getOpeningBalance();
        if (opening.compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ServiceException("期初余额不能小于0");
        }
        finAccount.setOpeningBalance(opening);
        finAccount.setCurrentBalance(opening);
        if (StringUtils.isEmpty(finAccount.getStatus()))
        {
            finAccount.setStatus("0");
        }
        if ("Y".equalsIgnoreCase(finAccount.getIsDefault()))
        {
            finAccountPeriodMapper.clearDefaultAccounts();
            finAccount.setIsDefault("Y");
        }
        else
        {
            finAccount.setIsDefault("N");
        }
        int rows = finAccountPeriodMapper.insertFinAccount(finAccount);
        if (rows > 0 && opening.compareTo(BigDecimal.ZERO) > 0)
        {
            FinAccountJournal journal = new FinAccountJournal();
            journal.setJournalNo(generateJournalNo("OP"));
            journal.setAccountId(finAccount.getAccountId());
            journal.setAccountName(finAccount.getAccountName());
            journal.setDirection("in");
            journal.setBizType("opening");
            journal.setBizId(finAccount.getAccountId());
            journal.setBizNo(finAccount.getAccountCode());
            journal.setTargetName("期初建账");
            journal.setAmount(opening);
            journal.setBeforeBalance(BigDecimal.ZERO);
            journal.setAfterBalance(opening);
            journal.setOccurTime(new Date());
            journal.setRemark("新建资金结算账户期初入账");
            journal.setCreateBy(StringUtils.isEmpty(finAccount.getCreateBy()) ? "system" : finAccount.getCreateBy());
            finAccountPeriodMapper.insertFinAccountJournal(journal);
        }
        return rows;
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateFinAccount(FinAccount finAccount)
    {
        if (finAccount == null || finAccount.getAccountId() == null)
        {
            throw new ServiceException("账户主键不能为空");
        }
        FinAccount existing = finAccountPeriodMapper.selectFinAccountById(finAccount.getAccountId());
        if (existing == null)
        {
            throw new ServiceException("结算账户不存在");
        }
        if ("1".equals(finAccount.getStatus()) && !"1".equals(existing.getStatus()))
        {
            if ("Y".equalsIgnoreCase(existing.getIsDefault()))
            {
                throw new ServiceException("默认结算账户不允许直接停用，请先将其他账户设为默认后再停用");
            }
            BigDecimal balance = existing.getCurrentBalance() == null ? BigDecimal.ZERO : existing.getCurrentBalance();
            if (balance.compareTo(BigDecimal.ZERO) != 0)
            {
                throw new ServiceException("该资金账户当前余额不为0（余额：" + balance + "），请先通过【账户互转】清零后再停用");
            }
        }
        if ("Y".equalsIgnoreCase(finAccount.getIsDefault()))
        {
            if ("1".equals(finAccount.getStatus()) || ("1".equals(existing.getStatus()) && StringUtils.isEmpty(finAccount.getStatus())))
            {
                throw new ServiceException("已停用账户不允许设为默认结算账户");
            }
            finAccountPeriodMapper.clearDefaultAccounts();
            finAccount.setIsDefault("Y");
        }
        return finAccountPeriodMapper.updateFinAccount(finAccount);
    }

    public List<FinAccountJournal> selectFinAccountJournalList(FinAccountJournal query)
    {
        return finAccountPeriodMapper.selectFinAccountJournalList(query);
    }

    /**
     * 严格原子变更资金账户余额并写入资金收支流水（绝不吞异常，防止超额透支与账实脱节）
     */
    @Transactional(rollbackFor = Exception.class)
    public void changeAccountBalanceWithJournal(Long accountId, BigDecimal deltaAmount, String bizType,
                                                Long bizId, String bizNo, String targetName,
                                                Date occurTime, String remark, String operatorName)
    {
        if (deltaAmount == null || deltaAmount.compareTo(BigDecimal.ZERO) == 0)
        {
            return;
        }
        if (accountId == null)
        {
            throw new ServiceException("未指定资金结算账户，请先配置或选择有效结算账户");
        }
        FinAccount lockedAccount = finAccountPeriodMapper.selectFinAccountByIdForUpdate(accountId);
        if (lockedAccount == null)
        {
            throw new ServiceException("资金结算账户不存在（ID：" + accountId + "）");
        }
        if (!"0".equals(lockedAccount.getStatus()))
        {
            throw new ServiceException("资金结算账户【" + lockedAccount.getAccountName() + "】已停用，禁止发生资金变动");
        }
        BigDecimal before = lockedAccount.getCurrentBalance() == null ? BigDecimal.ZERO : lockedAccount.getCurrentBalance();
        BigDecimal after = before.add(deltaAmount);
        if (deltaAmount.compareTo(BigDecimal.ZERO) < 0 && after.compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ServiceException("资金账户【" + lockedAccount.getAccountName() + "】余额不足，当前余额：" + before + "，本次支出：" + deltaAmount.abs());
        }
        String op = StringUtils.isEmpty(operatorName) ? "system" : operatorName;
        finAccountPeriodMapper.updateFinAccountBalance(accountId, after, op);
        FinAccountJournal journal = new FinAccountJournal();
        journal.setJournalNo(generateJournalNo("LS"));
        journal.setAccountId(accountId);
        journal.setAccountName(lockedAccount.getAccountName());
        journal.setDirection(deltaAmount.compareTo(BigDecimal.ZERO) >= 0 ? "in" : "out");
        journal.setBizType(StringUtils.isEmpty(bizType) ? "other" : bizType);
        journal.setBizId(bizId);
        journal.setBizNo(bizNo);
        journal.setTargetName(targetName);
        journal.setAmount(deltaAmount.abs());
        journal.setBeforeBalance(before);
        journal.setAfterBalance(after);
        journal.setOccurTime(occurTime == null ? new Date() : occurTime);
        journal.setRemark(remark);
        journal.setCreateBy(op);
        finAccountPeriodMapper.insertFinAccountJournal(journal);
    }

    /**
     * 账户内部互转（单事务内同时扣减转出账户并增加转入账户，生成双边流水）
     */
    @Transactional(rollbackFor = Exception.class)
    public int transferBetweenAccounts(Long fromAccountId, Long toAccountId, BigDecimal amount,
                                       Date occurTime, String remark, String operatorName)
    {
        if (fromAccountId == null || toAccountId == null)
        {
            throw new ServiceException("转出账户和转入账户均不能为空");
        }
        if (Objects.equals(fromAccountId, toAccountId))
        {
            throw new ServiceException("转出账户与转入账户不能为同一账户");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("划转金额必须大于0");
        }
        Date bizDate = occurTime == null ? new Date() : occurTime;
        if (complianceHelper != null)
        {
            complianceHelper.assertPeriodOpen(bizDate, "资金账户互转");
        }
        // 按账户ID升序预加行锁，消除双向并发互转 (A->B 与 B->A) 的 AB-BA 死锁
        Long firstLockId = Math.min(fromAccountId, toAccountId);
        Long secondLockId = Math.max(fromAccountId, toAccountId);
        FinAccount firstAcc = finAccountPeriodMapper.selectFinAccountByIdForUpdate(firstLockId);
        FinAccount secondAcc = finAccountPeriodMapper.selectFinAccountByIdForUpdate(secondLockId);
        FinAccount fromAcc = Objects.equals(fromAccountId, firstLockId) ? firstAcc : secondAcc;
        FinAccount toAcc = Objects.equals(toAccountId, firstLockId) ? firstAcc : secondAcc;
        if (fromAcc == null || toAcc == null)
        {
            throw new ServiceException("转出或转入结算账户不存在");
        }
        String transferNo = generateJournalNo("ZZ");
        String note = StringUtils.isEmpty(remark) ? "内部资金划转" : remark.trim();
        changeAccountBalanceWithJournal(fromAccountId, amount.negate(), "transfer_out", toAccountId, transferNo,
            "转至：" + toAcc.getAccountName(), bizDate, note, operatorName);
        changeAccountBalanceWithJournal(toAccountId, amount, "transfer_in", fromAccountId, transferNo,
            "来自：" + fromAcc.getAccountName(), bizDate, note, operatorName);
        return 1;
    }

    /**
     * 登记日常其他收支（水电房租、运费补贴、利息收入等）
     */
    @Transactional(rollbackFor = Exception.class)
    public int recordOtherFund(Long accountId, String direction, BigDecimal amount, String targetName,
                               Date occurTime, String remark, String operatorName)
    {
        if (accountId == null)
        {
            throw new ServiceException("资金结算账户不能为空");
        }
        if (!"in".equals(direction) && !"out".equals(direction))
        {
            throw new ServiceException("收支方向必须为 in（其他收入）或 out（日常费用）");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("发生金额必须大于0");
        }
        Date bizDate = occurTime == null ? new Date() : occurTime;
        if (complianceHelper != null)
        {
            complianceHelper.assertPeriodOpen(bizDate, "登记日常资金收支");
        }
        String bizType = "in".equals(direction) ? "other_income" : "other_expense";
        BigDecimal delta = "in".equals(direction) ? amount : amount.negate();
        String bizNo = generateJournalNo("in".equals(direction) ? "SR" : "FY");
        String target = StringUtils.isEmpty(targetName) ? ("in".equals(direction) ? "其他业务收入" : "日常经营费用") : targetName.trim();
        changeAccountBalanceWithJournal(accountId, delta, bizType, null, bizNo, target, bizDate, remark, operatorName);
        return 1;
    }

    private String generateJournalNo(String prefix)
    {
        String dateStr = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String rand = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "ZJ-" + prefix + "-" + dateStr + "-" + rand;
    }

    public List<FinPeriodClose> selectFinPeriodCloseList(FinPeriodClose query)
    {
        return finAccountPeriodMapper.selectFinPeriodCloseList(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public int closePeriod(String periodMonth, String remark, boolean forceClose, String operatorName)
    {
        if (StringUtils.isEmpty(periodMonth) || !PERIOD_PATTERN.matcher(periodMonth.trim()).matches())
        {
            throw new ServiceException("会计期间格式必须为 YYYY-MM（例如 2026-10）");
        }
        String month = periodMonth.trim();
        Map<String, Object> snap = finAccountPeriodMapper.selectMonthSnapshot(month);
        long pendingCount = snap == null || snap.get("pendingBillCount") == null ? 0L : ((Number) snap.get("pendingBillCount")).longValue();
        if (pendingCount > 0 && !forceClose)
        {
            throw new ServiceException("期间 [" + month + "] 尚有 " + pendingCount + " 张待审核业务单据，请先完成审核或勾选强制月结");
        }
        BigDecimal saleSnap = toBigDecimal(snap == null ? null : snap.get("saleAmount"));
        BigDecimal purchaseSnap = toBigDecimal(snap == null ? null : snap.get("purchaseAmount"));
        FinPeriodClose existing = finAccountPeriodMapper.selectFinPeriodCloseByMonth(month);
        if (existing != null)
        {
            if ("closed".equals(existing.getCloseStatus()))
            {
                throw new ServiceException("会计期间 [" + month + "] 已处于关账锁定状态");
            }
            existing.setCloseStatus("closed");
            existing.setSaleAmountSnapshot(saleSnap);
            existing.setPurchaseAmountSnapshot(purchaseSnap);
            existing.setClosedBy(operatorName);
            existing.setClosedTime(new Date());
            existing.setRemark(remark);
            return finAccountPeriodMapper.updateFinPeriodClose(existing);
        }
        FinPeriodClose periodClose = new FinPeriodClose();
        periodClose.setPeriodMonth(month);
        periodClose.setCloseStatus("closed");
        periodClose.setSaleAmountSnapshot(saleSnap);
        periodClose.setPurchaseAmountSnapshot(purchaseSnap);
        periodClose.setClosedBy(operatorName);
        periodClose.setRemark(remark);
        return finAccountPeriodMapper.insertFinPeriodClose(periodClose);
    }

    @Transactional(rollbackFor = Exception.class)
    public int reopenPeriod(Long periodId, String reopenReason, String operatorName)
    {
        if (periodId == null)
        {
            throw new ServiceException("月结期间ID不能为空");
        }
        if (StringUtils.isEmpty(StringUtils.trim(reopenReason)))
        {
            throw new ServiceException("反结账必须填写原因说明");
        }
        FinPeriodClose existing = finAccountPeriodMapper.selectFinPeriodCloseById(periodId);
        if (existing == null)
        {
            throw new ServiceException("月结记录不存在");
        }
        if (!"closed".equals(existing.getCloseStatus()))
        {
            throw new ServiceException("当前期间未处于关账状态，无需反结账");
        }
        existing.setCloseStatus("reopened");
        existing.setReopenReason(reopenReason.trim() + "（操作人：" + operatorName + "）");
        return finAccountPeriodMapper.updateFinPeriodClose(existing);
    }

    private BigDecimal toBigDecimal(Object val)
    {
        if (val == null)
        {
            return BigDecimal.ZERO;
        }
        if (val instanceof BigDecimal)
        {
            return (BigDecimal) val;
        }
        return new BigDecimal(String.valueOf(val));
    }
}
