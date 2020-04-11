package com.shanjupay.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.PhoneUtil;
import com.shanjupay.common.util.StringUtil;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.convert.MerchantConvert;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.mapper.MerchantMapper;
import jdk.nashorn.internal.ir.CallNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author DL_Wu
 * @Date 2020/4/6 10:20
 * @Version 1.0
 */
@org.apache.dubbo.config.annotation.Service
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    private MerchantMapper merchantMapper;

    /**
     * 根据id查询商户
     * @param id 商户Id
     * @return
     */
    @Override
    public MerchantDTO queryMerchantById(Long id) {
        Merchant merchant = merchantMapper.selectById(id);
        /*MerchantDTO merchantDTO = new MerchantDTO();
        merchantDTO.setId(merchant.getId());
        merchantDTO.setMerchantName(merchant.getMerchantName());*/
        return MerchantConvert.INSTANCE.entity2dto(merchant);
    }

    /**
     * 注册商户接口，接收账号、密码、手机号、为了可扩展性使用MerchantDTO接收数据
     * @param merchantDTO 商户注册信息
     * @return  注册成功的商户信息
     */
    @Override
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) throws BusinessException {
        //校验参数合法性
        if (merchantDTO == null){
            throw new BusinessException(CommonErrorCode.E_100108);  //传入对象为空
        }
        //手机号非空校验
        if (StringUtil.isBlank(merchantDTO.getMobile())){
            throw new BusinessException(CommonErrorCode.E_100112) ;//手机号为空
        }
        //验证手机号的合法性
        if (!PhoneUtil.isMatches(merchantDTO.getMobile())){
            throw new BusinessException(CommonErrorCode.E_100109);
        }
        //密码非空校验
        if (StringUtil.isBlank(merchantDTO.getPassword())){
            throw new BusinessException(CommonErrorCode.E_100111);
        }
        //校验商户手机号的唯一性,根据商户的手机号查询商户表，如果存在记录则说明已有相同的手机号重复
        LambdaQueryWrapper<Merchant> lambdaQueryWrapper = new LambdaQueryWrapper<Merchant>()
                .eq(Merchant::getMobile, merchantDTO.getMobile());
        Integer cout = merchantMapper.selectCount(lambdaQueryWrapper);
        if (cout> 0){
            throw new BusinessException(CommonErrorCode.E_100113);
        }

        Merchant merchant = MerchantConvert.INSTANCE.dto2entity(merchantDTO);
        //设置审核状态为0 - 未审核状态
        merchant.setAuditStatus("0");
        //调用mapper向数据库写入记录
        merchantMapper.insert(merchant);
        //向dto中写入新增商户id
        return MerchantConvert.INSTANCE.entity2dto(merchant);
    }

    /**
     * 资质申请
     * @param merchantId    商户id
     * @param merchantDTO   资质申请信息
     * @throws BusinessException
     */
    @Override
    @Transactional
    public void applyMerchant(Long merchantId, MerchantDTO merchantDTO) throws BusinessException {
        if (merchantId == null || merchantDTO == null){
            throw new BusinessException(CommonErrorCode.E_100108);//传入对象为空
        }
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null){
            throw new BusinessException(CommonErrorCode.E_200002);//商户不存在
        }
        Merchant merchant_update = MerchantConvert.INSTANCE.dto2entity(merchantDTO);
        merchant_update.setAuditStatus("1");  //1-已申请待审核
        merchant_update.setId(merchant.getId());//拿到商户id
        merchant_update.setMobile(merchant.getMobile());
        merchant_update.setTenantId(merchant.getTenantId());// 租户ID,关联统一用户
        //更新merchant信息
        merchantMapper.updateById(merchant_update);
    }


}
