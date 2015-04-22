package my.hdfs.mediator.camel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CookieSync {

    @JsonProperty
    private String dspId;
    @JsonProperty
    private String uuid;
    @JsonProperty
    private String machineId;
    @JsonProperty
    private Date timestamp;

    public String getDspId() {
        return dspId;
    }

    public void setDspId(String dspId) {
        this.dspId = dspId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "CookieSync{" +
                "dspId='" + dspId + '\'' +
                ", uuid='" + uuid + '\'' +
                ", machineId='" + machineId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
