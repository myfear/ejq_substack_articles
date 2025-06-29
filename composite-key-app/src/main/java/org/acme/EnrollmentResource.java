package org.acme;

import java.util.List;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/enrollments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EnrollmentResource {

    @GET
    public List<Enrollment> getAll() {
        return Enrollment.listAll();
    }

    @GET
    @Path("/{studentId}/{courseCode}")
    public Response get(@PathParam("studentId") String studentId,
            @PathParam("courseCode") String courseCode) {
        EnrollmentId id = new EnrollmentId(studentId, courseCode);
        return Enrollment.findByIdOptional(id)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @POST
    @Transactional
    public Response create(EnrollmentDTO dto) {
        EnrollmentId id = new EnrollmentId(dto.studentId, dto.courseCode);
        if (Enrollment.findByIdOptional(id).isPresent()) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Enrollment already exists.")
                    .build();
        }

        Enrollment enrollment = new Enrollment(id, dto.grade);
        enrollment.persist();
        return Response.status(Response.Status.CREATED).entity(enrollment).build();
    }

    @PUT
    @Path("/{studentId}/{courseCode}")
    @Transactional
    public Response update(@PathParam("studentId") String studentId,
            @PathParam("courseCode") String courseCode,
            EnrollmentDTO dto) {
        Enrollment enrollment = Enrollment.findById(new EnrollmentId(studentId, courseCode));
        if (enrollment == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        enrollment.grade = dto.grade;
        return Response.ok(enrollment).build();
    }

    @DELETE
    @Path("/{studentId}/{courseCode}")
    @Transactional
    public Response delete(@PathParam("studentId") String studentId,
            @PathParam("courseCode") String courseCode) {
        boolean deleted = Enrollment.deleteById(new EnrollmentId(studentId, courseCode));
        return deleted ? Response.noContent().build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    public record EnrollmentDTO(String studentId, String courseCode, int grade) {
    }
}