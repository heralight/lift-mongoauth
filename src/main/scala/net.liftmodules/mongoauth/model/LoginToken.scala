

package net.liftmodules.mongoauth
package model

import field.ExpiresField

import org.joda.time.Hours

import net.liftweb._
import common._
import http._
import mongodb.record._
import mongodb.record.field._
import record.MandatoryTypedField

import org.bson.types.ObjectId

/**
  * This is a token for automatically logging a user in
  */
class LoginToken extends MongoRecord[LoginToken] with StringPk[LoginToken] {
  def meta = LoginToken

  object userId extends ObjectIdField(this)
  object expires extends ExpiresField(this, meta.loginTokenExpires)

  def url: String = meta.url(this)
}

object LoginToken extends LoginToken with MongoMetaRecord[LoginToken] {
  import mongodb.BsonDSL._

  override val collectionName = "user.logintokens"

  ensureIndex((userId.name -> 1))

  private lazy val loginTokenUrl = MongoAuth.loginTokenUrl.vend
  private lazy val loginTokenExpires = MongoAuth.loginTokenExpires.vend

  def url(inst: LoginToken): String = "%s%s?token=%s".format(S.hostAndPath, loginTokenUrl, inst.id)

  def createForUserId(uid: ObjectId): LoginToken = {
    createRecord.userId(uid).save
  }

  def deleteAllByUserId(uid: ObjectId) {
    delete(userId.name, uid)
  }

  def isValid(s: String, len: Int = 32): Boolean = {
    if (s == null) return false
    val len: Int = s.length
    if (s.length != len) return false

    s.foreach(c => {
      if (!(c >= '0' && c <= '9'))
        if (!(c >= 'a' && c <= 'f'))
          if (!(c >= 'A' && c <= 'F'))
            return false
    })
    true
  }

  def findByStringId(in: String): Box[LoginToken] =
    if (isValid(in)) findByStringId(in)
    else Failure("Invalid Token Id: "+in)
}
