package com.ruoyi.business.service.impl;

import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.business.domain.BizMessage;
import com.ruoyi.business.domain.WmsStock;
import com.ruoyi.business.mapper.WmsStockMapper;
import com.ruoyi.business.service.IBizMessageService;
import com.ruoyi.business.service.IWmsStockService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.service.ISysNoticeService;

@Service
public class WmsStockServiceImpl implements IWmsStockService
{
    @Autowired
    private WmsStockMapper wmsStockMapper;

    @Autowired
    private ISysNoticeService sysNoticeService;

    @Autowired
    private IBizMessageService bizMessageService;

    /**
     * 查询库存台账
     *
     * @param stockId 库存台账编号
     * @return 库存台账
     */
    @Override
    public WmsStock selectWmsStockById(Long stockId)
    {
        return wmsStockMapper.selectWmsStockById(stockId);
    }

    /**
     * 根据库存唯一键查询库存台账
     *
     * @param wmsStock 库存台账查询条件
     * @return 库存台账
     */
    @Override
    public WmsStock selectWmsStockByKey(WmsStock wmsStock)
    {
        return wmsStockMapper.selectWmsStockByKey(wmsStock);
    }

    /**
     * 查询库存预警列表
     *
     * @param wmsStock 库存预警查询条件
     * @return 库存预警列表
     */
    @Override
    public List<WmsStock> selectWmsStockWarningList(WmsStock wmsStock)
    {
        return wmsStockMapper.selectWmsStockWarningList(wmsStock);
    }

    /**
     * 推送库存预警提醒
     *
     * @param operatorName 操作人
     * @return 预警条数
     */
    @Override
    public int pushWarningReminder(String operatorName)
    {
        Long warningStockCount = wmsStockMapper.selectWmsStockWarningCount(new WmsStock());
        int warningStockCountValue = warningStockCount == null ? 0 : warningStockCount.intValue();
        if (warningStockCountValue <= 0)
        {
            return 0;
        }
        String noticeTitle = "库存预警提醒-" + LocalDate.now();
        String noticeContent = "当前库存预警商品共" + warningStockCountValue + "条，请及时处理。";
        boolean existsBizMessage = bizMessageService.existsBizMessageByTypeAndTitle("stock_warning", noticeTitle);
        if (!existsBizMessage)
        {
            BizMessage bizMessage = new BizMessage();
            bizMessage.setMessageType("stock_warning");
            bizMessage.setMessageTitle(noticeTitle);
            bizMessage.setMessageContent(noticeContent);
            bizMessage.setMessageLevel("warning");
            bizMessage.setBusinessType("stock");
            bizMessage.setStatus("0");
            bizMessage.setCreateBy(operatorName);
            bizMessageService.insertBizMessage(bizMessage);
        }
        boolean existsNotice = sysNoticeService.existsNoticeByTitle(noticeTitle);
        if (existsNotice)
        {
            return warningStockCountValue;
        }
        SysNotice notice = new SysNotice();
        notice.setNoticeTitle(noticeTitle);
        notice.setNoticeType("1");
        notice.setStatus("0");
        notice.setCreateBy(operatorName);
        notice.setNoticeContent(noticeContent);
        sysNoticeService.insertNotice(notice);
        return warningStockCountValue;
    }

    /**
     * 查询库存台账列表
     *
     * @param wmsStock 库存台账查询条件
     * @return 库存台账列表
     */
    @Override
    public List<WmsStock> selectWmsStockList(WmsStock wmsStock)
    {
        return wmsStockMapper.selectWmsStockList(wmsStock);
    }

    /**
     * 库存台账由业务单据自动维护，禁止手工新增。
     *
     * @param wmsStock 库存台账
     * @return 处理结果
     */
    @Override
    public int insertWmsStock(WmsStock wmsStock)
    {
        throw new ServiceException("库存台账由入库、出库、盘点、调拨等业务单据自动维护，不支持手工新增");
    }

    /**
     * 库存台账由业务单据自动维护，禁止手工修改。
     *
     * @param wmsStock 库存台账
     * @return 处理结果
     */
    @Override
    public int updateWmsStock(WmsStock wmsStock)
    {
        throw new ServiceException("库存台账由入库、出库、盘点、调拨等业务单据自动维护，不支持手工修改");
    }

    /**
     * 库存台账由业务单据自动维护，禁止手工删除。
     *
     * @param stockId 库存台账编号
     * @return 处理结果
     */
    @Override
    public int deleteWmsStockById(Long stockId)
    {
        throw new ServiceException("库存台账由业务单据自动维护，不支持手工删除");
    }

    /**
     * 库存台账由业务单据自动维护，禁止手工批量删除。
     *
     * @param stockIds 库存台账编号集合
     * @return 处理结果
     */
    @Override
    public int deleteWmsStockByIds(Long[] stockIds)
    {
        throw new ServiceException("库存台账由业务单据自动维护，不支持手工删除");
    }
}
