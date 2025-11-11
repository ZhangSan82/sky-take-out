package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@Api(tags = "通用接口")
@RequestMapping("/admin/common")
public class CommonController {

    @Autowired
    private AliOssUtil aliossUtil;


    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传:{}",file);

        try {
            //文件原始名
            String fileName = file.getOriginalFilename();

            //文件原始名的后缀
            String suffixName = fileName.substring(fileName.lastIndexOf("."));

            //创造新的文件名
            String newFileName = UUID.randomUUID().toString() + suffixName;

            //文件的请求路径
            String filePath =  aliossUtil.upload(file.getBytes(),newFileName);
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败:{}",e);
        }

        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
