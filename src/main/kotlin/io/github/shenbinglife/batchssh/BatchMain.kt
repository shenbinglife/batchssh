package io.github.shenbinglife.batchssh

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*

fun main(args: Array<String>) {
    BatchMain(args).doBatch()

}

class BatchMain(args: Array<String>) {
    companion object {
        const val  EXEC_CMD = "exec"
        const val SCP_CMD = "scp"
        val  LOGGER: Logger = LoggerFactory.getLogger(BatchMain::class.java)
    }

    private val configFile: File
    private val cmdType: String
    private val hosts: List<String>
    private val username: String
    private val pwd: String
    private var port: Int
    private val timeout: Long
    private var cmd: String? = null
    private var scpFrom: String? = null
    private var scpTo: String? = null

    init {
        var configPropertyFile = File("config.properties")
        var cmdTypeIndex = 0
        args.forEachIndexed { index, value ->
            if (value == "--conf") {
                configPropertyFile = File(args[index + 1])
                cmdTypeIndex = index + 2
            }
        }
        cmdType = args[cmdTypeIndex]
        when (cmdType) {
            SCP_CMD -> {
                scpFrom = args[cmdTypeIndex + 1].trim()
                scpTo = args[cmdTypeIndex + 2].trim()
            }
            EXEC_CMD -> {
                cmd = args[cmdTypeIndex + 1].trim()
            }
            else -> {
                throw IllegalArgumentException("error cmd type: $cmdType")
            }
        }

        configFile = configPropertyFile
        if (!configFile.exists() || configFile.isDirectory) {
            throw IllegalArgumentException("config file not exists: ${configFile.canonicalPath}")
        }
        val properties = Properties()
        configFile.reader(charset = Charsets.UTF_8).use {
            properties.load(it)
        }
        hosts = properties.getProperty("hosts", "").split(",").map { it.trim() }.filterNot { it.isBlank() }
        username = properties.getProperty("user").trim()
        if (username == null || username.isBlank()) {
            throw IllegalArgumentException("error config property file for user not exists")
        }
        port = properties.getProperty("port", "22").trim().toInt()
        pwd = properties.getProperty("pwd", "").trim()
        timeout = properties.getProperty("timeout", "600000").trim().toLong()
    }

    fun doBatch() {
        LOGGER.info("batch ssh start.")
        val start = System.currentTimeMillis()
        val threads = mutableListOf<Thread>()
        for (host in hosts) {
            val t = Thread {
                val conn =
                    SSHConnection(host, username, port, pwd, timeout)
                conn.use {
                    when (cmdType) {
                        SCP_CMD -> {
                            Objects.requireNonNull(scpFrom, "scp from path can not be null")
                            Objects.requireNonNull(scpFrom, "scp to path can not be null")
                            conn.scp(scpFrom!!, scpTo!!)
                        }
                        EXEC_CMD -> {
                            Objects.requireNonNull(cmd, "cmd to exec can not be null")
                            conn.exec(cmd!!)
                        }
                        else -> {
                            throw IllegalArgumentException("error cmd type: $cmdType")
                        }
                    }
                }
            }
            t.start()
            threads.add(t)
        }
        for (thread in threads) {
            thread.join()
        }
        val end = System.currentTimeMillis()
        LOGGER.info("batch ssh run end, using millseconds: ${end - start}")
    }

}