package com.icapps.niddler.ui.form

/**
 * @author Nicola Verbeeck
 * @date 21/11/16.
 */
interface MainThreadDispatcher {

    companion object {
        lateinit var instance: MainThreadDispatcher
    }

    fun dispatch(toExecute: Runnable)

}