package smartcast.abj.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import smartcast.abj.dto.error.ErrorResponse;

@RestControllerAdvice
@CrossOrigin
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handlerNotFoundException(NotFoundException exception) {
        return new ResponseEntity<>(new ErrorResponse("not_found", exception.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(LimitExceededException.class)
    public ResponseEntity<ErrorResponse> handlerLimitExceededException(LimitExceededException exception) {
        return new ResponseEntity<>(new ErrorResponse("limit_exceeded", exception.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AlreadyException.class)
    public ResponseEntity<ErrorResponse> handlerAlreadyException(AlreadyException exception) {
        return new ResponseEntity<>(new ErrorResponse("not_found", exception.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ErrorResponse> handlerInvalidData(InvalidDataException exception) {
        return new ResponseEntity<>(new ErrorResponse("invalid_data", exception.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingFieldException.class)
    public ResponseEntity<ErrorResponse> handlerMissingFieldException(MissingFieldException exception) {
        return new ResponseEntity<>(new ErrorResponse("missing_field", exception.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ETagMismatchException.class)
    public ResponseEntity<ErrorResponse> handlerETagMismatchException(ETagMismatchException exception) {
        return new ResponseEntity<>(new ErrorResponse("eTag_mismatch", "ETag mismatch"), HttpStatus.PRECONDITION_FAILED);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handlerInsufficientFundsException(InsufficientFundsException exception) {
        return new ResponseEntity<>(new ErrorResponse("insufficient_funds", "card amount is not enough"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handlerResponseStatusException(ResponseStatusException exception) {
        return new ResponseEntity<>(new ErrorResponse(((HttpStatus) exception.getStatusCode()).getReasonPhrase(), (exception).getReason()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handlerAuthorizationDeniedException(AuthorizationDeniedException exception) {
        return new ResponseEntity<>(new ErrorResponse("access_denied", "you do not have permission"), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handlerRuntimeException(RuntimeException exception) {
        return new ResponseEntity<>(new ErrorResponse("server_error", exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
