package com.shanjupay.merchant.controller;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.StringUtil;
import com.shanjupay.merchant.common.util.SecurityUtil;
import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
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

    @ApiOperation("根据平台服务类型获取支付渠道列表")
    @ApiImplicitParam(value = "服务类型编码",name = "platformChannelCode" ,dataType = "String",paramType = "path")
    @GetMapping("/my/pay-channels/platform-channel/{platformChannelCode}")
    public List<PayChannelDTO> queryPayChannelByPlatformChannel(@PathVariable("platformChannelCode") String platformChannelCode)throws BusinessException{
        return payChannelService.queryPayChannelByPlatformChannel(platformChannelCode);
    }

    @ApiOperation("商户配置支付渠道参数")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "商户支付渠道参数",name = "payChannelParamDTO" ,dataType = "PayChannelParamDTO",
                    required = true, paramType = "body")
    })
    @RequestMapping(value = "/my/pay-channels",method = {RequestMethod.POST,RequestMethod.POST})  //在更新使用put，新建使用post
    public void createPayChannelParam(@RequestBody PayChannelParamDTO payChannelParamDTO)throws BusinessException{
        if (payChannelParamDTO == null || StringUtil.isBlank(payChannelParamDTO.getChannelName())){
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        Long merchantId = SecurityUtil.getMerchantId();
        payChannelParamDTO.setMerchantId(merchantId);
        payChannelService.savePayChannelParam(payChannelParamDTO);
    }

    @ApiOperation("根据应用和服务类型获取支付渠道参数列表")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "应用id",name = "appId" ,dataType = "String ",
                    required = true, paramType = "path"),
            @ApiImplicitParam(value = "服务类型",name = "platformChannel",dataType = "String",
                    required = true,paramType = "path")

    })
    @GetMapping("/my/pay-channels/apps/{appId}/platform-channels/{platformChannel}")
    public List<PayChannelParamDTO> queryPayChannelParam(@PathVariable("appId") String appId,
                                                         @PathVariable("platformChannel")String platformChannel){
        if (StringUtil.isBlank(appId) || StringUtil.isBlank(platformChannel) ){
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        return  payChannelService.queryPayChannelParamByAppAndPlatform(appId,platformChannel);
    }

    @ApiOperation("根据应用和服务类型和支付渠道获取单个支付渠道参数")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "应用id",name = "appId" ,dataType = "String ",
                    required = true, paramType = "path"),
            @ApiImplicitParam(value = "服务类型",name = "platformChannel",dataType = "String",
                    required = true,paramType = "path"),
            @ApiImplicitParam(value = "实际支付渠道编码",name = "payChannel",dataType = "String",
                    required = true,paramType = "path")

    })
    @GetMapping("/my/pay-channels/apps/{appId}/platform-channels/{platformChannel}/pay-channels/{payChannel}")
    public PayChannelParamDTO queryPayChannelParam(@PathVariable("appId") String appId,
                                                         @PathVariable("platformChannel")String platformChannel,
                                                         @PathVariable("payChannel")String payChannel){
        if (StringUtil.isBlank(appId) || StringUtil.isBlank(platformChannel) ||StringUtil.isBlank(payChannel) ){
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        return  payChannelService.queryParamByAppPlatformAndPayChannel(appId,platformChannel,payChannel);
    }





}
