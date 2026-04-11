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

    @Override
    public WmsStock selectWmsStockById(Long stockId)
    {
        return wmsStockMapper.selectWmsStockById(stockId);
    }

    @Override
    public WmsStock selectWmsStockByKey(WmsStock wmsStock)
    {
        return wmsStockMapper.selectWmsStockByKey(wmsStock);
    }

    @Override
    public List<WmsStock> selectWmsStockWarningList(WmsStock wmsStock)
    {
        return wmsStockMapper.selectWmsStockWarningList(wmsStock);
    }

    @Override
    public int pushWarningReminder(String operatorName)
    {
        List<WmsStock> warningStockList = wmsStockMapper.selectWmsStockWarningList(new WmsStock());
        if (warningStockList.isEmpty())
        {
            return 0;
        }
        String noticeTitle = "库存预警提醒-" + LocalDate.now();
        String noticeContent = "当前库存预警商品共" + warningStockList.size() + "条，请及时处理。";
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
        SysNotice queryNotice = new SysNotice();
        queryNotice.setNoticeTitle(noticeTitle);
        List<SysNotice> existingNoticeList = sysNoticeService.selectNoticeList(queryNotice);
        for (SysNotice existingNotice : existingNoticeList)
        {
            if (noticeTitle.equals(existingNotice.getNoticeTitle()))
            {
                return warningStockList.size();
            }
        }
        SysNotice notice = new SysNotice();
        notice.setNoticeTitle(noticeTitle);
        notice.setNoticeType("1");
        notice.setStatus("0");
        notice.setCreateBy(operatorName);
        notice.setNoticeContent(noticeContent);
        sysNoticeService.insertNotice(notice);
        return warningStockList.size();
    }

    @Override
    public List<WmsStock> selectWmsStockList(WmsStock wmsStock)
    {
        return wmsStockMapper.selectWmsStockList(wmsStock);
    }

    @Override
    public int insertWmsStock(WmsStock wmsStock)
    {
        return wmsStockMapper.insertWmsStock(wmsStock);
    }

    @Override
    public int updateWmsStock(WmsStock wmsStock)
    {
        return wmsStockMapper.updateWmsStock(wmsStock);
    }

    @Override
    public int deleteWmsStockById(Long stockId)
    {
        return wmsStockMapper.deleteWmsStockById(stockId);
    }

    @Override
    public int deleteWmsStockByIds(Long[] stockIds)
    {
        return wmsStockMapper.deleteWmsStockByIds(stockIds);
    }
}
