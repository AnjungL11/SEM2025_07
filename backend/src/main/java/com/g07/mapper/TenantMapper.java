package com.g07.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.g07.entity.Tenant;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TenantMapper extends BaseMapper<Tenant> {
}