package xyz.rexlin600.oss.storage;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.UploadResult;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.Download;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.Upload;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.rexlin600.oss.config.TxOssConfig;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 腾讯云存储服务实现类
 *
 * @author: hekunlin
 * @date: 2020/6/21
 */
@SuppressWarnings("DuplicatedCode")
@Service
public class TxStorageService extends AbstractStorageService {

    private static final String SLASH = "/";
    private static final String SLASH_PLUS = "/+";

    private ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNamePrefix("oss-pool-%d").build();
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            4,
            8,
            15,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(),
            namedThreadFactory,
            new ThreadPoolExecutor.AbortPolicy());

    private TransferManager transferManager;
    private COSClient cosClient;

    /**
     * 腾讯云配置
     */
    private TxOssConfig config;

    public TxStorageService(TxOssConfig config) {
        this.config = config;
        //初始化
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        COSCredentials cred = new BasicCOSCredentials(config.getSecretId(), config.getSecretKey());
        Region region = new Region(config.getRegion());
        ClientConfig clientConfig = new ClientConfig(region);
        cosClient = new COSClient(cred, clientConfig);

        transferManager = new TransferManager(cosClient, threadPoolExecutor);
    }

    @Override
    public String upload(byte[] data, String fileName, String path) throws IOException {
        // 处理 path
        path = path.replaceAll(SLASH_PLUS, SLASH);
        if (StringUtils.isEmpty(path) || path.equals(SLASH)) {
            path = fileName;
        } else {
            // 注：阿里云不支持 path 开头为 /
            if (path.startsWith(SLASH)) {
                path = path.substring(1);
            }
            if (path.endsWith(SLASH)) {
                path = path.concat(fileName);
            } else {
                path = path.concat(SLASH).concat(fileName);
            }
        }
        // 腾讯云必需要以SLASH开头
        if (!path.startsWith(SLASH)) {
            path = SLASH + path;
        }

        return this.upload(new ByteArrayInputStream(data), fileName, path);
    }

    @Override
    public String upload(InputStream inputStream, String fileName, String path) throws IOException {
        //上传到腾讯云
        ObjectMetadata metadata = new ObjectMetadata();
        PutObjectRequest putObjectRequest = new PutObjectRequest(config.getBucketName(), path, inputStream, metadata);

        UploadResult uploadResult;
        try {
            Upload upload = transferManager.upload(putObjectRequest);
            uploadResult = upload.waitForUploadResult();
        } catch (InterruptedException e) {
            throw new IOException("上传文件异常");
        } finally {
            transferManager.shutdownNow();
        }

        return config.getDomain().concat(SLASH).concat(path);
    }

    @Override
    public InputStream download(String key) {
        throw new UnsupportedOperationException("不支持的下载方式");
    }

    @Override
    public void download(String key, String path) throws InterruptedException, IOException {
        try {
            // 创建文件
            File file = new File(path);
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }

            GetObjectRequest getObjectRequest = new GetObjectRequest(config.getBucketName(), key);
            getObjectRequest.setTrafficLimit(80 * 1024 * 1024);
            // 下载文件
            Download download = transferManager.download(getObjectRequest, new File(path));
            // 等待传输结束（如果想同步的等待上传结束，则调用 waitForCompletion）
            download.waitForCompletion();
        } finally {
            transferManager.shutdownNow();
        }
    }

}
