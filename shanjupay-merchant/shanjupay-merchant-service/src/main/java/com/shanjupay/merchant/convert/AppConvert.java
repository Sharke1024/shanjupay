package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.AppDTO;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.entity.App;
import com.shanjupay.merchant.entity.Merchant;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Author DL_Wu
 * @Date 2020/4/9 15:45
 * @Version 1.0
 *
 * 将dto 与 entity 进行转换
 */
@Mapper
public interface AppConvert {

    AppConvert INSTANCE = Mappers.getMapper(AppConvert.class);

    //entity 转 dto
    AppDTO entity2dto(App app);

    //dto 转 entity
    App dto2entity(AppDTO appDTO);

    //集合entity 转 DTO
    List<AppDTO> listEntity2DTO (List<App> app);

}
