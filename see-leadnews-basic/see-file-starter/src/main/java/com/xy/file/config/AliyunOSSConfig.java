package com.xy.file.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.xy.file.service.AliyunFileStorageService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author 杨路恒
 */
@Data
@Configuration
@EnableConfigurationProperties({AliyunOSSConfigProperties.class})
//当引入FileStorageService接口时
@ConditionalOnClass(AliyunFileStorageService.class)
public class AliyunOSSConfig {

    @Autowired
    private AliyunOSSConfigProperties aliyunOSSConfigProperties;

//    @Bean
//    public MinioClient buildMinioClient() {
//        return MinioClient
//                .builder()
//                .credentials(aliyunOSSConfigProperties.getAccessKey(), aliyunOSSConfigProperties.getSecretKey())
//                .endpoint(aliyunOSSConfigProperties.getEndpoint())
//                .build();
//    }
    @Bean
    public OSS buildOSSClient(){
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(aliyunOSSConfigProperties.getEndpoint(), aliyunOSSConfigProperties.getAccessKey(), aliyunOSSConfigProperties.getSecretKey());
        return ossClient;
    }
}