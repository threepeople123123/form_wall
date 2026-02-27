package com.wj.form.wall.mapping;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wj.form.wall.entity.pojo.UserPojo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserPojo> {
}
