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
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

@Category(IntegrationTest.class)
public class ExcelExportRestServiceIntegrationTest {
    private static final String EXCEL_EXPORT_SERVICE_NAME = "excelexportservice";
    private static final int EXCEL_EXPORT_SERVICE_PORT = 4444;
    private static String excelExportEndpoint;

    @ClassRule
    public static DockerComposeRule excelExportDockerComposition = DockerComposeRule.builder()
            .file("compositions/excel-export-intagration-test-compose.yml")
//            .saveLogsTo("build/dockerLogs/dockerCompositionTest")
            .projectName(ProjectName.random())
            .waitingForService(
                    EXCEL_EXPORT_SERVICE_NAME, HealthChecks.toRespondOverHttp(
                            EXCEL_EXPORT_SERVICE_PORT, (p) -> p.inFormat("http://$HOST:$EXTERNAL_PORT/excel-export-service/api/parking-lot")))
            .build();

    @BeforeClass
    public static void initialize() {
        DockerPort excelExportPort = excelExportDockerComposition.containers()
                .container(EXCEL_EXPORT_SERVICE_NAME)
                .port(EXCEL_EXPORT_SERVICE_PORT);

        excelExportEndpoint = String.format("http://%s:%s/excel-export-service/api/parking-lot", excelExportPort.getIp(), excelExportPort.getExternalPort());
    }

    @Test
    public void checkExcelExportServiceEndpoint() throws Exception {
        HttpResponse<String> response = Unirest.get(excelExportEndpoint).asString();

        HSSFWorkbook workbook = new HSSFWorkbook(response.getRawBody());

        Assert.assertEquals(2, workbook.getSheet("Parking lots overview").getPhysicalNumberOfRows());
    }

}
