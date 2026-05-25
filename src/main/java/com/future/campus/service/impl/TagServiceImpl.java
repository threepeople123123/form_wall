package com.future.campus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.future.campus.entity.pojo.rdb.TagPojo;
import com.future.campus.mapper.TagMapper;
import com.future.campus.service.TagService;
import org.springframework.stereotype.Service;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, TagPojo> implements TagService {
}
