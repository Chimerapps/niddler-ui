package com.chimerapps.discovery.utils

import com.google.gson.reflect.TypeToken
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * @author Nicola Verbeeck
 */
class GsonList(val type: Type) : ParameterizedType {

    override fun getRawType(): Type {
        return List::class.java
    }

    override fun getOwnerType(): Type? {
        return null
    }

    override fun getActualTypeArguments(): Array<Type> {
        return arrayOf(type)
    }

}

inline fun <reified T> createGsonListType(): GsonList {
    return GsonList(object : TypeToken<T>() {}.type)
}