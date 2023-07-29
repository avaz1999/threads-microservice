package uz.avaz.subscribe

enum class ErrorCode(val code: Int){
    FOLLOWER_NOT_FOUND(401),
    FOLLOW_NOR_FOUND(402),
    NOT_FOLLOW(403),
    GENERAL_API_EXCEPTION(404),
    NOT_UN_FOLLOW(405),
    SUBSCRIBE_NOT_FOUND(406)
}

enum class Type{
    FOLLOW,FOLLOWING
}