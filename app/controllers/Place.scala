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
 * Manage a database of places
 */
object Place extends Controller with securesocial.core.SecureSocial {

	/**
	 * This result directly redirect to the wifiadmin home.
	 */
	val Home = Redirect(routes.Place.list(0, 1, ""))

	/**
	 * Describe the place form (used in both edit and create screens).
	 */
	val placeForm = Form(
		mapping(
			"id" -> ignored(NotAssigned: Pk[Long]),
			"url_key" -> nonEmptyText,
			"title" -> optional(text),
			"page_id" -> optional(longNumber)
		)(models.Place.apply)(models.Place.unapply)
	)


	// -- Actions

	/**
	 * Handle default path requests, redirect to places list
	 */
	def index = Action {
		Home
	}

	/**
	 * Display the paginated list of places.
	 *
	 * @param page Current page number (starts from 0)
	 * @param orderBy Column to be sorted
	 * @param filter Filter applied on place names
	 */
	def list(page: Int, orderBy: Int, filter: String) = SecuredAction {
		implicit request =>
			
			Ok(views.html.listPlace(models.Place.list(page = page, orderBy = orderBy, filter = ("%" + filter + "%")),
			    orderBy, filter, request.user))
	}

	/**
	 * Display the 'edit form' of a existing place.
	 *
	 * @param id Id of the place to edit
	 */
	def edit(id: Long) = Action {
		models.Place.findById(id).map {
			place =>
				Ok(html.editPlaceForm(id, placeForm.fill(place)))
		}.getOrElse(NotFound)
	}

	/**
	 * Handle the 'edit form' submission
	 *
	 * @param id Id of the place to edit
	 */
	def update(id: Long) = Action {
		implicit request =>
			placeForm.bindFromRequest.fold(
				formWithErrors => BadRequest(html.createPlaceForm(formWithErrors)),
				place => {
					models.Place.update(id, place)
					Home.flashing("success" -> "Place %s has been updated".format(place.urlKey))
				}
			)
	}

	/**
	 * Display the 'new place form'.
	 */
	def create = Action {
		
		Ok(html.createPlaceForm(placeForm))
	}

	/**
	 * Handle the 'new place form' submission.
	 */
	def save = Action {
		implicit request =>
			placeForm.bindFromRequest.fold(
				formWithErrors => BadRequest(html.createPlaceForm(formWithErrors)),
				place => {
					models.Place.insert(place)
					Home.flashing("success" -> "Place %s has been created".format(place.urlKey))
				}
			)
	}

	/**
	 * Handle place deletion.
	 */
	def delete(id: Long) = Action {
		models.Place.delete(id)
		Home.flashing("success" -> "place has been deleted")
	}

}
            