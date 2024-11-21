package com.scy.springbootinit.controller;

import cn.hutool.core.io.FileUtil;
import com.scy.springbootinit.common.BaseResponse;
import com.scy.springbootinit.common.ErrorCode;
import com.scy.springbootinit.common.ResultUtils;
import com.scy.springbootinit.constant.FileConstant;
import com.scy.springbootinit.manager.CosManager;
import com.scy.springbootinit.model.dto.file.SaveFileRequest;
import com.scy.springbootinit.model.dto.file.UploadFileRequest;
import com.scy.springbootinit.model.entity.FileEntity;
import com.scy.springbootinit.model.entity.User;
import com.scy.springbootinit.model.enums.FileUploadBizEnum;
import com.scy.springbootinit.service.FileService;
import com.scy.springbootinit.service.UserService;
import com.scy.springbootinit.exception.BusinessException;

import java.io.File;
import java.util.Arrays;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private FileService fileService;

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    /**
     * 文件上传
     *
     * @param multipartFile
     * @param uploadFileRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
                                           UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        String biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        validFile(multipartFile, fileUploadBizEnum);
        User loginUser = userService.getLoginUser(request);
        // 文件目录：根据业务、用户来划分
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + multipartFile.getOriginalFilename();
        String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), filename);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            // 返回可访问地址
            return ResultUtils.success(FileConstant.COS_HOST + filepath);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file!= null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

    /**
     * 文件保存
     *
     * @param saveFileRequest
     * @param request
     * @return
     */
    @PostMapping("/save")
    public BaseResponse<String> save(@RequestBody SaveFileRequest saveFileRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String biz = "saveImage";
//        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
//        if (fileUploadBizEnum == null) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
        byte[] imageFile = saveFileRequest.getImageFile();
        if (imageFile == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片文件不能为空");
        }
        try {
            // 将文件数据转换为字节数组
            byte[] imageData = imageFile;

            FileEntity fileEntity = new FileEntity();
            fileEntity.setUserId(loginUser.getId());
            fileEntity.setText(saveFileRequest.getText());
            fileEntity.setColor(saveFileRequest.getColor());
            fileEntity.setTimestamp(System.currentTimeMillis());
            // 设置字节数组到 FileEntity 对象
            fileEntity.setImageFile(imageData);
            int rowsAffected = fileService.insertFileEntity(fileEntity);
            if (rowsAffected > 0) {
                return ResultUtils.success("文件保存成功");
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存失败");
            }
        } catch (Exception e) {
            log.error("文件保存错误", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存失败");
        }
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L;
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }
}