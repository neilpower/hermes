package com.hootsuite.hermes

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.google.gson.Gson
import com.hootsuite.hermes.database.DatabaseUtils
import com.hootsuite.hermes.github.GithubEventHandler
import com.hootsuite.hermes.github.model.Events
import com.hootsuite.hermes.github.model.SupportedEvents
import com.hootsuite.hermes.model.Team
import com.hootsuite.hermes.slack.SlashCommandHandler
import com.hootsuite.hermes.slack.model.SlackAuth
import com.hootsuite.hermes.slack.model.SlashCommand
import com.hootsuite.hermes.slack.model.SlashResponse
import io.kotless.dsl.ktor.Kotless
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.file
import io.ktor.http.content.resource
import io.ktor.http.content.static
import io.ktor.request.header
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import org.slf4j.LoggerFactory
import java.text.DateFormat

class Server : Kotless() {
    override fun prepare(app: Application) {

        with(app) {

            install(DefaultHeaders)
            install(Compression)
            install(CallLogging)
            install(ContentNegotiation) {
                gson {
                    setDateFormat(DateFormat.LONG)
                    setPrettyPrinting()
                }

            }
            routing {
                get("/") { call.respondText("Hermes") }

                static("/") {
                    file("install.html")
                    file("success.html")
                    file("error.html")
                    resource("favicon.ico")
                }

                // Webhooks from Github
                post("/github") { githubPost(call) }

                // Install Slack App
                get("/install") { installGet(call) }

                // Handle Slack Slash Command
                post("/slack") { slackPost(call) }
            }
        }
    }

    /**
     * Handle the POST to the /webhook Endpoint
     * @param call - The ApplicationCall for the request
     */
    private suspend fun githubPost(call: ApplicationCall) {
        when (val eventType = call.request.header(Events.EVENT_HEADER) ?: Events.NO_EVENT) {
            SupportedEvents.PULL_REQUEST_REVIEW.eventName -> GithubEventHandler.pullRequestReview(call.receive())
            SupportedEvents.PULL_REQUEST.eventName -> GithubEventHandler.pullRequest(call.receive())
            SupportedEvents.ISSUE_COMMENT.eventName -> GithubEventHandler.issueComment(call.receive())
            SupportedEvents.STATUS.eventName -> GithubEventHandler.status(call.receive())
            SupportedEvents.PING.eventName -> GithubEventHandler.ping(call.receive())
            else -> GithubEventHandler.unhandledEvent(eventType)
        }
        // TODO Handle Problems
        call.respond(HttpStatusCode.OK)
    }

    /**
     * Hande the GET to the /install Endpoint. Auth the app with a specific slack channel and store that as a team in the db
     * @param call - The ApplicationCall of the request
     */
    private suspend fun installGet(call: ApplicationCall) {
        val (_, response, result) = Config.SLACK_AUTH_URL
            .httpGet(createSlackQueryParams(call.request.queryParameters["code"]))
            .responseObject<SlackAuth>()

        logger.debug(response.toString())

        // TODO Handle non-200
        if (response.statusCode == HttpStatusCode.OK.value) {
            val (slackAuth, _) = result
            slackAuth?.incomingWebhook?.let { webhook ->
                DatabaseUtils.createOrUpdateTeam(Team(webhook.channel, webhook.url))
                if (webhook.channel == Config.ADMIN_CHANNEL) {
                    logger.debug("Admin Channel registered")
                }
                // TODO Remove the /1 here and do full relative
                call.respondRedirect("/1/success.html")
            } ?: call.respondRedirect("/1/error.html")
        }
    }

    /**
     * Handle the POST to the /slack Endpoint. This handles all the slash commands that are supported by hermes.
     * @param call - The ApplicationCall of the request
     */
    private suspend fun slackPost(call: ApplicationCall) {
        val slashCommand = SlashCommand.fromParameters(call.receive())
        val splitText = slashCommand.text.split(' ')
        val command = splitText.firstOrNull()
        val parameters = splitText.drop(1)
        val responseText = SlashCommandHandler.handleSlashCommand(slashCommand, command, parameters)

        val response = Fuel
            .post(slashCommand.responseUrl)
            .body(Gson().toJson(SlashResponse.ephemeral(responseText)))
            .response()
            .second
        logger.debug(response.toString())

        call.respond(HttpStatusCode.OK)
    }

    /**
     * Create the Query Params for authorizing with slack
     * @param code The code parameter to include in the query parameters
     */
    private fun createSlackQueryParams(code: String?): List<Pair<String, String>> = listOf(
        "code" to code.toString(),
        "client_id" to Config.authData.clientId,
        "client_secret" to Config.authData.secret
    )

    companion object {
        private val logger = LoggerFactory.getLogger(Server::class.java)
    }
}
