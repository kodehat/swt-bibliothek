package de.fhl.swtlibrary.mvc;

import com.google.inject.Inject;
import de.fhl.swtlibrary.model.Author;
import de.fhl.swtlibrary.model.Book;
import de.fhl.swtlibrary.util.Paths;
import io.requery.EntityStore;
import io.requery.Persistable;
import org.jooby.Request;
import org.jooby.Result;
import org.jooby.Results;
import org.jooby.mvc.GET;
import org.jooby.mvc.POST;
import org.jooby.mvc.Path;

import java.util.List;

@Path("/")
public class SearchController {

  private Request req;
  private EntityStore<Persistable, Book> bookEntityStore;

  @Inject
  public SearchController(Request req, EntityStore<Persistable, Book> bookEntityStore) {
    this.req = req;
    this.bookEntityStore = bookEntityStore;
  }

  @GET
  public Result getBasicSearch() {
    return Results.html("pages/index");
  }

  @POST
  public Result postBasicSearch() {
    String query = req.param("query").value();

    if (query.trim().isEmpty()) {
      req.flash("error", true)
        .flash("error_message", "ERROR_MISSING_FIELDS");
      return Results.redirect(Paths.BOOK_SEARCH);
    } else if (query.trim().length() < 3) {
      req.flash("error", true)
        .flash("error_message", "ERROR_SHORT_QUERY");
      return Results.redirect(Paths.BOOK_SEARCH);
    }

    List<Book> books = bookEntityStore.select(Book.class)
      .where(Book.TITLE.lower().like("%" + query.toLowerCase() + "%"))
      .get()
      .toList();

    if (books.isEmpty()) {
      req.flash("error", true)
        .flash("error_message", "ERROR_NO_RESULTS");
      return Results.redirect(Paths.BOOK_SEARCH);
    }

    books.forEach(b -> bookEntityStore.refresh(b, Book.AUTHORS));
    books.forEach(b -> bookEntityStore.refresh(b, Book.COPIES));
    req.set("query", query);
    req.set("books", books);
    return Results.html("pages/search_results");
  }

}
