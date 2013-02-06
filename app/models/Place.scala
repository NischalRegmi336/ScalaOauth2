package models

import anorm._
import java.util.Date
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current
import anorm.~

/**
 * User: gsd
 * Date: 18.01.13
 * Time: 14:09
 */

case class Place(id: Pk[Long] = NotAssigned, urlKey: String, title: Option[String], page_id: Option[Long])

object Place {

  val simple = {
    get[Pk[Long]]("institution.id") ~
      get[String]("institution.url_key") ~
      get[Option[String]]("institution.title") ~
      get[Option[Long]]("institution.page_id") map {
        case id ~ urlKey ~ title ~ page_id =>
          Place(id, urlKey, title, page_id)
      }
  }

  /* mapping place and page */

  val withPage = Place.simple ~ (Page_model.simple ?) map {
    case place ~ page => (place, page)
  }

  def list(page: Int = 0, pageSize: Int = 20, orderBy: Int = 3, filter: String = "%"): InfoPage[(Place, Option[Page_model])] = {

    val offest = pageSize * page

    DB.withConnection {
      implicit connection =>

        val places = SQL(
          """
	          select * from institution left join pages on institution.page_id=pages.id
	          where institution.title like {filter}
	          order by {orderBy}
	          limit {pageSize} offset {offset}
					""").on(
            'pageSize -> pageSize,
            'offset -> offest,
            'filter -> filter,
            'orderBy -> orderBy).as(Place.withPage *)

        val totalRows = SQL(
          """
	          select count(*) from institution
	          where institution.title like {filter}
					""").on(
            'filter -> filter).as(scalar[Long].single)

        InfoPage(places, page, offest, totalRows)

    }

  }

  def findById(id: Long): Option[Place] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from institution where id = {id}").on('id -> id).as(Place.simple.singleOpt)
    }
  }

  def update(id: Long, place: Place) = {

    DB.withConnection {
      implicit connection =>
        SQL(
          """
          update institution
        		set title = {title},
				    url_key={urlKey},
        			page_id={page_id}
        			
          where id = {id}
					""").on(
            'id -> id,
            'title -> place.title,
            'urlKey -> place.urlKey,
            'page_id -> place.page_id).executeUpdate()
    }

  }

  def insert(place: Place) = {
    DB.withConnection {
      implicit connection =>
        SQL(
          """
	  insert into institution(url_key, title, page_id) values (
	    {url_key}, {title}, {page_id}
	  )
					""").on(
            'url_key -> place.urlKey,
            'title -> place.title,
            'page_id -> place.page_id).executeUpdate()
    }
  }

  def delete(id: Long) = {
    DB.withConnection {
      implicit connection =>
        SQL("delete from institution where id = {id}").on('id -> id).executeUpdate()

    }
  }
}