package com.xy;



import com.xy.file.service.FileStorageService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Unit test for simple App.
 */
@SpringBootTest(classes = MinIOApplication.class)
@RunWith(SpringRunner.class)
public class MinIOTest {
    @Autowired
    private FileStorageService fileStorageService;
    /**
     * 把list.html文件上传到minio中，并且可以在浏览器中访问
     * @param args
     */
    public static void main(String[] args) {
        FileInputStream fileInputStream = null;
        try {

//            fileInputStream =  new FileInputStream("D:\\Java\\项目\\xy-leadnews\\xy-leadnews-test\\minio-demo\\src\\main\\resources\\templates\\list.html");
            fileInputStream =  new FileInputStream("D:\\Java\\2022Java传智播客\\06、阶段六 服务端框架高级+黑马头条项目\\07、第七章 黑马头条项目\\黑马头条【更多资料：600xue.com】\\day02-app端文章查看，静态化freemarker,分布式文件系统minIO\\资料\\模板文件\\plugins\\js\\index.js");
            //1.创建minio链接客户端
            MinioClient minioClient = MinioClient.builder().credentials("minio", "ylh013954").endpoint("http://192.168.22.129:9000").build();
            //2.上传
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .object("plugins/js/index.js")//文件名
                    .contentType("text/js")//文件类型
                    .bucket("leadnews")//桶名词  与minio创建的名词一致
                    .stream(fileInputStream, fileInputStream.available(), -1) //文件流
                    .build();
            minioClient.putObject(putObjectArgs);

            System.out.println("http://192.168.22.129:9000/leadnews/list.html");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 把list.html文件上传到minio中，并且可以在浏览器中访问
     */
    @Test
    public void test() throws FileNotFoundException {
        FileInputStream fileInputStream =  new FileInputStream("D:\\Java\\项目\\xy-leadnews\\xy-leadnews-test\\minio-demo\\src\\main\\resources\\templates\\list.html");;
        String path = fileStorageService.uploadHtmlFile("", "list.html", fileInputStream);
        System.out.println(path);
    }

}
