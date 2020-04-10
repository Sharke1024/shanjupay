package com.shanjupay.merchant.common.intercept;

import com.shanjupay.common.domain.BusinessException;
import com.shanjupay.common.domain.CommonErrorCode;
import com.shanjupay.common.domain.ErrorCode;
import com.shanjupay.common.domain.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author DL_Wu
 * @Date 2020/4/9 17:18
 * @Version 1.0
 * <p>
 * 异常处理类
 */
@ControllerAdvice //与@ExceptionHandler 配合使用，实现全局异常处理
public class GlobalExceptionHandler {

    private static final Logger LOGGER  = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 捕获Exception异常
     * @param request http请求
     * @param response  http响应
     * @param e     异常
     * @return  错误代码，错误描述
     */
    @ExceptionHandler(value = Exception.class)  //捕获Exception异常
    @ResponseBody  //返回响应数据
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)   //抛出状态为500的错误
    public RestResponse processException(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Exception e){
        //如果是自定义异常，则直接取出异常信息
        if (e instanceof BusinessException){
            LOGGER.info(e.getMessage(),e);

            //解析系统自定义异常信息
            BusinessException businessException = (BusinessException)e;
            ErrorCode errorCode = businessException.getErrorCode();
            //返回错误代码，错误信息
            return new RestResponse(errorCode.getCode(),errorCode.getDesc());
        }

        //如果不是，则抛出系统未知异常 .统一为 999999
        LOGGER.error("系统异常",999999);
        return new RestResponse(CommonErrorCode.UNKNOWN.getCode(),CommonErrorCode.UNKNOWN.getDesc());
    }

}
