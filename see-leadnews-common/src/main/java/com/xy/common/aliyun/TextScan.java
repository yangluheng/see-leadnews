package com.xy.common.aliyun;

import com.aliyun.imageaudit20191230.Client;
import com.aliyun.imageaudit20191230.models.ScanTextRequest;
import com.aliyun.imageaudit20191230.models.ScanTextResponse;
import com.aliyun.imageaudit20191230.models.ScanTextResponseBody;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.aliyun.teautil.Common.assertAsString;

/**
 * @author 杨路恒
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "aliyun")
public class TextScan {
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

    public Map greenTextScan(String content) throws Exception {
        // 工程代码泄露可能会导致AccessKey泄露，并威胁账号下所有资源的安全性。以下代码示例仅供参考，建议使用更安全的 STS 方式，更多鉴权访问方式请参见：https://help.aliyun.com/document_detail/378657.html
        Client client = TextScan.createClient(accessKey, secretKey);
        ScanTextRequest.ScanTextRequestLabels labels0 = new ScanTextRequest.ScanTextRequestLabels()
                .setLabel(scene);
        ScanTextRequest.ScanTextRequestTasks tasks0 = new ScanTextRequest.ScanTextRequestTasks()
                .setContent(content);
        ScanTextRequest scanTextRequest = new ScanTextRequest().setTasks(Arrays.asList(
                tasks0
        )).setLabels(Arrays.asList(
                labels0));
        RuntimeOptions runtime = new RuntimeOptions();
        Map<String, String> resultMap = new HashMap<>();
        try {
            // 复制代码运行请自行打印 API 的返回值
            ScanTextResponse scanTextResponse = client.scanTextWithOptions(scanTextRequest, runtime);
            System.out.println(scanTextResponse.getStatusCode());
            System.out.println(scanTextResponse.getHeaders());

            if (scanTextResponse != null && scanTextResponse.getBody().getData() != null) {
                if (200 == scanTextResponse.getStatusCode()) {
                    List<ScanTextResponseBody.ScanTextResponseBodyDataElements> elements = scanTextResponse.getBody().getData().getElements();
                    for (ScanTextResponseBody.ScanTextResponseBodyDataElements element : elements) {
                        List<ScanTextResponseBody.ScanTextResponseBodyDataElementsResults> results = element.getResults();
                        for (ScanTextResponseBody.ScanTextResponseBodyDataElementsResults result : results) {
                            System.out.println(result.label);
                            String label = result.getLabel();
                            String suggestion = result.getSuggestion();
                            /**
                             * 建议您执行的操作，取值包括：
                             *
                             *     pass：文本正常。
                             *     review：需要人工审核。
                             *     block：文本违规，可以直接删除或者做限制处理。
                             */
                            if (!suggestion.equals("pass")) {
                                resultMap.put("suggestion", suggestion);
                                resultMap.put("label", label);
                                return resultMap;
                            }
                        }
                    }
                    resultMap.put("suggestion", "pass");
                    return resultMap;
                }
            }
            else {
                return null;
            }
        } catch (TeaException error) {
            // 如有需要，请打印 error
            assertAsString(error.message);
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            // 如有需要，请打印 error
            assertAsString(error.message);
        }
        return null;
    }

    public static void main(String[] args_) throws Exception {
        List<String> args = Arrays.asList(args_);
        // 工程代码泄露可能会导致AccessKey泄露，并威胁账号下所有资源的安全性。以下代码示例仅供参考，建议使用更安全的 STS 方式，更多鉴权访问方式请参见：https://help.aliyun.com/document_detail/378657.html
        Client client = TextScan.createClient("LTAI5t8vuaLfZRWcWryDobZB", "GGIisNar7qAoQaO0Y1KgLnFOIKm1tq");
        ScanTextRequest.ScanTextRequestLabels labels0 = new ScanTextRequest.ScanTextRequestLabels()
                .setLabel("abuse");
        ScanTextRequest.ScanTextRequestTasks tasks0 = new ScanTextRequest.ScanTextRequestTasks()
                .setContent("a");
        ScanTextRequest scanTextRequest = new ScanTextRequest().setTasks(Arrays.asList(
                tasks0
        )).setLabels(Arrays.asList(
                labels0));
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            // 复制代码运行请自行打印 API 的返回值
            ScanTextResponse scanTextResponse = client.scanTextWithOptions(scanTextRequest, runtime);
            System.out.println(scanTextResponse.getStatusCode());
            System.out.println(scanTextResponse.getHeaders());
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
