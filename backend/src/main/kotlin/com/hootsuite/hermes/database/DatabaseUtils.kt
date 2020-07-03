package com.hootsuite.hermes.database

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest
import com.amazonaws.services.dynamodbv2.model.GetItemRequest
import com.amazonaws.services.dynamodbv2.model.PutItemRequest
import com.amazonaws.services.dynamodbv2.model.ScanRequest
import com.hootsuite.hermes.Config
import com.hootsuite.hermes.model.Review
import com.hootsuite.hermes.model.ReviewRequest
import com.hootsuite.hermes.model.ReviewState
import com.hootsuite.hermes.model.Team
import com.hootsuite.hermes.model.User
import com.hootsuite.hermes.slack.SlackMessageHandler
import com.hootsuite.hermes.slack.model.SlackUser
import io.kotless.AwsResource
import io.kotless.PermissionLevel
import io.kotless.dsl.lang.DynamoDBTable
import io.kotless.dsl.lang.withKotlessLocal
import org.slf4j.LoggerFactory

const val USER_TABLE = "users"
const val TEAMS_TABLE = "teams"
const val REVIEW_TABLE = "reviews"
const val REVIEW_REQUESTS_TABLE = "review_requests"

/**
 * Object to wrap database calls
 */
object DatabaseUtils {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @DynamoDBTable(USER_TABLE, PermissionLevel.ReadWrite)
    object UserTable

    @DynamoDBTable(TEAMS_TABLE, PermissionLevel.ReadWrite)
    object TeamTable

    @DynamoDBTable(REVIEW_TABLE, PermissionLevel.ReadWrite)
    object ReviewTable

    @DynamoDBTable(REVIEW_REQUESTS_TABLE, PermissionLevel.ReadWrite)
    object ReviewRequestTable

    private val client: AmazonDynamoDB =
        AmazonDynamoDBClientBuilder.standard().withKotlessLocal(AwsResource.DynamoDB).build()

    /**
     * Gets a slack user from the database from a given github name. If either the user or the team doesn't exist
     * returns null. Sends an admin message to slack if either is missing.
     * @param githubName - The github login for the user to lookup.
     * @return SlackUser? The slack user from the database or null if the user or team is missing
     */
    fun getSlackUserOrNull(githubName: String): SlackUser? {
        val req = GetItemRequest().withKey(mapOf(
            "githubName" to AttributeValue().apply { s = githubName }
        )).withTableName(USER_TABLE)

        val user: MutableMap<String, AttributeValue> = client.getItem(req).item

        if (user.isEmpty()) {
            SlackMessageHandler.onMissingUser(githubName, Config.ADMIN_URL)
            return null
        } else {
            val teamName: String = user["teamName"]!!.s
            val teamReq = GetItemRequest().withKey(mapOf(
                "teamName" to AttributeValue().apply { s = teamName }
            )).withTableName(TEAMS_TABLE)

            val team: MutableMap<String, AttributeValue> = client.getItem(teamReq).item

            if (team.isEmpty()) {
                SlackMessageHandler.onMissingTeam(
                    user["githubName"]!!.s,
                    user["slackName"]!!.s,
                    user["teamName"]!!.s,
                    Config.ADMIN_URL
                )
                return null
            } else {
                return SlackUser(user["slackName"]!!.s, team["slackUrl"]!!.s)
            }
        }
    }

    /**
     * Gets a team from the database or null if the team does not exist
     * @param team - The name of the team to lookup
     * @return true if the team exists, false otherwise
     */
    fun getTeamOrNull(team: String): Boolean {
        val req = GetItemRequest().withKey(mapOf(
            "teamName" to AttributeValue().apply { s = team }
        )).withTableName(TEAMS_TABLE)

        val res: MutableMap<String, AttributeValue> = client.getItem(req).item

        return res.isNotEmpty()
    }

    /**
     * Create or update a User in the Database keyed on a User's Github Name
     * @param user - The User Model Object to be stored
     */
    fun createOrUpdateUserByGithubName(user: User) {
        // TODO Check for old user
        // val existingUser = UserEntity.find { Users.githubName eq user.githubName }.firstOrNull()
        // if (existingUser != null) {
        //     existingUser.slackName = formatSlackHandle(user.slackName)
        //     existingUser.teamName = user.teamName
        //     existingUser.avatarUrl = user.avatarUrl
        //     SlackMessageHandler.onUpdateUser(
        //         user.githubName,
        //         user.slackName,
        //         user.teamName,
        //         user.avatarUrl,
        //         Config.ADMIN_URL
        //     )
        // }
        val values = mapOf(
            "githubName" to AttributeValue().withS(user.githubName),
            "slackName" to AttributeValue().withS(formatSlackHandle(user.slackName)),
            "teamName" to AttributeValue().withS(user.teamName)
        )

        val req = PutItemRequest().withItem(
            if (user.avatarUrl != null) {
                values.plus("avatarUrl" to AttributeValue().withS(user.avatarUrl))
            } else {
                values
            }
        ).withTableName(USER_TABLE)

        client.putItem(req)

        SlackMessageHandler.onCreateUser(
            user.githubName,
            user.slackName,
            user.teamName,
            user.avatarUrl,
            Config.ADMIN_URL
        )
    }

    /**
     * Delete Users for the given Slack Handle from the database
     * @param slackHandle - The Slack Handle of the User to be deleted
     */
    fun deleteUsersBySlackHandle(slackHandle: String): Int {
        // TODO
        // val users = UserEntity.find { Users.slackName eq formatSlackHandle(slackHandle) }
        // val size = users.toList().size
        // users.forEach { it.delete() }
        // size
        return 1
    }

    /**
     * Update a Users Avatar based on a slack handle
     * @param slackHandle - The slack handle of the user (including the mention character)
     * @param avatarString - The string of the User's avatar
     */
    fun updateAvatar(slackHandle: String, avatarString: String) {
        // TODO Support
        // val existingUser = UserEntity.find { Users.slackName eq formatSlackHandle(slackHandle) }.firstOrNull()
        // if (existingUser != null) {
        //     existingUser.avatarUrl = avatarString
        //     SlackMessageHandler.onUpdateUser(
        //         existingUser.githubName,
        //         existingUser.slackName,
        //         existingUser.teamName,
        //         existingUser.avatarUrl,
        //         Config.ADMIN_URL
        //     )
        // }
    }

    /**
     * Create or update a Team in the Database
     * @param team - The Team Model Object to be stored
     */
    fun createOrUpdateTeam(team: Team) {
        // TODO Fix for update
        // val existingTeam = TeamEntity.find { Teams.teamName eq team.teamName }.firstOrNull()
        // if (existingTeam != null) {
        //     existingTeam.slackUrl = team.slackUrl
        //     SlackMessageHandler.onUpdateTeam(
        //         team.teamName,
        //         team.slackUrl,
        //         Config.ADMIN_URL
        //     )
        // }
        val values = mapOf(
            "teamName" to AttributeValue().apply { s = team.teamName },
            "slackUrl" to AttributeValue().apply { s = team.slackUrl }
        )

        val req = PutItemRequest().withItem(values).withTableName(TEAMS_TABLE)

        client.putItem(req)
        SlackMessageHandler.onUpdateTeam(
            team.teamName,
            team.slackUrl,
            Config.ADMIN_URL
        )
    }

    /**
     * Create or update a Review Request in the database
     * @param request - The Review Request to be stored in the database
     */
    fun createOrUpdateReviewRequest(request: ReviewRequest) {

        val values = mapOf(
            "htmlUrl" to AttributeValue().apply { s = request.htmlUrl },
            "githubName" to AttributeValue().apply { s = request.githubName }
        )

        val req = PutItemRequest().withItem(values).withTableName(REVIEW_REQUESTS_TABLE)

        client.putItem(req)

    }

    /**
     * Create or update a Review in the database
     * @param review - The Review to be stored in the database
     */
    fun createOrUpdateReview(review: Review) {
        val values = mapOf(
            "htmlUrl" to AttributeValue().apply { s = review.htmlUrl },
            "githubName" to AttributeValue().apply { s = review.githubName },
            "reviewState" to AttributeValue().apply { s = review.reviewState.name }
        )

        val req = PutItemRequest().withItem(values).withTableName(REVIEW_TABLE)

        client.putItem(req)
    }

    /**
     * Delete review requests for the given Pull Request from the database
     * @param url - The Url of the Pull Request
     */
    fun deleteReviewRequests(url: String) {
        // TODO
//            ReviewRequestEntity.find { ReviewRequests.htmlUrl eq url }.forEach { it.delete() }
    }

    /**
     * Delete reviews for the given Pull Request from the database
     * @param url - The Url of the Pull Request
     */
    fun deleteReviews(url: String) {
        // TODO
        // ReviewEntity.find { Reviews.htmlUrl eq url }.forEach { it.delete() }
    }

    /**
     * Delete the review for the given Pull Request and Github User from the database
     * @param url - The Url of the Pull Request
     */
    fun deleteReview(url: String, githubName: String) {
        val result = client.deleteItem(
            DeleteItemRequest()
                .withKey(mapOf("githubName" to AttributeValue().withS(githubName)))
                .withConditionExpression("htmlUrl = :a")
                .withExpressionAttributeValues(mapOf(":a" to AttributeValue().withS(url)))
        )
    }

    /**
     * Get a list of rereviewers from the database based on a key for the Pull Request
     * @param htmlUrl - The Html URL of the Pull Request
     * @return List<SlackUser> - A list of slack users reviewing the pull request
     */
    fun getRereviewers(htmlUrl: String): List<SlackUser> {
        val scanReq = ScanRequest()
            .withTableName(REVIEW_REQUESTS_TABLE)
            .withProjectionExpression("htmlUrl, githubName")
            .withFilterExpression("htmlUrl = :a")
            .withExpressionAttributeValues(mapOf(":a" to AttributeValue().withS(htmlUrl)))

        val result = client.scan(scanReq)

        return result.items.mapNotNull {
            getSlackUserOrNull(it["githubName"]!!.s)
        }
    }

    /**
     * Get a list of Reviewers from the database based on a key for the Pull Request and a Review State
     * @param htmlUrl - The Html URL of the Pull Request
     * @param reviewState - The state of review to get reviews for
     * @return List<SlackUser> - A list of slack users who have reviewed the Pull Request with a given state
     */
    fun getReviewsByState(htmlUrl: String, reviewState: Set<ReviewState>): List<SlackUser> {
        val scanReq = ScanRequest()
            .withTableName(REVIEW_TABLE)
            .withProjectionExpression("githubName, htmlUrl, reviewState")
            .withFilterExpression("htmlUrl = :a")
            .withExpressionAttributeValues(mapOf(":a" to AttributeValue().withS(htmlUrl)))

        val result = client.scan(scanReq)

        return result.items.mapNotNull {
            if (it["reviewState"]!!.s in reviewState.map { it.name }) {
                getSlackUserOrNull(it["githubName"]!!.s)
            } else {
                null
            }
        }
    }

    /**
     * Get a list of Review Requests from the database based on a github username
     * @param slackHandle - The slack handle of the Review Requests to find
     * @return List<ReviewRequest> - The Review requests associated with the given github user
     */
    fun getReviewRequestsBySlackHandle(slackHandle: String): List<ReviewRequest> {
        val req = ScanRequest()
            .withTableName(USER_TABLE)
            .withProjectionExpression("githubName, slackName")
            .withFilterExpression("slackName = :a")
            .withExpressionAttributeValues(mapOf(":a" to AttributeValue().withS(slackHandle)))

        val items = client.scan(req)

        val githubName = items.items.firstOrNull()?.get("githubName")!!.s

        val scanReq = ScanRequest()
            .withTableName(REVIEW_REQUESTS_TABLE)
            .withProjectionExpression("htmlUrl, githubName")
            .withFilterExpression("githubName = :a")
            .withExpressionAttributeValues(mapOf(":a" to AttributeValue().withS(githubName)))

        val result = client.scan(scanReq)

        return result.items.map {
            ReviewRequest(it["htmlUrl"]!!.s, it["githubName"]!!.s)
        }
    }

    /**
     * Format a handle for tagging in Slack
     * TODO Should the handle formatting be handled here?
     */
    private fun formatSlackHandle(name: String) = "@$name"
}