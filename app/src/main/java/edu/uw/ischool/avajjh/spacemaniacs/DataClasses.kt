package edu.uw.ischool.avajjh.spacemaniacs

data class Event(
    val name: String,
    val description: String,
    val location: String,
    val featureImage: String,
    val date: String,
    val type: String

)

data class Astronaut(
    val name: String,
    val age: String,
    val nationality: String,
    val bio: String,
    val profileImage: String,
    val flightCount: String
)

data class Launch(
    val name: String,
    val windowStart: String,
    val windowEnd: String,
    val description: String,
    val image: String
)