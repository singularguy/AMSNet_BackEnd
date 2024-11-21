package com.scy.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scy.springbootinit.model.entity.FileEntity;

/**
 * 用户数据库操作
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public interface FileEntityMapper extends BaseMapper<FileEntity> {
    int insertFileEntity(FileEntity fileEntity);
}