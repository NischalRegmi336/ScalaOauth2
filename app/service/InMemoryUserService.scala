package service

/**
 * User: gsd
 * Date: 24.01.13
 * Time: 11:07
 */

import play.api.{Logger, Application}
import securesocial.core._
import securesocial.core.providers.Token
import securesocial.core.UserId
import scala.Some
import org.mindrot.jbcrypt.BCrypt


/**
 * A Sample In Memory user service in Scala
 *
 * IMPORTANT: This is just a sample and not suitable for a production environment since
 * it stores everything in memory.
 */
class InMemoryUserService(application: Application) extends UserServicePlugin(application) {
	private var users = Map[String, Identity]()
	private var tokens = Map[String, Token]()

	def find(id: UserId) = {
		if (Logger.isDebugEnabled) {
			Logger.debug("users = %s".format(users))
		}
		users.get(id.id + id.providerId)
	}

	def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {
		if (Logger.isDebugEnabled) {
			Logger.debug("users = %s".format(users))
		}
		users.values.find(u => u.email.map(e => e == email && u.id.providerId == providerId).getOrElse(false))
	}

	def save(user: Identity) {
		users = users + (user.id.id + user.id.providerId -> user)
	}

	def save(token: Token) {
		tokens += (token.uuid -> token)
	}

	def findToken(token: String): Option[Token] = {
		tokens.get(token)
	}

	def deleteToken(uuid: String) {
		tokens -= uuid
	}

	def deleteTokens() {
		tokens = Map()
	}

	def deleteExpiredTokens() {
		tokens = tokens.filter(!_._2.isExpired)
	}
}