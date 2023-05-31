package com.xy.wemedia;

import com.xy.common.aliyun.GreenImageScan;
import com.xy.common.aliyun.GreenTextScan;
import com.xy.common.aliyun.ImageScan;
import com.xy.file.service.AliyunFileStorageService;
import com.xy.file.service.FileStorageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Map;

@SpringBootTest(classes = WemediaApplication.class)
@RunWith(SpringRunner.class)
public class AliyunTest {
    @Autowired
    private GreenTextScan greenTextScan;
    @Autowired
    private GreenImageScan greenImageScan;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private AliyunFileStorageService aliyunFileStorageService;
    @Autowired
    private ImageScan imageScan;
    @Test
    public void testScanText() throws Exception {
        Map map = greenTextScan.greenTextScan("去有风的地方，海洛因");
        System.out.println(map);
    }
    @Test
    public void testScanImage() throws Exception {
        String url = aliyunFileStorageService.downLoadFile("https://dataleadnews.oss-cn-shanghai.aliyuncs.com/2023/01/28/d5c3e77a12ac40bc89d5148ae7a9ae30.jpg");
        Map map = imageScan.imageScan(Arrays.asList(url));
        System.out.println(map);
    }
}
