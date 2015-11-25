/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kaaproject.kaa.server.operations.service.akka.actors.supervision;

import org.kaaproject.kaa.server.common.core.plugin.instance.PluginLifecycleException;
import org.kaaproject.kaa.server.operations.service.akka.AkkaContext;

import scala.concurrent.duration.Duration;
import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.actor.SupervisorStrategy.Directive;
import akka.japi.Function;

public final class SupervisionStrategyFactory {

    public static SupervisorStrategy createIORouterStrategy(AkkaContext context) {
        return buildResumeOrEscalateStrategy();
    }

    public static SupervisorStrategy createOpsActorStrategy(AkkaContext context) {
        return buildResumeOnRuntimeErrorStrategy();
    }

    public static SupervisorStrategy createTenantActorStrategy(AkkaContext context) {
        return buildResumeOnRuntimeErrorStrategy();
    }

    public static SupervisorStrategy createApplicationActorStrategy(AkkaContext context) {
        return buildRestartOrEscalateStrategy();
    }

    private static SupervisorStrategy buildResumeOrEscalateStrategy() {
        return new OneForOneStrategy(-1, Duration.Inf(), new Function<Throwable, SupervisorStrategy.Directive>() {
            @Override
            public Directive apply(Throwable t) throws Exception {
                if (t instanceof PluginLifecycleException){
                    return OneForOneStrategy.stop();
                } else if (t instanceof Error) {
                    return OneForOneStrategy.escalate();
                } else {
                    return OneForOneStrategy.resume();
                }
            }
        });
    }

    private static SupervisorStrategy buildRestartOrEscalateStrategy() {
        return new OneForOneStrategy(-1, Duration.Inf(), new Function<Throwable, SupervisorStrategy.Directive>() {
            @Override
            public Directive apply(Throwable t) throws Exception {
                if (t instanceof PluginLifecycleException){
                    return OneForOneStrategy.stop();
                } else if (t instanceof Error) {
                    return OneForOneStrategy.escalate();
                } else {
                    return OneForOneStrategy.restart();
                }
            }
        });
    }

    private static SupervisorStrategy buildResumeOnRuntimeErrorStrategy() {
        return new OneForOneStrategy(-1, Duration.Inf(), new Function<Throwable, SupervisorStrategy.Directive>() {
            @Override
            public Directive apply(Throwable t) throws Exception {
                if (t instanceof PluginLifecycleException){
                    return OneForOneStrategy.stop();
                } else if (t instanceof Error) {
                    return OneForOneStrategy.escalate();
                } else if (t instanceof RuntimeException) {
                    return OneForOneStrategy.resume();
                } else {
                    return OneForOneStrategy.restart();
                }
            }
        });
    }

}