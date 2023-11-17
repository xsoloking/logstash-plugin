/*
 * The MIT License
 *
 * Copyright 2014 Rusty Gerard
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package jenkins.plugins.logstash.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.bson.Document;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * MongoDB Data Access Object.
 *
 * @author Soloking
 * @since 1.0.0
 */
@SuppressFBWarnings(value="SE_NO_SERIALVERSIONID")
public class MongoDao extends HostBasedLogstashIndexerDao {

  private static final String DEFAULT_COLLECTION = "logstash";

  private transient MongoClient pool;

  private final String username;
  private final String password;
  private final String database;
  private final String serviceUri;
  private final String collection;
  private final String customization;

  public MongoDao(String host, int port, String database, String username, String password, String serviceUri, String collection, String customization) {
    this(null, host, port, database, username, password, serviceUri, collection, customization);
  }

  MongoDao(MongoClient factory, String host, int port, String database, String username, String password, String serviceUri, String collection, String customization) {
    super(host, port);
    this.database = database;
    this.username = username;
    this.password = password;
    this.serviceUri = serviceUri;
    this.collection = collection;
    this.customization = customization;

    // The MongoPool must be a singleton
    // We assume this is used as a singleton as well
    pool = factory;
  }

  public String getUsername() {
    return this.username;
  }

  public String getDatabase() {
    return this.database;
  }

  public String getPassword() {
    return this.password;
  }

  public String getServiceUri() {
    return this.serviceUri;
  }

  public String getCollection() {
    return this.collection;
  }

  public String getCustomization() {
    return this.customization;
  }

  private synchronized MongoClient getMongoClient() {
    if (pool == null) {
      MongoCredential credential = MongoCredential.createCredential(username, database, password.toCharArray());
      MongoClientSettings settings;
      if (serviceUri != null && !serviceUri.isEmpty()) {
        settings = MongoClientSettings.builder()
                .credential(credential)
                .applyConnectionString(new ConnectionString(serviceUri))
                .applyToConnectionPoolSettings(builder -> {
                  // default mongodb values: min=0 and max=100
                  builder.minSize(0).maxSize(200).maxConnectionIdleTime(30, TimeUnit.SECONDS);
                })
                .build();
      } else {
        settings = MongoClientSettings.builder()
                .credential(credential)
                .applyToClusterSettings(builder ->
                        builder.hosts(Collections.singletonList(new ServerAddress(getHost(), getPort()))))
                .applyToConnectionPoolSettings(builder -> {
                  // default mongodb values: min=0 and max=100
                  builder.minSize(0).maxSize(200).maxConnectionIdleTime(30, TimeUnit.SECONDS);
                })
                .build();
      }
      pool = MongoClients.create(settings);
    }
    return pool;
  }

  @SuppressWarnings("unchecked")
  private String getCollectionName(String data, String customization) {
    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> dMap = null;
    try {
      dMap = mapper.readValue(data, HashMap.class);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    String[] groups = customization.split(";");
    StringBuilder value = new StringBuilder();
    if (dMap != null && groups.length > 0) {
      for(String group: groups) {
        if (! group.isEmpty()) {
          Map<String, Object> tmp = new HashMap<>(dMap);
          String[] mCustomization = group.split("\\.");
            for (String key: mCustomization)  {
              try {
                tmp = (Map<String, Object>) tmp.get(key);
              } catch (ClassCastException  e)  {
                if (value.length() > 0) {
                  value.append("-");
                }
                value.append(tmp.get(key).toString());
              }
            }
        }
      }
    }
    if (value.length() <= 0) {
      return DEFAULT_COLLECTION;
    }
    return value.toString();
  }

  @Override
  public void push(String data) throws IOException {

    MongoDatabase mongoDatabase;
    try {
      mongoDatabase = getMongoClient().getDatabase(database);
    } catch (IllegalArgumentException e) {
      throw new IOException("Failed to get MongoDB database: " + database);
    }
    String collectionName = DEFAULT_COLLECTION;
    if (collection != null && !collection.isEmpty()) {
      collectionName = collection;
    }
    if (customization != null && !customization.isEmpty()) {
      collectionName = getCollectionName(data, customization);
    }

    try {
      MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collectionName);
      mongoCollection.insertOne(Document.parse(data));
    } catch (IllegalArgumentException e) {
      mongoDatabase.createCollection(collectionName);
      MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(collectionName);
      mongoCollection.insertOne(Document.parse(data));
    } catch (MongoInterruptedException e) {
      throw new IOException("Failed to insert new doc with exception: MongoInterruptedException " + e.getMessage());
    }
  }

}
