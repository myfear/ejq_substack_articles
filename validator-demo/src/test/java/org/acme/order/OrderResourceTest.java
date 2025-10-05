package org.acme.order;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
public class OrderResourceTest {

        // Helper methods for building JSON
        private String buildOrderJson(String customerName, String customerEmail,
                        String street, String city,
                        String productCode, int quantity) {
                return String.format("""
                                {
                                    "orderNumber": "ORD-001",
                                    "customer": {
                                        "name": "%s",
                                        "email": "%s"
                                    },
                                    "address": {
                                        "street": "%s",
                                        "city": "%s"
                                    },
                                    "items": [
                                        {
                                            "productCode": "%s",
                                            "quantity": %d
                                        }
                                    ]
                                }
                                """, customerName, customerEmail, street, city, productCode, quantity);
        }

        private String buildOrderJsonWithoutAddress(String customerName, String customerEmail,
                        String productCode, int quantity) {
                return String.format("""
                                {
                                    "orderNumber": "ORD-002",
                                    "customer": {
                                        "name": "%s",
                                        "email": "%s"
                                    },
                                    "items": [
                                        {
                                            "productCode": "%s",
                                            "quantity": %d
                                        }
                                    ]
                                }
                                """, customerName, customerEmail, productCode, quantity);
        }

        private String buildOrderJsonWithEmptyAddress(String customerName, String customerEmail,
                        String productCode, int quantity) {
                return String.format("""
                                {
                                    "orderNumber": "ORD-003",
                                    "customer": {
                                        "name": "%s",
                                        "email": "%s"
                                    },
                                    "address": {
                                        "street": "",
                                        "city": ""
                                    },
                                    "items": [
                                        {
                                            "productCode": "%s",
                                            "quantity": %d
                                        }
                                    ]
                                }
                                """, customerName, customerEmail, productCode, quantity);
        }

        private String buildOrderJsonWithoutCustomer(String productCode, int quantity) {
                return String.format("""
                                {
                                    "orderNumber": "ORD-004",
                                    "items": [
                                        {
                                            "productCode": "%s",
                                            "quantity": %d
                                        }
                                    ]
                                }
                                """, productCode, quantity);
        }

        private String buildOrderJsonWithoutItems(String customerName, String customerEmail) {
                return String.format("""
                                {
                                    "orderNumber": "ORD-005",
                                    "customer": {
                                        "name": "%s",
                                        "email": "%s"
                                    }
                                }
                                """, customerName, customerEmail);
        }

        private String buildOrderJsonWithoutOrderNumber(String customerName, String customerEmail,
                        String productCode, int quantity) {
                return String.format("""
                                {
                                "customer": {
                                "name": "%s",
                                "email": "%s"
                                },
                                "items": [
                                {
                                "productCode": "%s",
                                "quantity": %d
                                }
                                ]
                                }
                                """, customerName, customerEmail, productCode, quantity);
        }

        @Test
        public void testValidOrderSubmission() {
                String validOrderJson = buildOrderJson("John Doe", "john.doe@example.com",
                                "123 Main St", "Springfield",
                                "PROD-001", 2);

                given()
                                .contentType(ContentType.JSON)
                                .body(validOrderJson)
                                .when()
                                .post("/orders")
                                .then()
                                .statusCode(200)
                                .body("customer.name", is("John Doe"))
                                .body("customer.email", is("john.doe@example.com"))
                                .body("address.street", is("123 Main St"))
                                .body("address.city", is("Springfield"))
                                .body("items[0].productCode", is("PROD-001"))
                                .body("items[0].quantity", is(2));
        }

        @Test
        public void testInvalidOrder_EmptyCustomerName() {
                String invalidOrderJson = buildOrderJson("", "john.doe@example.com",
                                "123 Main St", "Springfield",
                                "PROD-001", 2);

                given()
                                .contentType(ContentType.JSON)
                                .body(invalidOrderJson)
                                .when()
                                .post("/orders")
                                .then()
                                .statusCode(400)
                                .body(containsString("must not be blank"));
        }

        @Test
        public void testInvalidOrder_EmptyCustomerEmail() {
                String invalidOrderJson = buildOrderJson("John Doe", "",
                                "123 Main St", "Springfield",
                                "PROD-001", 2);

                given()
                                .contentType(ContentType.JSON)
                                .body(invalidOrderJson)
                                .when()
                                .post("/orders")
                                .then()
                                .statusCode(400)
                                .body(containsString("must not be blank"));
        }

        @Test
        public void testInvalidOrder_NullCustomer() {
                String invalidOrderJson = buildOrderJsonWithoutCustomer("PROD-001", 2);

                given()
                                .contentType(ContentType.JSON)
                                .body(invalidOrderJson)
                                .when()
                                .post("/orders")
                                .then()
                                .statusCode(400)
                                .body(containsString("must not be null"));
        }

        @Test
        public void testInvalidOrder_NullItems() {
                String invalidOrderJson = buildOrderJsonWithoutItems("John Doe", "john.doe@example.com");

                given()
                                .contentType(ContentType.JSON)
                                .body(invalidOrderJson)
                                .when()
                                .post("/orders")
                                .then()
                                .statusCode(400)
                                .body(containsString("must not be null"));
        }

        @Test
        public void testInvalidOrder_EmptyProductCode() {
                String invalidOrderJson = buildOrderJson("John Doe", "john.doe@example.com",
                                "123 Main St", "Springfield",
                                "", 2);

                given()
                                .contentType(ContentType.JSON)
                                .body(invalidOrderJson)
                                .when()
                                .post("/orders")
                                .then()
                                .statusCode(400)
                                .body(containsString("must not be blank"));
        }

        @Test
        public void testInvalidOrder_InvalidQuantity() {
                String invalidOrderJson = buildOrderJson("John Doe", "john.doe@example.com",
                                "123 Main St", "Springfield",
                                "PROD-001", 0);

                given()
                                .contentType(ContentType.JSON)
                                .body(invalidOrderJson)
                                .when()
                                .post("/orders")
                                .then()
                                .statusCode(400)
                                .body(containsString("must be greater than or equal to 1"));
        }

        @Test
        public void testInvalidOrder_MultipleValidationErrors() {
                String invalidOrderJson = buildOrderJson("", "",
                                "123 Main St", "Springfield",
                                "", 0);

                given()
                                .contentType(ContentType.JSON)
                                .body(invalidOrderJson)
                                .when()
                                .post("/orders")
                                .then()
                                .statusCode(400)
                                .body(containsString("must not be blank"))
                                .body(containsString("must be greater than or equal to 1"));
        }

        @Test
        public void testValidOrderWithNullAddress() {
                String validOrderJson = buildOrderJsonWithoutAddress("John Doe", "john.doe@example.com",
                                "PROD-001", 2);

                given()
                                .contentType(ContentType.JSON)
                                .body(validOrderJson)
                                .when()
                                .post("/orders")
                                .then()
                                .statusCode(200)
                                .body("customer.name", is("John Doe"))
                                .body("customer.email", is("john.doe@example.com"))
                                .body("items[0].productCode", is("PROD-001"))
                                .body("items[0].quantity", is(2));
        }

        @Test
        public void testInvalidOrderWithEmptyAddress() {
                String invalidOrderJson = buildOrderJsonWithEmptyAddress("John Doe", "john.doe@example.com",
                                "PROD-001", 2);

                given()
                                .contentType(ContentType.JSON)
                                .body(invalidOrderJson)
                                .when()
                                .post("/orders")
                                .then()
                                .statusCode(400)
                                .body(containsString("must not be blank"));
        }

        @Test
        void shouldRequireOrderNumberOnCreate() {
                String orderJsonWithoutOrderNumber = buildOrderJsonWithoutOrderNumber("A", "a@b.com", "X", 1);

                given()
                                .contentType(ContentType.JSON)
                                .body(orderJsonWithoutOrderNumber)
                                .when()
                                .post("/orders")
                                .then()
                                .statusCode(400)
                                .body(containsString("orderNumber"));
        }

}