package com.xy.file.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

/**
 * @author 杨路恒
 */
@Data
@ConfigurationProperties(prefix = "oss")  // 文件上传 配置前缀file.oss
public class AliyunOSSConfigProperties implements Serializable {

    private String accessKey;
    private String secretKey;
    private String bucket;
    private String endpoint;
    private String readPath;
}
