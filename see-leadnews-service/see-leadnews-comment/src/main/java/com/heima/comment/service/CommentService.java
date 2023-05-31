package com.xy.comment.service;

import com.xy.model.comment.dtos.CommentDto;
import com.xy.model.comment.dtos.CommentLikeDto;
import com.xy.model.comment.dtos.CommentSaveDto;
import com.xy.model.common.dtos.ResponseResult;
import org.springframework.web.bind.annotation.RequestBody;

public interface CommentService {

    /**
     * 保存评论
     * @param dto
     * @return
     */
    public ResponseResult saveComment(CommentSaveDto dto);

    /**
     * 点赞
     * @param dto
     * @return
     */
    public ResponseResult like(CommentLikeDto dto);

    /**
     * 加载评论列表
     * @param dto
     * @return
     */
    public ResponseResult findByArticleId(CommentDto dto);
}
