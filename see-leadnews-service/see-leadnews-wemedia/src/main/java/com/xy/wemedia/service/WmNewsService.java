package com.xy.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xy.model.common.dtos.ResponseResult;
import com.xy.model.wemedia.dtos.WmNewsDto;
import com.xy.model.wemedia.dtos.WmNewsPageReqDto;
import com.xy.model.wemedia.pojos.WmNews;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author 杨路恒
 */
public interface WmNewsService extends IService<WmNews> {
    /**
     * 查询文章
     * @param wmNewsPageReqDto
     * @return
     */
    public ResponseResult findAll(WmNewsPageReqDto wmNewsPageReqDto);

    /**
     * 发布修改文章或保存为草稿
     * @param wmNewsDto
     * @return
     */
    public ResponseResult submitNews(WmNewsDto wmNewsDto);

    /**
     * 文章的上下架
     * @param wmNewsDto
     * @return
     */
    public ResponseResult downOrUp(WmNewsDto wmNewsDto);

    /**
     * 删除发布文章或草稿
     * @param id
     * @return
     */
    public ResponseResult deleteNews(Integer id);

    /**
     * 编辑发布文章或草稿
     * @param id
     * @return
     */
    public ResponseResult editNews(Integer id);
}
