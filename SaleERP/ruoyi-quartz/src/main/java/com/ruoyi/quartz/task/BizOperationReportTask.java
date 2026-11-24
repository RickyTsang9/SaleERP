package com.ruoyi.quartz.task;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.business.domain.BizOperationSnapshot;
import com.ruoyi.business.service.IBizOperationSnapshotService;
import com.ruoyi.business.service.IBizReportService;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * 经营日报/周报/月报自动归档定时任务
 */
@Component("bizOperationReportTask")
public class BizOperationReportTask
{
    @Autowired
    private IBizReportService bizReportService;

    @Autowired
    private IBizOperationSnapshotService bizOperationSnapshotService;

    public void generateDailySnapshot()
    {
        generateSnapshot("daily", "经营日报");
    }

    public void generateWeeklySnapshot()
    {
        generateSnapshot("weekly", "经营周报");
    }

    public void generateMonthlySnapshot()
    {
        generateSnapshot("monthly", "经营月报");
    }

    void generateSnapshot(String snapshotType, String typeLabel)
    {
        LocalDate today = LocalDate.now();
        String snapshotTitle = today.toString() + " " + typeLabel + "自动快照";
        BizOperationSnapshot query = new BizOperationSnapshot();
        query.setSnapshotType(snapshotType);
        List<BizOperationSnapshot> existingList = bizOperationSnapshotService.selectBizOperationSnapshotList(query);
        if (existingList != null)
        {
            for (BizOperationSnapshot existing : existingList)
            {
                if (snapshotTitle.equals(existing.getSnapshotTitle()))
                {
                    return;
                }
            }
        }

        AjaxResult dashboard = bizReportService.getDashboardData();
        BigDecimal totalSale = toBigDecimal(dashboard.get("totalSaleAmount"));
        BigDecimal totalReceivable = toBigDecimal(dashboard.get("totalReceivableAmount"));
        BigDecimal totalReceived = toBigDecimal(dashboard.get("totalReceivedAmount"));
        BigDecimal collectionRate = totalReceivable.compareTo(BigDecimal.ZERO) > 0
            ? totalReceived.multiply(new BigDecimal("100")).divide(totalReceivable, 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        @SuppressWarnings("unchecked")
        Map<String, Object> managementOverview = dashboard.get("managementOverview") instanceof Map
            ? (Map<String, Object>) dashboard.get("managementOverview")
            : null;
        BigDecimal grossProfit = managementOverview != null
            ? toBigDecimal(managementOverview.get("currentMonthGrossProfitAmount"))
            : BigDecimal.ZERO;
        int overdueCount = toInt(dashboard.get("overdueCount"));
        int stockWarningCount = toInt(dashboard.get("stockWarningCount"));

        Map<String, Object> todoData = bizReportService.getTodoCenterData();
        @SuppressWarnings("unchecked")
        Map<String, Object> todoSummary = todoData != null && todoData.get("summary") instanceof Map
            ? (Map<String, Object>) todoData.get("summary")
            : null;
        int pendingActionCount = todoSummary != null ? toInt(todoSummary.get("totalCount")) : 0;

        String summary = String.format(
            "截至 %s，累计审核销售额 ¥%s，本月预估毛利 ¥%s，回款率 %s%%；当前逾期应收 %d 笔，库存预警 %d 项，统一待办 %d 项。",
            today,
            totalSale.setScale(2, RoundingMode.HALF_UP),
            grossProfit.setScale(2, RoundingMode.HALF_UP),
            collectionRate.setScale(2, RoundingMode.HALF_UP),
            overdueCount,
            stockWarningCount,
            pendingActionCount
        );

        BizOperationSnapshot snapshot = new BizOperationSnapshot();
        snapshot.setSnapshotTitle(snapshotTitle);
        snapshot.setSnapshotDate(Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        snapshot.setSnapshotType(snapshotType);
        snapshot.setSaleAmount(totalSale);
        snapshot.setGrossProfitAmount(grossProfit);
        snapshot.setCollectionRate(collectionRate);
        snapshot.setOverdueCount(overdueCount);
        snapshot.setStockWarningCount(stockWarningCount);
        snapshot.setPendingActionCount(pendingActionCount);
        snapshot.setSummaryContent(summary);
        snapshot.setPlainTextContent(snapshotTitle + "\n" + summary);
        snapshot.setSourceMode("scheduled_job");
        bizOperationSnapshotService.insertBizOperationSnapshot(snapshot, "system");
    }

    private BigDecimal toBigDecimal(Object val)
    {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal) return (BigDecimal) val;
        return new BigDecimal(String.valueOf(val));
    }

    private int toInt(Object val)
    {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        return Integer.parseInt(String.valueOf(val));
    }
}
