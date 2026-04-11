package com.ruoyi.web.controller.business;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.business.domain.BizMessage;
import com.ruoyi.business.service.IBizMessageService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;

@RestController
@RequestMapping("/business/message")
public class BizMessageController extends BaseController
{
    private static final Integer DEFAULT_POPUP_LIMIT = 5;

    @Autowired
    private IBizMessageService bizMessageService;

    @PreAuthorize("@ss.hasPermi('business:stock:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizMessage bizMessage)
    {
        Long userId = SecurityUtils.getUserId();
        startPage();
        List<BizMessage> bizMessageList = bizMessageService.selectBizMessageList(bizMessage, userId);
        return getDataTable(bizMessageList);
    }

    @PreAuthorize("@ss.hasPermi('business:stock:list')")
    @GetMapping("/popupList")
    public AjaxResult popupList(@RequestParam(required = false) String messageType, @RequestParam(required = false) Integer limitCount)
    {
        Long userId = SecurityUtils.getUserId();
        int queryLimitCount = limitCount == null || limitCount <= 0 ? DEFAULT_POPUP_LIMIT : limitCount;
        List<BizMessage> popupBizMessageList = bizMessageService.selectPopupBizMessageList(userId, messageType, queryLimitCount);
        AjaxResult ajaxResult = AjaxResult.success();
        ajaxResult.put("rows", popupBizMessageList);
        ajaxResult.put("total", popupBizMessageList.size());
        return ajaxResult;
    }

    @PreAuthorize("@ss.hasPermi('business:stock:list')")
    @GetMapping("/unreadCount")
    public AjaxResult unreadCount(@RequestParam(required = false) String messageType)
    {
        Long userId = SecurityUtils.getUserId();
        Long unreadCount = bizMessageService.selectUnreadCount(userId, messageType);
        AjaxResult ajaxResult = AjaxResult.success();
        ajaxResult.put("unreadCount", unreadCount == null ? 0L : unreadCount);
        return ajaxResult;
    }

    @PreAuthorize("@ss.hasPermi('business:stock:list')")
    @PostMapping("/read/{messageId}")
    public AjaxResult read(@PathVariable Long messageId)
    {
        Long userId = SecurityUtils.getUserId();
        return toAjax(bizMessageService.markBizMessageRead(messageId, userId));
    }

    @PreAuthorize("@ss.hasPermi('business:stock:list')")
    @PostMapping("/readAll")
    public AjaxResult readAll(@RequestParam(required = false) String messageType)
    {
        Long userId = SecurityUtils.getUserId();
        int readCount = bizMessageService.markAllBizMessageRead(userId, messageType);
        AjaxResult ajaxResult = AjaxResult.success();
        ajaxResult.put("readCount", readCount);
        return ajaxResult;
    }
}
