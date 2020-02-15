package xyz.rexlin600.rest;

import ch.qos.logback.core.encoder.EchoEncoder;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabProject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.rexlin600.common.apiparam.Response;
import xyz.rexlin600.common.apiparam.ResponseGenerator;
import xyz.rexlin600.config.GitLabConfigBean;
import xyz.rexlin600.req.GitlabCloneReq;
import xyz.rexlin600.util.GitlabUtil;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Gitlab批量下载工具类
 *
 * @author: rexlin600
 * @date: 2020-02-14
 */
@SuppressWarnings("AlibabaRemoveCommentedCode")
@Slf4j
@RestController
@RequestMapping("/gitlab")
public class Git4CloneRest {

    /**
     * 线程池
     */
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            50,
            60,
            10,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<>());

    /**
     * GitLab 配置
     */
    @Autowired
    private GitLabConfigBean gitLabConfigBean;

    @PostMapping("/clone")
    public Response gitlab4Clone(@RequestBody GitlabCloneReq req) {
        long start = Instant.now().toEpochMilli();

        // 建立 GitLab 连接、获取所有项目
        GitlabAPI gitlabAPI = GitlabAPI.connect(gitLabConfigBean.getHost(), gitLabConfigBean.getToken());
        List<GitlabProject> allProjects = gitlabAPI.getAllProjects();
        log.info("==>  建立 GitLab【{}】连接，并获取所有的项目成功，共计【{}】个项目", gitLabConfigBean.getHost(), allProjects.size());

        // 筛选匹配的项目
        List<GitlabProject> matchList = allProjects.stream()
                .filter(m -> m.getName().contains(req.getProjectName()))
                .collect(Collectors.toList());

        CountDownLatch countDownLatch = new CountDownLatch(matchList.size());

        // 打印筛选出的项目名称
        log.info("==>  筛选条件=【 {} 】，共计筛选出【{}】个项目", req.toString(), matchList.toString());

        // 组装 git 地址
        final UsernamePasswordCredentialsProvider provider = new UsernamePasswordCredentialsProvider(
                gitLabConfigBean.getUsername(), gitLabConfigBean.getPassword());
        for (int i = 0; i < matchList.size(); i++) {
            int finalI = i;
            threadPoolExecutor.execute(new Runnable() {
                GitlabProject m = matchList.get(finalI);

                @Override
                public void run() {
                    try {
                        log.info("********** clone 项目=【{}】到本地目录=【{}】】 **********", m.getName(), req.getOutDir());
                        GitlabUtil.clone(req, provider, m);
                    } catch (GitAPIException e) {
                        log.error("==>  clone 项目=【{}】出错=【{}】", m.getName(), e.getMessage());
                    } finally {
                        // 确保计数器 -1
                        countDownLatch.countDown();
                    }
                }
            });
        }

        // 等待线程执行结束再返回
        try {
            countDownLatch.await(gitLabConfigBean.getMaxWaitTime(), TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            log.info("==>  克隆项目已经等待=【{}】分钟，请自己查看克隆是否完成", gitLabConfigBean.getMaxWaitTime());
            return ResponseGenerator.success("克隆超时，请自行检查是否克隆成功");
        }
        long end = Instant.now().toEpochMilli();

        log.info("==>  项目已全部克隆结束，共计耗时=【{}】ms", (end - start));
        return ResponseGenerator.success("克隆成功");
    }

}