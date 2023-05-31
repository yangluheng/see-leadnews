package com.xy.wemedia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xy.model.wemedia.pojos.WmNewsMaterial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 杨路恒
 */
@Mapper
public interface WmNewsMaterialMapper extends BaseMapper<WmNewsMaterial> {
     /**
      *
      * @param materialIds
      * @param newsId
      * @param type
      */
     void saveRelations(@Param("materialIds") List<Integer> materialIds,@Param("newsId") Integer newsId, @Param("type")Short type);
}