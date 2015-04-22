package my.hdfs.mediator.camel;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.List;

@RestController
public class CookieSyncController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Produce(uri = "direct:cookieSyncRest")
    private ProducerTemplate template;

    @Autowired
    private MetricRegistry registry;

    private final ObjectMapper mapper = new ObjectMapper();

    @RequestMapping(name = "/cookiesync", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON)
    public void upload(@RequestBody List<CookieSync> cookieSync) {
        logger.trace("Uploading cookieSync:{}", cookieSync);
        for (CookieSync sync : cookieSync) {
            try {
                if (sync.getTimestamp() == null) {
                    sync.setTimestamp(new Date());
                }

                registry.meter("hdfs-mediator.cookiesync.upload.rate").mark();
                template.sendBody(mapper.writeValueAsString(sync));
            } catch (JsonProcessingException e) {
                logger.error("Error serializing object:{}", sync, e);
            }
        }
        logger.trace("CookieSync uploaded!");
    }
}
