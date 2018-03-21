package com.icapps.niddler.ui.model.ui.json

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.math.BigInteger
import javax.swing.tree.TreeNode

interface JsonNode<T> {

    val children: MutableList<T>
    val jsonElement: JsonElement
    val name: String?
    var primitiveNumber: Number
    var type: JsonNode.Type
    var value: String?

    fun createElement(value: JsonElement, key: String?): T

    fun populateFromArray(jsonArray: JsonArray) {
        jsonArray.forEach {
            children.add(createElement(it, null))
        }
        type = JsonNode.Type.ARRAY
    }

    fun populateFromObject(jsonObject: JsonObject) {
        jsonObject.entrySet().forEach {
            children.add(createElement(it.value, it.key))
        }
        type = JsonNode.Type.OBJECT
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

    fun actualType(): JsonNode.JsonDataType {
        return when (type) {
            JsonNode.Type.ARRAY -> JsonNode.JsonDataType.ARRAY
            JsonNode.Type.OBJECT -> JsonNode.JsonDataType.OBJECT
            JsonNode.Type.PRIMITIVE -> {
                if (jsonElement.isJsonNull)
                    return JsonNode.JsonDataType.NULL

                val primitive = jsonElement.asJsonPrimitive
                if (primitive.isBoolean)
                    return JsonNode.JsonDataType.BOOLEAN
                else if (primitive.isString)
                    return JsonNode.JsonDataType.STRING

                val number = primitiveNumber
                if (number is BigInteger || number is Long || number is Int
                        || number is Short || number is Byte)
                    return JsonNode.JsonDataType.INT
                return JsonNode.JsonDataType.DOUBLE
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