package spendthrift.adapters.repositories.sql

import cats.implicits.*

import cats.effect.*

import natchez.Trace.Implicits.noop

import skunk.*
import skunk.implicits.*
import skunk.util.*

import munit.*

abstract class SqlSuite extends CatsEffectSuite {

  private[this] var _close: IO[Unit]      = _
  private[this] var _session: Session[IO] = _

  def cleanUpTables: List[String]

  override def beforeAll(): Unit = {
    super.beforeAll()

    val (session, closeResource) =
      Session
        .single[IO](
          host = "localhost",
          user = "spendthrift",
          database = "spendthrift",
          password = "spendthrift@dev".some
        )
        .allocated
        .unsafeRunSync()

    _session = session
    _close = closeResource
  }

  override def beforeEach(context: BeforeEach): Unit =
    replicaSessionPool
      .use(session => cleanUpTables.parTraverse_(table => session.execute(sql"DELETE FROM #$table".command)))
      .unsafeRunSync()

  override def afterAll(): Unit = {
    super.afterAll()
    _close.unsafeRunSync()
  }

  final def session: Session[IO] =
    _session

  final def sessionPool: Resource[IO, Session[IO]] =
    Resource.pure[IO, Session[IO]](session)

  final def replicaSessionPool: Resource[IO, Session[IO]] = {
    val setReplicaMode = sql"SET session_replication_role = replica".command
    val setDefaultMode = sql"SET session_replication_role = origin".command
    Resource.make(session.execute(setReplicaMode).as(session))(_.execute(setDefaultMode).void)
  }
}
