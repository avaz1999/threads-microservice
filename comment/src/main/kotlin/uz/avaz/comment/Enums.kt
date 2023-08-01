package uz.avaz.comment

enum class ErrorCode(val code: Int){
    USER_NOT_FOUND(101),
    PASSWORD_ERROR(102),
    EXIST_USERNAME(103),
    PHONE_NUMBER_ERROR(104),
    EMAIL_ERROR_EXCEPTION(105)
}