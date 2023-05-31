package com.xy.wemedia.controller.v1;

import com.xy.model.common.dtos.ResponseResult;
import com.xy.model.wemedia.dtos.WmMaterialDto;
import com.xy.wemedia.service.WmMaterialService;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author 杨路恒
 */
@RestController
@RequestMapping("/api/v1/material")
public class WmMaterialController {
    @Autowired
    private WmMaterialService wmMaterialService;
    @PostMapping("/upload_picture")
    public ResponseResult uploadPicture(MultipartFile multipartFile){
        return wmMaterialService.uploadPicture(multipartFile);
    }

    @PostMapping("/list")
    public ResponseResult findList(@RequestBody WmMaterialDto wmMaterialDto){
        return wmMaterialService.findList(wmMaterialDto);
    }

    @GetMapping("/collect/{id}")
    public ResponseResult collect(@PathVariable Integer id){
        return wmMaterialService.collect(id);
    }

    @GetMapping("/del_picture/{id}")
    public ResponseResult deletePicture(@PathVariable Integer id){
        return wmMaterialService.deletePicture(id);
    }

    @GetMapping("/cancel_collect/{id}")
    public ResponseResult cancelCollect(@PathVariable Integer id){
        return wmMaterialService.cancelCollect(id);
    }
}
