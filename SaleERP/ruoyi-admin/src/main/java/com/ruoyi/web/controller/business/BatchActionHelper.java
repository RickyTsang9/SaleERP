package com.ruoyi.web.controller.business;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * 单据批量提交/批量审核统一结果聚合器（逐单独立执行，汇总成功与失败原因）
 */
public final class BatchActionHelper
{
    private BatchActionHelper()
    {
    }

    public static AjaxResult execute(List<Long> ids, String actionLabel, Consumer<Long> singleAction)
    {
        if (ids == null || ids.isEmpty())
        {
            return AjaxResult.error("请至少选择一条单据执行" + actionLabel);
        }
        int successCount = 0;
        List<String> failures = new ArrayList<>();
        for (Long id : ids)
        {
            if (id == null)
            {
                continue;
            }
            try
            {
                singleAction.accept(id);
                successCount++;
            }
            catch (Exception ex)
            {
                String reason = ex.getMessage() == null ? "未知错误" : ex.getMessage();
                failures.add("#" + id + "：" + reason);
            }
        }
        Map<String, Object> detail = new HashMap<>();
        detail.put("successCount", successCount);
        detail.put("failCount", failures.size());
        detail.put("failures", failures);
        if (successCount == 0 && !failures.isEmpty())
        {
            return AjaxResult.error("批量" + actionLabel + "未成功（共失败 " + failures.size() + " 单）：" + String.join("；", failures));
        }
        if (!failures.isEmpty())
        {
            return AjaxResult.success("批量" + actionLabel + "部分完成：成功 " + successCount + " 单，失败 " + failures.size() + " 单（" + String.join("；", failures) + "）", detail);
        }
        return AjaxResult.success("批量" + actionLabel + "成功，共处理 " + successCount + " 单", detail);
    }
}
