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

package org.apache.shenyu.web.configuration;

//@ConfigurationProperties(prefix = "shenyu.disruptor")
public class ShenyuDisruptorConfig {
    
    private Integer bufferSize = 1024;
    
    private Integer threadSize = 20;
    
    /**
     * get bufferSize.
     *
     * @return bufferSize
     */
    public Integer getBufferSize() {
        return bufferSize;
    }
    
    /**
     * set bufferSize.
     * @param bufferSize bufferSize
     */
    public void setBufferSize(final Integer bufferSize) {
        this.bufferSize = bufferSize;
    }
    
    /**
     * get threadSize.
     * @return  threadSize
     */
    public Integer getThreadSize() {
        return threadSize;
    }
    
    /**
     * set threadSize.
     * @param threadSize threadSize
     */
    public void setThreadSize(final Integer threadSize) {
        this.threadSize = threadSize;
    }
}
