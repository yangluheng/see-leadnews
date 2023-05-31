package com.xy.common.aliyun;

import com.aliyun.imageaudit20191230.Client;
import com.aliyun.imageaudit20191230.models.ScanImageRequest;
import com.aliyun.imageaudit20191230.models.ScanImageResponse;
import com.aliyun.imageaudit20191230.models.ScanImageResponseBody;
import com.aliyun.tea.*;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.aliyun.teautil.Common.assertAsString;

/**
 * @author 杨路恒
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "aliyun")
public class ImageScan {
    private String accessKey;
    private String secretKey;
    private String scene;
    /**
     * 使用AK&SK初始化账号Client
     *
     * @param accessKeyId
     * @param accessKeySecret
     * @return Client
     * @throws Exception
     */
    public static Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
        Config config = new Config()
                // 必填，您的 AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 必填，您的 AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        // 访问的域名
        config.endpoint = "imageaudit.cn-shanghai.aliyuncs.com";
        return new Client(config);
    }

    public Map imageScan(List<String> imageList) throws Exception {
        // 工程代码泄露可能会导致AccessKey泄露，并威胁账号下所有资源的安全性。以下代码示例仅供参考，建议使用更安全的 STS 方式，更多鉴权访问方式请参见：https://help.aliyun.com/document_detail/378657.html
        Client client = ImageScan.createClient(accessKey, secretKey);

//        String url = null;
        List<ScanImageRequest.ScanImageRequestTask> urlList = new ArrayList<ScanImageRequest.ScanImageRequestTask>();
//        for (byte[] bytes : imageList) {
//            url = clientUploader.uploadBytes(bytes);
//            JSONObject task = new JSONObject();
//            task.put("dataId", UUID.randomUUID().toString());
//            //设置图片链接为上传后的url
//            task.put("url", url);
//            task.put("time", new Date());
//            urlList.add(task);
//        }

        // 场景一，使用本地文件
        // InputStream inputStream = new FileInputStream(new File("/tmp/bankCard.png"));
        // 场景二，使用任意可访问的url
//        URL url = new URL("https://viapi-test-bj.oss-cn-beijing.aliyuncs.com/viapi-3.0domepic/ocr/RecognizeBankCard/yhk1.jpg");
//        InputStream inputStream = url.openConnection().getInputStream();

//        http://192.168.22.129:9000/leadnews/2023/01/05/5fde2a072d6740758a5ccf41dcbe8208.jpg
        for (String url : imageList) {
//            JSONObject task = new JSONObject();
//            task.put("dataId", UUID.randomUUID().toString());
//            //设置图片链接为上传后的url
//            task.put("url", url);
//            task.put("time", new Date());
//            urlList.add(task);
            String imageURL = url;
            ScanImageRequest.ScanImageRequestTask task0 = new ScanImageRequest.ScanImageRequestTask()
                    .setImageURL(imageURL);
            task0.setDataId(UUID.randomUUID().toString());
            urlList.add(task0);
        }


        ScanImageRequest scanImageRequest = new ScanImageRequest().setTask(urlList)
                .setScene(Arrays.asList(
                        scene
                ));
        RuntimeOptions runtime = new RuntimeOptions();
        Map<String, String> resultMap = new HashMap<>();
        try {
            // 复制代码运行请自行打印 API 的返回值
            ScanImageResponse scanImageResponse = client.scanImageWithOptions(scanImageRequest, runtime);
            System.out.println(scanImageResponse.getStatusCode());
            System.out.println(scanImageResponse.getHeaders());
            System.out.println(scanImageResponse.getBody().getData().getResults().get(0));
            //服务端接收到请求，并完成处理返回的结果
            if (scanImageResponse != null && scanImageResponse.getBody().getData() != null) {
                if (200 == scanImageResponse.getStatusCode()) {
                    //根据scene和suggetion做相关处理
                    //do something
                    List<ScanImageResponseBody.ScanImageResponseBodyDataResults> results = scanImageResponse.getBody().getData().getResults();
                    for (ScanImageResponseBody.ScanImageResponseBodyDataResults result : results) {
                        System.out.println(result.getImageURL());
                        List<ScanImageResponseBody.ScanImageResponseBodyDataResultsSubResults> subResults = result.getSubResults();
                        for (ScanImageResponseBody.ScanImageResponseBodyDataResultsSubResults subResult : subResults) {
                            System.out.println(subResult.label);
                            String scene = subResult.getScene();
                            String label = subResult.getLabel();
                            String suggestion = subResult.getSuggestion();
                            /**
                             * 建议您执行的操作。
                             *
                             *     pass：图片正常，无需进行其余操作；或者未识别出目标对象。
                             *     review：检测结果不确定，需要进行人工审核；或者未识别出目标对象。
                             *     block：图片违规，建议执行进一步操作（如直接删除或做限制处理）。
                             */
                            System.out.println(subResult.suggestion);
                            if (!suggestion.equals("pass")) {
                                resultMap.put("suggestion", suggestion);
                                resultMap.put("label", label);
                                return resultMap;
                            }
                        }
                    }

                } else {
                    //单张图片处理失败, 原因视具体的情况详细分析
                    System.out.println("task process fail. task response:");
                    return null;
                }
                resultMap.put("suggestion", "pass");
                return resultMap;
            } else {
                /**
                 * 表明请求整体处理失败，原因视具体的情况详细分析
                 */
                System.out.println("the whole image scan request failed. response:");
                return null;
            }
        } catch (
                TeaException error) {
            // 如有需要，请打印 error
            assertAsString(error.message);
        } catch (
                Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            // 如有需要，请打印 error
            assertAsString(error.message);
        }

        return null;
    }

    public static void main(String[] args_) throws Exception {
        List<String> args = Arrays.asList(args_);
        // 工程代码泄露可能会导致AccessKey泄露，并威胁账号下所有资源的安全性。以下代码示例仅供参考，建议使用更安全的 STS 方式，更多鉴权访问方式请参见：https://help.aliyun.com/document_detail/378657.html
        Client client = ImageScan.createClient("LTAI5t8vuaLfZRWcWryDobZB", "GGIisNar7qAoQaO0Y1KgLnFOIKm1tq");

        // 场景一，使用本地文件
        // InputStream inputStream = new FileInputStream(new File("/tmp/bankCard.png"));
        // 场景二，使用任意可访问的url
//        URL url = new URL("https://viapi-test-bj.oss-cn-beijing.aliyuncs.com/viapi-3.0domepic/ocr/RecognizeBankCard/yhk1.jpg");
//        InputStream inputStream = url.openConnection().getInputStream();

//        http://192.168.22.129:9000/leadnews/2023/01/05/5fde2a072d6740758a5ccf41dcbe8208.jpg
        String imageURL = "https://dataleadnews.oss-cn-shanghai.aliyuncs.com/2023/01/28/d5c3e77a12ac40bc89d5148ae7a9ae30.jpg";
        ScanImageRequest.ScanImageRequestTask task0 = new ScanImageRequest.ScanImageRequestTask()
                .setImageURL(imageURL);

        ScanImageRequest scanImageRequest = new ScanImageRequest().setTask(Arrays.asList(
                        task0
                ))
                .setScene(Arrays.asList(
                        "porn"
                ));
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            // 复制代码运行请自行打印 API 的返回值
            ScanImageResponse scanImageResponse = client.scanImageWithOptions(scanImageRequest, runtime);
            System.out.println(scanImageResponse.getStatusCode());
            System.out.println(scanImageResponse.getHeaders());
            List<ScanImageResponseBody.ScanImageResponseBodyDataResults> results = scanImageResponse.getBody().getData().getResults();
            for (ScanImageResponseBody.ScanImageResponseBodyDataResults result : results) {
                System.out.println(result.getImageURL());
                List<ScanImageResponseBody.ScanImageResponseBodyDataResultsSubResults> subResults = result.getSubResults();
                for (ScanImageResponseBody.ScanImageResponseBodyDataResultsSubResults subResult : subResults) {
                    System.out.println(subResult.label);
                    /**
                     * 建议您执行的操作。
                     *
                     *     pass：图片正常，无需进行其余操作；或者未识别出目标对象。
                     *     review：检测结果不确定，需要进行人工审核；或者未识别出目标对象。
                     *     block：图片违规，建议执行进一步操作（如直接删除或做限制处理）。
                     */
                    System.out.println(subResult.suggestion);
                }
            }
            System.out.println();
        } catch (TeaException error) {
            // 如有需要，请打印 error
            assertAsString(error.message);
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            // 如有需要，请打印 error
            assertAsString(error.message);
        }
    }
}
