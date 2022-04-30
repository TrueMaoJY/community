package com.maomao.community.dao;

import com.maomao.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    // @Param注解用于给参数取别名,
    // 如果只有一个参数,并且在<if>里使用,则必须加别名.
    int selectDiscussPostRows(@Param("userId") int userId);

    /**
    * Description:将评论写入数据库
    * date: 2022/4/5 21:50
    * @author: MaoJY
    * @since JDK 1.8
    */
    int insertDiscussPost(DiscussPost post);
    /**
    * Description:帖子详情
    * date: 2022/4/6 18:46
    * @author: MaoJY
    * @since JDK 1.8
    */
    DiscussPost discussPostDetail(int id);

    /**
    * Description:修改评论数
    * date: 2022/4/6 18:47
    * @author: MaoJY
    * @since JDK 1.8
    */
    int updateCommentCount(int id,int commentCount);

    DiscussPost findDiscussById(int id);

    /**
    * Description:置顶
    * date: 2022/4/28 20:43
    * @author: MaoJY
    * @since JDK 1.8
    */
    void updateType(int id,int type);
    /**
    * Description:加精1，删除2
    * date: 2022/4/28 20:44
    * @author: MaoJY
    * @since JDK 1.8
    */
    void updateStatus(int id,int status);
}
