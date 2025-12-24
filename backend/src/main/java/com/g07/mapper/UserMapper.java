package com.g07.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.g07.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 【新增】查询用户列表，并关联查询租户名称
     */
    @Select("SELECT u.*, t.tenant_name " +
            "FROM user u " +
            "LEFT JOIN tenant t ON u.tenant_id = t.tenant_id " +
            "ORDER BY u.create_time DESC")
    List<User> selectListWithTenantName();
}