package com.liuyanzhao.sens.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.liuyanzhao.sens.entity.RolePermissionRef;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * @author liuyanzhao
 */
@Mapper
public interface RolePermissionRefMapper extends BaseMapper<RolePermissionRef> {


    /**
     * 根据角色Id删除
     *
     * @param roleId 角色Id
     * @return 影响行数
     */
   Integer deleteByRoleId(Integer roleId);

    /**
     * 批量添加
     *
     * @param rolePermissionRefList 列表
     * @return 影响喊你高数
     */
   Integer batchInsert(List<RolePermissionRef> rolePermissionRefList);
}

