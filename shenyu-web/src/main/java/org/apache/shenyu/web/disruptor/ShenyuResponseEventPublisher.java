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
import org.apache.shenyu.web.configuration.ShenyuDisruptorConfig;
import org.apache.shenyu.web.disruptor.consumer.ShenyuResponseConsumerExecutor;
import reactor.core.publisher.Mono;

public class ShenyuResponseEventPublisher {
    
    private static final ShenyuResponseEventPublisher INSTANCE = new ShenyuResponseEventPublisher();
    
    private DisruptorProviderManage<Mono> providerManage;
    
    /**
     * Get instance.
     *
     * @return ShenyuClientRegisterEventPublisher instance
     */
    public static ShenyuResponseEventPublisher getInstance() {
        return INSTANCE;
    }
    
    
    /**
     * Start shenyu request disruptor.
     *
     * @param shenyuDisruptorConfig config
     */
    public void start(final ShenyuDisruptorConfig shenyuDisruptorConfig) {
        ShenyuResponseConsumerExecutor.ShenyuResponseConsumerExecutorFactory factory = new ShenyuResponseConsumerExecutor.ShenyuResponseConsumerExecutorFactory();
        providerManage = new DisruptorProviderManage<>(factory, shenyuDisruptorConfig.getThreadSize(), shenyuDisruptorConfig.getBufferSize());
        providerManage.startup();
    }
    
    /**
     * Publish event.
     *
     * @param responseMono the data
     */
    public void publishEvent(final Mono responseMono) {
        providerManage.getProvider().onData(responseMono);
    }
}
