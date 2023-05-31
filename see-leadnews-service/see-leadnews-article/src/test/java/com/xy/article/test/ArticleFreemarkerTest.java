package com.xy.article.test;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xy.file.service.FileStorageService;
import com.xy.model.article.pojos.ApArticle;
import com.xy.model.article.pojos.ApArticleContent;
import com.xy.article.ArticleApplication;
import com.xy.article.mapper.ApArticleContentMapper;
import com.xy.article.mapper.ApArticleMapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest(classes = ArticleApplication.class)
@RunWith(SpringRunner.class)
public class ArticleFreemarkerTest {
    @Autowired
    private Configuration configuration;

    @Autowired
    private ApArticleMapper apArticleMapper;

    @Autowired
    private ApArticleContentMapper apArticleContentMapper;

    @Autowired
    private FileStorageService fileStorageService;

    @Test
    public void test() throws IOException, TemplateException {
        //1.获取文章内容
        ApArticleContent apArticleContent = apArticleContentMapper.selectOne(Wrappers.<ApArticleContent>lambdaQuery().eq(ApArticleContent::getArticleId, 1302862387124125698L));
        if (apArticleContent != null && StringUtils.isNotBlank(apArticleContent.getContent())) {
            //2.文章内容通过freemarker生成html文件
            Template template = configuration.getTemplate("article.ftl");
            //数据模型
            StringWriter out = new StringWriter();
            Map<String,Object> params = new HashMap<String,Object>();
            params.put("content", JSONArray.parseArray(apArticleContent.getContent()));
            //合成
            template.process(params,out);
            InputStream is = new ByteArrayInputStream(out.toString().getBytes());
            //3.把html文件上传到minio中
            String path = fileStorageService.uploadHtmlFile("", apArticleContent.getArticleId() + ".html", is);
            //4.修改ap_article表，保存static_url字段
            ApArticle apArticle = new ApArticle();
            apArticle.setId(apArticleContent.getArticleId());
            apArticle.setStaticUrl(path);
            apArticleMapper.updateById(apArticle);
        }


    }

}
