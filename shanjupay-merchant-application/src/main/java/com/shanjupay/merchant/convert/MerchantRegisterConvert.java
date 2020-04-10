package com.shanjupay.merchant.convert;


import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.vo.MerchantRegisterVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @Author DL_Wu
 * @Date 2020/4/9 15:33
 * @Version 1.0
 *
 * 将商户注册vo 与dto转换
 */
@Mapper
public interface MerchantRegisterConvert {

    MerchantRegisterConvert INSTANCE = Mappers.getMapper(MerchantRegisterConvert.class);

    //vo 转 DTO
    MerchantDTO vo2dto(MerchantRegisterVO merchantRegisterVO);

    //dto 转 vo
    MerchantRegisterVO dto2vo(MerchantDTO merchantDTO);

}
