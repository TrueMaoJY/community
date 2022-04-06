package com.maomao.community.service;

import com.maomao.community.dao.DiscussPostMapper;
import com.maomao.community.entity.DiscussPost;
import com.maomao.community.filter.SensitiveWordFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }
    /**
    * Description:插入评论
    * date: 2022/4/5 21:52
    * @author: MaoJY
    * @since JDK 1.8
    */
    public int insertDiscussPost(DiscussPost post){
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        post.setTitle(sensitiveWordFilter.filter(post.getTitle()));
        post.setContent(sensitiveWordFilter.filter(post.getContent()));
        return discussPostMapper.insertDiscussPost(post);
    }
}
