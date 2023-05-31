package com.xy.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xy.model.common.dtos.ResponseResult;
import com.xy.model.wemedia.dtos.WmMaterialDto;
import com.xy.model.wemedia.pojos.WmMaterial;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author 杨路恒
 */
public interface WmMaterialService extends IService<WmMaterial> {
    /**
     * 图片上传
     * @param multipartFile
     * @return
     */
    public ResponseResult uploadPicture(MultipartFile multipartFile);

    /**
     * 素材列表查询
     * @param wmMaterialDto
     * @return
     */
    public ResponseResult findList(WmMaterialDto wmMaterialDto);

    /**
     * 收藏
     * @param id
     * @return
     */
    public ResponseResult collect(Integer id);

    /**
     * 取消收藏
     * @param id
     * @return
     */
    public ResponseResult cancelCollect(Integer id);

    /**
     * 删除图片
     * @param id
     * @return
     */
    public ResponseResult deletePicture(Integer id);

}
