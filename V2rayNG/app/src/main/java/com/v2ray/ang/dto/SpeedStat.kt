package com.v2ray.ang.dto

import java.io.Serializable

data class SpeedStat(
    val uploadBps: Long,
    val downloadBps: Long,
    val serverName: String
) : Serializable
