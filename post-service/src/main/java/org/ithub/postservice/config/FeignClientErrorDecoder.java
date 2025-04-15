package org.ithub.postservice.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.ithub.postservice.exception.ExternalServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class FeignClientErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            return new ExternalServiceException("Error calling external service: " + response.reason());
        }
        return defaultErrorDecoder.decode(methodKey, response);
    }
}