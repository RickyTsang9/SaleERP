package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.WmsStock;

public interface WmsStockMapper
{
    /**
     * 查询库存
     *
     * @param stockId 库存主键
     * @return 库存
     */
    public WmsStock selectWmsStockById(Long stockId);

    /**
     * 根据库存唯一键查询库存
     *
     * @param wmsStock 库存条件
     * @return 库存
     */
    public WmsStock selectWmsStockByKey(WmsStock wmsStock);

    /**
     * 查询预警库存列表
     *
     * @param wmsStock 库存条件
     * @return 预警库存集合
     */
    public List<WmsStock> selectWmsStockWarningList(WmsStock wmsStock);

    /**
     * 查询预警库存数量
     *
     * @param wmsStock 库存条件
     * @return 预警库存数量
     */
    public Long selectWmsStockWarningCount(WmsStock wmsStock);

    /**
     * 查询库存列表
     *
     * @param wmsStock 库存条件
     * @return 库存集合
     */
    public List<WmsStock> selectWmsStockList(WmsStock wmsStock);

    /**
     * 根据仓库和商品查询首条库存
     *
     * @param warehouseId 仓库主键
     * @param productId 商品主键
     * @return 库存
     */
    public WmsStock selectFirstWmsStockByWarehouseAndProduct(Long warehouseId, Long productId);

    /**
     * 新增库存
     *
     * @param wmsStock 库存
     * @return 结果
     */
    public int insertWmsStock(WmsStock wmsStock);

    /**
     * 修改库存
     *
     * @param wmsStock 库存
     * @return 结果
     */
    public int updateWmsStock(WmsStock wmsStock);

    /**
     * 删除库存
     *
     * @param stockId 库存主键
     * @return 结果
     */
    public int deleteWmsStockById(Long stockId);

    /**
     * 批量删除库存
     *
     * @param stockIds 库存主键集合
     * @return 结果
     */
    public int deleteWmsStockByIds(Long[] stockIds);
}
