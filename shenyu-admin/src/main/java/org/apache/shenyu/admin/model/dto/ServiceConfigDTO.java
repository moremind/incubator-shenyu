package org.apache.shenyu.admin.model.dto;

import java.util.List;

public class ServiceConfigDTO {

    private String id;

    private String serviceName;

    private List<SelectorDTO> selectors;

    private List<RuleDTO> rules;

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

    public List<SelectorDTO> getSelectors() {
        return selectors;
    }

    public void setSelectors(List<SelectorDTO> selectors) {
        this.selectors = selectors;
    }

    public List<RuleDTO> getRules() {
        return rules;
    }

    public void setRules(List<RuleDTO> rules) {
        this.rules = rules;
    }

    @Override
    public String toString() {
        return "ServiceConfigDTO{" +
                "id='" + id + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", selectors=" + selectors +
                '}';
    }
}
