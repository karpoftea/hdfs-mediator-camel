package my.hdfs.mediator.camel.config;

import com.google.common.collect.ImmutableMap;
import my.hdfs.mediator.camel.CookieSync;
import org.apache.camel.Converter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Converter
public class CookieSyncConverter {
    @Converter
    public List<?> toList(CookieSync cookieSync) {
        return Collections.singletonList(cookieSync);
    }

    @Converter
    public Map<String, ?> toMap(CookieSync cookieSync) {
        return ImmutableMap.of(
                "machineId", cookieSync.getMachineId(),
                "dspId", cookieSync.getDspId(),
                "uuid", cookieSync.getUuid(),
                "timestamp", cookieSync.getTimestamp().getTime()
        );
    }
}
