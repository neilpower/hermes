package com.hootsuite.hermes

import com.google.gson.Gson
import java.io.File

/**
 * Configuration for the Hermes Server
 */
object Config {

    private const val CONFIG_PATH = "config.json"
    private const val SECRETS_PATH = "secrets.json"

    const val SLACK_AUTH_URL = "https://slack.com/api/oauth.access"

    private val configData: ConfigData =
        Gson().fromJson<ConfigData>(File(CONFIG_PATH).readText(), ConfigData::class.java)

    val authData: AuthData = Gson().fromJson<AuthData>(File(SECRETS_PATH).readText(), AuthData::class.java)

    // TODO We should only need one of these, either register admin channel or configure via file
    // Admin slack webhook to send Hermes status messages to
    var ADMIN_URL = configData.adminUrl

    // Admin Channel to send Hermes Status messages to
    val ADMIN_CHANNEL = configData.adminChannel ?: "#hermes-admin"

    // Trigger Comment for sending review request updates
    const val REREVIEW = "!hermes"

    // Parameter passed to rereview command to only notify people who have requested changes to the pull request
    const val REJECTED = "rejected"

    // Parameter passed to rereview command to only notify people who have not approved the pull request
    const val UNAPPROVED = "unapproved"
}

/**
 * Data class for the config.json file
 */
data class ConfigData(
    val adminUrl: String,
    val serverPort: Int? = null,
    val adminChannel: String? = null,
    val rereview: RereviewCommand? = null
)

data class RereviewCommand(
    val command: String? = null,
    val rejected: String? = null,
    val unapproved: String? = null
)

/**
 * Auth Data for authorizing with slack
 */
data class AuthData(
    val clientId: String,
    val secret: String
)