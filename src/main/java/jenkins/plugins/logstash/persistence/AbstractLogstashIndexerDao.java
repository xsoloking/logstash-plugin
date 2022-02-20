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

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

import net.sf.json.JSONObject;

/**
 * Abstract data access object for Logstash indexers.
 *
 * @author Rusty Gerard
 * @since 1.0.0
 */
public abstract class AbstractLogstashIndexerDao implements LogstashIndexerDao, Serializable {

  private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

  @Override
  public JSONObject buildPayload(BuildData buildData, String jenkinsUrl, List<String> logLines) {
    JSONObject payload = new JSONObject();
    String log = String.join("/n", logLines);
    String timestamp = sdf.format(new Timestamp(System.currentTimeMillis()));
    String msg = timestamp + " [INFO] " + log;
    int logType = 2;
    if(log.contains("[INFO]")) {
      msg = timestamp + " " + log;
    } else if(log.contains("[ERROR]") || log.contains("ERROR")) {
      logType = 1;
      msg = timestamp + " " + log;
    } else if (log.contains("WARNING")) {
      logType = 3;
      msg = timestamp + " " + log;
    }
    try {
      payload.put("flowId", Long.parseLong(buildData.getBuildVariables().get("flowId")));
    } catch (NumberFormatException e) {
      payload.put("flowId", buildData.getBuildVariables().get("flowId"));
    }
    try {
      payload.put("flowInstanceId", Long.parseLong(buildData.getBuildVariables().get("flowInstanceId")));
    } catch (NumberFormatException e) {
      payload.put("flowInstanceId", buildData.getBuildVariables().get("flowInstanceId"));
    }
    try {
      payload.put("nodeInstanceId", Long.parseLong(buildData.getBuildVariables().get("nodeInstanceId")));
    } catch (NumberFormatException e) {
      payload.put("nodeInstanceId", buildData.getBuildVariables().get("nodeInstanceId"));
    }
    try {
      payload.put("taskInstanceId", Long.parseLong(buildData.getBuildVariables().get("taskInstanceId")));
    } catch (NumberFormatException e) {
      payload.put("taskInstanceId", buildData.getBuildVariables().get("taskInstanceId"));
    }
    try {
      payload.put("executeBatchId", Long.parseLong(buildData.getBuildVariables().get("executeBatchId")));
    }  catch (NumberFormatException e) {
      payload.put("executeBatchId", buildData.getBuildVariables().get("executeBatchId"));
    }

    payload.put("logContent", msg);
    payload.put("htmlLog", log.contains("http://") || log.contains("https://"));
    payload.put("logType", logType);
    return payload;
  }

}
