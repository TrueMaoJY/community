package com.maomao.community.controller;

import com.maomao.community.entity.Comment;
import com.maomao.community.entity.User;
import com.maomao.community.service.CommentService;
import com.maomao.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * @author MaoJY
 * @create 2022-04-06 19:21
 * @Description:
 */
@Controller
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    CommentService commentService;
    @Autowired
    HostHolder hostHolder;
    /**
    * Description:添加评论
    * date: 2022/4/6 19:24
    * @author: MaoJY
    * @since JDK 1.8
    */
    @RequestMapping("/addComment/{discussPostId}")
    public  String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment){
        User user = hostHolder.getUser();
//        if (user == null) {
//
//        }
        comment.setUserId(user.getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.insertComment(comment);

        return "redirect:/discuss/detail/"+discussPostId;
    }
}