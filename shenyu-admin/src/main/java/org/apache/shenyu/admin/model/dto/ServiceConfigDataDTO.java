package org.apache.shenyu.admin.model.dto;

/**
 * @author <a href="mailto:hefe@dazd.cn">hefengen</a>
 * @version ServiceConfigDataDTO Create On 2022/5/10
 */
public class ServiceConfigDataDTO {

    private String id;

    private String serviceName;

    private String selectorIds;

    private String ruleIds;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public static ServiceConfigDataDTO.ServiceConfigDataDTOBuilder builder() {
        return new ServiceConfigDataDTO.ServiceConfigDataDTOBuilder();
    }


    public static final class ServiceConfigDataDTOBuilder {
        private String id;
        private String serviceName;
        private String selectorIds;
        private String ruleIds;

        private ServiceConfigDataDTOBuilder() {
        }

        public ServiceConfigDataDTOBuilder id(String id) {
            this.id = id;
            return this;
        }

        public ServiceConfigDataDTOBuilder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public ServiceConfigDataDTOBuilder selectorIds(String selectorIds) {
            this.selectorIds = selectorIds;
            return this;
        }

        public ServiceConfigDataDTOBuilder ruleIds(String ruleIds) {
            this.ruleIds = ruleIds;
            return this;
        }

        public ServiceConfigDataDTO build() {
            ServiceConfigDataDTO serviceConfigDataDTO = new ServiceConfigDataDTO();
            serviceConfigDataDTO.setId(id);
            serviceConfigDataDTO.setServiceName(serviceName);
            serviceConfigDataDTO.setSelectorIds(selectorIds);
            serviceConfigDataDTO.setRuleIds(ruleIds);
            return serviceConfigDataDTO;
        }
    }
}
