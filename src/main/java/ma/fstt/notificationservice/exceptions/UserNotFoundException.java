package ma.fstt.notificationservice.exceptions;
import ma.fstt.notificationservice.enums.ErrorCode;
import ma.fstt.notificationservice.exceptions.BusinessException;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(Long userId) {
        super(
                ErrorCode.USER_NOT_FOUND,
                String.format("User not found with ID: %d", userId)
        );
    }

    public UserNotFoundException(String technicalMessage) {
        super(ErrorCode.USER_NOT_FOUND, technicalMessage);
    }
}
