package jenkins.plugins.logstash.configuration;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.cloudbees.syslog.MessageFormat;

import hudson.Extension;
import jenkins.plugins.logstash.persistence.LogstashIndexerDao.SyslogProtocol;
import jenkins.plugins.logstash.persistence.SyslogDao;

public class Syslog extends LogstashIndexer<SyslogDao> {

	private SyslogData data;

	@DataBoundConstructor
	public Syslog()
	{
	}

	public MessageFormat getMessageFormat()
	{
		return data.getMessageFormat();
	}

	public SyslogProtocol getSyslogProtocol()
	{
		return data.getSyslogProtocol();
	}

	public void setMessageFormat(MessageFormat messageFormat)
	{
		data.setMessageFormat(messageFormat);
	}

	public void setSyslogProtocol(SyslogProtocol syslogProtocol)
	{
		data.setSyslogProtocol(syslogProtocol);
	}

	@Override
	public SyslogDao createIndexerInstance()
	{
		SyslogDao syslogDao = new SyslogDao(getData().getHost(), getData().getPort());
		syslogDao.setMessageFormat(getMessageFormat());
		return syslogDao;
	}

	static class SyslogData extends LogstashIndexerData {

		private MessageFormat messageFormat;
		private SyslogProtocol syslogProtocol;

		public MessageFormat getMessageFormat()
		{
			return messageFormat;
		}

		public SyslogProtocol getSyslogProtocol()
		{
			return syslogProtocol;
		}
		
		public void setMessageFormat(MessageFormat messageFormat)
		{
			this.messageFormat = messageFormat;
		}
		
		public void setSyslogProtocol(SyslogProtocol syslogProtocol)
		{
			this.syslogProtocol = syslogProtocol;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((messageFormat == null) ? 0 : messageFormat.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			SyslogData other = (SyslogData) obj;
			if (messageFormat != other.messageFormat)
				return false;
			return true;
		}	
	}

	@Extension
	public static class SyslogDescriptor extends LogstashIndexerDescriptor
	{
		@Override
		public String getDisplayName()
		{
			return "Syslog";
		}

		@Override
		public int getDefaultPort()
		{
			return 519;
		}
	}

	@Override
	protected LogstashIndexerData getData()
	{
		return data;
	}
}
