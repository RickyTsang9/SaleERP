package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.business.domain.BizMessage;
import com.ruoyi.business.mapper.BizMessageMapper;
import com.ruoyi.business.service.IBizMessageService;

@Service
public class BizMessageServiceImpl implements IBizMessageService
{
    @Autowired
    private BizMessageMapper bizMessageMapper;

    @Override
    public List<BizMessage> selectBizMessageList(BizMessage bizMessage, Long userId)
    {
        return bizMessageMapper.selectBizMessageList(bizMessage, userId);
    }

    @Override
    public List<BizMessage> selectPopupBizMessageList(Long userId, String messageType, Integer limitCount)
    {
        return bizMessageMapper.selectPopupBizMessageList(userId, messageType, limitCount);
    }

    @Override
    public Long selectUnreadCount(Long userId, String messageType)
    {
        return bizMessageMapper.selectUnreadCount(userId, messageType);
    }

    @Override
    public int markBizMessageRead(Long messageId, Long userId)
    {
        Long readCount = bizMessageMapper.selectReadCount(messageId, userId);
        if (readCount != null && readCount > 0)
        {
            return 1;
        }
        return bizMessageMapper.insertBizMessageRead(messageId, userId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int markAllBizMessageRead(Long userId, String messageType)
    {
        List<BizMessage> unreadBizMessageList = bizMessageMapper.selectUnreadBizMessageList(userId, messageType);
        int readCount = 0;
        for (BizMessage unreadBizMessage : unreadBizMessageList)
        {
            readCount += bizMessageMapper.insertBizMessageRead(unreadBizMessage.getMessageId(), userId);
        }
        return readCount;
    }

    @Override
    public boolean existsBizMessageByTypeAndTitle(String messageType, String messageTitle)
    {
        BizMessage existingBizMessage = bizMessageMapper.selectBizMessageByTypeAndTitle(messageType, messageTitle);
        return existingBizMessage != null;
    }

    @Override
    public int insertBizMessage(BizMessage bizMessage)
    {
        return bizMessageMapper.insertBizMessage(bizMessage);
    }
}
