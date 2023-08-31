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
import org.apache.shenyu.disruptor.consumer.QueueConsumerExecutor;
import org.apache.shenyu.disruptor.consumer.QueueConsumerFactory;
import org.springframework.http.HttpLogging;
import reactor.core.publisher.Mono;
import reactor.netty.channel.AbortedException;

public class ShenyuResponseConsumerExecutor<T extends Mono> extends QueueConsumerExecutor<T> {

    private static final Log LOGGER = HttpLogging.forLogName(ShenyuResponseConsumerExecutor.class);
    
    @Override
    public void run() {
        LOGGER.info("send response...");
        try {
            // consumer response Mono
            getData().subscribe();
        } catch (AbortedException e) {
            // ignore AbortedException
        }
    }
    
    public static class ShenyuResponseConsumerExecutorFactory<T extends Mono> implements QueueConsumerFactory<T> {
        @Override
        public ShenyuResponseConsumerExecutor<T> create() {
            
            return new ShenyuResponseConsumerExecutor<>();
        }
    
        @Override
        public String fixName() {
            return "thread-response";
        }
    }
}
