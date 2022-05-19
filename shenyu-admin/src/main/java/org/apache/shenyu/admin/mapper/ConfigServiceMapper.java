package org.apache.shenyu.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.shenyu.admin.model.entity.RuleDO;
import org.apache.shenyu.admin.model.entity.ServiceConfigDO;
import org.apache.shenyu.admin.validation.ExistProvider;

import java.io.Serializable;

/**
 * The interface Config Service mapper.
 */
@Mapper
public interface ConfigServiceMapper extends ExistProvider {

    /**
     * service existed.
     * @param key key
     * @return existed
     */
    @Override
    Boolean existed(Serializable key);

    /**
     * insert service config.
     * @param serviceConfigDO {@linkplain ServiceConfigDO}
     * @return rows int
     */
    int insert(ServiceConfigDO serviceConfigDO);

    /**
     * insert selective service config.
     *
     * @param ruleDO {@linkplain RuleDO}
     * @return rows int
     */
    int insertSelective(RuleDO ruleDO);
}
