package tests;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.configuration.ProjectName;
import com.palantir.docker.compose.connection.DockerPort;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class ParkingSpotRestServiceIntegrationTest {
    private static final String PARKING_LOT_SERVICE_NAME = "parkinglotservice";
    private static final int PARKING_LOT_SERVICE_PORT = 5555;
    private static String parkingLotEndpoint;

    @ClassRule
    public static DockerComposeRule parkingSpotDockerComposition = DockerComposeRule.builder()
            .file("compositions/parking-lot-intagration-test-compose.yml")
//            .saveLogsTo("build/dockerLogs/dockerCompositionTest")
            .projectName(ProjectName.random())
            .waitingForService(
                    PARKING_LOT_SERVICE_NAME, HealthChecks.toRespondOverHttp(
                            PARKING_LOT_SERVICE_PORT, (p) -> p.inFormat("http://$HOST:$EXTERNAL_PORT/parking-lot-service/api/parking-lot")))
            .build();

    @BeforeClass
    public static void initialize() {
        DockerPort parkingLotPort = parkingSpotDockerComposition.containers()
                .container(PARKING_LOT_SERVICE_NAME)
                .port(PARKING_LOT_SERVICE_PORT);

        parkingLotEndpoint = String.format("http://%s:%s/parking-lot-service/api/parking-lot", parkingLotPort.getIp(), parkingLotPort.getExternalPort());
    }

    @Test
    public void checkParkingLotServiceEndpoint() throws Exception {
        HttpResponse<String> response = Unirest.get(parkingLotEndpoint).asString();

        System.out.println(response.getBody());
        System.out.println(response.getStatus());

        Assert.assertEquals(200, response.getStatus());
    }

}
