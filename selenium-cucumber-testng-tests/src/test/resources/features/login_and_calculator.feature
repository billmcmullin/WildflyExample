Feature: Selenium login and calculator navigation with Cucumber + TestNG
  As a browser test user
  I want to login and navigate to calculator
  So that the app flow is verified and coverage can be reported

  Scenario: Login and open calculator page
    Given the user opens the login page
    When the user logs in with configured credentials
    Then the home page is displayed
    When the user opens the calculator page
    Then the calculator page is displayed
