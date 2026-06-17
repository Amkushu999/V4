package cn.dianbobo.dbb.util

import android.content.Context
import android.util.Log
import de.robv.android.xposed.XposedBridge
import com.itsme.amkush.config.Config
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object HLog {
    enum class LogLevel { DEBUG, INFO, WARN, ERROR }
    var lastTransitionTime: Long = 0
    val logBuffer = mutableListOf<String>()
    val MAX_LOG_ENTRIES = 5
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    fun d(logtype: String = Config.LOG_TAG, msg: String) = log(LogLevel.DEBUG, logtype, msg)
    fun i(logtype: String = Config.LOG_TAG, msg: String) = log(LogLevel.INFO, logtype, msg)
    fun w(logtype: String = Config.LOG_TAG, msg: String) = log(LogLevel.WARN, logtype, msg)
    fun e(logtype: String = Config.LOG_TAG, msg: String) = log(LogLevel.ERROR, logtype, msg)
    
    private fun log(level: LogLevel, tag: String, msg: String) {
        if (!Config.DEBUG_MODE && level == LogLevel.DEBUG) return
        
        val prefix = when (level) {
            LogLevel.DEBUG -> "[D]"
            LogLevel.INFO -> "[I]"
            LogLevel.WARN -> "[W]"
            LogLevel.ERROR -> "[E]"
        }
        
        val formattedMsg = "$prefix $tag: $msg"
        
        try {
            // 🛡️ Try Xposed logging first (works when injected into target system/apps)
            XposedBridge.log(formattedMsg)
        } catch (e: NoClassDefFoundError) {
            // 💡 Fallback: Xposed is missing because this is running inside your Manager UI App process!
            // Route the messages safely to standard Android Logcat instead of crashing.
            when (level) {
                LogLevel.DEBUG -> Log.d(tag, msg)
                LogLevel.INFO -> Log.i(tag, msg)
                LogLevel.WARN -> Log.w(tag, msg)
                LogLevel.ERROR -> Log.e(tag, msg)
            }
        }
    }
    
    fun localeLog(context: Context, msg: String) {
        val currentTimeMillis = System.currentTimeMillis()
        val formattedDate = dateFormat.format(Date(currentTimeMillis))
        val timeInterval = if (lastTransitionTime != 0L) (currentTimeMillis - lastTransitionTime) else 0L
        lastTransitionTime = currentTimeMillis
        val logMessage = "时间：$formattedDate\n$msg \n日志间隔时间：${timeInterval}毫秒"
        Log.d("dbb", logMessage)
        logBuffer.add(logMessage)
        if (logBuffer.size >= MAX_LOG_ENTRIES) saveLogsToFile(context)
    }
    
    private fun saveLogsToFile(context: Context) {
        val logFileDir = context.getExternalFilesDir(null)?.absolutePath ?: return
        val logFilePath = File(logFileDir, "log.txt")
        try {
            logBuffer.forEach { logMessage -> logFilePath.appendText(logMessage + "\n\n") }
            logBuffer.clear()
        } catch (e: IOException) { e.printStackTrace() }
    }
}
