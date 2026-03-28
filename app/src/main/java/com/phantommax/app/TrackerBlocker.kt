package com.phantommax.app

object TrackerBlocker {

    private val blockedDomains = listOf(
        "mc.yandex.ru",
        "top-fwz1.mail.ru",
        "counter.yadro.ru",
        "pixel.mail.ru",
        "ad.mail.ru",
        "sdc.mail.ru"
    )

    private val blockedSubstrings = listOf(
        "analytics",
        "telemetry",
        "fingerprint",
        "tracking",
        "metrics",
        "stat.",
        "counter.",
        "pixel.",
        "beacon.",
        "collect."
    )

    fun shouldBlock(url: String): Boolean {
        val lower = url.lowercase()
        for (domain in blockedDomains) {
            if (lower.contains(domain)) return true
        }
        for (sub in blockedSubstrings) {
            if (lower.contains(sub)) return true
        }
        return false
    }
}
