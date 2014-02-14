// ========================================================================
// "UCP Proxy" LOGBack configuration file.
// ========================================================================
// =======================================================
// Useful API
// =======================================================
// root(Level level, List<String> appenderNames = [])
// logger(String name, Level level, List<String> appenderNames = [], Boolean additivity = null)
// appender(String name, Class clazz, Closure closure = null)

// =======================================================
// Required imports
// =======================================================
import static ch.qos.logback.classic.Level.*

// =======================================================
// Appender that will log to STDOUT
// =======================================================
appender("rootLogConsole", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = "%date{yyyy-MM-dd HH:mm:ss.SSS} %X{channel} %-5level [%thread] - \\(%logger\\) - %message%n"
  }
}

// =======================================================
// Rootlogger: Every log goes there by default. 
// It uses the rootLog appender to log to a file
// =======================================================
//root(INFO, ["rootLog", "rootLogHtml"])
root(WARN, ["rootLogConsole"])

// =======================================================
// Example of a logger that's declared just to limit the verbose of a package
// =======================================================
logger("be.demmel.protocol.ucp.serialization.UCPPacketSerializerImpl", INFO, ["rootLogConsole"], false)
