package com.andrioseptianto.marvelous.model;

import com.andrioseptianto.marvelous.MarvelApiError;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class MarvelApiErrorResponse {

    private static Status mapHttpStatusToGrpcStatus(int httpStatusCode) {
        return switch (httpStatusCode) {
            case 400 -> Status.INVALID_ARGUMENT;
            case 401 -> Status.UNAUTHENTICATED;
            case 403 -> Status.PERMISSION_DENIED;
            case 404 -> Status.NOT_FOUND;
            case 409 -> Status.ABORTED;
            case 429 -> Status.RESOURCE_EXHAUSTED;
            case 499 -> Status.CANCELLED;
            case 500 -> Status.INTERNAL;
            case 501 -> Status.UNIMPLEMENTED;
            case 503 -> Status.UNAVAILABLE;
            case 504 -> Status.DEADLINE_EXCEEDED;
            default -> Status.UNKNOWN;
        };
    }

    public static void handleError(StreamObserver<?> responseObserver, MarvelApiError error) {
        Status status = mapHttpStatusToGrpcStatus(error.getCode()).withDescription(error.getStatus());
        responseObserver.onError(status.asRuntimeException());
    }
}