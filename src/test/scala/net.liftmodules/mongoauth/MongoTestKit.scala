

package net.liftmodules.mongoauth


import net.liftweb._
import mongodb._

import com.mongodb.{Mongo, ServerAddress}
import util.Props
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, WordSpec}



object MongoTestDb  {
  //  def dbName = "test_" + this.getClass.getName
  //    .replace(".", "_")
  //    .toLowerCase

  def dbName = "test_mongoauth"

  def defaultServer = new ServerAddress(
    Props.get("mongo.address") openOr "localhost",
    Props.getInt("mongo.port") openOr 12345)

  // If you need more than one db, override this
  def dbs: List[(MongoIdentifier, ServerAddress, String)] = List((DefaultMongoIdentifier, defaultServer, dbName))

  def debug = false

  def beforeAllSuite() {
    // define the dbs
    dbs foreach {
      case (id, srvr, name) =>
        MongoDB.defineDb(id, new Mongo(srvr), name)
    }
  }

  def afterAllSuite() {
    if (!debug) {
      // drop the databases
      dbs foreach {
        case (id, _, _) =>
          MongoDB.use(id) {
            db => db.dropDatabase
          }
      }
    }
    // clear the mongo instances
    MongoDB.close
  }

  def dropCollection() {
    MongoTestDb.dbs foreach {
      case (id, _, _) =>
        MongoDB.use(id) {
          db => {
            val cs =  db.getCollectionNames.toArray.map{c => {
              c + ""
            }}.filter((c => c != "system.indexes"))
            cs.foreach(c => db.getCollection(c).drop())
          }
        }
    }
  }


  def init() {}

  beforeAllSuite()

}

trait MongoTestKit extends BeforeAndAfterAll {
  this: WordSpec =>

  def dbName = "test_" + this.getClass.getName
    .replace(".", "_")
    .toLowerCase

  def defaultServer = new ServerAddress(
    Props.get("mongo.address") openOr "localhost",
    Props.getInt("mongo.port") openOr 12345)

  // If you need more than one db, override this
  //def dbs: List[(MongoIdentifier, ServerAddress, String)] = List((DefaultMongoIdentifier, defaultServer, dbName))

  def debug = false

  override def beforeAll(configMap: Map[String, Any]) {
    MongoTestDb.init()
  }

  override def afterAll(configMap: Map[String, Any]) {
    if (!debug) {
      // drop collections
      MongoTestDb.dropCollection()
    }


  }
}


class IntegrationTestCleanup {
  MongoTestDb.afterAllSuite()
}


//
///**
// * Creates a Mongo instance named after the class.
// * Therefore, each Spec class shares the same database.
// * Database is dropped after.
// */
//trait MongoTestKit extends BeforeAndAfter {
//  this: WordSpec =>
//
//  def dbName = "test_"+this.getClass.getName
//    .replace(".", "_")
//    .toLowerCase
//
//  def defaultServer = new ServerAddress("127.0.0.1", 12345)
//
//  // If you need more than one db, override this
//  def dbs: List[(MongoIdentifier, ServerAddress, String)] = List((DefaultMongoIdentifier, defaultServer, dbName))
//
//  def debug = false
//
//  before {
//    // define the dbs
//    dbs foreach { case (id, srvr, name) =>
//      MongoDB.defineDb(id, new Mongo(srvr), name)
//    }
//  }
//
//    after {
//    if (!debug) {
//      // drop the databases
//      dbs foreach { case (id, _, _) =>
//        MongoDB.use(id) { db => db.dropDatabase }
//      }
//    }
//
//    // clear the mongo instances
//    MongoDB.close
//  }
//}
//
