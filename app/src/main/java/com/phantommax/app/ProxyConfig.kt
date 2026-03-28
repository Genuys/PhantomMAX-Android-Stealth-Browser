package com.phantommax.app

import java.net.URI
import java.net.URLDecoder

data class ProxyConfig(
    val type: Type,
    val host: String,
    val port: Int,
    val uuid: String = "",
    val security: String = "",
    val sni: String = "",
    val fingerprint: String = "",
    val flow: String = "",
    val publicKey: String = "",
    val shortId: String = "",
    val transportType: String = "tcp"
) {
    enum class Type { SOCKS5, HTTP, VLESS }

    companion object {
        fun parse(input: String): ProxyConfig? {
            val cleaned = input
                .replace("\n", "")
                .replace("\r", "")
                .replace("\t", "")
                .replace(" ", "")
                .trim()

            if (cleaned.isEmpty()) return null

            return when {
                cleaned.startsWith("socks5://", ignoreCase = true) -> parseSocks5(cleaned)
                cleaned.startsWith("socks://", ignoreCase = true) -> parseSocks5(cleaned)
                cleaned.startsWith("http://", ignoreCase = true) -> parseHttp(cleaned)
                cleaned.startsWith("https://", ignoreCase = true) -> parseHttp(cleaned)
                cleaned.startsWith("vless://", ignoreCase = true) -> parseVless(cleaned)
                cleaned.startsWith("https://t.me/proxy?", ignoreCase = true) || 
                cleaned.startsWith("http://t.me/proxy?", ignoreCase = true) || 
                cleaned.startsWith("tg://proxy?", ignoreCase = true) ||
                (cleaned.startsWith("t.me/proxy?", ignoreCase = true) && !cleaned.startsWith("http")) -> parseTelegramProxy(cleaned)
                cleaned.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+:\\d+")) -> {
                    val parts = cleaned.split(":")
                    ProxyConfig(Type.SOCKS5, parts[0], parts[1].toIntOrNull() ?: return null)
                }
                else -> parseVless("vless://$cleaned")
            }
        }

        private fun parseSocks5(url: String): ProxyConfig? {
            return try {
                val stripped = url.replaceFirst(Regex("^socks5?://", RegexOption.IGNORE_CASE), "")
                    .split("#")[0]
                    .split("@")
                val hostPort = if (stripped.size > 1) stripped.last() else stripped[0]
                val hp = hostPort.split(":")
                if (hp.size >= 2) {
                    ProxyConfig(Type.SOCKS5, hp[0], hp[1].toIntOrNull() ?: return null)
                } else null
            } catch (e: Exception) {
                null
            }
        }

        private fun parseHttp(url: String): ProxyConfig? {
            return try {
                val stripped = url.replaceFirst(Regex("^https?://", RegexOption.IGNORE_CASE), "")
                    .split("#")[0]
                    .split("@")
                val hostPort = if (stripped.size > 1) stripped.last() else stripped[0]
                val hp = hostPort.split(":")
                if (hp.size >= 2) {
                    ProxyConfig(Type.HTTP, hp[0], hp[1].toIntOrNull() ?: return null)
                } else null
            } catch (e: Exception) {
                null
            }
        }

        private fun parseVless(url: String): ProxyConfig? {
            return try {
                val cleanUrl = url.split("#")[0].trim()

                val withoutScheme = cleanUrl.removePrefix("vless://").removePrefix("VLESS://")
                val atIndex = withoutScheme.indexOf("@")
                if (atIndex < 0) return null

                val uuid = try {
                    URLDecoder.decode(withoutScheme.substring(0, atIndex), "UTF-8")
                } catch (e: Exception) {
                    withoutScheme.substring(0, atIndex)
                }

                val rest = withoutScheme.substring(atIndex + 1)
                val questionIndex = rest.indexOf("?")

                val hostPortStr: String
                val queryStr: String

                if (questionIndex >= 0) {
                    hostPortStr = rest.substring(0, questionIndex)
                    queryStr = rest.substring(questionIndex + 1)
                } else {
                    hostPortStr = rest
                    queryStr = ""
                }

                val host: String
                val port: Int

                if (hostPortStr.startsWith("[")) {
                    val bracketEnd = hostPortStr.indexOf("]")
                    if (bracketEnd < 0) return null
                    host = hostPortStr.substring(1, bracketEnd)
                    val portStr = if (bracketEnd + 1 < hostPortStr.length && hostPortStr[bracketEnd + 1] == ':') {
                        hostPortStr.substring(bracketEnd + 2)
                    } else "443"
                    port = portStr.toIntOrNull() ?: 443
                } else {
                    val colonIndex = hostPortStr.lastIndexOf(":")
                    if (colonIndex > 0) {
                        host = hostPortStr.substring(0, colonIndex)
                        port = hostPortStr.substring(colonIndex + 1).toIntOrNull() ?: 443
                    } else {
                        host = hostPortStr
                        port = 443
                    }
                }

                if (host.isEmpty()) return null

                val params = mutableMapOf<String, String>()
                if (queryStr.isNotEmpty()) {
                    queryStr.split("&").forEach { pair ->
                        val eqIndex = pair.indexOf("=")
                        if (eqIndex > 0) {
                            val key = pair.substring(0, eqIndex)
                            val value = try {
                                URLDecoder.decode(pair.substring(eqIndex + 1), "UTF-8")
                            } catch (e: Exception) {
                                pair.substring(eqIndex + 1)
                            }
                            params[key] = value
                        }
                    }
                }

                ProxyConfig(
                    type = Type.VLESS,
                    host = host,
                    port = port,
                    uuid = uuid,
                    security = params["security"] ?: "reality",
                    sni = params["sni"] ?: params["serverName"] ?: "",
                    fingerprint = params["fp"] ?: params["fingerprint"] ?: "chrome",
                    flow = params["flow"] ?: "",
                    publicKey = params["pbk"] ?: params["publicKey"] ?: "",
                    shortId = params["sid"] ?: params["shortId"] ?: "",
                    transportType = params["type"] ?: "tcp"
                )
            } catch (e: Exception) {
                null
            }
        }
        private fun parseTelegramProxy(url: String): ProxyConfig? {
            return try {
                val query = url.substringAfter("?")
                val params = query.split("&").associate {
                    val parts = it.split("=")
                    parts[0] to (parts.getOrNull(1) ?: "")
                }
                val server = params["server"] ?: return null
                val port = params["port"]?.toIntOrNull() ?: return null
                ProxyConfig(Type.SOCKS5, server, port)
            } catch (e: Exception) {
                null
            }
        }
    }
}
