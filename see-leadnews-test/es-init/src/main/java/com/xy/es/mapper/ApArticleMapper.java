package com.xy.es.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xy.es.pojo.SearchArticleVo;
import com.xy.model.article.pojos.ApArticle;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author 杨路恒
 */
@Mapper
public interface ApArticleMapper extends BaseMapper<ApArticle> {

    public List<SearchArticleVo> loadArticleList();

}
