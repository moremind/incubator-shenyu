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

package org.apache.shenyu.web.disruptor;

import org.apache.shenyu.disruptor.DisruptorProviderManage;
import org.apache.shenyu.disruptor.provider.DisruptorProvider;
import org.apache.shenyu.web.configuration.ShenyuDisruptorConfig;
import org.apache.shenyu.web.disruptor.consumer.ShenyuRequestConsumerExecutor.ShenyuRequestConsumerExecutorFactory;
import org.apache.shenyu.web.server.ShenyuRequestExchange;

public class ShenyuRequestEventPublisher {
    
    private static final ShenyuRequestEventPublisher INSTANCE = new ShenyuRequestEventPublisher();
    
    private DisruptorProviderManage<ShenyuRequestExchange> providerManage;

    /**
     * Get instance.
     *
     * @return ShenyuClientRegisterEventPublisher instance
     */
    public static ShenyuRequestEventPublisher getInstance() {
        return INSTANCE;
    }
    
    
    /**
     * Start shenyu request disruptor.
     *
     * @param shenyuDisruptorConfig config
     */
    public void start(final ShenyuDisruptorConfig shenyuDisruptorConfig) {
        ShenyuRequestConsumerExecutorFactory<ShenyuRequestExchange> factory = new ShenyuRequestConsumerExecutorFactory<>();
        providerManage = new DisruptorProviderManage<>(factory, shenyuDisruptorConfig.getThreadSize(), shenyuDisruptorConfig.getBufferSize());
        providerManage.startup();
    }
    
    /**
     * Publish event.
     *
     * @param shenyuServerExchange the data
     */
    public void publishEvent(final ShenyuRequestExchange shenyuServerExchange) {
        DisruptorProvider<ShenyuRequestExchange> provider = providerManage.getProvider();
        provider.onData(shenyuServerExchange);
    }
}
