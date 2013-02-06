package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import anorm._

import views._
import models._
import securesocial.core.{Identity, Authorization}
import play.Logger


/**
 * Manage a database of pages
 */
object Page extends Controller with securesocial.core.SecureSocial {

	/**
	 * This result directly redirect to the wifiadmin home.
	 */
	val Home = Redirect(routes.Page.list(0, 1, ""))
	/**
	 * Describe the page form (used in both edit and create screens).
	 */
	val pageForm = Form(
		mapping(
			"id" -> ignored(NotAssigned: Pk[Long]),
			"title" -> nonEmptyText,
			"template" -> nonEmptyText,
			"options" -> optional(longNumber)
		)(models.Page_model.apply)(models.Page_model.unapply)
	)

	// -- Actions

	/**
	 * Handle default path requests, redirect to pages list
	 */
	def index = Action {
		Home
	}

	/**
	 * Display the paginated list of pages.
	 *
	 * @param page Current page number (starts from 0)
	 * @param orderBy Column to be sorted
	 * @param filter Filter applied on page names
	 */
	def list(page: Int, orderBy: Int, filter: String) = SecuredAction {
		implicit request =>
			Ok(views.html.listPage(models.Page_model.list(page = page, orderBy = orderBy, filter = ("%" + filter + "%")),
			    orderBy, filter, request.user))
	}

	/**
	 * Display the 'edit form' of a existing page.
	 *
	 * @param id Id of the page to edit
	 */
	def edit(id: Long) = Action {
		models.Page_model.findById(id).map {
			page =>
				Ok(html.editPageForm(id, pageForm.fill(page)))
		}.getOrElse(NotFound)
	}

	/**
	 * Handle the 'edit form' submission
	 *
	 * @param id Id of the page to edit
	 */
	def update(id: Long) = Action {
		implicit request =>
			pageForm.bindFromRequest.fold(
				formWithErrors => BadRequest(views.html.createPageForm(formWithErrors)),
				page => {
					models.Page_model.update(id, page)
					Home.flashing("success" -> "page %s has been updated")
				}
			)
	}

	/**
	 * Display the 'new page form'.
	 */
	def create = Action {
		Ok(html.createPageForm(pageForm))
	}

	/**
	 * Handle the 'new page form' submission.
	 */
	def save = Action {
		implicit request =>
			pageForm.bindFromRequest.fold(
				formWithErrors => BadRequest(html.createPageForm(formWithErrors)),
				page => {
					models.Page_model.insert(page)
					Home.flashing("success" -> "page %s has been created")
				}
			)
	}

	/**
	 * Handle page deletion.
	 */
	def delete(id: Long) = Action {
		models.Page_model.delete(id)
		Home.flashing("success" -> "page has been deleted")
	}

}
            