package com.shanjupay.merchant.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.PhoneUtil;
import com.shanjupay.common.util.StringUtil;
import com.shanjupay.merchant.api.MerchantService;
import com.shanjupay.merchant.api.dto.MerchantDTO;
import com.shanjupay.merchant.api.dto.StaffDTO;
import com.shanjupay.merchant.api.dto.StoreDTO;
import com.shanjupay.merchant.convert.MerchantConvert;
import com.shanjupay.merchant.convert.StaffConvert;
import com.shanjupay.merchant.convert.StoreConvert;
import com.shanjupay.merchant.entity.Merchant;
import com.shanjupay.merchant.entity.Staff;
import com.shanjupay.merchant.entity.Store;
import com.shanjupay.merchant.entity.StoreStaff;
import com.shanjupay.merchant.mapper.MerchantMapper;
import com.shanjupay.merchant.mapper.StaffMapper;
import com.shanjupay.merchant.mapper.StoreMapper;
import com.shanjupay.merchant.mapper.StoreStaffMapper;
import com.shanjupay.user.api.TenantService;
import com.shanjupay.user.api.dto.tenant.CreateTenantRequestDTO;
import com.shanjupay.user.api.dto.tenant.TenantDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author DL_Wu
 * @Date 2020/4/6 10:20
 * @Version 1.0
 */
@org.apache.dubbo.config.annotation.Service
@Slf4j
public class MerchantServiceImpl implements MerchantService {



    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private StaffMapper staffMapper;

    @Autowired
    private StoreMapper storeMapper;

    @Autowired
    private StoreStaffMapper storeStaffMapper;

    @org.apache.dubbo.config.annotation.Reference
    private TenantService tenantService;


    /**
     * 根据id查询商户
     * @param id 商户Id
     * @return
     */
    @Override
    public MerchantDTO queryMerchantById(Long id) {
        Merchant merchant = merchantMapper.selectById(id);
        return MerchantConvert.INSTANCE.entity2dto(merchant);
    }

    /**
     *根据租户Id查询商户信息
     * @param tenantId
     * @return
     */
    @Override
    public MerchantDTO queryMerchantByTenantId(Long tenantId) {
        Merchant merchant = merchantMapper.selectOne(new QueryWrapper<Merchant>().lambda().eq(Merchant::getTenantId, tenantId));
        return MerchantConvert.INSTANCE.entity2dto(merchant);
    }


    /**
     * 注册商户接口，接收账号、密码、手机号、为了可扩展性使用MerchantDTO接收数据
     *  调用SaaS接口：新增租户、用户、绑定租户和用户的关系，初始化权限
     * @param merchantDTO 商户注册信息
     * @return  注册成功的商户信息
     */
    @Override
    @Transactional
    public MerchantDTO createMerchant(MerchantDTO merchantDTO) throws BusinessException {
        //1.校验参数合法性
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
        Integer count = merchantMapper.selectCount(new QueryWrapper<Merchant>().lambda()
                .eq(Merchant::getMobile, merchantDTO.getMobile()));
        if (count> 0){
            throw new BusinessException(CommonErrorCode.E_100113);
        }


        //2.添加租户 和账号 并绑定关系
        CreateTenantRequestDTO createTenantRequestDTO = new CreateTenantRequestDTO();
        createTenantRequestDTO.setMobile(merchantDTO.getMobile());
        //表示该租户类型是商户
        createTenantRequestDTO.setTenantTypeCode("shanju-merchant");
        //设置租户套餐为初始化套餐
        createTenantRequestDTO.setBundleCode("shanju-merchant");
        //租户的账号信息
        createTenantRequestDTO.setUsername(merchantDTO.getUsername());
        createTenantRequestDTO.setPassword(merchantDTO.getPassword());
        //新增租户并设置为管理员
        createTenantRequestDTO.setName(merchantDTO.getUsername());
        log.info("商户中心调用统一账号服务，新增租户和账号");

        //调用SaaS接口
        TenantDTO tenantDTO = tenantService.createTenantAndAccount(createTenantRequestDTO);
        if (tenantDTO == null || tenantDTO.getId() == null){
            throw new BusinessException(CommonErrorCode.E_200012);  //租户不存在
        }
        //判断租户下是否已经注册过商户
        Merchant merchant = merchantMapper.selectOne(new QueryWrapper<Merchant>().lambda().eq(Merchant::getTenantId, tenantDTO.getId()));
        if (merchant != null && merchant.getId() != null){
            throw  new BusinessException(CommonErrorCode.E_200017);//商户在当前租户下已经注册，不可重复注册
        }

        //3.设置商户所属租户
        merchantDTO.setTenantId(tenantDTO.getId());
        //设置审核状态，注册时默认为"0"
        merchantDTO.setAuditStatus("0");//审核状态 0‐未申请,1‐已申请待审核,2‐审核通过,3‐审核拒绝
        Merchant entity = MerchantConvert.INSTANCE.dto2entity(merchantDTO);
        //保存商户信息
        log.info("保存商户注册信息");
        merchantMapper.insert(entity);

        //4.新增门店，创建根门店
        StoreDTO storeDTO = new StoreDTO();
        storeDTO.setMerchantId(entity.getId());
        storeDTO.setStoreName("根门店");
        storeDTO = createStore(storeDTO);
        log.info("门店信息：{}" + JSON.toJSONString(storeDTO));

        //5.新增员工，并设置归属门店
        StaffDTO staffDTO = new StaffDTO();
        staffDTO.setMerchantId(entity.getId());
        staffDTO.setMobile(entity.getMobile());
        staffDTO.setUsername(entity.getUsername());
        //为员工选择归属门店,此处为根门店
        staffDTO.setStoreId(entity.getId());
        staffDTO= createStaff(staffDTO);
        log.info("员工信息：{}" + JSON.toJSONString(staffDTO));

        //6.为门店设置管理员
        bindStoreToStaff(storeDTO.getId(),staffDTO.getId());

        //返回商户注册信息
        return MerchantConvert.INSTANCE.entity2dto(entity);
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

    /**
     * 商户下新增门店
     * @param storeDTO
     * @return  新增门店
     * @throws BusinessException
     */
    @Override
    public StoreDTO createStore(StoreDTO storeDTO) throws BusinessException {
        Store store = StoreConvert.INSTANS.dto2entity(storeDTO);
        storeMapper.insert(store);
        return StoreConvert.INSTANS.entity2dto(store);
    }

    /**
     * 商户下新增员工
     * @param staffDTO
     * @return  新增员工
     * @throws BusinessException
     */
    @Override
    public StaffDTO createStaff(StaffDTO staffDTO) throws BusinessException {
        String mobile = staffDTO.getMobile();
        //1.校验手机号格式及是否存在
        if (StringUtil.isBlank(mobile)){
            throw new BusinessException(CommonErrorCode.E_100112);
        }
        //根据商户id和手机号校验唯一性
        if (isExistStaffByMobie(mobile,staffDTO.getMerchantId())){
            throw new BusinessException(CommonErrorCode.E_100113);
        }
        //2.校验用户名是否为空
        String userName = staffDTO.getUsername();
        if (StringUtil.isBlank(userName)){
            throw new BusinessException(CommonErrorCode.E_100110);
        }

        //根据商户id和账号校验唯一性
        if (isExistStaffByUsername(userName,staffDTO.getMerchantId())){
            throw new BusinessException(CommonErrorCode.E_100114);
        }
        log.info("商户下新增员工");

        Staff staff = StaffConvert.INSTANS.dto2entity(staffDTO);
        staffMapper.insert(staff);
        return StaffConvert.INSTANS.entity2dto(staff);
    }

    /**
     * 绑定门店与员工关系
     * @param storeId
     * @param staffId
     * @throws BusinessException
     */
    @Override
    public void bindStoreToStaff(Long storeId, Long staffId) throws BusinessException {
        if (staffId == null && storeId == null) {
            throw new BusinessException(CommonErrorCode.E_110006);
        }
        StoreStaff storeStaff = new StoreStaff();
        storeStaff.setStaffId(staffId);
        storeStaff.setStoreId(storeId);
        storeStaffMapper.insert(storeStaff);
    }

    /**
     * 根据账号判断员工是否已在指定商户存在
     * @param userName
     * @param merchantId
     * @return
     */
    private boolean isExistStaffByUsername(String userName,Long merchantId) {
        Integer count = staffMapper.selectCount(new QueryWrapper<Staff>().lambda().eq(Staff::getUsername, userName).eq(Staff::getMerchantId, merchantId));
        return count > 0;
    }

    /**
     * 根据手机号判断员工是否已在指定商户存在
     * @param mobile 手机号
     * @return
     */
    private boolean isExistStaffByMobie(String mobile, Long merchantId) {
        Integer count = staffMapper.selectCount(new QueryWrapper<Staff>().lambda().eq(Staff::getMobile, mobile).eq(Staff::getMerchantId, merchantId));
        return count > 0;
    }


}
