package com.shanjupay.transaction.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PlatformChannelDTO;

import java.util.List;

/**
 * @Author DL_Wu
 * @Date 2020/4/13 16:09
 * @Version 1.0
 * <p>
 * 支付渠道服务 管理平台支付渠道，原始支付渠道，以及相关配置
 */
public interface PayChannelService {

    /**
     *  获取平台服务类型
     * @return
     * @throws BusinessException
     */
    List<PlatformChannelDTO> queryPlatformChannel() throws BusinessException;

    /**
     * 为app绑定平台服务类型
     * @param appId 应用id
     * @param platformChannelCodes 平台服务类型列表
     */
    void bindPlatformChannelForApp (String appId, String platformChannelCodes) throws BusinessException;

    /**
     * 绑定服务状态的查询
     * @param appId
     * @param platformChannel
     * @return 已绑定返回1，否则 返回0
     * @throws BusinessException
     */
    int queryAppBindPlatformChannel(String appId,String platformChannel) throws BusinessException;

    /**
     * 根据服务类型查询支付渠道
     * @param platformChannelCode 服务类型编码
     * @return  支付渠道列表
     * @throws BusinessException
     */
    List<PayChannelDTO> queryPayChannelByPlatformChannel(String platformChannelCode) throws BusinessException;

    /**
     * 保存支付渠道参数
     * @param payChannelParamDTO    配置支付渠道参数： 包括： 商户id ，应用id， 服务类型code, 支付渠道code，配置名称，配置参数（json）
     * @throws BusinessException
     */
    void savePayChannelParam(PayChannelParamDTO payChannelParamDTO) throws BusinessException;

    /**
     * 获取指定应用指定服务类型下所包含的原始支付渠道参数列表
     * @param appId 应用id
     * @param platformChannel 服务类型
     * @return
     */
    List<PayChannelParamDTO>queryPayChannelParamByAppAndPlatform (String appId,String platformChannel)throws BusinessException;

    /**
     * 获取指定应用指定服务类型下所包含的某个原始支付参数
     * @param appId 应用id
     * @param platformChannel 服务类型
     * @param payChannel    支付渠道
     * @return
     * @throws BusinessException
     */
    PayChannelParamDTO queryParamByAppPlatformAndPayChannel(String appId,String platformChannel,String payChannel)throws BusinessException;


}
