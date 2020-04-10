package com.shanjupay.merchant.service.impl;


import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.util.QiniuUtils;
import com.shanjupay.merchant.service.FileService;
import org.springframework.beans.factory.annotation.Value;


/**
 * @Author DL_Wu
 * @Date 2020/4/10 16:08
 * @Version 1.0
 */
@org.springframework.stereotype.Service
public class FileServiceImpl implements FileService {

    @Value("${oss.qiniu.url}")
    private String url;

    @Value("${oss.qiniu.accessKey}")
    private String accessKey;

    @Value("${oss.qiniu.secretKey}")
    private String secretKey;

    @Value("${oss.qiniu.bucket}")
    private String bucket;


    /**
     * 文件上传
     * @param bytes 文件字节数组
     * @param fileName  文件名称
     * @return  真实文件下载路径
     * @throws BusinessException
     */
    @Override
    public String upLoad(byte[] bytes, String fileName) throws BusinessException {
        //String accessKey,String secretKey,String bucket,byte[] bytes,String filePath

        //调用common下的工具类
        try{
            QiniuUtils.upload2qiniu(accessKey,secretKey,bucket,bytes,fileName);
        }catch (BusinessException e){
            throw new BusinessException(CommonErrorCode.E_100106);
        }
        return url+fileName;
    }
}
