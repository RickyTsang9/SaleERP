package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.BizMessage;

public interface IBizMessageService
{
    public List<BizMessage> selectBizMessageList(BizMessage bizMessage, Long userId);

    public List<BizMessage> selectPopupBizMessageList(Long userId, String messageType, Integer limitCount);

    public Long selectUnreadCount(Long userId, String messageType);

    public int markBizMessageRead(Long messageId, Long userId);

    public int markAllBizMessageRead(Long userId, String messageType);

    public boolean existsBizMessageByTypeAndTitle(String messageType, String messageTitle);

    public int insertBizMessage(BizMessage bizMessage);
}
