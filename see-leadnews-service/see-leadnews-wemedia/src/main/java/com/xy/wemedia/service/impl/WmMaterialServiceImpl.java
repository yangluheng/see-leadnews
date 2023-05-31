package com.xy.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xy.file.service.AliyunFileStorageService;
import com.xy.file.service.FileStorageService;
import com.xy.model.common.dtos.PageResponseResult;
import com.xy.model.common.dtos.ResponseResult;
import com.xy.model.common.enums.AppHttpCodeEnum;
import com.xy.model.wemedia.dtos.WmMaterialDto;
import com.xy.model.wemedia.pojos.WmMaterial;
import com.xy.model.wemedia.pojos.WmNews;
import com.xy.utils.thread.WmThreadLocalUtil;
import com.xy.wemedia.mapper.WmMaterialMapper;
import com.xy.wemedia.service.WmMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

/**
 * @author 杨路恒
 */
@Service
@Transactional
@Slf4j
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {
    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private AliyunFileStorageService aliyunFileStorageService;

    /**
     * 图片上传
     *
     * @param multipartFile
     * @return
     */
    @Override
    public ResponseResult uploadPicture(MultipartFile multipartFile) {

        //1.检查参数
        if (multipartFile == null || multipartFile.getSize() == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //2.上传图片到minio
        String fileName = UUID.randomUUID().toString().replace("-", "");
        //aa.jpg
        String originalFilename = multipartFile.getOriginalFilename();
        String postfix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileId = null;
        try {
//            fileId = fileStorageService.uploadImgFile("", fileName + postfix, multipartFile.getInputStream());
//            log.info("上传图片到MinIO中，fileId:{}",fileId);
            fileId = aliyunFileStorageService.uploadImgFile("", fileName + postfix, multipartFile.getInputStream(), multipartFile.getOriginalFilename());
            log.info("上传图片到AliyunOSS中，fileId:{}", fileId);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("WmMaterialServiceImpl-上传文件失败");
        }
        //3.保存到数据库中
        WmMaterial wmMaterial = new WmMaterial();
        wmMaterial.setUserId(WmThreadLocalUtil.getUser().getId());
        wmMaterial.setUrl(fileId);
        wmMaterial.setIsCollection((short) 0);
        wmMaterial.setType((short) 0);
        wmMaterial.setCreatedTime(new Date());
        save(wmMaterial);
        //4.返回结果
        return ResponseResult.okResult(wmMaterial);
    }

    @Override
    public ResponseResult findList(WmMaterialDto wmMaterialDto) {
        //1.检查参数
        wmMaterialDto.checkParam();

        //2.分页查询
        IPage page = new Page(wmMaterialDto.getPage(), wmMaterialDto.getSize());
        LambdaQueryWrapper<WmMaterial> wmMaterialLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //是否收藏
        if (wmMaterialDto.getIsCollection() != null && wmMaterialDto.getIsCollection() == 1) {
            wmMaterialLambdaQueryWrapper.eq(WmMaterial::getIsCollection, wmMaterialDto.getIsCollection());
        }
        //按照用户查询
        wmMaterialLambdaQueryWrapper.eq(WmMaterial::getUserId, WmThreadLocalUtil.getUser().getId());
        //按时间倒序
        wmMaterialLambdaQueryWrapper.orderByDesc(WmMaterial::getCreatedTime);
        page = page(page, wmMaterialLambdaQueryWrapper);
        //3.结果返回
        PageResponseResult pageResponseResult = new PageResponseResult(wmMaterialDto.getPage(), wmMaterialDto.getSize(), (int) page.getTotal());
        pageResponseResult.setData(page.getRecords());
        return pageResponseResult;
    }

    /**
     * 收藏
     *
     * @param id
     * @return
     */
    @Override
    public ResponseResult collect(Integer id) {
        //1.根据id查询素材
        WmMaterial wmMaterial = getById(id);
        if (wmMaterial == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "素材不存在");
        }
        //2.收藏
        //是否收藏
        update(Wrappers.<WmMaterial>lambdaUpdate().set(WmMaterial::getIsCollection, 1).eq(WmMaterial::getId, wmMaterial.getId()));
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 取消收藏
     * @param id
     * @return
     */
    @Override
    public ResponseResult cancelCollect(Integer id) {
        //1.根据id查询素材
        WmMaterial wmMaterial = getById(id);
        if (wmMaterial == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "素材不存在");
        }
        //2.收藏
        //是否收藏
        update(Wrappers.<WmMaterial>lambdaUpdate().set(WmMaterial::getIsCollection, 0).eq(WmMaterial::getId, wmMaterial.getId()));
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    /**
     * 删除图片
     *
     * @param id
     * @return
     */
    @Override
    public ResponseResult deletePicture(Integer id) {
        //1.根据id查询素材
        WmMaterial wmMaterial = getById(id);
        if (wmMaterial == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "素材不存在");
        }
        //2.删除素材
        removeById(id);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
