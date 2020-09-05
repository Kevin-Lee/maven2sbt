package maven2sbt.core

import Common._

import scala.xml.Elem

import Repository._

import just.fp.{Named, Render}

/**
  * @author Kevin Lee
  * @since 2019-04-21
  */
final case class Repository(id: RepoId, name: RepoName, url: RepoUrl)

object Repository {
  final case class RepoId(repoId: String) extends AnyVal
  final case class RepoName(repoName: String) extends AnyVal
  final case class RepoUrl(repoUrl: String) extends AnyVal

  implicit val named: Named[Repository] = Named.named("resolvers")

  implicit val render: Render[Repository] =
    Render.namedRender("repository", repository => Repository.render(repository))

  def from(pom: Elem): Seq[Repository] = for {
    repositories <- pom \ "repositories"
    repository <- repositories.child
    url = (repository \ "url").text
    if url.nonEmpty
    id = (repository \ "id").text
    name = (repository \ "name").text
  } yield Repository(RepoId(id), RepoName(name), RepoUrl(url))

  def render(repository: Repository): String =
    s""""${repository.name.repoName}" at "${repository.url.repoUrl}""""

  // TODO: Remove it. It's no longer in use in favor of maven2sbt.core.BuildSbt.toListOfFieldValue.
  def renderToResolvers(
    repositories: Seq[Repository],
    indentSize: Int
  ): String = {
    val idt = indent(indentSize)
    repositories match {
      case Nil =>
        ""

      case x :: Nil =>
        s"""resolvers += ${render(x)}"""

      case x :: xs =>
        s"""resolvers ++= Seq(
           |$idt  ${render(x)},
           |${xs.map(render).mkString(s"$idt  ", s",\n$idt  ", "")}
           |$idt)""".stripMargin
    }
  }

}