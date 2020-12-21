package com.filmproject.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.criterion.Restrictions;

import com.filmproject.model.Film;


public class FilmDatabaseUtils {

	private static SessionFactory generateFactorySession() {
		SessionFactory sessionFactory = null;

		try {
			sessionFactory = new AnnotationConfiguration().configure().addAnnotatedClass(Film.class)
					.buildSessionFactory();
		} catch (Throwable ex) {
			System.err.println("Failed to create sessionFactory object." + ex);
			throw new ExceptionInInitializerError(ex);
		}

		return sessionFactory;
	}

	public List<Film> getAllFilms() {
		Session session = generateFactorySession().openSession();
		Transaction tx = null;
		List<Film> resultList = new ArrayList<Film>();
		try {
			tx = session.beginTransaction();
			List films = session.createQuery("FROM Film").list();
			for (Iterator iterator = films.iterator(); iterator.hasNext();) {
				Film film = (Film) iterator.next();
				resultList.add(film);
			}
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
		return resultList;
	}

	public boolean insertFilm(Film film) {
		Session session = generateFactorySession().openSession();
		Transaction tx = null;
		Boolean successfulOperation = false;

		try {
			tx = session.beginTransaction();
			session.save(new Film(999, film.getTitle(), film.getYear(), film.getDirector(), film.getStars(),
					film.getReview()));

			tx.commit();
			successfulOperation = true;
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
		return successfulOperation;
	}

	public Film getFilmById(int filmId) {
		Session session = generateFactorySession().openSession();
		Transaction tx = null;
		Film film = null;
		try {
			tx = session.beginTransaction();
			film = (Film) session.get(Film.class, filmId);
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}

		return film;
	}

	public List<Film> getFilmsByTitle(String queryTerm) {
		Session session = generateFactorySession().openSession();
		Transaction tx = null;
		List<Film> filmResultList = new ArrayList<Film>();
		try {
			tx = session.beginTransaction();

			// The set parameter allows you to inject values into the SQL query similar to
			// passing arguments.
//			List<Query> queryResult = session.createQuery("FROM Film where title LIKE :title")
//					.setParameter("title", queryTerm).list();
			
			List<Query> queryResult = session.createCriteria(Film.class)
					.add(Restrictions.disjunction()
							.add(Restrictions.like("title", "%" + queryTerm + "%")))
					.list();

			System.out.println(queryResult);
			for (Iterator iterator = queryResult.iterator(); iterator.hasNext();) {
				Film film = (Film) iterator.next();
				filmResultList.add(film);
			}
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
		return filmResultList;
	}

	@SuppressWarnings("unchecked")
	public List<Film> getFilmsByAnyTerm(String queryTerm) {
		Session session = generateFactorySession().openSession();
		Transaction tx = null;
		List<Film> filmResultList = new ArrayList<Film>();
		try {
			tx = session.beginTransaction();

			List<String> queryResult = new ArrayList<String>();

			// Two separate queries are required because the query fails if a string is
			// parsed.
			if (isStringInteger(queryTerm)) {
				queryResult = session.createCriteria(Film.class)
						.add(Restrictions.disjunction()
								.add(Restrictions.eq("year", Integer.parseInt(queryTerm)))
								.add(Restrictions.eq("id", Integer.parseInt(queryTerm)))
								.add(Restrictions.like("title", "%" + queryTerm + "%"))
								.add(Restrictions.like("director", "%" + queryTerm + "%"))
								.add(Restrictions.like("stars", "%" + queryTerm + "%"))
								.add(Restrictions.like("review", "%" + queryTerm + "%")))
						.list();
			} else {
				queryResult = session.createCriteria(Film.class)
						.add(Restrictions.disjunction()
								.add(Restrictions.like("title", "%" + queryTerm + "%"))
								.add(Restrictions.like("director", "%" + queryTerm + "%"))
								.add(Restrictions.like("stars", "%" + queryTerm + "%"))
								.add(Restrictions.like("review", "%" + queryTerm + "%")))
						.list();
			}

			for (Iterator iterator = queryResult.iterator(); iterator.hasNext();) {
				Film film = (Film) iterator.next();
				filmResultList.add(film);
			}
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
		return filmResultList;
	}

	public boolean updateFilm(int filmId, Film updatedFilm) {
		Session session = generateFactorySession().openSession();
		Transaction tx = null;
		Boolean successfulOperation = false;

		try {
			tx = session.beginTransaction();
			Film film = (Film) session.get(Film.class, filmId);
			film.setTitle(updatedFilm.getTitle());
			film.setYear(updatedFilm.getYear());
			film.setDirector(updatedFilm.getDirector());
			film.setStars(updatedFilm.getStars());
			film.setReview(updatedFilm.getReview());

			session.update(film);
			tx.commit();
			successfulOperation = true;
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
		return successfulOperation;
	}

	public boolean deleteFilm(int filmId) {
		Session session = generateFactorySession().openSession();
		Transaction tx = null;
		Boolean successfulOperation = false;

		try {
			tx = session.beginTransaction();
			Film film = (Film) session.get(Film.class, filmId);
			session.delete(film);
			tx.commit();
			successfulOperation = true;
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
		return successfulOperation;
	}

	public static boolean isStringInteger(String number) {
		try {
			Integer.parseInt(number);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

}
