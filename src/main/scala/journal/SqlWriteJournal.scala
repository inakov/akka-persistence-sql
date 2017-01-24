package journal

import akka.actor.ActorSystem
import akka.persistence.{AtomicWrite, PersistentRepr}
import akka.persistence.journal.AsyncWriteJournal
import akka.serialization.{Serialization, SerializationExtension}
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.Source
import database.SqlPersistenceExtension

import scala.collection.immutable.Seq
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try

/**
  * Created by inakov on 22.01.17.
  */
class SqlWriteJournal extends AsyncWriteJournal{

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val system: ActorSystem = context.system
  implicit val mat: Materializer = ActorMaterializer()

  private val sqlPersistenceExtension = SqlPersistenceExtension(context.system)

  private val journalRepository: JournalRepository = sqlPersistenceExtension.journalRepository
  private val persistenceKeyRepository: PersistenceKeyRepository = sqlPersistenceExtension.persistenceKeyRepository

  private val serialization: Serialization = SerializationExtension(context.system)

  override def asyncWriteMessages(messages: Seq[AtomicWrite]): Future[Seq[Try[Unit]]] = {
    val batches = for(atomicWrite <- messages) yield writeAtomicBatch(atomicWrite)
    Future.sequence(batches)
  }

  def writeAtomicBatch(atomicWrite: AtomicWrite): Future[Try[Unit]] = {
    persistenceKeyRepository.saveOrLoadKey(atomicWrite.persistenceId).map{ persistenceKey =>
      Try(atomicWrite.payload.map(repr => createEventRecord(persistenceKey, repr)).map(_.get)).map{ events =>
        persistBatch(events)
      }
    }
  }

  private def createEventRecord(persistenceKey: Long, persistentRepr: PersistentRepr): Try[EventRecord] = {
    serialization.serialize(persistentRepr).map{ content =>
      EventRecord(persistenceKey, persistentRepr.sequenceNr, content, None)
    }
  }

  private def persistBatch(events: Seq[EventRecord]): Future[Try[Unit]] = {
    val persistenceResult = journalRepository.save(events).map(_ => ())
    val result = Promise[Try[Unit]]()
    persistenceResult.onComplete(result.success)
    result.future
  }

  override def asyncDeleteMessagesTo(persistenceId: String, toSequenceNr: Long): Future[Unit] = {
    for {
      persistenceKey <- persistenceKeyRepository.saveOrLoadKey(persistenceId)
      _ <- journalRepository.delete(persistenceKey, toSequenceNr)
    } yield ()
  }

  override def asyncReplayMessages(persistenceId: String, fromSequenceNr: Long, toSequenceNr: Long,
                                   max: Long)(recoveryCallback: (PersistentRepr) => Unit): Future[Unit] = {
    persistenceKeyRepository.saveOrLoadKey(persistenceId).map{ persistenceKey =>
      Source.fromPublisher(journalRepository.eventStream(persistenceKey, fromSequenceNr, toSequenceNr, max))
        .map(event => serialization.deserialize(event.content, classOf[PersistentRepr]).get)
        .runFold(0){ (count, event) =>
          recoveryCallback(event)
          count + 1
        }.map(count => ())
    }
  }


  override def asyncReadHighestSequenceNr(persistenceId: String, fromSequenceNr: Long): Future[Long] = {
    for{
      persistenceKey <- persistenceKeyRepository.saveOrLoadKey(persistenceId)
      seqNr <- journalRepository.loadHighestSequenceNr(persistenceKey, fromSequenceNr)
    } yield seqNr.getOrElse(0L)
  }
}
