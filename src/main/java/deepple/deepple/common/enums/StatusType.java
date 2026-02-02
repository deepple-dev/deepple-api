package deepple.deepple.common.enums;

import lombok.Getter;

@Getter
public enum StatusType {
    OK(200, "200", "OK"),
    SUCCESS(200, "200100", "SUCCESS"),
    DUPLICATE(200, "200101", "DUPLICATE"),
    APPROVAL_REQUIRED(200, "200102", "Approval required"),
    NO_APPROVAL_REQUIRED(200, "200103", "No Approval required"),
    FAIL(200, "200200", "FAIL"),
    NO_DATA(200, "200400", "NO_DATA"),
    MULTIPLE_RESULTS_FOUND(200, "200200", "Multiple Results Found"),

    BAD_REQUEST(400, "400", "Bad Request"),
    INVALID_INPUT_VALUE(400, "400001", " Invalid Input Value"),
    INVALID_TYPE_VALUE(400, "400002", " Invalid Type Value"),
    INVALID_ENUM_VALUE(400, "400003", " Invalid Enum Value"),
    INVALID_DUPLICATE_VALUE(400, "400004", " Invalid Duplicate Value"),
    INVALID_UNKNOWN_FIELD(400, "400005", " Invalid Unknown field"),
    SIGN_IN_BAD_REQUEST(400, "400100", "SignIn Bad Request"),

    PROCESSED_ORDER(400, "400102", "Processed Order"),

    REISSUE_BAD_REQUEST(400, "400200", "Reissue Bad Request"),
    ACCESS_TOKEN_NOT_EXPIRED(400, "400300", "The access token has not expired"),
    CANNOT_BE_DELETED(400, "400400", "Cannot be deleted"),
    EXISTS_NOTIFICATION_DATETIME(400, "400500", "Exists Reservation Time"),
    CANNOT_BE_EDITED(400, "400600", "Cannot be edited"),
    INSUFFICIENT_HEARTS(400, "400700", "Insufficient heart balance"),
    DORMANT_STATUS(400, "400800", "Member is Dormant status"), // 활동중이 아닌 경우.

    UNAUTHORIZED(401, "401", "Unauthorized"),
    MISSING_ACCESS_TOKEN(401, "401001", "Missing Access Token"),
    INVALID_ACCESS_TOKEN(401, "401002", "Invalid Access Token"),
    MISSING_REFRESH_TOKEN(401, "401003", "Missing Refresh Token"),
    INVALID_REFRESH_TOKEN(401, "401004", "Invalid Refresh Token"),
    EXPIRED_REFRESH_TOKEN(401, "401005", "Expired Refresh Token"),

    FORBIDDEN(403, "403", "Forbidden"),
    HANDLE_ACCESS_DENIED(403, "403001", "Access is Denied"),
    ADMIN_ACCESS_DENIED(403, "403002", "Manager Access Denied"),
    DELETED_TARGET(403, "403003", "Deleted Target"), // 회원 삭제.
    WAITING_STATUS(403, "403004", "Waiting Screening"), // 심사 대기 상태.
    TEMPORARILY_FORBIDDEN(403, "403005", "Temporarily Forbidden"),

    NOT_FOUND(404, "404", "Not Found"),

    METHOD_NOT_ALLOWED(405, "405", "Method Not Allowed"),

    CONFLICT(409, "409", "Conflict"),

    FILE_SAVE_FAIL(500, "500900", "File Save Fail"),
    FILE_DELETE_FAIL(500, "500901", "File Delete Fail"),
    FILE_NOT_FOUND(404, "404900", "File Not Found"),
    FILE_IS_EMPTY(400, "400903", "File Is Empty"),
    INVALID_FILE_EXTENSION(400, "400901", "Invalid File Extension"),
    FILE_SIZE_EXCEEDS_CAPACITY(400, "400902", "File size exceeds capacity"),

    INTERNAL_SERVER_ERROR(500, "500", "Internal Server Error"),
    PERSISTENCE_FAIL(500, "500100", "DB Persistence Error");

    private final int status;
    private final String code;
    private final String message;

    // Enum 생성자 추가
    StatusType(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
