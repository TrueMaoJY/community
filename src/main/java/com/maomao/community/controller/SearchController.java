package com.maomao.community.controller;

import com.maomao.community.entity.DiscussPost;
import com.maomao.community.entity.Page;
import com.maomao.community.service.ElasticsearchService;
import com.maomao.community.service.LikesService;
import com.maomao.community.service.UserService;
import com.maomao.community.vo.ConstantVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author MaoJY
 * @create 2022-04-26 15:21
 * @Description:
 */
@Controller
public class SearchController {
    @Autowired
    private ElasticsearchService elasticsearchService;
    @Autowired
    private UserService userService;
    @Autowired
    private LikesService likesService;
    @RequestMapping("/search")
    public String search(String keyword, Page page, Model model){
        org.springframework.data.domain.Page<DiscussPost> searchResult =
                elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
        List<Map<String,Object>> discussPosts =new ArrayList<>();
        if (searchResult != null) {
            for (DiscussPost post : searchResult) {
                Map<String,Object> map=new HashMap();
                map.put("post",post);
                map.put("user",userService.findUserById(post.getUserId()));
                map.put("likeCount",likesService.getLikeCount(ConstantVO.ENTITY_TYPE_POST,post.getId()));
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("keyword",keyword);
        page.setPath("/search?keyword="+keyword);
        page.setRows((int) searchResult.getTotalElements());
        return "site/search";
    }
}