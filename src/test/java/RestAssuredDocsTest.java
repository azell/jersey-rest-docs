import java.lang.reflect.Method;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTestNg.ContainerPerClassTest;
import org.glassfish.jersey.test.TestProperties;

import org.springframework.restdocs.ManualRestDocumentation;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.equalTo;

import static org.springframework.restdocs.payload.PayloadDocumentation
  .fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation
  .requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation
  .responseFields;
import static org.springframework.restdocs.request.RequestDocumentation
  .parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation
  .pathParameters;
import static org.springframework.restdocs.restassured
  .RestAssuredRestDocumentation.document;
import static org.springframework.restdocs.restassured
  .RestAssuredRestDocumentation.documentationConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

import static com.jayway.restassured.RestAssured.given;

/**
 * Stack Overflow http://stackoverflow.com/q/35068860/2587435
 *
 * Running the test should produces the following snippets in target/generated-snippets/example-put:
 *
 * - curl-request.adoc
 * - http-request.adoc
 * - http-response.adoc
 * - path-parameters.adoc
 * - request-fields.adoc
 * - response-fields.adoc
 *
 */
@Test
public class RestAssuredDocsTest extends ContainerPerClassTest {
  private final ManualRestDocumentation restDocumentation =
    new ManualRestDocumentation("build/generated-snippets");
  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public ResourceConfig configure() {
    forceSet(TestProperties.CONTAINER_PORT, "0");

    return new ResourceConfig(TestResource.class).register(
        new LoggingFilter(Logger.getAnonymousLogger(),
                          true));
  }

  public void examplePut() throws Exception {
    TestBean bean = new TestBean(1, "a message");

    given().port(getPort())
           .filter(documentationConfiguration(restDocumentation))
           .filter(document("example-put",
                            requestFields(
                                fieldWithPath("id").description("The id"),
                                fieldWithPath("message").description(
                                    "The message")),
                            responseFields(
                                fieldWithPath("id").description("The id"),
                                fieldWithPath("message").description(
                                    "The message")),
                            pathParameters(
                                parameterWithName("id").description("The id"))))
           .contentType(APPLICATION_JSON)
           .accept(APPLICATION_JSON)
           .content(mapper.writeValueAsString(bean))
           .put("/test/{id}", "1")
           .then()
           .statusCode(200)
           .body("id", equalTo(1))
           .body("message", equalTo("a message"));

  }

  @AfterMethod
  public void tearDown() {
    restDocumentation.afterTest();
  }

  @BeforeMethod
  public void setUp(Method method) {
    restDocumentation.beforeTest(getClass(), method.getName());
  }

  public static class TestBean {
    public int    id;
    public String message;

    public TestBean() {}

    public TestBean(int id, String message) {
      this.id      = id;
      this.message = message;
    }
  }


  @Path("test")
  @Produces(APPLICATION_JSON)
  @Consumes(APPLICATION_JSON)
  public static class TestResource {

    @PUT
    @Path("{id}")
    public TestBean update(TestBean bean) {
      return bean;
    }
  }
}
