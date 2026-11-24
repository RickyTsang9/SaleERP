package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.MdProduct;
import com.ruoyi.business.mapper.MdProductMapper;
import com.ruoyi.business.service.IMdProductService;
import com.ruoyi.common.annotation.DataScope;

@Service
public class MdProductServiceImpl implements IMdProductService
{
    @Autowired
    private MdProductMapper mdProductMapper;

    @Autowired(required = false)
    private com.ruoyi.business.service.support.BizComplianceHelper complianceHelper;

    /**
     * 查询商品
     *
     * @param productId 商品编号
     * @return 商品信息
     */
    @Override
    public MdProduct selectMdProductById(Long productId)
    {
        return mdProductMapper.selectMdProductById(productId);
    }

    /**
     * 查询商品列表
     *
     * @param mdProduct 商品查询条件
     * @return 商品列表
     */
    @Override
    public List<MdProduct> selectMdProductList(MdProduct mdProduct)
    {
        return mdProductMapper.selectMdProductList(mdProduct);
    }

    /**
     * 新增商品
     *
     * @param mdProduct 商品信息
     * @return 结果
     */
    @Override
    public int insertMdProduct(MdProduct mdProduct)
    {
        return mdProductMapper.insertMdProduct(mdProduct);
    }

    /**
     * 修改商品
     *
     * @param mdProduct 商品信息
     * @return 结果
     */
    @Override
    public int updateMdProduct(MdProduct mdProduct)
    {
        if (mdProduct != null && "1".equals(mdProduct.getStatus()) && mdProduct.getProductId() != null && complianceHelper != null)
        {
            BigDecimal remainStock = complianceHelper.queryBigDecimal(
                "SELECT IFNULL(SUM(IFNULL(quantity, 0)), 0) FROM wms_stock WHERE product_id = ?",
                mdProduct.getProductId()
            );
            if (remainStock.compareTo(BigDecimal.ZERO) > 0)
            {
                throw new ServiceException("该商品当前仍有在库库存（" + remainStock + "），禁止直接停用，请先清零库存");
            }
        }
        return mdProductMapper.updateMdProduct(mdProduct);
    }

    /**
     * 删除商品
     *
     * @param productId 商品编号
     * @return 结果
     */
    @Override
    public int deleteMdProductById(Long productId)
    {
        validateProductCanBeDeleted(productId, false);
        return mdProductMapper.deleteMdProductById(productId);
    }

    /**
     * 批量删除商品
     *
     * @param productIds 商品编号数组
     * @return 结果
     */
    @Override
    public int deleteMdProductByIds(Long[] productIds)
    {
        for (Long productId : productIds)
        {
            validateProductCanBeDeleted(productId, true);
        }
        return mdProductMapper.deleteMdProductByIds(productIds);
    }

    /**
     * 导入商品数据
     *
     * @param mdProductList 商品导入集合
     * @param isUpdateSupport 是否支持更新
     * @param operName 操作人
     * @return 导入结果
     */
    @Override
    public String importMdProduct(List<MdProduct> mdProductList, Boolean isUpdateSupport, String operName)
    {
        if (StringUtils.isNull(mdProductList) || mdProductList.size() == 0)
        {
            throw new ServiceException("导入商品数据不能为空！");
        }
        Map<String, MdProduct> existingProductMap = buildExistingProductMap(mdProductList);
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (MdProduct product : mdProductList)
        {
            try
            {
                // 导入前批量加载已存在商品，循环内直接命中映射，减少逐条查库。
                MdProduct existingProduct = existingProductMap.get(product.getProductCode());
                boolean exists = existingProduct != null;
                
                if (!exists)
                {
                    product.setCreateBy(operName);
                    product.setStatus(StringUtils.isNotEmpty(product.getStatus()) ? product.getStatus() : "0");
                    this.insertMdProduct(product);
                    existingProductMap.put(product.getProductCode(), product);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、商品编码 " + product.getProductCode() + " 导入成功");
                }
                else if (isUpdateSupport)
                {
                    product.setProductId(existingProduct.getProductId());
                    product.setUpdateBy(operName);
                    this.updateMdProduct(product);
                    existingProductMap.put(product.getProductCode(), product);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、商品编码 " + product.getProductCode() + " 更新成功");
                }
                else
                {
                    failureNum++;
                    failureMsg.append("<br/>" + failureNum + "、商品编码 " + product.getProductCode() + " 已存在");
                }
            }
            catch (Exception e)
            {
                failureNum++;
                String msg = "<br/>" + failureNum + "、商品编码 " + product.getProductCode() + " 导入失败：";
                failureMsg.append(msg + e.getMessage());
            }
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        }
        else
        {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }

    /**
     * 批量构建已存在商品映射
     *
     * @param mdProductList 商品导入集合
     * @return 商品编码映射
     */
    private Map<String, MdProduct> buildExistingProductMap(List<MdProduct> mdProductList)
    {
        Map<String, MdProduct> existingProductMap = new HashMap<String, MdProduct>();
        List<String> productCodeList = new ArrayList<String>();
        for (MdProduct mdProduct : mdProductList)
        {
            if (mdProduct != null && StringUtils.isNotEmpty(mdProduct.getProductCode()))
            {
                productCodeList.add(mdProduct.getProductCode());
            }
        }
        if (productCodeList.isEmpty())
        {
            return existingProductMap;
        }
        List<MdProduct> existingProductList = mdProductMapper.selectMdProductListByProductCodes(productCodeList);
        if (existingProductList == null || existingProductList.isEmpty())
        {
            return existingProductMap;
        }
        for (MdProduct existingProduct : existingProductList)
        {
            existingProductMap.put(existingProduct.getProductCode(), existingProduct);
        }
        return existingProductMap;
    }

    /**
     * 校验商品是否允许删除
     *
     * @param productId 商品编号
     * @param batchDelete 是否批量删除
     */
    private void validateProductCanBeDeleted(Long productId, boolean batchDelete)
    {
        if (productId == null)
        {
            return;
        }
        MdProduct databaseProduct = mdProductMapper.selectMdProductById(productId);
        if (databaseProduct == null)
        {
            return;
        }
        Integer referenceCount = mdProductMapper.selectProductReferenceCount(productId);
        if (referenceCount != null && referenceCount.intValue() > 0)
        {
            if (batchDelete)
            {
                throw new ServiceException("商品已被业务单据或库存引用，不能删除，商品名称：" + databaseProduct.getProductName());
            }
            throw new ServiceException("商品已被业务单据或库存引用，不能删除");
        }
    }

    /**
     * 查询商品经营画像
     *
     * @param productId 商品编号
     * @return 商品经营画像数据
     */
    @Override
    public Map<String, Object> selectProductProfile(Long productId)
    {
        if (productId == null)
        {
            throw new ServiceException("商品编号不能为空");
        }
        MdProduct product = mdProductMapper.selectMdProductById(productId);
        if (product == null)
        {
            throw new ServiceException("商品不存在或已被删除");
        }
        Map<String, Object> saleSummary = mdProductMapper.selectProductSaleSummary(productId);
        Map<String, Object> returnSummary = mdProductMapper.selectProductReturnSummary(productId);
        Map<String, Object> purchaseSummary = mdProductMapper.selectProductPurchaseSummary(productId);
        List<Map<String, Object>> warehouseStockList = mdProductMapper.selectProductWarehouseStockList(productId);

        long saleOrderCount = toLong(saleSummary == null ? null : saleSummary.get("saleOrderCount"));
        BigDecimal totalSaleQty = toBigDecimal(saleSummary == null ? null : saleSummary.get("totalSaleQty"));
        BigDecimal totalSaleAmount = toBigDecimal(saleSummary == null ? null : saleSummary.get("totalSaleAmount"));
        BigDecimal returnQty = toBigDecimal(returnSummary == null ? null : returnSummary.get("returnQty"));
        BigDecimal returnAmount = toBigDecimal(returnSummary == null ? null : returnSummary.get("returnAmount"));
        long returnCount = toLong(returnSummary == null ? null : returnSummary.get("returnCount"));
        BigDecimal totalPurchaseQty = toBigDecimal(purchaseSummary == null ? null : purchaseSummary.get("totalPurchaseQty"));
        BigDecimal totalPurchaseAmount = toBigDecimal(purchaseSummary == null ? null : purchaseSummary.get("totalPurchaseAmount"));

        BigDecimal costPrice = product.getCostPrice() == null ? BigDecimal.ZERO : product.getCostPrice();
        BigDecimal netSaleQty = totalSaleQty.subtract(returnQty).max(BigDecimal.ZERO);
        BigDecimal netSaleAmount = totalSaleAmount.subtract(returnAmount);
        BigDecimal estimatedCostAmount = netSaleQty.multiply(costPrice).setScale(2, RoundingMode.HALF_UP);
        BigDecimal grossProfitAmount = netSaleAmount.subtract(estimatedCostAmount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal grossMarginRate = netSaleAmount.compareTo(BigDecimal.ZERO) > 0
            ? grossProfitAmount.multiply(new BigDecimal("100")).divide(netSaleAmount, 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        boolean canViewProfit = false;
        try
        {
            com.ruoyi.common.core.domain.model.LoginUser loginUser = com.ruoyi.common.utils.SecurityUtils.getLoginUser();
            if (loginUser == null || com.ruoyi.common.utils.SecurityUtils.isAdmin(loginUser.getUserId()))
            {
                canViewProfit = true;
            }
            else
            {
                java.util.Set<String> perms = loginUser.getPermissions();
                canViewProfit = perms != null && (perms.contains("*:*:*") || perms.contains("business:report:profit"));
            }
        }
        catch (Exception ignored) {}
        if (!canViewProfit)
        {
            product.setCostPrice(null);
            estimatedCostAmount = null;
            grossProfitAmount = null;
            grossMarginRate = null;
        }
        BigDecimal returnRate = totalSaleQty.compareTo(BigDecimal.ZERO) > 0
            ? returnQty.multiply(new BigDecimal("100")).divide(totalSaleQty, 2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;

        BigDecimal totalStockQty = BigDecimal.ZERO;
        BigDecimal totalAvailableQty = BigDecimal.ZERO;
        boolean hasBelowMin = false;
        boolean hasAboveMax = false;
        if (warehouseStockList != null)
        {
            for (Map<String, Object> stockRow : warehouseStockList)
            {
                BigDecimal qty = toBigDecimal(stockRow.get("quantity"));
                BigDecimal locked = toBigDecimal(stockRow.get("lockedQuantity"));
                BigDecimal frozen = toBigDecimal(stockRow.get("frozenQuantity"));
                BigDecimal available = qty.subtract(locked).subtract(frozen);
                if (available.compareTo(BigDecimal.ZERO) < 0)
                {
                    available = BigDecimal.ZERO;
                }
                stockRow.put("availableQuantity", available);
                totalStockQty = totalStockQty.add(qty);
                totalAvailableQty = totalAvailableQty.add(available);
                Object minObj = stockRow.get("warningMinQty");
                Object maxObj = stockRow.get("warningMaxQty");
                if (minObj != null && qty.compareTo(toBigDecimal(minObj)) < 0)
                {
                    hasBelowMin = true;
                }
                if (maxObj != null && qty.compareTo(toBigDecimal(maxObj)) > 0)
                {
                    hasAboveMax = true;
                }
            }
        }
        String warningStatus = "正常";
        if (warehouseStockList == null || warehouseStockList.isEmpty() || totalStockQty.compareTo(BigDecimal.ZERO) <= 0)
        {
            warningStatus = "无库存";
        }
        else if (hasBelowMin)
        {
            warningStatus = "低于下限";
        }
        else if (hasAboveMax)
        {
            warningStatus = "超出上限";
        }

        Map<String, Object> summary = new HashMap<String, Object>();
        summary.put("saleOrderCount", saleOrderCount);
        summary.put("totalSaleQty", totalSaleQty);
        summary.put("totalSaleAmount", totalSaleAmount);
        summary.put("estimatedCostAmount", estimatedCostAmount);
        summary.put("grossProfitAmount", grossProfitAmount);
        summary.put("grossMarginRate", grossMarginRate);
        summary.put("returnCount", returnCount);
        summary.put("returnQty", returnQty);
        summary.put("returnAmount", returnAmount);
        summary.put("returnRate", returnRate);
        summary.put("totalPurchaseQty", totalPurchaseQty);
        summary.put("totalPurchaseAmount", totalPurchaseAmount);
        summary.put("totalStockQty", totalStockQty);
        summary.put("totalAvailableQty", totalAvailableQty);
        summary.put("warningStatus", warningStatus);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("product", product);
        result.put("summary", summary);
        result.put("warehouseStockList", warehouseStockList == null ? new ArrayList<Map<String, Object>>() : warehouseStockList);
        return result;
    }

    /**
     * 按条码或商品编码扫码匹配商品
     *
     * @param scanCode 条码或商品编码
     * @return 匹配到的商品
     */
    @Override
    public MdProduct selectMdProductByScanCode(String scanCode)
    {
        if (StringUtils.isEmpty(scanCode) || scanCode.trim().isEmpty())
        {
            throw new ServiceException("请输入或扫描商品条码/编码");
        }
        String normalizedCode = scanCode.trim();
        MdProduct product = mdProductMapper.selectMdProductByScanCode(normalizedCode);
        if (product == null)
        {
            throw new ServiceException("未找到条码或编码对应的商品：" + normalizedCode);
        }
        if ("1".equals(product.getStatus()))
        {
            throw new ServiceException("该商品已停用，禁止出入库或盘点：" + product.getProductName());
        }
        return product;
    }

    private BigDecimal toBigDecimal(Object value)
    {
        if (value == null)
        {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal)
        {
            return (BigDecimal) value;
        }
        return new BigDecimal(String.valueOf(value));
    }

    private long toLong(Object value)
    {
        if (value == null)
        {
            return 0L;
        }
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }
}
