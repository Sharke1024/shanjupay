package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.vo.MerchantDetailVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @Author DL_Wu
 * @Date 2020/4/11 17:08
 * @Version 1.0
 *
 * 将商户资质申请 VO 转为DTO
 */
@Mapper
public interface MerchantDetailConvert {

    MerchantDetailConvert INSTANCE = Mappers.getMapper(MerchantDetailConvert.class);

    //将VO 转DTO
    MerchantDTO vo2dto (MerchantDetailVO merchantDetailVO);

    //将DTO 转VO
    MerchantDetailVO dto2vo (MerchantDTO merchantDTO);

}
