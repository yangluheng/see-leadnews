package com.xy.wemedia.controller.v1;

import com.xy.model.common.dtos.ResponseResult;
import com.xy.model.wemedia.dtos.WmNewsDto;
import com.xy.model.wemedia.dtos.WmNewsPageReqDto;
import com.xy.wemedia.service.WmNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author 杨路恒
 */
@RestController
@RequestMapping("/api/v1/news")
public class WmNewsController {
    @Autowired
    private WmNewsService wmNewsService;
    /**
     * 查询文章
     * @param wmNewsPageReqDto
     * @return
     */
    @PostMapping("/list")
    public ResponseResult findAll(@RequestBody WmNewsPageReqDto wmNewsPageReqDto){
        return wmNewsService.findAll(wmNewsPageReqDto);
    }

    @PostMapping("/submit")
    public ResponseResult submitNews(@RequestBody WmNewsDto wmNewsDto){
        return wmNewsService.submitNews(wmNewsDto);
    }

    @PostMapping("/down_or_up")
    public ResponseResult downOrUp(@RequestBody WmNewsDto wmNewsDto){
        return wmNewsService.downOrUp(wmNewsDto);
    }

    @GetMapping("/del_news/{id}")
    public ResponseResult deleteNews(@PathVariable Integer id){
        return wmNewsService.deleteNews(id);
    }

    @GetMapping("/one/{id}")
    public ResponseResult editNews(@PathVariable Integer id){
        return wmNewsService.editNews(id);
    }
}
