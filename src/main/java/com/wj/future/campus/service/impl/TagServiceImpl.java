package com.wj.future.campus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wj.future.campus.entity.pojo.TagPojo;
import com.wj.future.campus.mapper.TagMapper;
import com.wj.future.campus.service.TagService;
import org.springframework.stereotype.Service;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, TagPojo> implements TagService {
}
