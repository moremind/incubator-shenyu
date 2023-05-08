/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shenyu.k8s.parser;

import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Endpoints;
import io.kubernetes.client.openapi.models.V1EndpointSubset;
import io.kubernetes.client.openapi.models.V1EndpointAddress;
import io.kubernetes.client.openapi.models.V1Ingress;
import io.kubernetes.client.openapi.models.V1IngressBackend;
import io.kubernetes.client.openapi.models.V1IngressRule;
import io.kubernetes.client.openapi.models.V1IngressTLS;
import io.kubernetes.client.openapi.models.V1HTTPIngressPath;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1IngressServiceBackend;
import io.kubernetes.client.openapi.models.V1Secret;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shenyu.common.config.ssl.SslCrtAndKeyStream;
import org.apache.shenyu.common.dto.ConditionData;
import org.apache.shenyu.common.dto.RuleData;
import org.apache.shenyu.common.dto.SelectorData;
import org.apache.shenyu.common.dto.convert.rule.impl.DivideRuleHandle;
import org.apache.shenyu.common.dto.convert.selector.DivideUpstream;
import org.apache.shenyu.common.enums.MatchModeEnum;
import org.apache.shenyu.common.enums.LoadBalanceEnum;
import org.apache.shenyu.common.enums.OperatorEnum;
import org.apache.shenyu.common.enums.ParamTypeEnum;
import org.apache.shenyu.common.enums.PluginEnum;
import org.apache.shenyu.common.enums.SelectorTypeEnum;
import org.apache.shenyu.common.utils.GsonUtils;
import org.apache.shenyu.k8s.common.IngressConstants;
import org.apache.shenyu.k8s.common.ShenyuMemoryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Map;

/**
 * Parser of Ingress.
 */
public class IngressParser implements K8sResourceParser<V1Ingress> {

    private static final Logger LOG = LoggerFactory.getLogger(IngressParser.class);

    private final Lister<V1Service> serviceLister;

    private final Lister<V1Endpoints> endpointsLister;

    /**
     * IngressParser Constructor.
     *
     * @param serviceInformer serviceInformer
     * @param endpointsInformer endpointsInformer
     */
    public IngressParser(final SharedIndexInformer<V1Service> serviceInformer, final SharedIndexInformer<V1Endpoints> endpointsInformer) {
        this.serviceLister = new Lister<>(serviceInformer.getIndexer());
        this.endpointsLister = new Lister<>(endpointsInformer.getIndexer());
    }

    /**
     * Parse ingress to ShenyuMemoryConfig.
     *
     * @param ingress ingress resource
     * @param coreV1Api coreV1Api
     * @return ShenyuMemoryConfig
     */
    @Override
    public ShenyuMemoryConfig parse(final V1Ingress ingress, final CoreV1Api coreV1Api) {
        ShenyuMemoryConfig res = new ShenyuMemoryConfig();

        if (ingress.getSpec() != null) {
            // Parse the default backend
            V1IngressBackend defaultBackend = ingress.getSpec().getDefaultBackend();
            List<V1IngressRule> rules = ingress.getSpec().getRules();
            List<V1IngressTLS> tlsList = ingress.getSpec().getTls();

            String namespace = Objects.requireNonNull(ingress.getMetadata()).getNamespace();
            List<DivideUpstream> defaultUpstreamList = parseDefaultService(defaultBackend, namespace);

            if (rules == null || rules.isEmpty()) {
                // if rules is null, defaultBackend become global default
                if (defaultBackend != null && defaultBackend.getService() != null) {
                    Pair<SelectorData, RuleData> defaultRouteConfig = getDefaultRouteConfig(defaultUpstreamList, ingress.getMetadata().getAnnotations());
                    res.setGlobalDefaultBackend(Pair.of(Pair.of(namespace + "/" + ingress.getMetadata().getName(), defaultBackend.getService().getName()),
                            defaultRouteConfig));
                }
            } else {
                // if rules is not null, defaultBackend is default in this ingress
                List<Pair<SelectorData, RuleData>> routeList = new ArrayList<>(rules.size());
                for (V1IngressRule ingressRule : rules) {
                    List<Pair<SelectorData, RuleData>> routes = parseIngressRule(ingressRule, defaultUpstreamList,
                            Objects.requireNonNull(ingress.getMetadata()).getNamespace(), ingress.getMetadata().getAnnotations());
                    routeList.addAll(routes);
                }
                res.setRouteConfigList(routeList);
            }

            // Parse tls
            if (tlsList != null && !tlsList.isEmpty()) {
                List<SslCrtAndKeyStream> sslList = new ArrayList<>();
                for (V1IngressTLS tls : tlsList) {
                    if (tls.getSecretName() != null && tls.getHosts() != null && !tls.getHosts().isEmpty()) {
                        try {
                            V1Secret secret = coreV1Api.readNamespacedSecret(tls.getSecretName(), namespace, "ture");
                            if (secret.getData() != null) {
                                InputStream keyCertChainInputStream = new ByteArrayInputStream(secret.getData().get("tls.crt"));
                                InputStream keyInputStream = new ByteArrayInputStream(secret.getData().get("tls.key"));
                                tls.getHosts().forEach(host ->
                                    sslList.add(new SslCrtAndKeyStream(host, keyCertChainInputStream, keyInputStream))
                                );
                            }
                        } catch (ApiException e) {
                            LOG.error("parse tls failed ", e);
                        }
                    }
                }
                res.setTlsConfigList(sslList);
            }
        }
        return res;
    }

    private List<DivideUpstream> parseDefaultService(final V1IngressBackend defaultBackend, final String namespace) {
        List<DivideUpstream> defaultUpstreamList = new ArrayList<>();
        if (defaultBackend != null && defaultBackend.getService() != null) {
            String serviceName = defaultBackend.getService().getName();
            // shenyu routes directly to the container
            V1Endpoints v1Endpoints = endpointsLister.namespace(namespace).get(serviceName);
            List<V1EndpointSubset> subsets = v1Endpoints.getSubsets();
            if (subsets == null || subsets.isEmpty()) {
                LOG.info("Endpoints {} do not have subsets", serviceName);
            } else {
                for (V1EndpointSubset subset : subsets) {
                    List<V1EndpointAddress> addresses = subset.getAddresses();
                    if (addresses == null || addresses.isEmpty()) {
                        continue;
                    }
                    for (V1EndpointAddress address : addresses) {
                        String upstreamIp = address.getIp();
                        String defaultPort = parsePort(defaultBackend.getService());
                        if (defaultPort != null) {
                            DivideUpstream upstream = new DivideUpstream();
                            upstream.setUpstreamUrl(upstreamIp + ":" + defaultPort);
                            upstream.setWeight(100);
                            // TODO support config protocol in annotation
                            upstream.setProtocol("http://");
                            upstream.setWarmup(0);
                            upstream.setStatus(true);
                            upstream.setUpstreamHost("");
                            defaultUpstreamList.add(upstream);
                        }
                    }
                }
            }
        }
        return defaultUpstreamList;
    }

    private List<Pair<SelectorData, RuleData>> parseIngressRule(final V1IngressRule ingressRule,
                                                                final List<DivideUpstream> defaultUpstream,
                                                                final String namespace,
                                                                final Map<String, String> annotations) {
        List<Pair<SelectorData, RuleData>> res = new ArrayList<>();

        ConditionData hostCondition = null;
        if (ingressRule.getHost() != null) {
            hostCondition = new ConditionData();
            hostCondition.setParamType(ParamTypeEnum.DOMAIN.getName());
            hostCondition.setOperator(OperatorEnum.EQ.getAlias());
            hostCondition.setParamValue(ingressRule.getHost());
        }
        if (ingressRule.getHttp() != null) {
            List<V1HTTPIngressPath> paths = ingressRule.getHttp().getPaths();
            if (paths != null) {
                for (V1HTTPIngressPath path : paths) {
                    if (path.getPath() == null) {
                        continue;
                    }

                    OperatorEnum operator;
                    if ("ImplementationSpecific".equals(path.getPathType())) {
                        operator = OperatorEnum.MATCH;
                    } else if ("Prefix".equals(path.getPathType())) {
                        operator = OperatorEnum.STARTS_WITH;
                    } else if ("Exact".equals(path.getPathType())) {
                        operator = OperatorEnum.EQ;
                    } else {
                        LOG.info("Invalid path type, set it with match operator");
                        operator = OperatorEnum.MATCH;
                    }

                    ConditionData pathCondition = new ConditionData();
                    pathCondition.setOperator(operator.getAlias());
                    pathCondition.setParamType(ParamTypeEnum.URI.getName());
                    pathCondition.setParamValue(path.getPath());
                    List<ConditionData> conditionList = new ArrayList<>(2);
                    if (hostCondition != null) {
                        conditionList.add(hostCondition);
                    }
                    conditionList.add(pathCondition);

                    SelectorData selectorData = SelectorData.builder()
                            .pluginId(String.valueOf(PluginEnum.DIVIDE.getCode()))
                            .pluginName(PluginEnum.DIVIDE.getName())
                            .name(path.getPath())
                            .matchMode(MatchModeEnum.AND.getCode())
                            .type(SelectorTypeEnum.CUSTOM_FLOW.getCode())
                            .enabled(true)
                            .logged(false)
                            .continued(true)
                            .conditionList(conditionList).build();
                    List<DivideUpstream> upstreamList = parseUpstream(path.getBackend(), namespace);
                    if (upstreamList.isEmpty()) {
                        upstreamList = defaultUpstream;
                    }
                    selectorData.setHandle(GsonUtils.getInstance().toJson(upstreamList));

                    DivideRuleHandle divideRuleHandle = new DivideRuleHandle();
                    if (annotations != null) {
                        divideRuleHandle.setLoadBalance(annotations.getOrDefault(IngressConstants.LOADBALANCER_ANNOTATION_KEY, LoadBalanceEnum.RANDOM.getName()));
                        divideRuleHandle.setRetry(Integer.parseInt(annotations.getOrDefault(IngressConstants.RETRY_ANNOTATION_KEY, "3")));
                        divideRuleHandle.setTimeout(Long.parseLong(annotations.getOrDefault(IngressConstants.TIMEOUT_ANNOTATION_KEY, "3000")));
                        divideRuleHandle.setHeaderMaxSize(Long.parseLong(annotations.getOrDefault(IngressConstants.HEADER_MAX_SIZE_ANNOTATION_KEY, "10240")));
                        divideRuleHandle.setRequestMaxSize(Long.parseLong(annotations.getOrDefault(IngressConstants.REQUEST_MAX_SIZE_ANNOTATION_KEY, "102400")));
                    }
                    RuleData ruleData = RuleData.builder()
                            .name(path.getPath())
                            .pluginName(PluginEnum.DIVIDE.getName())
                            .matchMode(MatchModeEnum.AND.getCode())
                            .conditionDataList(conditionList)
                            .handle(GsonUtils.getInstance().toJson(divideRuleHandle))
                            .loged(false)
                            .enabled(true).build();

                    res.add(Pair.of(selectorData, ruleData));
                }
            }
        }
        return res;
    }

    private String parsePort(final V1IngressServiceBackend service) {
        if (service.getPort() != null) {
            if (service.getPort().getNumber() != null && service.getPort().getNumber() > 0) {
                return String.valueOf(service.getPort().getNumber());
            } else if (service.getPort().getName() != null && !"".equals(service.getPort().getName().trim())) {
                return service.getPort().getName().trim();
            }
        }
        return null;
    }

    private List<DivideUpstream> parseUpstream(final V1IngressBackend backend, final String namespace) {
        List<DivideUpstream> upstreamList = new ArrayList<>();
        if (backend != null && backend.getService() != null && backend.getService().getName() != null) {
            String serviceName = backend.getService().getName();
            // shenyu routes directly to the container
            V1Endpoints v1Endpoints = endpointsLister.namespace(namespace).get(serviceName);
            List<V1EndpointSubset> subsets = v1Endpoints.getSubsets();
            if (subsets == null || subsets.isEmpty()) {
                LOG.info("Endpoints {} do not have subsets", serviceName);
            } else {
                for (V1EndpointSubset subset : subsets) {
                    List<V1EndpointAddress> addresses = subset.getAddresses();
                    if (addresses == null || addresses.isEmpty()) {
                        continue;
                    }
                    for (V1EndpointAddress address : addresses) {
                        String upstreamIp = address.getIp();
                        String defaultPort = parsePort(backend.getService());
                        if (defaultPort != null) {
                            DivideUpstream upstream = new DivideUpstream();
                            upstream.setUpstreamUrl(upstreamIp + ":" + defaultPort);
                            upstream.setWeight(100);
                            // TODO support config protocol in annotation
                            upstream.setProtocol("http://");
                            upstream.setWarmup(0);
                            upstream.setStatus(true);
                            upstream.setUpstreamHost("");
                            upstreamList.add(upstream);
                        }
                    }
                }
            }
        }
        return upstreamList;
    }

    private Pair<SelectorData, RuleData> getDefaultRouteConfig(final List<DivideUpstream> divideUpstream, final Map<String, String> annotations) {
        final ConditionData conditionData = new ConditionData();
        conditionData.setParamName("default");
        conditionData.setParamType(ParamTypeEnum.URI.getName());
        conditionData.setOperator(OperatorEnum.PATH_PATTERN.getAlias());
        conditionData.setParamValue("/**");

        final SelectorData selectorData = SelectorData.builder()
                .name("default-selector")
                .sort(Integer.MAX_VALUE)
                .conditionList(Collections.singletonList(conditionData))
                .handle(GsonUtils.getInstance().toJson(divideUpstream))
                .enabled(true)
                .id("1")
                .pluginName(PluginEnum.DIVIDE.getName())
                .pluginId(String.valueOf(PluginEnum.DIVIDE.getCode()))
                .logged(false)
                .continued(true)
                .matchMode(MatchModeEnum.AND.getCode())
                .type(SelectorTypeEnum.FULL_FLOW.getCode()).build();

        DivideRuleHandle divideRuleHandle = new DivideRuleHandle();
        // TODO need an annotation parsing common way
        if (annotations != null) {
            divideRuleHandle.setLoadBalance(annotations.getOrDefault(IngressConstants.LOADBALANCER_ANNOTATION_KEY, LoadBalanceEnum.RANDOM.getName()));
            divideRuleHandle.setRetry(Integer.parseInt(annotations.getOrDefault(IngressConstants.RETRY_ANNOTATION_KEY, "3")));
            divideRuleHandle.setTimeout(Long.parseLong(annotations.getOrDefault(IngressConstants.TIMEOUT_ANNOTATION_KEY, "3000")));
            divideRuleHandle.setHeaderMaxSize(Long.parseLong(annotations.getOrDefault(IngressConstants.HEADER_MAX_SIZE_ANNOTATION_KEY, "10240")));
            divideRuleHandle.setRequestMaxSize(Long.parseLong(annotations.getOrDefault(IngressConstants.REQUEST_MAX_SIZE_ANNOTATION_KEY, "102400")));
        }
        final RuleData ruleData = RuleData.builder()
                .selectorId("1")
                .pluginName(PluginEnum.DIVIDE.getName())
                .name("default-rule")
                .matchMode(MatchModeEnum.AND.getCode())
                .conditionDataList(Collections.singletonList(conditionData))
                .handle(GsonUtils.getInstance().toJson(divideRuleHandle))
                .loged(false)
                .enabled(true)
                .sort(Integer.MAX_VALUE).build();

        return Pair.of(selectorData, ruleData);
    }
}
