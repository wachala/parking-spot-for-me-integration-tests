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
public class EurekaIntegrationTest {
    private static final String EUREKA_SERVICE_NAME = "eureka";
    private static final int EUREKA_SERVICE_PORT = 8761;
    private static String eurekaEndpoint;

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
            .file("compositions/eureka-integration-test-compose.yml")
            .saveLogsTo("build/dockerLogs/dockerCompositionTest")
            .projectName(ProjectName.random())
            .waitingForService(EUREKA_SERVICE_NAME, HealthChecks.toRespondOverHttp(EUREKA_SERVICE_PORT, (p) -> p.inFormat("http://$HOST:$EXTERNAL_PORT")))
            .build();

    @BeforeClass
    public static void initialize() {
        DockerPort eureka = docker.containers()
                .container(EUREKA_SERVICE_NAME)
                .port(EUREKA_SERVICE_PORT);

        eurekaEndpoint = String.format("http://%s:%s", eureka.getIp(), eureka.getExternalPort());
    }

    @Test
    public void checkEurekaEndpoint() throws Exception {
        HttpResponse<String> response = Unirest.get(eurekaEndpoint).asString();
        System.out.println(response.getBody());
        System.out.println(response.getStatus());

        Assert.assertEquals(200, response.getStatus());
    }

}
