import sbt._
import Keys._
import PlayProject._
import com.github.play2war.plugin._

object ApplicationBuild extends Build {

	val appName = "fwplay"
	val appVersion = "1.0"

	val appDependencies = Seq(
		"postgresql" 	% "postgresql" 		% "9.1-901-1.jdbc4",
		"securesocial"	% "securesocial_2.9.1" 	% "2.0.8"
	)

	val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
		resolvers += Resolver.url("SecureSocial Repository", url("http://securesocial.ws/repository/releases/"))(Resolver.ivyStylePatterns),
		Play2WarKeys.servletVersion := "3.0"
	).settings(Play2WarPlugin.play2WarSettings: _*)

}
            
