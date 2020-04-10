package com.shanjupay.merchant.service.impl;

import com.alibaba.fastjson.JSON;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.merchant.service.SmsService;
import feign.Request;;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author DL_Wu
 * @Date 2020/4/7 11:40
 * @Version 1.0
 *
 * 手机短信服务接口实现
 */
@Service  //调用本地service服务
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Value("${sms.url}")
    private String smsUrl ;

    @Value("${sms.effectiveTime}")
    private String effectiveTime;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 发送手机验证码
     * @param phone 手机号
     * @return 验证码对应的key
     */
    @Override
    public String sendMsg(String phone) throws BusinessException{
        String url = smsUrl+"/generate?name=sms&effectiveTime="+effectiveTime;//effectiveTime;//验证码过期时间为 600秒 10分钟
        log.info("调用短信微服务：url:{}" + url);

        //请求体
        Map<String ,Object> body = new HashMap<>();
        body.put("mobile",phone);
        //请求头
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        //将请求封装
        HttpEntity entity = new HttpEntity(body,httpHeaders);

        ResponseEntity<Map> exchange = null;
        try{
             exchange = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        }catch (Exception e){
            log.info(e.getMessage(),e);
//            throw new RuntimeException("发送验证码出错");
            throw new BusinessException(CommonErrorCode.E_100107);  //E_100107:发送验证码出错
        }

        log.info("调用短信微服务发送验证码：返回值：{}", JSON.toJSONString(exchange));
        Map bodyMap = exchange.getBody();
        System.out.println(bodyMap);

        Map result = (Map)bodyMap.get("result");
        String key =(String) result.get("key");
        System.out.println(key);
        return key;
    }

    /**
     * 校验验证码，抛出异常则校验无效
     * @param verifiykey 验证码的key
     * @param verifiyCode   验证码
     */
    @Override
    public void checkCode(String verifiykey, String verifiyCode) throws BusinessException {
        String url = smsUrl+"/verify?name=sms&verificationCode="+verifiyCode+ "&verificationKey="+verifiykey;
        Map responseMap = null;
        try{
            ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, HttpEntity.EMPTY, Map.class);
            responseMap = exchange.getBody();
            log.info("校验验证码，响应内容：{}",JSON.toJSONString(responseMap));
        }catch (Exception e){
            log.info(e.getMessage(),e);
//            throw new RuntimeException("验证码错误");
            throw new BusinessException(CommonErrorCode.E_100102);
        }
        if (responseMap == null || responseMap.get("result") == null || !(Boolean)responseMap.get("result")){
//            throw new RuntimeException("验证码错误");
            throw new BusinessException(CommonErrorCode.E_100102);//E_100102:验证码错误
        }
    }
}
