package twitter.controllers

import io.javalin.http.*
import twitter.model.User

class TwController {
    private val users = mutableListOf<User>()

    fun getUsersList(ctx: Context) {
        ctx.status(200)
        ctx.json(users.map { BasicUserAdapter(it.username, it.fullname) })
    }

    fun getUserByUsername(ctx: Context) {
        val user = users.firstOrNull { it.username == ctx.pathParam("username") }
                ?: throw NotFoundResponse("Invalid username")

        ctx.json(FullUserAdapter(user.username, user.fullname, user.tweets))
    }

    fun createTweet(ctx: Context) {
        val user = users.firstOrNull { it.username == ctx.pathParam("username") }
                ?: throw NotFoundResponse("Invalid username")

        val tweet = ctx.body<Map<String, String>>()
        user.addTweet(tweet["text"] ?: "")
        ctx.json(FullUserAdapter(user.username, user.fullname, user.tweets, tweet["text"]))
    }

    fun getTweets(ctx: Context) {
        val tweets = users.flatMap { user ->
            user.tweets.map { tweet -> TweetAdapter(user.username, tweet) }
        }
        ctx.json(tweets.shuffled())
    }

    fun storeUser(ctx: Context) {
        val user = ctx.body<StoreUserAdapter>()
        assert(users.all { it.username != user.username }) {
            throw ConflictResponse("Username is taken")
        }
        users.add(user.toUser())
        ctx.status(201)
    }

    fun deleteUser(ctx: Context) {
        users.removeIf { it.username == ctx.pathParam("username") }
        ctx.status(204)
    }
}

data class TweetAdapter(val username: String, val tweet: String)
data class StoreUserAdapter(val username: String, val fullname: String, val password: String) {
    fun toUser() = User(username, fullname, password)
}
data class BasicUserAdapter(val username: String, val fullname: String)
data class FullUserAdapter(
        val username: String,
        val fullname: String,
        val tweets: List<String>,
        val lastTweet: String? = null
)
