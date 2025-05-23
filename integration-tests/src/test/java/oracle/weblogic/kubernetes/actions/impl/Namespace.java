// Copyright (c) 2020, 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package oracle.weblogic.kubernetes.actions.impl;

import java.util.List;
import java.util.Map;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1NamespaceList;
import oracle.weblogic.kubernetes.actions.impl.primitive.Kubernetes;

import static oracle.weblogic.kubernetes.utils.ThreadSafeLogger.getLogger;

public class Namespace extends UniqueName {
  private String name;

  public Namespace name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Generates a "unique" name by choosing a random name from
   * 26^6 possible combinations.
   *
   * @return name
   */
  public static String uniqueName() {
    return uniqueName("ns-");
  }

  public boolean create() throws ApiException {
    return Kubernetes.createNamespace(name);
  }

  /**
   * Create a Kubernetes namespace.
   *
   * @param name V1Namespace object containing namespace configuration data
   * @return true on success, false otherwise
   * @throws ApiException if Kubernetes Client API request fails
   */
  public static boolean create(V1Namespace name) throws ApiException {
    return Kubernetes.createNamespace(name);
  }

  /**
   * List of namespaces in Kubernetes cluster.
   *
   * @return List of names of all namespaces in Kubernetes cluster
   * @throws ApiException if Kubernetes client API call fails
   */
  public static List<String> listNamespaces() throws ApiException {
    return Kubernetes.listNamespaces();
  }

  /**
   * Delete a Kubernetes namespace.
   *
   * @param namespace name of namespace
   * @return true if successful, false otherwise
   */
  public static boolean delete(String namespace) {
    return Kubernetes.deleteNamespace(namespace);
  }

  public static boolean exists(String name) throws ApiException {
    return Kubernetes.listNamespaces().contains(name);
  }

  /**
   * Add labels to a namespace.
   *
   * @param name name of the namespace
   * @param labels map of labels to add to the namespace
   * @throws ApiException when adding labels to namespace fails
   */
  public static void addLabelsToNamespace(String name, Map<String, String> labels)
      throws ApiException {
    boolean found = false;
    V1NamespaceList namespaces = Kubernetes.listNamespacesAsObjects();
    if (!namespaces.getItems().isEmpty()) {
      for (var ns : namespaces.getItems()) {
        if (name.equals(ns.getMetadata().getName())) {
          ns.metadata(ns.getMetadata().labels(labels));
          Kubernetes.replaceNamespace(ns);
          found = true;
        }
      }
    }
    if (!found) {
      getLogger().severe("Namespace {0} not found or failed to add labels", name);
    }
  }
  
  /**
   * Add labels to a namespace.
   *
   * @param name name of the namespace
   * @param labels map of labels to add to the namespace
   * @param result to return the result
   * @return if replaced
   */
  public static boolean addLabelsToNamespace(String name, Map<String, String> labels, boolean result) {
    boolean success = false;
    try {
      V1NamespaceList namespaces = Kubernetes.listNamespacesAsObjects();
      if (!namespaces.getItems().isEmpty()) {
        for (var ns : namespaces.getItems()) {
          if (name.equals(ns.getMetadata().getName())) {
            ns.metadata(ns.getMetadata().labels(labels));
            Kubernetes.replaceNamespace(ns);
            return true;
          }
        }
        getLogger().severe("Namespace {0} not found or failed to add labels", name);
      }
    } catch (ApiException ex) {
      return success;
    }
    return success;
  }
  
}
