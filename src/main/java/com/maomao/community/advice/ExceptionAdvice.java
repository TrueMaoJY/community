package com.maomao.community.advice;

import com.maomao.community.util.JsonUtil;
import com.maomao.community.vo.RespBean;
import com.maomao.community.vo.RespBeanEnum;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author MaoJY
 * @create 2022-04-11 22:49
 * @Description:统一异常处理
 */
@ControllerAdvice(annotations = Controller.class)//处理含特定注解的类的异常
@Slf4j
public class ExceptionAdvice {
    @ExceptionHandler(Exception.class)
    public void exceptionHandler(Exception e, HttpServletResponse response, HttpServletRequest request) throws IOException {
        log.error("异常信息:"+e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            log.error(element.toString());
        }
        String header = request.getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(header)){
            response.setContentType("application/json;charset=utf-8");
                PrintWriter writer = response.getWriter();
                writer.write(JsonUtil.object2JsonStr(RespBean.error(RespBeanEnum.ERROR)));
        }else {
            response.sendRedirect(request.getContextPath()+"/error");
        }
    }

}