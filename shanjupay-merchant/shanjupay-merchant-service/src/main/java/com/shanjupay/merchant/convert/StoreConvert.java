package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.StoreDTO;
import com.shanjupay.merchant.entity.Store;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @Author DL_Wu
 * @Date 2020/4/16 17:49
 * @Version 1.0
 */
@Mapper
public interface StoreConvert   {

    StoreConvert INSTANS =  Mappers.getMapper(StoreConvert.class);

    //entity 2 dto
    StoreDTO entity2dto (Store store);

    //dto 2 entity
    Store dto2entity(StoreDTO storeDTO);

}
