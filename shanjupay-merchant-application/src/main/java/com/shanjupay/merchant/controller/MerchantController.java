package com.shanjupay.merchant.controller;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.PhoneUtil;
import com.shanjupay.common.util.StringUtil;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.common.LoginUser;
import com.shanjupay.merchant.common.SecurityUtil;
import com.shanjupay.merchant.convert.MerchantDetailConvert;
import com.shanjupay.merchant.convert.MerchantRegisterConvert;
import com.shanjupay.merchant.service.FileService;
import com.shanjupay.merchant.service.SmsService;
import com.shanjupay.merchant.vo.MerchantDetailVO;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * @Author DL_Wu
 * @Date 2020/4/6 10:33
 * @Version 1.0
 */
@Api(value = "商户平台应用", tags = "商户平台应用",description = "商户平台应用")
@RestController
@Slf4j
public class MerchantController {

    //商户服务接口
    @org.apache.dubbo.config.annotation.Reference   //Dubbo远程调用接口
    private MerchantService merchantService;

    //短信服务接口
    @Autowired    //本地接口的注入
    private SmsService smsService;

    //文件服务接口
    @Autowired
    private FileService fileService;



    @ApiOperation("根据merchant Id查询")
    @GetMapping("/merchants/{id}")
    public MerchantDTO queryMerchantById(@PathVariable("id") Long id){
        MerchantDTO merchantDTO = merchantService.queryMerchantById(id);
        return merchantDTO ;
    }

    @ApiOperation("获取手机验证码")
    @ApiImplicitParam(name="phone",value = "手机号",required = true, dataType = "String",paramType ="query")
    @GetMapping("/sms")
    public String getSmsCode(@RequestParam("phone") String phone) {
        log.info("向手机号:{}发送验证码", phone);
        return smsService.sendMsg(phone);
    }


    @ApiOperation("注册商户")
    @ApiImplicitParam(name = "merchantRegister",value = "注册信息",required = true,
            dataType = "MerchantRegisterVO",paramType = "body")
    @PostMapping("/merchant/register")
    public MerchantRegisterVO registerMerchant(@RequestBody MerchantRegisterVO merchantRegister){
        //校验参数合法性

        if (merchantRegister == null){
            throw new BusinessException(CommonErrorCode.E_100108);  //传入对象为空
        }
        //手机号非空校验
        if (StringUtil.isBlank(merchantRegister.getMobile())){
            throw new BusinessException(CommonErrorCode.E_100112) ;//手机号为空
        }
        //验证手机号的合法性
        if (!PhoneUtil.isMatches(merchantRegister.getMobile())){
            throw new BusinessException(CommonErrorCode.E_100109);
        }
        //联系人非空校验
        if (StringUtil.isBlank(merchantRegister.getUsername())) {
            throw new BusinessException(CommonErrorCode.E_100110);
        }
        //密码非空校验
        if (StringUtil.isBlank(merchantRegister.getPassword())) {
            throw new BusinessException(CommonErrorCode.E_100111);
        }
        //验证码非空校验
        if (StringUtil.isBlank(merchantRegister.getVerifiyCode()) ||
                StringUtil.isBlank(merchantRegister.getVerifiykey())) {
            throw new BusinessException(CommonErrorCode.E_100103);
        }

        //校验验证码
        smsService.checkCode(merchantRegister.getVerifiykey(),merchantRegister.getVerifiyCode());
        //将 转为DTO
        MerchantDTO merchantDTO = MerchantRegisterConvert.INSTANCE.vo2dto(merchantRegister);
        //调用dubbo服务
        merchantService.createMerchant(merchantDTO);
        return merchantRegister;
    }


    @ApiOperation("上传证件照")
    @PostMapping("/upload")
    public String upload(@ApiParam(value = "上传的文件",required = true) @RequestParam MultipartFile file)throws IOException{
        byte[] bytes = file.getBytes();
        //原始文件名称
        String originalFilename =file.getOriginalFilename();
        //文件后缀名
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".") );
        //文件名称
        String fileName = UUID.randomUUID().toString()+suffix;
        //fileService完成证件上传,返回下载路径
        return fileService.upLoad(bytes, fileName);
    }

    @ApiOperation("商户资质申请")
    @PostMapping("/my/merchants/save")
    @ApiImplicitParam(name = "merchantInfo",value = "商户认证资料",required = true,dataType = "MerchantDetailVO",paramType = "body")
    public void applyMerchant(@RequestBody MerchantDetailVO merchantDetailVO)throws BusinessException{
        Long merchantId = SecurityUtil.getMerchantId();
        MerchantDTO merchantDTO = MerchantDetailConvert.INSTANCE.vo2dto(merchantDetailVO);
        merchantService.applyMerchant(merchantId,merchantDTO);
    }

}
