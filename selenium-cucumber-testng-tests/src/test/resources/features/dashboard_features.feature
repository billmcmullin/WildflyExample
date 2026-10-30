Feature: Dashboard links and application workflows
  Background:
    Given the user opens the login page
    When the user logs in with configured credentials
    Then the home page is displayed

  Scenario: Dashboard exposes every expected link
    Then all dashboard navigation and Quick links are correct

  Scenario Outline: Dashboard navigation opens its application page
    When the user opens "<link>" from dashboard navigation
    Then the application page "<path>" has title "<title>"

    Examples:
      | link         | path             | title                           |
      | Calculator   | /calculator      | Calculator                      |
      | Number Guess | /numberguess     | Number Guess                    |
      | Snake        | /snake.html      | Snake                           |
      | Calendar     | /calendar        | Calendar                        |
      | Swagger API  | /swagger-ui.html | WildflyExample API - Swagger UI |
      | Manage Users | /admin/users     | Create User                     |

  Scenario Outline: Quick links open their features and return home
    When the user opens Quick link "<link>"
    Then the application page "<path>" has title "<title>"
    When the user returns through the Home link
    Then the home page is displayed

    Examples:
      | link         | path         | title        |
      | Calculator   | /calculator  | Calculator   |
      | Number Guess | /numberguess | Number Guess |
      | Snake        | /snake.html  | Snake        |
      | Calendar     | /calendar    | Calendar     |

  Scenario: Raw calculator Quick link explains the required operation
    When the user opens Quick link "Raw Calculator API (GET example)"
    Then the raw API reports its missing operation
    When the user returns to the preceding page
    Then the home page is displayed

  Scenario Outline: Calculator operations and validation
    When the user opens "Calculator" from dashboard navigation
    And the user calculates "<first>" and "<second>" using "<operation>"
    Then the form reports "<result>"

    Examples:
      | first | second | operation | result                       |
      | 2.5   | 3.25   | add       | Result: 5.75                 |
      | 2     | 5      | sub       | Result: -3                   |
      | -4    | 3      | mul       | Result: -12                  |
      | 7     | 2      | div       | Result: 3.5                  |
      | 7     | 0      | div       | Error: Division by zero      |
      | abc   | 3      | add       | Error: Invalid number format |

  Scenario: Snake canvas and game controls
    When the user opens "Snake" from dashboard navigation
    And the user exercises Snake start pause resume and restart
    And the user returns through the Home link
    Then the home page is displayed

  Scenario: Swagger and the calculator API
    When the user opens "Swagger API" from dashboard navigation
    Then Swagger renders the calculator operations
    And the calculator API returns success and rejects division by zero
    When the user returns through the Home link
    Then the home page is displayed

  Scenario: Administration requires username and password
    When the user opens "Manage Users" from dashboard navigation
    And the user submits the empty create-user form
    Then the form reports "Username and password are required."

  Scenario: Administration rejects duplicate usernames without a server error
    When the user opens "Manage Users" from dashboard navigation
    And the user attempts to create the already configured username
    Then the form reports "Failed to create user (maybe already exists)."

  Scenario: Invalid Number Guess input does not consume attempts
    When the user opens "Number Guess" from dashboard navigation
    And the user submits invalid Number Guess inputs

  Scenario: Number Guess records an attempt and resets its session state
    When the user opens "Number Guess" from dashboard navigation
    And the user guesses fifty and starts a new game

  @calendar
  Scenario: Calendar views and month navigation
    When the user opens "Calendar" from dashboard navigation
    And the user visits every calendar view and navigates months

  @calendar
  Scenario Outline: Calendar events persist through edit export and deletion
    When the user opens "Calendar" from dashboard navigation
    And the user creates a unique "<frequency>" calendar event with <count> occurrences
    Then calendar search shows <count> occurrences of that event
    When the user edits that "<frequency>" calendar event and exports it
    Then calendar search shows <count> occurrences of that event
    When the user deletes that event or its entire series
    Then that event is absent and its deletion is in calendar history

    Examples:
      | frequency | count |
      | NONE      | 1     |
      | DAILY     | 3     |