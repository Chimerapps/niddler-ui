package com.icapps.niddler.ui.model.ui.xml

interface XMLNode<T> {

    var value: String?
    var name: String?
    var type: Type

    enum class Type {
        NODE, TEXT
    }

}