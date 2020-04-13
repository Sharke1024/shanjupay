package com.shanjupay.merchant.controller;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.dto.PlatformChannelDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 平台支付参数相关的controller
 * @Author DL_Wu
 * @Date 2020/4/13 16:16
 * @Version 1.0
 */
@Api(value = "商户平台‐渠道和支付参数相关", tags = "商户平台‐渠道和支付参数", description = "商户平 台‐渠道和支付参数相关")
@RestController
@Slf4j
public class PlatformParamController {

    @org.apache.dubbo.config.annotation.Reference
    private PayChannelService payChannelService;

    @ApiOperation("获取平台服务类型")
    @GetMapping("/my/platform-channels")
    public List<PlatformChannelDTO> queryPlatformChanel(){
        return payChannelService.queryPlatformChannel();
    }

    @ApiOperation("应用绑定服务类型")
    @PostMapping("/my/apps/{appId}/platform-channels")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "应用Id",name = "appId" ,dataType = "String",paramType = "path"),
            @ApiImplicitParam(value = "服务类型Code",name = "platformChannelsCodes",dataType = "String",paramType = "query")})
    public void bindPlatformForApp(@PathVariable("appId")String appId,
                                   @RequestParam("platformChannelsCodes")String platformChannelsCodes )throws BusinessException{
        payChannelService.bindPlatformChannelForApp(appId,platformChannelsCodes);
    }

    @ApiOperation("应用绑定服务状态的查询")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "应用Id",name = "appId" ,dataType = "String",paramType = "query"),
            @ApiImplicitParam(value = "应用服务类型",name = "platformChannel",dataType = "String",paramType = "query")
    })
    @GetMapping("/my/apps/platform-channels")
    public int queryAppBindPlatformChannel(@RequestParam("appId")String appId,
                                           @RequestParam("platformChannel")String platformChannel){
        return payChannelService.queryAppBindPlatformChannel(appId,platformChannel);
    }

}
