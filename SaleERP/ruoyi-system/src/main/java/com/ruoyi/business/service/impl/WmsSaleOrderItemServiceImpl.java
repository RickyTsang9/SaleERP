package com.ruoyi.business.service.impl;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.MdLocation;
import com.ruoyi.business.domain.WmsSaleOrder;
import com.ruoyi.business.domain.WmsSaleOrderItem;
import com.ruoyi.business.mapper.MdLocationMapper;
import com.ruoyi.business.mapper.WmsSaleOrderMapper;
import com.ruoyi.business.mapper.WmsSaleOrderItemMapper;
import com.ruoyi.business.service.IWmsSaleOrderItemService;
import com.ruoyi.business.service.support.BizComplianceHelper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

@Service
public class WmsSaleOrderItemServiceImpl implements IWmsSaleOrderItemService
{
    private static final String STATUS_DRAFT = "draft";

    @Autowired
    private WmsSaleOrderItemMapper wmsSaleOrderItemMapper;

    @Autowired
    private WmsSaleOrderMapper wmsSaleOrderMapper;

    @Autowired(required = false)
    private MdLocationMapper mdLocationMapper;

    @Autowired(required = false)
    private BizComplianceHelper complianceHelper;

    @Override
    public WmsSaleOrderItem selectWmsSaleOrderItemById(Long saleOrderItemId)
    {
        return wmsSaleOrderItemMapper.selectWmsSaleOrderItemById(saleOrderItemId);
    }

    @Override
    public List<WmsSaleOrderItem> selectWmsSaleOrderItemList(WmsSaleOrderItem wmsSaleOrderItem)
    {
        return wmsSaleOrderItemMapper.selectWmsSaleOrderItemList(wmsSaleOrderItem);
    }

    @Override
    public List<WmsSaleOrderItem> selectWmsSaleOrderItemsByOrderId(Long saleOrderId)
    {
        return wmsSaleOrderItemMapper.selectWmsSaleOrderItemsByOrderId(saleOrderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertWmsSaleOrderItem(WmsSaleOrderItem wmsSaleOrderItem)
    {
        validateSaleOrderEditable(wmsSaleOrderItem.getSaleOrderId());
        prepareSaleOrderItemForSave(wmsSaleOrderItem);
        int insertRows = wmsSaleOrderItemMapper.insertWmsSaleOrderItem(wmsSaleOrderItem);
        refreshSaleOrderTotal(wmsSaleOrderItem.getSaleOrderId());
        return insertRows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateWmsSaleOrderItem(WmsSaleOrderItem wmsSaleOrderItem)
    {
        WmsSaleOrderItem databaseSaleOrderItem = wmsSaleOrderItemMapper.selectWmsSaleOrderItemById(wmsSaleOrderItem.getSaleOrderItemId());
        if (databaseSaleOrderItem == null)
        {
            throw new ServiceException("销售明细不存在");
        }
        validateSaleOrderEditable(databaseSaleOrderItem.getSaleOrderId());
        wmsSaleOrderItem.setSaleOrderId(databaseSaleOrderItem.getSaleOrderId());
        prepareSaleOrderItemForSave(wmsSaleOrderItem);
        int updateRows = wmsSaleOrderItemMapper.updateWmsSaleOrderItem(wmsSaleOrderItem);
        refreshSaleOrderTotal(wmsSaleOrderItem.getSaleOrderId());
        if (databaseSaleOrderItem != null && databaseSaleOrderItem.getSaleOrderId() != null
            && !databaseSaleOrderItem.getSaleOrderId().equals(wmsSaleOrderItem.getSaleOrderId()))
        {
            refreshSaleOrderTotal(databaseSaleOrderItem.getSaleOrderId());
        }
        return updateRows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWmsSaleOrderItemById(Long saleOrderItemId)
    {
        WmsSaleOrderItem databaseSaleOrderItem = wmsSaleOrderItemMapper.selectWmsSaleOrderItemById(saleOrderItemId);
        if (databaseSaleOrderItem == null)
        {
            return 0;
        }
        validateSaleOrderEditable(databaseSaleOrderItem.getSaleOrderId());
        int deleteRows = wmsSaleOrderItemMapper.deleteWmsSaleOrderItemById(saleOrderItemId);
        if (databaseSaleOrderItem != null)
        {
            refreshSaleOrderTotal(databaseSaleOrderItem.getSaleOrderId());
        }
        return deleteRows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWmsSaleOrderItemByIds(Long[] saleOrderItemIds)
    {
        List<Long> saleOrderIdList = wmsSaleOrderItemMapper.selectSaleOrderIdsBySaleOrderItemIds(saleOrderItemIds);
        if (saleOrderIdList != null && !saleOrderIdList.isEmpty())
        {
            Set<Long> saleOrderIdSet = new LinkedHashSet<Long>(saleOrderIdList);
            for (Long saleOrderId : saleOrderIdSet)
            {
                validateSaleOrderEditable(saleOrderId);
            }
        }
        int deleteRows = wmsSaleOrderItemMapper.deleteWmsSaleOrderItemByIds(saleOrderItemIds);
        if (saleOrderIdList != null && !saleOrderIdList.isEmpty())
        {
            Set<Long> saleOrderIdSet = new LinkedHashSet<Long>(saleOrderIdList);
            for (Long saleOrderId : saleOrderIdSet)
            {
                refreshSaleOrderTotal(saleOrderId);
            }
        }
        return deleteRows;
    }

    /**
     * 校验销售单是否允许维护明细
     *
     * @param saleOrderId 销售单编号
     */
    private void validateSaleOrderEditable(Long saleOrderId)
    {
        if (saleOrderId == null)
        {
            throw new ServiceException("销售单不存在");
        }
        WmsSaleOrder databaseSaleOrder = wmsSaleOrderMapper.selectWmsSaleOrderById(saleOrderId);
        if (databaseSaleOrder == null)
        {
            throw new ServiceException("销售单不存在");
        }
        if (!STATUS_DRAFT.equals(databaseSaleOrder.getStatus()))
        {
            throw new ServiceException("仅草稿状态销售单允许维护明细");
        }
    }

    /**
     * 保存前校验销售明细字段并重算金额
     *
     * @param wmsSaleOrderItem 销售明细
     */
    private void prepareSaleOrderItemForSave(WmsSaleOrderItem wmsSaleOrderItem)
    {
        if (wmsSaleOrderItem == null)
        {
            throw new ServiceException("销售明细不能为空");
        }
        if (wmsSaleOrderItem.getProductId() == null)
        {
            throw new ServiceException("销售明细商品不能为空");
        }
        if (wmsSaleOrderItem.getLocationId() == null)
        {
            throw new ServiceException("销售明细库位不能为空");
        }
        if (StringUtils.isEmpty(wmsSaleOrderItem.getBatchNo()))
        {
            throw new ServiceException("销售明细批次不能为空");
        }
        if (wmsSaleOrderItem.getQuantity() == null || wmsSaleOrderItem.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("销售数量必须大于0");
        }
        if (wmsSaleOrderItem.getPrice() == null || wmsSaleOrderItem.getPrice().compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ServiceException("销售单价不能小于0");
        }
        if (complianceHelper != null)
        {
            complianceHelper.assertEntityEnabled("md_product", "product_id", wmsSaleOrderItem.getProductId(), "商品");
            complianceHelper.assertEntityEnabled("md_location", "location_id", wmsSaleOrderItem.getLocationId(), "库位");
        }
        if (mdLocationMapper != null && wmsSaleOrderItem.getSaleOrderId() != null)
        {
            WmsSaleOrder order = wmsSaleOrderMapper.selectWmsSaleOrderById(wmsSaleOrderItem.getSaleOrderId());
            MdLocation loc = mdLocationMapper.selectMdLocationById(wmsSaleOrderItem.getLocationId());
            if (order != null && order.getWarehouseId() != null && loc != null && loc.getWarehouseId() != null
                && !order.getWarehouseId().equals(loc.getWarehouseId()))
            {
                throw new ServiceException("所选库位不属于销售订单所属仓库，禁止跨仓绑定库位");
            }
        }
        wmsSaleOrderItem.setAmount(calculateSaleOrderItemAmount(wmsSaleOrderItem));
    }

    /**
     * 计算销售明细金额
     *
     * @param wmsSaleOrderItem 销售明细
     * @return 金额
     */
    private BigDecimal calculateSaleOrderItemAmount(WmsSaleOrderItem wmsSaleOrderItem)
    {
        BigDecimal quantityValue = wmsSaleOrderItem.getQuantity() == null ? BigDecimal.ZERO : wmsSaleOrderItem.getQuantity();
        BigDecimal priceValue = wmsSaleOrderItem.getPrice() == null ? BigDecimal.ZERO : wmsSaleOrderItem.getPrice();
        return quantityValue.multiply(priceValue).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 刷新销售单主表总数量与总金额
     *
     * @param saleOrderId 销售单编号
     */
    private void refreshSaleOrderTotal(Long saleOrderId)
    {
        if (saleOrderId != null)
        {
            wmsSaleOrderMapper.refreshTotalBySaleOrderId(saleOrderId);
        }
    }
}
