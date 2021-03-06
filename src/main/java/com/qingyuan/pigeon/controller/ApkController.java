package com.qingyuan.pigeon.controller;

import com.qingyuan.pigeon.service.ApkService;
import com.qingyuan.pigeon.utils.UniversalResponseBody;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * app版本接口
 * @Author: qyl
 * @Date: 2020/11/16 20:33
 */
@RequestMapping("/apk")
@RestController
public class ApkController {

    @Resource
    @Qualifier("apkServiceImpl")
    private ApkService apkService;

    /**
     * 上传apk文件
     * @param multipartFile
     * @param version
     * @return 已上线
     */
    @PostMapping("/upload")
    public UniversalResponseBody<String> uploadApk(MultipartFile multipartFile,String version){
        return apkService.uploadApk(multipartFile, version);
    }

    /**
     * 获取最新版本号
     * @param version
     * @return
     * @apiNote 已上线 返回的data为false代表是最新版本 返回为true代表有最新版本
     */
    @GetMapping("/version")
    public UniversalResponseBody<Boolean> getVersionLatest(String version){
        return apkService.getVersionLatest(version);
    }

    /**
     * 下载apk文件
     * @param request
     * @param response
     * @apiNote 已上线
     */
    @GetMapping("/download")
    public void downloadApk(HttpServletRequest request, HttpServletResponse response) throws IOException{
        apkService.download(request, response);

    }

}
