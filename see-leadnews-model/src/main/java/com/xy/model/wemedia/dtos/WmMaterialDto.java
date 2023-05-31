package com.xy.model.wemedia.dtos;

import com.xy.model.common.dtos.PageRequestDto;
import lombok.Data;

/**
 * @author 杨路恒
 */
@Data
public class WmMaterialDto extends PageRequestDto {
    /**
     * 1 收藏
     * 0 未收藏
     */
    private Short isCollection;
}
