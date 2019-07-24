package com.chimerapps.niddler.ui.model.renderer.impl.json

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.math.BigInteger

internal interface JsonNode<T> {

    val children: MutableList<T>
    val jsonElement: JsonElement
    val name: String?
    var primitiveNumber: Number
    var type: Type
    var value: String?

    fun createElement(value: JsonElement, key: String?): T

    fun populateFromArray(jsonArray: JsonArray) {
        jsonArray.forEach {
            children.add(createElement(it, null))
        }
        type = Type.ARRAY
    }

    fun populateFromObject(jsonObject: JsonObject) {
        jsonObject.entrySet().forEach {
            children.add(createElement(it.value, it.key))
        }
        type = Type.OBJECT
    }

    fun initLeaf(valueAsString: String) {
        value = valueAsString
    }

    fun initLeafPrimitive(jsonPrimitive: JsonPrimitive) {
        value = jsonPrimitive.toString()

        if (jsonPrimitive.isNumber) {
            try {
                primitiveNumber = jsonPrimitive.asLong
            } catch (e: NumberFormatException) {
                primitiveNumber = jsonPrimitive.asDouble
            }
        }
    }

    fun actualType(): JsonDataType {
        return when (type) {
            Type.ARRAY -> JsonDataType.ARRAY
            Type.OBJECT -> JsonDataType.OBJECT
            Type.PRIMITIVE -> {
                if (jsonElement.isJsonNull)
                    return JsonDataType.NULL

                val primitive = jsonElement.asJsonPrimitive
                if (primitive.isBoolean)
                    return JsonDataType.BOOLEAN
                else if (primitive.isString)
                    return JsonDataType.STRING

                val number = primitiveNumber
                if (number is BigInteger || number is Long || number is Int
                        || number is Short || number is Byte)
                    return JsonDataType.INT
                return JsonDataType.DOUBLE
            }
        }
    }

    fun isAnonymous(): Boolean {
        return name == null
    }

    enum class Type {
        ARRAY, OBJECT, PRIMITIVE
    }

    enum class JsonDataType {
        ARRAY, OBJECT, BOOLEAN, INT, STRING, DOUBLE, NULL
    }
}