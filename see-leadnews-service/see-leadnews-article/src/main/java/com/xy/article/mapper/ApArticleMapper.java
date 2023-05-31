package com.xy.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xy.model.article.dtos.ArticleHomeDto;
import com.xy.model.article.pojos.ApArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * @author 杨路恒
 */
@Mapper
public interface ApArticleMapper extends BaseMapper<ApArticle> {
    /**
     * 加载文章列表
     * @param articleHomeDto
     * @param type  1 加载更多 2 加载最新
     * @return
     */
    public List<ApArticle> loadArticleList(@Param("articleHomeDto") ArticleHomeDto articleHomeDto, @Param("type") Short type);

    public List<ApArticle> findArticleListByLast5days(@Param("dayParam") Date dayParam);
}
