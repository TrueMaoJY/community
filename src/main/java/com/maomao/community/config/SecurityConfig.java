package com.maomao.community.config;

import com.maomao.community.entity.User;
import com.maomao.community.service.UserService;
import com.maomao.community.util.CommunityUtil;
import com.maomao.community.util.JsonUtil;
import com.maomao.community.util.RedisKeyUtil;
import com.maomao.community.vo.ConstantVO;
import com.maomao.community.vo.RespBean;
import com.maomao.community.vo.RespBeanEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author MaoJY
 * @create 2022-04-26 20:31
 * @Description:spring-security ?????????
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
    * Description:??????????????????????????????
    * date: 2022/4/26 20:32
    * @author: MaoJY
    * @since JDK 1.8
    */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }
    /**
     AuthenticationManager:?????????????????????

    */

//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        //?????????????????????
////        auth.userDetailsService(userService).passwordEncoder(new Pbkdf2PasswordEncoder("salt"));
//        /*????????????????????????
//        AuthenticationProvider???providerManager????????????AuthenticationProvider?????????AuthenticationProvider??????????????????
//        ???????????????providerManager??????????????????AuthenticationProvider
//        */
//        auth.authenticationProvider(new AuthenticationProvider() {
//            //Authentication????????????????????????????????????????????????????????????????????????????????????
//            @Override
//            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
//                String username = authentication.getName();
//                String password = (String) authentication.getCredentials();
//                User user = userService.findUserByName(username);
//                if (user == null) {
//                    throw new UsernameNotFoundException("???????????????");
//                }
//                if (!user.getPassword().equals(password)) {
//                    throw new BadCredentialsException("???????????????");
//                }
////principal??????????????? credential ?????? authorities?????????
//                return new UsernamePasswordAuthenticationToken(user,user.getPassword(),userService.getAuthorities(user.getId()));
//            }
////??????AuthenticationProvider???????????????????????????
//            @Override
//            public boolean supports(Class<?> aClass) {
//                return UsernamePasswordAuthenticationToken.class.equals(aClass);
//            }
//        });
//    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // ??????????????????
        http.formLogin()
                .loginPage("/community/login")
                .loginProcessingUrl("/community/login")
                .successHandler(new AuthenticationSuccessHandler() {
                    @Override
                    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                        response.sendRedirect(request.getContextPath() + "/index");
                    }
                })
                .failureHandler(new AuthenticationFailureHandler() {
                    @Override
                    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                        request.setAttribute("error", e.getMessage());
                        request.getRequestDispatcher(request.getContextPath()+"/login").forward(request, response);
                    }
                });
//
//        // ??????????????????
//        http.logout()
//                .logoutUrl("/logout")
//                .logoutSuccessHandler(new LogoutSuccessHandler() {
//                    @Override
//                    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
//                        response.sendRedirect(request.getContextPath() + "/index");
//                    }
//                });

        // ????????????
        http.authorizeRequests()
                .antMatchers(
                        "/letter/**",
                        "/user/**",
                        "/message/**",
                        "/like",
                        "/follow",
                        "/unfollow",
                        "/followees/**",
                        "/followers/**",
//                        "/discuss/**", ??????????????????????????????????????????
                        "/discuss/addPost",
                        "/discuss/top",
                        "/discuss/wonderful",
                        "/discuss/block",
                        "/comment/**"
                )
                    .hasAnyAuthority(
                            ConstantVO.AUTHORITY_ADMIN,
                            ConstantVO.AUTHORITY_USER,
                            ConstantVO.AUTHORITY_MODERATOR)
                .antMatchers(
                        "/discuss/top",
                        "/discuss/wonderful"
                ).hasAnyAuthority(
                        ConstantVO.AUTHORITY_MODERATOR
                ).antMatchers(
                        "/discuss/block",
                         "/data/**",
                         "/actuator/**"
                ).hasAnyAuthority(
                        ConstantVO.AUTHORITY_ADMIN
                )
                            .anyRequest().permitAll()/*.and().csrf().disable()*/;
        //??????????????? ?????????
        http.exceptionHandling().authenticationEntryPoint(new AuthenticationEntryPoint() {
            //????????????
            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                String header = request.getHeader("x-requested-with");
                if ("XMLHttpRequest".equals(header)) {
                    response.setContentType("application/plain;charset=utf-8");
                    PrintWriter writer = response.getWriter();
                    writer.write(JsonUtil.object2JsonStr(RespBean.error(RespBeanEnum.NOT_LOGIN)));
                }else {
                    response.sendRedirect(request.getContextPath()+"/login");
                }
            }
        }).accessDeniedHandler(new AccessDeniedHandler() {
            //????????????
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
                String header = request.getHeader("x-requested-with");
                if ("XMLHttpRequest".equals(header)) {
                    response.setContentType("application/plain;charset=utf-8");
                    PrintWriter writer = response.getWriter();
                    writer.write(JsonUtil.object2JsonStr(RespBean.error(RespBeanEnum.AUTHORITY_DENIED)));
                }else {
                    response.sendRedirect(request.getContextPath()+"/denied");
                }
            }
        });
        /*
        *springsecurity?????????????????????logout???????????????????????????
        * ???????????????????????????????????????????????????????????????
        * */
        http.logout().logoutUrl("/securitylogout");

//         ??????Filter,???????????????
        http.addFilterBefore(new Filter() {
            @Override
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
                HttpServletRequest request = (HttpServletRequest) servletRequest;
                HttpServletResponse response = (HttpServletResponse) servletResponse;
                if (request.getServletPath().equals("/login")) {
                    String verifyCode = request.getParameter("code");
                    Cookie[] cookies = request.getCookies();
                    String kaptchaOwner="";
                    for (Cookie cookie : cookies) {
                        if (cookie.getName().equals("kaptcha")){
                            kaptchaOwner=cookie.getValue();
                        }
                    }
                    String captcha = (String) redisTemplate.opsForValue().get(RedisKeyUtil.getKaptcha(kaptchaOwner));
                    if (verifyCode == null || !verifyCode.equalsIgnoreCase(captcha)) {
                        request.setAttribute("error", "???????????????!");
                        request.getRequestDispatcher("/login").forward(request, response);
                        return;
                    }
                }
                // ???????????????????????????.
                filterChain.doFilter(request, response);
            }
        }, UsernamePasswordAuthenticationFilter.class);

        // ?????????
        http.rememberMe()
                .tokenRepository(new InMemoryTokenRepositoryImpl())
                .tokenValiditySeconds(3600 * 24)
                .userDetailsService(new UserDetailsService() {
                    @Override
                    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
                        return null;
                    }
                });

    }
}