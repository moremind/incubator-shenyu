package org.apache.shenyu.admin.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.shenyu.admin.mapper.ConfigServiceMapper;
import org.apache.shenyu.admin.model.dto.SelectorDTO;
import org.apache.shenyu.admin.model.dto.ServiceConfigDTO;
import org.apache.shenyu.admin.model.dto.ServiceConfigDataDTO;
import org.apache.shenyu.admin.model.entity.BaseDO;
import org.apache.shenyu.admin.model.entity.SelectorDO;
import org.apache.shenyu.admin.model.entity.ServiceConfigDO;
import org.apache.shenyu.admin.service.SelectorService;
import org.apache.shenyu.admin.service.ServiceConfigService;
import org.apache.shenyu.admin.utils.Assert;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link org.apache.shenyu.admin.service.ServiceConfigService}.
 */
@Service
public class ServiceConfigServiceImpl implements ServiceConfigService {

//    private final SelectorMapper selectorMapper;
//
//    private final SelectorConditionMapper selectorConditionMapper;
//
//    private final RuleMapper ruleMapper;
//
//    private final RuleConditionMapper ruleConditionMapper;

    private final SelectorService selectorService;

    private final ConfigServiceMapper configServiceMapper;

    public ServiceConfigServiceImpl(SelectorService selectorService, ConfigServiceMapper configServiceMapper) {
        this.selectorService = selectorService;
        this.configServiceMapper = configServiceMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int createOrUpdate(ServiceConfigDTO serviceConfigDTO) {
        return ServiceConfigService.super.createOrUpdate(serviceConfigDTO);
    }

    @Override
    public int create(ServiceConfigDTO serviceConfigDTO) {
        Assert.notNull(serviceConfigDTO.getServiceName(), "service name must exist.");
        Assert.notNull(serviceConfigDTO.getSelectors(), "selector must exist.");

        List<SelectorDTO> selectorDTOList = serviceConfigDTO.getSelectors();
        // generate selector
        List<SelectorDO> selectorDOList = selectorDTOList.stream()
                .map(SelectorDO::buildSelectorDO)
                .collect(Collectors.toList());

        List<String> selectorIdList = selectorDOList.stream()
                .map(BaseDO::getId)
                .collect(Collectors.toList());

        ServiceConfigDataDTO serviceConfigDataDTO = ServiceConfigDataDTO.builder()
                .serviceName(serviceConfigDTO.getServiceName())
                .selectorIds(StringUtils.join(selectorIdList, ","))
                .build();

        ServiceConfigDO serviceConfigDO = ServiceConfigDO.buildServiceConfigDO(serviceConfigDataDTO);

        List<SelectorDTO> selectorDTOS = selectorDOList.stream().map(ServiceConfigServiceImpl::buildSelectorDTO).collect(Collectors.toList());
        // add selector
        selectorDTOS.forEach(selectorService::create);

        // add selector
//        selectorDTOS.forEach(selectorService::create);

        return 0;
    }

    @Override
    public int update(ServiceConfigDTO serviceConfigDTO) {
        return 0;
    }

    private static SelectorDTO buildSelectorDTO(SelectorDO selectorDO) {
        return Optional.ofNullable(selectorDO).map(item -> SelectorDTO.builder()
                .id(item.getId())
                .pluginId(item.getPluginId())
                .name(item.getName())
                .matchMode(item.getMatchMode())
                .type(item.getType())
                .sort(item.getSort())
                .enabled(item.getEnabled())
                .loged(item.getLoged())
                .continued(item.getContinued())
                .handle(item.getHandle())
                .build()).orElse(null);
    }
}
