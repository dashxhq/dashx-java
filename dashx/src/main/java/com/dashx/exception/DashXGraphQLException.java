package com.dashx.exception;

import com.netflix.graphql.dgs.client.GraphQLError;
import java.util.List;

/**
 * Exception thrown when GraphQL errors occur during query or mutation execution.
 * Contains the list of GraphQL errors returned by the server.
 */
public class DashXGraphQLException extends DashXException {
    private final List<GraphQLError> errors;

    /**
     * Constructs a new GraphQL exception with the specified error list.
     *
     * @param errors the list of GraphQL errors
     */
    public DashXGraphQLException(List<GraphQLError> errors) {
        super(formatErrorMessage(errors));
        this.errors = errors;
    }

    /**
     * Constructs a new GraphQL exception with the specified message and error list.
     *
     * @param message the detail message
     * @param errors the list of GraphQL errors
     */
    public DashXGraphQLException(String message, List<GraphQLError> errors) {
        super(message);
        this.errors = errors;
    }

    /**
     * Returns the list of GraphQL errors.
     *
     * @return the list of GraphQL errors
     */
    public List<GraphQLError> getErrors() {
        return errors;
    }

    /**
     * Formats the error list into a readable error message.
     *
     * @param errors the list of GraphQL errors
     * @return formatted error message
     */
    private static String formatErrorMessage(List<GraphQLError> errors) {
        if (errors == null || errors.isEmpty()) {
            return "GraphQL error occurred";
        }

        if (errors.size() == 1) {
            return "GraphQL error: " + errors.get(0).getMessage();
        }

        StringBuilder sb = new StringBuilder("GraphQL errors occurred:\n");
        for (int i = 0; i < errors.size(); i++) {
            sb.append(i + 1).append(". ").append(errors.get(i).getMessage());
            if (i < errors.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
