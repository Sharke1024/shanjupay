package com.shanjupay.merchant;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author DL_Wu
 * @Date 2020/4/7 10:13
 * @Version 1.0
 *
 * restTemplate 远程获取 短信服务信息
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class RestTempleteTest {

    @Autowired
    private RestTemplate restTemplate;

    @Test
    public void getHtml(){
        String url  = "http://www.baidu.com";
        ResponseEntity<String> entity = restTemplate.getForEntity(url, String.class);
        String body = entity.getBody();
        System.out.println(body);
    }

    @Test
    public void getSmsCode(){
        String url = "http://localhost:56085/sailing/generate?effectiveTime=300&name=sms";
        log.info("调用短信微服务发送验证码：url{}",url);

        //请求体
        Map<String,Integer> body = new HashMap<>();
        body.put("mobile",12345324);
        //请求头
        HttpHeaders httpHeaders = new HttpHeaders();
        //设置数据格式为JSON
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        //疯转请求参数
        HttpEntity entity = new HttpEntity(body,httpHeaders);

        ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        log.info("请求验证码服务，得到参数:{}", JSON.toJSONString(exchange));
        Map bodyMap = exchange.getBody();
        System.out.println(bodyMap);
    }

}
