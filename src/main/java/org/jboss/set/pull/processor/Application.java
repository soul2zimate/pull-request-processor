/*
 * Copyright 2020 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.set.pull.processor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.quarkus.scheduler.Scheduled;

/**
 * @author wangc
 *
 */
@ApplicationScoped
public class Application {

    private static final Logger LOG = Logger.getLogger(Application.class);
    private static final Main main = new Main();

    // see application.properties
    @ConfigProperty(name = "streams")
    String streams;

    @ConfigProperty(name = "permitted")
    String permitted;

    @ConfigProperty(name = "file")
    String file;

    @ConfigProperty(name = "write")
    String write;

    @Scheduled(cron = "{cron.expr}")
    void cronJobWithExpressionInConfig() {
        LOG.info("Start Cron Job with expression configured in application.properties.");
        List<String> streamList = Arrays.asList(streams.split(" "));

        List<StreamDefinition> parsedStreams = streamList.stream().map(e -> new StreamDefinition(e))
                .collect(Collectors.toList());

        streamList = Arrays.asList(permitted.split(" "));

        List<StreamDefinition> writePermittedStreams = null;
        if (streamList != null) {
            writePermittedStreams = streamList.stream().map(e -> new StreamDefinition(e)).collect(Collectors.toList());
        }

        Boolean performWriteOperations = Boolean.valueOf(write);
        try {
            main.start(parsedStreams, writePermittedStreams, file, performWriteOperations);
        } catch (Exception e) {
            String errorMsg = "The schedule job can't start with arguments parsedStreams :  " + parsedStreams
                    + " writePermittedStreams : " + writePermittedStreams + " file: " + file + " performWriteOperations : "
                    + performWriteOperations;
            LOG.error(errorMsg, e);
        }
        LOG.info("Cron Job is completed");
    }
}