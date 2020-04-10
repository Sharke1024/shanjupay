package com.shanjupay.common.util;


import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;


/**
 * @Author DL_Wu
 * @Date 2020/4/10 11:26
 * @Version 1.0
 *
 * 七牛云对象存储工具类
 */
public class QiniuUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(QiniuUtils.class);

    /**
     * 文件上传工具类
     * @param accessKey
     * @param secretKey
     * @param bucket
     * @param bytes
     * @param fileName 文件路径
     */
    public static void upload2qiniu(String accessKey,String secretKey,String bucket,byte[] bytes,String fileName)throws RuntimeException{
        //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.huanan());
        //...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);

        //默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = fileName;
        //认证
        Auth auth = Auth.create(accessKey, secretKey);
        //认证通过后得到token（令牌）
        String upToken = auth.uploadToken(bucket);
        try {
            //上传文件
            Response response = uploadManager.put(bytes, key, upToken);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            System.out.println(putRet.key);
            System.out.println(putRet.hash);
        } catch (QiniuException ex) {
            Response r = ex.response;
            System.err.println(r.toString());
            LOGGER.error("上传文件到七牛: {}",ex.getMessage());
            try {
                System.err.println(r.bodyString());
            } catch (QiniuException ex2) {
                //ignore
            }
        } catch (Exception e) {
            LOGGER.error("上传文件到七牛: {}",e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }


    public static void fileDown(){
        String fileName = "3c79d3e1-dd0d-484d-8c20-c134b81fb031.png";
        String domainOfBucket = "http://q8k888uoo.bkt.clouddn.com";
        String finalUrl = String.format("%s/%s", domainOfBucket, fileName);
        System.out.println(finalUrl);
    }

/*    public static void main(String[] args) {
        //upload();
        fileDown();
    }*/

}
