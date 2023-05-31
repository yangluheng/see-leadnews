package com.xy.search.service;

import com.xy.model.common.dtos.ResponseResult;
import com.xy.model.search.dtos.HistorySearchDto;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author 杨路恒
 */
public interface ApUserSearchService {
    /**
     *  保存用户搜索历史记录
     * @param keyword
     * @param userId
     */
    public void insert(String keyword,Integer userId);

    /**
     * 查询搜索历史
     * @return
     */
    public ResponseResult findUserSearch();

    /**
     * 删除历史记录
     * @param historySearchDto
     * @return
     */
    public ResponseResult delUserSearch(HistorySearchDto historySearchDto);
}
