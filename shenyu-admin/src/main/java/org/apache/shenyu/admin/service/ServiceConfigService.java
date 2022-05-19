package org.apache.shenyu.admin.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.shenyu.admin.model.dto.ServiceConfigDTO;
import org.apache.shenyu.admin.model.page.CommonPager;
import org.apache.shenyu.admin.model.page.PageParameter;
import org.apache.shenyu.admin.model.vo.SelectorVO;

/**
 * ServiceConfigService
 */
public interface ServiceConfigService {
    default int createOrUpdate(ServiceConfigDTO serviceConfigDTO) {
        return StringUtils.isEmpty(serviceConfigDTO.getId()) ? create(serviceConfigDTO) : update(serviceConfigDTO);
    }

    int create(ServiceConfigDTO serviceConfigDTO);

    int update(ServiceConfigDTO serviceConfigDTO);

    CommonPager<SelectorVO> listByPage(PageParameter pageParameter);
}
