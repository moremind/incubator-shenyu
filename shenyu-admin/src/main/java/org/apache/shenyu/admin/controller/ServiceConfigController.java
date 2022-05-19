package org.apache.shenyu.admin.controller;

import org.apache.shenyu.admin.model.dto.ServiceConfigDTO;
import org.apache.shenyu.admin.model.page.CommonPager;
import org.apache.shenyu.admin.model.page.PageParameter;
import org.apache.shenyu.admin.model.query.SelectorQuery;
import org.apache.shenyu.admin.model.result.ShenyuAdminResult;
import org.apache.shenyu.admin.model.vo.SelectorVO;
import org.apache.shenyu.admin.service.ServiceConfigService;
import org.apache.shenyu.admin.utils.ShenyuResultMessage;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * this is service config controller
 */
@Validated
@RestController
@RequestMapping("/service/config")
public class ServiceConfigController {

    private final ServiceConfigService serviceConfigService;

    public ServiceConfigController(ServiceConfigService serviceConfigService) {
        this.serviceConfigService = serviceConfigService;

    }

    @GetMapping("")
    public ShenyuAdminResult queryServiceConfig(@RequestParam @NotNull final Integer currentPage,
                                            @RequestParam @NotNull final Integer pageSize) {
        CommonPager<SelectorVO> commonPager = serviceConfigService.listByPage(new PageParameter(currentPage, pageSize));
        return ShenyuAdminResult.success(ShenyuResultMessage.QUERY_SUCCESS, commonPager);
    }

    @PostMapping("/insert")
    public ShenyuAdminResult createServiceConfig(@Valid @RequestBody final ServiceConfigDTO serviceConfigDTO) {
        Integer createCount = serviceConfigService.createOrUpdate(serviceConfigDTO);
//        System.out.println(serviceConfigDTO.getServiceName());
//        System.out.println(serviceConfigDTO.getSelectors());;
        return ShenyuAdminResult.success(ShenyuResultMessage.CREATE_SUCCESS, createCount);
    }
}
