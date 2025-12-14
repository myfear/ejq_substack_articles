package com.example;

import com.example.domain.Customer;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class CustomerResourceTest {

    @BeforeEach
    void setUp() {
        // Clean up any existing customers before each test
        given()
            .when().get("/customers")
            .then()
            .extract()
            .body()
            .jsonPath()
            .getList("id", Long.class)
            .forEach(id -> given()
                .when().delete("/customers/" + id)
                .then()
                .statusCode(204));
    }

    @Test
    void testGetAllCustomers_WhenEmpty_ShouldReturnEmptyList() {
        given()
            .when().get("/customers")
            .then()
            .statusCode(200)
            .body("size()", is(0));
    }

    @Test
    void testGetAllCustomers_WhenCustomersExist_ShouldReturnAllCustomers() {
        // Create customers
        createCustomer("John Doe", "john@example.com");
        createCustomer("Jane Smith", "jane@example.com");

        given()
            .when().get("/customers")
            .then()
            .statusCode(200)
            .body("size()", is(2))
            .body("name", hasItems("John Doe", "Jane Smith"))
            .body("email", hasItems("john@example.com", "jane@example.com"));
    }

    @Test
    void testGetCustomer_WhenCustomerExists_ShouldReturnCustomer() {
        Customer customer = createCustomer("John Doe", "john@example.com");
        Long customerId = customer.getId();

        given()
            .when().get("/customers/" + customerId)
            .then()
            .statusCode(200)
            .body("id", is(customerId.intValue()))
            .body("name", is("John Doe"))
            .body("email", is("john@example.com"));
    }

    @Test
    void testGetCustomer_WhenCustomerDoesNotExist_ShouldReturn404() {
        given()
            .when().get("/customers/999")
            .then()
            .statusCode(404);
    }

    @Test
    void testCreateCustomer_WithValidData_ShouldReturn201() {
        String customerJson = """
            {
                "name": "John Doe",
                "email": "john@example.com"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(customerJson)
            .when().post("/customers")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", is("John Doe"))
            .body("email", is("john@example.com"));
    }

    @Test
    void testCreateCustomer_WithMissingName_ShouldReturn400() {
        String customerJson = """
            {
                "email": "john@example.com"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(customerJson)
            .when().post("/customers")
            .then()
            .statusCode(400)
            .body(is("Customer name is required"));
    }

    @Test
    void testCreateCustomer_WithBlankName_ShouldReturn400() {
        String customerJson = """
            {
                "name": "   ",
                "email": "john@example.com"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(customerJson)
            .when().post("/customers")
            .then()
            .statusCode(400)
            .body(is("Customer name is required"));
    }

    @Test
    void testCreateCustomer_WithMissingEmail_ShouldReturn400() {
        String customerJson = """
            {
                "name": "John Doe"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(customerJson)
            .when().post("/customers")
            .then()
            .statusCode(400)
            .body(is("Valid email is required"));
    }

    @Test
    void testCreateCustomer_WithInvalidEmail_ShouldReturn400() {
        String customerJson = """
            {
                "name": "John Doe",
                "email": "invalid-email"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(customerJson)
            .when().post("/customers")
            .then()
            .statusCode(400)
            .body(is("Valid email is required"));
    }

    @Test
    void testDeleteCustomer_WhenCustomerExists_ShouldReturn204() {
        Customer customer = createCustomer("John Doe", "john@example.com");
        Long customerId = customer.getId();

        given()
            .when().delete("/customers/" + customerId)
            .then()
            .statusCode(204);

        // Verify customer is deleted
        given()
            .when().get("/customers/" + customerId)
            .then()
            .statusCode(404);
    }

    @Test
    void testDeleteCustomer_WhenCustomerDoesNotExist_ShouldReturn204() {
        // Delete should still return 204 even if customer doesn't exist
        given()
            .when().delete("/customers/999")
            .then()
            .statusCode(204);
    }

    @Test
    void testCreateAndRetrieveCustomer_ShouldPersistCorrectly() {
        // Create a customer
        Customer created = createCustomer("John Doe", "john@example.com");
        Long customerId = created.getId();

        // Retrieve the customer
        given()
            .when().get("/customers/" + customerId)
            .then()
            .statusCode(200)
            .body("id", is(customerId.intValue()))
            .body("name", is("John Doe"))
            .body("email", is("john@example.com"));
    }

    // Helper method to create a customer and return it
    private Customer createCustomer(String name, String email) {
        String customerJson = String.format("""
            {
                "name": "%s",
                "email": "%s"
            }
            """, name, email);

        return given()
            .contentType(ContentType.JSON)
            .body(customerJson)
            .when().post("/customers")
            .then()
            .statusCode(201)
            .extract()
            .as(Customer.class);
    }
}