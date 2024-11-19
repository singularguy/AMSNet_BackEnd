package com.scy.springbootinit.service;

import com.scy.springbootinit.model.entity.FileEntity;

public interface FileService {
    /**
     * 保存文件实体到数据库。
     * @param fileEntity 文件实体
     * @return 插入的行数
     */
    int insertFileEntity(FileEntity fileEntity);
}