package my.hdfs.mediator.camel.config;

import my.hdfs.mediator.camel.CookieSync;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.dataformat.csv.CsvDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.commons.csv.QuoteMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CamelConfiguration {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private CamelContext context;

    private String brokerList;
    private String zookeeperConnect;
    private String topic;
    private Integer consumersCount;

    private String serializerClass = "kafka.serializer.StringEncoder";
    private String producerType = "async";
    private String groupId = "testGroup";
    private Integer consumerStreams = 1;
    private Integer fetchMessageMaxBytes = 1024 * 1024;

    private Integer batchNumMessages = 1000;
    private Integer queueBufferingMaxMs = 5000;
    private Integer queueBufferingMaxMessages = 3_000_000;
    private Integer queueEnqueueTimeoutMs = 0;
    private Integer messageSendMaxRetries = 1;
    private Integer sendBufferBytes = 64 * 1024;

    private String splitStrategy = "IDLE:5000,BYTES:1048576";
    private String nameNodeHost = "192.168.56.104";
    private Integer nameNodePort = 8020;
    private String baseDir = "/user/botscanner/dev";
    private String overwrite = "false";
    private Integer replication = 1;
    private String connectOnStartup = "false";

    public CamelConfiguration(
            CamelContext context,
            String brokerList,
            String zookeeperConnect,
            String topic,
            Integer consumersCount
    ) {
        this.context = context;
        this.brokerList = brokerList;
        this.zookeeperConnect = zookeeperConnect;
        this.topic = topic;
        this.consumersCount = consumersCount;
    }

    public void init() throws Exception {
        context.addRoutes(createRoutes());
    }

    public RouteBuilder createRoutes() {
        logger.info("Creating routes");

        CsvDataFormat csv = new CsvDataFormat();
        csv.setDelimiter(',');
        csv.setHeaderDisabled(true);
        csv.setAllowMissingColumnNames(true);
        csv.setQuoteMode(QuoteMode.MINIMAL);

        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:cookieSyncRest")
                        .setHeader(KafkaConstants.PARTITION_KEY).simple("${header.breadcrumbId}")
//                        .to("log:my.hdfs.mediator.camel.config.kafka.producer?level=INFO")
                        .to(createKafkaProducerEndpoint());

                from(createKafkaConsumerEndpoint())
//                        .to("log:my.hdfs.mediator.camel.config.kafka.consumer?level=INFO")
                        .unmarshal().json(JsonLibrary.Jackson, CookieSync.class)
                        .setHeader("timestamp").simple("${body.timestamp}")
//                        .to("log:my.hdfs.mediator.camel.config.kafka.consumer?level=INFO")
                        .setHeader("partition").simple("${date:in.header.timestamp:yyyy-MM-dd}")
                        .marshal(csv)
                        .recipientList(simple(createHdfsProducerEndpoint()), "|")
                        .to("metrics:meter:hdfs-mediator.cookiesync.hdfs-upload.rate");
            }

            private String createHdfsProducerEndpoint() {
                return String.format(
                        "hdfs2://%s:%d%s/${header.partition}?splitStrategy=%s&overwrite=%s&replication=%s&connectOnStartup=%s",
                        nameNodeHost, nameNodePort, baseDir, splitStrategy, overwrite, replication, connectOnStartup
                );
            }

            private String createKafkaConsumerEndpoint() {
                return String.format(
                        "kafka:%s?zookeeperConnect=%s&topic=%s&groupId=%s&consumersCount=%s&consumerStreams=%s" +
                        "&fetchMessageMaxBytes=%s",
                        brokerList, zookeeperConnect, topic, groupId, consumersCount, consumerStreams,
                        fetchMessageMaxBytes
                );
            }

            private String createKafkaProducerEndpoint() {
                return String.format(
                        "kafka:%s?zookeeperConnect=%s&topic=%s&producerType=%s&serializerClass=%s" +
                        "&queueBufferingMaxMs=%s&queueBufferingMaxMessages=%s&queueEnqueueTimeoutMs=%s&batchNumMessages=%s" +
                        "&messageSendMaxRetries=%s&sendBufferBytes=%s",
                        brokerList, zookeeperConnect, topic, producerType, serializerClass,
                        queueBufferingMaxMs, queueBufferingMaxMessages, queueEnqueueTimeoutMs, batchNumMessages,
                        messageSendMaxRetries, sendBufferBytes
                );
            }
        };
    }
}
