// Copyright (c) 2021, 2023, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package oracle.kubernetes.weblogic.domain.model;

import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import jakarta.validation.constraints.NotNull;
import oracle.kubernetes.json.Default;
import oracle.kubernetes.json.Description;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.yaml.snakeyaml.Yaml;

/**
 * The configuration to be applied to the WebLogic Monitoring Exporter sidecars in the domain.
 * Note that the operator does not access any of the fields in the configuration; this definition
 * exists simply to define the CRD and to control serialization.
 */
@JsonAdapter(MonitoringExporterConfiguration.ConfigurationTypeAdapter.class)
public class MonitoringExporterConfiguration {
  @Description("If true, metrics names will be constructed with underscores between words (snake case). "
             + "By default, metrics names will be constructed with capital letters separating words "
             + "(camel case).")
  private Boolean metricsNameSnakeCase;

  @Description("If true, metrics qualifiers will include the operator domain. Defaults to false.")
  @Default(boolDefault = false)
  private Boolean domainQualifier;

  private ExporterQuery[] queries;

  public static MonitoringExporterConfiguration createFromYaml(String yaml) {
    return new Gson().fromJson(JsonParser.parseString(convertToJson(yaml)), MonitoringExporterConfiguration.class);
  }

  public static String convertToJson(String yaml) {
    final Object loadedYaml = new Yaml().load(yaml);
    return new Gson().toJson(loadedYaml, LinkedHashMap.class);
  }

  /**
   * Returns the configuration in a form that can be send to the exporter sidecar.
   */
  public String asJsonString() {
    return toJson();
  }

  /**
   * Returns true if the specified YAML string matches this configuration, ignoring unknown fields
   * and field ordering. Note that ordering of elements in arrays is considered significant for this comparison.
   * @param yaml a monitoring exporter configuration to compare to this object.
   */
  public boolean matchesYaml(String yaml) {
    final String thisJson = toJson();
    final String otherJson = createFromYaml(yaml).toJson();
    return JsonParser.parseString(thisJson).equals(JsonParser.parseString(otherJson));
  }

  // Returns a JSON representation of this configuration.
  private String toJson() {
    return new Gson().toJson(this);
  }

  // A query which defines a set of values and sub-queries to select metrics to export.
  static class ExporterQuery extends HashMap<String,ExporterQuery> {
    @Serial
    private static final long serialVersionUID  = 1L;

    @Description("A filter for subtypes. "
               + "If specified, only those objects whose type attribute matches will be collected.")
    private String type;

    @Description("A name prefix to use for all the metrics gathered from the current level.")
    private String prefix;

    @Description("The name of the attribute to use as a key for qualifiers in the output.")
    private String key;

    @Description("The name to use for the key in the qualifier; defaults to the name of the attribute.")
    private String keyName;

    @Description("The attributes for which metrics are to be output. If not specified and a prefix is defined, "
               + "all values on the MBean will be selected.")
    private String[] values;

    private Map<String,String[]> stringValues;
  }

  // This class controls serialization of the exporter configuration.
  static class ConfigurationTypeAdapter extends TypeAdapter<MonitoringExporterConfiguration> {

    @Override
    public void write(JsonWriter out, MonitoringExporterConfiguration src) throws IOException {
      out.beginObject();
      writeOptionalBooleanField(out, "metricsNameSnakeCase", src.metricsNameSnakeCase);
      writeOptionalBooleanField(out, "domainQualifier", src.domainQualifier);

      writeOptionalQueryArray(out, src.queries);

      out.endObject();
    }

    private void writeOptionalBooleanField(JsonWriter out, String name, @Nullable Boolean value) throws IOException {
      if (value != null) {
        out.name(name).value(value);
      }
    }

    private void writeOptionalQueryArray(JsonWriter out, ExporterQuery[] queries) throws IOException {
      if (queries != null && queries.length > 0) {
        out.name("queries");
        out.beginArray();
        for (ExporterQuery query : queries) {
          writeQuery(out, query);
        }
        out.endArray();
      }
    }

    private void writeQuery(JsonWriter out, ExporterQuery src) throws IOException {
      out.beginObject();
      writeOptionalStringField(out, "key", src.key);
      writeOptionalStringField(out, "keyName", src.keyName);
      writeOptionalStringField(out, "type", src.type);
      writeOptionalStringField(out, "prefix", src.prefix);
      writeOptionalValueArray(out, src.values);
      writeOptionalStringValues(out, src.stringValues);

      for (Map.Entry<String, ExporterQuery> entry : src.entrySet()) {
        out.name(entry.getKey());
        writeQuery(out, entry.getValue());
      }

      out.endObject();
    }

    private void writeOptionalStringField(JsonWriter out, String name, @Nullable String value) throws IOException {
      if (value != null) {
        out.name(name).value(value);
      }
    }

    private void writeOptionalValueArray(JsonWriter out, @Nullable String[] values) throws IOException {
      if (values != null && values.length > 0) {
        writeArray(out, "values", values);
      }
    }

    private static void writeArray(JsonWriter out, String name, @NotNull String [] values) throws IOException {
      out.name(name);
      out.beginArray();
      for (String value : values) {
        out.value(value);
      }
      out.endArray();
    }

    private void writeOptionalStringValues(JsonWriter out, Map<String, String[]> stringValues) throws IOException {
      if (stringValues != null && !stringValues.isEmpty()) {
        out.name("stringValues");
        out.beginObject();
        for (String key: stringValues.keySet()) {
          writeArray(out, key, stringValues.get(key));
        }
        out.endObject();
      }
    }

    @Override
    public MonitoringExporterConfiguration read(JsonReader in) throws IOException {
      MonitoringExporterConfiguration configuration = new MonitoringExporterConfiguration();
      in.beginObject();
      while (in.hasNext()) {
        String name = in.nextName();
        switch (name) {
          case "metricsNameSnakeCase":
            configuration.metricsNameSnakeCase = in.nextBoolean();
            break;
          case "domainQualifier":
            configuration.domainQualifier = in.nextBoolean();
            break;
          case "queries":
            configuration.queries = readQueryArray(in);
            break;
          default:
            in.skipValue();
        }
      }
      in.endObject();
      return configuration;
    }

    private ExporterQuery[] readQueryArray(JsonReader in) throws IOException {
      List<ExporterQuery> result = new ArrayList<>();
      in.beginArray();
      while (in.hasNext()) {
        result.add(readQuery(in));
      }
      in.endArray();
      return result.toArray(new ExporterQuery[0]);
    }

    private ExporterQuery readQuery(JsonReader in) throws IOException {
      ExporterQuery query = new ExporterQuery();
      in.beginObject();
      while (in.hasNext()) {
        String name = in.nextName();
        switch (name) {
          case "key" -> query.key = in.nextString();
          case "keyName" -> query.keyName = in.nextString();
          case "type" -> query.type = in.nextString();
          case "prefix" -> query.prefix = in.nextString();
          case "values" -> query.values = readArrayValue(in);
          case "stringValues" -> query.stringValues = readStringValues(in);
          default -> query.put(name, readQuery(in));
        }
      }
      in.endObject();
      return query;
    }

    private String[] readArrayValue(JsonReader in) throws IOException {
      List<String> result = new ArrayList<>();
      if (in.peek().equals(JsonToken.BEGIN_ARRAY)) {
        readStringArray(in, result);
      } else {
        result.add(in.nextString());
      }
      return result.toArray(new String[0]);
    }

    private void readStringArray(JsonReader in, List<String> result) throws IOException {
      in.beginArray();
      while (in.hasNext()) {
        result.add(in.nextString());
      }
      in.endArray();
    }

    private Map<String,String[]> readStringValues(JsonReader in) throws IOException {
      Map<String,String[]> result = new HashMap<>();
      in.beginObject();
      while (in.hasNext()) {
        String name = in.nextName();
        result.put(name, readArrayValue(in));
      }
      in.endObject();
      return result;
    }
  }

  @Override
  public boolean equals(Object o) {
    return (this == o)
        || ((o instanceof MonitoringExporterConfiguration monitoringExporterConfiguration)
        && equals(monitoringExporterConfiguration));
  }

  private boolean equals(MonitoringExporterConfiguration that) {
    return new EqualsBuilder()
          .append(metricsNameSnakeCase, that.metricsNameSnakeCase)
          .append(domainQualifier, that.domainQualifier)
          .append(queries, that.queries)
          .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
          .append(metricsNameSnakeCase)
          .append(domainQualifier)
          .append(queries)
          .toHashCode();
  }
}
