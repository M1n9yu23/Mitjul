/*
 * Copyright 2026 MinGyu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gyugle.mitjul.server.app

import com.gyugle.mitjul.server.core.security.Secret
import java.nio.file.Path

data class ServerConfig(
    val host: String,
    val port: Int,
    val databaseUrl: String,
    val uploadDir: Path,
    val signingKey: Secret,
) {
    companion object {
        fun fromEnvironment(): ServerConfig = ServerConfig(
            host = setting("MITJUL_HOST", default = "127.0.0.1"),
            port = portSetting("MITJUL_PORT", default = 8080),
            databaseUrl = setting("MITJUL_DATABASE_URL", default = "jdbc:h2:./data/mitjul"),
            uploadDir = Path.of(setting("MITJUL_UPLOAD_DIR", default = "./data/images")),
            signingKey = secret("MITJUL_SIGNING_KEY", minLength = 32),
        )
    }
}

private fun setting(name: String, default: String): String =
    System.getenv(name)?.takeIf { it.isNotBlank() } ?: default

private fun portSetting(name: String, default: Int): Int {
    val raw = setting(name, default = default.toString())
    return raw.toIntOrNull()?.takeIf { it in 1..65535 }
        ?: error("$name 값은 1~65535 사이의 포트 번호여야 합니다. 현재 값: \"$raw\"")
}

private fun secret(name: String, minLength: Int): Secret {
    val raw = System.getenv(name)?.takeIf { it.isNotBlank() }
        ?: error("$name 환경변수가 설정되지 않았습니다. 이 값이 없으면 서버를 시작할 수 없습니다.")
    if (raw.length < minLength) {
        error("$name 값은 ${minLength}자 이상이어야 합니다.")
    }
    return Secret(raw)
}
