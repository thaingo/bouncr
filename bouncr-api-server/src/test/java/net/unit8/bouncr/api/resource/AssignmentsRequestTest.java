package net.unit8.bouncr.api.resource;

import enkan.component.BeansConverter;
import enkan.component.jackson.JacksonBeansConverter;
import enkan.system.EnkanSystem;
import net.unit8.bouncr.api.boundary.AssignmentsRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class AssignmentsRequestTest {
    @Test
    void test() {
        EnkanSystem system = EnkanSystem.of("converter", new JacksonBeansConverter());
        system.start();
        try {
            BeansConverter converter = system.getComponent("converter");

            AssignmentsRequest assignmentsRequest = converter.createFrom(List.of(
                    Map.of("group",
                            Map.of("id", 1L))
                    )
                    , AssignmentsRequest.class);
            System.out.println(assignmentsRequest);
        } finally {
            system.stop();
        }
    }

    @Test
    void testGeneric() {
        EnkanSystem system = EnkanSystem.of("converter", new JacksonBeansConverter());
        system.start();
        try {
            BeansConverter converter = system.getComponent("converter");

            List assignmentsRequest = converter.createFrom(List.of(
                    Map.of("group",
                            Map.of("id", 1L))
                    )
                    , List.class);
            System.out.println(assignmentsRequest);
        } finally {
            system.stop();
        }
    }

}
