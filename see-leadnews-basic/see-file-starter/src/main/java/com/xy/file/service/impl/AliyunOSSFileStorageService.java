package com.xy.file.service.impl;


import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.*;
import com.xy.file.config.AliyunOSSConfig;
import com.xy.file.config.AliyunOSSConfigProperties;
import com.xy.file.service.AliyunFileStorageService;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author 杨路恒
 */
@Slf4j
@EnableConfigurationProperties(AliyunOSSConfigProperties.class)
@Import(AliyunOSSConfig.class)
public class AliyunOSSFileStorageService implements AliyunFileStorageService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private AliyunOSSConfigProperties aliyunOSSConfigProperties;

    @Autowired
    private AliyunOSSConfig aliyunOSSConfig;
    private final static String separator = "/";

    /**
     * @param dirPath
     * @param filename yyyy/mm/dd/file.jpg
     * @return
     */
    public String builderFilePath(String dirPath, String filename) {
        StringBuilder stringBuilder = new StringBuilder(50);
        if (!StringUtils.isEmpty(dirPath)) {
            stringBuilder.append(dirPath).append(separator);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String todayStr = sdf.format(new Date());
        stringBuilder.append(todayStr).append(separator);
        stringBuilder.append(filename);
        return stringBuilder.toString();
    }

    /**
     * 上传图片文件
     *
     * @param prefix      文件前缀
     * @param filename    文件名
     * @param inputStream 文件流
     * @return 文件全路径
     */
    @Override
    public String uploadImgFile(String prefix, String filename, InputStream inputStream, String file) {
        String filePath = builderFilePath(prefix, filename);
        System.out.println(inputStream);
        try {
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = inputStream.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            StringBuilder urlPath = new StringBuilder(aliyunOSSConfigProperties.getReadPath());
//            urlPath.append(separator + aliyunOSSConfigProperties.getBucket());
            urlPath.append(separator);
            urlPath.append(filePath);

            // 创建OSSClient实例。
            OSS ossClient = aliyunOSSConfig.buildOSSClient();
            try {
                // 创建InitiateMultipartUploadRequest对象。
                InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(aliyunOSSConfigProperties.getBucket(), filePath);

                // 如果需要在初始化分片时设置请求头，请参考以下示例代码。
                // ObjectMetadata metadata = new ObjectMetadata();
                // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
                // 指定该Object的网页缓存行为。
                // metadata.setCacheControl("no-cache");
                // 指定该Object被下载时的名称。
                // metadata.setContentDisposition("attachment;filename=oss_MultipartUpload.txt");
                // 指定该Object的内容编码格式。
                // metadata.setContentEncoding(OSSConstants.DEFAULT_CHARSET_NAME);
                // 指定初始化分片上传时是否覆盖同名Object。此处设置为true，表示禁止覆盖同名Object。
                // metadata.setHeader("x-oss-forbid-overwrite", "true");
                // 指定上传该Object的每个part时使用的服务器端加密方式。
                // metadata.setHeader(OSSHeaders.OSS_SERVER_SIDE_ENCRYPTION, ObjectMetadata.KMS_SERVER_SIDE_ENCRYPTION);
                // 指定Object的加密算法。如果未指定此选项，表明Object使用AES256加密算法。
                // metadata.setHeader(OSSHeaders.OSS_SERVER_SIDE_DATA_ENCRYPTION, ObjectMetadata.KMS_SERVER_SIDE_ENCRYPTION);
                // 指定KMS托管的用户主密钥。
                // metadata.setHeader(OSSHeaders.OSS_SERVER_SIDE_ENCRYPTION_KEY_ID, "9468da86-3509-4f8d-a61e-6eab1eac****");
                // 指定Object的存储类型。
                // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard);
                // 指定Object的对象标签，可同时设置多个标签。
                // metadata.setHeader(OSSHeaders.OSS_TAGGING, "a:1");
                // request.setObjectMetadata(metadata);

                // 初始化分片。
                InitiateMultipartUploadResult upresult = ossClient.initiateMultipartUpload(request);
                // 返回uploadId，它是分片上传事件的唯一标识。您可以根据该uploadId发起相关的操作，例如取消分片上传、查询分片上传等。
                String uploadId = upresult.getUploadId();

                // partETags是PartETag的集合。PartETag由分片的ETag和分片号组成。
                List<PartETag> partETags = new ArrayList<PartETag>();
                // 每个分片的大小，用于计算文件有多少个分片。单位为字节。
                final long partSize = 1 * 1024 * 1024L;   //1 MB。

                // 填写本地文件的完整路径。如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件。
                final File sampleFile = new File(file);
                long fileLength = sampleFile.length();
                int partCount = (int) (fileLength / partSize);
                if (fileLength % partSize != 0) {
                    partCount++;
                }
                // 遍历分片上传。
                for (int i = 0; i < partCount; i++) {
                    long startPos = i * partSize;
                    long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : partSize;
                    InputStream instream = new FileInputStream(sampleFile);
//                    InputStream instream = inputStream;
                    // 跳过已经上传的分片。
                    instream.skip(startPos);
                    UploadPartRequest uploadPartRequest = new UploadPartRequest();
                    uploadPartRequest.setBucketName(aliyunOSSConfigProperties.getBucket());
                    uploadPartRequest.setKey(filePath);
                    uploadPartRequest.setUploadId(uploadId);
                    uploadPartRequest.setInputStream(instream);
                    // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100 KB。
                    uploadPartRequest.setPartSize(curPartSize);
                    // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出此范围，OSS将返回InvalidArgument错误码。
                    uploadPartRequest.setPartNumber(i + 1);
                    // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
                    UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
                    // 每次上传分片之后，OSS的返回结果包含PartETag。PartETag将被保存在partETags中。
                    partETags.add(uploadPartResult.getPartETag());
                }


                // 创建CompleteMultipartUploadRequest对象。
                // 在执行完成分片上传操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
                CompleteMultipartUploadRequest completeMultipartUploadRequest =
                        new CompleteMultipartUploadRequest(aliyunOSSConfigProperties.getBucket(), filePath, uploadId, partETags);

                // 如果需要在完成分片上传的同时设置文件访问权限，请参考以下示例代码。
                // completeMultipartUploadRequest.setObjectACL(CannedAccessControlList.Private);
                // 指定是否列举当前UploadId已上传的所有Part。如果通过服务端List分片数据来合并完整文件时，以上CompleteMultipartUploadRequest中的partETags可为null。
                // Map<String, String> headers = new HashMap<String, String>();
                // 如果指定了x-oss-complete-all:yes，则OSS会列举当前UploadId已上传的所有Part，然后按照PartNumber的序号排序并执行CompleteMultipartUpload操作。
                // 如果指定了x-oss-complete-all:yes，则不允许继续指定body，否则报错。
                // headers.put("x-oss-complete-all","yes");
                // completeMultipartUploadRequest.setHeaders(headers);

                // 完成分片上传。
                CompleteMultipartUploadResult completeMultipartUploadResult = ossClient.completeMultipartUpload(completeMultipartUploadRequest);
                System.out.println(completeMultipartUploadResult.getETag());

            } catch (OSSException oe) {
                System.out.println("Caught an OSSException, which means your request made it to OSS, "
                        + "but was rejected with an error response for some reason.");
                System.out.println("Error Message:" + oe.getErrorMessage());
                System.out.println("Error Code:" + oe.getErrorCode());
                System.out.println("Request ID:" + oe.getRequestId());
                System.out.println("Host ID:" + oe.getHostId());
            } catch (ClientException ce) {
                System.out.println("Caught an ClientException, which means the client encountered "
                        + "a serious internal problem while trying to communicate with OSS, "
                        + "such as not being able to access the network.");
                System.out.println("Error Message:" + ce.getMessage());
            } finally {
                if (ossClient != null) {
                    ossClient.shutdown();
                }
            }
            return urlPath.toString();
        } catch (Exception ex) {
            log.error("oss put file error.", ex);
            throw new RuntimeException("上传文件失败");
        }
    }

    /**
     * 上传html文件
     *
     * @param prefix      文件前缀
     * @param filename    文件名
     * @param inputStream 文件流
     * @return 文件全路径
     */
    @Override
    public String uploadHtmlFile(String prefix, String filename, InputStream inputStream, String file) {
        String filePath = builderFilePath(prefix, filename);
        System.out.println(inputStream);
        try {
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = inputStream.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            StringBuilder urlPath = new StringBuilder(aliyunOSSConfigProperties.getReadPath());
//            urlPath.append(separator + aliyunOSSConfigProperties.getBucket());
            urlPath.append(separator);
            urlPath.append(filePath);

            // 创建OSSClient实例。
            OSS ossClient = aliyunOSSConfig.buildOSSClient();
            try {
                // 创建InitiateMultipartUploadRequest对象。
                InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(aliyunOSSConfigProperties.getBucket(), filePath);

                // 如果需要在初始化分片时设置请求头，请参考以下示例代码。
                // ObjectMetadata metadata = new ObjectMetadata();
                // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
                // 指定该Object的网页缓存行为。
                // metadata.setCacheControl("no-cache");
                // 指定该Object被下载时的名称。
                // metadata.setContentDisposition("attachment;filename=oss_MultipartUpload.txt");
                // 指定该Object的内容编码格式。
                // metadata.setContentEncoding(OSSConstants.DEFAULT_CHARSET_NAME);
                // 指定初始化分片上传时是否覆盖同名Object。此处设置为true，表示禁止覆盖同名Object。
                // metadata.setHeader("x-oss-forbid-overwrite", "true");
                // 指定上传该Object的每个part时使用的服务器端加密方式。
                // metadata.setHeader(OSSHeaders.OSS_SERVER_SIDE_ENCRYPTION, ObjectMetadata.KMS_SERVER_SIDE_ENCRYPTION);
                // 指定Object的加密算法。如果未指定此选项，表明Object使用AES256加密算法。
                // metadata.setHeader(OSSHeaders.OSS_SERVER_SIDE_DATA_ENCRYPTION, ObjectMetadata.KMS_SERVER_SIDE_ENCRYPTION);
                // 指定KMS托管的用户主密钥。
                // metadata.setHeader(OSSHeaders.OSS_SERVER_SIDE_ENCRYPTION_KEY_ID, "9468da86-3509-4f8d-a61e-6eab1eac****");
                // 指定Object的存储类型。
                // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard);
                // 指定Object的对象标签，可同时设置多个标签。
                // metadata.setHeader(OSSHeaders.OSS_TAGGING, "a:1");
                // request.setObjectMetadata(metadata);

                // 初始化分片。
                InitiateMultipartUploadResult upresult = ossClient.initiateMultipartUpload(request);
                // 返回uploadId，它是分片上传事件的唯一标识。您可以根据该uploadId发起相关的操作，例如取消分片上传、查询分片上传等。
                String uploadId = upresult.getUploadId();

                // partETags是PartETag的集合。PartETag由分片的ETag和分片号组成。
                List<PartETag> partETags = new ArrayList<PartETag>();
                // 每个分片的大小，用于计算文件有多少个分片。单位为字节。
                final long partSize = 1 * 1024 * 1024L;   //1 MB。

                // 填写本地文件的完整路径。如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件。
                final File sampleFile = new File(file);
                long fileLength = sampleFile.length();
                int partCount = (int) (fileLength / partSize);
                if (fileLength % partSize != 0) {
                    partCount++;
                }
                // 遍历分片上传。
                for (int i = 0; i < partCount; i++) {
                    long startPos = i * partSize;
                    long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : partSize;
                    InputStream instream = new FileInputStream(sampleFile);
//                    InputStream instream = inputStream;
                    // 跳过已经上传的分片。
                    instream.skip(startPos);
                    UploadPartRequest uploadPartRequest = new UploadPartRequest();
                    uploadPartRequest.setBucketName(aliyunOSSConfigProperties.getBucket());
                    uploadPartRequest.setKey(filePath);
                    uploadPartRequest.setUploadId(uploadId);
                    uploadPartRequest.setInputStream(instream);
                    // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100 KB。
                    uploadPartRequest.setPartSize(curPartSize);
                    // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出此范围，OSS将返回InvalidArgument错误码。
                    uploadPartRequest.setPartNumber(i + 1);
                    // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
                    UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
                    // 每次上传分片之后，OSS的返回结果包含PartETag。PartETag将被保存在partETags中。
                    partETags.add(uploadPartResult.getPartETag());
                }


                // 创建CompleteMultipartUploadRequest对象。
                // 在执行完成分片上传操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
                CompleteMultipartUploadRequest completeMultipartUploadRequest =
                        new CompleteMultipartUploadRequest(aliyunOSSConfigProperties.getBucket(), filePath, uploadId, partETags);

                // 如果需要在完成分片上传的同时设置文件访问权限，请参考以下示例代码。
                // completeMultipartUploadRequest.setObjectACL(CannedAccessControlList.Private);
                // 指定是否列举当前UploadId已上传的所有Part。如果通过服务端List分片数据来合并完整文件时，以上CompleteMultipartUploadRequest中的partETags可为null。
                // Map<String, String> headers = new HashMap<String, String>();
                // 如果指定了x-oss-complete-all:yes，则OSS会列举当前UploadId已上传的所有Part，然后按照PartNumber的序号排序并执行CompleteMultipartUpload操作。
                // 如果指定了x-oss-complete-all:yes，则不允许继续指定body，否则报错。
                // headers.put("x-oss-complete-all","yes");
                // completeMultipartUploadRequest.setHeaders(headers);

                // 完成分片上传。
                CompleteMultipartUploadResult completeMultipartUploadResult = ossClient.completeMultipartUpload(completeMultipartUploadRequest);
                System.out.println(completeMultipartUploadResult.getETag());

            } catch (OSSException oe) {
                System.out.println("Caught an OSSException, which means your request made it to OSS, "
                        + "but was rejected with an error response for some reason.");
                System.out.println("Error Message:" + oe.getErrorMessage());
                System.out.println("Error Code:" + oe.getErrorCode());
                System.out.println("Request ID:" + oe.getRequestId());
                System.out.println("Host ID:" + oe.getHostId());
            } catch (ClientException ce) {
                System.out.println("Caught an ClientException, which means the client encountered "
                        + "a serious internal problem while trying to communicate with OSS, "
                        + "such as not being able to access the network.");
                System.out.println("Error Message:" + ce.getMessage());
            } finally {
                if (ossClient != null) {
                    ossClient.shutdown();
                }
            }
            return urlPath.toString();
        } catch (Exception ex) {
            log.error("oss put file error.", ex);
            throw new RuntimeException("上传文件失败");
        }
    }

    /**
     * 删除文件
     *
     * @param pathUrl 文件全路径
     */
    @Override
    public void delete(String pathUrl) {
        String key = pathUrl.replace(aliyunOSSConfigProperties.getEndpoint() + "/", "");
        int index = key.indexOf(separator);
        String bucket = key.substring(0, index);
        String filePath = key.substring(index + 1);
        // 删除Objects
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket(bucket).object(filePath).build();
        try {
            minioClient.removeObject(removeObjectArgs);
        } catch (Exception e) {
            log.error("minio remove file error.  pathUrl:{}", pathUrl);
            e.printStackTrace();
        }
    }


    /**
     * 下载文件
     *
     * @param pathUrl 文件全路径
     * @return 文件流
     */
    @Override
    public String downLoadFile(String pathUrl) {
        String key = pathUrl.replace(aliyunOSSConfigProperties.getReadPath() + "/", "");
        int index = key.indexOf(separator);
        String filePath = "1.png";
        String objectName = key;

        // 创建OSSClient实例。
        OSS ossClient = aliyunOSSConfig.buildOSSClient();
        InputStream inputStream = null;
        try {
            // 下载Object到本地文件，并保存到指定的本地路径中。如果指定的本地文件存在会覆盖，不存在则新建。
            // 如果未指定本地路径，则下载后的文件默认保存到示例程序所属项目对应本地路径中。
            ossClient.getObject(new GetObjectRequest(aliyunOSSConfigProperties.getBucket(), objectName), new File(filePath));
            inputStream = new FileInputStream(filePath);
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
            log.error("minio down file error.  pathUrl:{}", pathUrl);
            ce.printStackTrace();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while (true) {
            try {
                if (!((rc = inputStream.read(buff, 0, 100)) > 0)) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            byteArrayOutputStream.write(buff, 0, rc);
        }
//        return byteArrayOutputStream.toByteArray();
        return pathUrl;
    }
}
