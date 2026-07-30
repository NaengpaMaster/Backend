package com.naengpa.naengpamasterbackend.global.exception;

import com.naengpa.naengpamasterbackend.global.response.ApiResponse;
import com.naengpa.naengpamasterbackend.product.exception.ProductNotFoundException;
import jakarta.persistence.NonUniqueResultException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() == null ? "입력값 검증에 실패했습니다." : error.getDefaultMessage())
                .orElse("입력값 검증에 실패했습니다.");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail("입력값 검증에 실패했습니다."));
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handlePasswordMismatchException(PasswordMismatchException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateEmailException(DuplicateEmailException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(DuplicateNicknameException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateNicknameException(DuplicateNicknameException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(NicknameGenerationFailedException.class)
    public ResponseEntity<ApiResponse<Void>> handleNicknameGenerationFailedException(
            NicknameGenerationFailedException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(WithdrawnEmailException.class)
    public ResponseEntity<ApiResponse<Void>> handleWithdrawnEmailException(WithdrawnEmailException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(EmailVerificationException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailVerificationException(EmailVerificationException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler({
            NonUniqueResultException.class,
            IncorrectResultSizeDataAccessException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleNonUniqueResultException(Exception exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail("리프레시 토큰 상태가 올바르지 않습니다. 다시 로그인해주세요."));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleDisabledException(DisabledException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsernameNotFoundException(UsernameNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(ScoreNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleScoreNotFoundException(ScoreNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(RecipeNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRecipeNotFoundException(RecipeNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductNotFoundException(ProductNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleMemberNotFoundException(MemberNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCommentNotFoundException(CommentNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(FoodCategoryNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleFoodCategoryNotFoundException(FoodCategoryNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(CommentAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleCommentAccessDeniedException(CommentAccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(DuplicateProductNameException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateProductNameException(DuplicateProductNameException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(DuplicateShoppingItemException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateShoppingItemException(DuplicateShoppingItemException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(InquiryNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleInquiryNotFoundException(InquiryNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(InquiryAnswerNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleInquiryAnswerNotFoundException(InquiryAnswerNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(ShoppingItemNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleShoppingItemNotFoundException(ShoppingItemNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(InquiryAlreadyAnsweredException.class)
    public ResponseEntity<ApiResponse<Void>> handleInquiryAlreadyAnsweredException(InquiryAlreadyAnsweredException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(exception.getMessage()));
    }
}
