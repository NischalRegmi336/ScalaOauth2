package models

import anorm._
import java.util.Date
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current
import anorm.~




//case class Place(id: Pk[Long] = NotAssigned, urlKey: String, title: Option[String])
case class Page_model(id: Pk[Long] = NotAssigned, 
    			title: String, 
    			template: String, 
    			options: Option[Long])
    			
object Page_model {

	val simple = {
			get[Pk[Long]]("pages.id") ~
			get[String]("pages.title") ~
            get[String]("pages.template") ~
			get[Option[Long]]("pages.options") map {
                            case id~title~template~options
                                => Page_model(id, title, template, options)
                        }
	}
	


	def list(page: Int = 0, pageSize: Int = 20, orderBy: Int = 3, filter: String = "%"): InfoPage[Page_model] = {

		val offest = pageSize * page

		DB.withConnection {
			implicit connection =>

				val pages = SQL(
					"""
	          select * from pages
	          where title like {filter}
	          order by {orderBy} 
	          limit {pageSize} offset {offset}
					"""
				).on(
					'pageSize -> pageSize,
					'offset -> offest,
					'filter -> filter,
					'orderBy -> orderBy
				).as(Page_model.simple *)

				val totalRows = SQL(
					"""
	          select count(*) from pages
	          where title like {filter}
					"""
				).on(
					'filter -> filter
				).as(scalar[Long].single)

				InfoPage(pages, page, offest, totalRows)

		}

	}

	//def findById(id: Long): Option[Page_model] = {
	//	DB.withConnection {
	//		implicit connection =>
	//			SQL("select * from pages where id = 1").as(Page_model*);
	//	}
	//}

        def findById(id: Long): Option[Page_model] = {
		DB.withConnection {
			implicit connection =>
				SQL("select * from pages where id = {id}").on('id -> id).as(Page_model.simple.singleOpt)
		}
	}

	def update(id: Long, page: Page_model) = {
	  
	  DB.withConnection {
			implicit connection =>
				SQL(
					"""
          update pages
          set title = {title},
				    template={template},
				    options={options}
          where id = {id}
					"""
				).on(
					'id -> id,
					'title -> page.title,
					'template -> page.template,
					'options -> page.options
				).executeUpdate()
		}
		
	}

	def insert(page: Page_model) = {
		DB.withConnection {
			implicit connection =>
				SQL(
					"""
	  insert into pages(title, template, options) values (
	    {title}, {template}, {options}
	  )
					"""
				).on(
					'title -> page.title,
					'template -> page.template,
					'options -> page.options
				).executeUpdate()
		}
	}

	def delete(id: Long) = {
		DB.withConnection {
			implicit connection =>
				SQL("delete from pages where id = {id}").on('id -> id).executeUpdate()
				
		}
	}
}