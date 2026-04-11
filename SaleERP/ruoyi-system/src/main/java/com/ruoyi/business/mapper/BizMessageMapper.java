package com.ruoyi.business.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.business.domain.BizMessage;

public interface BizMessageMapper
{
    public List<BizMessage> selectBizMessageList(@Param("bizMessage") BizMessage bizMessage, @Param("userId") Long userId);

    public List<BizMessage> selectPopupBizMessageList(@Param("userId") Long userId, @Param("messageType") String messageType,
        @Param("limitCount") Integer limitCount);

    public Long selectUnreadCount(@Param("userId") Long userId, @Param("messageType") String messageType);

    public List<BizMessage> selectUnreadBizMessageList(@Param("userId") Long userId, @Param("messageType") String messageType);

    public BizMessage selectBizMessageByTypeAndTitle(@Param("messageType") String messageType, @Param("messageTitle") String messageTitle);

    public int insertBizMessage(BizMessage bizMessage);

    public Long selectReadCount(@Param("messageId") Long messageId, @Param("userId") Long userId);

    public int insertBizMessageRead(@Param("messageId") Long messageId, @Param("userId") Long userId);
}
