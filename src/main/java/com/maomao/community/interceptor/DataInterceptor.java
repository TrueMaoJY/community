package com.maomao.community.interceptor;

import com.maomao.community.entity.User;
import com.maomao.community.service.DataService;
import com.maomao.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author MaoJY
 * @create 2022-04-29 22:46
 * @Description:
 */
@Component
public class DataInterceptor implements HandlerInterceptor {
    @Autowired
    private DataService dataService;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            //存uv
        String ip = request.getRemoteHost();
        dataService.recordUV(ip);
        User user = HostHolder.getUser();
        if (user!=null){
            dataService.recordDAU(user.getId());
        }
        return true;
    }
}