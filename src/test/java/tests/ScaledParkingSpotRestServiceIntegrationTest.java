package tests;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.configuration.ProjectName;
import com.palantir.docker.compose.connection.DockerPort;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static junit.framework.TestCase.assertEquals;

@Category(IntegrationTest.class)
public class ScaledParkingSpotRestServiceIntegrationTest {
    private static final String PARKING_LOT_SERVICE_NAME = "parkinglotservice";
    private static final int PARKING_LOT_SERVICE_PORT = 5555;

    private static final String EUREKA_SERVICE_NAME = "eureka";
    private static final int EUREKA_SERVICE_PORT = 8761;
    private static String eurekaEndpoint;

    @ClassRule
    public static DockerComposeRule parkingSpotDockerComposition = DockerComposeRule.builder()
            .file("compositions/scaled-parking-lot-intagration-test-compose.yml")
//            .saveLogsTo("build/dockerLogs/dockerCompositionTest")
            .projectName(ProjectName.random())
            .waitingForService(
                    PARKING_LOT_SERVICE_NAME + "_1", HealthChecks.toRespondOverHttp(
                            PARKING_LOT_SERVICE_PORT, (p) -> p.inFormat("http://$HOST:$EXTERNAL_PORT")))
            .waitingForService(
                    PARKING_LOT_SERVICE_NAME + "_2", HealthChecks.toRespondOverHttp(
                            PARKING_LOT_SERVICE_PORT, (p) -> p.inFormat("http://$HOST:$EXTERNAL_PORT")))
            .build();

    @BeforeClass
    public static void initialize() {
        DockerPort eurekaPort = parkingSpotDockerComposition.containers()
                .container(EUREKA_SERVICE_NAME)
                .port(EUREKA_SERVICE_PORT);

        eurekaEndpoint = String.format("http://%s:%s/eureka/apps/PARKING-LOT-SERVICE", eurekaPort.getIp(), eurekaPort.getExternalPort());
    }

    @Test
    public void checkParkingLotServiceEndpoint() throws Exception {
        HttpResponse<String> response = Unirest.get(eurekaEndpoint).asString();

        System.out.println(response.getBody());
        System.out.println(response.getStatus());

        assertEquals(2, StringUtils.countMatches(response.getBody(), "<instanceId>"));
    }

}
