package com.app.calendar;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.MockMakers.PROXY;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.app.database.DatabaseMigrations;
import com.app.support.JdbcTestSupport;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

class CalendarServletTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletContext context;
    private JDBCDataSource dataSource;
    private ByteArrayOutputStream output;
    private Map<String, String> parameters;

    @BeforeEach
    void setUp() throws Exception {
        request = mock(HttpServletRequest.class, withSettings().mockMaker(PROXY));
        response = mock(HttpServletResponse.class, withSettings().mockMaker(PROXY));
        context = mock(ServletContext.class, withSettings().mockMaker(PROXY));
        dataSource = JdbcTestSupport.newDataSource();
        DatabaseMigrations.migrate(dataSource);
        JdbcTestSupport.insertUser(dataSource, "alice", "hash", "USER");
        parameters = new HashMap<>();
        when(request.getParameter(anyString())).thenAnswer(invocation -> parameters.get(invocation.getArgument(0)));
        when(request.getMethod()).thenReturn("GET");
        Map<String, Object> requestAttributes = new HashMap<>();
        when(request.getAttribute(anyString())).thenAnswer(invocation -> requestAttributes.get(invocation.getArgument(0)));
        doAnswer(invocation -> { requestAttributes.put(invocation.getArgument(0), invocation.getArgument(1)); return null; })
            .when(request).setAttribute(anyString(), any());
        HttpSession session = mock(HttpSession.class, withSettings().mockMaker(PROXY));
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("user", "alice");
        when(session.getAttribute(anyString())).thenAnswer(invocation -> attributes.get(invocation.getArgument(0)));
        doAnswer(invocation -> { attributes.put(invocation.getArgument(0), invocation.getArgument(1)); return null; })
            .when(session).setAttribute(anyString(), any());
        doAnswer(invocation -> { attributes.remove(invocation.getArgument(0)); return null; }).when(session).removeAttribute(anyString());
        when(request.getContextPath()).thenReturn("/app");
        when(request.getServletContext()).thenReturn(context);
        when(request.getSession(false)).thenReturn(session);
        when(context.getAttribute("datasource")).thenReturn(dataSource);
        when(context.getResourceAsStream("/WEB-INF/templates/calendar.html")).thenAnswer(invocation ->
                Files.newInputStream(Path.of("src/main/webapp/WEB-INF/templates/calendar.html")));
        output = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public void write(int value) {
                output.write(value);
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener listener) {
            }
        });
    }

    @Test
    void rendersCurrentMonthTodayAndContextRelativeNavigation() throws IOException, SQLException {
        new CalendarServlet().doGet(request, response);
        String html = output.toString(StandardCharsets.UTF_8);
        assertTrue(html.contains("datetime=\"" + LocalDate.now() + "\""));
        assertEquals(1, html.split("aria-current=\"date\"", -1).length - 1);
        assertTrue(html.contains("/app/calendar?month="));
        assertTrue(html.contains("href=\"/app/home\""));
        assertTrue(html.contains("name=\"month\""));
        assertFalse(html.contains("{{"));
        assertEquals(42, html.split("<td", -1).length - 1);
        assertEquals(0, new CalendarStore(dataSource).events("alice").size());
        verify(response).setHeader("Cache-Control", "no-store");
    }

    @Test
    void rendersLeapMonthAndYearBoundaryLinks() throws IOException {
        when(request.getParameterValues("month")).thenReturn(new String[] {"2024-02"});
        new CalendarServlet().doGet(request, response);
        String html = output.toString(StandardCharsets.UTF_8);
        assertTrue(html.contains("February 2024"));
        assertTrue(html.contains("datetime=\"2024-02-29\""));
        assertFalse(html.contains("datetime=\"2024-02-30\""));
        assertTrue(html.contains("month=2024-01"));
        assertTrue(html.contains("month=2024-03"));
    }

    @Test
    void rejectsInvalidMonthBeforeAccessingDatabase() throws IOException {
        when(request.getParameterValues("month")).thenReturn(new String[] {"2026-99"});
        new CalendarServlet().doGet(request, response);
        verify(response).sendError(eq(400), anyString());
        verify(context, never()).getAttribute("datasource");
    }

    @Test
    void requiresAuthenticatedSession() throws IOException {
        when(request.getSession(false)).thenReturn(null);
        new CalendarServlet().doGet(request, response);
        verify(response).sendError(eq(401), anyString());
        verify(context, never()).getAttribute("datasource");
    }

    @Test
    void reportsUnavailableStorageWithoutRenderingSuccess() throws Exception {
        DataSource unavailable = mock(DataSource.class, withSettings().mockMaker(PROXY));
        when(unavailable.getConnection()).thenThrow(new SQLException("database unavailable"));
        when(context.getAttribute("datasource")).thenReturn(unavailable);
        new CalendarServlet().doGet(request, response);
        verify(response).sendError(eq(503), anyString());
        verify(context).log(anyString(), any(SQLException.class));
        assertEquals(0, output.size());
    }

    @Test
    void reportsMissingDatasource() throws IOException {
        when(context.getAttribute("datasource")).thenReturn(null);
        new CalendarServlet().doGet(request, response);
        verify(response).sendError(eq(503), anyString());
    }

    @Test
    void escapesSessionUsername() throws Exception {
        HttpSession session = request.getSession(false);
        JdbcTestSupport.insertUser(dataSource, "<img src=x onerror=alert(1)>", "hash", "USER");
        when(session.getAttribute("user")).thenReturn("<img src=x onerror=alert(1)>");
        new CalendarServlet().doGet(request, response);
        String html = output.toString(StandardCharsets.UTF_8);
        assertTrue(html.contains("&lt;img src=x onerror=alert(1)&gt;"));
        assertFalse(html.contains("<img"));
    }

    @AfterEach
    void closeDatabase() throws Exception {
        try (Connection connection = dataSource.getConnection(); java.sql.Statement statement = connection.createStatement()) {
            statement.execute("SHUTDOWN");
        }
    }

    @Test
    void calendarWritesRequireCsrf() throws Exception {
        parameters.put("action", "save");
        new CalendarActionsServlet().doPost(request, response);
        verify(response).sendError(eq(403), anyString());
        assertTrue(new CalendarStore(dataSource).events("alice").isEmpty());
    }

    @Test
    void createsUpdatesRejectsStaleWritesAndDeletesUsingAuthenticatedOwner() throws Exception {
        eventParameters();
        parameters.put("username", "bob");
        new CalendarActionsServlet().doPost(request, response);
        verify(response).setStatus(303);
        CalendarStore store = new CalendarStore(dataSource);
        CalendarEvent saved = store.events("alice").get(0);
        assertEquals("alice", saved.username());
        parameters.put("id", Long.toString(saved.id()));
        parameters.put("version", "1");
        parameters.put("title", "Updated meeting");
        new CalendarActionsServlet().doPost(request, response);
        assertEquals(2, store.find("alice", saved.id()).version());
        new CalendarActionsServlet().doPost(request, response);
        verify(response).setStatus(409);
        assertEquals(2, store.history("alice", 0).size());
        parameters.put("action", "delete");
        parameters.put("version", "2");
        parameters.put("confirm", "on");
        new CalendarActionsServlet().doPost(request, response);
        assertTrue(store.events("alice").isEmpty());
        assertEquals(3, store.history("alice", 0).size());
    }

    @Test
    void rejectsAnotherAccountsEventEvenWithValidCsrf() throws Exception {
        eventParameters();
        new CalendarActionsServlet().doPost(request, response);
        long id = new CalendarStore(dataSource).events("alice").get(0).id();
        JdbcTestSupport.insertUser(dataSource, "bob", "hash", "USER");
        request.getSession(false).setAttribute("user", "bob");
        parameters.put("id", Long.toString(id));
        parameters.put("version", "1");
        new CalendarActionsServlet().doPost(request, response);
        verify(response).sendError(eq(404), anyString());
        assertEquals("Meeting", new CalendarStore(dataSource).find("alice", id).title());
    }

    @Test
    void savesPreferencesAndUsesThePreferredDefaultView() throws Exception {
        parameters.put("csrf", CalendarSecurity.token(request.getSession(false)));
        parameters.put("action", "preferences");
        parameters.put("zoneId", "Europe/London");
        parameters.put("weekStart", "1");
        parameters.put("defaultView", "agenda");
        new CalendarActionsServlet().doPost(request, response);
        assertEquals(new CalendarPreferences("Europe/London", 1, "agenda"), new CalendarStore(dataSource).preferences("alice"));
        parameters.clear();
        new CalendarServlet().doGet(request, response);
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("aria-current=\"page\">Agenda</a>"));
    }

    private void eventParameters() {
        when(request.getMethod()).thenReturn("POST");
        parameters.put("csrf", CalendarSecurity.token(request.getSession(false)));
        parameters.put("action", "save");
        parameters.put("title", "Meeting");
        parameters.put("startDate", "2026-10-23");
        parameters.put("endDate", "2026-10-23");
        parameters.put("startTime", "10:00");
        parameters.put("endTime", "11:00");
        parameters.put("zoneId", "UTC");
        parameters.put("month", "2026-10");
    }

    @Test
    void invalidEventKeepsEnteredFieldsAndDoesNotWrite() throws Exception {
        eventParameters();
        parameters.put("title", "Keep this title");
        parameters.put("endTime", "09:00");
        new CalendarActionsServlet().doPost(request, response);
        verify(response).setStatus(400);
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Keep this title"));
        assertTrue(new CalendarStore(dataSource).events("alice").isEmpty());
    }

    @Test
    void importRequiresPreviewAndExplicitConfirmation() throws Exception {
        String document = "BEGIN:VCALENDAR\r\nVERSION:2.0\r\nPRODID:-//Test//EN\r\nBEGIN:VEVENT\r\nUID:upload-id\r\n"
                + "DTSTAMP:20261023T000000Z\r\nDTSTART:20261023T100000Z\r\nDTEND:20261023T110000Z\r\nSUMMARY:Uploaded event\r\nEND:VEVENT\r\nEND:VCALENDAR\r\n";
        byte[] bytes = document.getBytes(StandardCharsets.UTF_8);
        jakarta.servlet.http.Part file = mock(jakarta.servlet.http.Part.class, withSettings().mockMaker(PROXY));
        when(file.getSize()).thenReturn((long) bytes.length);
        when(file.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(bytes));
        when(request.getPart("file")).thenReturn(file);
        String token = CalendarSecurity.token(request.getSession(false));
        parameters.put("action", "preview");
        parameters.put("csrf", token);
        new CalendarActionsServlet().doPost(request, response);
        assertTrue(new CalendarStore(dataSource).events("alice").isEmpty());
        parameters.put("previewId", String.valueOf(request.getSession(false).getAttribute("calendar.import.id")));
        parameters.put("action", "import");
        new CalendarActionsServlet().doPost(request, response);
        assertEquals(1, new CalendarStore(dataSource).events("alice").size());
        assertNull(request.getSession(false).getAttribute("calendar.import.preview"));
    }

    @Test
    void templateReadFailuresReturnControlledErrors() throws Exception {
        when(context.getResourceAsStream("/WEB-INF/templates/calendar.html")).thenReturn(new java.io.InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Internal template path must not be exposed");
            }
        });
        new CalendarServlet().doGet(request, response);
        verify(response).sendError(500, "Calendar response is unavailable");
        assertFalse(output.toString(StandardCharsets.UTF_8).contains("Internal template path"));
    }

    @Test
    void requestEncodingFailureIsHandledInsideTheServlet() throws Exception {
        doThrow(new java.io.UnsupportedEncodingException("Internal encoding detail"))
                .when(request).setCharacterEncoding("UTF-8");
        new CalendarActionsServlet().doPost(request, response);
        verify(response).sendError(500, "Calendar request is unavailable");
        assertTrue(new CalendarStore(dataSource).events("alice").isEmpty());
    }
}