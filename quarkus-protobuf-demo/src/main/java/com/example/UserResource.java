package com.example;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.proto.CreateUserRequest;
import com.example.proto.User;
import com.example.proto.UserList;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/users")
public class UserResource {

    private static final AtomicInteger idCounter = new AtomicInteger(1);
    private static final List<User> users = new ArrayList<>();

    static {
        users.add(User.newBuilder()
                .setId(idCounter.getAndIncrement())
                .setName("Alice")
                .setEmail("alice@example.com")
                .setIsActive(true)
                .setCreatedAt(System.currentTimeMillis())
                .build());

        users.add(User.newBuilder()
                .setId(idCounter.getAndIncrement())
                .setName("Bob")
                .setEmail("bob@example.com")
                .setIsActive(true)
                .setCreatedAt(System.currentTimeMillis())
                .build());
    }

    @GET
    @Produces("application/x-protobuf")
    public UserList getAll() {
        return UserList.newBuilder()
                .addAllUsers(users)
                .setTotalCount(users.size())
                .build();
    }

    @GET
    @Path("/{id}")

    @Produces("application/x-protobuf")
    public User find(@PathParam("id") int id) {
        return users.stream()
                .filter(u -> u.getId() == id)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @POST
    @Consumes("application/x-protobuf")
    @Produces("application/x-protobuf")

    public User create(CreateUserRequest request) {
        User newUser = User.newBuilder()
                .setId(idCounter.getAndIncrement())
                .setName(request.getName())
                .setEmail(request.getEmail())
                .setIsActive(true)
                .setCreatedAt(System.currentTimeMillis())
                .build();

        users.add(newUser);
        return newUser;
    }

    @GET
    @Path("/json")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserJson> allAsJson() {
        return users.stream()
                .map(u -> new UserJson(u.getId(), u.getName(), u.getEmail(), u.getIsActive()))
                .toList();
    }

    @GET
    @Path("/compare-sizes")
    @Produces(MediaType.TEXT_PLAIN)
    public String compareSizes() throws Exception {
        // Create a sample user
        User user = User.newBuilder()
                .setId(42)
                .setName("Alice")
                .setEmail("alice@example.com")
                .setIsActive(true)
                .setCreatedAt(System.currentTimeMillis())
                .build();

        // Protobuf size
        byte[] protobufBytes = user.toByteArray();
        int protobufSize = protobufBytes.length;

        // JSON size (approximate)
        String json = String.format(
                "{\"id\":%d,\"name\":\"%s\",\"email\":\"%s\",\"isActive\":%b,\"createdAt\":%d}",
                user.getId(), user.getName(), user.getEmail(),
                user.getIsActive(), user.getCreatedAt());
        int jsonSize = json.getBytes().length;

        return String.format(
                "Size Comparison:\n" +
                        "Protobuf: %d bytes\n" +
                        "JSON: %d bytes\n" +
                        "Savings: %.1f%% smaller with Protobuf\n",
                protobufSize, jsonSize,
                (1 - (double) protobufSize / jsonSize) * 100);
    }

    public record UserJson(int id, String name, String email, boolean isActive) {
    }
}