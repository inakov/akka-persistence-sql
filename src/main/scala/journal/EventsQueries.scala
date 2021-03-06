package journal

import java.sql.Timestamp

import database.DbComponent

/**
  * Created by inakov on 21.01.17.
  */
private[journal] trait EventsQueries {
  this: DbComponent =>

  import config.profile.api._

  private[EventsQueries] class EventsTable(tag: Tag) extends Table[EventRecord](tag, "events_journal"){
    def persistenceKey = column[String]("persistence_id")
    def sequenceNumber = column[Long]("sequence_nr")
    def content = column[Array[Byte]]("content")
    def created = column[Timestamp]("created")
    def removed = column[Boolean]("removed")
    val pk = primaryKey("events_pk", (persistenceKey, sequenceNumber))

    def * = (persistenceKey, sequenceNumber, content, created.?, removed) <> (EventRecord.tupled, EventRecord.unapply)
  }

  private val eventsJournal = TableQuery[EventsTable]

  protected def insertEvents(events: Seq[EventRecord]) = eventsJournal ++= events.sortBy(_.sequenceNumber)

  protected def removeEvents(persistenceKey: String, toSeqNr: Long) =
    eventsJournal
      .filter(_.persistenceKey === persistenceKey)
      .filter(_.sequenceNumber <= toSeqNr)
      .filter(_.removed === false)
      .map(_.removed).update(true)

  protected def selectEvents(persistenceKey: String) =
    eventsJournal
      .filter(_.persistenceKey === persistenceKey)
      .sortBy(_.sequenceNumber.desc)

  protected def selectEvents(persistenceKey: String, fromSeqNr: Long, toSeqNr: Long, maxSize: Long) =
    eventsJournal
      .filter(_.persistenceKey === persistenceKey)
      .filter(_.sequenceNumber >= fromSeqNr)
      .filter(_.sequenceNumber <= toSeqNr)
      .filter(_.removed === false)
      .take(maxSize)

  protected def highestSeqNum(persistenceKey: String) =
    eventsJournal
      .filter(_.persistenceKey === persistenceKey)
      .sortBy(_.sequenceNumber.desc)
      .map(_.sequenceNumber).take(1)


}

case class EventRecord(persistenceKey: String, sequenceNumber: Long, content: Array[Byte],
                       created: Option[Timestamp], removed: Boolean = false)