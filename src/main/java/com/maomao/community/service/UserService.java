package com.maomao.community.service;

import com.maomao.community.dao.UserMapper;
import com.maomao.community.entity.LoginTicket;
import com.maomao.community.entity.User;
import com.maomao.community.util.CommunityUtil;
import com.maomao.community.util.MailService;


import com.maomao.community.util.RedisKeyUtil;
import com.maomao.community.vo.ConstantVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.maomao.community.vo.ConstantVO.*;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
   private MailService mailService;


    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    public User findUserById(int id) {
//        return userMapper.selectById(id);
        User user = findUserByRedis(id);
        if(user==null){
            user=findUserByDB(id);
        }
        return user;
    }
    private User findUserByRedis(int userId){
        String loginUser = RedisKeyUtil.getPrefixUser(userId);
       return (User)redisTemplate.opsForValue().get(loginUser);
    }
    private User findUserByDB(int userId){
        User user = userMapper.selectById(userId);
        String loginUser = RedisKeyUtil.getPrefixUser(userId);
        redisTemplate.opsForValue().set(loginUser,user,3600, TimeUnit.SECONDS);
        return user;
    }
    private void deleteCache(int userId){
        String loginUser = RedisKeyUtil.getPrefixUser(userId);
        redisTemplate.delete(loginUser);
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        // ????????????
        if (user == null) {
            throw new IllegalArgumentException("??????????????????!");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "??????????????????!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "??????????????????!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "??????????????????!");
            return map;
        }

        // ????????????
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "??????????????????!");
            return map;
        }

        // ????????????
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "?????????????????????!");
            return map;
        }

        // ????????????
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // ????????????
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
//        mailClient.sendMail(user.getEmail(), "????????????", content);
        try {
            mailService.sendHtmlMail(user.getEmail(),"????????????",content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            deleteCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // ????????????
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "??????????????????!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "??????????????????!");
            return map;
        }

        // ????????????
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "??????????????????!");
            return map;
        }

        // ????????????
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "??????????????????!");
            return map;
        }

        // ????????????
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "???????????????!");
            return map;
        }

        // ??????????????????
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        String redisKey= RedisKeyUtil.getLoginTicket(loginTicket.getTicket());
       redisTemplate.opsForValue().set(redisKey,loginTicket);
//        loginTicketMapper.insertLoginTicket(loginTicket);
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
//        loginTicketMapper.updateStatus(ticket, 1);
        String loginTicket = RedisKeyUtil.getLoginTicket(ticket);
        LoginTicket vo = (LoginTicket) redisTemplate.opsForValue().get(loginTicket);
        vo.setStatus(1);
        redisTemplate.opsForValue().set(loginTicket,vo);
    }

    public LoginTicket findLoginTicket(String ticket) {
//        return loginTicketMapper.selectByTicket(ticket);
        String loginTicket = RedisKeyUtil.getLoginTicket(ticket);
       return (LoginTicket) redisTemplate.opsForValue().get(loginTicket);
    }

    public int updateHeader(int userId, String headerUrl) {
        int rows = userMapper.updateHeader(userId, headerUrl);
        deleteCache(userId);

        return rows;
    }

    public User findUserByName(String toName) {
       return userMapper.selectByName(toName);
    }


    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
