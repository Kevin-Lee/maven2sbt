package maven2sbt.core

import cats.syntax.all._
import hedgehog._
import hedgehog.runner._

/**
 * @author Kevin Lee
 * @since 2020-09-05
 */
object BuildSbtSpec extends Properties {
  override def tests: List[Test] = List(
    property(
      "[Render][Repository] test BuildSbt.renderListOfFieldValue(None, List.empty[Repository], n)",
      RenderRepositorySpec.testRenderToResolvers0
    ),
    property(
      "[Render][Repository] test BuildSbt.renderListOfFieldValue(None, List(repository), n)",
      RenderRepositorySpec.testRenderToResolvers1
    ),
    property(
      "[Render][Repository] test BuildSbt.renderListOfFieldValue(None, List(repository with empty name), n)",
      RenderRepositorySpec.testRenderToResolvers1WithEmptyName
    ),
    property(
      "[Render][Repository] test BuildSbt.renderListOfFieldValue(None, List(repository with no name (None)), n)",
      RenderRepositorySpec.testRenderToResolvers1WithNoName
    ),
    property(
      "[Render][Repository] test BuildSbt.renderListOfFieldValue(None, List(repository with empty id and empty name), n)",
      RenderRepositorySpec.testRenderToResolvers1WithEmptyIdEmptyName
    ),
    property(
      "[Render][Repository] test BuildSbt.renderListOfFieldValue(None, List(repository with no id (None) and no name (None)), n)",
      RenderRepositorySpec.testRenderToResolvers1WithNoIdNoName
    ),
    property(
      "[Render][Repository] test BuildSbt.renderListOfFieldValue(None, List(repository1, repository2, ...), n)",
      RenderRepositorySpec.testRenderToResolversMany
    ),
    property(
      "[Render][Repository] test BuildSbt.renderListOfFieldValue(None, List(repository1, repository2, ... which may have empty names), n)",
      RenderRepositorySpec.testRenderToResolversManyWithEmptyRepoNames
    ),
    property(
      "[Render][Repository] test BuildSbt.renderListOfFieldValue(None, List(repository1, repository2, ... which may have empty id and empty names), n)",
      RenderRepositorySpec.testRenderToResolversManyWithEmptyRepoIdEmptyRepoNames
    ),
    property(
      "[Render][Repository] test BuildSbt.renderListOfFieldValue(None, List(repository1, repository2, ... which may have no names), n)",
      RenderRepositorySpec.testRenderToResolversManyWithNoRepoNames
    ),
    property(
      "[Render][Repository] test BuildSbt.renderListOfFieldValue(None, List(repository1, repository2, ... which may have no id and no names), n)",
      RenderRepositorySpec.testRenderToResolversManyWithNoRepoIdNoRepoNames
    ),
    property(
      "[Render][Dependency] test BuildSbt.renderListOfFieldValue(None, List.empty[Dependency], n)",
      RenderDependencySpec.testRenderLibraryDependenciesEmpty
    ),
    property(
      "[Render][Dependency] test BuildSbt.renderListOfFieldValue(None, List(dependency), n)",
      RenderDependencySpec.testRenderLibraryDependencies1
    ),
    property(
      "[Render][Dependency] test BuildSbt.renderListOfFieldValue(None, List(dependency1, dependency2, ...), n)",
      RenderDependencySpec.testRenderLibraryDependenciesMany
    )
  )


  object RenderRepositorySpec {

    def testRenderToResolvers0: Property = for {
      n <- Gen.int(Range.linear(0, 10)).log("n")
    } yield {
      val expected = none[String]
      val propsName = Props.PropsName("testProps")
      val actual = BuildSbt.renderListOfFieldValue(
          none[String],
          List.empty[Repository],
          n
        )(repo => Render[Repository].render(propsName, repo))
      actual ==== expected
    }


    def testRenderToResolvers1: Property = for {
      n <- Gen.int(Range.linear(0, 10)).log("n")
      repository <- Gens.genRepository.log("repository")
    } yield {
      (repository.id, repository.name) match {
        case (_, Some(repoName)) =>
          val propsName = Props.PropsName("testProps")
          val expected = s"""resolvers += "${repoName.repoName}" at "${repository.url.repoUrl}"""".some
          val actual = BuildSbt.renderListOfFieldValue(
              none[String],
              List(repository),
              n
            )(repo => Render[Repository].render(propsName, repo))
          actual ==== expected

        case (Some(_), None) =>
          Result.failure.log(
            s"""> Repository generated by Gens.genRepository has no name.
               |> If you see this message, it means there is a bug in Gens.genRepository.
               |> repository: ${repository.show}
               |""".stripMargin)
        case (None, None) =>
          Result.failure.log(
            s"""> Repository generated by Gens.genRepository has neither id nor name.
               |> If you see this message, it means there is a bug in Gens.genRepository.
               |> repository: ${repository.show}
               |""".stripMargin)
      }
    }

    def testRenderToResolvers1WithEmptyName: Property = for {
      n <- Gen.int(Range.linear(0, 10)).log("n")
      repository <- Gens.genRepositoryWithEmptyName.log("repository")
    } yield {
      (repository.id, repository.name) match {
        case (Some(repoId), Some(Repository.RepoName(""))) =>
          val propsName = Props.PropsName("testProps")
          val expected = s"""resolvers += "${repoId.repoId}" at "${repository.url.repoUrl}"""".some
          val actual = BuildSbt.renderListOfFieldValue(
              none[String],
              List(repository),
              n
            )(repo => Render[Repository].render(propsName, repo))
          actual ==== expected
        case (repoId, repoNmae) =>
          Result.failure.log(
            s"""> Repository generated by Gens.genRepositoryWithEmptyName is supposed to have id and an empty name
               |> but it has something else. repoId: ${repoId.map(_.show).show}, repoNmae: ${repoNmae.map(_.show).show}
               |> If you see this message, it means there is a bug in Gens.genRepositoryWithEmptyName or using a wrong Gen.
               |> repository: ${repository.show}
               |""".stripMargin)
      }
    }

    def testRenderToResolvers1WithNoName: Property = for {
      n <- Gen.int(Range.linear(0, 10)).log("n")
      repository <- Gens.genRepositoryWithNoName.log("repository")
    } yield {
      (repository.id, repository.name) match {
        case (Some(repoId), None) =>
          val propsName = Props.PropsName("testProps")
          val expected = s"""resolvers += "${repoId.repoId}" at "${repository.url.repoUrl}"""".some
          val actual = BuildSbt.renderListOfFieldValue(
              none[String],
              List(repository),
              n
            )(repo => Render[Repository].render(propsName, repo))
          actual ==== expected
        case (repoId, repoNmae) =>
          Result.failure.log(
            s"""> Repository generated by Gens.genRepositoryWithNoName is supposed to have id and no name (None)
               |> but it has something else. repoId: ${repoId.map(_.show).show}, repoNmae: ${repoNmae.map(_.show).show}
               |> If you see this message, it means there is a bug in Gens.genRepositoryWithNoName or using a wrong Gen.
               |> repository: ${repository.show}
               |""".stripMargin)
      }
    }

    def testRenderToResolvers1WithEmptyIdEmptyName: Property = for {
      n <- Gen.int(Range.linear(0, 10)).log("n")
      repository <- Gens.genRepositoryWithEmptyIdEmptyName.log("repository")
    } yield {
      (repository.id, repository.name) match {
        case (Some(Repository.RepoId("")), Some(Repository.RepoName(""))) =>
          val propsName = Props.PropsName("testProps")
          val expected = s"""resolvers += "${repository.url.repoUrl}" at "${repository.url.repoUrl}"""".some
          val actual = BuildSbt.renderListOfFieldValue(
              none[String],
              List(repository),
              n
            )(repo => Render[Repository].render(propsName, repo))
          actual ==== expected
        case (repoId, repoNmae) =>
          Result.failure.log(
            s"""> Repository generated by Gens.genRepositoryWithEmptyIdEmptyName is supposed to have an empty id and an empty name
               |> but it has something else. repoId: ${repoId.map(_.show).show}, repoNmae: ${repoNmae.map(_.show).show}
               |> If you see this message, it means there is a bug in Gens.genRepositoryWithEmptyIdEmptyName or using a wrong Gen.
               |> repository: ${repository.show}
               |""".stripMargin)
      }
    }

    def testRenderToResolvers1WithNoIdNoName: Property = for {
      n <- Gen.int(Range.linear(0, 10)).log("n")
      repository <- Gens.genRepositoryWithNoIdNoName.log("repository")
    } yield {
      (repository.id, repository.name) match {
        case (None, None) =>
          val propsName = Props.PropsName("testProps")
          val expected = s"""resolvers += "${repository.url.repoUrl}" at "${repository.url.repoUrl}"""".some
          val actual = BuildSbt.renderListOfFieldValue(
              none[String],
              List(repository),
              n
            )(repo => Render[Repository].render(propsName, repo))
          actual ==== expected
        case (repoId, repoNmae) =>
          Result.failure.log(
            s"""> Repository generated by Gens.genRepositoryWithNoIdNoName is supposed to have neither id nor name (None for both)
               |> but it has something else. repoId: ${repoId.map(_.show).show}, repoNmae: ${repoNmae.map(_.show).show}
               |> If you see this message, it means there is a bug in Gens.genRepositoryWithNoIdNoName or using a wrong Gen.
               |> repository: ${repository.show}
               |""".stripMargin)
      }
    }

    def testRenderToResolversMany: Property = for {
      n <- Gen.int(Range.linear(0, 10)).log("n")
      repositories <- Gens.genRepository.list(Range.linear(2, 10)).log("repositories")
    } yield {
      val propsName = Props.PropsName("testProps")
      val idt = StringUtils.indent(n)
      val expected =
        s"""resolvers ++= List(
           |$idt${repositories.map(repo => Repository.render(propsName, repo).toQuotedString).stringsMkString("  ", s",\n$idt  ", "")}
           |$idt)""".stripMargin.some
      val actual = BuildSbt.renderListOfFieldValue(
          none[String],
          repositories,
          n
        )(repo => Render[Repository].render(propsName, repo))
      actual ==== expected
    }

    def testRenderToResolversManyWithEmptyRepoNames: Property = for {
      n <- Gen.int(Range.linear(0, 10)).log("n")
      repositories <- Gens.genRepository.list(Range.linear(2, 10)).log("repositories")
      repositoriesWithEmptyNames <- Gens.genRepositoryWithEmptyName.list(Range.linear(2, 10)).log("repositoriesWithEmptyNames")
    } yield {
      val propsName = Props.PropsName("testProps")
      val idt = StringUtils.indent(n)
      val expected =
        s"""resolvers ++= List(
           |$idt${repositories.map(repo => Repository.render(propsName, repo).toQuotedString).stringsMkString("  ", s",\n$idt  ", ",")}
           |$idt${repositoriesWithEmptyNames.map(repo => Repository.render(propsName, repo).toQuotedString).stringsMkString("  ", s",\n$idt  ", "")}
           |$idt)""".stripMargin.some
      val input = repositories ++ repositoriesWithEmptyNames
      val actual = BuildSbt.renderListOfFieldValue(
          none[String],
          input,
          n
        )(repo => Render[Repository].render(propsName, repo))
      actual ==== expected
    }

    def testRenderToResolversManyWithEmptyRepoIdEmptyRepoNames: Property = for {
      n <- Gen.int(Range.linear(0, 10)).log("n")
      repositories <- Gens.genRepository.list(Range.linear(2, 10)).log("repositories")
      repositoriesWithEmptyNames <- Gens.genRepositoryWithEmptyIdEmptyName.list(Range.linear(2, 10)).log("repositoriesWithEmptyNames")
    } yield {
      val propsName = Props.PropsName("testProps")
      val idt = StringUtils.indent(n)
      val expected =
        s"""resolvers ++= List(
           |$idt${repositories.map(repo => Repository.render(propsName, repo).toQuotedString).stringsMkString("  ", s",\n$idt  ", ",")}
           |$idt${repositoriesWithEmptyNames.map(repo => Repository.render(propsName, repo).toQuotedString).stringsMkString("  ", s",\n$idt  ", "")}
           |$idt)""".stripMargin.some
      val input = repositories ++ repositoriesWithEmptyNames
      val actual = BuildSbt.renderListOfFieldValue(
          none[String],
          input,
          n
        )(repo => Render[Repository].render(propsName, repo))
      actual ==== expected
    }

    def testRenderToResolversManyWithNoRepoNames: Property = for {
      n <- Gen.int(Range.linear(0, 10)).log("n")
      repositories <- Gens.genRepository.list(Range.linear(2, 10)).log("repositories")
      repositoriesWithEmptyNames <- Gens.genRepositoryWithNoName.list(Range.linear(2, 10)).log("repositoriesWithEmptyNames")
    } yield {
      val propsName = Props.PropsName("testProps")
      val idt = StringUtils.indent(n)
      val expected =
        s"""resolvers ++= List(
           |$idt${repositories.map(repo => Repository.render(propsName, repo).toQuotedString).stringsMkString("  ", s",\n$idt  ", ",")}
           |$idt${repositoriesWithEmptyNames.map(repo => Repository.render(propsName, repo).toQuotedString).stringsMkString("  ", s",\n$idt  ", "")}
           |$idt)""".stripMargin.some
      val input = repositories ++ repositoriesWithEmptyNames
      val actual = BuildSbt.renderListOfFieldValue(
          none[String],
          input,
          n
        )(repo => Render[Repository].render(propsName, repo))
      actual ==== expected
    }

    def testRenderToResolversManyWithNoRepoIdNoRepoNames: Property = for {
      n <- Gen.int(Range.linear(0, 10)).log("n")
      repositories <- Gens.genRepository.list(Range.linear(2, 10)).log("repositories")
      repositoriesWithEmptyNames <- Gens.genRepositoryWithNoIdNoName.list(Range.linear(2, 10)).log("repositoriesWithEmptyNames")
    } yield {
      val propsName = Props.PropsName("testProps")
      val idt = StringUtils.indent(n)
      val expected =
        s"""resolvers ++= List(
           |$idt${repositories.map(repo => Repository.render(propsName, repo).toQuotedString).stringsMkString("  ", s",\n$idt  ", ",")}
           |$idt${repositoriesWithEmptyNames.map(repo => Repository.render(propsName, repo).toQuotedString).stringsMkString("  ", s",\n$idt  ", "")}
           |$idt)""".stripMargin.some
      val input = repositories ++ repositoriesWithEmptyNames
      val actual = BuildSbt.renderListOfFieldValue(
          none[String],
          input,
          n
        )(repo => Render[Repository].render(propsName, repo))
      actual ==== expected
    }

  }

  object RenderDependencySpec {

    def testRenderLibraryDependenciesEmpty: Property = for {
      n <- Gen.int(Range.linear(0, 10)).log("n")
    } yield {
      val propsName = Props.PropsName("testProps")
      val libs = Libs(List.empty[(Libs.LibValName, Dependency)])
      val expected = none[String]
      val actual = BuildSbt.renderListOfFieldValue(
          none[String],
          List.empty[Dependency],
          n
        )(dep => ReferencedRender[Dependency].render(propsName, libs, dep))
      actual ==== expected
    }

    def testRenderLibraryDependencies1: Property = for {
      n <- Gen.int(Range.linear(0, 10)).log("n")
      dependency <- Gens.genDependency.log("dependency")
    } yield {
      val propsName = Props.PropsName("testProps")
      val libs = Libs(List.empty[(Libs.LibValName, Dependency)])
      val expected = s"libraryDependencies += ${Dependency.render(propsName, libs, dependency).toQuotedString}".some
      val actual = BuildSbt.renderListOfFieldValue(
          none[String],
          List(dependency),
          n
        )(dep => ReferencedRender[Dependency].render(propsName, libs, dep))
      actual ==== expected
    }

    def testRenderLibraryDependenciesMany: Property = for {
      n <- Gen.int(Range.linear(0, 10)).log("n")
      libraryDependencies <- Gens.genDependency.list(Range.linear(2, 10)).log("libraryDependencies")
    } yield {
      val propsName = Props.PropsName("testProps")
      val libs = Libs(List.empty[(Libs.LibValName, Dependency)])
      val idt = StringUtils.indent(n)
      val expected =
        s"""libraryDependencies ++= List(
           |$idt${libraryDependencies.map(dep => Dependency.render(propsName, libs, dep).toQuotedString).stringsMkString("  ", s",\n$idt  ", "")}
           |$idt)""".stripMargin.some
      val actual = BuildSbt.renderListOfFieldValue(
          none[String],
          libraryDependencies,
          n
        )(dep => ReferencedRender[Dependency].render(propsName, libs, dep))
      actual ==== expected
    }

  }

}
