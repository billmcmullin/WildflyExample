package com.example.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "NumberGuessServlet", urlPatterns = {"/numberguess"}) public class NumberGuessServlet extends HttpServlet {

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
        HttpSession session = req.getSession(true);

        // If new game requested or not initialized, start a new game
        if ("1".equals(req.getParameter("new")) || session.getAttribute(ATTR_SECRET) == null) {
            initNewGame(session);
        }

        int low = (int) session.getAttribute(ATTR_LOW);
        int high = (int) session.getAttribute(ATTR_HIGH);
        int remaining = (int) session.getAttribute(ATTR_REMAIN);
        @SuppressWarnings("unchecked")
        List<String> history = (List<String>) session.getAttribute(ATTR_HISTORY);
        String message = (String) session.getAttribute(ATTR_MESSAGE);
        boolean over = session.getAttribute(ATTR_OVER) != null && (boolean) session.getAttribute(ATTR_OVER);

        // Build candidate display: if small range, show explicit list; otherwise show range + count
        String candidatesHtml;
        int count = Math.max(0, high - low + 1);
        if (count <= 30) {
            StringBuilder sb = new StringBuilder();
            sb.append("<p>Possible numbers (").append(count).append("): ");
            for (int i = low; i <= high; i++) {
                if (i > low) {
                    sb.append(", ");
                }
                sb.append(i);
            }
            sb.append("</p>");
            candidatesHtml = sb.toString();
        } else {
            candidatesHtml = "<p>Possible numbers: [" + low + "," + high + "] (" + count + " candidates)</p>";
        }

        StringBuilder historyHtml = new StringBuilder();
        if (history != null && !history.isEmpty()) {
            historyHtml.append("<ul>");
            for (String h : history) {
                historyHtml.append("<li>").append(TemplateRenderer.escapeHtml(h)).append("</li>");
            }
            historyHtml.append("</ul>");
        }

        java.util.Map<String, String> vals = new java.util.HashMap<>();
        vals.put("ctx", TemplateRenderer.escapeHtml(req.getContextPath()));
        vals.put("low", String.valueOf(low));
        vals.put("high", String.valueOf(high));
        vals.put("remaining", String.valueOf(remaining));
        vals.put("message", message != null ? TemplateRenderer.escapeHtml(message) : "");
        vals.put("history", historyHtml.toString());
        vals.put("guess_value", ""); // blank default
        vals.put("game_over", over ? "true" : "false");
        vals.put("candidates", candidatesHtml);

        String html = TemplateRenderer.render(req.getServletContext(), "/WEB-INF/templates/numberguess.html", vals);
        resp.setContentType("text/html;charset=UTF-8");
        resp.getWriter().write(html);

        // Clear transient message after showing
        session.removeAttribute(ATTR_MESSAGE);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(true);

        // start new game if requested
        if ("1".equals(req.getParameter("new"))) {
            initNewGame(session);
            resp.sendRedirect(req.getContextPath() + "/numberguess");
            return;
        }

        // If not initialized, init
        if (session.getAttribute(ATTR_SECRET) == null) {
            initNewGame(session);
        }

        if (session.getAttribute(ATTR_OVER) != null && (boolean) session.getAttribute(ATTR_OVER)) {
            // Game already over — redirect to GET (user can start new game)
            resp.sendRedirect(req.getContextPath() + "/numberguess");
            return;
        }

        String guessParam = req.getParameter("guess");
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
            resp.sendRedirect(req.getContextPath() + "/numberguess");
            return;
        }

        int guess;
        try {
            guess = Integer.parseInt(guessParam.trim());
        } catch (NumberFormatException e) {
            session.setAttribute(ATTR_MESSAGE, "Invalid number format.");
            resp.sendRedirect(req.getContextPath() + "/numberguess");
            return;
        }

        if (guess < MIN || guess > MAX) {
            session.setAttribute(ATTR_MESSAGE, "Guess must be between " + MIN + " and " + MAX + ".");
            resp.sendRedirect(req.getContextPath() + "/numberguess");
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
            resp.sendRedirect(req.getContextPath() + "/numberguess");
            return;
        }

        // New narrowing logic: set the guessed number as the new bound
        if (guess < secret) {
            // guessed too low -> new low is guess + 1
            int newLow = guess + 1;
            if (newLow > low) {
                low = newLow;
            }
            history.add("Guess " + guess + " — Too low. New possible range: [" + low + "," + high + "].");
        } else {
            // guessed too high -> new high is guess - 1
            int newHigh = guess - 1;
            if (newHigh < high) {
                high = newHigh;
            }
            history.add("Guess " + guess + " — Too high. New possible range: [" + low + "," + high + "].");
        }

        // Keep bounds within [MIN,MAX]
        low = Math.max(low, MIN);
        high = Math.min(high, MAX);

        session.setAttribute(ATTR_LOW, low);
        session.setAttribute(ATTR_HIGH, high);
        session.setAttribute(ATTR_HISTORY, history);

        if (remaining <= 0) {
            session.setAttribute(ATTR_MESSAGE, "Out of guesses. The number was " + secret + ".");
            session.setAttribute(ATTR_OVER, true);
        } else {
            session.setAttribute(ATTR_MESSAGE, "Try a " + (guess < secret ? "higher" : "lower") + " number.");
        }

        // Redirect to GET to show results and avoid form re-submission
        resp.sendRedirect(req.getContextPath() + "/numberguess");
    }

    private void initNewGame(HttpSession session) {
        int secret = ThreadLocalRandom.current().nextInt(MIN, MAX + 1);
        session.setAttribute(ATTR_SECRET, secret);
        session.setAttribute(ATTR_LOW, MIN);
        session.setAttribute(ATTR_HIGH, MAX);
        session.setAttribute(ATTR_REMAIN, MAX_GUESSES);
        session.setAttribute(ATTR_HISTORY, new ArrayList<String>());
        session.setAttribute(ATTR_MESSAGE, null);
        session.setAttribute(ATTR_OVER, false);
        session.removeAttribute("ng.lastGuess");
    }

}
