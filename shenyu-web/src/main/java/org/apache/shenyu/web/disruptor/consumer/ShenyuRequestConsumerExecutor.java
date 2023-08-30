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

package org.apache.shenyu.web.disruptor.consumer;

import org.apache.commons.logging.Log;
import org.apache.shenyu.common.constant.Constants;
import org.apache.shenyu.disruptor.consumer.QueueConsumerExecutor;
import org.apache.shenyu.disruptor.consumer.QueueConsumerFactory;
import org.apache.shenyu.web.disruptor.ShenyuResponseEventPublisher;
import org.apache.shenyu.web.handler.ShenyuWebHandler;
import org.apache.shenyu.web.server.ShenyuRequestExchange;
import org.springframework.http.HttpLogging;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class ShenyuRequestConsumerExecutor<T extends ShenyuRequestExchange> extends QueueConsumerExecutor<T> {
    
    private static final Log LOGGER = HttpLogging.forLogName(ShenyuRequestConsumerExecutor.class);

    private final ShenyuResponseEventPublisher shenyuResponseEventPublisher = ShenyuResponseEventPublisher.getInstance();
    
    @Override
    public void run() {
        final ShenyuRequestExchange shenyuRequestExchange = getData();
        LOGGER.info("get request...");
        new Thread(() -> {
            Mono<Void> execute = new ShenyuWebHandler.DefaultShenyuPluginChain(shenyuRequestExchange.getPlugins()).execute(shenyuRequestExchange.getExchange());
            execute.doOnSubscribe(s -> {
                final ServerWebExchange requestExchangeExchange = shenyuRequestExchange.getExchange();
                final Mono responstWriteWithMono = requestExchangeExchange.getAttribute(Constants.RESPONSE_HANDLER_SEND_DISRUPTOR_MONO);
                shenyuResponseEventPublisher.publishEvent(responstWriteWithMono);
            }).subscribe();
        }).start();
    }
    
    public static class ShenyuRequestConsumerExecutorFactory<T extends ShenyuRequestExchange> implements QueueConsumerFactory<T> {
        @Override
        public ShenyuRequestConsumerExecutor<T> create() {
            
            return new ShenyuRequestConsumerExecutor<>();
        }
    
        @Override
        public String fixName() {
            return "shenyu_request";
        }
    }
}
