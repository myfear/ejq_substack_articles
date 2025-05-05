package org.acme;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;

@GraphQLApi
public class UserMutation {

    @Mutation("updateUserEmail")
    public User updateUserEmail(@Name("id") int id, @Name("email") String email) {
        return updateEmailInService(id, email);
    }

    private User updateEmailInService(int id, String email) {
        // Simulate a mutation result
        return new User(id, email);
    }
}
