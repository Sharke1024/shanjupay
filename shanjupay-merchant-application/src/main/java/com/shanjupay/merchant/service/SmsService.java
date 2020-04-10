package com.shanjupay.merchant.service;

import com.shanjupay.common.domain.BusinessException;

/**
 * @Author DL_Wu
 * @Date 2020/4/7 11:37
 * @Version 1.0
 * <p>
 * 手机短信服务接口
 */
public interface SmsService {

    /**
     * 获取短信验证码
     */
    String sendMsg(String phone) throws BusinessException;

    /**
     * 校验验证码，抛出异常则校验无效
     * @param verifiykey 验证码的key
     * @param verifiyCode   验证码
     */
    void checkCode(String verifiykey, String verifiyCode)throws BusinessException;

}
