/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tooling.runtime.tooling;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.progress.ProgressIndicator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.mule.maven.client.api.model.MavenConfiguration;
import org.mule.tooling.runtime.process.controller.MuleProcessController;
import org.mule.tooling.runtime.util.MavenConfigurationUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.tuple.ImmutablePair.of;

/**
 * Controller to start/stop Mule Runtime in order to allow testing Tooling client operations.
 *
 * @since 1.0
 */
public class MuleStandaloneController {


  private static final String REST_AGENT_TRANSPORT_PORT_SYS_PROP = "rest.agent.transport.port";
  private static final String MULESOFT_PUBLIC_REPOSITORY = "https://repository.mulesoft.org/nexus/content/repositories/public/";
  private static final String MULESOFT_PRIVATE_REPOSITORY = "https://repository.mulesoft.org/nexus/content/repositories/private/";


  private MuleProcessController muleProcessController;
  private AtomicBoolean initialized = new AtomicBoolean(false);
  private volatile boolean initializing = false;
  private MuleRuntimeStatusChecker statusChecker;

  public MuleStandaloneController(MuleProcessController muleProcessController, MuleRuntimeStatusChecker statusChecker) {
    this.statusChecker = statusChecker;
    requireNonNull(muleProcessController, "configuration cannot be null");

    this.muleProcessController = muleProcessController;
  }

  public MuleRuntimeStatusChecker getChecker() {
    return statusChecker;
  }

  public boolean isRunning() {
    return muleProcessController.isRunning();
  }

  private static String[] createArgs(int agentPort, MavenConfiguration mavenConfiguration) {
    List<ImmutablePair<String, String>> result = newArrayList(of(REST_AGENT_TRANSPORT_PORT_SYS_PROP, valueOf(
        agentPort)),
        of("muleRuntimeConfig.maven.repositoryLocation",
            mavenConfiguration.getLocalMavenRepositoryLocation().getAbsolutePath()),
        of("muleRuntimeConfig.maven.repositories.mulesoft-public.url",
            MULESOFT_PUBLIC_REPOSITORY),
        of("muleRuntimeConfig.maven.repositories.mulesoft-private.url",
            MULESOFT_PRIVATE_REPOSITORY),
        of("mule.testingMode", "true"),
        of("muleRuntimeConfig.maven.userSettingsLocation",
            mavenConfiguration.getUserSettingsLocation().map(File::getAbsolutePath).orElse(""))
    );


    result.add(of("debug", ""));
    for (Map.Entry<Object, Object> sysPropEntry: System.getProperties().entrySet()) {
      final String key = (String) sysPropEntry.getKey();
      final String value = (String) sysPropEntry.getValue();
      if (key.startsWith("-M")) {
        result.add(of(key.replace("-M-D", ""), value));
      }
    }
    return result.stream()
        .map(pair -> {
          if (pair.getKey().equals("debug")) {
            return "-debug";
          } else {
            return format("-M-D%s=%s", pair.getKey(), pair.getValue());
          }
        }).toArray(String[]::new);
  }

  public boolean isInitializing() {
    return initializing;
  }

  public boolean start(ProgressIndicator progressIndicator) {
    progressIndicator.setText("Starting New Instance Of  Mule Runtime");
    if (initialized.compareAndSet(false, true)) {

      try {
        initializing = true;
        progressIndicator.setText2("Starting Mule Runtime With Agent Port " + statusChecker.getAgentPort());
        List<String> parameters = new LinkedList<>(asList(createArgs(statusChecker.getAgentPort(), MavenConfigurationUtils.createMavenConfiguration())));
        parameters.removeIf(StringUtils::isBlank);
        muleProcessController.start(parameters.toArray(new String[0]));
        progressIndicator.setText2("Mule Runtime Process Starting. Waiting for agent to connect at port " + statusChecker.getAgentPort());
        statusChecker.waitUntilIsRunning(progressIndicator);
        progressIndicator.setText2("Runtime Started Successfully.");
      } catch (Exception e) {
        Notifications.Bus.notify(new Notification("Mule Runtime", "Unable to start mule runtime ", "Unable to start mule runtime . Reason: \n" + e.getMessage(), NotificationType.ERROR));
        stop(progressIndicator);
        return false;
      } finally {
        initializing = false;
      }
    } else {
      progressIndicator.setText2("Runtime Already Started.");
    }
    progressIndicator.setText("Mule Runtime Started");
    return statusChecker.isRunning();
  }


  public void stop(ProgressIndicator progressIndicator) {
    progressIndicator.setText("Stopping Mule Runtime");
    if (initialized.compareAndSet(true, false)) {
      try {
        progressIndicator.setText2("Stopping Mule Runtime ...");
        if (muleProcessController != null && muleProcessController.isRunning()) {
          muleProcessController.stop();
        }
        progressIndicator.setText2("Mule Runtime Stopped");
        initialized.getAndSet(false);
      } catch (Exception e) {
        Notifications.Bus.notify(new Notification("Mule Runtime", "Unable to stop mule runtime ", "Unable to stop mule runtime . Reason: \n" + e.getMessage(), NotificationType.ERROR));
        e.printStackTrace();
      }
    }
    progressIndicator.setText("Mule Runtime Stopped");
  }


}
