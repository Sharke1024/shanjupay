package com.shanjupay.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.additional.query.impl.QueryChainWrapper;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.merchant.api.AppService;
import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.merchant.convert.AppConvert;
import com.shanjupay.merchant.entity.App;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.mapper.AppMapper;
import com.shanjupay.merchant.mapper.MerchantMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

/**
 * @Author DL_Wu
 * @Date 2020/4/11 19:23
 * @Version 1.0
 */

@org.apache.dubbo.config.annotation.Service   // 供其他服务远程调用
public class AppServiceImpl implements AppService {

    @Autowired
    private AppMapper appMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    /**
     * 商户下创建应用
     * @param merchantId 商户Id
     * @param appDTO    APP应用信息
     * @return  创建成功的应用信息
     * @throws BusinessException
     */
    @Override
    public AppDTO createApp(Long merchantId, AppDTO appDTO) throws BusinessException {
        if (merchantId == null || appDTO == null ){
            throw new BusinessException(CommonErrorCode.E_110006);//参数为空
        }
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null ){
            throw new BusinessException(CommonErrorCode.E_200002);  //商户不存在
        }
        //判断是否通过审核
        if ( !"2".equals(merchant.getAuditStatus())){
            throw new BusinessException(CommonErrorCode.E_200003);
        }
        if (isExistAppName(appDTO.getAppName())){
            throw new BusinessException(CommonErrorCode.E_200004);  //应用已存在
        }

        //保存应用信息
        appDTO.setAppId(UUID.randomUUID().toString());
        appDTO.setMerchantId(merchant.getId());
        App app = AppConvert.INSTANCE.dto2entity(appDTO);
        appMapper.insert(app);

        return AppConvert.INSTANCE.entity2dto(app);
    }

    //判断是否存在appName
    public Boolean isExistAppName(String appName){
        Integer count = appMapper.selectCount(new QueryWrapper<App>().lambda().eq(App::getAppName, appName));
        return count.intValue() > 0;
    }

}
