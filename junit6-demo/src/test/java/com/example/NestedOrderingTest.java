package com.example;

import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)  // ← For test methods
@TestClassOrder(ClassOrderer.OrderAnnotation.class)  // ← For nested classes
class NestedOrderingTest {

    @Nested
    @Order(1) // ← Controls when this nested class runs
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class TopLevel {
        @Test
        void topLevelTest() {
            System.out.println("1. Top level");
        }
    }

    @Nested
    @Order(2) // ← InnerA runs second
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class InnerA {
        @Test
        @Order(1)
        void a1() {
            System.out.println("2. InnerA.a1");
        }

        @Test
        @Order(2)
        void a2() {
            System.out.println("3. InnerA.a2");
        }
    }

    @Nested
    @Order(3) // ← InnerB runs third
    @TestClassOrder(ClassOrderer.OrderAnnotation.class)  // ← For its own nested classes
    class InnerB {

        @Nested
        @Order(1) // ← Within InnerB, BTests runs before Deep
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class BTests {
            @Test
            void b1() {
                System.out.println("4. InnerB.b1");
            }
        }

        @Nested
        @Order(2) // ← Deep runs last
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class Deep {
            @Test
            void deepTest() {
                System.out.println("5. Deep.deepTest");
            }
        }
    }
}