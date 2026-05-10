package com.limelight.ui

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputConnectionWrapper
import android.widget.EditText

class RemoteImeEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : EditText(context, attrs, defStyleAttr) {

    interface RemoteInputListener {
        fun onCommitText(text: CharSequence)
        fun onDeleteSurroundingText(beforeLength: Int)
        fun onSendKeyEvent(event: KeyEvent)
        fun onEditorAction(actionId: Int)
    }

    var remoteInputListener: RemoteInputListener? = null

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        val baseConnection = super.onCreateInputConnection(outAttrs) ?: return null
        outAttrs.imeOptions = outAttrs.imeOptions or EditorInfo.IME_FLAG_NO_EXTRACT_UI

        return object : InputConnectionWrapper(baseConnection, true) {
            override fun commitText(text: CharSequence?, newCursorPosition: Int): Boolean {
                val result = super.commitText(text, newCursorPosition)
                if (!text.isNullOrEmpty()) {
                    remoteInputListener?.onCommitText(text)
                }
                return result
            }

            override fun deleteSurroundingText(beforeLength: Int, afterLength: Int): Boolean {
                val editingComposition = hasComposingText()
                val result = super.deleteSurroundingText(beforeLength, afterLength)
                if (!editingComposition && beforeLength > 0) {
                    remoteInputListener?.onDeleteSurroundingText(beforeLength)
                }
                return result
            }

            override fun deleteSurroundingTextInCodePoints(beforeLength: Int, afterLength: Int): Boolean {
                val editingComposition = hasComposingText()
                val result = super.deleteSurroundingTextInCodePoints(beforeLength, afterLength)
                if (!editingComposition && beforeLength > 0) {
                    remoteInputListener?.onDeleteSurroundingText(beforeLength)
                }
                return result
            }

            override fun sendKeyEvent(event: KeyEvent): Boolean {
                if (!(event.keyCode == KeyEvent.KEYCODE_DEL && hasComposingText())) {
                    remoteInputListener?.onSendKeyEvent(event)
                }
                return super.sendKeyEvent(event)
            }

            override fun performEditorAction(editorAction: Int): Boolean {
                remoteInputListener?.onEditorAction(editorAction)
                return true
            }
        }
    }

    private fun hasComposingText(): Boolean {
        val editable = text ?: return false
        val start = BaseInputConnection.getComposingSpanStart(editable)
        val end = BaseInputConnection.getComposingSpanEnd(editable)
        return start >= 0 && end > start
    }
}
