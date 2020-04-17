package com.shanjupay.merchant.convert;

import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.entity.Staff;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @Author DL_Wu
 * @Date 2020/4/16 17:59
 * @Version 1.0
 */
@Mapper
public interface StaffConvert {

    StaffConvert INSTANS = Mappers.getMapper(StaffConvert.class);

    StaffDTO entity2dto(Staff staff);

    Staff dto2entity(StaffDTO staffDTO);

}
