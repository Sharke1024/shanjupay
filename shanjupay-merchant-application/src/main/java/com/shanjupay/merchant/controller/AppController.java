package com.shanjupay.merchant.controller;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.merchant.common.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.smartcardio.CommandAPDU;

/**
 * @Author DL_Wu
 * @Date 2020/4/11 19:57
 * @Version 1.0
 */
@Api(value = "商户平台‐应用管理", tags = "商户平台‐应用相关", description = "商户平台‐应用相关")
@RestController
@Slf4j
public class AppController {

    @org.apache.dubbo.config.annotation.Reference
    private AppService appService;

    @ApiOperation("商户创建应用")
    @ApiImplicitParam(name = "app",value = "应用信息",required = true,dataType = "AppDTO",paramType = "body")
    @PostMapping("/my/apps")
    public AppDTO createApp(@RequestBody AppDTO appDTO)throws BusinessException {
        if (appDTO == null){
            throw new BusinessException(CommonErrorCode.E_100108);
        }
        Long merchantId = SecurityUtil.getMerchantId();

        return appService.createApp(merchantId,appDTO);
    }

}
