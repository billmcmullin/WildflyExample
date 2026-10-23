package com.app.calendar;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.app.util.TemplateRenderer;

import jakarta.servlet.http.HttpServletRequest;

final class CalendarPage {
    private static final String FIELD_NAV = "nav";
    private static final String TEXT_CALENDAR_TABS = "calendar-tabs";
    private static final String TEXT_ARIA_LABEL = "aria-label";
    private static final String TEXT_CALENDAR_VIEWS = "Calendar views";
    private static final String FIELD_MONTH = com.app.util.AppWebConstants.MONTH_KEY;
    private static final String TEXT_MONTH = "Month";
    private static final String FIELD_AGENDA = "agenda";
    private static final String TEXT_AGENDA = "Agenda";
    private static final String FIELD_HISTORY = "history";
    private static final String TEXT_HISTORY = "History";
    private static final String FIELD_NOTIFICATIONS = "notifications";
    private static final String TEXT_NOTIFICATIONS = "Notifications";
    private static final String FIELD_SETTINGS = "settings";
    private static final String TEXT_PREFERENCES = "Preferences";
    private static final String FIELD_IMPORT = "import";
    private static final String TEXT_IMPORT = "Import";
    private static final String FIELD_VIEW = "view";
    private static final String TEXT_ARIA_CURRENT = "aria-current";
    private static final String FIELD_PAGE = "page";
    private static final String TEXT_UNKNOWN_CALENDAR_VIEW = "Unknown calendar view";
    private static final String FIELD_FROM = "from";
    private static final String FIELD_TO = "to";
    private static final String FIELD_Q = "q";
    private static final String EMPTY_TEXT = "";
    private static final String FIELD_DIV = "div";
    private static final String FIELD_SECTION = "section";
    private static final String FIELD_ID = "id";
    private static final String FIELD_H2 = "h2";
    private static final String TEXT_EVENTS = "Events";
    private static final String FIELD_FORM = "form";
    private static final String FIELD_METHOD = "method";
    private static final String FIELD_GET = "get";
    private static final String TEXT_CALENDAR_SEARCH = "calendar-search";
    private static final String FIELD_ACTION = "action";
    private static final String TEXT_CALENDAR = "/calendar";
    private static final String TEXT_SEARCH = "Search";
    private static final String FIELD_SEARCH = "search";
    private static final String FIELD_MAXLENGTH = "maxlength";
    private static final String TEXT_200 = "200";
    private static final String TEXT_FROM = "From";
    private static final String FIELD_DATE = "date";
    private static final String FIELD_REQUIRED = "required";
    private static final String TEXT_THROUGH = "Through";
    private static final String FIELD_P = "p";
    private static final String TEXT_NO_EVENTS_IN_THIS_DATE_RANGE = "No events in this date range.";
    private static final String TEXT_CALENDAR_TABLE_SCROLL = "calendar-table-scroll";
    private static final String FIELD_TABLE = "table";
    private static final String TEXT_CALENDAR_LIST = "calendar-list";
    private static final String FIELD_THEAD = "thead";
    private static final String FIELD_TR = "tr";
    private static final String TEXT_EVENT = "Event";
    private static final String TEXT_WHEN = "When";
    private static final String TEXT_ACTIONS = "Actions";
    private static final String FIELD_TH = "th";
    private static final String FIELD_TBODY = "tbody";
    private static final String TEXT_OCCURRENCES_PAGE = " occurrences; page ";
    private static final String TEXT_OF = " of ";
    private static final String TEXT_PREVIOUS_PAGE = "Previous page";
    private static final String TEXT_NEXT_PAGE = "Next page";
    private static final String FIELD_TD = "td";
    private static final String FIELD_STRONG = "strong";
    private static final String TEXT_CALENDAR_DESCRIPTION = "calendar-description";
    private static final String TEXT_ALL_DAY = " (all day)";
    private static final String TEXT_CALENDAR_ROW_ACTIONS = "calendar-row-actions";
    private static final String FIELD_EDIT = "edit";
    private static final String TEXT_EVENT_EDITOR = "#event-editor";
    private static final String TEXT_EDIT = "Edit";
    private static final String TEXT_EDIT_SERIES = "Edit series";
    private static final String FIELD_OCCURRENCE = "occurrence";
    private static final String TEXT_EDIT_OCCURRENCE = "Edit occurrence";
    private static final String FIELD_EXPORT = "export";
    private static final String TEXT_EXPORT = "Export";
    private static final String FIELD_DETAILS = "details";
    private static final String FIELD_SUMMARY = "summary";
    private static final String TEXT_DELETE = "Delete";
    private static final String FIELD_DELETE = "delete";
    private static final String FIELD_VERSION = "version";
    private static final String FIELD_SCOPE = "scope";
    private static final String TEXT_THIS_OCCURRENCE = "This occurrence";
    private static final String FIELD_SERIES = "series";
    private static final String TEXT_ENTIRE_SERIES = "Entire series";
    private static final String FIELD_LABEL = "label";
    private static final String TEXT_CALENDAR_CHECKBOX = "calendar-checkbox";
    private static final String FIELD_INPUT = "input";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_CHECKBOX = "checkbox";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_CONFIRM = "confirm";
    private static final String TEXT_CONFIRM_DELETION = "Confirm deletion";
    private static final String TEXT_CALENDAR_DANGER = "calendar-danger";
    private static final String TEXT_CALENDAR_TOOLBAR = "calendar-toolbar";
    private static final String TEXT_CALENDAR_MONTH = "calendar-month";
    private static final String TEXT_CALENDAR_ACTIONS = "calendar-actions";
    private static final String TEXT_SYMBOL = "\u2039";
    private static final String TEXT_PREVIOUS_MONTH = "Previous month";
    private static final String TEXT_TODAY = "Today";
    private static final String TEXT_SYMBOL_203A = "\u203a";
    private static final String TEXT_NEXT_MONTH = "Next month";
    private static final String TEXT_CALENDAR_JUMP = "calendar-jump";
    private static final String FIELD_MIN = "min";
    private static final String TEXT_0001_01 = "0001-01";
    private static final String FIELD_MAX = "max";
    private static final String TEXT_9999_12 = "9999-12";
    private static final String TEXT_VIEW = "View";
    private static final String TEXT_CALENDAR_GRID = "calendar-grid";
    private static final String TEXT_ARIA_LABELLEDBY = "aria-labelledby";
    private static final String FIELD_COL = "col";
    private static final String FIELD_ABBR = "abbr";
    private static final String FIELD_TITLE = "title";
    private static final String TEXT_CALENDAR_EMPTY = "calendar-empty";
    private static final String TEXT_CALENDAR_TODAY = "calendar-today";
    private static final String FIELD_TIME = "time";
    private static final String FIELD_DATETIME = "datetime";
    private static final String TEXT_CALENDAR_EVENT = "calendar-event";
    private static final String TEXT_SYMBOL_2B = "+";
    private static final String TEXT_MORE = " more";
    private static final String TEXT_CALENDAR_FORM = "calendar.form";
    private static final String TEXT_0 = "0";
    private static final String FIELD_NEW = "new";
    private static final String TEXT_UNTIL = "UNTIL";
    private static final String FIELD_ON = "on";
    private static final String FIELD_ALL_DAY = "allDay";
    private static final String TEXT_EVENT_EDITOR_D16ED800 = "event-editor";
    private static final String TEXT_CALENDAR_EDITOR = "calendar-editor";
    private static final String FIELD_OPEN = "open";
    private static final String TEXT_NEW_EVENT = "New event";
    private static final String TEXT_EDIT_SERIES_EVENT = "Edit series / event";
    private static final String FIELD_SAVE = "save";
    private static final String TEXT_1 = "1";
    private static final String TEXT_TITLE = "Title";
    private static final String FIELD_TEXT = "text";
    private static final String FIELD_FOR = "for";
    private static final String FIELD_DESCRIPTION = "description";
    private static final String TEXT_DESCRIPTION = "Description";
    private static final String FIELD_TEXTAREA = "textarea";
    private static final String TEXT_5000 = "5000";
    private static final String FIELD_ROWS = "rows";
    private static final String TEXT_3 = "3";
    private static final String FIELD_CHECKED = "checked";
    private static final String TEXT_ALL_DAY_2CC2687D = "All day";
    private static final String TEXT_CALENDAR_FORM_GRID = "calendar-form-grid";
    private static final String TEXT_1900_01_01 = "1900-01-01";
    private static final String TEXT_9998_12_31 = "9998-12-31";
    private static final String FIELD_START_DATE = "startDate";
    private static final String TEXT_START_DATE = "Start date";
    private static final String FIELD_START_TIME = "startTime";
    private static final String TEXT_START_TIME = "Start time";
    private static final String FIELD_CLASS = "class";
    private static final String TEXT_CALENDAR_TIME = "calendar-time";
    private static final String FIELD_END_DATE = "endDate";
    private static final String TEXT_LAST_DAY_END_DATE = "Last day / end date";
    private static final String FIELD_END_TIME = "endTime";
    private static final String TEXT_END_TIME = "End time";
    private static final String FIELD_ZONE_ID = "zoneId";
    private static final String FIELD_FREQUENCY = "frequency";
    private static final String TEXT_REPEAT = "Repeat";
    private static final String TEXT_NONE = "NONE";
    private static final String TEXT_FREQ = "FREQ";
    private static final String TEXT_DOES_NOT_REPEAT = "Does not repeat";
    private static final String TEXT_DAILY = "DAILY";
    private static final String TEXT_DAILY_3EBEDF9 = "Daily";
    private static final String TEXT_WEEKLY = "WEEKLY";
    private static final String TEXT_WEEKLY_9A346CA1 = "Weekly";
    private static final String TEXT_MONTHLY = "MONTHLY";
    private static final String TEXT_MONTHLY_ACEE27ED = "Monthly";
    private static final String FIELD_END_MODE = "endMode";
    private static final String TEXT_REPEAT_ENDS = "Repeat ends";
    private static final String FIELD_COUNT = "count";
    private static final String FIELD_UNTIL = "until";
    private static final String TEXT_AFTER_OCCURRENCES = "After occurrences";
    private static final String TEXT_ON_DATE = "On date";
    private static final String TEXT_OCCURRENCES = "Occurrences";
    private static final String FIELD_NUMBER = "number";
    private static final String TEXT_10 = "10";
    private static final String TEXT_COUNT = "COUNT";
    private static final String TEXT_999 = "999";
    private static final String TEXT_REPEAT_UNTIL = "Repeat until";
    private static final String FIELD_REMINDER = "reminder";
    private static final String TEXT_REMINDER = "Reminder";
    private static final String TEXT_1_5A4 = "-1";
    private static final String TEXT_NONE_252358 = "None";
    private static final String TEXT_AT_START = "At start";
    private static final String TEXT_5 = "5";
    private static final String TEXT_5_MINUTES_BEFORE = "5 minutes before";
    private static final String TEXT_15 = "15";
    private static final String TEXT_15_MINUTES_BEFORE = "15 minutes before";
    private static final String TEXT_30 = "30";
    private static final String TEXT_30_MINUTES_BEFORE = "30 minutes before";
    private static final String TEXT_60 = "60";
    private static final String TEXT_1_HOUR_BEFORE = "1 hour before";
    private static final String TEXT_1440 = "1440";
    private static final String TEXT_1_DAY_BEFORE = "1 day before";
    private static final String TEXT_10080 = "10080";
    private static final String TEXT_1_WEEK_BEFORE = "1 week before";
    private static final String TEXT_SAVE_EVENT = "Save event";
    private static final String TEXT_CANCEL = "Cancel";
    private static final String FIELD_PREFERENCES = "preferences";
    private static final String FIELD_WEEK_START = "weekStart";
    private static final String TEXT_WEEK_STARTS = "Week starts";
    private static final String TEXT_MONDAY = "Monday";
    private static final String TEXT_7 = "7";
    private static final String TEXT_SUNDAY = "Sunday";
    private static final String FIELD_DEFAULT_VIEW = "defaultView";
    private static final String TEXT_DEFAULT_VIEW = "Default view";
    private static final String TEXT_SAVE_PREFERENCES = "Save preferences";
    private static final String TEXT_EVENT_HISTORY = "Event history";
    private static final String TEXT_NO_HISTORY_ON_THIS_PAGE = "No history on this page.";
    private static final String TEXT_CALENDAR_HISTORY = "calendar-history";
    private static final String TEXT_SYMBOL_8E69 = " \u00b7 ";
    private static final String FIELD_PRE = "pre";
    private static final String TEXT_NO_REMINDERS_YET = "No reminders yet.";
    private static final String TEXT_CALENDAR_NOTIFICATION = "calendar-notification";
    private static final String TEXT_READ = " \u00b7 Read";
    private static final String TEXT_UNREAD = " \u00b7 Unread";
    private static final String FIELD_READ = "read";
    private static final String TEXT_MARK_READ = "Mark read";
    private static final String TEXT_IMPORT_CALENDAR = "Import calendar";
    private static final String FIELD_POST = "post";
    private static final String FIELD_ENCTYPE = "enctype";
    private static final String TEXT_MULTIPART_FORM_DATA = "multipart/form-data";
    private static final String TEXT_CALENDAR_ACTION = "/calendar/action";
    private static final String FIELD_PREVIEW = "preview";
    private static final String FIELD_CSRF = "csrf";
    private static final String TEXT_CALENDAR_FILE = "calendar-file";
    private static final String TEXT_I_CALENDAR_FILE = "iCalendar file";
    private static final String FIELD_FILE = "file";
    private static final String FIELD_ACCEPT = "accept";
    private static final String TEXT_ICS_TEXT_CALENDAR = ".ics,text/calendar";
    private static final String TEXT_PREVIEW_IMPORT = "Preview import";
    private static final String TEXT_CALENDAR_IMPORT_EVENTS = "calendar.import.events";
    private static final String FIELD_H3 = "h3";
    private static final String TEXT_IMPORT_PREVIEW = "Import preview";
    private static final String TEXT_EVENTS_EXISTING_UIDS_WILL_BE_SKIPPED = " events; existing UIDs will be skipped.";
    private static final String FIELD_UL = "ul";
    private static final String FIELD_LI = "li";
    private static final String FIELD_PREVIEW_ID = "previewId";
    private static final String TEXT_CALENDAR_IMPORT_ID = "calendar.import.id";
    private static final String TEXT_IMPORT_EVENTS = "Import events";
    private static final String FIELD_CANCEL_IMPORT = "cancelImport";
    private static final String TEXT_CANCEL_IMPORT = "Cancel import";
    private static final String TEXT_TIME_ZONE = "Time zone";
    private static final String TEXT_CALENDAR_FIELD = "calendar-field";
    private static final String FIELD_SELECT = "select";
    private static final String FIELD_OPTION = "option";
    private static final String FIELD_VALUE = "value";
    private static final String FIELD_SELECTED = "selected";
    private static final String FIELD_HIDDEN = "hidden";
    private static final String FIELD_BUTTON = "button";
    private static final String FIELD_SUBMIT = "submit";
    private static final String TEXT_CALENDAR_8FFC3172 = "/calendar?";
    private static final String TEXT_SYMBOL_3D = "=";
    private static final String TEXT_SYMBOL_26 = "&";
    private static final String FIELD_A = "a";
    private static final String FIELD_HREF = "href";
    private static final String TEXT_CALENDAR_ARROW = "calendar-arrow";
    private static final String TEXT_INVALID_PAGE_NUMBER = "Invalid page number";
    private static final String TEXT_YYYY_MM_DD_HH_MM_Z = "yyyy-MM-dd HH:mm z";

    private final HttpServletRequest request;
    private final String context;
    private final String owner;
    private final String csrf;
    private final CalendarPreferences preferences;
    private final CalendarMonth calendar;
    private final CalendarStore store;
    private final CalendarService service;

    CalendarPage(HttpServletRequest request, String context, String owner, String csrf, CalendarPreferences preferences,
            CalendarMonth calendar, CalendarStore store, CalendarService service) {
        this.request = request;
        this.context = context;
        this.owner = owner;
        this.csrf = csrf;
        this.preferences = preferences;
        this.calendar = calendar;
        this.store = store;
        this.service = service;
    }

    String tabs(String active) {
        Element navigation = element(CalendarPage.FIELD_NAV).addClass(CalendarPage.TEXT_CALENDAR_TABS).attr(CalendarPage.TEXT_ARIA_LABEL, CalendarPage.TEXT_CALENDAR_VIEWS);
        String[][] tabs = {{CalendarPage.FIELD_MONTH, CalendarPage.TEXT_MONTH}, {CalendarPage.FIELD_AGENDA, CalendarPage.TEXT_AGENDA}, {CalendarPage.FIELD_HISTORY, CalendarPage.TEXT_HISTORY},
                {CalendarPage.FIELD_NOTIFICATIONS, CalendarPage.TEXT_NOTIFICATIONS}, {CalendarPage.FIELD_SETTINGS, CalendarPage.TEXT_PREFERENCES}, {CalendarPage.FIELD_IMPORT, CalendarPage.TEXT_IMPORT}};
        for (String[] tab : tabs) {
            Element link = anchor(url(Map.of(CalendarPage.FIELD_VIEW, tab[0], CalendarPage.FIELD_MONTH, calendar.month().toString())), tab[1]);
            if (active.equals(tab[0])) {
                link.attr(CalendarPage.TEXT_ARIA_CURRENT, CalendarPage.FIELD_PAGE);
            }
            navigation.appendChild(link);
        }
        return html(navigation);
    }

    String render(String view) throws SQLException {
        Element content = switch (view) {
            case CalendarPage.FIELD_HISTORY -> history();
            case CalendarPage.FIELD_NOTIFICATIONS -> notifications();
            case CalendarPage.FIELD_SETTINGS -> settings();
            case CalendarPage.FIELD_IMPORT -> imports();
            case CalendarPage.FIELD_MONTH, CalendarPage.FIELD_AGENDA -> events(view);
            default -> throw new IllegalArgumentException(CalendarPage.TEXT_UNKNOWN_CALENDAR_VIEW);
        };
        return html(content);
    }

    private Element events(String view) throws SQLException {
        LocalDate from = view.equals(CalendarPage.FIELD_MONTH) ? calendar.month().atDay(1)
                : LocalDate.parse(value(CalendarPage.FIELD_FROM, calendar.month().atDay(1).toString()));
        LocalDate through = view.equals(CalendarPage.FIELD_MONTH) ? calendar.month().atEndOfMonth()
                : LocalDate.parse(value(CalendarPage.FIELD_TO, calendar.month().atEndOfMonth().toString()));
        String query = value(CalendarPage.FIELD_Q, CalendarPage.EMPTY_TEXT);
        List<CalendarRecurrence.Occurrence> occurrences = service.search(owner, from, through.plusDays(1), query);
        Element root = element(CalendarPage.FIELD_DIV).appendChild(toolbar());
        if (view.equals(CalendarPage.FIELD_MONTH)) {
            root.appendChild(grid(occurrences));
        }
        Element section = root.appendElement(CalendarPage.FIELD_SECTION).attr(CalendarPage.FIELD_ID, CalendarPage.FIELD_AGENDA);
        section.appendElement(CalendarPage.FIELD_H2).text(CalendarPage.TEXT_EVENTS);
        Element search = section.appendElement(CalendarPage.FIELD_FORM).attr(CalendarPage.FIELD_METHOD, CalendarPage.FIELD_GET).addClass(CalendarPage.TEXT_CALENDAR_SEARCH).attr(CalendarPage.FIELD_ACTION, context + CalendarPage.TEXT_CALENDAR);
        search.appendChild(hidden(CalendarPage.FIELD_VIEW, CalendarPage.FIELD_AGENDA)).appendChild(hidden(CalendarPage.FIELD_MONTH, calendar.month().toString()));
        search.appendChild(input(CalendarPage.FIELD_Q, CalendarPage.TEXT_SEARCH, CalendarPage.FIELD_SEARCH, query, Map.of(CalendarPage.FIELD_MAXLENGTH, CalendarPage.TEXT_200)));
        search.appendChild(input(CalendarPage.FIELD_FROM, CalendarPage.TEXT_FROM, CalendarPage.FIELD_DATE, from.toString(), Map.of(CalendarPage.FIELD_REQUIRED, CalendarPage.EMPTY_TEXT)));
        search.appendChild(input(CalendarPage.FIELD_TO, CalendarPage.TEXT_THROUGH, CalendarPage.FIELD_DATE, through.toString(), Map.of(CalendarPage.FIELD_REQUIRED, CalendarPage.EMPTY_TEXT)));
        search.appendChild(button(CalendarPage.TEXT_SEARCH));
        int page = Math.min(page(), Math.max(1, (occurrences.size() + 24) / 25) - 1);
        int totalPages = Math.max(1, (occurrences.size() + 24) / 25);
        if (occurrences.isEmpty()) {
            section.appendElement(CalendarPage.FIELD_P).text(CalendarPage.TEXT_NO_EVENTS_IN_THIS_DATE_RANGE);
        } else {
            Element table = section.appendElement(CalendarPage.FIELD_DIV).addClass(CalendarPage.TEXT_CALENDAR_TABLE_SCROLL).appendElement(CalendarPage.FIELD_TABLE).addClass(CalendarPage.TEXT_CALENDAR_LIST);
            Element headings = table.appendElement(CalendarPage.FIELD_THEAD).appendElement(CalendarPage.FIELD_TR);
            for (String heading : List.of(CalendarPage.TEXT_EVENT, CalendarPage.TEXT_WHEN, CalendarPage.TEXT_ACTIONS)) {
                headings.appendElement(CalendarPage.FIELD_TH).text(heading);
            }
            Element body = table.appendElement(CalendarPage.FIELD_TBODY);
            for (CalendarRecurrence.Occurrence occurrence : occurrences.subList(page * 25, Math.min(occurrences.size(), page * 25 + 25))) {
                eventRow(body, occurrence);
            }
            section.appendElement(CalendarPage.FIELD_P).text(occurrences.size() + CalendarPage.TEXT_OCCURRENCES_PAGE + (page + 1) + CalendarPage.TEXT_OF + totalPages);
            Map<String, String> filters = new HashMap<>(Map.of(CalendarPage.FIELD_VIEW, CalendarPage.FIELD_AGENDA, CalendarPage.FIELD_MONTH, calendar.month().toString(),
                    CalendarPage.FIELD_Q, query, CalendarPage.FIELD_FROM, from.toString(), CalendarPage.FIELD_TO, through.toString()));
            if (page > 0) {
                filters.put(CalendarPage.FIELD_PAGE, Integer.toString(page - 1));
                section.appendChild(anchor(url(filters), CalendarPage.TEXT_PREVIOUS_PAGE));
            }
            if (page + 1 < totalPages) {
                filters.put(CalendarPage.FIELD_PAGE, Integer.toString(page + 1));
                section.appendChild(anchor(url(filters), CalendarPage.TEXT_NEXT_PAGE));
            }
        }
        return root.appendChild(editor());
    }

    private void eventRow(Element body, CalendarRecurrence.Occurrence occurrence) {
        CalendarEvent event = occurrence.event();
        Element row = body.appendElement(CalendarPage.FIELD_TR);
        Element description = row.appendElement(CalendarPage.FIELD_TD);
        description.appendElement(CalendarPage.FIELD_STRONG).text(event.title());
        description.appendElement(CalendarPage.FIELD_DIV).addClass(CalendarPage.TEXT_CALENDAR_DESCRIPTION).text(event.description());
        row.appendElement(CalendarPage.FIELD_TD).text(occurrence.dateLabel() + (event.allDay() ? CalendarPage.TEXT_ALL_DAY : CalendarPage.EMPTY_TEXT));
        Element actions = row.appendElement(CalendarPage.FIELD_TD).addClass(CalendarPage.TEXT_CALENDAR_ROW_ACTIONS);
        actions.appendChild(anchor(url(Map.of(CalendarPage.FIELD_EDIT, Long.toString(event.id()), CalendarPage.FIELD_MONTH, calendar.month().toString())) + CalendarPage.TEXT_EVENT_EDITOR,
                event.recurrenceRule().isEmpty() ? CalendarPage.TEXT_EDIT : CalendarPage.TEXT_EDIT_SERIES));
        if (!event.recurrenceRule().isEmpty()) {
            actions.appendChild(anchor(url(Map.of(CalendarPage.FIELD_EDIT, Long.toString(event.id()), CalendarPage.FIELD_OCCURRENCE, occurrence.originalStart().toString(),
                    CalendarPage.FIELD_MONTH, calendar.month().toString())) + CalendarPage.TEXT_EVENT_EDITOR, CalendarPage.TEXT_EDIT_OCCURRENCE));
        }
        actions.appendChild(anchor(url(Map.of(CalendarPage.FIELD_EXPORT, Long.toString(event.id()))), CalendarPage.TEXT_EXPORT));
        Element deletion = actions.appendElement(CalendarPage.FIELD_DETAILS);
        deletion.appendElement(CalendarPage.FIELD_SUMMARY).text(CalendarPage.TEXT_DELETE);
        Element form = form(CalendarPage.FIELD_DELETE).appendChild(hidden(CalendarPage.FIELD_ID, Long.toString(event.id())))
                .appendChild(hidden(CalendarPage.FIELD_VERSION, Long.toString(event.version())));
        if (!event.recurrenceRule().isEmpty()) {
            form.appendChild(select(CalendarPage.FIELD_SCOPE, CalendarPage.TEXT_DELETE, CalendarPage.FIELD_OCCURRENCE, Map.of(CalendarPage.FIELD_OCCURRENCE, CalendarPage.TEXT_THIS_OCCURRENCE, CalendarPage.FIELD_SERIES, CalendarPage.TEXT_ENTIRE_SERIES)));
            form.appendChild(hidden(CalendarPage.FIELD_OCCURRENCE, occurrence.originalStart().toString()));
        }
        Element confirmation = form.appendElement(CalendarPage.FIELD_LABEL).addClass(CalendarPage.TEXT_CALENDAR_CHECKBOX);
        confirmation.appendElement(CalendarPage.FIELD_INPUT).attr(CalendarPage.FIELD_TYPE, CalendarPage.FIELD_CHECKBOX).attr(CalendarPage.FIELD_NAME, CalendarPage.FIELD_CONFIRM).attr(CalendarPage.FIELD_REQUIRED, true);
        confirmation.appendText(CalendarPage.TEXT_CONFIRM_DELETION);
        form.appendChild(button(CalendarPage.TEXT_DELETE).addClass(CalendarPage.TEXT_CALENDAR_DANGER));
        deletion.appendChild(form);
    }

    private Element toolbar() {
        Element toolbar = element(CalendarPage.FIELD_DIV).addClass(CalendarPage.TEXT_CALENDAR_TOOLBAR);
        toolbar.appendElement(CalendarPage.FIELD_H2).attr(CalendarPage.FIELD_ID, CalendarPage.TEXT_CALENDAR_MONTH).text(calendar.label());
        Element actions = toolbar.appendElement(CalendarPage.FIELD_DIV).addClass(CalendarPage.TEXT_CALENDAR_ACTIONS);
        if (calendar.month().getYear() != 1 || calendar.month().getMonthValue() != 1) {
            actions.appendChild(arrow(url(Map.of(CalendarPage.FIELD_MONTH, calendar.month().minusMonths(1).toString(), CalendarPage.FIELD_VIEW, CalendarPage.FIELD_MONTH)), CalendarPage.TEXT_SYMBOL, CalendarPage.TEXT_PREVIOUS_MONTH));
        }
        actions.appendChild(anchor(url(Map.of(CalendarPage.FIELD_VIEW, CalendarPage.FIELD_MONTH)), CalendarPage.TEXT_TODAY));
        if (calendar.month().getYear() != 9999 || calendar.month().getMonthValue() != 12) {
            actions.appendChild(arrow(url(Map.of(CalendarPage.FIELD_MONTH, calendar.month().plusMonths(1).toString(), CalendarPage.FIELD_VIEW, CalendarPage.FIELD_MONTH)), CalendarPage.TEXT_SYMBOL_203A, CalendarPage.TEXT_NEXT_MONTH));
        }
        Element jump = toolbar.appendElement(CalendarPage.FIELD_FORM).attr(CalendarPage.FIELD_METHOD, CalendarPage.FIELD_GET).addClass(CalendarPage.TEXT_CALENDAR_JUMP).attr(CalendarPage.FIELD_ACTION, context + CalendarPage.TEXT_CALENDAR);
        jump.appendChild(hidden(CalendarPage.FIELD_VIEW, CalendarPage.FIELD_MONTH));
        jump.appendChild(input(CalendarPage.FIELD_MONTH, CalendarPage.TEXT_MONTH, CalendarPage.FIELD_MONTH, calendar.month().toString(), Map.of(CalendarPage.FIELD_REQUIRED, CalendarPage.EMPTY_TEXT, CalendarPage.FIELD_MIN, CalendarPage.TEXT_0001_01, CalendarPage.FIELD_MAX, CalendarPage.TEXT_9999_12)));
        jump.appendChild(button(CalendarPage.TEXT_VIEW));
        return toolbar;
    }

    private Element grid(List<CalendarRecurrence.Occurrence> occurrences) {
        Element table = element(CalendarPage.FIELD_TABLE).addClass(CalendarPage.TEXT_CALENDAR_GRID).attr(CalendarPage.TEXT_ARIA_LABELLEDBY, CalendarPage.TEXT_CALENDAR_MONTH);
        Element header = table.appendElement(CalendarPage.FIELD_THEAD).appendElement(CalendarPage.FIELD_TR);
        for (int offset = 0; offset < 7; offset++) {
            DayOfWeek day = DayOfWeek.of((preferences.weekStart() - 1 + offset) % 7 + 1);
            header.appendElement(CalendarPage.FIELD_TH).attr(CalendarPage.FIELD_SCOPE, CalendarPage.FIELD_COL).appendElement(CalendarPage.FIELD_ABBR)
                    .attr(CalendarPage.FIELD_TITLE, day.getDisplayName(TextStyle.FULL, Locale.ENGLISH)).text(day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        }
        Element body = table.appendElement(CalendarPage.FIELD_TBODY);
        Element row = body;
        int leading = (calendar.month().atDay(1).getDayOfWeek().getValue() - preferences.weekStart() + 7) % 7;
        for (int cell = 0; cell < 42; cell++) {
            if (cell % 7 == 0) {
                row = body.appendElement(CalendarPage.FIELD_TR);
            }
            Element dateCell = row.appendElement(CalendarPage.FIELD_TD);
            int day = cell - leading + 1;
            if (day < 1 || day > calendar.dayCount()) {
                dateCell.addClass(CalendarPage.TEXT_CALENDAR_EMPTY);
                continue;
            }
            LocalDate date = calendar.month().atDay(day);
            if (calendar.isToday(day)) {
                dateCell.addClass(CalendarPage.TEXT_CALENDAR_TODAY).attr(CalendarPage.TEXT_ARIA_CURRENT, CalendarPage.FIELD_DATE);
            }
            dateCell.appendElement(CalendarPage.FIELD_TIME).attr(CalendarPage.FIELD_DATETIME, date.toString()).text(Integer.toString(day));
            List<CalendarRecurrence.Occurrence> daily = occurrences.stream().filter(occurrence ->
                    occurrence.start().isBefore(date.plusDays(1).atStartOfDay()) && occurrence.end().isAfter(date.atStartOfDay())).toList();
            for (CalendarRecurrence.Occurrence occurrence : daily.stream().limit(3).toList()) {
                dateCell.appendChild(anchor(url(Map.of(CalendarPage.FIELD_EDIT, Long.toString(occurrence.event().id()), CalendarPage.FIELD_MONTH, calendar.month().toString(),
                        CalendarPage.FIELD_OCCURRENCE, occurrence.event().recurrenceRule().isEmpty() ? CalendarPage.EMPTY_TEXT : occurrence.originalStart().toString()))
                        + CalendarPage.TEXT_EVENT_EDITOR, occurrence.event().title()).addClass(CalendarPage.TEXT_CALENDAR_EVENT));
            }
            if (daily.size() > 3) {
                dateCell.appendChild(anchor(url(Map.of(CalendarPage.FIELD_VIEW, CalendarPage.FIELD_AGENDA, CalendarPage.FIELD_FROM, date.toString(), CalendarPage.FIELD_TO, date.toString())), CalendarPage.TEXT_SYMBOL_2B + (daily.size() - 3) + CalendarPage.TEXT_MORE));
            }
        }
        return table;
    }

    private Element editor() throws SQLException {
        boolean submitted = Boolean.TRUE.equals(request.getAttribute(CalendarPage.TEXT_CALENDAR_FORM));
        String editing = submitted ? value(CalendarPage.FIELD_ID, CalendarPage.TEXT_0) : value(CalendarPage.FIELD_EDIT, CalendarPage.FIELD_NEW);
        CalendarEvent event = editing.equals(CalendarPage.FIELD_NEW) || editing.equals(CalendarPage.TEXT_0) ? null : store.find(owner, Long.parseLong(editing));
        String original = value(CalendarPage.FIELD_OCCURRENCE, CalendarPage.EMPTY_TEXT);
        boolean single = event != null && !original.isBlank();
        LocalDateTime start = event == null ? calendar.today().atTime(10, 0) : single ? LocalDateTime.parse(original) : event.start();
        LocalDateTime end = event == null ? start.plusHours(1) : start.plus(Duration.between(event.start(), event.end()));
        Map<String, String> rule = event == null || single ? Map.of() : CalendarRecurrence.parts(event.recurrenceRule());
        String ruleUntil = rule.get(CalendarPage.TEXT_UNTIL);
        String until = ruleUntil != null ? LocalDate.parse(ruleUntil.substring(0, 8), DateTimeFormatter.BASIC_ISO_DATE).toString()
                : start.toLocalDate().plusYears(1).toString();
        boolean allDay = submitted ? CalendarPage.FIELD_ON.equals(value(CalendarPage.FIELD_ALL_DAY, CalendarPage.EMPTY_TEXT)) : event != null && event.allDay();
        Element editor = element(CalendarPage.FIELD_DETAILS).attr(CalendarPage.FIELD_ID, CalendarPage.TEXT_EVENT_EDITOR_D16ED800).addClass(CalendarPage.TEXT_CALENDAR_EDITOR);
        editor.attr(CalendarPage.FIELD_OPEN, request.getParameter(CalendarPage.FIELD_EDIT) != null || submitted);
        editor.appendElement(CalendarPage.FIELD_SUMMARY).text(event == null ? CalendarPage.TEXT_NEW_EVENT : single ? CalendarPage.TEXT_EDIT_OCCURRENCE : CalendarPage.TEXT_EDIT_SERIES_EVENT);
        Element form = form(CalendarPage.FIELD_SAVE).appendChild(hidden(CalendarPage.FIELD_ID, event == null ? CalendarPage.TEXT_0 : Long.toString(event.id())))
                .appendChild(hidden(CalendarPage.FIELD_VERSION, submitted ? value(CalendarPage.FIELD_VERSION, CalendarPage.TEXT_1) : event == null ? CalendarPage.TEXT_1 : Long.toString(event.version())))
                .appendChild(hidden(CalendarPage.FIELD_SCOPE, single ? CalendarPage.FIELD_OCCURRENCE : CalendarPage.FIELD_SERIES)).appendChild(hidden(CalendarPage.FIELD_OCCURRENCE, original));
        form.appendChild(input(CalendarPage.FIELD_TITLE, CalendarPage.TEXT_TITLE, CalendarPage.FIELD_TEXT, submitted ? value(CalendarPage.FIELD_TITLE, CalendarPage.EMPTY_TEXT) : event == null ? CalendarPage.EMPTY_TEXT : event.title(), Map.of(CalendarPage.FIELD_REQUIRED, CalendarPage.EMPTY_TEXT, CalendarPage.FIELD_MAXLENGTH, CalendarPage.TEXT_200)));
        form.appendElement(CalendarPage.FIELD_LABEL).attr(CalendarPage.FIELD_FOR, CalendarPage.FIELD_DESCRIPTION).text(CalendarPage.TEXT_DESCRIPTION);
        form.appendElement(CalendarPage.FIELD_TEXTAREA).attr(CalendarPage.FIELD_ID, CalendarPage.FIELD_DESCRIPTION).attr(CalendarPage.FIELD_NAME, CalendarPage.FIELD_DESCRIPTION).attr(CalendarPage.FIELD_MAXLENGTH, CalendarPage.TEXT_5000).attr(CalendarPage.FIELD_ROWS, CalendarPage.TEXT_3)
                .text(submitted ? value(CalendarPage.FIELD_DESCRIPTION, CalendarPage.EMPTY_TEXT) : event == null ? CalendarPage.EMPTY_TEXT : event.description());
        Element checkbox = form.appendElement(CalendarPage.FIELD_LABEL).addClass(CalendarPage.TEXT_CALENDAR_CHECKBOX);
        checkbox.appendElement(CalendarPage.FIELD_INPUT).attr(CalendarPage.FIELD_TYPE, CalendarPage.FIELD_CHECKBOX).attr(CalendarPage.FIELD_NAME, CalendarPage.FIELD_ALL_DAY).attr(CalendarPage.FIELD_ID, CalendarPage.FIELD_ALL_DAY).attr(CalendarPage.FIELD_CHECKED, allDay);
        checkbox.appendText(CalendarPage.TEXT_ALL_DAY_2CC2687D);
        Element fields = form.appendElement(CalendarPage.FIELD_DIV).addClass(CalendarPage.TEXT_CALENDAR_FORM_GRID);
        Map<String, String> dateAttributes = Map.of(CalendarPage.FIELD_REQUIRED, CalendarPage.EMPTY_TEXT, CalendarPage.FIELD_MIN, CalendarPage.TEXT_1900_01_01, CalendarPage.FIELD_MAX, CalendarPage.TEXT_9998_12_31);
        fields.appendChild(input(CalendarPage.FIELD_START_DATE, CalendarPage.TEXT_START_DATE, CalendarPage.FIELD_DATE, submitted ? value(CalendarPage.FIELD_START_DATE, CalendarPage.EMPTY_TEXT) : start.toLocalDate().toString(), dateAttributes));
        fields.appendChild(input(CalendarPage.FIELD_START_TIME, CalendarPage.TEXT_START_TIME, CalendarPage.FIELD_TIME, submitted ? value(CalendarPage.FIELD_START_TIME, CalendarPage.EMPTY_TEXT) : start.toLocalTime().toString(), Map.of(CalendarPage.FIELD_CLASS, CalendarPage.TEXT_CALENDAR_TIME)));
        fields.appendChild(input(CalendarPage.FIELD_END_DATE, CalendarPage.TEXT_LAST_DAY_END_DATE, CalendarPage.FIELD_DATE, submitted ? value(CalendarPage.FIELD_END_DATE, CalendarPage.EMPTY_TEXT) : (allDay ? end.minusDays(1) : end).toLocalDate().toString(), dateAttributes));
        fields.appendChild(input(CalendarPage.FIELD_END_TIME, CalendarPage.TEXT_END_TIME, CalendarPage.FIELD_TIME, submitted ? value(CalendarPage.FIELD_END_TIME, CalendarPage.EMPTY_TEXT) : end.toLocalTime().toString(), Map.of(CalendarPage.FIELD_CLASS, CalendarPage.TEXT_CALENDAR_TIME)));
        fields.appendChild(zones(submitted ? value(CalendarPage.FIELD_ZONE_ID, preferences.zoneId()) : event == null ? preferences.zoneId() : event.zoneId()));
        if (!single) {
            fields.appendChild(select(CalendarPage.FIELD_FREQUENCY, CalendarPage.TEXT_REPEAT, submitted ? value(CalendarPage.FIELD_FREQUENCY, CalendarPage.TEXT_NONE) : rule.getOrDefault(CalendarPage.TEXT_FREQ, CalendarPage.TEXT_NONE),
                    Map.of(CalendarPage.TEXT_NONE, CalendarPage.TEXT_DOES_NOT_REPEAT, CalendarPage.TEXT_DAILY, CalendarPage.TEXT_DAILY_3EBEDF9, CalendarPage.TEXT_WEEKLY, CalendarPage.TEXT_WEEKLY_9A346CA1, CalendarPage.TEXT_MONTHLY, CalendarPage.TEXT_MONTHLY_ACEE27ED)));
            fields.appendChild(select(CalendarPage.FIELD_END_MODE, CalendarPage.TEXT_REPEAT_ENDS, submitted ? value(CalendarPage.FIELD_END_MODE, CalendarPage.FIELD_COUNT) : ruleUntil != null ? CalendarPage.FIELD_UNTIL : CalendarPage.FIELD_COUNT,
                    Map.of(CalendarPage.FIELD_COUNT, CalendarPage.TEXT_AFTER_OCCURRENCES, CalendarPage.FIELD_UNTIL, CalendarPage.TEXT_ON_DATE)));
            fields.appendChild(input(CalendarPage.FIELD_COUNT, CalendarPage.TEXT_OCCURRENCES, CalendarPage.FIELD_NUMBER, submitted ? value(CalendarPage.FIELD_COUNT, CalendarPage.TEXT_10) : rule.getOrDefault(CalendarPage.TEXT_COUNT, CalendarPage.TEXT_10), Map.of(CalendarPage.FIELD_MIN, CalendarPage.TEXT_1, CalendarPage.FIELD_MAX, CalendarPage.TEXT_999)));
            fields.appendChild(input(CalendarPage.FIELD_UNTIL, CalendarPage.TEXT_REPEAT_UNTIL, CalendarPage.FIELD_DATE, submitted ? value(CalendarPage.FIELD_UNTIL, until) : until, Map.of(CalendarPage.FIELD_MAX, CalendarPage.TEXT_9998_12_31)));
        }
        fields.appendChild(select(CalendarPage.FIELD_REMINDER, CalendarPage.TEXT_REMINDER, submitted ? value(CalendarPage.FIELD_REMINDER, CalendarPage.TEXT_1_5A4) : event == null ? CalendarPage.TEXT_1_5A4 : Integer.toString(event.reminderMinutes()),
                Map.of(CalendarPage.TEXT_1_5A4, CalendarPage.TEXT_NONE_252358, CalendarPage.TEXT_0, CalendarPage.TEXT_AT_START, CalendarPage.TEXT_5, CalendarPage.TEXT_5_MINUTES_BEFORE, CalendarPage.TEXT_15, CalendarPage.TEXT_15_MINUTES_BEFORE, CalendarPage.TEXT_30, CalendarPage.TEXT_30_MINUTES_BEFORE,
                        CalendarPage.TEXT_60, CalendarPage.TEXT_1_HOUR_BEFORE, CalendarPage.TEXT_1440, CalendarPage.TEXT_1_DAY_BEFORE, CalendarPage.TEXT_10080, CalendarPage.TEXT_1_WEEK_BEFORE)));
        form.appendElement(CalendarPage.FIELD_DIV).addClass(CalendarPage.TEXT_CALENDAR_ACTIONS).appendChild(button(CalendarPage.TEXT_SAVE_EVENT))
                .appendChild(anchor(url(Map.of(CalendarPage.FIELD_MONTH, calendar.month().toString())), CalendarPage.TEXT_CANCEL));
        return editor.appendChild(form);
    }

    private Element settings() {
        Element section = section(CalendarPage.TEXT_PREFERENCES);
        Element form = form(CalendarPage.FIELD_PREFERENCES).appendChild(zones(preferences.zoneId()))
                .appendChild(select(CalendarPage.FIELD_WEEK_START, CalendarPage.TEXT_WEEK_STARTS, Integer.toString(preferences.weekStart()), Map.of(CalendarPage.TEXT_1, CalendarPage.TEXT_MONDAY, CalendarPage.TEXT_7, CalendarPage.TEXT_SUNDAY)))
                .appendChild(select(CalendarPage.FIELD_DEFAULT_VIEW, CalendarPage.TEXT_DEFAULT_VIEW, preferences.defaultView(), Map.of(CalendarPage.FIELD_MONTH, CalendarPage.TEXT_MONTH, CalendarPage.FIELD_AGENDA, CalendarPage.TEXT_AGENDA)))
                .appendChild(button(CalendarPage.TEXT_SAVE_PREFERENCES));
        return section.appendChild(form);
    }

    private Element history() throws SQLException {
        int page = page();
        List<CalendarStore.AuditEntry> entries = store.history(owner, page);
        Element section = section(CalendarPage.TEXT_EVENT_HISTORY);
        if (entries.isEmpty()) {
            section.appendElement(CalendarPage.FIELD_P).text(CalendarPage.TEXT_NO_HISTORY_ON_THIS_PAGE);
        }
        for (CalendarStore.AuditEntry entry : entries) {
            Element details = section.appendElement(CalendarPage.FIELD_DETAILS).addClass(CalendarPage.TEXT_CALENDAR_HISTORY);
            details.appendElement(CalendarPage.FIELD_SUMMARY).text(timestamp(entry.changedMillis()) + CalendarPage.TEXT_SYMBOL_8E69 + entry.action() + CalendarPage.TEXT_SYMBOL_8E69 + entry.title());
            details.appendElement(CalendarPage.FIELD_PRE).text(entry.details());
        }
        if (page > 0) {
            section.appendChild(anchor(url(Map.of(CalendarPage.FIELD_VIEW, CalendarPage.FIELD_HISTORY, CalendarPage.FIELD_PAGE, Integer.toString(page - 1))), CalendarPage.TEXT_PREVIOUS_PAGE));
        }
        if (entries.size() == 50) {
            section.appendChild(anchor(url(Map.of(CalendarPage.FIELD_VIEW, CalendarPage.FIELD_HISTORY, CalendarPage.FIELD_PAGE, Integer.toString(page + 1))), CalendarPage.TEXT_NEXT_PAGE));
        }
        return section;
    }

    private Element notifications() throws SQLException {
        Element section = section(CalendarPage.TEXT_NOTIFICATIONS);
        List<CalendarStore.Notification> notifications = store.notifications(owner);
        if (notifications.isEmpty()) {
            section.appendElement(CalendarPage.FIELD_P).text(CalendarPage.TEXT_NO_REMINDERS_YET);
        }
        for (CalendarStore.Notification notification : notifications) {
            Element row = section.appendElement(CalendarPage.FIELD_DIV).addClass(CalendarPage.TEXT_CALENDAR_NOTIFICATION);
            Element description = row.appendElement(CalendarPage.FIELD_DIV);
            description.appendElement(CalendarPage.FIELD_STRONG).text(notification.title());
            description.appendElement(CalendarPage.FIELD_P).text(timestamp(notification.dueMillis()) + (notification.read() ? CalendarPage.TEXT_READ : CalendarPage.TEXT_UNREAD));
            if (!notification.read()) {
                row.appendChild(form(CalendarPage.FIELD_READ).appendChild(hidden(CalendarPage.FIELD_ID, Long.toString(notification.id()))).appendChild(button(CalendarPage.TEXT_MARK_READ)));
            }
        }
        return section;
    }

    private Element imports() {
        Element section = section(CalendarPage.TEXT_IMPORT_CALENDAR);
        Element upload = element(CalendarPage.FIELD_FORM).attr(CalendarPage.FIELD_METHOD, CalendarPage.FIELD_POST).attr(CalendarPage.FIELD_ENCTYPE, CalendarPage.TEXT_MULTIPART_FORM_DATA).attr(CalendarPage.FIELD_ACTION, context + CalendarPage.TEXT_CALENDAR_ACTION);
        upload.appendChild(hidden(CalendarPage.FIELD_ACTION, CalendarPage.FIELD_PREVIEW)).appendChild(hidden(CalendarPage.FIELD_CSRF, csrf)).appendChild(hidden(CalendarPage.FIELD_VIEW, CalendarPage.FIELD_IMPORT));
        upload.appendElement(CalendarPage.FIELD_LABEL).attr(CalendarPage.FIELD_FOR, CalendarPage.TEXT_CALENDAR_FILE).text(CalendarPage.TEXT_I_CALENDAR_FILE);
        upload.appendElement(CalendarPage.FIELD_INPUT).attr(CalendarPage.FIELD_TYPE, CalendarPage.FIELD_FILE).attr(CalendarPage.FIELD_ID, CalendarPage.TEXT_CALENDAR_FILE).attr(CalendarPage.FIELD_NAME, CalendarPage.FIELD_FILE).attr(CalendarPage.FIELD_ACCEPT, CalendarPage.TEXT_ICS_TEXT_CALENDAR).attr(CalendarPage.FIELD_REQUIRED, true);
        section.appendChild(upload.appendChild(button(CalendarPage.TEXT_PREVIEW_IMPORT)));
        Object preview = request.getSession(false).getAttribute(CalendarPage.TEXT_CALENDAR_IMPORT_EVENTS);
        if (preview instanceof List<?> events) {
            section.appendElement(CalendarPage.FIELD_H3).text(CalendarPage.TEXT_IMPORT_PREVIEW);
            section.appendElement(CalendarPage.FIELD_P).text(events.size() + CalendarPage.TEXT_EVENTS_EXISTING_UIDS_WILL_BE_SKIPPED);
            Element list = section.appendElement(CalendarPage.FIELD_UL);
            for (Object item : events) {
                CalendarEvent event = (CalendarEvent) item;
                list.appendElement(CalendarPage.FIELD_LI).text(event.title() + CalendarPage.TEXT_SYMBOL_8E69 + event.start());
            }
            section.appendChild(form(CalendarPage.FIELD_IMPORT).appendChild(hidden(CalendarPage.FIELD_PREVIEW_ID, String.valueOf(request.getSession(false).getAttribute(CalendarPage.TEXT_CALENDAR_IMPORT_ID))))
                    .appendChild(button(CalendarPage.TEXT_IMPORT_EVENTS)));
            section.appendChild(form(CalendarPage.FIELD_CANCEL_IMPORT).appendChild(button(CalendarPage.TEXT_CANCEL_IMPORT)));
        }
        return section;
    }

    private Element form(String action) {
        return element(CalendarPage.FIELD_FORM).attr(CalendarPage.FIELD_METHOD, CalendarPage.FIELD_POST).attr(CalendarPage.FIELD_ACTION, context + CalendarPage.TEXT_CALENDAR_ACTION)
                .appendChild(hidden(CalendarPage.FIELD_CSRF, csrf)).appendChild(hidden(CalendarPage.FIELD_ACTION, action)).appendChild(hidden(CalendarPage.FIELD_MONTH, calendar.month().toString()));
    }

    private Element zones(String selected) {
        List<String> zones = new ArrayList<>(ZoneId.getAvailableZoneIds());
        if (!zones.contains(selected)) {
            zones.add(selected);
        }
        Map<String, String> choices = new TreeMap<>();
        zones.forEach(zone -> choices.put(zone, zone));
        return select(CalendarPage.FIELD_ZONE_ID, CalendarPage.TEXT_TIME_ZONE, selected, choices);
    }

    private static Element select(String name, String label, String selected, Map<String, String> choices) {
        Element field = element(CalendarPage.FIELD_DIV).addClass(CalendarPage.TEXT_CALENDAR_FIELD);
        field.appendElement(CalendarPage.FIELD_LABEL).attr(CalendarPage.FIELD_FOR, name).text(label);
        Element select = field.appendElement(CalendarPage.FIELD_SELECT).attr(CalendarPage.FIELD_ID, name).attr(CalendarPage.FIELD_NAME, name);
        Map<String, String> options = new TreeMap<>(choices);
        options.putIfAbsent(selected, selected);
        options.forEach((key, value) -> select.appendElement(CalendarPage.FIELD_OPTION).attr(CalendarPage.FIELD_VALUE, key).attr(CalendarPage.FIELD_SELECTED, key.equals(selected)).text(value));
        return field;
    }

    private static Element input(String name, String label, String type, String value, Map<String, String> attributes) {
        Element field = element(CalendarPage.FIELD_DIV).addClass(CalendarPage.TEXT_CALENDAR_FIELD);
        field.appendElement(CalendarPage.FIELD_LABEL).attr(CalendarPage.FIELD_FOR, name).text(label);
        Element input = field.appendElement(CalendarPage.FIELD_INPUT).attr(CalendarPage.FIELD_ID, name).attr(CalendarPage.FIELD_NAME, name).attr(CalendarPage.FIELD_TYPE, type).attr(CalendarPage.FIELD_VALUE, value);
        attributes.forEach(input::attr);
        return field;
    }

    private static Element hidden(String name, String value) {
        return element(CalendarPage.FIELD_INPUT).attr(CalendarPage.FIELD_TYPE, CalendarPage.FIELD_HIDDEN).attr(CalendarPage.FIELD_NAME, name).attr(CalendarPage.FIELD_VALUE, value);
    }

    private static Element button(String label) {
        return element(CalendarPage.FIELD_BUTTON).attr(CalendarPage.FIELD_TYPE, CalendarPage.FIELD_SUBMIT).text(label);
    }

    private static Element section(String title) {
        Element section = element(CalendarPage.FIELD_SECTION);
        section.appendElement(CalendarPage.FIELD_H2).text(title);
        return section;
    }

    private String url(Map<String, String> values) {
        return context + CalendarPage.TEXT_CALENDAR_8FFC3172 + values.entrySet().stream().map(entry -> encode(entry.getKey()) + CalendarPage.TEXT_SYMBOL_3D + encode(entry.getValue()))
                .collect(java.util.stream.Collectors.joining(CalendarPage.TEXT_SYMBOL_26));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static Element anchor(String url, String label) {
        return element(CalendarPage.FIELD_A).attr(CalendarPage.FIELD_HREF, url).text(label);
    }

    private static Element arrow(String url, String symbol, String label) {
        return anchor(url, symbol).addClass(CalendarPage.TEXT_CALENDAR_ARROW).attr(CalendarPage.TEXT_ARIA_LABEL, label).attr(CalendarPage.FIELD_TITLE, label);
    }

    private static Element element(String tag) {
        return new Element(tag);
    }

    private static String html(Element element) {
        Document document = new Document(CalendarPage.EMPTY_TEXT).outputSettings(new Document.OutputSettings().prettyPrint(false));
        document.appendChild(element);
        return element.outerHtml();
    }

    private int page() {
        int page = Integer.parseInt(value(CalendarPage.FIELD_PAGE, CalendarPage.TEXT_0));
        if (page < 0 || page > 10000) {
            throw new IllegalArgumentException(CalendarPage.TEXT_INVALID_PAGE_NUMBER);
        }
        return page;
    }

    private String value(String name, String fallback) {
        String value = request.getParameter(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    private String timestamp(long millis) {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.of(preferences.zoneId()))
                .format(DateTimeFormatter.ofPattern(CalendarPage.TEXT_YYYY_MM_DD_HH_MM_Z, Locale.ENGLISH));
    }

    static String esc(String value) {
        return TemplateRenderer.escapeHtml(value);
    }
}
