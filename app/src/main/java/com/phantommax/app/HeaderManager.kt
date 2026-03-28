package com.phantommax.app

object HeaderManager {

    private val DESKTOP_UAS = listOf(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/132.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36"
    )

    private val HEADERS_TO_REMOVE = listOf(
        "X-Requested-With",
        "X-Android-Selected-Application",
        "X-Android-Application-Id",
        "X-Android-Package",
        "X-Android-Sdk-Version"
    )

    fun getUA(@Suppress("UNUSED_PARAMETER") isDesktop: Boolean): String {
        val seed = PhantomApp.sessionSeed
        val idx = ((seed ushr 1) % DESKTOP_UAS.size).toInt().let { if (it < 0) it + DESKTOP_UAS.size else it }
        return DESKTOP_UAS[idx]
    }

    fun getHeaders(@Suppress("UNUSED_PARAMETER") isDesktop: Boolean): Map<String, String> {
        val ua = getUA(true)
        val chromeVersion = Regex("Chrome/(\\d+)").find(ua)?.groupValues?.get(1) ?: "131"
        return mapOf(
            "User-Agent"                    to ua,
            "Accept-Language"               to "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7",
            "Sec-CH-UA"                     to "\"Google Chrome\";v=\"$chromeVersion\", \"Chromium\";v=\"$chromeVersion\", \"Not_A Brand\";v=\"24\"",
            "Sec-CH-UA-Mobile"              to "?0",
            "Sec-CH-UA-Platform"            to "\"Windows\"",
            "Sec-CH-UA-Arch"                to "\"x86\"",
            "Sec-CH-UA-Bitness"             to "\"64\"",
            "Sec-CH-UA-Model"               to "\"\"",
            "Sec-CH-UA-WoW64"               to "?0",
            "Sec-CH-UA-Full-Version-List"   to "\"Google Chrome\";v=\"$chromeVersion.0.0.0\", \"Chromium\";v=\"$chromeVersion.0.0.0\", \"Not_A Brand\";v=\"24.0.0.0\""
        )
    }

    fun applyHeaders(existingHeaders: MutableMap<String, String>, isDesktop: Boolean): Map<String, String> {
        for (remove in HEADERS_TO_REMOVE) {
            existingHeaders.remove(remove)
            existingHeaders.remove(remove.lowercase())
        }
        existingHeaders.keys.filter { it.startsWith("X-Android", ignoreCase = true) }
            .forEach { existingHeaders.remove(it) }
        existingHeaders.putAll(getHeaders(isDesktop))
        return existingHeaders
    }
}
