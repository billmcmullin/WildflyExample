package com.app.game.numberguess;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.app.util.RequestParamContext;
import com.app.util.ServletResponseUtil;
import com.app.util.TemplateRenderer;
import com.app.util.UserDataService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "NumberGuessServlet", urlPatterns = {"/numberguess"})
public class NumberGuessServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String ATTR_SECRET = "ng.secret";
    private static final String ATTR_LOW = "ng.low";
    private static final String ATTR_HIGH = "ng.high";
    private static final String ATTR_REMAIN = "ng.remaining";
    private static final String ATTR_HISTORY = "ng.history";
    private static final String ATTR_MESSAGE = "ng.message";
    private static final String ATTR_OVER = "ng.over";

    private static final int MAX_GUESSES = 10;
    private static final int MIN = 1;
    private static final int MAX = 100;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RequestParamContext requestContext = new RequestParamContext(req);
        HttpSession session = req.getSession(true);

        // If new game requested or not initialized, start a new game
        if ("1".equals(requestContext.value("new")) || session.getAttribute(ATTR_SECRET) == null) {
            initNewGame(session);
        }

        int low = (int) session.getAttribute(ATTR_LOW);
        int high = (int) session.getAttribute(ATTR_HIGH);
        int remaining = (int) session.getAttribute(ATTR_REMAIN);
        @SuppressWarnings("unchecked")
        List<String> history = (List<String>) session.getAttribute(ATTR_HISTORY);
        String message = (String) session.getAttribute(ATTR_MESSAGE);
        boolean over = session.getAttribute(ATTR_OVER) != null && (boolean) session.getAttribute(ATTR_OVER);

        // Build candidate display: if small range, show explicit list; otherwise show range + count.
        String candidatesText;
        int count = Math.max(0, high - low + 1);
        if (count <= 30) {
            StringBuilder sb = new StringBuilder();
            sb.append("Possible numbers (").append(count).append("): ");
            for (int i = low; i <= high; i++) {
                if (i > low) {
                    sb.append(',');
                    sb.append(' ');
                }
                sb.append(i);
            }
            candidatesText = sb.toString();
        } else {
            candidatesText = "Possible numbers: [" + low + ',' + high + "] (" + count + " candidates)";
        }

        StringBuilder historyText = new StringBuilder();
        if (history != null && !history.isEmpty()) {
            for (String h : history) {
                if (!historyText.isEmpty()) {
                    historyText.append('\n');
                }
                historyText.append("- ").append(h);
            }
        }

        // Get current user for header
        String user = UserDataService.currentUser(req);

        // Determine admin link if the user is an ADMIN
        boolean showAdminLink = UserDataService.isAdmin(req.getServletContext(), user);

        java.util.Map<String, String> vals = new java.util.HashMap<>();
        vals.put("ctx", TemplateRenderer.escapeHtml(requestContext.contextPath()));
        vals.put("user", TemplateRenderer.escapeHtml(user));
        vals.put("admin_href", TemplateRenderer.escapeHtml(requestContext.appPath("/admin/users")));
        vals.put("admin_style", showAdminLink ? "" : " style=\"display:none\"");
        vals.put("low", String.valueOf(low));
        vals.put("high", String.valueOf(high));
        vals.put("remaining", String.valueOf(remaining));
        vals.put("message", message != null ? TemplateRenderer.escapeHtml(message) : "");
        vals.put("history_text", TemplateRenderer.escapeHtml(historyText.toString()));

        // Pre-fill last guess if available
        String last = (String) session.getAttribute("ng.lastGuess");
        vals.put("guess_value", last != null ? TemplateRenderer.escapeHtml(last) : "");

        vals.put("game_over", over ? "true" : "false");
        vals.put("candidates_text", TemplateRenderer.escapeHtml(candidatesText));

        String html = TemplateRenderer.render(req.getServletContext(), "/WEB-INF/templates/numberguess.html", vals);
        ServletResponseUtil.writeHtml(resp, html);

        // Clear transient message after showing
        session.removeAttribute(ATTR_MESSAGE);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        RequestParamContext requestContext = new RequestParamContext(req);
        HttpSession session = req.getSession(true);

        // start new game if requested
        if ("1".equals(requestContext.value("new"))) {
            initNewGame(session);
            safeRedirect(resp);
            return;
        }

        // If not initialized, init
        if (session.getAttribute(ATTR_SECRET) == null) {
            initNewGame(session);
        }

        if (session.getAttribute(ATTR_OVER) != null && (boolean) session.getAttribute(ATTR_OVER)) {
            // Game already over — redirect to GET (user can start new game)
            safeRedirect(resp);
            return;
        }

        String guessParam = requestContext.value("guess");
        int secret = (int) session.getAttribute(ATTR_SECRET);
        int low = (int) session.getAttribute(ATTR_LOW);
        int high = (int) session.getAttribute(ATTR_HIGH);
        int remaining = (int) session.getAttribute(ATTR_REMAIN);
        @SuppressWarnings("unchecked")
        List<String> history = (List<String>) session.getAttribute(ATTR_HISTORY);

        if (history == null) {
            history = new ArrayList<>();
            session.setAttribute(ATTR_HISTORY, history);
        }

        if (guessParam == null || guessParam.isBlank()) {
            session.setAttribute(ATTR_MESSAGE, "Please enter a number.");
            safeRedirect(resp);
            return;
        }

        int guess;
        try {
            guess = Integer.parseInt(guessParam.trim());
        } catch (NumberFormatException e) {
            session.setAttribute(ATTR_MESSAGE, "Invalid number format.");
            safeRedirect(resp);
            return;
        }

        if (guess < MIN || guess > MAX) {
            session.setAttribute(ATTR_MESSAGE, "Guess must be between " + MIN + " and " + MAX + '.');
            safeRedirect(resp);
            return;
        }

        remaining = Math.max(0, remaining - 1);
        session.setAttribute(ATTR_REMAIN, remaining);

        // Save the submitted guess value so the form can be pre-filled on next render
        session.setAttribute("ng.lastGuess", String.valueOf(guess));

        if (guess == secret) {
            history.add("Guess " + guess + " — Correct! You win.");
            session.setAttribute(ATTR_MESSAGE, "Correct! The number was " + secret + ". You win!");
            session.setAttribute(ATTR_OVER, true);
            safeRedirect(resp);
            return;
        }

        // New narrowing logic: set the guessed number as the new bound
        if (guess < secret) {
            // guessed too low -> new low is guess + 1
            int newLow = guess + 1;
            if (newLow > low) {
                low = newLow;
            }
            history.add("Guess " + guess + " — Too low. New possible range: [" + low + ',' + high + "].");
        } else {
            // guessed too high -> new high is guess - 1
            int newHigh = guess - 1;
            if (newHigh < high) {
                high = newHigh;
            }
            history.add("Guess " + guess + " — Too high. New possible range: [" + low + ',' + high + "].");
        }

        // Keep bounds within [MIN,MAX]
        low = Math.max(low, MIN);
        high = Math.min(high, MAX);

        session.setAttribute(ATTR_LOW, low);
        session.setAttribute(ATTR_HIGH, high);
        session.setAttribute(ATTR_HISTORY, history);

        if (remaining <= 0) {
            session.setAttribute(ATTR_MESSAGE, "Out of guesses. The number was " + secret + '.');
            session.setAttribute(ATTR_OVER, true);
        } else {
            session.setAttribute(ATTR_MESSAGE, "Try a " + (guess < secret ? "higher" : "lower") + " number.");
        }

        // Redirect to GET to show results and avoid form re-submission
        safeRedirect(resp);
    }

    private void initNewGame(HttpSession session) {
        int secret = ThreadLocalRandom.current().nextInt(MIN, MAX + 1);
        session.setAttribute(ATTR_SECRET, secret);
        session.setAttribute(ATTR_LOW, MIN);
        session.setAttribute(ATTR_HIGH, MAX);
        session.setAttribute(ATTR_REMAIN, MAX_GUESSES);
        session.setAttribute(ATTR_HISTORY, new ArrayList<String>());
        session.setAttribute(ATTR_MESSAGE, null);
        session.setAttribute(ATTR_OVER, true);
        session.removeAttribute("ng.lastGuess");
    }

    private void readObject(ObjectInputStream in) throws IOException {
        throw new NotSerializableException("NumberGuessServlet");
    }

    private static void safeRedirect(HttpServletResponse resp) {
        try {
            resp.sendRedirect("/numberguess");
        } catch (IOException ex) {
            ServletResponseUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Redirect failed");
        }
    }
}
