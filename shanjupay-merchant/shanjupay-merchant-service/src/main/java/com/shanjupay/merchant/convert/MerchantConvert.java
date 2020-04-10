package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @Author DL_Wu
 * @Date 2020/4/9 15:45
 * @Version 1.0
 *
 * 将dto 与 entity 进行转换
 */
@Mapper
public interface MerchantConvert {

    MerchantConvert INSTANCE = Mappers.getMapper(MerchantConvert.class);

    //entity 转 dto
    MerchantDTO entity2dto(Merchant merchant);

    //dto 转 entity
    Merchant dto2entity(MerchantDTO merchantDTO);

}
