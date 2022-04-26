package com.maomao.community.es;

import com.maomao.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author MaoJY
 * @create 2022-04-25 21:45
 * @Description:
 */
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer> {
    //<DiscussPost,Integer>实体类和主键的类型
}
