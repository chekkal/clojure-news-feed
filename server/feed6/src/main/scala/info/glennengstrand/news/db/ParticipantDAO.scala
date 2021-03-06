package info.glennengstrand.news.db;

import doobie._
import doobie.implicits._
import cats._
import cats.effect._
import cats.data._
import cats.implicits._
import info.glennengstrand.news.model.Participant
import info.glennengstrand.news.core.EntityDAO

class ParticipantDAO extends EntityDAO[Participant] {
  def get(id: Long)(implicit db: Transactor[IO]): Option[Participant] = {
    val result = sql"call FetchParticipant(${id})"
      .query[String]
      .to[List]
      .transact(db)
      .unsafeRunSync
      .take(1)
    if (result.size > 0) {
      Option(Participant(Option(id), Option(result.head)))
    } else {
      None
    }
  }
  def gets(id: Long)(implicit db: Transactor[IO]): List[Participant] = {
    List()
  }
  def add(participant: Participant)(implicit db: Transactor[IO]): Participant = {
    participant.name match {
      case Some(moniker) => {
        val result = sql"call UpsertParticipant(${moniker})"
          .query[Long]
          .to[List]
          .transact(db)
          .unsafeRunSync
          .take(1)
        Participant(Option(result.head), participant.name)
      }
      case None => participant
    }
  }
}
class MockParticipantDAO extends EntityDAO[Participant] {
  def get(id: Long)(implicit db: Transactor[IO]): Option[Participant] = {
    Option(Participant(Option(id), Option("test")))
  }
  def gets(id: Long)(implicit db: Transactor[IO]): List[Participant] = {
    List()
  }
  def add(participant: Participant)(implicit db: Transactor[IO]): Participant = {
    participant
  }
}

