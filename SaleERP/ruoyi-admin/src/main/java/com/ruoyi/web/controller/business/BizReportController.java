package com.ruoyi.web.controller.business;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.service.IBizReportService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

@RestController
@RequestMapping("/business/report")
public class BizReportController extends BaseController
{
    @Autowired
    private IBizReportService bizReportService;

    @PreAuthorize("@ss.hasPermi('business:report:view')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard()
    {
        return bizReportService.getDashboardData();
    }

    @PreAuthorize("@ss.hasAnyPermi('business:report:sale,business:report:view')")
    @GetMapping("/saleList")
    public TableDataInfo saleList(@RequestParam Map<String, Object> params)
    {
        startPage();
        List<Map<String, Object>> list = bizReportService.selectSaleReportList(params);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('business:report:sale')")
    @Log(title = "销售明细报表", businessType = BusinessType.EXPORT)
    @PostMapping("/saleExport")
    public void saleExport(HttpServletResponse response, @RequestParam Map<String, Object> params) throws Exception
    {
        List<Map<String, Object>> list = bizReportService.selectSaleReportList(params);
        try (SXSSFWorkbook wb = new SXSSFWorkbook(500))
        {
            Sheet sheet = wb.createSheet("销售明细报表");
            String[] headers = { "业务单号", "单据类型", "业务日期", "客户名称", "商品编码", "商品名称", "分类", "净销数量", "单价(元)", "成交金额(元)", "成本单价(元)", "成本金额(元)", "毛利额(元)" };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++)
            {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            int rowIdx = 1;
            if (list != null)
            {
                for (Map<String, Object> item : list)
                {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(safeStr(item.get("orderNo")));
                    row.createCell(1).setCellValue("sale_return".equals(safeStr(item.get("billType"))) ? "销售退货冲减" : "销售订单");
                    row.createCell(2).setCellValue(safeStr(item.get("saleDate")));
                    row.createCell(3).setCellValue(safeStr(item.get("customerName")));
                    row.createCell(4).setCellValue(safeStr(item.get("productCode")));
                    row.createCell(5).setCellValue(safeStr(item.get("productName")));
                    row.createCell(6).setCellValue(safeStr(item.get("categoryName")));
                    row.createCell(7).setCellValue(safeStr(item.get("quantity")));
                    row.createCell(8).setCellValue(safeStr(item.get("price")));
                    row.createCell(9).setCellValue(safeStr(item.get("amount")));
                    row.createCell(10).setCellValue(item.get("costPrice") == null ? "权限脱敏" : safeStr(item.get("costPrice")));
                    row.createCell(11).setCellValue(item.get("costAmount") == null ? "权限脱敏" : safeStr(item.get("costAmount")));
                    row.createCell(12).setCellValue(item.get("grossProfit") == null ? "权限脱敏" : safeStr(item.get("grossProfit")));
                }
            }
            String fileName = URLEncoder.encode("sale_report_" + System.currentTimeMillis() + ".xlsx", StandardCharsets.UTF_8.name());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            try (OutputStream out = response.getOutputStream())
            {
                wb.write(out);
                out.flush();
            }
        }
    }

    private String safeStr(Object val)
    {
        return val == null ? "" : String.valueOf(val);
    }

    @PreAuthorize("@ss.hasPermi('business:report:view')")
    @GetMapping("/reconciliationList")
    public TableDataInfo reconciliationList(@RequestParam Map<String, Object> params)
    {
        startPage();
        List<Map<String, Object>> list = bizReportService.selectReconciliationList(params);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('business:report:view')")
    @GetMapping("/reconciliationStatement")
    public AjaxResult reconciliationStatement(@RequestParam Map<String, Object> params)
    {
        return AjaxResult.success(bizReportService.getReconciliationStatement(params));
    }

    @PreAuthorize("@ss.hasPermi('business:report:view')")
    @GetMapping("/productRanking")
    public AjaxResult productRanking(@RequestParam Map<String, Object> params)
    {
        return AjaxResult.success(bizReportService.selectProductRankingList(params));
    }

    @PreAuthorize("@ss.hasPermi('business:report:view')")
    @GetMapping("/invoiceTaxList")
    public TableDataInfo invoiceTaxList(@RequestParam Map<String, Object> params)
    {
        startPage();
        List<Map<String, Object>> list = bizReportService.selectInvoiceTaxList(params);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('business:report:view')")
    @GetMapping("/cashFlowForecast")
    public AjaxResult cashFlowForecast(@RequestParam(required = false) Integer forecastDays)
    {
        return AjaxResult.success(bizReportService.getCashFlowForecast(forecastDays));
    }

    /**
     * 查询统一待办中心数据
     *
     * @return 统一待办中心数据
     */
    @PreAuthorize("@ss.hasPermi('business:report:view')")
    @Log(title = "往来对账汇总报表", businessType = BusinessType.EXPORT)
    @PostMapping("/reconciliationExport")
    public void reconciliationExport(HttpServletResponse response, @RequestParam Map<String, Object> params) throws Exception
    {
        List<Map<String, Object>> list = bizReportService.selectReconciliationList(params);
        writeDynamicSheet(response, "reconciliation_report_", "往来对账汇总",
            new String[] { "往来类型", "单位名称", "累计业务金额(元)", "累计已结金额(元)", "期末未结余额(元)", "逾期未结金额(元)", "待结单数" },
            list, item -> new String[] {
                "supplier".equals(safeStr(item.get("targetType"))) ? "供应商应付" : "客户应收",
                safeStr(item.get("targetName")),
                safeStr(item.get("totalAmount")),
                safeStr(item.get("settledAmount")),
                safeStr(item.get("remainAmount")),
                safeStr(item.get("overdueAmount")),
                safeStr(item.get("openBillCount"))
            });
    }

    @PreAuthorize("@ss.hasPermi('business:report:view')")
    @Log(title = "商品经营排行报表", businessType = BusinessType.EXPORT)
    @PostMapping("/productRankingExport")
    public void productRankingExport(HttpServletResponse response, @RequestParam Map<String, Object> params) throws Exception
    {
        List<Map<String, Object>> list = bizReportService.selectProductRankingList(params);
        writeDynamicSheet(response, "product_ranking_", "商品经营排行",
            new String[] { "商品编码", "商品名称", "分类", "净销数量", "净销售额(元)", "毛利额(元)", "退货率(%)", "当前库存" },
            list, item -> new String[] {
                safeStr(item.get("productCode")),
                safeStr(item.get("productName")),
                safeStr(item.get("categoryName")),
                safeStr(item.get("netSaleQty")),
                safeStr(item.get("netSaleAmount")),
                item.get("grossProfitAmount") == null ? "权限脱敏" : safeStr(item.get("grossProfitAmount")),
                safeStr(item.get("returnRate")),
                safeStr(item.get("currentStockQty"))
            });
    }

    @PreAuthorize("@ss.hasPermi('business:report:view')")
    @Log(title = "进销项税票汇总", businessType = BusinessType.EXPORT)
    @PostMapping("/invoiceTaxExport")
    public void invoiceTaxExport(HttpServletResponse response, @RequestParam Map<String, Object> params) throws Exception
    {
        List<Map<String, Object>> list = bizReportService.selectInvoiceTaxList(params);
        writeDynamicSheet(response, "invoice_tax_report_", "进销项税票汇总",
            new String[] { "发票号码", "发票方向", "往来单位", "开票日期", "不含税金额(元)", "税率(%)", "税额(元)", "价税合计(元)", "状态" },
            list, item -> new String[] {
                safeStr(item.get("invoiceNo")),
                "purchase".equals(safeStr(item.get("invoiceDirection"))) ? "进项发票" : "销项发票",
                safeStr(item.get("targetName")),
                safeStr(item.get("invoiceDate")),
                safeStr(item.get("amountWithoutTax")),
                safeStr(item.get("taxRate")),
                safeStr(item.get("taxAmount")),
                safeStr(item.get("totalAmount")),
                safeStr(item.get("status"))
            });
    }

    private void writeDynamicSheet(HttpServletResponse response, String filePrefix, String sheetName,
                                   String[] headers, List<Map<String, Object>> list,
                                   java.util.function.Function<Map<String, Object>, String[]> mapper) throws Exception
    {
        try (SXSSFWorkbook wb = new SXSSFWorkbook(500))
        {
            Sheet sheet = wb.createSheet(sheetName);
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++)
            {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            int rowIdx = 1;
            if (list != null)
            {
                for (Map<String, Object> item : list)
                {
                    Row row = sheet.createRow(rowIdx++);
                    String[] cols = mapper.apply(item);
                    for (int c = 0; c < cols.length; c++)
                    {
                        row.createCell(c).setCellValue(cols[c] == null ? "" : cols[c]);
                    }
                }
            }
            String fileName = URLEncoder.encode(filePrefix + System.currentTimeMillis() + ".xlsx", StandardCharsets.UTF_8.name());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            try (OutputStream out = response.getOutputStream())
            {
                wb.write(out);
                out.flush();
            }
            wb.dispose();
        }
    }

    @PreAuthorize("@ss.hasPermi('business:todoCenter:list')")
    @GetMapping("/todoCenter")
    public AjaxResult todoCenter()
    {
        return AjaxResult.success(bizReportService.getTodoCenterData());
    }
}
