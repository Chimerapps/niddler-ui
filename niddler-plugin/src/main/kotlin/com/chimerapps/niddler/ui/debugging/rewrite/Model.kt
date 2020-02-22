package com.chimerapps.niddler.ui.debugging.rewrite

enum class RewriteType(val charlesCode: Int) {
    ADD_HEADER(1),
    MODIFY_HEADER(3),
    REMOVE_HEADER(2),
    HOST(4),
    PATH(5),
    URL(6),
    ADD_QUERY_PARAM(8),
    MODIFY_QUERY_PARAM(9),
    REMOVE_QUERY_PARAM(10),
    RESPONSE_STATUS(11),
    BODY(7)
}