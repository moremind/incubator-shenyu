#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

docker save shenyu-examples-eureka:latest shenyu-examples-springcloud:latest | sudo k3s ctr images import -

# init kubernetes for mysql
SHENYU_TESTCASE_DIR=$(dirname "$(dirname "$(dirname "$(dirname "$0")")")")
bash "$SHENYU_TESTCASE_DIR"/k8s/script/init/mysql_container_init.sh

# init register center
CUR_PATH=$(readlink -f "$(dirname "$0")")
PRGDIR=$(dirname "$CUR_PATH")
echo "$PRGDIR"
kubectl apply -f "${PRGDIR}"/shenyu-examples-eureka.yml
kubectl apply -f "${PRGDIR}"/shenyu-cm.yml
sleep 10s
./mvnw -B -f ./shenyu-e2e/pom.xml -pl shenyu-e2e-case/shenyu-e2e-case-spring-cloud -am test-compile -T1C
# init shenyu sync
SYNC_ARRAY=("websocket" "http" "zookeeper" "etcd" "nacos")
MIDDLEWARE_SYNC_ARRAY=("zookeeper" "etcd" "nacos")
for sync in ${SYNC_ARRAY[@]}; do
  echo -e "-------------------\n"
  echo "[Start ${sync} synchronous] create shenyu-admin-${sync}.yml shenyu-bootstrap-${sync}.yml shenyu-examples-springcloud.yml"
  # shellcheck disable=SC2199
  # shellcheck disable=SC2076
  if [[ "${MIDDLEWARE_SYNC_ARRAY[@]}" =~ "${sync}" ]]; then
    kubectl apply -f "$SHENYU_TESTCASE_DIR"/k8s/shenyu-"${sync}".yml
    sleep 10s
  fi
  kubectl apply -f "${PRGDIR}"/shenyu-admin-"${sync}".yml
  sleep 10s
  kubectl apply -f "${PRGDIR}"/shenyu-bootstrap-"${sync}".yml
  kubectl apply -f "${PRGDIR}"/shenyu-examples-springcloud.yml
  sleep 50s
  kubectl get pod -o wide
  sh "${CUR_PATH}"/healthcheck.sh mysql http://localhost:31095/actuator/health http://localhost:31195/actuator/health
  ## run e2e-test
  ./mvnw -B -f ./shenyu-e2e/pom.xml -pl shenyu-e2e-case/shenyu-e2e-case-spring-cloud -am test
  # shellcheck disable=SC2181
  if (($?)); then
    echo "${sync}-sync-e2e-test failed"
    exit 1
  fi
  kubectl delete -f "${PRGDIR}"/shenyu-admin-"${sync}".yml
  kubectl delete -f "${PRGDIR}"/shenyu-bootstrap-"${sync}".yml
  kubectl delete -f "${PRGDIR}"/shenyu-examples-springcloud.yml
  # shellcheck disable=SC2199
  # shellcheck disable=SC2076
  if [[ "${MIDDLEWARE_SYNC_ARRAY[@]}" =~ "${sync}" ]]; then
    kubectl delete -f "$SHENYU_TESTCASE_DIR"/k8s/shenyu-"${sync}".yml
  fi
  echo "[Remove ${sync} synchronous] delete shenyu-admin-${sync}.yml shenyu-bootstrap-${sync}.yml shenyu-examples-springcloud.yml"
done

#kubectl apply -f "${PRGDIR}"/shenyu-admin-websocket.yml
#kubectl apply -f "${PRGDIR}"/shenyu-bootstrap-websocket.yml
#
#
#kubectl get pod -o wide
#
#sleep 60s
#
#kubectl get pod -o wide
#
#chmod +x "${CUR_PATH}"/healthcheck.sh
#sh "${CUR_PATH}"/healthcheck.sh mysql http://localhost:31095/actuator/health http://localhost:31195/actuator/health
#
### run e2e-test
#
#curl -S "http://localhost:31195/actuator/pluginData"
#
#./mvnw -B -f ./shenyu-e2e/pom.xml -pl shenyu-e2e-case/shenyu-e2e-case-spring-cloud -am test