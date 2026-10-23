package com.app.calendar;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.sql.DataSource;

import org.jsoup.nodes.Element;

import com.app.util.RequestParamContext;
import com.app.util.ServletResponseUtil;
import com.app.util.TemplateRenderer;
import com.app.util.UserDataService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "CalendarServlet", urlPatterns = {"/calendar"})
public class CalendarServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        render(req, resp);
    }

    static void render(HttpServletRequest req, HttpServletResponse resp) {
        resp.setHeader("Cache-Control", "no-store");
        resp.setHeader("X-Content-Type-Options", "nosniff");
        RequestParamContext requestContext = new RequestParamContext(req);
        String username = UserDataService.currentUser(req);
        if (username.isBlank()) {
            ServletResponseUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Sign in to open your calendar");
            return;
        }

        try {
            String requestedMonth = requestContext.value(com.app.util.AppWebConstants.MONTH_KEY);
            CalendarMonth checkedMonth = CalendarMonth.from(requestedMonth, LocalDate.now());
            CalendarStore store = store(req);
            CalendarPreferences preferences = store.preferences(username);
            LocalDate today = LocalDate.now(ZoneId.of(preferences.zoneId()));
            CalendarMonth calendar = new CalendarMonth(requestedMonth == null ? YearMonth.from(today) : checkedMonth.month(), today);
            if ("GET".equals(req.getMethod()) && CalendarEventInput.text(req, "export", null, 20) != null) {
                String target = CalendarEventInput.text(req, "export", "all", 20);
                List<CalendarEvent> events = target.equals("all") ? store.events(username) : List.of(store.find(username, Long.parseLong(target)));
                resp.setContentType("text/calendar;charset=UTF-8");
                resp.setHeader("Content-Disposition", "attachment; filename=\"calendar.ics\"");
                resp.getOutputStream().write(new CalendarExchange().export(username, events, store));
                return;
            }
            CalendarService service = new CalendarService(store);
            service.refreshReminders(username);
            String view = req.getAttribute("calendar.view") instanceof String override ? override
                    : CalendarEventInput.text(req, "view", preferences.defaultView(), 20);
            if (CalendarEventInput.text(req, "edit", null, 20) != null || Boolean.TRUE.equals(req.getAttribute("calendar.form"))) {
                view = com.app.util.AppWebConstants.MONTH_KEY;
            }
            CalendarPage page = new CalendarPage(req, requestContext.contextPath(), username,
                    CalendarSecurity.token(req.getSession(false)), preferences, calendar, store, service);
            Map<String, String> values = new HashMap<>();
            values.put(com.app.util.AppWebConstants.CONTEXT_KEY, TemplateRenderer.escapeHtml(requestContext.contextPath()));
            values.put(com.app.util.AppWebConstants.USER_KEY, TemplateRenderer.escapeHtml(username));
            values.put("admin_href", TemplateRenderer.escapeHtml(requestContext.appPath("/admin/users")));
            values.put("admin_style", UserDataService.isAdmin(req.getServletContext(), username) ? "" : " style=\"display:none\"");
            values.put("today_iso", calendar.today().toString());
            values.put("today_label", calendar.today().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, uuuu", Locale.ENGLISH))
                    + " (" + preferences.zoneId() + ')');
            values.put("tabs", page.tabs(view));
            values.put("content", page.render(view));
            Object error = req.getAttribute("calendar.error");
            Object notice = req.getSession(false).getAttribute("calendar.notice");
            req.getSession(false).removeAttribute("calendar.notice");
                values.put("message", error == null && notice == null ? "" : new Element("p").attr("role", "alert")
                    .addClass("calendar-message").text(String.valueOf(error == null ? notice : error)).outerHtml());
            ServletResponseUtil.writeHtml(resp, TemplateRenderer.render(req.getServletContext(), "/WEB-INF/templates/calendar.html", values));
        } catch (NoSuchElementException exception) {
            ServletResponseUtil.sendError(resp, 404, "Event not found");
        } catch (IllegalArgumentException | DateTimeException exception) {
            ServletResponseUtil.sendError(resp, 400, "Invalid calendar input");
        } catch (IOException exception) {
            req.getServletContext().log("Unable to write calendar response", exception);
            ServletResponseUtil.sendError(resp, 500, "Calendar response is unavailable");
        } catch (SQLException exception) {
            req.getServletContext().log("Unable to load calendar", exception);
            ServletResponseUtil.sendError(resp, 503, "Calendar storage is unavailable");
        } catch (IllegalStateException exception) {
            req.getServletContext().log("Calendar is unavailable", exception);
            ServletResponseUtil.sendError(resp, 503, "Calendar storage is unavailable");
        }
    }

    static CalendarStore store(HttpServletRequest request) {
        Object configured = request.getServletContext().getAttribute(UserDataService.CONTEXT_DATASOURCE);
        if (!(configured instanceof DataSource dataSource)) {
            throw new IllegalStateException("DataSource is not configured");
        }
        return new CalendarStore(dataSource);
    }
}