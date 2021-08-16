package jenkins.plugins.logstash.configuration;

import hudson.Extension;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.plugins.logstash.Messages;
import jenkins.plugins.logstash.persistence.MongoDao;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

public class MongoDb extends HostBasedLogstashIndexer<MongoDao>
{

  private String database;
  private String username;
  private Secret password;
  private String serviceUri;
  private String customization;
  private String collection;

  @DataBoundConstructor
  public MongoDb()
  {
  }

  public String getDatabase()
  {
    return database;
  }

  @DataBoundSetter
  public void setDatabase(String database)
  {
    this.database = database;
  }

  public String getServiceUri() { return serviceUri;}

  @DataBoundSetter
  public void setServiceUri(String serviceUri)
  {
    this.serviceUri = serviceUri;
  }

  public String getUsername()
  {
    return username;
  }

  @DataBoundSetter
  public void setUsername(String username)
  {
    this.username = username;
  }

  public Secret getPassword()
  {
    return password;
  }

  @DataBoundSetter
  public void setPassword(Secret password)
  {
    this.password = password;
  }

  public String getCustomization() {
    return customization;
  }

  @DataBoundSetter
  public void setCustomization(String customization) {
    this.customization = customization;
  }

  public String getCollection() {
    return collection;
  }

  @DataBoundSetter
  public void setCollection(String collection) {
    this.collection = collection;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    MongoDb other = (MongoDb) obj;
    if (!Secret.toString(password).equals(Secret.toString(other.getPassword())))
    {
      return false;
    }
    if (!StringUtils.equals(database, other.database))
    {
        return false;
    }
    if (!StringUtils.equals(username, other.username))
    {
        return false;
    }
    if (!StringUtils.equals(serviceUri, other.serviceUri))
    {
      return false;
    }
    if (!StringUtils.equals(customization, other.customization))
    {
      return false;
    }
    if (!StringUtils.equals(collection, other.collection))
    {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((database == null) ? 0 : database.hashCode());
    result = prime * result + ((username == null) ? 0 : username.hashCode());
    result = prime * result + ((serviceUri == null) ? 0 : serviceUri.hashCode());
    result = prime * result + ((customization == null) ? 0 : customization.hashCode());
    result = prime * result + ((collection == null) ? 0 : collection.hashCode());
    result = prime * result + Secret.toString(password).hashCode();
    return result;
  }

  @Override
  public MongoDao createIndexerInstance()
  {
    return new MongoDao(getHost(), getPort(), database, username, Secret.toString(password), serviceUri, collection, customization);
  }

  @Extension
  @Symbol("mongoDb")
  public static class MongoDbDescriptor extends LogstashIndexerDescriptor
  {
    @Override
    public String getDisplayName()
    {
      return "MongoDB";
    }

    @Override
    public int getDefaultPort()
    {
      return 27017;
    }

    public FormValidation doCheckDatabase(@QueryParameter("value") String value)
    {
      if (StringUtils.isBlank(value))
      {
        return FormValidation.error(Messages.ValueIsRequired());
      }
      return FormValidation.ok();
    }
  }
}
