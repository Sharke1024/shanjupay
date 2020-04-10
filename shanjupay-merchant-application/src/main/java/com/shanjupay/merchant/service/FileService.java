package com.shanjupay.merchant.service;

import com.shanjupay.common.domain.BusinessException;

/**
 * @Author DL_Wu
 * @Date 2020/4/10 16:07
 * @Version 1.0
 *
 * 文件上传接口
 */
public interface FileService {

    /**
     * 文件上传
     * @param bytes 文件字节数组
     * @param fileName  文件名称
     * @return  文件下载路径
     * @throws BusinessException
     */
    String upLoad(byte[] bytes, String fileName) throws BusinessException;

}
