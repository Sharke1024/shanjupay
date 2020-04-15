package com.shanjupay.transaction.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shanjupay.common.cache.Cache;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.RedisUtil;
import com.shanjupay.common.util.StringUtil;
import com.shanjupay.transaction.api.PayChannelService;
import com.shanjupay.transaction.api.dto.PayChannelDTO;
import com.shanjupay.transaction.api.dto.PayChannelParamDTO;
import com.shanjupay.transaction.api.dto.PlatformChannelDTO;
import com.shanjupay.transaction.convert.PayChannelParamConvert;
import com.shanjupay.transaction.convert.PlatformChannelConvert;
import com.shanjupay.transaction.entity.AppPlatformChannel;
import com.shanjupay.transaction.entity.PayChannelParam;
import com.shanjupay.transaction.entity.PlatformChannel;
import com.shanjupay.transaction.mapper.AppPlatformChannelMapper;
import com.shanjupay.transaction.mapper.PayChannelParamMapper;
import com.shanjupay.transaction.mapper.PlatformChannelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @Author DL_Wu
 * @Date 2020/4/13 16:30
 * @Version 1.0
 */
@org.apache.dubbo.config.annotation.Service
public class PayChannelServiceImpl implements PayChannelService {

    @Autowired
    private Cache cache;

    @Autowired
    private PlatformChannelMapper platformChannelMapper;

    @Autowired
    private AppPlatformChannelMapper appPlatformChannelMapper;

    @Autowired
    private PayChannelParamMapper payChannelParamMapper;


    /**
     * 查询所有支付服务列表
     * @return
     * @throws BusinessException
     */
    @Override
    public List<PlatformChannelDTO> queryPlatformChannel() throws BusinessException {
        List<PlatformChannel> platformChannels = platformChannelMapper.selectList(null);
        return PlatformChannelConvert.INSTANCE.listentity2listdto(platformChannels);

    }

    /**
     * 为app绑定平台服务类型
     * @param appId 应用id
     * @param platformChannelCodes 平台服务类型列表
     */
    @Override
    public void bindPlatformChannelForApp(String appId, String platformChannelCodes) throws BusinessException {
        AppPlatformChannel appPlatformChannel = appPlatformChannelMapper.selectOne(new QueryWrapper<AppPlatformChannel>().lambda()
                .eq(AppPlatformChannel::getAppId, appId)
                .eq(AppPlatformChannel::getPlatformChannel, platformChannelCodes));
        if (appPlatformChannel == null){
            appPlatformChannel = new AppPlatformChannel();
            appPlatformChannel.setAppId(appId);
            appPlatformChannel.setPlatformChannel(platformChannelCodes);
            appPlatformChannelMapper.insert(appPlatformChannel);
        }
    }

    /**
     * 绑定服务状态的查询
     * @param appId
     * @param platformChannel
     * @return 已绑定返回1，否则 返回0
     * @throws BusinessException
     */
    @Override
    public int queryAppBindPlatformChannel(String appId, String platformChannel) throws BusinessException {
        int count = appPlatformChannelMapper.selectCount(new QueryWrapper<AppPlatformChannel>().lambda()
                .eq(AppPlatformChannel::getAppId, appId)
                .eq(AppPlatformChannel::getPlatformChannel, platformChannel));
        if (count>0) {
            return 1;
        }
        return 0;
    }

    /**
     * 根据服务类型查询支付渠道
     * @param platformChannelCode 服务类型编码
     * @return  支付渠道列表
     * @throws BusinessException
     */
    @Override
    public List<PayChannelDTO> queryPayChannelByPlatformChannel(String platformChannelCode) throws BusinessException {
        //调用Mapper 查询数据库 platform_pay_channel, pay_channel ,platform_channel
        return  platformChannelMapper.selectPayChannelByPlatformChannel(platformChannelCode);
    }

    /**
     * 保存支付渠道参数
     * @param payChannelParam  配置支付渠道参数： 包括： 商户id ，应用id， 服务类型code, 支付渠道code，配置名称，配置参数（json）
     * @throws BusinessException
     */
    @Override
    public void savePayChannelParam(PayChannelParamDTO payChannelParam) throws BusinessException {
        if(payChannelParam == null || payChannelParam.getChannelName() == null || payChannelParam.getParam()== null){
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        //根据应用、服务类型、支付渠道查询一条记录
        //根据应用、服务类型查询应用与服务类型的绑定id
        Long appPlatformChannelId = selectIdByAppPlatformChannel(payChannelParam.getAppId(), payChannelParam.getPlatformChannelCode());
        if(appPlatformChannelId == null){
            throw new BusinessException(CommonErrorCode.E_300010);
        }
        //根据应用与服务类型的绑定id和支付渠道查询PayChannelParam的一条记录
        PayChannelParam entity = payChannelParamMapper.selectOne(new LambdaQueryWrapper<PayChannelParam>()
                .eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId)
                .eq(PayChannelParam::getPayChannel, payChannelParam.getPayChannel()));
        //如果存在配置则更新
        if(entity != null){
            entity.setChannelName(payChannelParam.getChannelName());//配置名称
            entity.setParam(payChannelParam.getParam());//json格式的参数
            payChannelParamMapper.updateById(entity);
        }else{
            //否则添加配置
            PayChannelParam entityNew = PayChannelParamConvert.INSTANCE.dto2entity(payChannelParam);
            entityNew.setId(null);
            entityNew.setAppPlatformChannelId(appPlatformChannelId);//应用与服务类型绑定关系id
            payChannelParamMapper.insert(entityNew);
        }

        //保存到redis
        updateCache(payChannelParam.getAppId(),payChannelParam.getPlatformChannelCode());
    }

    /**
     * 更新缓存
     * @param appId 应用id
     * @param platformChannel   服务类型
     */
    private void updateCache(String appId, String platformChannel){
        //处理redis缓存
        //1.构建key
        String redisKey = RedisUtil.keyBuilder(appId, platformChannel);
        //2.查询redis，检查key是否存在
        Boolean exists = cache.exists(redisKey);
        if (exists){  //存在则清除
            cache.del(redisKey);
        }
        //3.从数据库查询应用的服务类型对应的实际支付参数，并重新存入缓存
        Long appPlatformChannelId = selectIdByAppPlatformChannel(appId, platformChannel);
        if (appPlatformChannelId != null){
            //应用和服务类型绑定id查询支付渠道参数记录
            List<PayChannelParam> payChannelParams = payChannelParamMapper.selectList(new QueryWrapper<PayChannelParam>().lambda()
                    .eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId));
            List<PayChannelParamDTO> payChannelParamDTOS = PayChannelParamConvert.INSTANCE.listentity2listdto(payChannelParams);
            //将payChannelParamDTOS转成Json字符串存入redis
            cache.set(redisKey, JSON.toJSON(payChannelParamDTOS).toString());
        }
    }


    /**
     * 获取指定应用指定服务类型下所包含的原始支付渠道参数列表
     * @param appId 应用id
     * @param platformChannel 服务类型
     * @return
     */
    @Override
    public List<PayChannelParamDTO> queryPayChannelParamByAppAndPlatform(String appId, String platformChannel) throws BusinessException {
        if (StringUtil.isBlank(appId) || StringUtil.isBlank(platformChannel) ){
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        //从缓存查询
        //1.构建key
        String redisKey = RedisUtil.keyBuilder(appId, platformChannel);
        //是否有缓存
        Boolean exists = cache.exists(redisKey);
        if (exists){
            //从redis中获取key对应的value
            String value = cache.get(redisKey);
            //将value转成对象
            List<PayChannelParamDTO> payChannelParamDTOS = JSONObject.parseArray(value, PayChannelParamDTO.class);
            return payChannelParamDTOS;
        }

        //查出应用id和服务类型代码在app_platform_channel主键
        Long appPlatformChannelId = selectIdByAppPlatformChannel(appId, platformChannel);
        List<PayChannelParam> payChannelParams = payChannelParamMapper.selectList(new QueryWrapper<PayChannelParam>().lambda()
                .eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId));
        //存入缓存
        updateCache(appId,platformChannel);
        return PayChannelParamConvert.INSTANCE.listentity2listdto(payChannelParams);
    }

    /**
     * 获取指定应用指定服务类型下所包含的某个原始支付参数
     * @param appId 应用id
     * @param platformChannel 服务类型
     * @param payChannel    支付渠道
     * @return
     * @throws BusinessException
     */
    @Override
    public PayChannelParamDTO queryParamByAppPlatformAndPayChannel(String appId, String platformChannel, String payChannel) throws BusinessException {
        if (StringUtil.isBlank(appId) || StringUtil.isBlank(platformChannel) || StringUtil.isBlank(payChannel)) {
            throw new BusinessException(CommonErrorCode.E_300009);
        }
        Long appPlatformChannelId = selectIdByAppPlatformChannel(appId, platformChannel);
        PayChannelParam payChannelParam = payChannelParamMapper.selectOne(new QueryWrapper<PayChannelParam>().lambda().eq(PayChannelParam::getPayChannel, payChannel)
                .eq(PayChannelParam::getAppPlatformChannelId, appPlatformChannelId));
        return PayChannelParamConvert.INSTANCE.entity2dto(payChannelParam);
    }

    /**
     * 根据应用、服务类型查询应用与服务类型的绑定id
     * @param appId
     * @param platformChannelCode
     * @return
     */
    private Long selectIdByAppPlatformChannel(String appId,String platformChannelCode){
        AppPlatformChannel appPlatformChannel = appPlatformChannelMapper.selectOne(new LambdaQueryWrapper<AppPlatformChannel>().eq(AppPlatformChannel::getAppId, appId)
                .eq(AppPlatformChannel::getPlatformChannel, platformChannelCode));
        if(appPlatformChannel!=null){
            return appPlatformChannel.getId();//应用与服务类型的绑定id
        }
        return null;
    }

}
