package com.maomao.community.controller;

import com.maomao.community.annotation.LoginRequired;
import com.maomao.community.entity.Event;
import com.maomao.community.entity.Page;
import com.maomao.community.entity.User;
import com.maomao.community.kafkaEvent.EventProducer;
import com.maomao.community.service.UserService;
import com.maomao.community.service.FollowService;
import com.maomao.community.util.HostHolder;
import com.maomao.community.vo.ConstantVO;
import com.maomao.community.vo.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author MaoJY
 * @create 2022-04-16 22:52
 * @Description:
 */
@Controller
public class FollowController {
    @Autowired
    private FollowService followService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @Autowired
    private EventProducer eventProducer;

    /**
     * Description: 关注
     * date: 2022/4/16 22:56
     * @author: MaoJY
     * @since JDK 1.8
     */
    @RequestMapping("/follow")
    @ResponseBody
    @LoginRequired
    public RespBean follow(int entityId,int entityType,Model model){
        User user = hostHolder.getUser();
        followService.follow(user.getId(),entityType,entityId);
        Event event = new Event();
        event.setTopic(ConstantVO.TOPIC_FOLLOW)
                .setEntityId(entityId)
                .setUserId(user.getId())
                .setEntityType(entityType)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);
        return RespBean.success();
    }
    /**
     * Description:取关
     * date: 2022/4/16 22:56
     * @author: MaoJY
     * @since JDK 1.8
     */
    @RequestMapping("/unfollow")
    @ResponseBody
    @LoginRequired
    public RespBean unfollow(int entityId,int entityType){
        followService.unfollow(hostHolder.getUser().getId(),entityType,entityId);
        return RespBean.success();
    }
    /**
     * Description:关注用户列表
     * date: 2022/4/18 18:23
     * @author: MaoJY
     * @since JDK 1.8
     */
    @RequestMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId") Integer userId, Model model, Page page){
        User user = userService.findUserById(userId);

        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user",user);
        page.setLimit(5);
        page.setPath("/followees/"+userId);
        page.setRows((int) followService.getFolloweesCount(userId, ConstantVO.ENTITY_TYPE_USER));
        List<Map<String, Object>> findfollowee = followService.findfollowee(userId, page.getOffset(), page.getLimit());
        if (findfollowee != null) {
            for (Map<String, Object> map : findfollowee) {
                User followeeUser = (User) map.get("user");
                if (hostHolder.getUser()==null){
                    map.put("hasFollowed",false);
                }else if (hasFollowed(hostHolder.getUser().getId(),ConstantVO.ENTITY_TYPE_USER,followeeUser.getId())){
                    map.put("hasFollowed",true);
                }else {
                    map.put("hasFollowed",false);
                }
            }
        }
        model.addAttribute("followees",findfollowee);
        return "site/followee";
    }

    /**
     * Description:粉丝列表
     * date: 2022/4/18 18:23
     * @author: MaoJY
     * @since JDK 1.8
     */
    @RequestMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId") Integer userId, Model model, Page page){
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user",user);
        page.setLimit(5);
        page.setPath("/followers/"+userId);
        page.setRows((int) followService.getFollowersCount(ConstantVO.ENTITY_TYPE_USER,userId));
        List<Map<String, Object>> findfollower = followService.findfollower(userId, page.getOffset(), page.getLimit());
        if (findfollower != null) {
            for (Map<String, Object> map : findfollower) {
                User followerUser = (User) map.get("user");
                if (hostHolder.getUser()==null){
                    map.put("hasFollowed",false);
                }else if (hasFollowed(hostHolder.getUser().getId(),ConstantVO.ENTITY_TYPE_USER,followerUser.getId())){
                    map.put("hasFollowed",true);
                }else {
                    map.put("hasFollowed",false);
                }
            }
        }
        model.addAttribute("followers",findfollower);
        return "site/follower";
    }
    private boolean hasFollowed(int userId,int entityType,int entity){
        return followService.hasFollow(userId, entityType, entity);
    }
}