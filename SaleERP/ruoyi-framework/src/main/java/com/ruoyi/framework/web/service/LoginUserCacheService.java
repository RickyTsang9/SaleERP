package com.ruoyi.framework.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;

/**
 * 统一处理登录用户缓存读取逻辑
 * 
 * @author ruoyi
 */
@Component
public class LoginUserCacheService
{
    @Autowired
    private RedisCache redisCache;

    /**
     * 按缓存键读取登录用户，兼容 Redis 中不同反序列化结构
     *
     * @param cacheKey 缓存键
     * @return 登录用户
     */
    public LoginUser getLoginUserByCacheKey(String cacheKey)
    {
        Object cacheObject = redisCache.getCacheObject(cacheKey);
        if (cacheObject == null)
        {
            return null;
        }
        if (cacheObject instanceof LoginUser)
        {
            return (LoginUser) cacheObject;
        }
        if (cacheObject instanceof JSONObject)
        {
            return ((JSONObject) cacheObject).to(LoginUser.class);
        }
        return JSON.parseObject(JSON.toJSONString(cacheObject), LoginUser.class);
    }
}
