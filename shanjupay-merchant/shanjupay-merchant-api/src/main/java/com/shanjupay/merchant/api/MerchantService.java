package com.shanjupay.merchant.api;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;

/**
 * @Author DL_Wu
 * @Date 2020/4/4 16:26
 * @Version 1.0
 */
public interface MerchantService {

    /**
     * 根据id查询商户
     * @param id 商户Id
     * @return
     */
    public MerchantDTO queryMerchantById(Long id);

    /**
     *根据租户Id查询商户信息
     * @param tenantId
     * @return
     */
    MerchantDTO queryMerchantByTenantId(Long tenantId);

    /**
     * 注册商户接口，接收账号、密码、手机号、为了可扩展性使用MerchantDTO接收数据
     * @param merchantDTO 商户注册信息
     * @return  注册成功的商户信息
     */
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) throws BusinessException;

    /**
     * 资质申请
     * @param merchantId    商户id
     * @param merchantDTO   资质申请信息
     * @throws BusinessException
     */
    public void applyMerchant(Long merchantId, MerchantDTO merchantDTO)throws BusinessException;

    /**
     * 商户下新增门店
     * @param storeDTO
     * @return  新增门店
     * @throws BusinessException
     */
    StoreDTO createStore(StoreDTO storeDTO)throws BusinessException;

    /**
     * 商户下新增员工
     * @param staffDTO
     * @return  新增员工
     * @throws BusinessException
     */
    StaffDTO createStaff(StaffDTO staffDTO)throws BusinessException;

    /**
     * 绑定门店与员工关系
     * @param storeId
     * @param staffId
     * @throws BusinessException
     */
    void bindStoreToStaff(Long storeId,Long  staffId)throws BusinessException;


}
