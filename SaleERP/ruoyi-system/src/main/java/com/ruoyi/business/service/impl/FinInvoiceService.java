package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.FinInvoice;
import com.ruoyi.business.domain.MdCustomer;
import com.ruoyi.business.domain.MdSupplier;
import com.ruoyi.business.domain.WmsPurchaseOrder;
import com.ruoyi.business.domain.WmsSaleOrder;
import com.ruoyi.business.mapper.FinInvoiceMapper;
import com.ruoyi.business.mapper.MdCustomerMapper;
import com.ruoyi.business.mapper.MdSupplierMapper;
import com.ruoyi.business.mapper.WmsPurchaseOrderMapper;
import com.ruoyi.business.mapper.WmsSaleOrderMapper;
import com.ruoyi.business.service.support.BizComplianceHelper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class FinInvoiceService
{
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    @Autowired
    private FinInvoiceMapper finInvoiceMapper;

    @Autowired
    private MdCustomerMapper mdCustomerMapper;

    @Autowired
    private MdSupplierMapper mdSupplierMapper;

    @Autowired
    private WmsSaleOrderMapper wmsSaleOrderMapper;

    @Autowired
    private WmsPurchaseOrderMapper wmsPurchaseOrderMapper;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private BizComplianceHelper bizComplianceHelper;

    public FinInvoice selectFinInvoiceById(Long invoiceId)
    {
        return finInvoiceMapper.selectFinInvoiceById(invoiceId);
    }

    public List<FinInvoice> selectFinInvoiceList(FinInvoice finInvoice)
    {
        return finInvoiceMapper.selectFinInvoiceList(finInvoice);
    }

    public Map<String, Object> selectFinInvoiceSummary(FinInvoice finInvoice)
    {
        Map<String, Object> raw = finInvoiceMapper.selectFinInvoiceSummary(finInvoice);
        Map<String, Object> result = new HashMap<>();
        BigDecimal saleTotal = toBigDecimal(raw == null ? null : raw.get("saleTotalAmount"));
        BigDecimal saleTax = toBigDecimal(raw == null ? null : raw.get("saleTaxAmount"));
        BigDecimal purchaseTotal = toBigDecimal(raw == null ? null : raw.get("purchaseTotalAmount"));
        BigDecimal purchaseTax = toBigDecimal(raw == null ? null : raw.get("purchaseTaxAmount"));
        result.put("saleTotalAmount", saleTotal);
        result.put("saleTaxAmount", saleTax);
        result.put("purchaseTotalAmount", purchaseTotal);
        result.put("purchaseTaxAmount", purchaseTax);
        result.put("estimatedVatPayable", saleTax.subtract(purchaseTax));
        result.put("voidCount", raw == null || raw.get("voidCount") == null ? 0 : ((Number) raw.get("voidCount")).longValue());
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertFinInvoice(FinInvoice finInvoice)
    {
        normalizeAndValidate(finInvoice, null);
        if (bizComplianceHelper != null)
        {
            bizComplianceHelper.assertPeriodOpen(finInvoice.getInvoiceDate(), "登记发票");
        }
        FinInvoice duplicate = finInvoiceMapper.selectFinInvoiceByNo(finInvoice.getInvoiceNo());
        if (duplicate != null)
        {
            throw new ServiceException("发票号码 [" + finInvoice.getInvoiceNo() + "] 已存在，请勿重复登记");
        }
        int rows = finInvoiceMapper.insertFinInvoice(finInvoice);
        syncOrderInvoiceStatus(finInvoice.getInvoiceDirection(), finInvoice.getSourceOrderId());
        return rows;
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateFinInvoice(FinInvoice finInvoice)
    {
        if (finInvoice.getInvoiceId() == null)
        {
            throw new ServiceException("发票主键不能为空");
        }
        FinInvoice existing = finInvoiceMapper.selectFinInvoiceById(finInvoice.getInvoiceId());
        if (existing == null)
        {
            throw new ServiceException("发票记录不存在");
        }
        if ("red_void".equals(existing.getStatus()))
        {
            throw new ServiceException("已红冲作废的发票禁止修改");
        }
        if (!"draft".equals(existing.getStatus()))
        {
            throw new ServiceException("已开具的正式税票禁止直接修改，如需更正请先执行【红冲作废】后重新开票");
        }
        normalizeAndValidate(finInvoice, existing.getInvoiceId());
        if (bizComplianceHelper != null)
        {
            bizComplianceHelper.assertPeriodOpen(existing.getInvoiceDate(), "修改原期间发票");
            bizComplianceHelper.assertPeriodOpen(finInvoice.getInvoiceDate(), "修改发票至目标期间");
        }
        FinInvoice duplicate = finInvoiceMapper.selectFinInvoiceByNo(finInvoice.getInvoiceNo());
        if (duplicate != null && !duplicate.getInvoiceId().equals(existing.getInvoiceId()))
        {
            throw new ServiceException("发票号码 [" + finInvoice.getInvoiceNo() + "] 已被其他发票使用");
        }
        int rows = finInvoiceMapper.updateFinInvoice(finInvoice);
        syncOrderInvoiceStatus(existing.getInvoiceDirection(), existing.getSourceOrderId());
        syncOrderInvoiceStatus(finInvoice.getInvoiceDirection(), finInvoice.getSourceOrderId());
        return rows;
    }

    @Transactional(rollbackFor = Exception.class)
    public int voidFinInvoice(Long invoiceId, String voidReason, String operatorName)
    {
        if (StringUtils.isEmpty(StringUtils.trim(voidReason)))
        {
            throw new ServiceException("红冲作废原因不能为空");
        }
        FinInvoice existing = finInvoiceMapper.selectFinInvoiceById(invoiceId);
        if (existing == null)
        {
            throw new ServiceException("发票记录不存在");
        }
        if ("red_void".equals(existing.getStatus()))
        {
            throw new ServiceException("该发票已处于红冲作废状态");
        }
        if (bizComplianceHelper != null)
        {
            bizComplianceHelper.assertPeriodOpen(existing.getInvoiceDate(), "红冲作废发票");
        }
        int rows = finInvoiceMapper.voidFinInvoice(invoiceId, voidReason.trim(), operatorName);
        if (rows == 0)
        {
            throw new ServiceException("发票状态已变更，请刷新重试");
        }
        syncOrderInvoiceStatus(existing.getInvoiceDirection(), existing.getSourceOrderId());
        return rows;
    }

    /**
     * 为发票管理页面提供客户/供应商/有效关联订单下拉选项（纯财务角色无需跨模块列表权限）
     */
    public Map<String, Object> getInvoiceSourceOptions()
    {
        Map<String, Object> result = new HashMap<>();
        if (jdbcTemplate == null)
        {
            return result;
        }
        result.put("customers", jdbcTemplate.queryForList(
            "SELECT customer_id AS customerId, customer_name AS customerName FROM md_customer WHERE status = '0' AND (del_flag IS NULL OR del_flag = '0') ORDER BY customer_id ASC"
        ));
        result.put("suppliers", jdbcTemplate.queryForList(
            "SELECT supplier_id AS supplierId, supplier_name AS supplierName FROM md_supplier WHERE status = '0' AND (del_flag IS NULL OR del_flag = '0') ORDER BY supplier_id ASC"
        ));
        result.put("saleOrders", jdbcTemplate.queryForList(
            "SELECT so.sale_order_id AS saleOrderId, so.order_no AS orderNo, so.customer_id AS customerId, c.customer_name AS customerName, " +
            "so.total_amount AS totalAmount, IFNULL(so.invoiced_amount, 0) AS invoicedAmount, IFNULL(so.invoice_status, 'uninvoiced') AS invoiceStatus " +
            "FROM wms_sale_order so LEFT JOIN md_customer c ON c.customer_id = so.customer_id " +
            "WHERE so.status IN ('audited', 'closed') ORDER BY so.sale_order_id DESC LIMIT 200"
        ));
        result.put("purchaseOrders", jdbcTemplate.queryForList(
            "SELECT po.purchase_order_id AS purchaseOrderId, po.order_no AS orderNo, po.supplier_id AS supplierId, s.supplier_name AS supplierName, " +
            "po.total_amount AS totalAmount, IFNULL(po.invoiced_amount, 0) AS invoicedAmount, IFNULL(po.invoice_status, 'uninvoiced') AS invoiceStatus " +
            "FROM wms_purchase_order po LEFT JOIN md_supplier s ON s.supplier_id = po.supplier_id " +
            "WHERE po.status IN ('1', '2', '3') ORDER BY po.purchase_order_id DESC LIMIT 200"
        ));
        return result;
    }

    private void normalizeAndValidate(FinInvoice finInvoice, Long excludeInvoiceId)
    {
        if (finInvoice == null)
        {
            throw new ServiceException("发票数据不能为空");
        }
        if (StringUtils.isEmpty(StringUtils.trim(finInvoice.getInvoiceNo())))
        {
            throw new ServiceException("发票号码不能为空");
        }
        finInvoice.setInvoiceNo(finInvoice.getInvoiceNo().trim());
        String dir = finInvoice.getInvoiceDirection();
        if (!"sale".equals(dir) && !"purchase".equals(dir))
        {
            throw new ServiceException("发票方向必须为 sale（销项）或 purchase（进项）");
        }
        if (StringUtils.isEmpty(finInvoice.getInvoiceType()))
        {
            finInvoice.setInvoiceType("electronic");
        }
        if (finInvoice.getInvoiceDate() == null)
        {
            finInvoice.setInvoiceDate(new Date());
        }
        if (StringUtils.isEmpty(finInvoice.getStatus()))
        {
            finInvoice.setStatus("issued");
        }
        else if (!"draft".equals(finInvoice.getStatus()) && !"issued".equals(finInvoice.getStatus()))
        {
            throw new ServiceException("发票保存状态仅允许为草稿(draft)或已开具(issued)，红冲作废请走专用红冲流程");
        }
        if (finInvoice.getTotalAmount() == null || finInvoice.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("价税合计金额必须大于0");
        }
        BigDecimal taxRate = finInvoice.getTaxRate() == null ? new BigDecimal("13.00") : finInvoice.getTaxRate();
        if (taxRate.compareTo(BigDecimal.ZERO) < 0 || taxRate.compareTo(ONE_HUNDRED) >= 0)
        {
            throw new ServiceException("税率必须在 0% ~ 99% 之间");
        }
        finInvoice.setTaxRate(taxRate);
        BigDecimal divisor = BigDecimal.ONE.add(taxRate.divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP));
        BigDecimal withoutTax = finInvoice.getTotalAmount().divide(divisor, 2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = finInvoice.getTotalAmount().subtract(withoutTax).setScale(2, RoundingMode.HALF_UP);
        finInvoice.setAmountWithoutTax(withoutTax);
        finInvoice.setTaxAmount(taxAmount);

        if ("sale".equals(dir))
        {
            if (finInvoice.getSourceOrderId() != null)
            {
                WmsSaleOrder saleOrder = wmsSaleOrderMapper.selectWmsSaleOrderById(finInvoice.getSourceOrderId());
                if (saleOrder == null)
                {
                    throw new ServiceException("关联销售订单不存在");
                }
                if (!"audited".equals(saleOrder.getStatus()) && !"closed".equals(saleOrder.getStatus()))
                {
                    throw new ServiceException("仅已审核或已结案的销售订单允许登记销项发票");
                }
                if (finInvoice.getTargetId() == null)
                {
                    finInvoice.setTargetId(saleOrder.getCustomerId());
                }
                else if (!Objects.equals(finInvoice.getTargetId(), saleOrder.getCustomerId()))
                {
                    throw new ServiceException("销项发票客户必须与关联销售订单的客户保持一致");
                }
                finInvoice.setSourceOrderNo(saleOrder.getOrderNo());
                if (jdbcTemplate != null)
                {
                    jdbcTemplate.queryForList(
                        "SELECT sale_order_id FROM wms_sale_order WHERE sale_order_id = ? FOR UPDATE",
                        Long.class, saleOrder.getSaleOrderId()
                    );
                }
                if (!"red_void".equals(finInvoice.getStatus()) && saleOrder.getTotalAmount() != null)
                {
                    BigDecimal baseOrderAmount = resolveOrderBaseInvoiceableAmount("sale", saleOrder.getSaleOrderId(), saleOrder.getStatus(), saleOrder.getTotalAmount());
                    BigDecimal returnedAmount = queryAuditedReturnAmount("sale", saleOrder.getSaleOrderId());
                    BigDecimal maxInvoiceable = baseOrderAmount.subtract(returnedAmount).max(BigDecimal.ZERO);
                    BigDecimal alreadyActive = finInvoiceMapper.selectActiveAmountBySourceOrder("sale", saleOrder.getSaleOrderId(), excludeInvoiceId);
                    if (alreadyActive == null)
                    {
                        alreadyActive = BigDecimal.ZERO;
                    }
                    if (alreadyActive.add(finInvoice.getTotalAmount()).compareTo(maxInvoiceable) > 0)
                    {
                        throw new ServiceException("累计开票金额（含草稿）已超出销售订单可开票净额（净额：" + maxInvoiceable + "，已占：" + alreadyActive + "）");
                    }
                }
            }
            if (finInvoice.getTargetId() == null)
            {
                throw new ServiceException("销项发票必须选择客户");
            }
            if (bizComplianceHelper != null)
            {
                bizComplianceHelper.assertEntityEnabled("md_customer", "customer_id", finInvoice.getTargetId(), "客户");
            }
            MdCustomer customer = mdCustomerMapper.selectMdCustomerById(finInvoice.getTargetId());
            if (customer != null)
            {
                finInvoice.setTargetName(customer.getCustomerName());
            }
            else if (StringUtils.isEmpty(finInvoice.getTargetName()))
            {
                finInvoice.setTargetName("客户#" + finInvoice.getTargetId());
            }
        }
        else
        {
            if (finInvoice.getSourceOrderId() != null)
            {
                WmsPurchaseOrder purchaseOrder = wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(finInvoice.getSourceOrderId());
                if (purchaseOrder == null)
                {
                    throw new ServiceException("关联采购订单不存在");
                }
                if (!"1".equals(purchaseOrder.getStatus()) && !"2".equals(purchaseOrder.getStatus()) && !"3".equals(purchaseOrder.getStatus()))
                {
                    throw new ServiceException("仅已审核/已入库的采购订单允许登记进项发票");
                }
                if (finInvoice.getTargetId() == null)
                {
                    finInvoice.setTargetId(purchaseOrder.getSupplierId());
                }
                else if (!Objects.equals(finInvoice.getTargetId(), purchaseOrder.getSupplierId()))
                {
                    throw new ServiceException("进项发票供应商必须与关联采购订单的供应商保持一致");
                }
                finInvoice.setSourceOrderNo(purchaseOrder.getOrderNo());
                if (jdbcTemplate != null)
                {
                    jdbcTemplate.queryForList(
                        "SELECT purchase_order_id FROM wms_purchase_order WHERE purchase_order_id = ? FOR UPDATE",
                        Long.class, purchaseOrder.getPurchaseOrderId()
                    );
                }
                if (!"red_void".equals(finInvoice.getStatus()) && purchaseOrder.getTotalAmount() != null)
                {
                    BigDecimal baseOrderAmount = resolveOrderBaseInvoiceableAmount("purchase", purchaseOrder.getPurchaseOrderId(), purchaseOrder.getStatus(), purchaseOrder.getTotalAmount());
                    BigDecimal returnedAmount = queryAuditedReturnAmount("purchase", purchaseOrder.getPurchaseOrderId());
                    BigDecimal maxInvoiceable = baseOrderAmount.subtract(returnedAmount).max(BigDecimal.ZERO);
                    BigDecimal alreadyActive = finInvoiceMapper.selectActiveAmountBySourceOrder("purchase", purchaseOrder.getPurchaseOrderId(), excludeInvoiceId);
                    if (alreadyActive == null)
                    {
                        alreadyActive = BigDecimal.ZERO;
                    }
                    if (alreadyActive.add(finInvoice.getTotalAmount()).compareTo(maxInvoiceable) > 0)
                    {
                        throw new ServiceException("累计收票金额（含草稿）已超出采购订单可收票净额（净额：" + maxInvoiceable + "，已占：" + alreadyActive + "）");
                    }
                }
            }
            if (finInvoice.getTargetId() == null)
            {
                throw new ServiceException("进项发票必须选择供应商");
            }
            if (bizComplianceHelper != null)
            {
                bizComplianceHelper.assertEntityEnabled("md_supplier", "supplier_id", finInvoice.getTargetId(), "供应商");
            }
            MdSupplier supplier = mdSupplierMapper.selectMdSupplierBySupplierId(finInvoice.getTargetId());
            if (supplier != null)
            {
                finInvoice.setTargetName(supplier.getSupplierName());
            }
            else if (StringUtils.isEmpty(finInvoice.getTargetName()))
            {
                finInvoice.setTargetName("供应商#" + finInvoice.getTargetId());
            }
        }
    }

    private BigDecimal queryAuditedReturnAmount(String direction, Long sourceOrderId)
    {
        if (jdbcTemplate == null || sourceOrderId == null)
        {
            return BigDecimal.ZERO;
        }
        String sql = "sale".equals(direction)
            ? "SELECT IFNULL(SUM(total_amount), 0) FROM wms_sale_return WHERE sale_order_id = ? AND status = 'audited'"
            : "SELECT IFNULL(SUM(total_amount), 0) FROM wms_purchase_return WHERE purchase_order_id = ? AND status = 'audited'";
        BigDecimal val = jdbcTemplate.queryForObject(sql, BigDecimal.class, sourceOrderId);
        return val == null ? BigDecimal.ZERO : val;
    }

    private BigDecimal resolveOrderBaseInvoiceableAmount(String direction, Long sourceOrderId, String orderStatus, BigDecimal orderTotalAmount)
    {
        BigDecimal total = orderTotalAmount == null ? BigDecimal.ZERO : orderTotalAmount;
        if (jdbcTemplate == null || sourceOrderId == null)
        {
            return total;
        }
        if ("sale".equals(direction) && "closed".equals(orderStatus))
        {
            BigDecimal shipped = jdbcTemplate.queryForObject(
                "SELECT IFNULL(SUM(total_amount), 0) FROM wms_outbound WHERE sale_order_id = ? AND status = 'audited'",
                BigDecimal.class, sourceOrderId
            );
            return shipped == null ? BigDecimal.ZERO : shipped.min(total);
        }
        if ("purchase".equals(direction) && "3".equals(orderStatus))
        {
            BigDecimal received = jdbcTemplate.queryForObject(
                "SELECT IFNULL(SUM(total_amount), 0) FROM wms_inbound WHERE purchase_order_id = ? AND status = 'audited'",
                BigDecimal.class, sourceOrderId
            );
            if (received != null && received.compareTo(BigDecimal.ZERO) > 0 && received.compareTo(total) < 0)
            {
                return received;
            }
        }
        return total;
    }

    /**
     * 反写来源销售单/采购单的累计已开票/收票金额与开票状态（uninvoiced/partial/invoiced）
     */
    public void syncOrderInvoiceStatus(String direction, Long sourceOrderId)
    {
        if (jdbcTemplate == null || sourceOrderId == null || StringUtils.isEmpty(direction))
        {
            return;
        }
        BigDecimal issued = finInvoiceMapper.selectIssuedAmountBySourceOrder(direction, sourceOrderId, null);
        if (issued == null)
        {
            issued = BigDecimal.ZERO;
        }
        if ("sale".equals(direction))
        {
            WmsSaleOrder order = wmsSaleOrderMapper.selectWmsSaleOrderById(sourceOrderId);
            if (order == null)
            {
                return;
            }
            BigDecimal baseOrder = resolveOrderBaseInvoiceableAmount("sale", sourceOrderId, order.getStatus(), order.getTotalAmount());
            BigDecimal netOrder = baseOrder.subtract(queryAuditedReturnAmount("sale", sourceOrderId)).max(BigDecimal.ZERO);
            String status = issued.compareTo(BigDecimal.ZERO) <= 0
                ? "uninvoiced"
                : (netOrder.compareTo(BigDecimal.ZERO) > 0 && issued.compareTo(netOrder) >= 0 ? "invoiced" : "partial");
            jdbcTemplate.update(
                "UPDATE wms_sale_order SET invoiced_amount = ?, invoice_status = ? WHERE sale_order_id = ?",
                issued, status, sourceOrderId
            );
        }
        else if ("purchase".equals(direction))
        {
            WmsPurchaseOrder order = wmsPurchaseOrderMapper.selectWmsPurchaseOrderByPurchaseOrderId(sourceOrderId);
            if (order == null)
            {
                return;
            }
            BigDecimal baseOrder = resolveOrderBaseInvoiceableAmount("purchase", sourceOrderId, order.getStatus(), order.getTotalAmount());
            BigDecimal netOrder = baseOrder.subtract(queryAuditedReturnAmount("purchase", sourceOrderId)).max(BigDecimal.ZERO);
            String status = issued.compareTo(BigDecimal.ZERO) <= 0
                ? "uninvoiced"
                : (netOrder.compareTo(BigDecimal.ZERO) > 0 && issued.compareTo(netOrder) >= 0 ? "invoiced" : "partial");
            jdbcTemplate.update(
                "UPDATE wms_purchase_order SET invoiced_amount = ?, invoice_status = ? WHERE purchase_order_id = ?",
                issued, status, sourceOrderId
            );
        }
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
