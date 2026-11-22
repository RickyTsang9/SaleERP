package com.ruoyi.business.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.FinAccount;
import com.ruoyi.business.domain.FinAccountJournal;
import com.ruoyi.business.domain.FinPeriodClose;

public interface FinAccountPeriodMapper
{
    public FinAccount selectFinAccountById(Long accountId);

    public FinAccount selectFinAccountByIdForUpdate(Long accountId);

    public FinAccount selectFinAccountByCode(String accountCode);

    public List<FinAccount> selectFinAccountList(FinAccount finAccount);

    public Map<String, Object> selectFinAccountSummary();

    public int clearDefaultAccounts();

    public int insertFinAccount(FinAccount finAccount);

    public int updateFinAccount(FinAccount finAccount);

    public int updateFinAccountBalance(@Param("accountId") Long accountId,
                                       @Param("currentBalance") BigDecimal currentBalance,
                                       @Param("updateBy") String updateBy);

    public int insertFinAccountJournal(FinAccountJournal journal);

    public List<FinAccountJournal> selectFinAccountJournalList(FinAccountJournal query);

    public List<FinPeriodClose> selectFinPeriodCloseList(FinPeriodClose query);

    public FinPeriodClose selectFinPeriodCloseById(Long periodId);

    public FinPeriodClose selectFinPeriodCloseByMonth(String periodMonth);

    public Map<String, Object> selectMonthSnapshot(@Param("periodMonth") String periodMonth);

    public int insertFinPeriodClose(FinPeriodClose periodClose);

    public int updateFinPeriodClose(FinPeriodClose periodClose);
}
