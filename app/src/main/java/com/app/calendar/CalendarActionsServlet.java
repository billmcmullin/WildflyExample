package com.app.calendar;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

import com.app.util.UserDataService;
import com.app.util.ServletResponseUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

@WebServlet(name = "CalendarActionsServlet", urlPatterns = {"/calendar/action"})
@MultipartConfig(maxFileSize = CalendarExchange.MAX_BYTES, maxRequestSize = 300000)
public class CalendarActionsServlet extends HttpServlet {
    private final void readObject(java.io.ObjectInputStream input) throws IOException {
        throw new java.io.NotSerializableException("CalendarActionsServlet");
    }

    private static final String FIELD_MONTH = com.app.util.AppWebConstants.MONTH_KEY;
    private static final String FIELD_OCCURRENCE = "occurrence";
    private static final String FIELD_IMPORT = "import";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            request.setCharacterEncoding("UTF-8");
        } catch (java.io.UnsupportedEncodingException exception) {
            request.getServletContext().log("Unable to set calendar request encoding", exception);
            ServletResponseUtil.sendError(response, 500, "Calendar request is unavailable");
            return;
        }
        response.setHeader("Cache-Control", "no-store");
        String owner = UserDataService.currentUser(request);
        if (owner.isBlank()) {
            ServletResponseUtil.sendError(response, 401, "Sign in to change your calendar");
            return;
        }
        if (request.getContentLengthLong() > 300000) {
            ServletResponseUtil.sendError(response, 413, "Calendar requests must be smaller than 300000 bytes");
            return;
        }
        if (!CalendarSecurity.valid(request)) {
            ServletResponseUtil.sendError(response, 403, "The form expired. Reload the calendar and try again.");
            return;
        }
        String action = "";
        try {
            action = CalendarEventInput.text(request, "action", "", 30);
            final String month = CalendarMonth.from(CalendarEventInput.text(request, CalendarActionsServlet.FIELD_MONTH, LocalDate.now().toString().substring(0, 7), 7), LocalDate.now()).month().toString();
            CalendarStore store = CalendarServlet.store(request);
            store.preferences(owner);
            CalendarService service = new CalendarService(store);
            CalendarActionResult result = switch (action) {
                case "save" -> {
                    long id = Long.parseLong(CalendarEventInput.text(request, "id", "0", 20));
                    CalendarEvent previous = id == 0 ? null : store.find(owner, id);
                    boolean single = CalendarActionsServlet.FIELD_OCCURRENCE.equals(CalendarEventInput.text(request, "scope", "series", 16));
                    CalendarEvent event = CalendarEventInput.parse(request, owner, previous, single);
                    if (single) {
                        if (previous == null) {
                            throw new IllegalArgumentException("Choose an existing recurring event");
                        }
                        service.changeOccurrence(owner, id, Long.parseLong(CalendarEventInput.text(request, "version", "", 20)),
                                LocalDateTime.parse(CalendarEventInput.text(request, CalendarActionsServlet.FIELD_OCCURRENCE, "", 40)), event);
                    } else {
                        service.save(owner, event);
                    }
                    yield new CalendarActionResult("Event saved", CalendarActionsServlet.FIELD_MONTH);
                }
                case "delete" -> {
                    if (!"on".equals(CalendarEventInput.text(request, "confirm", "", 8))) {
                        throw new IllegalArgumentException("Confirm the deletion first");
                    }
                    long id = Long.parseLong(CalendarEventInput.text(request, "id", "", 20));
                    long version = Long.parseLong(CalendarEventInput.text(request, "version", "", 20));
                    if (CalendarActionsServlet.FIELD_OCCURRENCE.equals(CalendarEventInput.text(request, "scope", "series", 16))) {
                        service.changeOccurrence(owner, id, version,
                                LocalDateTime.parse(CalendarEventInput.text(request, CalendarActionsServlet.FIELD_OCCURRENCE, "", 40)), null);
                    } else {
                        store.delete(owner, id, version);
                    }
                    yield new CalendarActionResult("Event deleted", CalendarActionsServlet.FIELD_MONTH);
                }
                case "preferences" -> {
                    store.savePreferences(owner, new CalendarPreferences(CalendarEventInput.text(request, "zoneId", "UTC", 100),
                            Integer.parseInt(CalendarEventInput.text(request, "weekStart", "7", 1)),
                            CalendarEventInput.text(request, "defaultView", CalendarActionsServlet.FIELD_MONTH, 16)));
                    yield new CalendarActionResult("Preferences saved", "settings");
                }
                case "read" -> {
                    store.markRead(owner, Long.parseLong(CalendarEventInput.text(request, "id", "", 20)));
                    yield new CalendarActionResult("Notification marked read", "notifications");
                }
                case "preview" -> {
                    Part file = request.getPart("file");
                    if (file == null || file.getSize() == 0) {
                        throw new IllegalArgumentException("Choose an iCalendar file");
                    }
                    CalendarExchange.Preview preview;
                    try (InputStream input = file.getInputStream()) {
                        preview = new CalendarExchange().preview(input, owner, store.preferences(owner).zoneId());
                    } finally {
                        file.delete();
                    }
                    HttpSession session = request.getSession(false);
                    session.setAttribute("calendar.import.preview", preview);
                    session.setAttribute("calendar.import.events", preview.events());
                    session.setAttribute("calendar.import.id", UUID.randomUUID().toString());
                    yield new CalendarActionResult("Import preview ready", CalendarActionsServlet.FIELD_IMPORT);
                }
                case CalendarActionsServlet.FIELD_IMPORT -> {
                    HttpSession session = request.getSession(false);
                    synchronized (session) {
                        Object pending = session.getAttribute("calendar.import.preview");
                        if (!(pending instanceof CalendarExchange.Preview preview)
                                || !CalendarEventInput.text(request, "previewId", "", 40).equals(session.getAttribute("calendar.import.id"))) {
                            throw new IllegalArgumentException("Import preview expired. Upload the file again.");
                        }
                        CalendarStore.ImportResult imported = store.importCalendar(owner, preview.events(), preview.exceptions());
                        clearPreview(session);
                        yield new CalendarActionResult("Imported " + imported.added() + " events; skipped " + imported.skipped() + " existing UIDs", CalendarActionsServlet.FIELD_IMPORT);
                    }
                }
                case "cancelImport" -> {
                    clearPreview(request.getSession(false));
                    yield new CalendarActionResult("Import cancelled", CalendarActionsServlet.FIELD_IMPORT);
                }
                default -> throw new IllegalArgumentException("Unknown calendar action");
            };
            request.getSession(false).setAttribute("calendar.notice", result.notice());
            response.setStatus(303);
            response.setHeader("Location", request.getContextPath() + "/calendar?month=" + month + "&view=" + result.view());
        } catch (NoSuchElementException exception) {
            ServletResponseUtil.sendError(response, 404, "Event or notification not found");
        } catch (CalendarStore.ConflictException exception) {
            renderError(request, response, 409, action, "This event changed in another tab. Reload before saving.");
        } catch (IllegalArgumentException | DateTimeException | ServletException exception) {
            renderError(request, response, 400, action, "Check the form values.");
        } catch (IOException exception) {
            request.getServletContext().log("Unable to read calendar request", exception);
            ServletResponseUtil.sendError(response, 500, "Calendar request is unavailable");
        } catch (SQLException exception) {
            request.getServletContext().log("Calendar update failed", exception);
            ServletResponseUtil.sendError(response, "23505".equals(exception.getSQLState()) ? 409 : 503,
                    "23505".equals(exception.getSQLState()) ? "This change conflicts with another calendar update. Reload and try again."
                            : "Calendar storage is unavailable");
        } catch (IllegalStateException exception) {
            request.getServletContext().log("Calendar update unavailable", exception);
            ServletResponseUtil.sendError(response, 503, "Calendar storage is unavailable");
        }
    }

    private static void clearPreview(HttpSession session) {
        session.removeAttribute("calendar.import.preview");
        session.removeAttribute("calendar.import.events");
        session.removeAttribute("calendar.import.id");
    }

    private static void renderError(HttpServletRequest request, HttpServletResponse response, int status, String action, String message) {
        response.setStatus(status);
        request.setAttribute("calendar.error", message);
        request.setAttribute("calendar.form", action.equals("save"));
        request.setAttribute("calendar.view", action.equals("preferences") ? "settings"
                : action.equals("preview") || action.equals(CalendarActionsServlet.FIELD_IMPORT) ? CalendarActionsServlet.FIELD_IMPORT : CalendarActionsServlet.FIELD_MONTH);
        CalendarServlet.render(request, response);
    }
}