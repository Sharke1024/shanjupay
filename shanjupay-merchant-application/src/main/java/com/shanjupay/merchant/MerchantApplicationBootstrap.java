package com.shanjupay.merchant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @Author DL_Wu
 * @Date 2020/4/3 19:19
 * @Version 1.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class MerchantApplicationBootstrap {

    public static void main(String[] args) {
        SpringApplication.run(MerchantApplicationBootstrap.class,args);
    }


    /**
     * 当RestTemplate默认使用String存储body内容时默认使用ISO_8859_1字符集
     * 需要修改编码格式 为utf-8 ,  才不会出现网页乱码
     * @return
     */
    @Bean
    public RestTemplate restTemplate(){
        RestTemplate restTemplate = new RestTemplate(new OkHttp3ClientHttpRequestFactory());
        //消息转换器列表
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        //配置消息转换器StringHttpMessageConverter，并设置utf‐8
        messageConverters.set(1,new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }

}
