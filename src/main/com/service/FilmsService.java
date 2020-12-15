package main.com.service;

import java.util.List;

import org.hibernate.HibernateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import main.com.model.Film;
import main.com.model.Films;
import main.com.utils.DataUtils;
import main.com.utils.FilmDatabaseUtils;

public class FilmsService {

	final FilmDatabaseUtils filmDbUtils = new FilmDatabaseUtils();
	DataUtils dataUtils = new DataUtils();

	public ResponseEntity<?> getAllFilmsService(String contentType) {
		List<Film> listOfFilmsReturnedByDb = filmDbUtils.getAllFilms();

		System.out.println("--------------------");
		System.out.println("Request received to retrieve all films in the database in '" + contentType + "'.");

		if (listOfFilmsReturnedByDb.isEmpty()) {
			System.out.println("No movies found in the database.");
			return new ResponseEntity<String>("No movies found in the database.", HttpStatus.NO_CONTENT);
		}

		Films filmsResult = new Films();
		filmsResult.setFilmList(listOfFilmsReturnedByDb);

		return dataUtils.convertFilmsForClientContentType(contentType, listOfFilmsReturnedByDb, filmsResult);
	}

	public ResponseEntity<?> getFilmByIdService(String contentType, String filmId) {

		System.out.println("--------------------");
		System.out.println(String.format("Request recieved to GET data by ID: '%s' in format '%s'", filmId, contentType));

		if (!dataUtils.isValidFilmId(filmId)) {
			return dataUtils.failedRequestErrorMessage("No movie found due to invalid Film ID: " + filmId);

		}

		Film film = filmDbUtils.getFilmById(Integer.parseInt(filmId));

		if (film == null) {
			return dataUtils.failedRequestErrorMessage("No movie found for film ID: " + filmId);
		}
		
		System.out.println("Successfully found '" + film.getTitle() + "' to be returned to client.");
		System.out.println("--------------------");

		if (contentType.equalsIgnoreCase("text/plain")) {
			return new ResponseEntity<String>(film.toString(), HttpStatus.OK);
		} else {
			return new ResponseEntity<Film>(film, HttpStatus.OK);
		}

	}

	
public ResponseEntity<?> getFilmsByTitleService(String contentType, String filmTitle) {

		System.out.println("--------------------");
		System.out.println(
				String.format("Request recieved to GET data by title: '%s' in format '%s'", filmTitle, contentType));

		if (filmTitle.isEmpty()) {
			return dataUtils.failedRequestErrorMessage("No movie found due invalid film title: " + filmTitle);
		}

		List<Film> listOfFilmsReturnedByDb = filmDbUtils.getFilmsByTitle(filmTitle);

		if (listOfFilmsReturnedByDb.isEmpty()) {
			return dataUtils.failedRequestErrorMessage("No movie matches found for: " + filmTitle);
		}

		Films filmsResult = new Films();
		filmsResult.setFilmList(listOfFilmsReturnedByDb);

		return dataUtils.convertFilmsForClientContentType(contentType, listOfFilmsReturnedByDb, filmsResult);

	}

	public ResponseEntity<?> getFilmsByAnyTermService(String contentType, String searchTerm) {

		System.out.println("--------------------");
		System.out.println(
				String.format("Request recieved to GET data by search term: '%s' in format '%s'", searchTerm, contentType));

		if (searchTerm.isEmpty()) {
			return dataUtils.failedRequestErrorMessage("No movie found due invalid search term: " + searchTerm);
		}

		List<Film> listOfFilmsReturnedByDb = filmDbUtils.getFilmsByAnyTerm(searchTerm);

		if (listOfFilmsReturnedByDb.isEmpty()) {
			return dataUtils.failedRequestErrorMessage("No movie matches found for: " + searchTerm);
		}

		Films filmsResult = new Films();
		filmsResult.setFilmList(listOfFilmsReturnedByDb);

		return dataUtils.convertFilmsForClientContentType(contentType, listOfFilmsReturnedByDb, filmsResult);
	}

	public ResponseEntity<?> insertFilm(String contentType, Film newFilm) {
		
		System.out.println("--------------------");
		System.out.println(String.format("Request recieved to INSERT film with title: '%s' in format '%s'", newFilm.getTitle(), contentType));

		try {
			filmDbUtils.insertFilm(newFilm);
		} catch (Exception e) {
			return dataUtils.failedRequestErrorMessage("Failed to insert movie with title: " + newFilm.getTitle());
		}

		String resultMessage = "Successfully inserted movie with title: " + newFilm.getTitle();
		System.out.println("--------------------");
		System.out.println(resultMessage);
		return new ResponseEntity<String>(resultMessage, HttpStatus.OK);
	}
	
	public ResponseEntity<?> updateFilmById(String contentType, Film updatedFilm) {
		
		int filmId = updatedFilm.getId();

		System.out.println("--------------------");
		System.out.println(String.format("Request recieved to UPDATE film with ID: '%d' in format '%s'", filmId, contentType));
		
//		Method is created to avoid an ambiguous exception being thrown when the movie isn't identified in the try/catch
		if (!filmExistsInDatabase(filmId)) {
			return dataUtils.failedRequestErrorMessage("No movie found with Film ID: " + filmId);
		}

		String resultMessage = "";

		try {
			filmDbUtils.updateFilm(updatedFilm);
		} catch (Exception e) {
			resultMessage = String.format("Failed to update movie with ID: '%d'. Make sure the movie exists.", filmId);
			return dataUtils.failedRequestErrorMessage(resultMessage);
		}

		resultMessage = "Successfully updated movie with ID: " + filmId;
		System.out.println("--------------------");
		System.out.println(resultMessage);
		return new ResponseEntity<String>(resultMessage, HttpStatus.OK);
	}

	public ResponseEntity<?> deleteFilmByIdService(String filmIdString)
			throws HibernateException, IllegalArgumentException {

		System.out.println("--------------------");
		System.out.println("Request recieved to delete film with ID: " + filmIdString);

		String resultMessage = "";
		if (!dataUtils.isValidFilmId(filmIdString)) {
			return dataUtils.failedRequestErrorMessage("No movie found to delete due to invalid Film ID: " + filmIdString);
		}

		int filmId = Integer.parseInt(filmIdString);

//		Method is created to avoid an ambiguous exception being thrown when the movie isn't identified in the try/catch
		if (!filmExistsInDatabase(filmId)) {
			return dataUtils.failedRequestErrorMessage("No movie found with Film ID: " + filmId);
		}

		try {
			filmDbUtils.deleteFilm(filmId);
		} catch (Exception e) {
			resultMessage = String.format("Failed to delete movie with ID: %d. Make sure the movie exists.", filmId);
			e.printStackTrace();
			return dataUtils.failedRequestErrorMessage(resultMessage);
		}
		
		resultMessage = "Successfully deleted movie with ID: " + filmId;
		System.out.println(resultMessage);
		System.out.println("--------------------");
		return new ResponseEntity<String>(resultMessage, HttpStatus.OK);
	}

	private boolean filmExistsInDatabase(int filmId) {
		Film film = filmDbUtils.getFilmById(filmId);

		if (film == null) {
			return false;
		}
		return true;
	}


}
