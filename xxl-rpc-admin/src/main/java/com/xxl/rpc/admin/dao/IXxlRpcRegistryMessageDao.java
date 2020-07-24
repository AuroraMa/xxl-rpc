package com.xxl.rpc.admin.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.xxl.rpc.admin.core.model.XxlRpcRegistryMessage;

/**
 * @author xuxueli 2018-11-20
 */
@Mapper
public interface IXxlRpcRegistryMessageDao {

    public int add(@Param("xxlRpcRegistryMessage") XxlRpcRegistryMessage xxlRpcRegistryMessage);

    public List<XxlRpcRegistryMessage> findMessage(@Param("excludeIds") List<Integer> excludeIds);

    public int cleanMessage(@Param("messageTimeout") int messageTimeout);

}
