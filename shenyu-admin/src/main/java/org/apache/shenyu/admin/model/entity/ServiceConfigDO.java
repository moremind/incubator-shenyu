package org.apache.shenyu.admin.model.entity;

import org.apache.commons.lang3.StringUtils;
import org.apache.shenyu.admin.model.dto.ServiceConfigDataDTO;
import org.apache.shenyu.common.utils.UUIDUtils;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.Optional;

/**
 * ServiceConfigDO
 */
public class ServiceConfigDO extends BaseDO {

    private String serviceName;

    private String selectorIds;

    private String ruleIds;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getSelectorIds() {
        return selectorIds;
    }

    public void setSelectorIds(String selectorIds) {
        this.selectorIds = selectorIds;
    }

    public String getRuleIds() {
        return ruleIds;
    }

    public void setRuleIds(String ruleIds) {
        this.ruleIds = ruleIds;
    }

    public static ServiceConfigDO.ServiceConfigDOBuilder builder() {
        return new ServiceConfigDO.ServiceConfigDOBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ServiceConfigDO that = (ServiceConfigDO) o;
        return serviceName.equals(that.serviceName) && selectorIds.equals(that.selectorIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), serviceName, selectorIds, ruleIds);
    }

    public static ServiceConfigDO buildServiceConfigDO(final ServiceConfigDataDTO serviceConfigDataDTO) {
        return Optional.ofNullable(serviceConfigDataDTO).map(item -> {
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            ServiceConfigDO serviceConfigDO = ServiceConfigDO.builder()
                    .serviceName(item.getServiceName())
                    .selectorIds(item.getSelectorIds())
                    .ruleIds(item.getRuleIds())
                    .dateUpdated(currentTime)
                    .build();
            if (StringUtils.isEmpty(item.getId())) {
                serviceConfigDO.setId(UUIDUtils.getInstance().generateShortUuid());
                serviceConfigDO.setDateCreated(currentTime);
            } else {
                serviceConfigDO.setId(item.getId());
            }
            return serviceConfigDO;
        }).orElse(null);
    }


    public static final class ServiceConfigDOBuilder {

        private String id;

        private Timestamp dateCreated;

        private Timestamp dateUpdated;

        private String serviceName;

        private String selectorIds;

        private String ruleIds;

        private ServiceConfigDOBuilder() {
        }

        public ServiceConfigDOBuilder id(String id) {
            this.id = id;
            return this;
        }

        public ServiceConfigDOBuilder dateCreated(Timestamp dateCreated) {
            this.dateCreated = dateCreated;
            return this;
        }

        public ServiceConfigDOBuilder dateUpdated(Timestamp dateUpdated) {
            this.dateUpdated = dateUpdated;
            return this;
        }

        public ServiceConfigDOBuilder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public ServiceConfigDOBuilder selectorIds(String selectorIds) {
            this.selectorIds = selectorIds;
            return this;
        }

        public ServiceConfigDOBuilder ruleIds(String ruleIds) {
            this.ruleIds = ruleIds;
            return this;
        }

        public ServiceConfigDO build() {
            ServiceConfigDO serviceConfigDO = new ServiceConfigDO();
            serviceConfigDO.setId(id);
            serviceConfigDO.setDateCreated(dateCreated);
            serviceConfigDO.setDateUpdated(dateUpdated);
            serviceConfigDO.setServiceName(serviceName);
            serviceConfigDO.setSelectorIds(selectorIds);
            serviceConfigDO.setRuleIds(ruleIds);
            return serviceConfigDO;
        }
    }
}
