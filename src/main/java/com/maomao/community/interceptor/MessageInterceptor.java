package com.maomao.community.interceptor;

import com.maomao.community.entity.User;
import com.maomao.community.service.MessageService;
import com.maomao.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.mail.Message;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author MaoJY
 * @create 2022-04-25 19:25
 * @Description:
 */
@Component
public class MessageInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private MessageService messageService;
    /**
    * Description:
     * preHandle：调用controller之前
     * postHandler：调用controller之后，模板渲染之前
     * afterCompletion：视图渲染之后
    * date: 2022/4/25 19:26
    * @author: MaoJY
    * @since JDK 1.8
    */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null&&modelAndView!=null) {
            int letterUnreadCount =messageService.selectLetterUnreadCount(user.getId(), null);
            int noticeUnreadConut=messageService.findNoticeUnread(user.getId(),null);
            modelAndView.addObject("allUnreadCount",letterUnreadCount+noticeUnreadConut);
        }
    }
}