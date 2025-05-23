// Copyright (c) 2018, 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package oracle.kubernetes.operator.processing;

import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

import io.kubernetes.client.openapi.models.V1Affinity;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1EnvFromSource;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1HostAlias;
import io.kubernetes.client.openapi.models.V1LocalObjectReference;
import io.kubernetes.client.openapi.models.V1PodReadinessGate;
import io.kubernetes.client.openapi.models.V1PodSecurityContext;
import io.kubernetes.client.openapi.models.V1Probe;
import io.kubernetes.client.openapi.models.V1ResourceRequirements;
import io.kubernetes.client.openapi.models.V1SecurityContext;
import io.kubernetes.client.openapi.models.V1Toleration;
import io.kubernetes.client.openapi.models.V1TopologySpreadConstraint;
import io.kubernetes.client.openapi.models.V1Volume;
import io.kubernetes.client.openapi.models.V1VolumeMount;
import oracle.kubernetes.weblogic.domain.model.Shutdown;

public interface EffectiveServerSpec {

  String getImage();

  String getImagePullPolicy();

  /**
   * The secrets used to authenticate to an image repository when pulling an image.
   *
   * @return a list of objects containing the name of secrets. May be empty.
   */
  List<V1LocalObjectReference> getImagePullSecrets();

  /**
   * Returns the environment variables to be defined for this server.
   *
   * @return a list of environment variables
   */
  List<V1EnvVar> getEnvironmentVariables();

  /**
   * Desired startup state. Legal values are RUNNING or ADMIN.
   *
   * @return desired state
   */
  String getStateGoal();

  /**
   * Returns true if the specified server should be started, based on the current domain spec.
   *
   * @param currentReplicas the number of replicas already selected for the cluster.
   * @return whether to start the server
   */
  boolean shouldStart(int currentReplicas);

  /**
   * Returns true if the server is shutting down, or not configured to be started.
   *
   * @return whether the server is shutting down, or not configured to be started.
   */
  boolean isShuttingDown();

  /**
   * Returns the volume mounts to be defined for this server.
   *
   * @return a list of environment volume mounts
   */
  List<V1VolumeMount> getAdditionalVolumeMounts();

  /**
   * Returns the volumes to be defined for this server.
   *
   * @return a list of volumes
   */
  List<V1Volume> getAdditionalVolumes();

  /**
   * Returns a list of sources (config-map or secret) for environment variables to be set for this server.
   *
   * @return envFrom list of sources.
   */
  List<V1EnvFromSource> getEnvFrom();

  @Nonnull
  V1Probe getLivenessProbe();

  @Nonnull
  V1Probe getReadinessProbe();

  V1Probe getStartupProbe();

  @Nonnull
  Shutdown getShutdown();

  /**
   * Returns the labels applied to the pod.
   *
   * @return a map of labels
   */
  @Nonnull
  Map<String, String> getPodLabels();

  /**
   * Returns the annotations applied to the pod.
   *
   * @return a map of annotations
   */
  @Nonnull
  Map<String, String> getPodAnnotations();

  /**
   * Returns true if the per-server instance service should be created for this server instance even
   * if the pod for this server instance is not running.
   *
   * @return true, if the per-server instance service should be pre-created
   */
  Boolean isPrecreateServerService();

  /**
   * Returns the labels applied to the service.
   *
   * @return a map of labels
   */
  @Nonnull
  Map<String, String> getServiceLabels();

  /**
   * Returns the annotations applied to the service.
   *
   * @return a map of annotations
   */
  @Nonnull
  Map<String, String> getServiceAnnotations();

  @Nonnull
  List<V1Container> getInitContainers();

  @Nonnull
  List<V1Container> getContainers();

  Map<String, String> getNodeSelectors();

  V1Affinity getAffinity();

  List<V1TopologySpreadConstraint> getTopologySpreadConstraints();

  String getPriorityClassName();

  List<V1PodReadinessGate> getReadinessGates();

  String getRestartPolicy();

  String getRuntimeClassName();

  String getNodeName();

  String getServiceAccountName();

  Boolean getAutomountServiceAccountToken();

  String getSchedulerName();

  List<V1Toleration> getTolerations();

  List<V1HostAlias> getHostAliases();

  V1ResourceRequirements getResources();

  V1PodSecurityContext getPodSecurityContext();

  V1SecurityContext getContainerSecurityContext();

  String getDomainRestartVersion();

  String getClusterRestartVersion();

  String getServerRestartVersion();

  boolean alwaysStart();

  Long getMaximumReadyWaitTimeSeconds();

  Long getMaximumPendingWaitTimeSeconds();
}
