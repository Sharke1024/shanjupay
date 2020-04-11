package com.shanjupay.merchant.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.merchant.api.dto.AppDTO;

/**
 * @Author DL_Wu
 * @Date 2020/4/11 19:19
 * @Version 1.0
 *
 * 应用服务接口
 */
public interface AppService {

    /**
     * 商户下创建应用
     * @param merchantId 商户Id
     * @param appDTO    APP应用信息
     * @return      创建成功的应用信息
     * @throws BusinessException
     */
    AppDTO createApp(Long merchantId, AppDTO appDTO)throws BusinessException;

}
