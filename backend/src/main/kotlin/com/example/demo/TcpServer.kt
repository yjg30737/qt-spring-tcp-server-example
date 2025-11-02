package com.example.demo

import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlin.concurrent.thread

@Component
class TcpServer {
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private val clients = mutableListOf<ClientHandler>()

    @PostConstruct
    fun start() {
        isRunning = true
        thread {
            try {
                serverSocket = ServerSocket(5000)
                println("TCP Server started on port 5000")
                
                while (isRunning) {
                    try {
                        val clientSocket = serverSocket?.accept()
                        clientSocket?.let {
                            println("Client connected: ${it.inetAddress.hostAddress}")
                            val handler = ClientHandler(it)
                            clients.add(handler)
                            handler.start()
                        }
                    } catch (e: Exception) {
                        if (isRunning) {
                            println("Error accepting client: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                println("Server error: ${e.message}")
            }
        }
    }

    @PreDestroy
    fun stop() {
        isRunning = false
        clients.forEach { it.close() }
        clients.clear()
        serverSocket?.close()
        println("TCP Server stopped")
    }

    inner class ClientHandler(private val socket: Socket) {
        private val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        private val writer = PrintWriter(socket.getOutputStream(), true)
        private var running = true

        fun start() {
            thread {
                try {
                    while (running && !socket.isClosed) {
                        val message = reader.readLine()
                        if (message != null) {
                            println("[${socket.inetAddress.hostAddress}] Received: $message")
                            
                            // 메시지 처리 및 응답
                            val response = processMessage(message)
                            println("[${socket.inetAddress.hostAddress}] Sending: $response")
                            writer.println(response)
                        } else {
                            break
                        }
                    }
                } catch (e: Exception) {
                    println("Client handler error: ${e.message}")
                } finally {
                    close()
                }
            }
        }

        private fun processMessage(message: String): String {
            // 받은 메시지에 따라 다른 응답 반환
            return when {
                message.startsWith("HELLO", ignoreCase = true) -> 
                    "Welcome to Kotlin Server!"
                message.startsWith("PING", ignoreCase = true) -> 
                    "PONG"
                message.startsWith("ECHO", ignoreCase = true) -> 
                    message.substring(4).trim()
                message.startsWith("TIME", ignoreCase = true) -> 
                    "Server time: ${System.currentTimeMillis()}"
                else -> 
                    "Server received: $message"
            }
        }

        fun sendMessage(msg: String) {
            writer.println(msg)
        }

        fun close() {
            running = false
            try {
                socket.close()
                clients.remove(this)
                println("Client disconnected")
            } catch (e: Exception) {
                println("Error closing client: ${e.message}")
            }
        }
    }
}
