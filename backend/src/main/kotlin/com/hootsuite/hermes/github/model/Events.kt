package com.hootsuite.hermes.github.model

import com.google.gson.annotations.SerializedName

/**
 * Object to hold the Github Events handled by Hermes
 */
object Events {

    const val EVENT_HEADER = "X-GitHub-Event"

    const val NO_EVENT = "NO_EVENT"
}

/**
 * The events which are supported by Hermes
 */
enum class SupportedEvents(val eventName: String) {
    PULL_REQUEST_REVIEW("pull_request_review"),
    PULL_REQUEST("pull_request"),
    ISSUE_COMMENT("issue_comment"),
    STATUS("status"),
    PING("ping")
}

/**
 * Github API Event for an Issue Comment
 */
data class IssueCommentEvent(
    val action: IssueCommentAction,
    val issue: Issue,
    val comment: Comment
)

/**
 * Github API Event for a Pull Request
 */
data class PullRequestEvent(
    val action: PullRequestAction,
    @SerializedName("pull_request")
    val pullRequest: PullRequest,
    @SerializedName("requested_reviewer")
    val requestedReviewer: User? = null,
    val sender: User? = null
)

/**
 * Github API Event for a Pull Request Review
 */
data class PullRequestReviewEvent(
    val action: PullRequestReviewAction,
    val review: Review,
    @SerializedName("pull_request")
    val pullRequest: PullRequest,
    val sender: User
)

/**
 * Github API Event for a Status Update
 */
data class StatusEvent(
    val state: StatusState,
    val commit: Commit,
    val repository: Repository,
    @SerializedName("target_url")
    val targetUrl: String
)

/**
 * Github API Event for a Ping. Sent when registering a new webhook
 */
data class PingEvent(
    val zen: String,
    val hook: Webhook,
    val repository: Repository?,
    val sender: User
)

/**
 * The possible states of a Github Issue Comment Event
 */
@Suppress("unused")
enum class IssueCommentAction {
    @SerializedName("created")
    CREATED,

    @SerializedName("edited")
    EDITED,

    @SerializedName("deleted")
    DELETED
}

/**
 * Possible States for the Action of a Pull Request Event
 */
@Suppress("unused")
enum class PullRequestAction {
    @SerializedName("assigned")
    ASSIGNED,

    @SerializedName("unassigned")
    UNASSIGNED,

    @SerializedName("review_requested")
    REVIEW_REQUESTED,

    @SerializedName("review_request_removed")
    REVIEW_REQUEST_REMOVED,

    @SerializedName("labeled")
    LABELED,

    @SerializedName("unlabeled")
    UNLABELED,

    @SerializedName("opened")
    OPENED,

    @SerializedName("edited")
    EDITED,

    @SerializedName("closed")
    CLOSED,

    @SerializedName("reopened")
    REOPENED,

    @SerializedName("synchronize")
    SYNCHRONIZE
}

/**
 * The possible states of a Github Review Event
 */
@Suppress("unused")
enum class PullRequestReviewAction {
    @SerializedName("submitted")
    SUBMITTED,

    @SerializedName("edited")
    EDITED,

    @SerializedName("dismissed")
    DISMISSED
}

/**
 * The Possible states of a Github Status Event
 */
@Suppress("unused")
enum class StatusState {
    @SerializedName("pending")
    PENDING,

    @SerializedName("success")
    SUCCESS,

    @SerializedName("failure")
    FAILURE,

    @SerializedName("error")
    ERROR
}